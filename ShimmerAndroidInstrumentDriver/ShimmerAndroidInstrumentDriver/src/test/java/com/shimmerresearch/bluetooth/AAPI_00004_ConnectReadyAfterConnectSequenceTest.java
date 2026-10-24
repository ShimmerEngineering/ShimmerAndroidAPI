package com.shimmerresearch.bluetooth;

import static org.junit.Assert.*;

import android.content.Context;
import android.os.Handler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.junit.Test;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

/**
 * DEV-1275: the connect sequence reads the unit's status part way through, before
 * the battery, the calibration dump and the inquiry. Shimmer's
 * logAndStreamStatusChanged() moved the state on that reply, from CONNECTING to
 * CONNECTED, or to SDLOGGING (which also sets mIsInitialised) for a unit that is
 * logging. So the app was told the unit was ready while the rest of the sequence
 * was still queued, and whatever it did next went out in among it. The Java HIL
 * found it on the driver's ShimmerPC (DEV-1269): a configure sent then cut a
 * Shimmer3's calibration dump short and left its accel NaN.
 * <p>
 * The sequence's own end, inquiryDone() and then isReadyForStreaming(), is what
 * reports the unit ready. The driver fixed ShimmerPC (API_00031); Shimmer
 * overrides the status handler, so it needs the same fix.
 * <p>
 * No hardware: the status reply is read from an in-memory byte queue by the
 * driver's own parser, as the IO thread does while not streaming.
 */
public class AAPI_00004_ConnectReadyAfterConnectSequenceTest {

	/** Not docked, sensing or logging; RTC set */
	private static final byte STATUS_IDLE = 0x04;

	/** Sensing and SD logging; RTC set */
	private static final byte STATUS_SD_LOGGING = 0x0E;

	/** A Shimmer whose Bluetooth link is in memory, and which records every state it is set to */
	private static class QueueShimmer extends Shimmer {
		final Deque<Byte> rx = new ArrayDeque<Byte>();
		final List<BT_STATE> states = new ArrayList<BT_STATE>();

		QueueShimmer() {
			super((Handler) null, (Context) null);
		}

		void queue(byte... bytes) {
			for(byte b:bytes){
				rx.add(b);
			}
		}

		/** What the IO thread does with an in-stream response while not streaming, once it has read its ACK and INSTREAM_CMD_RESPONSE */
		void parseInstreamResponse() {
			processInstreamResponse(false);
		}

		/** The end of the connect sequence: the inquiry's reply, or the ACK to the SET_SENSORS_COMMAND that ends it */
		void endConnectSequence() {
			inquiryDone();
		}

		@Override
		public boolean setBluetoothRadioState(BT_STATE state) {
			states.add(state);
			return super.setBluetoothRadioState(state);
		}

		@Override
		protected int availableBytes() {
			return rx.size();
		}

		@Override
		protected byte[] readBytes(int numberofBytes) {
			// Shimmer's would block for bytes that have not arrived, and none will
			assertTrue("read " + numberofBytes + " bytes but only " + rx.size() + " had arrived", numberofBytes<=rx.size());
			byte[] bytes = new byte[numberofBytes];
			for(int i=0;i<numberofBytes;i++){
				bytes[i] = rx.poll();
			}
			return bytes;
		}

		@Override
		protected void writeBytes(byte[] data) {
		}
	}

	/** A Shimmer3 part way through its connect sequence */
	private static QueueShimmer connectingShimmer3() {
		QueueShimmer device = new QueueShimmer();
		device.setShimmerVersionObjectAndCreateSensorMap(new ShimmerVerObject(HW_ID.SHIMMER_3, FW_ID.LOGANDSTREAM, 1, 1, 5));
		device.setBluetoothRadioState(BT_STATE.CONNECTING);
		device.states.clear();
		return device;
	}

	/**
	 * The reply to GET_STATUS_COMMAND as the IO thread hands it on while not
	 * streaming: it has read the ACK and INSTREAM_CMD_RESPONSE, and the driver's
	 * parser reads the rest, [STATUS_RESPONSE][status]
	 */
	private static void receiveStatusReply(QueueShimmer device, byte status) {
		device.mCurrentCommand = ShimmerObject.GET_STATUS_COMMAND;
		device.queue(ShimmerObject.STATUS_RESPONSE, status);
		device.parseInstreamResponse();
		assertTrue("the whole reply is read", device.rx.isEmpty());
	}

	private static void assertStillConnecting(QueueShimmer device) {
		assertEquals("no state is set before the sequence ends", new ArrayList<BT_STATE>(), device.states);
		assertEquals(BT_STATE.CONNECTING, device.getBluetoothRadioState());
		assertFalse("not reported initialised before the sequence ends", device.isInitialised());
	}

	@Test
	public void statusReplyWhileConnectingLeavesTheUnitConnecting() {
		QueueShimmer device = connectingShimmer3();
		receiveStatusReply(device, STATUS_IDLE);
		assertFalse("the status is still decoded", device.isSDLogging());
		assertStillConnecting(device);

		device.endConnectSequence();
		assertEquals("ready once the sequence ends", Arrays.asList(BT_STATE.CONNECTED), device.states);
		assertTrue(device.isInitialised());
	}

	@Test
	public void statusReplyWhileConnectingToALoggingUnitLeavesTheUnitConnecting() {
		QueueShimmer device = connectingShimmer3();
		receiveStatusReply(device, STATUS_SD_LOGGING);
		assertTrue("the status is still decoded", device.isSDLogging());
		assertStillConnecting(device);

		device.endConnectSequence();
		assertEquals("ready once the sequence ends", Arrays.asList(BT_STATE.SDLOGGING), device.states);
		assertTrue(device.isInitialised());
	}

	/** Once connected, a status reply still moves the state */
	@Test
	public void statusReplyAfterConnectingStillChangesTheState() {
		QueueShimmer device = connectingShimmer3();
		device.endConnectSequence();
		assertEquals(BT_STATE.CONNECTED, device.getBluetoothRadioState());

		receiveStatusReply(device, STATUS_SD_LOGGING);
		assertEquals(BT_STATE.SDLOGGING, device.getBluetoothRadioState());

		receiveStatusReply(device, STATUS_IDLE);
		assertEquals(BT_STATE.CONNECTED, device.getBluetoothRadioState());
	}
}
