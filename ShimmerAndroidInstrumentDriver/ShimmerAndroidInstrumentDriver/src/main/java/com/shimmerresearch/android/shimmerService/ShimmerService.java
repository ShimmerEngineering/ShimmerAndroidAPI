//v0.2 -  8 January 2013

/*
 * Copyright (c) 2010, Shimmer Research, Ltd.
 * All rights reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:

 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of Shimmer Research, Ltd. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author Jong Chern Lim
 * @date   October, 2013
 */

//Future updates needed
//- the handler should be converted to static

package com.shimmerresearch.android.shimmerService;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.shimmerresearch.algorithms.Filter;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.Shimmer4Android;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.biophysicalprocessing.ECGtoHRAdaptive;
import com.shimmerresearch.biophysicalprocessing.PPGtoHRAlgorithm;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.tools.Logging;
import com.shimmerresearch.tools.PlotManagerAndroid;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ShimmerService extends Service {
	private static final String TAG = "ShimmerService";
    public Logging shimmerLog1 = null;
    private boolean mEnableLogging=false;
	private BluetoothAdapter mBluetoothAdapter = null;
	private final IBinder mBinder = new LocalBinder();
	public HashMap<String, Object> mMultiShimmer = new HashMap<String, Object>(7);
	public HashMap<String, Logging> mLogShimmer = new HashMap<String, Logging>(7);
	private Handler mHandlerGraph=null;
	private boolean mGraphing=false;
	public String mLogFileName="Default";
	Filter mFilter;
	Filter mLPFilterECG;
	Filter mHPFilterECG;
	private double[] mLPFcECG = {51.2};
	private double[] mHPFcECG = {0.5};
	PPGtoHRAlgorithm mPPGtoHR;
	ECGtoHRAdaptive mECGtoHR;
	private int mNumberOfBeatsToAvg = 2;
	private int mNumberOfBeatsToAvgECG = 2;
	private int mECGTrainingInterval = 10;
	double[] mLPFc = {5};
	private boolean mPPGtoHREnabled = false;
	private boolean mECGtoHREnabled = false;
	private boolean mConvertGSRtoSiemens = true;
	private String mPPGtoHRSignalName = Configuration.Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A13;
	private String mECGtoHRSignalName = Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT;
	public PlotManagerAndroid mPlotManager;

	private ShimmerBluetoothManagerAndroid btManager;


	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public boolean isGSRtoSiemensEnabled(){
		return mConvertGSRtoSiemens;
	}

	@Override
	public void onCreate() {
		Toast.makeText(this, "Shimmer Service Created", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onCreate");

		try {
			btManager = new ShimmerBluetoothManagerAndroid(this, mHandler);
		} catch (Exception e) {
			Log.e(TAG, "ERROR! " + e);
			Toast.makeText(this, "Error! Could not create Bluetooth Manager!", Toast.LENGTH_LONG).show();
		}
	}

	public class LocalBinder extends Binder {
        public ShimmerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ShimmerService.this;
        }
    }

	public void setPPGtoHRSignal(String ppgtoHR){
		mPPGtoHRSignalName = ppgtoHR;
	}

	public void setECGtoHRSignal(String ecgtoHR){
		mECGtoHRSignalName = ecgtoHR;
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "Shimmer Service Stopped", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");

		btManager.disconnectAllDevices();

/*		TODO:
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			stemp.stop();
		}
*/

	}

	public void disconnectAllDevices(){

		btManager.disconnectAllDevices();

/*		TODO:
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			ShimmerDevice stemp=(ShimmerDevice) iterator.next();
			if (stemp instanceof Shimmer) {
				((Shimmer) stemp).stop();
			}
//			else if (stemp instanceof Shimmer4Android){
//				try {
//					((Shimmer4Android) stemp).mShimmerRadioHWLiteProtocol.disconnect();
//				} catch(Exception e){
//					e.printStackTrace();
//				}
//			}
		}
*/
		mMultiShimmer.clear();
		mLogShimmer.clear();
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

	@Override
	public void onStart(Intent intent, int startid) {
		Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();

		Log.d(TAG, "onStart");

	}

	public void enablePPGtoHR(String bluetoothAddress, boolean enable){
		if (enable){
			double sR = getSamplingRate(bluetoothAddress);
			mPPGtoHR = new PPGtoHRAlgorithm(sR, mNumberOfBeatsToAvg, true);
			try {
				mFilter = new Filter(Filter.LOW_PASS, sR,mLPFc);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mPPGtoHREnabled = enable;
	}

	public void enableECGtoHR(String bluetoothAddress, boolean enable){
		if (enable){
			double sR = getSamplingRate(bluetoothAddress);
			mECGtoHR = new ECGtoHRAdaptive(sR);
	    	try {
				mLPFilterECG = new Filter(Filter.LOW_PASS, sR, mLPFcECG);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	    	try {
				mHPFilterECG = new Filter(Filter.HIGH_PASS, sR, mHPFcECG);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mECGtoHREnabled = enable;
	}

	public boolean isPPGtoHREnabled(){
		return mPPGtoHREnabled;
	}

	public boolean isECGtoHREnabled(){
		return mECGtoHREnabled;
	}

	public void connectShimmer(final String bluetoothAddress, final String selectedDevice){

		btManager.connectShimmerTroughBTAddress(bluetoothAddress);

/*		TODO:
		Log.d("Shimmer","net Connection");
		Shimmer shimmerDevice=new Shimmer(this, mHandler, selectedDevice, true);
		mMultiShimmer.remove(bluetoothAddress);
		if (mMultiShimmer.get(bluetoothAddress)==null){
			mMultiShimmer.put(bluetoothAddress,shimmerDevice);
			((Shimmer) mMultiShimmer.get(bluetoothAddress)).connect(bluetoothAddress,"default");
		}
*/


//		mMultiShimmer.remove(bluetoothAddress);
//		if (mMultiShimmer.get(bluetoothAddress)==null) {
//			final Old_ShimmerSerialPortAndroid sspa = new Old_ShimmerSerialPortAndroid(bluetoothAddress);
//			//sspa = new ShimmerSerialPortJssc("COM89", SerialPort.BAUDRATE_115200);
//			sspa.setByteLevelDataCommListener(new ByteLevelDataCommListener() {
//				@Override
//				public void eventConnected() {
//					ShimmerVerObject svo = sspa.getShimmerVerObject();
//					ShimmerDevice shimmerDeviceNew = null;
//					if (svo.getHardwareVersion() == ShimmerVerDetails.HW_ID.SHIMMER_3 || svo.getHardwareVersion() == ShimmerVerDetails.HW_ID.SHIMMER_2R ) {
//						shimmerDeviceNew = initializeNewShimmer3(sspa, selectedDevice);
//						mMultiShimmer.put(bluetoothAddress,shimmerDeviceNew);
//					} else if (svo.getHardwareVersion() == ShimmerVerDetails.HW_ID.SHIMMER_4_SDK) {
//						shimmerDeviceNew = initializeNewShimmer4(sspa, selectedDevice);
//						shimmerDeviceNew.setMacIdFromUart(bluetoothAddress);
//						mMultiShimmer.put(bluetoothAddress,shimmerDeviceNew);
//					}
//				}
//
//				@Override
//				public void eventDisconnected() {
//					System.out.println("Shimmer Disconnected");
//					mHandler.obtainMessage(ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE, -1, -1, new ObjectCluster(selectedDevice,bluetoothAddress,BT_STATE.DISCONNECTED)).sendToTarget();
//				}
//			});
//			try {
//				sspa.connect();
//			} catch (DeviceException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}
//
//	private Shimmer initializeNewShimmer3(ByteLevelDataComm bldc,String selectedDevice) {
//		Old_ShimmerSerialPortAndroid ssjc = (Old_ShimmerSerialPortAndroid) bldc;
//		Shimmer shimmer = new Shimmer(this, mHandler,selectedDevice,true);
//		return initializeShimmer3(ssjc, shimmer);
//	}
//
//	private Shimmer initializeShimmer3(Old_ShimmerSerialPortAndroid ssja, Shimmer shimmer) {
//		shimmer.setRadio(ssja.mConnectedThread.mSocket);
//		shimmer.addCommunicationRoute(Configuration.COMMUNICATION_TYPE.BLUETOOTH);
//		return shimmer;
//	}
//	private Shimmer4Android initializeShimmer4(ByteLevelDataComm bldc, Shimmer4Android shimmer4) {
//		shimmer4.setRadio(new ShimmerRadioProtocol(bldc, new LiteProtocol()));
//		shimmer4.addCommunicationRoute(Configuration.COMMUNICATION_TYPE.BLUETOOTH);
//		return shimmer4;
//	}
//
//	private Shimmer4 initializeNewShimmer4(ByteLevelDataComm bldc, String selectedDevice) {
//		Shimmer4Android shimmer = new Shimmer4Android(mHandler);
//		shimmer.setShimmerUserAssignedName(selectedDevice);
//		return initializeShimmer4(bldc, shimmer);
//	}


	public void onStop(){
		Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");

		btManager.disconnectAllDevices();

/*		TODO:
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			stemp.stop();
		}
*/
	}



	public void toggleAllLEDS(){
		//TODO: Move to Bluetooth Manager class?
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING){
				stemp.toggleLed();
			}
		}
	}

	//TODO: Remove This
	public void clickToggle() {
		btManager.toggleLED();
	}


	  public final Handler mHandler = new Handler() {
	        public void handleMessage(Message msg) {
	            switch (msg.what) { // handlers have a what identifier which is used to identify the type of msg
	            case ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET:
	            	if ((msg.obj instanceof ObjectCluster)){	// within each msg an object can be include, objectclusters are used to represent the data structure of the shimmer device
	            	    ObjectCluster objectCluster =  (ObjectCluster) msg.obj;
						try {

							mPlotManager.filterDataAndPlot((ObjectCluster) msg.obj);

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	            	    //Filter Signal
	            	    //PPG to HR
	            	    if (mPPGtoHREnabled){
	            	    	Collection<FormatCluster> dataFormats = objectCluster.getCollectionOfFormatClusters(mPPGtoHRSignalName);  // first retrieve all the possible formats for the current sensor device
	            			FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(dataFormats,CHANNEL_TYPE.CAL.toString())); // retrieve the calibrated data
	            			if (formatCluster!=null){
	            				double ppgdata = formatCluster.mData;

	            				try {
	            					ppgdata = mFilter.filterData(ppgdata);
	            				} catch (Exception e) {
	            					// TODO Auto-generated catch block
	            					e.printStackTrace();
	            				}

	            				dataFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP);  // first retrieve all the possible formats for the current sensor device
	            				formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(dataFormats,CHANNEL_TYPE.CAL.toString())); // retrieve the calibrated data

	            				double calts = formatCluster.mData;

	            				double hr = mPPGtoHR.ppgToHrConversion(ppgdata, calts);
	            				System.out.print("Heart Rate: " + Integer.toString((int)hr) + "\n");
	            				objectCluster.addData(Configuration.Shimmer3.ObjectClusterSensorName.PPG_TO_HR,CHANNEL_TYPE.CAL,Configuration.CHANNEL_UNITS.BEATS_PER_MINUTE,hr);

	            			}


	            	    }

	            	    //Filter Signal
	            	    //ECG to HR
	            	    if (mECGtoHREnabled){
	            	    	Collection<FormatCluster> dataFormats = objectCluster.getCollectionOfFormatClusters(mECGtoHRSignalName);  // first retrieve all the possible formats for the current sensor device
	            			FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(dataFormats,CHANNEL_TYPE.CAL.toString())); // retrieve the calibrated data
	            			if (formatCluster!=null){
	            				double ecgdata = formatCluster.mData;

	            				try {
	            					ecgdata = mLPFilterECG.filterData(ecgdata);
	            					ecgdata = mHPFilterECG.filterData(ecgdata);
	            				} catch (Exception e) {
	            					// TODO Auto-generated catch block
	            					e.printStackTrace();
	            				}

	            				dataFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP);  // first retrieve all the possible formats for the current sensor device
	            				formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(dataFormats,CHANNEL_TYPE.CAL.toString())); // retrieve the calibrated data

	            				double calts = formatCluster.mData;

	            				double hr = mECGtoHR.ecgToHrConversion(ecgdata, calts);
	            				System.out.print("Heart Rate: " + Integer.toString((int)hr) + "\n");
	            				objectCluster.addData(Configuration.Shimmer3.ObjectClusterSensorName.ECG_TO_HR_FW,CHANNEL_TYPE.CAL,Configuration.CHANNEL_UNITS.BEATS_PER_MINUTE,hr);
	            			}
	            	    }

	            	    //PPG to HR

	            		if (mConvertGSRtoSiemens){
            				Collection<FormatCluster> dataFormatsGSR = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.GSR_RESISTANCE);  // first retrieve all the possible formats for the current sensor device
	            			FormatCluster formatClusterGSR = ((FormatCluster)ObjectCluster.returnFormatCluster(dataFormatsGSR,CHANNEL_TYPE.CAL.toString())); // retrieve the calibrated data
	            			if (formatClusterGSR!=null){
	            				double gsrdata = formatClusterGSR.mData * 1000; //in ohms
	            				double conductance = 1/gsrdata;
	            				conductance = conductance * 1000000; //convert to microSiemens
	            				//objectCluster.mPropertyCluster.remove(Configuration.Shimmer3.ObjectClusterSensorName.GSR, formatClusterGSR);
	            				objectCluster.addData(Configuration.Shimmer3.ObjectClusterSensorName.GSR_CONDUCTANCE,CHANNEL_TYPE.CAL,"microSiemens",conductance);
	            			}

            			}

	            		objectCluster.removeAll(Configuration.Shimmer3.ObjectClusterSensorName.BATT_PERCENTAGE);
	            		objectCluster.removeAll(Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP_PLOT);
	            		objectCluster.removeAll(Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_OVERALL);
	            		objectCluster.removeAll(Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_CURRENT);
	            		objectCluster.removeAll(Configuration.Shimmer3.ObjectClusterSensorName.SYSTEM_TIMESTAMP);

	            	   if (mEnableLogging==true){
		            	   shimmerLog1= (Logging)mLogShimmer.get(objectCluster.getMacAddress());
		            	   if (shimmerLog1!=null){
		            		   shimmerLog1.logData(objectCluster);
		            	   } else {
		            			char[] bA=objectCluster.getMacAddress().toCharArray();
		            			Logging shimmerLog;
		            			if (mLogFileName.equals("Default")){
		            				shimmerLog=new Logging(fromMilisecToDate(System.currentTimeMillis()) + " Device" + bA[12] + bA[13] + bA[15] + bA[16],"\t", "ShimmerCapture");
		            			} else {
		            				shimmerLog=new Logging(fromMilisecToDate(System.currentTimeMillis()) + mLogFileName,"\t", "ShimmerCapture");
		            			}
		            			mLogShimmer.remove(objectCluster.getMacAddress());
		            			if (mLogShimmer.get(objectCluster.getMacAddress())==null){
		            				mLogShimmer.put(objectCluster.getMacAddress(),shimmerLog);
		            			}
		            	   }
	            	   }

	            	   if (mGraphing==true){
	            		  // Log.d("ShimmerGraph","Sending");
	            		   mHandlerGraph.obtainMessage(ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET, objectCluster)
               	        .sendToTarget();
	            	   }
	            	}
	                break;
	                 case Shimmer.MESSAGE_TOAST:
	                	Log.d("toast",msg.getData().getString(Shimmer.TOAST));
	                	Toast.makeText(getApplicationContext(), msg.getData().getString(Shimmer.TOAST),
	                            Toast.LENGTH_SHORT).show();
	                	if (msg.getData().getString(Shimmer.TOAST).equals("Device connection was lost")){

	                	}
	                break;
	                 case ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE:
	                	 Intent intent = new Intent("com.shimmerresearch.service.ShimmerService");
	                	 Log.d("ShimmerGraph","Sending");
	            		   //mHandlerGraph.obtainMessage(ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE, msg.arg1, -1, msg.obj).sendToTarget();
	            		   if(msg.arg1==Shimmer.MSG_STATE_STOP_STREAMING){ //deprecated shimmer
	            			     closeAndRemoveFile(((ObjectCluster)msg.obj).getMacAddress());
	            		   } else {
							   BT_STATE state=null;
							   String macAddress = "";
							   String shimmerName = "";
							   if (msg.obj instanceof ObjectCluster){
								   state = ((ObjectCluster)msg.obj).mState;
								   macAddress = ((ObjectCluster)msg.obj).getMacAddress();
								   shimmerName = ((ObjectCluster) msg.obj).getShimmerName();
							   } else if(msg.obj instanceof CallbackObject){
								   state = ((CallbackObject)msg.obj).mState;
								   macAddress = ((CallbackObject)msg.obj).mBluetoothAddress;
								   shimmerName = "";
							   }
	            			   switch (state) {
	            			   case CONNECTED:
								   ShimmerDevice shimmerDevice = btManager.getShimmerDeviceBtConnectedFromMac(macAddress);
								   mMultiShimmer.remove(macAddress);
								   if (mMultiShimmer.get(macAddress)==null) { mMultiShimmer.put(macAddress,shimmerDevice); }
								   intent.putExtra("ShimmerBluetoothAddress", macAddress);
	            				   intent.putExtra("ShimmerDeviceName", shimmerName);
	            				   intent.putExtra("ShimmerState",BT_STATE.CONNECTED);
	            				   sendBroadcast(intent);
								   break;
	            			   case CONNECTING:
	            				   intent.putExtra("ShimmerBluetoothAddress", macAddress);
	            				   intent.putExtra("ShimmerDeviceName", shimmerName);
	            				   intent.putExtra("ShimmerState",BT_STATE.CONNECTING);
								   sendBroadcast(intent);
	            				   break;
	            			   case STREAMING:
								   intent.putExtra("ShimmerBluetoothAddress", macAddress);
								   intent.putExtra("ShimmerDeviceName", shimmerName);
								   intent.putExtra("ShimmerState", BT_STATE.STREAMING);
								   sendBroadcast(intent);
	            				   break;
	            			   case STREAMING_AND_SDLOGGING:
								   intent.putExtra("ShimmerBluetoothAddress", macAddress);
								   intent.putExtra("ShimmerDeviceName", shimmerName);
								   intent.putExtra("ShimmerState", BT_STATE.STREAMING_AND_SDLOGGING);
								   sendBroadcast(intent);
								   break;
	            			   case SDLOGGING:
	            				   Log.d("Shimmer",((ObjectCluster) msg.obj).getMacAddress() + "  " + ((ObjectCluster) msg.obj).getShimmerName());
	            				   intent.putExtra("ShimmerBluetoothAddress", ((ObjectCluster) msg.obj).getMacAddress() );
	            				   intent.putExtra("ShimmerDeviceName", ((ObjectCluster) msg.obj).getShimmerName() );
	            				   intent.putExtra("ShimmerState",BT_STATE.SDLOGGING);
	            				   sendBroadcast(intent);
	            				   break;
	            			   case DISCONNECTED:
	            				   intent.putExtra("ShimmerBluetoothAddress", macAddress );
	            				   intent.putExtra("ShimmerDeviceName", shimmerName );
	            				   intent.putExtra("ShimmerState",BT_STATE.DISCONNECTED);
	            				   sendBroadcast(intent);
	            				   break;
	            			   }

	            			   break;
	            		   }
                 case Shimmer.MESSAGE_STOP_STREAMING_COMPLETE:
                	 String address =  msg.getData().getString("Bluetooth Address");
                	 boolean stop  =  msg.getData().getBoolean("Stop Streaming");
                	 if (stop==true ){
                		 closeAndRemoveFile(address);
                	 }
                	break;
                 case Shimmer.MESSAGE_LOG_AND_STREAM_STATUS_CHANGED:
                 	mHandlerGraph.obtainMessage(Shimmer.MESSAGE_LOG_AND_STREAM_STATUS_CHANGED, msg.arg1, msg.arg2).sendToTarget();
                 	break;
	            }
	        }
	    };


    public void stopStreamingAllDevices() {
/*		TODO: check if this works
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();

			if (stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING){
				stemp.stopStreaming();
			}
		}
*/
		btManager.stopStreamingAllDevices();
	}

	public void startStreamingAllDevices() {

		//TODO: Check if this works
		btManager.startStreamingAllDevices();

		// TODO Auto-generated method stub
/*
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING)){
				if (mPPGtoHREnabled){
					enablePPGtoHR(stemp.getBluetoothAddress(),true);
				}
				if (mECGtoHREnabled){
					enableECGtoHR(stemp.getBluetoothAddress(),true);
				}

				stemp.startStreaming();
			}
		}
*/
	}

	public void setEnableLogging(boolean enableLogging){
		mEnableLogging=enableLogging;
		Log.d("Shimmer","Logging :" + Boolean.toString(mEnableLogging));
	}

	public boolean getEnableLogging(){
		return mEnableLogging;
	}
	public void setAllSampingRate(double samplingRate) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING)){
				stemp.writeShimmerAndSensorsSamplingRate(samplingRate);
				if (mPPGtoHREnabled){
					mPPGtoHR = new PPGtoHRAlgorithm(samplingRate, mNumberOfBeatsToAvg, true);
					try {
						mFilter = new Filter(Filter.LOW_PASS, samplingRate,mLPFc);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (mECGtoHREnabled){
					mECGtoHR = new ECGtoHRAdaptive(samplingRate);
					try {
						mLPFilterECG = new Filter(Filter.LOW_PASS, samplingRate, mLPFcECG);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					try {
						mHPFilterECG = new Filter(Filter.HIGH_PASS, samplingRate, mHPFcECG);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}
	}

	public void setAllAccelRange(int accelRange) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING)){
				stemp.writeAccelRange(accelRange);
			}
		}
	}

	public void setAllGSRRange(int gsrRange) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING)){
				stemp.writeGSRRange(gsrRange);
			}
		}
	}

	public void setAllEnabledSensors(int enabledSensors) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING)){
				stemp.writeEnabledSensors(enabledSensors);
			}
		}
	}


	public void setEnabledSensors(long enabledSensors,String bluetoothAddress) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){

				if (((enabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0)||((enabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0)){

				} else {
					mECGtoHREnabled = false;
				}

				if (stemp.getInternalExpPower()==1 && (((enabledSensors & Shimmer.SENSOR_INT_ADC_A1)>0)||((enabledSensors & Shimmer.SENSOR_INT_ADC_A12)>0)|((enabledSensors & Shimmer.SENSOR_INT_ADC_A13)>0)||((enabledSensors & Shimmer.SENSOR_INT_ADC_A14)>0))){

				} else {
					mPPGtoHREnabled = false;
				}
				stemp.writeEnabledSensors(enabledSensors);
			}
		}
	}

	public void toggleLED(String bluetoothAddress) {
		// TODO Move to ShimmerBluetoothManager
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.toggleLed();
			}
		}
	}

	public void writePMux(String bluetoothAddress,int setBit) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.writePMux(setBit);
			}
		}
	}

	public void write5VReg(String bluetoothAddress,int setBit) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.writeFiveVoltReg(setBit);
			}
		}
	}


	public List<String[]> getListofEnabledSensorSignals(String bluetoothAddress) {
		// TODO Auto-generated method stub
		List<String[]> listofSensors = new ArrayList<String[]>();
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			ShimmerDevice stemp=(ShimmerDevice) iterator.next();
			String address = stemp.getMacId();
			address = address.replace(":","");
			bluetoothAddress = bluetoothAddress.replace(":","");

			BT_STATE btState = stemp.getBluetoothRadioState();
			if ((btState==BT_STATE.STREAMING || btState==BT_STATE.STREAMING_AND_SDLOGGING) && address.equals(bluetoothAddress)){
				if (stemp.getShimmerVerObject().getHardwareVersion()== ShimmerVerDetails.HW_ID.SHIMMER_2R){
					return ((Shimmer)stemp).getListofEnabledChannelSignalsandFormats();
				} else {
					List<SensorDetails> listOfEnabledSensorDetails = stemp.getListOfEnabledSensors();
					for (SensorDetails sd : listOfEnabledSensorDetails) {
						for (ChannelDetails cd : sd.mListOfChannels) {
							for (CHANNEL_TYPE ct : cd.mListOfChannelTypes) {
								String[] sensor = new String[4];
								sensor[0] = stemp.getShimmerUserAssignedName();
								sensor[1] = cd.mObjectClusterName;
								sensor[2] = ct.toString();
								sensor[3] = "";
								if (ct.equals(CHANNEL_TYPE.UNCAL)) {
//									sensor[3] = cd.mDefaultUnit;
								} else if (ct.equals(CHANNEL_TYPE.CAL)) {
//									sensor[3] = cd.mDefaultCalibratedUnits;
								}
								listofSensors.add(sensor);
							}
						}
					}
				}
			}
		}
		return listofSensors;
	}


	public long getEnabledSensors(String bluetoothAddress) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		long enabledSensors=0;
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()!=BT_STATE.DISCONNECTED)){
				enabledSensors = stemp.getEnabledSensors();
			}
		}
		return enabledSensors;
	}


	public void writeSamplingRate(String bluetoothAddress,double samplingRate) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.writeShimmerAndSensorsSamplingRate(samplingRate);
				if (mPPGtoHREnabled){
					mPPGtoHR = new PPGtoHRAlgorithm(samplingRate, mNumberOfBeatsToAvg, true);
					try {
						mFilter = new Filter(Filter.LOW_PASS, samplingRate,mLPFc);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (mECGtoHREnabled){
					mECGtoHR = new ECGtoHRAdaptive(samplingRate);
					try {
						mLPFilterECG = new Filter(Filter.LOW_PASS, samplingRate, mLPFcECG);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					try {
						mHPFilterECG = new Filter(Filter.HIGH_PASS, samplingRate, mHPFcECG);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void writeAccelRange(String bluetoothAddress,int accelRange) {
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.writeAccelRange(accelRange);
			}
		}
	}

	public void writeGyroRange(String bluetoothAddress,int range) {
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.writeGyroRange(range);
			}
		}
	}

	public void writePressureResolution(String bluetoothAddress,int resolution) {
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				//currently not supported
				stemp.writePressureResolution(resolution);
			}
		}
	}

	public void writeMagRange(String bluetoothAddress,int range) {
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.writeMagRange(range);
			}
		}
	}

	public void writeGSRRange(String bluetoothAddress,int gsrRange) {
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.writeGSRRange(gsrRange);
			}
		}
	}


	public double getSamplingRate(String bluetoothAddress) {
		// TODO Auto-generated method stub

		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		double SRate=-1;
		while (iterator.hasNext()) {
			ShimmerDevice stemp=(ShimmerDevice) iterator.next();
			String address = stemp.getMacId();
			address = address.replace(":","");
			bluetoothAddress = bluetoothAddress.replace(":","");
			if (address.equals(bluetoothAddress)){
				SRate= stemp.getSamplingRateShimmer(Configuration.COMMUNICATION_TYPE.BLUETOOTH);
			}
		}
		return SRate;
	}

	public int getAccelRange(String bluetoothAddress) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		int aRange=-1;
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				aRange = stemp.getAccelRange();
			}
		}
		return aRange;
	}

	public BT_STATE getShimmerState(String bluetoothAddress){

		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		BT_STATE status=BT_STATE.DISCONNECTED;
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				status = stemp.getBluetoothRadioState();

			}
		}
		return status;

	}

	public int getGSRRange(String bluetoothAddress) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		int gRange=-1;
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				gRange = stemp.getGSRRange();
			}
		}
		return gRange;
	}

	public int get5VReg(String bluetoothAddress) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		int fiveVReg=-1;
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				fiveVReg = stemp.get5VReg();
			}
		}
		return fiveVReg;
	}

	public boolean isLowPowerMagEnabled(String bluetoothAddress) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		boolean enabled=false;
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				enabled = stemp.isLowPowerMagEnabled();
			}
		}
		return enabled;
	}


	public int getpmux(String bluetoothAddress) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		int pmux=-1;
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				pmux = stemp.getPMux();
			}
		}
		return pmux;
	}


	public void startStreaming(String bluetoothAddress) {
		// TODO Auto-generated method stub
				Collection<Object> colS=mMultiShimmer.values();
				Iterator<Object> iterator = colS.iterator();
				while (iterator.hasNext()) {
					ShimmerDevice stemp=(ShimmerDevice) iterator.next();
					String address = stemp.getMacId();
					address = address.replace(":","");
					bluetoothAddress = bluetoothAddress.replace(":","");
					if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && address.equals(bluetoothAddress)){
						if (mPPGtoHREnabled){
							enablePPGtoHR(stemp.getMacId(),true);
						}
						if (mECGtoHREnabled){
							enableECGtoHR(stemp.getMacId(),true);
						}
						if (stemp instanceof Shimmer) {
							((Shimmer)stemp).startStreaming();
						} else if (stemp instanceof Shimmer4Android){
//							((Shimmer4Android)stemp).mShimmerRadioHWLiteProtocol.startStreaming();
						}
					}
				}
	}

	public void startLogging(String bluetoothAddress) {
		// TODO Auto-generated method stub
				Collection<Object> colS=mMultiShimmer.values();
				Iterator<Object> iterator = colS.iterator();
				while (iterator.hasNext()) {
					Shimmer stemp=(Shimmer) iterator.next();
					if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.STREAMING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
						stemp.startSDLogging();
					}
				}
	}

	public void stopLogging(String bluetoothAddress) {
		// TODO Auto-generated method stub
				Collection<Object> colS=mMultiShimmer.values();
				Iterator<Object> iterator = colS.iterator();
				while (iterator.hasNext()) {
					Shimmer stemp=(Shimmer) iterator.next();
					if ((stemp.getBluetoothRadioState()==BT_STATE.STREAMING_AND_SDLOGGING || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
						stemp.stopSDLogging();
					}
				}
	}

	public void startLogAndStreaming(String bluetoothAddress) {
		// TODO Auto-generated method stub
				Collection<Object> colS=mMultiShimmer.values();
				Iterator<Object> iterator = colS.iterator();
				while (iterator.hasNext()) {
					Shimmer stemp=(Shimmer) iterator.next();
					if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
						stemp.startDataLogAndStreaming();
					}
				}
	}

	public long sensorConflictCheckandCorrection(String bluetoothAddress, long enabledSensors, int sensorToCheck) {
		// TODO Auto-generated method stub
		long newSensorBitmap = 0;
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				newSensorBitmap = stemp.sensorConflictCheckandCorrection(enabledSensors,sensorToCheck);
			}
		}
		return newSensorBitmap;
	}
	public List<String> getListofEnabledSensors(String bluetoothAddress) {
		// TODO Auto-generated method stub
		List<String> listofSensors = null;
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			ShimmerDevice stemp=(ShimmerDevice) iterator.next();
			if (stemp.getMacId().equals(bluetoothAddress)){
				if (stemp instanceof Shimmer) {
					listofSensors = ((Shimmer)stemp).getListofEnabledSensors();
				} else if(stemp instanceof Shimmer4Android){
					listofSensors = new ArrayList<String>();
				}
			}
		}
		return listofSensors;
	}


	public boolean bluetoothAddressComparator(String bluetoothAddress, String address){
		address = address.replace(":","");
		bluetoothAddress = bluetoothAddress.replace(":","");
		if (address.equals(bluetoothAddress)){
			return true;
		} else {
			return false;
		}
	}

	public void stopStreaming(String bluetoothAddress) {
		// TODO Auto-generated method stub
				Collection<Object> colS=mMultiShimmer.values();
				Iterator<Object> iterator = colS.iterator();
				while (iterator.hasNext()) {
					ShimmerDevice stemp=(ShimmerDevice) iterator.next();
					String address = stemp.getMacId();
					address = address.replace(":","");
					bluetoothAddress = bluetoothAddress.replace(":","");
					if ((stemp.getBluetoothRadioState()==BT_STATE.STREAMING || stemp.getBluetoothRadioState()==BT_STATE.STREAMING_AND_SDLOGGING) && address.equals(bluetoothAddress)){
						if (stemp instanceof Shimmer){
							((Shimmer)stemp).stopStreaming();
						} else if (stemp instanceof Shimmer4Android){
//							((Shimmer4Android)stemp).mShimmerRadioHWLiteProtocol.stopStreaming();
						}
						if (mPlotManager!=null){
							mPlotManager.removeAllSignals();
						}

					}
				}
	}

	public void setBlinkLEDCMD(String bluetoothAddress) {
		// TODO Auto-generated method stub
		Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				if (stemp.getCurrentLEDStatus()==0){
					stemp.writeLEDCommand(1);
				} else {
					stemp.writeLEDCommand(0);
				}
			}
		}

	}

	public void enableLowPowerMag(String bluetoothAddress,boolean enable) {
		// TODO Auto-generated method stub
		Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.enableLowPowerMag(enable);
			}
		}
	}


	public void setBattLimitWarning(String bluetoothAddress, double limit) {
		// TODO Auto-generated method stub
		Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.setBattLimitWarning(limit);
			}
		}

	}


	public double getBattLimitWarning(String bluetoothAddress) {
		// TODO Auto-generated method stub
		double limit=-1;
		Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				limit=stemp.getBattLimitWarning();
			}
		}
		return limit;
	}


	public double getPacketReceptionRate(String bluetoothAddress) {
		// TODO Auto-generated method stub
		double rate=-1;
		Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
				rate=stemp.getPacketReceptionRate();
			}
		}
		return rate;
	}


	public void disconnectShimmer(String bluetoothAddress){
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				stemp.stop();

			}
		}


		mLogShimmer.remove(bluetoothAddress);
		mMultiShimmer.remove(bluetoothAddress);

	}

	public void setGraphHandler(Handler handler){
		mHandlerGraph=handler;

	}

	public void enableGraphingHandler(boolean setting){
		mGraphing=setting;
	}

	public boolean DevicesConnected(String bluetoothAddress){
		if (bluetoothAddress==null){
			return false;
		}
		boolean deviceConnected=false;
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			ShimmerDevice stemp=(ShimmerDevice) iterator.next();
			String address = stemp.getMacId();
			address = address.replace(":","");
			bluetoothAddress = bluetoothAddress.replace(":","");
			if (stemp.getBluetoothRadioState()!=BT_STATE.DISCONNECTED && address.equals(bluetoothAddress)){
				deviceConnected=true;
			}
		}
		return deviceConnected;
	}

	public boolean DeviceIsLogging(String bluetoothAddress){
		boolean deviceLogging=false;
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			ShimmerDevice stemp=(ShimmerDevice) iterator.next();
			if ((stemp.mBluetoothRadioState == BT_STATE.SDLOGGING || stemp.mBluetoothRadioState == BT_STATE.STREAMING_AND_SDLOGGING) && stemp.getMacId().equals(bluetoothAddress.replaceAll(":",""))){
				deviceLogging=true;
			}
		}
		return deviceLogging;
	}

	public boolean DeviceIsStreaming(String bluetoothAddress){
		boolean deviceStreaming=false;
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			ShimmerDevice stemp=(ShimmerDevice) iterator.next();
			String address = stemp.getMacId();
			address = address.replace(":","");
			bluetoothAddress = bluetoothAddress.replace(":","");
			BT_STATE btState = stemp.getBluetoothRadioState();
			if ((btState==BT_STATE.STREAMING || btState==BT_STATE.STREAMING_AND_SDLOGGING)  && address.equals(bluetoothAddress)){
				deviceStreaming=true;
			}
		}
		return deviceStreaming;
	}

	public void setLoggingName(String name){
		mLogFileName=name;
	}

	public void closeAndRemoveFile(String bluetoothAddress){
		if (mLogShimmer.get(bluetoothAddress)!=null){
			mLogShimmer.get(bluetoothAddress).closeFile();
			MediaScannerConnection.scanFile(this, new String[] { mLogShimmer.get(bluetoothAddress).getAbsoluteName() }, null, null);
			mLogShimmer.remove(bluetoothAddress);

		}
	}

	public String getFWVersion (String bluetoothAddress){
		String version="";
		ShimmerDevice stemp=(ShimmerDevice) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			version=stemp.getFirmwareVersionMajor()+"."+stemp.getFirmwareVersionMinor();
		}
		return version;
	}

	public int getShimmerVersion (String bluetoothAddress){
		int version=0;
		ShimmerDevice stemp=(ShimmerDevice) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			version=stemp.getHardwareVersion();
		}
		return version;
	}



	public ShimmerDevice getShimmer(String bluetoothAddress){
		// TODO Auto-generated method stub
		ShimmerDevice shimmer = null;
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			shimmer=(ShimmerDevice) iterator.next();
			String address = shimmer.getMacId();
			address = address.replace(":","");
			bluetoothAddress = bluetoothAddress.replace(":","");
			if (address.equals(bluetoothAddress)){
				return shimmer;
			}
		}
		return shimmer;
	}

	public void test(){
		Log.d("ShimmerTest","Test");
	}


	public boolean isEXGUsingTestSignal24Configuration(String bluetoothAddress){

		Shimmer tmp = (Shimmer) mMultiShimmer.get(bluetoothAddress);
		return tmp.isEXGUsingTestSignal24Configuration();
	}

	public boolean isEXGUsingTestSignal16Configuration(String bluetoothAddress){

		Shimmer tmp = (Shimmer) mMultiShimmer.get(bluetoothAddress);
		return tmp.isEXGUsingTestSignal16Configuration();
	}

	public boolean isEXGUsingECG24Configuration(String bluetoothAddress){

		Shimmer tmp = (Shimmer) mMultiShimmer.get(bluetoothAddress);
		return tmp.isEXGUsingECG24Configuration();
	}

	public boolean isEXGUsingECG16Configuration(String bluetoothAddress){

		Shimmer tmp = (Shimmer) mMultiShimmer.get(bluetoothAddress);
		return tmp.isEXGUsingECG16Configuration();
	}

	public boolean isEXGUsingEMG24Configuration(String bluetoothAddress){

		Shimmer tmp = (Shimmer) mMultiShimmer.get(bluetoothAddress);
		return tmp.isEXGUsingEMG24Configuration();
	}

	public boolean isEXGUsingEMG16Configuration(String bluetoothAddress){

		Shimmer tmp = (Shimmer) mMultiShimmer.get(bluetoothAddress);
		return tmp.isEXGUsingEMG16Configuration();
	}

	public void writeEXGSetting(String bluetoothAddress,int setting) {
		// TODO Auto-generated method stub
		Collection<Object> colS=mMultiShimmer.values();
		Iterator<Object> iterator = colS.iterator();
		while (iterator.hasNext()) {
			Shimmer stemp=(Shimmer) iterator.next();
			if ((stemp.getBluetoothRadioState()==BT_STATE.CONNECTED || stemp.getBluetoothRadioState()==BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
				if (setting==0){
					stemp.enableDefaultECGConfiguration();
				} else if (setting==1){
					stemp.enableDefaultEMGConfiguration();
				} else if (setting==2){
					stemp.enableEXGTestSignal();

				}

			} else {

			}
		}
	}



	//convert the system time in miliseconds to a "readable" date format with the next format: YYYY MM DD HH MM SS
	private String fromMilisecToDate(long miliseconds){

		String date="";
		Date dateToParse = new Date(miliseconds);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		date = dateFormat.format(dateToParse);

		return date;
	}

	public boolean isUsingLogAndStreamFW(String bluetoothAddress){

		boolean logAndStream = false;
		ShimmerDevice stemp=(ShimmerDevice) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			if(stemp.getFirmwareIdentifier()==3)
				logAndStream = true;
		}
		return logAndStream;

	}

	public void readStatusLogAndStream(String bluetoothAddress){

		Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			if(stemp.getFirmwareIdentifier()==3)
				stemp.readStatusLogAndStream();
		}
	}

	public boolean isSensing(String bluetoothAddress){

		Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			if(stemp.getFirmwareIdentifier()==3)
				return stemp.isSensing();
		}

		return false;
	}

	public boolean isDocked(String bluetoothAddress){

		Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
		if (stemp!=null){
			if(stemp.getFirmwareIdentifier()==3)
				return stemp.isDocked();
		}

		return false;
	}

}
