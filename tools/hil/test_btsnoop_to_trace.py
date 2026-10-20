"""Checks btsnoop_to_trace.py on a synthetic snoop log. Run: python -m unittest test_btsnoop_to_trace"""
import os
import shutil
import struct
import tempfile
import unittest

import btsnoop_to_trace as bt

S3R, S3, OTHER = "EC:06:D8:0D:86:F8", "E8:EB:1B:71:3E:36", "11:22:33:44:55:66"
T0 = 1_792_476_000_000_000  # Unix time, us (2026-10-20 06:00 UTC)
PHONE_UTC_OFFSET_US = 8 * 3600 * 1_000_000  # a phone at UTC+8


def mac_le(mac):
    return bytes(reversed(bytes.fromhex(mac.replace(":", ""))))


class Snoop:
    def __init__(self):
        self.records = []
        self.t = T0

    def add(self, received, data, dt_ms=1):
        self.t += dt_ms * 1000
        self.records.append((self.t, received, bytes(data)))

    def event(self, code, params):
        self.add(True, bytes([bt.H4_EVENT, code, len(params)]) + params)

    def le_connected(self, handle, mac):
        self.event(0x3E, bytes([0x01, 0x00]) + struct.pack("<H", handle) + b"\x00\x00" + mac_le(mac) + b"\x00" * 7)

    def classic_connected(self, handle, mac):
        self.event(0x03, b"\x00" + struct.pack("<H", handle) + mac_le(mac) + b"\x01\x00")

    def acl(self, received, handle, cid, payload, split=None):
        pdu = struct.pack("<HH", len(payload), cid) + payload
        parts = [pdu] if split is None else [pdu[:split], pdu[split:]]
        for n, part in enumerate(parts):
            pb = 0x2 if n == 0 else 0x1
            self.add(received, bytes([bt.H4_ACL]) + struct.pack("<HH", handle | (pb << 12), len(part)) + part)

    def write(self, path, clock_offset_us=PHONE_UTC_OFFSET_US):
        """Stamped in the phone's local time by default, as Android does."""
        with open(path, "wb") as f:
            f.write(b"btsnoop\0" + struct.pack(">II", 1, 1002))
            for t, received, data in self.records:
                f.write(struct.pack(">IIIIq", len(data), len(data), 1 if received else 0, 0,
                                    t + bt.BTSNOOP_EPOCH_DELTA_US + clock_offset_us))
                f.write(data)


def rfcomm(dlci, payload, credit=None):
    control = bt.RFCOMM_UIH | (0x10 if credit is not None else 0)
    n = len(payload)
    length = bytes([(n << 1) | 1]) if n < 128 else bytes([(n << 1) & 0xFE, n >> 7])
    return bytes([(dlci << 2) | 0x3, control]) + length + (bytes([credit]) if credit is not None else b"") \
        + payload + b"\x9A"


