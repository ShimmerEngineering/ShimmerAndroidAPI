package com.shimmerresearch.hiltest;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Everything one scenario run found, written as JSON (plus a CSV of samples) to the app's
 * external files dir, hil/, for tools/hil/run-android-hil.ps1 to pull.
 */
final class HilReport {

	private final Context context;
	private final String scenario;
	private final String mac;
	private final JSONObject json = new JSONObject();
	private final List<String> failures = new ArrayList<>();
	private List<StreamAnalysis.Sample> samples;

	HilReport(Context context, String scenario, String mac, Bundle args) throws Exception {
		this.context = context;
		this.scenario = scenario;
		this.mac = mac;
		json.put("scenario", scenario);
		json.put("mac", mac);
		json.put("started", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT).format(new Date()));
		JSONObject a = new JSONObject();
		for (String key : args.keySet()) {
			a.put(key, String.valueOf(args.get(key)));
		}
		json.put("args", a);
	}

	void put(String key, Object value) {
		try {
			json.put(key, value);
		} catch (Exception e) {
			Log.w(ShimmerSession.TAG, "report: " + key + ": " + e);
		}
	}

	void putStream(String key, StreamAnalysis a) {
		try {
			JSONObject o = new JSONObject();
			o.put("received", a.received);
			o.put("missing", a.missing);
			o.put("lossPercent", a.lossPercent());
			o.put("nonIncreasing", a.nonIncreasing);
			o.put("effectiveRateHz", a.effectiveRateHz);
			o.put("accelMagnitudeMean", a.accelMagnitudeMean);
			o.put("accelMagnitudeStd", a.accelMagnitudeStd);
			o.put("gaps", a.gaps.size());
			json.put(key, o);
		} catch (Exception e) {
			Log.w(ShimmerSession.TAG, "report: " + key + ": " + e);
		}
	}

	/** Every gap, with when it arrived, so a loss can be matched to what the test was doing. */
	void putGaps(StreamAnalysis a) {
		try {
			JSONArray gaps = new JSONArray();
			for (StreamAnalysis.Gap g : a.gaps) {
				JSONObject o = new JSONObject();
				o.put("afterReceivedMs", Math.round(g.afterReceivedMs));
				o.put("afterTimestampMs", g.afterTimestampMs);
				o.put("missing", g.missing);
				gaps.put(o);
			}
			json.put("gapList", gaps);
		} catch (Exception e) {
			Log.w(ShimmerSession.TAG, "report: gapList: " + e);
		}
	}

	void samples(List<StreamAnalysis.Sample> samples) {
		this.samples = samples;
	}

	/** Records a failed check; the test fails at the end with every one of them. */
	void check(boolean ok, String failure) {
		if (!ok) {
			failures.add(failure);
			Log.e(ShimmerSession.TAG, scenario + " " + mac + " FAIL: " + failure);
		}
	}

	List<String> failures() {
		return failures;
	}

	void write(List<String> events) {
		try {
			json.put("passed", failures.isEmpty());
			json.put("failures", new JSONArray(failures));
			json.put("events", new JSONArray(events));
			File dir = new File(context.getExternalFilesDir(null), "hil");
			if (!dir.exists() && !dir.mkdirs()) {
				throw new IllegalStateException("cannot create " + dir);
			}
			String base = scenario + "_" + mac.replace(":", "") + "_"
					+ new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(new Date());
			try (Writer w = new OutputStreamWriter(new FileOutputStream(new File(dir, base + ".json")), StandardCharsets.UTF_8)) {
				w.write(json.toString(2));
			}
			if (samples != null) {
				try (Writer w = new OutputStreamWriter(new FileOutputStream(new File(dir, base + ".csv")), StandardCharsets.UTF_8)) {
					w.write("received_ms,timestamp_ms,accel_x,accel_y,accel_z\n");
					for (StreamAnalysis.Sample s : samples) {
						w.write(String.format(Locale.ROOT, "%.0f,%.3f,%.4f,%.4f,%.4f\n",
								s.receivedMs, s.timestampMs, s.accelX, s.accelY, s.accelZ));
					}
				}
			}
			Log.i(ShimmerSession.TAG, "report written: " + new File(dir, base + ".json"));
		} catch (Exception e) {
			Log.e(ShimmerSession.TAG, "could not write report: " + e);
		}
	}
}
