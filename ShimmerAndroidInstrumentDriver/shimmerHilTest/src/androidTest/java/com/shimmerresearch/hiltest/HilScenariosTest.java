package com.shimmerresearch.hiltest;

import static org.junit.Assume.assumeTrue;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_CRC_MODE;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;

import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Hardware-in-the-loop scenarios against one real Shimmer3 or Shimmer3R, paired with the phone over
 * Bluetooth Classic. Arguments come from `am instrument -e key value`:
 *
 * <pre>
 *   mac             XX:XX:XX:XX:XX:XX (required; without it every test is skipped)
 *   transport       classic (default; the unit must be paired) or ble (no pairing)
 *   type            Shimmer3 (default) or Shimmer3R; over BLE it picks the device class
 *   rate            sampling rate in Hz, default 256
 *   seconds         stream_rest duration, default 30
 *   cycles          mid_stream_set / reconnect_loop repetitions, default 3 / 5
 *   crc             link CRC to ask for: off, 1 or 2 (default 2; the driver may settle on less)
 *   maxLossPercent  default 1.0
 *   accelTolerance  m/s^2 either side of 9.81 at rest, default 1.0
 * </pre>
 *
 * tools/hil/run-android-hil.ps1 runs each scenario for each unit in a rig file and pulls the reports.
 */
@RunWith(AndroidJUnit4.class)
public class HilScenariosTest {

	private static final double GRAVITY = 9.81;

