package com.shimmerresearch.bluetooth;

import static org.junit.Assert.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.junit.Test;

import com.shimmerresearch.androidradiodriver.Shimmer3BLEAndroid;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.FW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails.HW_ID;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

/**
 * DEV-1275 over BLE: Shimmer3BLEAndroid told the app the unit was ready before its connect
 * sequence had run. It sent a CONNECTED state message as soon as the BLE notifications were on,
 * before initialize() queued the sequence, and a status reply part way through the sequence moved
 * the state to SDLOGGING for a logging unit, which also sets mIsInitialised. The Android HIL saw a
 * Shimmer3R reported ready 2.7 s before the sequence ended, with driver 0.11.11_beta: the driver's
 * fix is in ShimmerPC, and Shimmer3BLEAndroid overrides the status handler.
 * <p>
 * The sequence's own end, inquiryDone() and then isReadyForStreaming(), now reports the unit
 * ready, as SDLOGGING when it is logging. The message sent when the notifications come on can't be
 * reached without a phone; the Android HIL's connectReady checks it.
 * <p>
 * No hardware: the status reply is read from an in-memory byte queue by the driver's own parser,
 * as the IO thread does while not streaming.
 */
public class AAPI_00006_BleConnectReadyAfterConnectSequenceTest {

	/** Not docked, sensing or logging; RTC set */
	private static final byte STATUS_IDLE = 0x04;

	/** Sensing and SD logging; RTC set */
	private static final byte STATUS_SD_LOGGING = 0x0E;

	/** A Shimmer3BLEAndroid whose BLE link is in memory, and which records every state it is set to */
	private static class QueueShimmer extends Shimmer3BLEAndroid {
		final Deque<Byte> rx = new ArrayDeque<Byte>();
		final List<BT_STATE> states = new ArrayList<BT_STATE>();

		QueueShimmer() {
			super("E7:47:AA:7D:2F:31");
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

		/** The end of the connect sequence: the inquiry's reply */
		void endConnectSequence() {
			inquiryDone();
		}

		@Override
		public boolean setBluetoothRadioState(BT_STATE state) {
			states.add(state);
			return super.setBluetoothRadioState(state);
		}

		@Override
		protected boolean bytesAvailableToBeRead() {
			return !rx.isEmpty();
		}

		@Override
		protected int availableBytes() {
			return rx.size();
		}

		@Override
		protected byte[] readBytes(int numberofBytes) {
			assertTrue("read " + numberofBytes + " bytes but only " + rx.size() + " had arrived", numberofBytes<=rx.size());
			byte[] bytes = new byte[numberofBytes];
			for(int i=0;i<numberofBytes;i++){
				bytes[i] = rx.poll();
			}
			return bytes;
		}

		@Override
		public void writeBytes(byte[] data) {
		}
	}

	/** A Shimmer3R over BLE part way through its connect sequence */
	private static QueueShimmer connectingShimmer3r() {
		QueueShimmer device = new QueueShimmer();
		device.setShimmerVersionObjectAndCreateSensorMap(new ShimmerVerObject(HW_ID.SHIMMER_3R, FW_ID.LOGANDSTREAM, 1, 1, 22));
		device.setBluetoothRadioState(BT_STATE.CONNECTING);
		device.states.clear();
		return device;
	}

	/** A Shimmer3R's second status byte: USB not plugged in */
	private static final byte STATUS_USB_UNPLUGGED = 0x00;

	/**
	 * The reply to GET_STATUS_COMMAND as the IO thread hands it on while not streaming: it has read
	 * the ACK and INSTREAM_CMD_RESPONSE, and the driver's parser reads the rest,
	 * [STATUS_RESPONSE][status][USB status]
	 */
	private static void receiveStatusReply(QueueShimmer device, byte status) {
		device.mCurrentCommand = ShimmerObject.GET_STATUS_COMMAND;
		device.queue(ShimmerObject.STATUS_RESPONSE, status, STATUS_USB_UNPLUGGED);
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
		QueueShimmer device = connectingShimmer3r();
		device.setIsConnected(true);
		receiveStatusReply(device, STATUS_IDLE);
		assertFalse("the status is still decoded", device.isSDLogging());
		assertStillConnecting(device);

		device.endConnectSequence();
		assertEquals("ready once the sequence ends", Arrays.asList(BT_STATE.CONNECTED), device.states);
		assertTrue(device.isInitialised());
	}

	@Test
	public void statusReplyWhileConnectingToALoggingUnitLeavesTheUnitConnecting() {
		QueueShimmer device = connectingShimmer3r();
		receiveStatusReply(device, STATUS_SD_LOGGING);
		assertTrue("the status is still decoded", device.isSDLogging());
		assertStillConnecting(device);

		device.endConnectSequence();
		assertEquals("ready once the sequence ends, as logging", Arrays.asList(BT_STATE.SDLOGGING), device.states);
		assertTrue(device.isInitialised());
	}

	/** Once connected, a status reply still moves the state */
	@Test
	public void statusReplyAfterConnectingStillChangesTheState() {
		QueueShimmer device = connectingShimmer3r();
		device.endConnectSequence();
		assertEquals(BT_STATE.CONNECTED, device.getBluetoothRadioState());

		receiveStatusReply(device, STATUS_SD_LOGGING);
		assertEquals(BT_STATE.SDLOGGING, device.getBluetoothRadioState());

		receiveStatusReply(device, STATUS_IDLE);
		assertEquals(BT_STATE.CONNECTED, device.getBluetoothRadioState());
	}
}
