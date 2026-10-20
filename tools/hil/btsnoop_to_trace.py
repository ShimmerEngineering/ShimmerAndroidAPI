#!/usr/bin/env python3
"""
Turns an Android Bluetooth HCI snoop log (btsnoop_hci.log) into the byte-trace format that
shimmer-logandstream's replay tests read (tests/data/*.bytes.log): one line per write to the unit
(TX) or notification from it (RX), with its time of day.

    HH:MM:SS.mmm  TX h<att handle> [0x.. 0x..]     BLE: ATT write command or request
    HH:MM:SS.mmm  RX h<att handle> [0x.. 0x..]     BLE: ATT notification
    HH:MM:SS.mmm  TX r<dlci> [0x.. 0x..]           Classic: RFCOMM payload
    HH:MM:SS.mmm  RX r<dlci> [0x.. 0x..]

Classic payloads are kept as the radio delivered them, chunk by chunk, so a parser bug that
depends on how bytes arrive (a bulk read splitting a packet) replays the same way.

Standard library only. Usage:

    python btsnoop_to_trace.py btsnoop_hci.log --mac EC:06:D8:0D:86:F8 --out run.bytes.log
        [--from-ms EPOCH_MS] [--to-ms EPOCH_MS] [--utc-offset-min 480] [--snoop-clock local|utc]
        [--title TEXT]

--utc-offset-min is the phone's offset from UTC. Android writes the snoop log in local time, so
it is needed both to place the --from-ms/--to-ms window and to print times of day.
"""
import argparse
import datetime
import struct
import sys

# btsnoop counts microseconds from 0000-01-01; this is 1970-01-01 on that scale.
BTSNOOP_EPOCH_DELTA_US = 0x00DCDDB30F2F8000

H4_COMMAND, H4_ACL, H4_SCO, H4_EVENT = 1, 2, 3, 4
L2CAP_SIGNALLING, L2CAP_ATT = 0x0001, 0x0004
PSM_RFCOMM = 0x0003
ATT_WRITE_REQ, ATT_NOTIFY, ATT_WRITE_CMD = 0x12, 0x1B, 0x52
ATT_MTU_REQ, ATT_MTU_RSP = 0x02, 0x03
RFCOMM_UIH = 0xEF


class Record:
    __slots__ = ("time_us", "received", "data")

    def __init__(self, time_us, received, data):
        self.time_us = time_us  # Unix time, microseconds
        self.received = received  # True: controller to host (from the unit)
        self.data = data


def read_btsnoop(path, clock_offset_us=0):
    """clock_offset_us: how far the log's clock is ahead of UTC. Android stamps its snoop log in
    the phone's local time, so on a phone at UTC+8 it is 8 hours."""
    with open(path, "rb") as f:
        header = f.read(16)
        if len(header) < 16 or header[:8] != b"btsnoop\0":
            raise ValueError(f"{path}: not a btsnoop file")
        _version, datalink = struct.unpack(">II", header[8:16])
        if datalink != 1002:
            raise ValueError(f"{path}: datalink {datalink}, expected 1002 (HCI UART, H4)")
        while True:
            head = f.read(24)
            if len(head) < 24:
                return
            _orig_len, incl_len, flags, _drops, ts = struct.unpack(">IIIIq", head)
            data = f.read(incl_len)
            if len(data) < incl_len:
                return
            yield Record(ts - BTSNOOP_EPOCH_DELTA_US - clock_offset_us, bool(flags & 1), data)


def mac_from_le(b):
    return ":".join(f"{x:02X}" for x in reversed(b))


class Link:
    """One ACL connection: its address, and how to read the data on it."""

    def __init__(self, handle, mac, le):
        self.handle = handle
        self.mac = mac
        self.le = le
        self.rfcomm_cids = set()
        self.pending_rfcomm = {}  # signalling id -> source CID of a PSM 3 connection request
        self.reassembly = {}  # direction -> bytearray of an L2CAP PDU in progress
        self.mtu = None