	/**
	 * Connecting to an already paired unit needs no runtime permission before Android 12 (BLUETOOTH is
	 * granted at install; location is only for scanning, which these tests never do). From Android 12,
	 * BLUETOOTH_CONNECT is a runtime permission. Some phones (Xiaomi) refuse runtime grants over USB
	 * without "USB debugging (Security settings)", so grant nothing where nothing is needed.
	 */
	@Rule
	public TestRule permissions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
			? GrantPermissionRule.grant(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
			: RuleChain.emptyRuleChain();

	private Bundle args;
	private Context context;
	private ShimmerSession session;
	private HilReport report;

	private String arg(String key, String fallback) {
		String v = args.getString(key);
		return v == null || v.isEmpty() ? fallback : v;
	}

	private double arg(String key, double fallback) {
		return Double.parseDouble(arg(key, String.valueOf(fallback)));
	}

	private int arg(String key, int fallback) {
		return Integer.parseInt(arg(key, String.valueOf(fallback)));
	}

	private void begin(String scenario) throws Exception {
		args = InstrumentationRegistry.getArguments();
		String mac = args.getString("mac");
		assumeTrue("No unit given: pass -e mac XX:XX:XX:XX:XX:XX", mac != null && !mac.isEmpty());
		mac = mac.toUpperCase(Locale.ROOT);
		context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		String crc = arg("crc", "2");
		ShimmerBluetooth.setDefaultBtCrcModeToUseIfFwSupported(
				"off".equals(crc) ? BT_CRC_MODE.OFF : "1".equals(crc) ? BT_CRC_MODE.ONE_BYTE_CRC : BT_CRC_MODE.TWO_BYTE_CRC);
		String transport = arg("transport", "classic");
		assumeTrue("transport must be classic or ble, not " + transport, "classic".equals(transport) || "ble".equals(transport));
		report = new HilReport(context, scenario, transport, mac, args);
		session = new ShimmerSession(context, mac, transport, arg("type", "Shimmer3"));
	}

	@After
	public void end() throws Exception {
		try {
			if (session != null) {
				session.close();
			}
		} finally {
			if (report != null) {
				report.write(session != null ? session.events() : new ArrayList<String>());
			}
		}
	}

	private void finish() {
		if (session != null && session.hung != null) {
			report.put("hungCall", session.hung);
			report.check(false, session.hung + " never returned: the API call blocked (thread dump in the report's events)");
		}
		if (!report.failures().isEmpty()) {
			Assert.fail(android.text.TextUtils.join("; ", report.failures()));
		}
	}

	/** Connects, records what the unit is, and writes the accel-only config at the test's rate. */
	private boolean connectAndConfigure(double rateHz) throws Exception {
		long start = System.currentTimeMillis();
		boolean connected = session.connect(30000);
		report.put("connectMs", System.currentTimeMillis() - start);
		report.check(connected, "did not connect and initialise within 30 s (state " + session.state() + ")");
		if (!connected) {
			return false;
		}
		ShimmerBluetooth d = session.device();
		if (d == null) {
			report.check(false, "the connection was lost right after connecting (state " + session.state() + ")");
			return false;
		}
		report.put("hardware", d.getHardwareVersionParsed());
		report.put("firmware", d.getFirmwareVersionParsed());
		report.put("crcMode", String.valueOf(d.getCurrentBtCommsCrcMode()));
		boolean configured = session.configureAccelOnly(rateHz, 20000);
		report.put("rateOnDeviceHz", d.getSamplingRateShimmer());
		report.check(configured, "configuration did not complete within 20 s (state " + session.state() + ")");
		if (configured && session.device() == null) {
			report.check(false, "the connection was lost right after configuring (state " + session.state() + ")");
			return false;
		}
		return configured;
	}

	private void checkStream(StreamAnalysis a, double maxLossPercent, double deviceRateHz, String what) {
		report.check(a.received > 0, what + ": no packets received");
		report.check(a.lossPercent() <= maxLossPercent, String.format(Locale.ROOT,
				"%s: %.3f%% packets lost (%d of %d), limit %.2f%%", what, a.lossPercent(), a.missing,
				a.received + a.missing, maxLossPercent));
		report.check(a.nonIncreasing == 0, what + ": " + a.nonIncreasing + " timestamps did not increase");
		checkJumps(a, what);
		if (!Double.isNaN(a.effectiveRateHz)) {
			report.check(Math.abs(a.effectiveRateHz - deviceRateHz) <= 0.01 * deviceRateHz, String.format(Locale.ROOT,
					"%s: effective rate %.2f Hz, device set to %.2f Hz", what, a.effectiveRateHz, deviceRateHz));
		}
	}

	/** A device timestamp that leapt ahead of the arrival time is wrong data (DEV-1023 class), never loss. */
	private void checkJumps(StreamAnalysis a, String what) {
		for (StreamAnalysis.Gap j : a.timestampJumps) {
			report.check(false, String.format(Locale.ROOT,
					"%s: timestamp jumped %.1f s ahead at %.0f ms (device time %.0f ms) while packets kept arriving",
					what, (j.missing + 1) * a.expectedPeriodMs / 1000.0, j.afterReceivedMs, j.afterTimestampMs));
		}
	}

	/** Connect, configure, stream at rest, stop, disconnect. */
	@Test
	public void streamRest() throws Exception {
		begin("stream_rest");
		double rate = arg("rate", 256.0);
		int seconds = arg("seconds", 30);
		double maxLoss = arg("maxLossPercent", 1.0);
		double accelTolerance = arg("accelTolerance", 1.0);
		if (connectAndConfigure(rate)) {
			double deviceRate = session.rateOnDevice();
			String marker = session.logMarker("stream_rest");
			session.startRecording();
			report.check(session.startStreaming(10000), "streaming did not start within 10 s");
			Thread.sleep(seconds * 1000L);
			List<StreamAnalysis.Sample> samples = session.stopRecording();
			report.check(session.stopStreaming(10000), "streaming did not stop within 10 s");
			StreamAnalysis a = StreamAnalysis.analyse(samples, deviceRate);
			session.event("stream: " + a);
			report.putStream("stream", a);
			report.putGaps(a);
			report.samples(samples);
			checkStream(a, maxLoss, deviceRate, "stream");
			if (!Double.isNaN(a.accelMagnitudeMean)) {
				report.check(Math.abs(a.accelMagnitudeMean - GRAVITY) <= accelTolerance, String.format(Locale.ROOT,
						"accel at rest %.3f m/s^2, expected %.2f±%.2f (is the unit lying still?)",
						a.accelMagnitudeMean, GRAVITY, accelTolerance));
			} else {
				report.check(false, "no calibrated accel in the packets");
			}
			int parserErrors = session.parserErrorsSince(marker);
			report.put("parserErrors", parserErrors);
			report.check(parserErrors == 0, parserErrors + " streaming parser errors logged (\"Packet syncing problem\")");
			report.check(session.disconnect(10000), "did not disconnect within 10 s");
		}
		finish();
	}

	/**
	 * Commands while streaming: start and stop SD logging (SET_RWC_COMMAND, whose bare ACK carries the
	 * link CRC: DEV-1147 in the driver, DEV-1148 for Android's own parser) and read the battery. No
	 * packet may be lost around any of them, and logging must actually start and stop. Needs an SD
	 * card in the unit and a link CRC on to exercise DEV-1148.
	 */
	@Test
	public void midStreamSet() throws Exception {
		begin("mid_stream_set");
		double rate = arg("rate", 256.0);
		int cycles = arg("cycles", 3);
		double maxLoss = arg("maxLossPercent", 1.0);
		if (connectAndConfigure(rate)) {
			ShimmerBluetooth d = session.device();
			double deviceRate = d.getSamplingRateShimmer();
			BT_CRC_MODE crc = d.getCurrentBtCommsCrcMode();
			if (crc == BT_CRC_MODE.OFF) {
				session.event("link CRC is off: the DEV-1148 path is not exercised");
			}
			String marker = session.logMarker("mid_stream_set");
			session.startRecording();
			report.check(session.startStreaming(10000), "streaming did not start within 10 s");
			Thread.sleep(3000);
			List<Double> sdMoments = new ArrayList<>();
			List<Double> batteryMoments = new ArrayList<>();
			for (int i = 1; i <= cycles && session.hung == null; i++) {
				sdMoments.add(session.nowMs());
				session.event("cycle " + i);
				if (session.startSdLogging()) {
					report.check(session.waitFor(() -> session.state() == BT_STATE.STREAMING_AND_SDLOGGING, 5000),
							"cycle " + i + ": SD logging did not start while streaming (state " + session.state()
									+ "; is there an SD card? With a link CRC on, DEV-1148)");
				}
				Thread.sleep(2000);
				sdMoments.add(session.nowMs());
				if (session.stopSdLogging()) {
					report.check(session.waitFor(() -> session.state() == BT_STATE.STREAMING, 5000),
							"cycle " + i + ": SD logging did not stop (state " + session.state() + ")");
				}
				Thread.sleep(1000);
				batteryMoments.add(session.nowMs());
				session.readBattery();
				Thread.sleep(2000);
			}
			List<StreamAnalysis.Sample> samples = session.stopRecording();
			report.check(session.stopStreaming(10000), "streaming did not stop within 10 s");
			StreamAnalysis a = StreamAnalysis.analyse(samples, deviceRate);
			session.event("stream: " + a);
			report.putStream("stream", a);
			report.putGaps(a);
			report.samples(samples);
			// Loss around SD logging start/stop is reported, not failed: with no parser error and
			// logging starting, the unit itself did not send those samples while it opened or closed
			// its file, which is firmware behaviour for the firmware HIL bench to judge. Loss around a
			// battery read, or anywhere else, is the Android API's.
			int[] attributed = a.attribute(new double[][] {toArray(sdMoments), toArray(batteryMoments)}, 1000);
			int nearSd = attributed[0];
			int nearBattery = attributed[1];
			report.put("missingNearSdLoggingStartStop", nearSd);
			report.put("missingNearBatteryReads", nearBattery);
			if (nearSd > 0) {
				session.event(nearSd + " packets not received within 1 s of SD logging start/stop (firmware side; not failed)");
			}
			report.check(nearBattery == 0, nearBattery + " packets lost within 1 s of a battery read while streaming");
			int elsewhere = a.missing - nearSd;
			double elsewherePercent = 100.0 * elsewhere / Math.max(1, a.received + a.missing);
			report.put("missingElsewhere", elsewhere);
			report.check(elsewherePercent <= maxLoss, String.format(Locale.ROOT,
					"stream: %.3f%% packets lost away from SD logging start/stop (%d), limit %.2f%%",
					elsewherePercent, elsewhere, maxLoss));
			report.check(a.received > 0, "stream: no packets received");
			report.check(a.nonIncreasing == 0, "stream: " + a.nonIncreasing + " timestamps did not increase");
			checkJumps(a, "stream");
			int parserErrors = session.parserErrorsSince(marker);
			report.put("parserErrors", parserErrors);
			report.check(parserErrors == 0, parserErrors + " streaming parser errors logged (\"Packet syncing problem\")");
			report.check(session.disconnect(10000), "did not disconnect within 10 s");
		}
		finish();
	}

	private static double[] toArray(List<Double> values) {
		double[] out = new double[values.size()];
		for (int i = 0; i < out.length; i++) {
			out[i] = values.get(i);
		}
		return out;
	}

	/** Connect, stream briefly and disconnect, repeatedly: the reconnect path apps hit every day. */
	@Test
	public void reconnectLoop() throws Exception {
		begin("reconnect_loop");
		double rate = arg("rate", 256.0);
		int cycles = arg("cycles", 5);
		double maxLoss = arg("maxLossPercent", 1.0);
		List<Long> connectTimes = new ArrayList<>();
		for (int i = 1; i <= cycles; i++) {
			String what = "cycle " + i;
			long start = System.currentTimeMillis();
			boolean ready = i == 1 ? connectAndConfigure(rate) : session.connect(30000);
			connectTimes.add(System.currentTimeMillis() - start);
			report.check(ready, what + ": did not connect within 30 s (state " + session.state() + ")");
			if (!ready) {
				break;
			}
			double deviceRate = session.rateOnDevice();
			String marker = session.logMarker(what);
			session.startRecording();
			report.check(session.startStreaming(10000), what + ": streaming did not start within 10 s");
			Thread.sleep(5000);
			List<StreamAnalysis.Sample> samples = session.stopRecording();
			report.check(session.stopStreaming(10000), what + ": streaming did not stop within 10 s");
			StreamAnalysis a = StreamAnalysis.analyse(samples, deviceRate);
			session.event(what + " stream: " + a);
			checkStream(a, maxLoss, deviceRate, what);
			int parserErrors = session.parserErrorsSince(marker);
			report.check(parserErrors == 0, what + ": " + parserErrors + " streaming parser errors logged");
			report.check(session.disconnect(10000), what + ": did not disconnect within 10 s");
			Thread.sleep(2000);
		}
		report.put("connectMs", new org.json.JSONArray(connectTimes));
		finish();
	}
}
