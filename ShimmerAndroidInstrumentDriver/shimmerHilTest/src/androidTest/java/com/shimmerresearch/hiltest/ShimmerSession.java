package com.shimmerresearch.hiltest;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.AssembleShimmerConfig;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * One Shimmer, driven through the public Android API the way an app would: the Bluetooth manager,
 * a Handler for callbacks, and the ShimmerBluetooth calls. Records state changes and, while
 * recording, every data packet.
 */
final class ShimmerSession implements AutoCloseable {

	static final String TAG = "ShimmerHil";

	/** What the API logs (Log.d, tag "Shimmer") when its streaming parser loses its place. */
	private static final String[] PARSER_ERRORS = {"Packet syncing problem", "Unknown parsing error while streaming"};

	interface Condition {
		boolean holds();
	}

	final String mac;
	private final HandlerThread callbackThread = new HandlerThread("ShimmerHilCallbacks");
	private final ShimmerBluetoothManagerAndroid manager;
	private final long t0 = SystemClock.elapsedRealtime();
	private final List<String> events = new ArrayList<>();
	private final List<StreamAnalysis.Sample> samples = new ArrayList<>();
	private volatile boolean recording;
	private volatile BT_STATE lastState;
	private volatile boolean sawConfiguring;

	ShimmerSession(Context context, String mac) throws Exception {
		this.mac = mac;
		callbackThread.start();
		Handler handler = new Handler(callbackThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				onMessage(msg);
			}
		};
		BleManager.getInstance().init((Application) context.getApplicationContext());
		manager = new ShimmerBluetoothManagerAndroid(context, handler);
	}

	double nowMs() {
		return SystemClock.elapsedRealtime() - t0;
	}

	void event(String text) {
		String line = String.format(Locale.ROOT, "%9.0f %s", nowMs(), text);
		synchronized (events) {
			events.add(line);
		}
		Log.i(TAG, mac + " " + line);
	}

	List<String> events() {
		synchronized (events) {
			return new ArrayList<>(events);
		}
	}

	private void onMessage(Message msg) {
		if (msg.what == ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE) {
			BT_STATE state = null;
			if (msg.obj instanceof ObjectCluster) {
				state = ((ObjectCluster) msg.obj).mState;
			} else if (msg.obj instanceof CallbackObject) {
				state = ((CallbackObject) msg.obj).mState;
			}
			if (state != null) {
				if (state == BT_STATE.CONFIGURING) {
					sawConfiguring = true;
				}
				lastState = state;
				event("state " + state);
			}
		} else if (msg.what == ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET && recording && msg.obj instanceof ObjectCluster) {
			ObjectCluster oc = (ObjectCluster) msg.obj;
			StreamAnalysis.Sample s = new StreamAnalysis.Sample(nowMs(),
					cal(oc, Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP),
					cal(oc, Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X),
					cal(oc, Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y),
					cal(oc, Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z));
			synchronized (samples) {
				samples.add(s);
			}
		}
	}

	private static double cal(ObjectCluster oc, String channel) {
		Collection<FormatCluster> formats = oc.getCollectionOfFormatClusters(channel);
		FormatCluster c = (FormatCluster) ObjectCluster.returnFormatCluster(formats, "CAL");
		return c == null ? Double.NaN : c.mData;
	}

	ShimmerBluetooth device() {
		return (ShimmerBluetooth) manager.getShimmer(mac);
	}

	BT_STATE state() {
		ShimmerBluetooth d = device();
		return d == null ? lastState : d.getBluetoothRadioState();
	}

	boolean waitFor(Condition condition, long timeoutMs) throws InterruptedException {
		long deadline = SystemClock.elapsedRealtime() + timeoutMs;
		while (SystemClock.elapsedRealtime() < deadline) {
			if (condition.holds()) {
				return true;
			}
			Thread.sleep(50);
		}
		return condition.holds();
	}

	/** Connects over Bluetooth Classic and waits until the API has read the device's configuration. */
	boolean connect(long timeoutMs) throws InterruptedException {
		event("connect");
		manager.connectShimmerThroughBTAddress(mac, "Shimmer", ShimmerBluetoothManagerAndroid.BT_TYPE.BT_CLASSIC);
		return waitFor(() -> {
			ShimmerBluetooth d = device();
			return d != null && d.isInitialised() && d.getBluetoothRadioState() == BT_STATE.CONNECTED;
		}, timeoutMs);
	}

	/**
	 * Accel only (the low-noise accel on Shimmer3, the LSM6DSV's on Shimmer3R) at rateHz, written
	 * the way the examples do: a clone, AssembleShimmerConfig, then configureShimmer.
	 */
	boolean configureAccelOnly(double rateHz, long timeoutMs) throws InterruptedException {
		ShimmerDevice original = manager.getShimmer(mac);
		ShimmerDevice clone = original.deepClone();
		clone.disableAllSensors();
		if (clone.getHardwareVersion() == ShimmerVerDetails.HW_ID.SHIMMER_3) {
			clone.setSensorEnabledState(Configuration.Shimmer3.SENSOR_ID.SHIMMER_ANALOG_ACCEL, true);
		} else {
			clone.setSensorEnabledState(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM6DSV_ACCEL_LN, true);
		}
		clone.setShimmerAndSensorsSamplingRate(rateHz);
		AssembleShimmerConfig.generateSingleShimmerConfig(clone, Configuration.COMMUNICATION_TYPE.BLUETOOTH);
		sawConfiguring = false;
		event(String.format(Locale.ROOT, "configure accel only at %.1f Hz", rateHz));
		manager.configureShimmer(clone);
		boolean done = waitFor(() -> sawConfiguring && state() == BT_STATE.CONNECTED, timeoutMs);
		event(String.format(Locale.ROOT, "configured: rate on device %.2f Hz", device().getSamplingRateShimmer()));
		return done;
	}

	void startRecording() {
		synchronized (samples) {
			samples.clear();
		}
		recording = true;
	}

	List<StreamAnalysis.Sample> stopRecording() {
		recording = false;
		synchronized (samples) {
			return new ArrayList<>(samples);
		}
	}

	int sampleCount() {
		synchronized (samples) {
			return samples.size();
		}
	}

	boolean startStreaming(long timeoutMs) throws Exception {
		event("startStreaming");
		device().startStreaming();
		return waitFor(() -> device().isStreaming(), timeoutMs);
	}

	boolean stopStreaming(long timeoutMs) throws InterruptedException {
		event("stopStreaming");
		device().stopStreaming();
		return waitFor(() -> !device().isStreaming(), timeoutMs);
	}

	boolean disconnect(long timeoutMs) throws Exception {
		event("disconnect");
		ShimmerBluetooth d = device();
		if (d != null) {
			d.disconnect();
		}
		return waitFor(() -> {
			BT_STATE s = state();
			return s == null || s == BT_STATE.DISCONNECTED || s == BT_STATE.CONNECTION_LOST;
		}, timeoutMs);
	}

	/** Writes a marker to this process's log; parserErrorsSince(marker) counts lines after it. */
	String logMarker(String label) {
		String marker = "HIL-MARK " + label + " " + SystemClock.elapsedRealtimeNanos();
		Log.i(TAG, marker);
		return marker;
	}

	/** Parser errors this process logged after the marker. Apps may read their own process's log. */
	int parserErrorsSince(String marker) {
		int count = 0;
		try {
			java.lang.Process p = Runtime.getRuntime().exec(new String[] {"logcat", "-d", "--pid=" + Process.myPid()});
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			boolean afterMarker = false;
			for (String line; (line = r.readLine()) != null; ) {
				if (line.contains(marker)) {
					afterMarker = true;
				} else if (afterMarker) {
					for (String e : PARSER_ERRORS) {
						if (line.contains(e)) {
							count++;
						}
					}
				}
			}
			p.waitFor();
		} catch (Exception e) {
			event("could not read logcat: " + e);
			return -1;
		}
		return count;
	}

	@Override
	public void close() throws Exception {
		try {
			BT_STATE s = state();
			if (s != null && s != BT_STATE.DISCONNECTED && s != BT_STATE.CONNECTION_LOST) {
				if (device() != null && device().isStreaming()) {
					stopStreaming(5000);
				}
				disconnect(10000);
			}
		} finally {
			callbackThread.quitSafely();
		}
	}
}