class Extractor:
    def __init__(self, mac=None):
        self.mac = mac.upper() if mac else None
        self.links = {}  # ACL handle -> Link (current connection on that handle)
        self.entries = []  # (time_us, tx, token, payload)
        self.connections = []  # (time_us, mac, le, handle)

    def feed(self, rec):
        if not rec.data:
            return
        kind = rec.data[0]
        if kind == H4_EVENT:
            self._event(rec)
        elif kind == H4_ACL:
            self._acl(rec)

    def _event(self, rec):
        d = rec.data
        if len(d) < 3:
            return
        code, params = d[1], d[3:]
        if code == 0x03 and len(params) >= 9 and params[0] == 0:  # Connection Complete (BR/EDR)
            handle = struct.unpack_from("<H", params, 1)[0] & 0x0FFF
            self._connected(rec.time_us, handle, mac_from_le(params[3:9]), False)
        elif code == 0x3E and params and params[0] in (0x01, 0x0A) and len(params) >= 12:
            if params[1] == 0:  # LE Connection Complete / Enhanced Connection Complete
                handle = struct.unpack_from("<H", params, 2)[0] & 0x0FFF
                self._connected(rec.time_us, handle, mac_from_le(params[6:12]), True)
        elif code == 0x05 and len(params) >= 3:  # Disconnection Complete
            handle = struct.unpack_from("<H", params, 1)[0] & 0x0FFF
            self.links.pop(handle, None)

    def _connected(self, time_us, handle, mac, le):
        self.links[handle] = Link(handle, mac, le)
        self.connections.append((time_us, mac, le, handle))

    def _acl(self, rec):
        d = rec.data
        if len(d) < 5:
            return
        hf, length = struct.unpack_from("<HH", d, 1)
        handle, pb = hf & 0x0FFF, (hf >> 12) & 0x3
        link = self.links.get(handle)
        if link is None:
            # A connection made before the log started: its address is unknown.
            link = self.links[handle] = Link(handle, None, None)
        payload = d[5:5 + length]
        key = rec.received
        if pb == 0x1:  # continuing fragment
            buf = link.reassembly.get(key)
            if buf is None:
                return
            buf.extend(payload)
        else:  # start of an L2CAP PDU
            buf = link.reassembly[key] = bytearray(payload)
        if len(buf) < 4:
            return
        l2len, cid = struct.unpack_from("<HH", buf, 0)
        if len(buf) < 4 + l2len:
            return
        pdu = bytes(buf[4:4 + l2len])
        del link.reassembly[key]
        self._l2cap(rec, link, cid, pdu)

    def _wanted(self, link):
        return self.mac is None or link.mac == self.mac

    def _l2cap(self, rec, link, cid, pdu):
        if cid == L2CAP_ATT:
            if link.le is None:
                link.le = True
            self._att(rec, link, pdu)
        elif cid == L2CAP_SIGNALLING:
            self._signalling(link, pdu)
        elif cid in link.rfcomm_cids:
            self._rfcomm(rec, link, pdu)

    def _att(self, rec, link, pdu):
        if not pdu:
            return
        op = pdu[0]
        if op in (ATT_MTU_REQ, ATT_MTU_RSP) and len(pdu) >= 3:
            mtu = struct.unpack_from("<H", pdu, 1)[0]
            link.mtu = mtu if link.mtu is None else min(link.mtu, mtu)
            return
        if not self._wanted(link) or len(pdu) < 3:
            return
        handle = struct.unpack_from("<H", pdu, 1)[0]
        value = pdu[3:]
        if op == ATT_NOTIFY and rec.received:
            self.entries.append((rec.time_us, False, f"h{handle}", value, link))
        elif op in (ATT_WRITE_CMD, ATT_WRITE_REQ) and not rec.received:
            self.entries.append((rec.time_us, True, f"h{handle}", value, link))

    def _signalling(self, link, pdu):
        # Several commands can share one signalling PDU.
        i = 0
        while i + 4 <= len(pdu):
            code, ident, length = pdu[i], pdu[i + 1], struct.unpack_from("<H", pdu, i + 2)[0]
            body = pdu[i + 4:i + 4 + length]
            if code == 0x02 and len(body) >= 4:  # Connection Request
                psm, scid = struct.unpack_from("<HH", body, 0)
                if psm == PSM_RFCOMM:
                    link.pending_rfcomm[ident] = scid
            elif code == 0x03 and len(body) >= 8:  # Connection Response
                dcid, scid, result = struct.unpack_from("<HHH", body, 0)
                if result == 0 and link.pending_rfcomm.get(ident) == scid:
                    link.rfcomm_cids.update((dcid, scid))
                    if link.le is None:
                        link.le = False
            i += 4 + length

    def _rfcomm(self, rec, link, pdu):
        if len(pdu) < 4 or not self._wanted(link):
            return
        dlci = pdu[0] >> 2
        control = pdu[1]
        if dlci == 0 or (control & ~0x10) != RFCOMM_UIH:
            return  # the multiplexer's control channel, or not a data frame
        # TS 07.10 length: one octet (EA = 1) up to 127, otherwise two octets holding len << 1 as a
        # little-endian 16-bit value. Only the first octet carries the EA bit; the second is len >> 7
        # whole (Linux: __len16 in net/bluetooth/rfcomm/core.c; Android: RFCOMM_PARSE_LEN_FIELD).
        if pdu[2] & 1:
            length, i = pdu[2] >> 1, 3
        else:
            length, i = (pdu[2] >> 1) | (pdu[3] << 7), 4
        if control & 0x10:
            i += 1  # a credit byte (credit-based flow control) precedes the data
        payload = pdu[i:i + length]
        if payload:
            self.entries.append((rec.time_us, not rec.received, f"r{dlci}", payload, link))