class ExtractTest(unittest.TestCase):

    def setUp(self):
        s = Snoop()
        # BLE: the Shimmer3R, and another device that must be left out.
        s.le_connected(0x40, S3R)
        s.le_connected(0x41, OTHER)
        s.acl(False, 0x40, bt.L2CAP_ATT, b"\x02" + struct.pack("<H", 251))  # MTU request
        s.acl(True, 0x40, bt.L2CAP_ATT, b"\x03" + struct.pack("<H", 251))
        s.acl(False, 0x40, bt.L2CAP_ATT, b"\x12" + struct.pack("<H", 0x0F) + b"\x01\x00")  # CCCD on
        s.acl(False, 0x40, bt.L2CAP_ATT, b"\x52" + struct.pack("<H", 0x11) + b"\x03")  # TX command
        s.acl(True, 0x40, bt.L2CAP_ATT, b"\x1B" + struct.pack("<H", 0x0E) + bytes(range(40)), split=10)
        s.acl(True, 0x41, bt.L2CAP_ATT, b"\x1B" + struct.pack("<H", 0x0E) + b"\xAA")
        # Classic: the Shimmer3 over RFCOMM.
        s.classic_connected(0x0C, S3)
        s.acl(False, 0x0C, bt.L2CAP_SIGNALLING, bytes([0x02, 0x07]) + struct.pack("<HHH", 4, 0x0003, 0x0040))
        s.acl(True, 0x0C, bt.L2CAP_SIGNALLING, bytes([0x03, 0x07]) + struct.pack("<HHHHH", 8, 0x0041, 0x0040, 0, 0))
        s.acl(False, 0x0C, 0x0041, rfcomm(0, b"\x83\x01"))  # multiplexer control: left out
        s.acl(False, 0x0C, 0x0041, rfcomm(2, b"\x07"))
        s.acl(True, 0x0C, 0x0040, rfcomm(2, bytes(range(130)), credit=5))
        s.acl(True, 0x0C, 0x0040, rfcomm(2, b"", credit=3))  # credits only: no data
        self.tmp = tempfile.mkdtemp()
        self.log = os.path.join(self.tmp, "btsnoop_hci.log")
        s.write(self.log)
        self.t = s.records
        self.snoop = s

    def tearDown(self):
        shutil.rmtree(self.tmp, ignore_errors=True)

    def run_extract(self, *extra):
        out = os.path.join(self.tmp, "out.bytes.log")
        code = bt.main([self.log, "--out", out, "--utc-offset-min", "480", *extra])
        with open(out, encoding="utf-8") as f:
            lines = [l.rstrip("\n") for l in f if not l.startswith("#")]
        return code, lines

    def test_ble_session_of_one_unit(self):
        code, lines = self.run_extract("--mac", S3R)
        self.assertEqual(0, code)
        data = [l[14:] for l in lines if " TX " in l or " RX " in l]
        self.assertEqual(["TX h17 [0x03]",
                          "RX h14 [" + " ".join(f"0x{b:02X}" for b in range(40)) + "]"], data)
        self.assertTrue(any(f"connected {S3R} over BLE handle=64 mtu=251" in l for l in lines))
        # Times of day are the phone's: 06:00 UTC + 8 h.
        self.assertTrue(lines[-1].startswith("14:00:00."))

    def test_log_stamped_in_utc(self):
        self.snoop.write(self.log, clock_offset_us=0)
        rx_time_ms = self.t[7][0] // 1000
        _, lines = self.run_extract("--mac", S3R, "--snoop-clock", "utc", "--from-ms", str(rx_time_ms))
        rx = [l for l in lines if " RX " in l]
        self.assertEqual(1, len(rx))
        self.assertTrue(rx[0].startswith("14:00:00."))

    def test_classic_session_keeps_payloads_and_drops_control(self):
        _, lines = self.run_extract("--mac", S3)
        data = [l[14:] for l in lines if " TX " in l or " RX " in l]
        self.assertEqual(["TX r2 [0x07]",
                          "RX r2 [" + " ".join(f"0x{b:02X}" for b in range(130)) + "]"], data)

    def test_time_window(self):
        # A notification counts at the record that completes it: its second fragment, record 7. The
        # command before it is record 5, 2 ms earlier. Both ends of the window are inclusive.
        rx_time_ms = self.t[7][0] // 1000
        _, lines = self.run_extract("--mac", S3R, "--from-ms", str(rx_time_ms))
        self.assertEqual(1, sum(1 for l in lines if " RX " in l))
        self.assertEqual(0, sum(1 for l in lines if " TX " in l))
        _, lines = self.run_extract("--mac", S3R, "--to-ms", str(rx_time_ms - 1))
        self.assertEqual(0, sum(1 for l in lines if " RX " in l))
        self.assertEqual(1, sum(1 for l in lines if " TX " in l))

    def test_notification_write_filter_is_per_connection(self):
        # The unit reconnects on the same ACL handle; on the new connection a 2-byte command to
        # h15 must not be taken for the notification-on write of the old connection's h14.
        s = Snoop()
        s.le_connected(0x40, S3R)
        s.acl(True, 0x40, bt.L2CAP_ATT, b"\x1B" + struct.pack("<H", 14) + b"\x00")
        s.event(0x05, b"\x00" + struct.pack("<H", 0x40) + b"\x13")  # Disconnection Complete
        s.le_connected(0x40, S3R)
        s.acl(False, 0x40, bt.L2CAP_ATT, b"\x52" + struct.pack("<H", 15) + b"\x01\x00")
        s.write(self.log)
        _, lines = self.run_extract("--mac", S3R)
        self.assertEqual(["RX h14 [0x00]", "TX h15 [0x01 0x00]"],
                         [l[14:] for l in lines if " TX " in l or " RX " in l])

    def test_matches_shimmer_logandstream_line_layout(self):
        # Its loader takes the time from the first 12 characters and the bytes between [ ].
        _, lines = self.run_extract("--mac", S3R)
        for l in (l for l in lines if " TX " in l or " RX " in l):
            h, m, s_ms = l[:12].split(":")
            s, ms = s_ms.split(".")
            self.assertTrue(all(x.isdigit() for x in (h, m, s, ms)))
            self.assertEqual("  ", l[12:14])


if __name__ == "__main__":
    unittest.main()
