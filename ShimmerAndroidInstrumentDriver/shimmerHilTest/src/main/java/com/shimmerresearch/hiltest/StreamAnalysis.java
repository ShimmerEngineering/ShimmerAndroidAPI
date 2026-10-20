package com.shimmerresearch.hiltest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Judges a recorded stream from the device timestamps alone. The checks mirror the firmware HIL
 * harness in log-and-stream-common Test/hil (DEV-1165): packet loss and non-increasing timestamps
 * from the driver's already-unwrapped timestamps, the effective sample rate, and accel at rest.
 * Pure Java, so it is unit-tested on the JVM (src/test) as well as used on the phone.
 */
public final class StreamAnalysis {

	/** One received packet. Times in ms; accel in m/s^2, NaN when the channel is absent. */
	public static final class Sample {
		public final double receivedMs;
		public final double timestampMs;
		public final double accelX, accelY, accelZ;

		public Sample(double receivedMs, double timestampMs, double accelX, double accelY, double accelZ) {
			this.receivedMs = receivedMs;
			this.timestampMs = timestampMs;
			this.accelX = accelX;
			this.accelY = accelY;
			this.accelZ = accelZ;
		}

		double accelMagnitude() {
			return Math.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
		}
	}

	/** A run of packets missing between two received ones. */
	public static final class Gap {
		public final double afterTimestampMs;
		public final double afterReceivedMs;
		public final int missing;

		Gap(double afterTimestampMs, double afterReceivedMs, int missing) {
			this.afterTimestampMs = afterTimestampMs;
			this.afterReceivedMs = afterReceivedMs;
			this.missing = missing;
		}
	}

	public final int received;
	public final int missing;
	public final int nonIncreasing;
	public final double expectedPeriodMs;
	public final double effectiveRateHz;
	public final double accelMagnitudeMean;
	public final double accelMagnitudeStd;
	public final List<Gap> gaps;

	private StreamAnalysis(int received, int missing, int nonIncreasing, double expectedPeriodMs,
			double effectiveRateHz, double accelMagnitudeMean, double accelMagnitudeStd, List<Gap> gaps) {
		this.received = received;
		this.missing = missing;
		this.nonIncreasing = nonIncreasing;
		this.expectedPeriodMs = expectedPeriodMs;
		this.effectiveRateHz = effectiveRateHz;
		this.accelMagnitudeMean = accelMagnitudeMean;
		this.accelMagnitudeStd = accelMagnitudeStd;
		this.gaps = gaps;
	}

	/**
	 * @param samples      in arrival order
	 * @param rateHz       the sampling rate the device was configured with
	 */
	public static StreamAnalysis analyse(List<Sample> samples, double rateHz) {
		double period = 1000.0 / rateHz;
		int missing = 0;
		int nonIncreasing = 0;
		List<Gap> gaps = new ArrayList<>();
		for (int i = 1; i < samples.size(); i++) {
			double step = samples.get(i).timestampMs - samples.get(i - 1).timestampMs;
			if (step <= 0) {
				nonIncreasing++;
			} else if (step > 1.5 * period) {
				int lost = (int) Math.round(step / period) - 1;
				missing += lost;
				gaps.add(new Gap(samples.get(i - 1).timestampMs, samples.get(i - 1).receivedMs, lost));
			}
		}
		double effectiveRate = Double.NaN;
		if (samples.size() > 1) {
			double span = samples.get(samples.size() - 1).timestampMs - samples.get(0).timestampMs;
			if (span > 0) {
				effectiveRate = (samples.size() - 1 + missing) * 1000.0 / span;
			}
		}
		double sum = 0, sumSq = 0;
		int n = 0;
		for (Sample s : samples) {
			double m = s.accelMagnitude();
			if (!Double.isNaN(m)) {
				sum += m;
				sumSq += m * m;
				n++;
			}
		}
		double mean = n > 0 ? sum / n : Double.NaN;
		double std = n > 1 ? Math.sqrt(Math.max(0, (sumSq - n * mean * mean) / (n - 1))) : Double.NaN;
		return new StreamAnalysis(samples.size(), missing, nonIncreasing, period, effectiveRate, mean, std, gaps);
	}

	/**
	 * Charges each gap to the kind of command nearest to it (by arrival time), if one is within
	 * windowMs. Commands that follow each other closely therefore never share a loss.
	 *
	 * @param momentsByKind moments (ms) of each kind of command
	 * @return packets lost per kind, in the same order
	 */
	public int[] attribute(double[][] momentsByKind, double windowMs) {
		int[] lost = new int[momentsByKind.length];
		for (Gap g : gaps) {
			int nearestKind = -1;
			double nearest = Double.MAX_VALUE;
			for (int k = 0; k < momentsByKind.length; k++) {
				for (double t : momentsByKind[k]) {
					double d = Math.abs(g.afterReceivedMs - t);
					if (d < nearest) {
						nearest = d;
						nearestKind = k;
					}
				}
			}
			if (nearestKind >= 0 && nearest <= windowMs) {
				lost[nearestKind] += g.missing;
			}
		}
		return lost;
	}

	/** Packets lost within windowMs (by arrival time) of any of the given moments. */
	public int missingNear(double[] momentsMs, double windowMs) {
		int total = 0;
		for (Gap g : gaps) {
			for (double t : momentsMs) {
				if (Math.abs(g.afterReceivedMs - t) <= windowMs) {
					total += g.missing;
					break;
				}
			}
		}
		return total;
	}

	public double lossPercent() {
		int expected = received + missing;
		return expected == 0 ? 0 : 100.0 * missing / expected;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT,
				"received=%d missing=%d (%.3f%%) nonIncreasing=%d effectiveRate=%.2fHz accel=%.3f±%.3f m/s^2 gaps=%s",
				received, missing, lossPercent(), nonIncreasing, effectiveRateHz, accelMagnitudeMean,
				accelMagnitudeStd, gapsSummary());
	}

	private String gapsSummary() {
		int shown = Math.min(gaps.size(), 5);
		String[] parts = new String[shown];
		for (int i = 0; i < shown; i++) {
			parts[i] = String.format(Locale.ROOT, "%d@%.0fms", gaps.get(i).missing, gaps.get(i).afterReceivedMs);
		}
		return Arrays.toString(parts) + (gaps.size() > shown ? "+" + (gaps.size() - shown) : "");
	}
}