def drop_cccd_writes(entries):
    """A write that turns notifications on (to the descriptor after a notifying handle) is GATT
    housekeeping, not a command to the unit; the recorded traces leave it out. Handles belong to one
    connection, so a write is only matched against notifications on the same one."""
    notify_handles = {(id(e[4]), int(e[2][1:])) for e in entries if not e[1] and e[2].startswith("h")}
    return [e for e in entries
            if not (e[1] and e[2].startswith("h") and (id(e[4]), int(e[2][1:]) - 1) in notify_handles
                    and len(e[3]) == 2 and e[3][1] == 0 and e[3][0] in (0, 1, 2))]


def time_of_day(time_us, utc_offset_min):
    t = datetime.datetime.fromtimestamp(time_us / 1e6, datetime.timezone.utc)
    t += datetime.timedelta(minutes=utc_offset_min)
    return t.strftime("%H:%M:%S.") + f"{t.microsecond // 1000:03d}"


def main(argv=None):
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("log", nargs="+", help="btsnoop_hci.log (older file first, if it rotated)")
    ap.add_argument("--mac", help="only this unit's connection")
    ap.add_argument("--from-ms", type=int, help="Unix time, ms: drop what came before")
    ap.add_argument("--to-ms", type=int, help="Unix time, ms: drop what came after")
    ap.add_argument("--utc-offset-min", type=int, default=0, help="the phone's offset from UTC, in minutes")
    ap.add_argument("--snoop-clock", choices=("local", "utc"), default="local",
                    help="the clock the log is stamped in (Android: local)")
    ap.add_argument("--title", default="", help="first comment line")
    ap.add_argument("--out", required=True)
    args = ap.parse_args(argv)

    clock_offset_us = args.utc_offset_min * 60_000_000 if args.snoop_clock == "local" else 0
    ex = Extractor(args.mac)
    first = last = None
    for path in args.log:
        for rec in read_btsnoop(path, clock_offset_us):
            first = rec.time_us if first is None else first
            last = rec.time_us
            ex.feed(rec)
    lo = args.from_ms * 1000 if args.from_ms is not None else None
    hi = args.to_ms * 1000 if args.to_ms is not None else None
    entries = [e for e in drop_cccd_writes(ex.entries)
               if (lo is None or e[0] >= lo) and (hi is None or e[0] <= hi)]

    with open(args.out, "w", encoding="utf-8", newline="\n") as out:
        if args.title:
            out.write(f"# {args.title}\n")
        out.write("# Extracted from the phone's Bluetooth HCI snoop log by tools/hil/btsnoop_to_trace.py.\n")
        out.write("# TX = phone to unit, RX = unit to phone; h<n> is a BLE ATT handle, r<n> an RFCOMM channel (DLCI).\n")
        out.write("# Classic payloads are as the radio delivered them, not split into packets.\n")
        seen = set()
        for time_us, mac, le, handle in ex.connections:
            if (args.mac is None or mac == args.mac.upper()) and (lo is None or time_us >= lo - 30_000_000) \
                    and (hi is None or time_us <= hi):
                link_mtu = next((e[4].mtu for e in entries if e[4].handle == handle and e[4].mac == mac), None)
                key = (time_us, handle)
                if key not in seen:
                    seen.add(key)
                    mtu = f" mtu={link_mtu}" if link_mtu else ""
                    out.write(f"{time_of_day(time_us, args.utc_offset_min)}  connected {mac} "
                              f"over {'BLE' if le else 'Classic'} handle={handle}{mtu}\n")
        for time_us, tx, token, payload, _link in entries:
            data = " ".join(f"0x{b:02X}" for b in payload)
            out.write(f"{time_of_day(time_us, args.utc_offset_min)}  {'TX' if tx else 'RX'} {token} [{data}]\n")

    tx = [e for e in entries if e[1]]
    rx = [e for e in entries if not e[1]]
    span = ""
    if first is not None:
        span = (f"; the log covers {time_of_day(first, args.utc_offset_min)}"
                f" to {time_of_day(last, args.utc_offset_min)}")
    print(f"{args.out}: {len(tx)} TX ({sum(len(e[3]) for e in tx)} bytes), "
          f"{len(rx)} RX ({sum(len(e[3]) for e in rx)} bytes){span}")
    return 0 if entries else 3


if __name__ == "__main__":
    sys.exit(main())
