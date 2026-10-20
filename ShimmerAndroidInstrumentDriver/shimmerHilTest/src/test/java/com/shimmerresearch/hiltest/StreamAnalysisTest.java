package com.shimmerresearch.hiltest;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/** The HIL verdicts are only as good as this arithmetic, so it is checked without a phone. */
public class StreamAnalysisTest {

	private static List<StreamAnalysis.Sample> stream(double rateHz, int count, int... droppedIndices) {
		List<StreamAnalysis.Sample> samples = new ArrayList<>();
		double period = 1000.0 / rateHz;
		outer:
		for (int i = 0; i < count; i++) {
			for (int d : droppedIndices) {
				if (d == i) {
					continue outer;
				}
			}
			samples.add(new StreamAnalysis.Sample(i * period, 1000 + i * period, 0, 0, 9.81));
		}
		return samples;
	}

	@Test
	public void cleanStreamHasNoLoss() {
		StreamAnalysis a = StreamAnalysis.analyse(stream(256, 2560), 256);
		assertEquals(2560, a.received);
		assertEquals(0, a.missing);
		assertEquals(0, a.nonIncreasing);
		assertEquals(256, a.effectiveRateHz, 0.01);
		assertEquals(9.81, a.accelMagnitudeMean, 1e-9);
	}

	@Test
	public void countsEachDroppedPacket() {
		StreamAnalysis a = StreamAnalysis.analyse(stream(256, 1000, 100, 500, 501, 502), 256);
		assertEquals(996, a.received);
		assertEquals(4, a.missing);
		assertEquals(2, a.gaps.size());
		assertEquals(256, a.effectiveRateHz, 0.01);
	}

	@Test
	public void attributesLossToTheMomentItHappened() {
		StreamAnalysis a = StreamAnalysis.analyse(stream(100, 1000, 300), 100);
		// Packet 300 is missing; packet 299 arrived at 2990 ms.
		assertEquals(1, a.missingNear(new double[] {3000}, 500));
		assertEquals(0, a.missingNear(new double[] {9000}, 500));
	}

	@Test
	public void flagsTimestampsThatGoBackwards() {
		List<StreamAnalysis.Sample> s = stream(100, 10);
		s.add(5, new StreamAnalysis.Sample(45, 1000, 0, 0, 9.81));
		StreamAnalysis a = StreamAnalysis.analyse(s, 100);
		assertEquals(1, a.nonIncreasing);
	}

	@Test
	public void toleratesJitterBelowHalfAPeriod() {
		List<StreamAnalysis.Sample> s = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			s.add(new StreamAnalysis.Sample(i * 10, 1000 + i * 10 + (i % 2 == 0 ? 0 : 4), 0, 0, 9.81));
		}
		assertEquals(0, StreamAnalysis.analyse(s, 100).missing);
	}
}
