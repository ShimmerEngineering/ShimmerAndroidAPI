package com.shimmerresearch.androidradiodriver;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import it.gerdavax.easybluetooth.BtSocket;
import it.gerdavax.easybluetooth.LocalDevice;
import it.gerdavax.easybluetooth.RemoteDevice;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.comms.serialPortInterface.ByteLevelDataCommListener;
import com.shimmerresearch.comms.serialPortInterface.SerialPortComm;
import com.shimmerresearch.comms.serialPortInterface.ShimmerSerialEventCallback;
import com.shimmerresearch.comms.serialPortInterface.ByteLevelDataComm;
import com.shimmerresearch.driver.DeviceException;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;

public class ShimmerSerialPortAndroid extends SerialPortComm {
	//generic UUID for serial port protocol
	private UUID mSPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
		
	public String mBluetoothAddress = "";
	private BluetoothAdapter mBluetoothAdapter = null;
	public BT_STATE mState = BT_STATE.DISCONNECTED;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private final BluetoothAdapter mAdapter;
	private DataInputStream mInStream;
	private OutputStream mOutStream=null;
	
	public ShimmerSerialPortAndroid(String bluetoothAddress){
		mBluetoothAddress = bluetoothAddress;
		mAdapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	@Override
	public void connect() throws DeviceException {

			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mBluetoothAddress);

			// Cancel any thread attempting to make a connection
			if (mState == BT_STATE.CONNECTING) {
				if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
			}
			// Cancel any thread currently running a connection
			if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

			// Start the thread to connect with the given device
			mConnectThread = new ConnectThread(device);
			mConnectThread.start();
			 
		
	
		
	}

	@Override
	public void disconnect() throws DeviceException {
		// TODO Auto-generated method stub
		if (mConnectThread != null) {
			mConnectThread.cancel(); 
			mConnectThread = null;
		}
		if (mConnectedThread != null) {
			try {
				wait(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mConnectedThread.cancel(); 
			mConnectedThread = null;
		}

	
	}

	@Override
	public void closeSafely() throws DeviceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearSerialPortRxBuffer() throws DeviceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void txBytes(byte[] buf) throws DeviceException {
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			//if (mState != BT_STATE.CONNECTED && mState != BT_STATE.STREAMING) return;
			if (mState == BT_STATE.DISCONNECTED ) return;
			r = mConnectedThread;
		}
		// Perform the write unsynchronized
		r.write(buf);
	}

	@Override
	public byte[] rxBytes(int numBytes) throws DeviceException {
		// TODO Auto-generated method stub
		byte[] b = new byte[numBytes];
		try {
			//mIN.read(b,0,numberofBytes);
			mInStream.readFully(b,0,numBytes);
			return(b);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Connection Lost");
			e.printStackTrace();
		}	
		return null;
	}

	@Override
	public void registerSerialPortRxEventCallback(
			ShimmerSerialEventCallback shimmerSerialEventCallback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isSerialPortReaderStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setVerboseMode(boolean verboseMode, boolean isDebugMode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean bytesAvailableToBeRead() throws DeviceException {
		try {
			if (mInStream!=null){
				if (mInStream.available()!=0){
					return true;
				} else {
					return false;
				}
			} else {
				System.out.println("IN STREAM NULL");
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e);
			return false;
		}
	
	}

	@Override
	public int availableBytes() throws DeviceException {
		try {
			return mInStream.available();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public boolean isConnected() {
		if (mConnectThread!=null){
			return mConnectThread.isConnected();
		}
		return false;
	}
	


	/**
	 * This thread runs while attempting to make an outgoing connection
	 * with a device. It runs straight through; the connection either
	 * succeeds or fails.
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mSocket;
		private final BluetoothDevice mDevice;

		public ConnectThread(BluetoothDevice device) {
			mDevice = device;
			BluetoothSocket tmp = null;
			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				tmp = device.createInsecureRfcommSocketToServiceRecord(mSPP_UUID); // If your device fails to pair try: device.createInsecureRfcommSocketToServiceRecord(mSPP_UUID)
			} catch (IOException e) {
				eventDeviceDisconnected();

			}
			mSocket = tmp;
		}

		public void run() {
			setName("ConnectThread");

			// Always cancel discovery because it will slow down a connection
			mAdapter.cancelDiscovery();

			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mSocket.connect();
			} catch (IOException connectException) {
				eventDeviceDisconnected();
				// Unable to connect; close the socket and get out
				try {
					mSocket.close();
				} catch (IOException closeException) { }
				return;
			}
			// Reset the ConnectThread because we're done
			synchronized (ShimmerSerialPortAndroid.this) {
				mConnectThread = null;
			}
			// Start the connected thread
			//connected(mmSocket, mmDevice);
			// Cancel the thread that completed the connection
			if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
			// Cancel any thread currently running a connection
			if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
			// Start the thread to manage the connection and perform transmissions
			mConnectedThread = new ConnectedThread(mSocket);
		}
		
		public void cancel() {
			try {
				mSocket.close();
			} catch (IOException e) { }
		}
		
		public boolean isConnected(){
			return mSocket.isConnected();
		}
	}
	/**
	 * This thread runs during a connection with a remote device.
	 * It handles all incoming and outgoing transmissions.
	 */
	private class ConnectedThread{
		private BluetoothSocket mSocket=null;
		public ConnectedThread(BluetoothSocket socket) {

			mSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				eventDeviceDisconnected();
			}

			//mInStream = new BufferedInputStream(tmpIn);
			mInStream = new DataInputStream(tmpIn);
			mOutStream = tmpOut;
			mState = BT_STATE.CONNECTED;
			eventDeviceConnected();
		}
		/**
		 * Write to the connected OutStream.
		 * @param buffer  The bytes to write
		 */
		private void write(byte[] buffer) {
			try {
				mOutStream.write(buffer);
				
			} catch (IOException e) {
				
			}
		}

		public void cancel() {
			if(mInStream != null) {
				try {
					mInStream.close();
				} catch (IOException e) {}
			}
			if(mOutStream != null) {
				try {
					mOutStream.close();
				} catch (IOException e) {}
			}
			if(mSocket != null) {
				try {
					mSocket.close();
				} catch (IOException e) {}
			}
		}
	}







	@Override
	public boolean isDisonnected() {
		// TODO Auto-generated method stub
		return false;
	}

	public ShimmerVerObject getShimmerVerObject() {
		// TODO Auto-generated method stub
		return null;
	}
	
}

	



