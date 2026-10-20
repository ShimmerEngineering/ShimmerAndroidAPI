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

	@Rule
	public GrantPermissionRule permissions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
			? GrantPermissionRule.grant(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
			: GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);

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
		report = new HilReport(context, scenario, mac, args);
		session = new ShimmerSession(context, mac);
	}

	@After
	public void end() throws Exception {
		if (session != null) {
			try {
				session.close();
			} finally {
				report.write(session.events());
			}
		}
	}

	private void finish() {
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
		report.put("hardware", d.getHardwareVersionParsed());
		report.put("firmware", d.getFirmwareVersionParsed());
		report.put("crcMode", String.valueOf(d.getCurrentBtCommsCrcMode()));
		boolean configured = session.configureAccelOnly(rateHz, 20000);
		report.put("rateOnDeviceHz", d.getSamplingRateShimmer());
		report.check(configured, "configuration did not complete within 20 s (state " + session.state() + ")");
		return configured;
	}

	private void checkStream(StreamAnalysis a, double maxLossPercent, double deviceRateHz, String what) {
		report.check(a.received > 0, what + ": no packets received");
		report.check(a.lossPercent() <= maxLossPercent, String.format(Locale.ROOT,
				"%s: %.3f%% packets lost (%d of %d), limit %.2f%%", what, a.lossPercent(), a.missing,
				a.received + a.missing, maxLossPercent));
		report.check(a.nonIncreasing == 0, what + ": " + a.nonIncreasing + " timestamps did not increase");
		if (!Double.isNaN(a.effectiveRateHz)) {
			report.check(Math.abs(a.effectiveRateHz - deviceRateHz) <= 0.01 * deviceRateHz, String.format(Locale.ROOT,
					"%s: effective rate %.2f Hz, device set to %.2f Hz", what, a.effectiveRateHz, deviceRateHz));
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
			double deviceRate = session.device().getSamplingRateShimmer();
			String marker = session.logMarker("stream_rest");
			session.startRecording();
			report.check(session.startStreaming(10000), "streaming did not start within 10 s");
			Thread.sleep(seconds * 1000L);
			List<StreamAnalysis.Sample> samples = session.stopRecording();
			report.check(session.stopStreaming(10000), "streaming did not stop within 10 s");
			StreamAnalysis a = StreamAnalysis.analyse(samples, deviceRate);
			session.event("stream: " + a);
			report.putStream("stream", a);
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
			List<Double> moments = new ArrayList<>();
			for (int i = 1; i <= cycles; i++) {
				moments.add(session.nowMs());
				session.event("cycle " + i + ": startSDLogging");
				d.startSDLogging();
				report.check(session.waitFor(() -> session.state() == BT_STATE.STREAMING_AND_SDLOGGING, 5000),
						"cycle " + i + ": SD logging did not start while streaming (state " + session.state()
								+ "; is there an SD card?)");
				Thread.sleep(2000);
				moments.add(session.nowMs());
				session.event("cycle " + i + ": stopSDLogging");
				d.stopSDLogging();
				report.check(session.waitFor(() -> session.state() == BT_STATE.STREAMING, 5000),
						"cycle " + i + ": SD logging did not stop (state " + session.state() + ")");
				Thread.sleep(1000);
				moments.add(session.nowMs());
				session.event("cycle " + i + ": readBattery");
				d.readBattery();
				Thread.sleep(2000);
			}
			List<StreamAnalysis.Sample> samples = session.stopRecording();
			report.check(session.stopStreaming(10000), "streaming did not stop within 10 s");
			StreamAnalysis a = StreamAnalysis.analyse(samples, deviceRate);
			session.event("stream: " + a);
			report.putStream("stream", a);
			double[] m = new double[moments.size()];
			for (int i = 0; i < m.length; i++) {
				m[i] = moments.get(i);
			}
			int lostNearCommands = a.missingNear(m, 1000);
			report.put("missingNearCommands", lostNearCommands);
			report.check(lostNearCommands == 0, lostNearCommands
					+ " packets lost within 1 s of a mid-stream command (DEV-1148 with CRC " + crc + ")");
			checkStream(a, maxLoss, deviceRate, "stream");
			int parserErrors = session.parserErrorsSince(marker);
			report.put("parserErrors", parserErrors);
			report.check(parserErrors == 0, parserErrors + " streaming parser errors logged (\"Packet syncing problem\")");
			report.check(session.disconnect(10000), "did not disconnect within 10 s");
		}
		finish();
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
			double deviceRate = session.device().getSamplingRateShimmer();
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
