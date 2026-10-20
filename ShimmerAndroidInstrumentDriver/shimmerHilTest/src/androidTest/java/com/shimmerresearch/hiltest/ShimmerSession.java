package com.shimmerresearch.hiltest;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import com.clj.fastble.BleManager;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.bluetooth.BluetoothProgressReportPerDevice;
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
	private volatile BluetoothProgressReportPerDevice.OperationState configureResult;
	private volatile int stateMessages;
	private final java.util.Set<Integer> otherMessagesSeen = new java.util.HashSet<>();

	ShimmerSession(Context context, String mac) throws Exception {
		this.mac = mac;
		callbackThread.start();
		Handler handler = new Handler(callbackThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				onMessage(msg);
			}
		};
		// On the main thread, as an app does: the manager creates a Handler on whatever thread
		// constructs it, which fails on the instrumentation thread (no Looper).
		final ShimmerBluetoothManagerAndroid[] created = new ShimmerBluetoothManagerAndroid[1];
		final Exception[] failure = new Exception[1];
		InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
			try {
				BleManager.getInstance().init((Application) context.getApplicationContext());
				created[0] = new ShimmerBluetoothManagerAndroid(context, handler);
			} catch (Exception e) {
				failure[0] = e;
			}
		});
		if (failure[0] != null) {
			callbackThread.quitSafely();
			throw failure[0];
		}
		manager = created[0];
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
				lastState = state;
				stateMessages++;
				event("state " + state);
			}
		} else if (msg.what == ShimmerBluetooth.MSG_IDENTIFIER_PROGRESS_REPORT_PER_DEVICE
				&& msg.obj instanceof BluetoothProgressReportPerDevice) {
			// The manager's own verdict on an operation such as configureShimmer.
			BluetoothProgressReportPerDevice p = (BluetoothProgressReportPerDevice) msg.obj;
			if (p.mCurrentOperationBtState == BT_STATE.CONFIGURING
					&& p.mOperationState != BluetoothProgressReportPerDevice.OperationState.PENDING
					&& p.mOperationState != BluetoothProgressReportPerDevice.OperationState.INPROGRESS) {
				configureResult = p.mOperationState;
				event("configure progress: " + p.mOperationState);
			}
		} else if (msg.what == ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET) {
			if (recording && msg.obj instanceof ObjectCluster) {
				recordPacket((ObjectCluster) msg.obj);
			}
		} else if (otherMessagesSeen.add(msg.what)) {
			event("message " + msg.what + (msg.obj == null ? "" : " " + msg.obj.getClass().getSimpleName()));
		}
	}

	private void recordPacket(ObjectCluster oc) {
		StreamAnalysis.Sample s = new StreamAnalysis.Sample(nowMs(),
				cal(oc, Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP),
				cal(oc, Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X),
				cal(oc, Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y),
				cal(oc, Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z));
		synchronized (samples) {
			samples.add(s);
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

	interface Call {
		void run() throws Exception;
	}

	/** Set to the first library call that never returned; nothing more is attempted after it. */
	volatile String hung;

	private final java.util.concurrent.ExecutorService calls = java.util.concurrent.Executors.newCachedThreadPool(r -> {
		Thread t = new Thread(r, "ShimmerHilCall");
		t.setDaemon(true);
		return t;
	});

	/**
	 * Runs one API call with a time limit, so a call that blocks cannot hang the run. A call that
	 * never returns is recorded with every thread's stack (the evidence a phone without root cannot
	 * otherwise give) and returns false; so does one that throws.
	 */
	boolean call(String what, long timeoutMs, Call call) {
		if (hung != null) {
			event("skipped " + what + ": " + hung + " never returned");
			return false;
		}
		event(what);
		java.util.concurrent.Future<?> f = calls.submit(() -> {
			call.run();
			return null;
		});
		try {
			f.get(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
			return true;
		} catch (java.util.concurrent.TimeoutException e) {
			hung = what;
			event(what + " did not return within " + timeoutMs + " ms; thread dump follows");
			dumpThreads();
			return false;
		} catch (java.util.concurrent.ExecutionException e) {
			event(what + " threw " + e.getCause());
			return false;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
	}

	private void dumpThreads() {
		for (java.util.Map.Entry<Thread, StackTraceElement[]> t : Thread.getAllStackTraces().entrySet()) {
			StringBuilder b = new StringBuilder("  thread \"" + t.getKey().getName() + "\" " + t.getKey().getState());
			for (StackTraceElement frame : t.getValue()) {
				b.append("\n      at ").append(frame);
			}
			event(b.toString());
		}
	}

	/** Connects over Bluetooth Classic and waits until the API has read the device's configuration. */
	boolean connect(long timeoutMs) throws InterruptedException {
		if (!call("connect", 10000, () -> manager.connectShimmerThroughBTAddress(mac, "Shimmer",
				ShimmerBluetoothManagerAndroid.BT_TYPE.BT_CLASSIC))) {
			return false;
		}
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
		configureResult = null;
		final int stateMessagesBefore = stateMessages;
		if (!call(String.format(Locale.ROOT, "configure accel only at %.1f Hz", rateHz), 10000,
				() -> manager.configureShimmer(clone))) {
			return false;
		}
		// Done when the operation has finished: either the manager's progress report (not every
		// version forwards it to the Handler) or the fresh "Ready" the unit posts when the operation
		// ends, with the requested rate read back. The status timer is stopped during an operation,
		// so that "Ready" comes from the configuration.
		boolean done = waitFor(() -> state() == BT_STATE.CONNECTED
				&& (configureResult != null
					|| (stateMessages > stateMessagesBefore && Math.abs(device().getSamplingRateShimmer() - rateHz) < 0.5)),
				timeoutMs);
		event(String.format(Locale.ROOT, "configure %s (%s): rate on device %.2f Hz",
				done ? "finished" : "timed out", configureResult != null ? configureResult : "no progress report",
				device().getSamplingRateShimmer()));
		return done && (configureResult == null || configureResult == BluetoothProgressReportPerDevice.OperationState.SUCCESS);
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

	boolean startStreaming(long timeoutMs) throws InterruptedException {
		return call("startStreaming", 10000, () -> device().startStreaming())
				&& waitFor(() -> device().isStreaming(), timeoutMs);
	}

	boolean stopStreaming(long timeoutMs) throws InterruptedException {
		return call("stopStreaming", 10000, () -> device().stopStreaming())
				&& waitFor(() -> !device().isStreaming(), timeoutMs);
	}

	/** Returns once the call has returned; the caller waits for the state it expects. */
	boolean startSdLogging() {
		return call("startSDLogging", 10000, () -> device().startSDLogging());
	}

	boolean stopSdLogging() {
		return call("stopSDLogging", 10000, () -> device().stopSDLogging());
	}

	boolean readBattery() {
		return call("readBattery", 10000, () -> device().readBattery());
	}

	boolean disconnect(long timeoutMs) throws InterruptedException {
		final ShimmerBluetooth d = device();
		if (d != null && !call("disconnect", 10000, d::disconnect)) {
			return false;
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
			// After a hung call the API is in an unknown state; leave the unit for the runner's
			// force-stop rather than queue more calls behind the stuck one.
			BT_STATE s = state();
			if (hung == null && s != null && s != BT_STATE.DISCONNECTED && s != BT_STATE.CONNECTION_LOST) {
				if (device() != null && device().isStreaming()) {
					stopStreaming(5000);
				}
				disconnect(10000);
			}
		} finally {
			callbackThread.quitSafely();
			calls.shutdownNow();
		}
	}
}
