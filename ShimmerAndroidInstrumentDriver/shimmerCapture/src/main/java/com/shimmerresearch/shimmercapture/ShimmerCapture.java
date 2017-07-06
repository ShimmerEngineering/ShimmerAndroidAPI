//v0.5

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
 * @author Alejandro Saez
 * @date   July, 2014
 * 
 *  * Changes since v0.4
 * - Support for LogAndStream firmware
 * 
 * Changes since v0.2
 * - Radio button in the list of sensors to plot
 * 
 * Changes since v0.1
 * - Added functionality to plot ExG data
 */

package com.shimmerresearch.shimmercapture;

import im.delight.apprater.AppRater;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.androidplot.ui.DynamicTableModel;
import com.shimmerresearch.android.Shimmer4Android;
import com.shimmerresearch.android.guiUtilities.ShimmerDialogConfigurations;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.tools.PlotManagerAndroid;


import pl.flex_it.androidplot.XYSeriesShimmer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.google.common.collect.BiMap;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.android.shimmerService.ShimmerService;
import com.shimmersensing.shimmerconnect.R;

public class ShimmerCapture extends ServiceActivity {

	private static Context context;
	static final int REQUEST_ENABLE_BT = 1;
	static final int REQUEST_CONNECT_SHIMMER = 2;
	static final int REQUEST_CONFIGURE_SHIMMER = 3;
	static final int REQUEST_CONFIGURE_VIEW_SENSOR = 4;
	static final int REQUEST_COMMAND_SHIMMER = 5;
	static final int REQUEST_LOGFILE_SHIMMER = 6;
	private String CONNECT = "Connect";
	private String DISCONNECT = "Disconnect";
	private String START_STREAMING = "Start Streaming";
	private String START_LOGGING = "Start Logging";
	private String STOP_LOGGING = "Stop Logging";
	private String NEW_SHIMMER_CONFIGURATION = "Shimmer4 Configuration";
	private String NEW_ENABLE_SENSOR = "Shimmer4 Enable Sensors";

	//private String START_STREAMING_AND_LOGGING = "Start Streaming+Logging";
	private String STOP_STREAMING = "Stop Streaming";
	//private String STOP_STRAMING_AND_LOGGING = "Stop Streaming+Logging";
	private String EDIT_GRAPH = "Signals To Graph";
	private String LOG_FILE = "Write Data To File";
	private String SHIMMER_CONFIGURATION = "Shimmer Configuration";
	private String GRAPH_CONFIGURATION = "Graph Configuration";
	private String ENABLE_SENSOR = "Enable Sensors";
	private String DEVICE_INFO = "Device Info";
	private static XYPlot dynamicPlot;
	static List<Number> dataList = new ArrayList<Number>();
	static List<Number> dataTimeList = new ArrayList<Number>();
	static LineAndPointFormatter lineAndPointFormatter;
	public static int X_AXIS_LENGTH = 500;
	public static boolean clearGraph=false;
	public static HashMap<String, List<Number>> mPlotDataMap = new HashMap<String, List<Number>>(4);
	public static HashMap<String, XYSeriesShimmer> mPlotSeriesMap = new HashMap<String, XYSeriesShimmer>(4);
	static LineAndPointFormatter lineAndPointFormatter1, lineAndPointFormatter2, lineAndPointFormatter3;
	private static Paint LPFpaint;
	private Paint transparentPaint;
	private Paint outlinePaint;
	static String deviceState = "Disconnected"; 
	static TextView textDeviceName;
	static TextView textDeviceState;
	static TextView textAppVersion;
	String appVersion=""; 
	
	TextView mShimmerVersion, mFirmwareVersion;
	static TextView textDockedStatus, textSensingStatus;
	static LinearLayout logAndStreamStatusLayout;
		
	private static Button buttonMenu;
	public AlertDialog.Builder menuListViewDialog;
	
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Name of the connected device
    private static String mBluetoothAddress = null;
    // Member object for communication services
    private String mSignaltoGraph;
    //private static String mSensorView = ""; //The sensor device which should be viewed on the graph
    private static boolean legendInitializate;
    private static int mGraphSubSamplingCount = 0; //10 
    private static String mFileName = "Default";
    private static boolean mEnableLogging = false;
    Dialog writeToFileDialog, deviceInfoDialog, graphConfigurationDialog;
    long dialogEnabledSensors=0;
    public ArrayAdapter<String> arrayAdapter;
	CheckBox mCheckBoxEnableLogging, mCheckBoxResetScreen;
	Button mButtonCommitChanges, mButtonGraphConfiguration;
	EditText mEditFileName, mEditXAxisLimit;
	static boolean mValueConstant = true;
	static int mValueConstantTimes = 0;

	List<String[]> mListofChannels;

	static int mPlotLimit = 1;

	Map<String, ConfigOptionDetailsSensor> mConfigOptionsMap;
	ShimmerDevice mShimmerDeviceClone;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

    	super.onCreate(savedInstanceState);
        Log.d("ShimmerActivity","On Create");
        super.onCreate(savedInstanceState);
        ShimmerCapture.context = getApplicationContext();
        setContentView(R.layout.main);
        Log.d("ShimmerActivity","On Create");
        
        logAndStreamStatusLayout = (LinearLayout) findViewById(R.id.linearLayout3);
        textDockedStatus = (TextView) findViewById(R.id.textDockedStatus);
        textSensingStatus = (TextView) findViewById(R.id.textSensingStatus);

        writeToFileDialog = new Dialog(this);
        writeToFileDialog.setTitle("Write Data To File");
		writeToFileDialog.setContentView(R.layout.logfile);	
        mEditFileName = (EditText) writeToFileDialog.findViewById(R.id.editText1);
        mCheckBoxEnableLogging = (CheckBox) writeToFileDialog.findViewById(R.id.checkBox1);
        mButtonCommitChanges = (Button) writeToFileDialog.findViewById(R.id.button1);
        deviceInfoDialog = new Dialog(this);
        deviceInfoDialog.setTitle("Device Info");
        deviceInfoDialog.setContentView(R.layout.device_info);
        mShimmerVersion = (TextView) deviceInfoDialog.findViewById(R.id.TextShimmerVersion);
        mFirmwareVersion = (TextView) deviceInfoDialog.findViewById(R.id.TextFirmwareVersion);
        textAppVersion = (TextView) findViewById(R.id.textVersion);
        graphConfigurationDialog = new Dialog(this);
        graphConfigurationDialog.setTitle("Graph Configuration");
        graphConfigurationDialog.setContentView(R.layout.graph_configuration);	
        mEditXAxisLimit = (EditText) graphConfigurationDialog.findViewById(R.id.editTextXAxisSize);
        mCheckBoxResetScreen = (CheckBox) graphConfigurationDialog.findViewById(R.id.checkBoxResetScreen);
        
        try {
			appVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        textAppVersion.setText("v"+appVersion);
        textDeviceName = (TextView) findViewById(R.id.textDeviceName);
        textDeviceState = (TextView) findViewById(R.id.textDeviceState);
        if(mBluetoothAddress!=null){
        	textDeviceName.setText(mBluetoothAddress);
        	textDeviceState.setText(deviceState);
        }
        
       /**  --GRAPH SET UP--  **/
        
        dynamicPlot   = (XYPlot)   findViewById(R.id.dynamicPlot);
        
        dynamicPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
        lineAndPointFormatter1 = new LineAndPointFormatter(Color.rgb(51, 153, 255), null, null); // line color, point color, fill color
        LPFpaint = lineAndPointFormatter1.getLinePaint();
        LPFpaint.setStrokeWidth(3);
        lineAndPointFormatter1.setLinePaint(LPFpaint);
        lineAndPointFormatter2 = new LineAndPointFormatter(Color.rgb(245, 146, 107), null, null);
        LPFpaint = lineAndPointFormatter2.getLinePaint();
        LPFpaint.setStrokeWidth(3);
        lineAndPointFormatter2.setLinePaint(LPFpaint);
        lineAndPointFormatter3 = new LineAndPointFormatter(Color.rgb(150, 150, 150), null, null);
        LPFpaint = lineAndPointFormatter3.getLinePaint();
        LPFpaint.setStrokeWidth(3);
        lineAndPointFormatter3.setLinePaint(LPFpaint);
        transparentPaint = new Paint();
        transparentPaint.setColor(Color.TRANSPARENT);
        //lineAndPointFormatter1.setLinePaint(p);
        dynamicPlot.setDomainStepMode(XYStepMode.SUBDIVIDE);
        //dynamicPlot.setDomainStepValue(series1.size());
		dynamicPlot.getLegendWidget().setTableModel(new DynamicTableModel(1, 4));
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
        dynamicPlot.getLegendWidget().setSize(new SizeMetrics(width/2, SizeLayoutType.ABSOLUTE, height/3, SizeLayoutType.ABSOLUTE));
        // thin out domain/range tick labels so they dont overlap each other:
        dynamicPlot.setTicksPerDomainLabel(5);
        dynamicPlot.setTicksPerRangeLabel(3);
        dynamicPlot.disableAllMarkup();
        Paint gridLinePaint = new Paint();
        gridLinePaint.setColor(Color.parseColor("#969696")); // black
        Paint transparentLinePaint = new Paint();
        transparentLinePaint.setColor(Color.TRANSPARENT); 
        dynamicPlot.setRangeBoundaries(-15, 15, BoundaryMode.AUTO); // freeze the range boundary:
        dynamicPlot.setDomainBoundaries(0, X_AXIS_LENGTH, BoundaryMode.FIXED); // freeze the domain boundary:
//        dynamicPlot.getGraphWidget().setMargins(0, 20, 10, 10);
        dynamicPlot.setBorderStyle(Plot.BorderStyle.NONE, null, null);
        dynamicPlot.getBackgroundPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.setBackgroundColor(Color.TRANSPARENT);
        dynamicPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.getGraphWidget().setGridLinePaint(transparentPaint);
        dynamicPlot.getGraphWidget().setDomainOriginLabelPaint(transparentLinePaint);
        dynamicPlot.getGraphWidget().setDomainOriginLinePaint(gridLinePaint);
//        dynamicPlot.getGraphWidget().setDomainLabelPaint(gridLinePaint);
        dynamicPlot.getGraphWidget().setDomainLabelPaint(transparentLinePaint);
        dynamicPlot.getGraphWidget().getDomainLabelPaint().setTextSize(20);
        dynamicPlot.getDomainLabelWidget().pack();
        outlinePaint = dynamicPlot.getGraphWidget().getDomainOriginLinePaint();
        outlinePaint.setStrokeWidth(3);
        dynamicPlot.getGraphWidget().setClippingEnabled(false);
        dynamicPlot.getGraphWidget().setRangeOriginLabelPaint(gridLinePaint);
        dynamicPlot.getGraphWidget().setRangeOriginLinePaint(gridLinePaint);
        dynamicPlot.getGraphWidget().setRangeLabelPaint(gridLinePaint);
        dynamicPlot.getGraphWidget().getRangeLabelPaint().setTextSize(20);
        dynamicPlot.getRangeLabelWidget().pack();
        outlinePaint = dynamicPlot.getGraphWidget().getRangeOriginLinePaint();
        outlinePaint.setStrokeWidth(3);
        dynamicPlot.getLayoutManager().remove(dynamicPlot.getRangeLabelWidget());
        dynamicPlot.getLayoutManager().remove(dynamicPlot.getDomainLabelWidget());
        
        


        
        
        menuListViewDialog =  new AlertDialog.Builder(this);
        menuListViewDialog.setTitle("Menu");
        buttonMenu = (Button) findViewById(R.id.menuButton);
        buttonMenu.setEnabled(true);
        buttonMenu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.menu_item);
				if(mService.getBluetoothManager().DevicesConnected(mBluetoothAddress)){
					arrayAdapter.add(DISCONNECT);
					if(mService.getBluetoothManager().isUsingLogAndStreamFW(mBluetoothAddress)) {
						if (mService.getBluetoothManager().DeviceIsLogging(mBluetoothAddress)) {
							arrayAdapter.add(STOP_LOGGING);
						} else {
							arrayAdapter.add(START_LOGGING);
						}
					}
					if(mService.getBluetoothManager().DeviceIsStreaming(mBluetoothAddress)){
						arrayAdapter.add(STOP_STREAMING);
					}
					else{
						arrayAdapter.add(START_STREAMING);
						ShimmerDevice sDevice = mService.getShimmer(mBluetoothAddress);
						if (sDevice.getHardwareVersion()!= ShimmerVerDetails.HW_ID.SHIMMER_4_SDK) {
							arrayAdapter.add(ENABLE_SENSOR);
							arrayAdapter.add(SHIMMER_CONFIGURATION);

						} else { //This works for Shimmer3 as Well but for backward compatibility
							arrayAdapter.add(NEW_ENABLE_SENSOR);
							arrayAdapter.add(NEW_SHIMMER_CONFIGURATION);
						}

						arrayAdapter.add(GRAPH_CONFIGURATION);
						arrayAdapter.add(DEVICE_INFO);
					}
					arrayAdapter.add(EDIT_GRAPH);
				}
				else
					arrayAdapter.add(CONNECT);
				
				arrayAdapter.add(LOG_FILE);
				
				OnOptionSelected oos = new OnOptionSelected();
				menuListViewDialog.setAdapter(arrayAdapter, oos);
				menuListViewDialog.show();
			}
		});
        
		 
		 if (!isMyServiceRunning())
	      {
	      	Log.d("ShimmerH","Oncreate2");
	      	Intent intent=new Intent(this, ShimmerService.class);
	      	startService(intent);
	      	if (mServiceFirstTime==true){
	      		Log.d("ShimmerH","Oncreate3");
	  			getApplicationContext().bindService(intent,mTestServiceConnection, Context.BIND_AUTO_CREATE);
	  			mServiceFirstTime=false;
	  		}
	      }         
	           
		 
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
        	Toast.makeText(this, "Device does not support Bluetooth\nExiting...", Toast.LENGTH_LONG).show();
        	finish();
        }
        
        mButtonCommitChanges.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				//set the filename in the LogFile
				mFileName = mEditFileName.getText().toString();
    			if(!mFileName.equals(mService.mLogFileName)){
    				if(mService.getBluetoothManager().DeviceIsStreaming(mBluetoothAddress)){
	    				mService.closeAndRemoveFile(mBluetoothAddress);
	    				Toast.makeText(getApplicationContext(), "Writing data to the new file: "+mFileName, Toast.LENGTH_SHORT).show();
    				}
    			}
    			mService.setLoggingName(mFileName);			
				mEnableLogging = mCheckBoxEnableLogging.isChecked();
				if (mEnableLogging==true){
    				mService.setEnableLogging(mEnableLogging);
    				if(mService.getBluetoothManager().DeviceIsStreaming(mBluetoothAddress))
    					if(mFileName.equals("Default"))
    						Toast.makeText(getApplicationContext(), "Writing data to the defult file\n You can find this file in the ShimmerCapture folder", Toast.LENGTH_SHORT).show();
    					else
    						Toast.makeText(getApplicationContext(), "Writing data to "+mFileName+"\n You can find this file in the ShimmerCapture folder", Toast.LENGTH_SHORT).show();
    				else
    					Toast.makeText(getApplicationContext(), "Data will be writen when the Shimmer is streaming", Toast.LENGTH_SHORT).show();
    			}
				else{
					mService.setEnableLogging(mEnableLogging);
					mService.closeAndRemoveFile(mBluetoothAddress);
				}
    			
				//hide the keyboard
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mEditFileName.getWindowToken(), 0);
				
				writeToFileDialog.dismiss();
				menuListViewDialog.show();
			}
		});
        
        mButtonGraphConfiguration = (Button) graphConfigurationDialog.findViewById(R.id.buttonGraphDone);
        
        mButtonGraphConfiguration.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearGraph = mCheckBoxResetScreen.isChecked();
				String limit = mEditXAxisLimit.getText().toString();
				int numericLimit=-1;
				if(!limit.equals(""))
					numericLimit = Integer.parseInt(limit);
				
				if(numericLimit>=10 && numericLimit<=3000){
					Set<String> set = mPlotSeriesMap.keySet();
					for(String serie: set){
						mPlotSeriesMap.get(serie).setXAxisLimit(numericLimit);
						mPlotSeriesMap.get(serie).setClearGraphatLimit(clearGraph);
					}
					X_AXIS_LENGTH = numericLimit;
					dynamicPlot.setDomainBoundaries(0, X_AXIS_LENGTH, BoundaryMode.FIXED); // freeze the domain boundary:
					graphConfigurationDialog.dismiss();

				}
				else{
					Toast.makeText(getApplicationContext(), "Please introduce a valid size. The valid range goes from 10 to 3000", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
        
        
        AppRater appRater = new AppRater(this, getPackageName());
        appRater.show();

	}
    
    @Override
    public void onStart() {
    	super.onStart();

    	if(!mBluetoothAdapter.isEnabled()) {     	
        	Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    	}

    }
    
    
    
    @Override
	public void onPause() {
		super.onPause();
		Log.d("ShimmerActivity","On Pause");
		//finish();
		 mPlotSeriesMap.clear();
    	 mPlotDataMap.clear();
    	 dynamicPlot.clear();
		if(mServiceBind == true){
  		  //getApplicationContext().unbindService(mTestServiceConnection); 
  	  }
	}

	public void onResume() {
		super.onResume();
		ShimmerCapture.context = getApplicationContext();
		Log.d("ShimmerActivity","On Resume");
		Intent intent=new Intent(this, ShimmerService.class);
  	  	Log.d("ShimmerH","on Resume");
  	  	getApplicationContext().bindService(intent,mTestServiceConnection, Context.BIND_AUTO_CREATE);
  	  	mPlotSeriesMap.clear();
  	  	mPlotDataMap.clear();
  	  	dynamicPlot.clear();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {		
		super.onDestroy();
		Log.d("ShimmerActivity","On Destroy");
	}
	
	
	 protected ServiceConnection mTestServiceConnection = new ServiceConnection() {

	      	public void onServiceConnected(ComponentName arg0, IBinder service) {
	      		// TODO Auto-generated method stub
	      		Log.d("ShimmerService", "service connected from main activity");
	      		ShimmerService.LocalBinder binder = (ShimmerService.LocalBinder) service;
	      		mService = binder.getService();
	      		mServiceBind = true;
	      		mService.setGraphHandler(mHandler);
				if (mService.mPlotManager==null){
					mService.mPlotManager = new PlotManagerAndroid(false);
				}
				mService.mPlotManager.updateDynamicPlot(dynamicPlot);
	      	}
	      	public void onServiceDisconnected(ComponentName arg0) {
	      		// TODO Auto-generated method stub
	      		mServiceBind = false;
	      	}
	    };
	
	
	// The Handler that gets information back from the BluetoothChatService
    private static Handler mHandler = new Handler() {
   

		public void handleMessage(Message msg) {
			switch (msg.what) {
            
            case ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE:
				ShimmerBluetooth.BT_STATE state=null;
				String shimmerName = "";
				if (msg.obj instanceof ObjectCluster){
					state = ((ObjectCluster)msg.obj).mState;
					mBluetoothAddress = ((ObjectCluster)msg.obj).getMacAddress();
					shimmerName = ((ObjectCluster) msg.obj).getShimmerName();
				} else if(msg.obj instanceof CallbackObject){
					state = ((CallbackObject)msg.obj).mState;
					mBluetoothAddress = ((CallbackObject)msg.obj).mBluetoothAddress;
					shimmerName = "";
				}
				if (mService.getBluetoothManager().DevicesConnected(mBluetoothAddress)){
					logAndStreamStatusLayout.setVisibility(View.VISIBLE);
					if(mService.getBluetoothManager().isDocked(mBluetoothAddress))
						textDockedStatus.setText("Yes");
					else
						textDockedStatus.setText("No");

					if(mService.getBluetoothManager().isSensing(mBluetoothAddress))
						textSensingStatus.setText("Yes");
					else
						textSensingStatus.setText("No");
				}
                switch (state) {
                case CONNECTED:

                	Log.d("ShimmerActivity","Message Fully Initialized Received from Shimmer driver");
                    mService.enableGraphingHandler(true);
                    deviceState = "Connected";
                    textDeviceName.setText(mBluetoothAddress);
                    textDeviceState.setText(deviceState);
                    buttonMenu.setEnabled(true);

                    break;
                case SDLOGGING:

                	Log.d("ShimmerActivity","Message Fully Initialized Received from Shimmer driver");
                    mService.enableGraphingHandler(true);
                    deviceState = "Connected";
                    textDeviceName.setText(mBluetoothAddress);
                    textDeviceState.setText(deviceState);
                    buttonMenu.setEnabled(true);

                    break;
                case CONNECTING:
                	Log.d("ShimmerActivity","Driver is attempting to establish connection with Shimmer device");
                	deviceState = "Connecting";
                    textDeviceName.setText(mBluetoothAddress);
                    textDeviceState.setText(deviceState);
                    break;
                case STREAMING:
                	textSensingStatus.setText("Yes");
                	deviceState="Streaming";
                    textDeviceName.setText(mBluetoothAddress);
                    textDeviceState.setText(deviceState);
                    //TODO: set the enable logging regarding the user selection
                    mService.setEnableLogging(mEnableLogging);
                    //TODO: if(!mSensorView.equals(""))
                    //TODO:	setLegend();
                    //TODO: else{
                    	List<String> sensorList = mService.getBluetoothManager().getListofEnabledSensors(mBluetoothAddress);
                    	if(sensorList!=null){
    	            		if(mService.getBluetoothManager().getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3){
    	            			sensorList.remove("ECG");
    	            			sensorList.remove("EMG");
    	            			if(sensorList.contains("EXG1")){
    	            				sensorList.remove("EXG1");
    	            				sensorList.remove("EXG2");
    	            				if(mService.isEXGUsingECG24Configuration(mBluetoothAddress)){
    	            					sensorList.add("ECG");
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT);
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT);
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT);
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT);
    	            					//sensorName[0] = "ECG LL-RA";
    	                    			//sensorName[1] = "ECG LA-RA";
    	                    			//sensorName[2] = "EXG2 CH1";
    	                    			//sensorName[3] = "ECG Vx-RL";
    	            				}
    	            					
    	            				else if(mService.isEXGUsingEMG24Configuration(mBluetoothAddress)){
    	            					sensorList.add("EMG");
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT);
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT);
    	            				}
	            					
    	            				else if(mService.isEXGUsingTestSignal24Configuration(mBluetoothAddress))
    	            					sensorList.add("ExG Test Signal");
    	            			}
    	            			if(sensorList.contains("EXG1 16Bit")){
    	            				sensorList.remove("EXG1 16Bit");
    	            				sensorList.remove("EXG2 16Bit");
    	            				if(mService.isEXGUsingECG16Configuration(mBluetoothAddress)){
    	            					sensorList.add("ECG 16Bit");
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT);
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT);
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT);
    	            				}
    	            				else if(mService.isEXGUsingEMG16Configuration(mBluetoothAddress)){
    	            					sensorList.add("EMG 16Bit");
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT);
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT);
    	            				}
    	            				else if(mService.isEXGUsingTestSignal16Configuration(mBluetoothAddress))
    	            					sensorList.add("ExG Test Signal 16Bit");
    	            			}
    	            		}

							sensorList.add("Timestamp");

							//TODO: mSensorView = sensorList.get(0);
							//TODO: setLegend();
                    	}

                    //}
                	break;
                case STREAMING_AND_SDLOGGING:

                	textSensingStatus.setText("Yes");
                	deviceState="Streaming";
                    textDeviceName.setText(mBluetoothAddress);
                    textDeviceState.setText(deviceState);
                    //set the enable logging regarding the user selection
                    mService.setEnableLogging(mEnableLogging);

                	break;
                case DISCONNECTED:
                	Log.d("ShimmerActivity","Shimmer No State");
                    mBluetoothAddress=null;
                    // this also stops streaming
                    deviceState = "Disconnected";
                    textDeviceName.setText("Unknown");
                    textDeviceState.setText(deviceState);
                    buttonMenu.setEnabled(true);
                    logAndStreamStatusLayout.setVisibility(View.INVISIBLE);
                    break;
                }
           	 

                break;
            case Shimmer.MESSAGE_TOAST:
                Toast.makeText(getContext(), msg.getData().getString(Shimmer.TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
                

            }
        }
    };
	
    private static Context getContext(){
    	return ShimmerCapture.context;
    }
    
    class OnOptionSelected implements DialogInterface.OnClickListener{

		@Override
		public void onClick(DialogInterface arg0, int which) {
			// TODO Auto-generated method stub
			String optionSelected = arrayAdapter.getItem(which);
			if(optionSelected.equals(CONNECT)){
				
				Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_SHIMMER);
					
			} else if(optionSelected.equals(DISCONNECT)){
				
				mService.disconnectAllDevices();
				if(mEnableLogging){
					mService.setEnableLogging(false);
					mService.closeAndRemoveFile(mBluetoothAddress);
				}
				mPlotSeriesMap.clear();
		    	mPlotDataMap.clear();
		    	dynamicPlot.clear();

			} else if(optionSelected.equals(START_STREAMING)){
				mValueConstant = true;
				double samplingRate = mService.getSamplingRate(mBluetoothAddress);
				mPlotLimit = (int)(samplingRate/10.0);
				mService.startStreaming(mBluetoothAddress);
//				deviceState="Streaming";
//                textDeviceName.setText(mBluetoothAddress);
//                textDeviceState.setText(deviceState);
//                //set the enable logging regarding the user selection
//                mService.setEnableLogging(mEnableLogging);
//                if(!mSensorView.equals(""))
//                	setLegend();
//                else{
//                	List<String> sensorList = mService.getListofEnabledSensors(mBluetoothAddress);
//                	if(sensorList!=null){
//	            		if(mService.getShimmerVersion(mBluetoothAddress)==Shimmer.SHIMMER_3){
//	            			sensorList.remove("ECG");
//	            			sensorList.remove("EMG");
//	            			if(sensorList.contains("EXG1")){
//	            				sensorList.remove("EXG1");
//	            				sensorList.remove("EXG2");
//	            				if(mService.isEXGUsingECG24Configuration(mBluetoothAddress))
//	            					sensorList.add("ECG");
//	            				else if(mService.isEXGUsingEMG24Configuration(mBluetoothAddress))
//	            					sensorList.add("EMG");
//	            				else if(mService.isEXGUsingTestSignal24Configuration(mBluetoothAddress))
//	            					sensorList.add("Test Signal");
//	            			}
//	            			if(sensorList.contains("EXG1 16Bit")){
//	            				sensorList.remove("EXG1 16Bit");
//	            				sensorList.remove("EXG2 16Bit");
//	            				if(mService.isEXGUsingECG16Configuration(mBluetoothAddress))
//	            					sensorList.add("ECG 16Bit");
//	            				else if(mService.isEXGUsingEMG16Configuration(mBluetoothAddress))
//	            					sensorList.add("EMG 16Bit");
//	            				else if(mService.isEXGUsingTestSignal16Configuration(mBluetoothAddress))
//	            					sensorList.add("Test Signal 16Bit");
//	            			}
//	            		}
//                	}
//            			
//            		sensorList.add("Timestamp");
//                	mSensorView = sensorList.get(0);
//                	setLegend();
//                }
			} 
			 else if(optionSelected.equals(START_LOGGING)){
					
					mService.startLogging(mBluetoothAddress);
//					deviceState="Streaming";
//	                textDeviceName.setText(mBluetoothAddress);
//	                textDeviceState.setText(deviceState);
//	                //set the enable logging regarding the user selection
//	                mService.setEnableLogging(mEnableLogging);
//	                if(!mSensorView.equals(""))
//	                	setLegend();
//	                else{
//	                	List<String> sensorList = mService.getListofEnabledSensors(mBluetoothAddress);
//	                	if(sensorList!=null){
//		            		if(mService.getShimmerVersion(mBluetoothAddress)==Shimmer.SHIMMER_3){
//		            			sensorList.remove("ECG");
//		            			sensorList.remove("EMG");
//		            			if(sensorList.contains("EXG1")){
//		            				sensorList.remove("EXG1");
//		            				sensorList.remove("EXG2");
//		            				if(mService.isEXGUsingECG24Configuration(mBluetoothAddress))
//		            					sensorList.add("ECG");
//		            				else if(mService.isEXGUsingEMG24Configuration(mBluetoothAddress))
//		            					sensorList.add("EMG");
//		            				else if(mService.isEXGUsingTestSignal24Configuration(mBluetoothAddress))
//		            					sensorList.add("Test Signal");
//		            			}
//		            			if(sensorList.contains("EXG1 16Bit")){
//		            				sensorList.remove("EXG1 16Bit");
//		            				sensorList.remove("EXG2 16Bit");
//		            				if(mService.isEXGUsingECG16Configuration(mBluetoothAddress))
//		            					sensorList.add("ECG 16Bit");
//		            				else if(mService.isEXGUsingEMG16Configuration(mBluetoothAddress))
//		            					sensorList.add("EMG 16Bit");
//		            				else if(mService.isEXGUsingTestSignal16Configuration(mBluetoothAddress))
//		            					sensorList.add("Test Signal 16Bit");
//		            			}
//		            		}
//	                	}
//	            			
//	            		sensorList.add("Timestamp");
//	                	mSensorView = sensorList.get(0);
//	                	setLegend();
//	                }
			 }
			
			 else if(optionSelected.equals(STOP_STREAMING)){
								
				mService.stopStreaming(mBluetoothAddress);
//				if(mEnableLogging){
//					mService.setEnableLogging(false);
//					mService.closeAndRemoveFile(mBluetoothAddress);
//				}
//				mPlotSeriesMap.clear();
//		    	mPlotDataMap.clear();
//		    	dynamicPlot.clear();
//		    	deviceState="Connected";
//		    	textDeviceName.setText(mBluetoothAddress);
//                textDeviceState.setText(deviceState);
//                mTextSensor1.setVisibility(View.INVISIBLE);
//    			blueCircle.setVisibility(View.INVISIBLE);
//    			mTextSensor2.setVisibility(View.INVISIBLE);
//    			orangeCircle.setVisibility(View.INVISIBLE);
//    			mTextSensor3.setVisibility(View.INVISIBLE);
//    			greyCircle.setVisibility(View.INVISIBLE);
//    			mTextSensor4.setVisibility(View.INVISIBLE);
//    			greenCircle.setVisibility(View.INVISIBLE);
			}else if(optionSelected.equals(STOP_LOGGING)){
				
				mService.stopLogging(mBluetoothAddress);
//				if(mEnableLogging){
//					mService.setEnableLogging(false);
//					mService.closeAndRemoveFile(mBluetoothAddress);
//				}
//				mPlotSeriesMap.clear();
//				mPlotDataMap.clear();
//				dynamicPlot.clear();
//				deviceState="Connected";
//				textDeviceName.setText(mBluetoothAddress);
//				textDeviceState.setText(deviceState);
//				mTextSensor1.setVisibility(View.INVISIBLE);
//				blueCircle.setVisibility(View.INVISIBLE);
//				mTextSensor2.setVisibility(View.INVISIBLE);
//				orangeCircle.setVisibility(View.INVISIBLE);
//				mTextSensor3.setVisibility(View.INVISIBLE);
//				greyCircle.setVisibility(View.INVISIBLE);
//				mTextSensor4.setVisibility(View.INVISIBLE);
//				greenCircle.setVisibility(View.INVISIBLE);
			}			 
			 else if(optionSelected.equals(ENABLE_SENSOR)){
				
				Shimmer shimmer = (Shimmer)mService.getShimmer(mBluetoothAddress);
				String[] sensors = shimmer.getListofSupportedSensors();
				String[] enableSensorsForListView = new String[sensors.length+2]; 
				
				if (shimmer.getShimmerVersion() == ShimmerVerDetails.HW_ID.SHIMMER_3){
					//remove from the list of sensors EXG1, EXG2, EXG1 16bits, EXG2 16bits, and add ECG,EMG,TestSignal,ECG 16Bits,EMG 16Bits, TestSignal 16Bits
					for(int i=0; i<sensors.length-5;i++)
						enableSensorsForListView[i] = sensors[i];

					enableSensorsForListView[sensors.length-5] = sensors[sensors.length-1]; // add "Strain gauge" which is the last one in the sensor list
					enableSensorsForListView[sensors.length-4] = "ECG";
					enableSensorsForListView[sensors.length-3] = "ECG 16Bit";
					enableSensorsForListView[sensors.length-2] = "EMG";
					enableSensorsForListView[sensors.length-1] = "EMG 16Bit";
					enableSensorsForListView[sensors.length] = "ExG Test Signal";
					enableSensorsForListView[sensors.length+1] = "ExG Test Signal 16Bit";
				} else if (shimmer.getShimmerVersion() == ShimmerVerDetails.HW_ID.SHIMMER_2 ||
						shimmer.getShimmerVersion() == ShimmerVerDetails.HW_ID.SHIMMER_2R){
					enableSensorsForListView = sensors;
				}
								
				showEnableSensors(enableSensorsForListView,mService.getEnabledSensors(mBluetoothAddress));
				
			} else if(optionSelected.equals(EDIT_GRAPH)){

				ShimmerDialogConfigurations.showSelectSensorPlot(ShimmerCapture.this,mService,mBluetoothAddress,dynamicPlot);
				
			} else if (optionSelected.equals(NEW_ENABLE_SENSOR)){
				ShimmerDialogConfigurations.buildShimmerSensorEnableDetails(mService.getShimmer(mBluetoothAddress),ShimmerCapture.this);
			} else if(optionSelected.equals(SHIMMER_CONFIGURATION)){
				
				Intent commandIntent=new Intent(getApplicationContext(), CommandsActivity.class);
				commandIntent.putExtra("BluetoothAddress",mBluetoothAddress);
				commandIntent.putExtra("SamplingRate",mService.getSamplingRate(mBluetoothAddress));
				commandIntent.putExtra("AccelerometerRange",mService.getAccelRange(mBluetoothAddress));
				commandIntent.putExtra("GSRRange",mService.getGSRRange(mBluetoothAddress));
				commandIntent.putExtra("BatteryLimit",mService.getBattLimitWarning(mBluetoothAddress));
				startActivityForResult(commandIntent, REQUEST_COMMAND_SHIMMER);
				
			} else if(optionSelected.equals(NEW_SHIMMER_CONFIGURATION)){
				//buildShimmerConfigOptions(mService.getShimmer(mBluetoothAddress));
				ShimmerDialogConfigurations.buildShimmerConfigOptions(mService.getShimmer(mBluetoothAddress),ShimmerCapture.this, mService.getBluetoothManager());

			} else if(optionSelected.equals(GRAPH_CONFIGURATION)){
				
				mEditXAxisLimit.setText(""+X_AXIS_LENGTH);
				graphConfigurationDialog.show();	
			}
			else if(optionSelected.equals(LOG_FILE)){
				
				mEditFileName.setText(mFileName);
				writeToFileDialog.show();			
			} else if(optionSelected.equals(DEVICE_INFO)){
				
				ShimmerDevice shimmerTemp = mService.getShimmer(mBluetoothAddress);
				String shimmerVersion="";
				switch(shimmerTemp.getShimmerVerObject().getHardwareVersion()){
					case ShimmerVerDetails.HW_ID.SHIMMER_2:
						shimmerVersion = "Shimmer 2";
					break;
					case ShimmerVerDetails.HW_ID.SHIMMER_2R:
						shimmerVersion = "Shimmer 2R";
					break;
					case ShimmerVerDetails.HW_ID.SHIMMER_3:
						shimmerVersion = "Shimmer 3";
					break;
					case ShimmerVerDetails.HW_ID.SHIMMER_SR30:
						shimmerVersion = "Shimmer SR30";
					break;
				};
				String FWName = shimmerTemp.getFirmwareVersionParsed();
				if(FWName.equals(""))
					FWName = "Unknown";
				
				mShimmerVersion.setText("Shimmer Version: "+shimmerVersion);
				mFirmwareVersion.setText("Firmware Version: "+FWName);
				deviceInfoDialog.setCancelable(true);
				deviceInfoDialog.show();
			}
		}
    	
    }



	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
    	switch (requestCode) {
    	case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
            	
                //setMessage("\nBluetooth is now enabled");
                Toast.makeText(this, "Bluetooth is now enabled", Toast.LENGTH_SHORT).show();
            } else {
                // User did not enable Bluetooth or an error occured
            	Toast.makeText(this, "Bluetooth not enabled\nExiting...", Toast.LENGTH_SHORT).show();
                finish();       
            }
            break;
    	case REQUEST_CONNECT_SHIMMER:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                String address = data.getExtras()
                        .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                Log.d("ShimmerActivity",address);
          		mService.connectShimmer(address);
          		mBluetoothAddress = address;
          		mService.setGraphHandler(mHandler);
                buttonMenu.setEnabled(false);
                //mShimmerDevice.connect(address,"gerdavax");
                //mShimmerDevice.setgetdatainstruction("a");
            }
            break;
    	case REQUEST_COMMAND_SHIMMER:
    		
    		if (resultCode == Activity.RESULT_OK) {
	    		if(data.getExtras().getBoolean("ToggleLED",false) == true)
	    		{
	    			mService.toggleAllLEDS();
	    		}
	    		
	    		if(data.getExtras().getDouble("SamplingRate",-1) != -1)
	    		{
	    			mService.writeSamplingRate(mBluetoothAddress, data.getExtras().getDouble("SamplingRate",-1));
	    			Log.d("ShimmerActivity",Double.toString(data.getExtras().getDouble("SamplingRate",-1)));
	    			mGraphSubSamplingCount=0;
	    		}
	    		
	    		if(data.getExtras().getInt("AccelRange",-1) != -1)
	    		{
	    			mService.writeAccelRange(mBluetoothAddress, data.getExtras().getInt("AccelRange",-1));
	    		}
	    		
	    		if(data.getExtras().getInt("GyroRange",-1) != -1)
	    		{
	    			mService.writeGyroRange(mBluetoothAddress, data.getExtras().getInt("GyroRange",-1));
	    		}
	    		
	    		if(data.getExtras().getInt("PressureResolution",-1) != -1)
	    		{
	    			mService.writePressureResolution(mBluetoothAddress, data.getExtras().getInt("PressureResolution",-1));
	    		}
	    		
	    		if(data.getExtras().getInt("MagRange",-1) != -1)
	    		{
	    			mService.writeMagRange(mBluetoothAddress, data.getExtras().getInt("MagRange",-1));
	    		}
	    		
	    		if(data.getExtras().getInt("GSRRange",-1) != -1)
	    		{
	    			mService.writeGSRRange(mBluetoothAddress,data.getExtras().getInt("GSRRange",-1));
	    		}
	    		if(data.getExtras().getDouble("BatteryLimit",-1) != -1)
	    		{
	    			mService.setBattLimitWarning(mBluetoothAddress, data.getExtras().getDouble("BatteryLimit",-1));
	    		}
	    		
    		}
    		menuListViewDialog.show();
    		break;
        }
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		
		if(itemId == R.id.menu_actionBar){
			
			arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.menu_item);
			if(mService.DevicesConnected(mBluetoothAddress)){
				arrayAdapter.add(DISCONNECT);
				if(mService.DeviceIsStreaming(mBluetoothAddress))
					arrayAdapter.add(STOP_STREAMING);
				else{
					arrayAdapter.add(START_STREAMING);
					ShimmerDevice sDevice = mService.getShimmer(mBluetoothAddress);
					if (sDevice.getHardwareVersion()!= ShimmerVerDetails.HW_ID.SHIMMER_4_SDK) {
						arrayAdapter.add(ENABLE_SENSOR);
						arrayAdapter.add(SHIMMER_CONFIGURATION);

					} else { //This works for Shimmer3 as Well but for backward compatibility
						arrayAdapter.add(NEW_ENABLE_SENSOR);
						arrayAdapter.add(NEW_SHIMMER_CONFIGURATION);

					}
					arrayAdapter.add(GRAPH_CONFIGURATION);
				}
				arrayAdapter.add(EDIT_GRAPH);
			}
			else
				arrayAdapter.add(CONNECT);
			
			arrayAdapter.add(LOG_FILE);
			
			OnOptionSelected oos = new OnOptionSelected();
			menuListViewDialog.setAdapter(arrayAdapter, oos);
			menuListViewDialog.show();
			
			return true;

		}
		else
			return true;
		
		
	}
	

	
	public static void setLegend(){

	}
	
	
	public void showEnableSensors(final String[] sensorNames, long enabledSensors){
		dialogEnabledSensors=enabledSensors;
		final Dialog mDialog = new Dialog(this);
		mDialog.setContentView(R.layout.dialog_enable_sensor_view);
		TextView title = (TextView) mDialog.findViewById(android.R.id.title);
 		title.setText("Enable Sensor");
 		final ListView listView = (ListView) mDialog.findViewById(android.R.id.list);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		ArrayAdapter<String> adapterSensorNames = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, sensorNames);
		listView.setAdapter(adapterSensorNames);
		final BiMap<String,String> sensorBitmaptoName;
		sensorBitmaptoName = Shimmer.generateBiMapSensorIDtoSensorName(mService.getShimmerVersion(mBluetoothAddress));
		for (int i=0;i<sensorNames.length;i++){
			if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && 
					(sensorNames[i].equals("ECG") || sensorNames[i].equals("EMG") || sensorNames[i].equals("ExG Test Signal") ||
					sensorNames[i].equals("ECG 16Bit") || sensorNames[i].equals("EMG 16Bit") || sensorNames[i].equals("ExG Test Signal 16Bit"))){
				
				if(sensorNames[i].equals("ECG") && mService.isEXGUsingECG24Configuration(mBluetoothAddress))
					listView.setItemChecked(i, true);
				else if(sensorNames[i].equals("ECG 16Bit") && mService.isEXGUsingECG16Configuration(mBluetoothAddress))
					listView.setItemChecked(i, true);
				else if(sensorNames[i].equals("EMG") && mService.isEXGUsingEMG24Configuration(mBluetoothAddress))
					listView.setItemChecked(i, true);
				else if(sensorNames[i].equals("EMG 16Bit") && mService.isEXGUsingEMG16Configuration(mBluetoothAddress))
					listView.setItemChecked(i, true);
				else if(sensorNames[i].equals("ExG Test Signal") && mService.isEXGUsingTestSignal24Configuration(mBluetoothAddress))
					listView.setItemChecked(i, true);
				else if(sensorNames[i].equals("ExG Test Signal 16Bit") && mService.isEXGUsingTestSignal16Configuration(mBluetoothAddress))
					listView.setItemChecked(i, true);
			}
			else{	
				int iDBMValue = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[i]));	
				if( (iDBMValue & enabledSensors) >0){
					listView.setItemChecked(i, true);
				}
			}
		}
				
		listView.setOnItemClickListener(new OnItemClickListener(){
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int clickIndex,
					long arg3) {
								
				if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && sensorNames[clickIndex].equals("ECG")){
					int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1"));
					int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2"));
					if (!((dialogEnabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (dialogEnabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0)){
						dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
						dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
					}
					
					if(!listView.isItemChecked(clickIndex)){
						listView.setItemChecked(clickIndex, false); //ECG
						dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
						dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
					}
					else
						listView.setItemChecked(clickIndex, true); //ECG
						listView.setItemChecked(clickIndex+1, false); //ECG 16Bit
						listView.setItemChecked(clickIndex+2, false);// EMG
						listView.setItemChecked(clickIndex+3, false);// EMG 16Bit
						listView.setItemChecked(clickIndex+4, false);// Test Signal
					listView.setItemChecked(clickIndex+5, false);// Test Signal 16Bit
					if(listView.isItemChecked(clickIndex))
						mService.writeEXGSetting(mBluetoothAddress, 0);
				} 
				else if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && sensorNames[clickIndex].equals("ECG 16Bit")){
						int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1 16Bit"));
						int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2 16Bit"));
						if (!((dialogEnabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (dialogEnabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0)){
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
						}
						
						if(!listView.isItemChecked(clickIndex)){
							listView.setItemChecked(clickIndex, false); //ECG 16Bit
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
							dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
						}
						else
							listView.setItemChecked(clickIndex, true); //ECG 16Bit
						
						listView.setItemChecked(clickIndex-1, false); //ECG
						listView.setItemChecked(clickIndex+1, false);// EMG
						listView.setItemChecked(clickIndex+2, false);// EMG 16Bit
						listView.setItemChecked(clickIndex+3, false);// Test Signal
						listView.setItemChecked(clickIndex+4, false);// Test Signal 16Bit
						if(listView.isItemChecked(clickIndex))
							mService.writeEXGSetting(mBluetoothAddress, 0);
				}
				else if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && sensorNames[clickIndex].equals("EMG")){
					int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1"));
					int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2"));
					if (!((dialogEnabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (dialogEnabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0)){
						dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
						dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
					}
					
					if(!listView.isItemChecked(clickIndex)){
						listView.setItemChecked(clickIndex, false); //EMG
						dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
						dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
					}
					else
						listView.setItemChecked(clickIndex, true); //EMG
					
					listView.setItemChecked(clickIndex-2, false); //ECG
					listView.setItemChecked(clickIndex-1, false);// ECG 16Bit
					listView.setItemChecked(clickIndex+1, false);// EMG 16Bit
					listView.setItemChecked(clickIndex+2, false);// Test Signal
					listView.setItemChecked(clickIndex+3, false);// Test Signal 16Bit
					if(listView.isItemChecked(clickIndex))
						mService.writeEXGSetting(mBluetoothAddress, 1);
				}
				else if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && sensorNames[clickIndex].equals("EMG 16Bit")){
					int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1 16Bit"));
					int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2 16Bit"));
					if (!((dialogEnabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (dialogEnabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0)){
						dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
						dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
					}
					
					if(!listView.isItemChecked(clickIndex)){
						listView.setItemChecked(clickIndex, false); //EMG 16Bit
						dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
						dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
					}
					else
						listView.setItemChecked(clickIndex, true); //EMG 16Bit
					
					listView.setItemChecked(clickIndex-3, false); //ECG
					listView.setItemChecked(clickIndex-2, false);// ECG 16Bit
					listView.setItemChecked(clickIndex-1, false);// EMG 
					listView.setItemChecked(clickIndex+1, false);// Test Signal
					listView.setItemChecked(clickIndex+2, false);// Test Signal 16Bit
					if(listView.isItemChecked(clickIndex))
						mService.writeEXGSetting(mBluetoothAddress, 1);
				}
				else if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && sensorNames[clickIndex].equals("ExG Test Signal")){
					int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1"));
					int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2"));
					if (!((dialogEnabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (dialogEnabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0)){
						dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
						dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
					}
					
					if(!listView.isItemChecked(clickIndex)){
						listView.setItemChecked(clickIndex, false); //Test Signal
						dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
						dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
					}
					else
						listView.setItemChecked(clickIndex, true); //Test Signal
					
					listView.setItemChecked(clickIndex-4, false); //ECG
					listView.setItemChecked(clickIndex-3, false);// ECG 16Bit
					listView.setItemChecked(clickIndex-2, false);// EMG 
					listView.setItemChecked(clickIndex-1, false);// EMG 16Bit
					listView.setItemChecked(clickIndex+1, false);// Test Signal 16Bit
					if(listView.isItemChecked(clickIndex))
					mService.writeEXGSetting(mBluetoothAddress, 2);
				}
				else if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && sensorNames[clickIndex].equals("ExG Test Signal 16Bit")){
					int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1 16Bit"));
					int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2 16Bit"));
					if (!((dialogEnabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (dialogEnabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0)){
						dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
						dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
					}
						
					if(!listView.isItemChecked(clickIndex)){
						listView.setItemChecked(clickIndex, false); //Test Signal 16Bit
						dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue1);
						dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, dialogEnabledSensors,iDBMValue3);
					}
					else
						listView.setItemChecked(clickIndex, true); //Test Signal 16Bit
						listView.setItemChecked(clickIndex-5, false); //ECG
						listView.setItemChecked(clickIndex-4, false);// ECG 16Bit
						listView.setItemChecked(clickIndex-3, false);// EMG 
						listView.setItemChecked(clickIndex-2, false);// EMG 16Bit
						listView.setItemChecked(clickIndex-1, false);// Test Signal
					if(listView.isItemChecked(clickIndex))
						mService.writeEXGSetting(mBluetoothAddress, 2);
				}
				else{
					int sensorIdentifier = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[clickIndex]));
					//check and remove any old daughter boards (sensors) which will cause a conflict with sensorIdentifier 
					dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress,dialogEnabledSensors,sensorIdentifier);
					//update the checkbox accordingly
					int end=0;
					if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3)
						end=sensorNames.length-6;
					else
						end=sensorNames.length;
					
					for (int i=0;i<end;i++){
						int iDBMValue = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[i]));	
						if( (iDBMValue & dialogEnabledSensors) >0){
							listView.setItemChecked(i, true);
						} else {
							listView.setItemChecked(i, false);
						}
					}
				}
	
			}
			
		});
		
		Button mDoneButton = (Button)mDialog.findViewById(R.id.buttonEnableSensors);
		mDoneButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mService.setEnabledSensors(dialogEnabledSensors,mBluetoothAddress);
				mDialog.dismiss();
				menuListViewDialog.show();
			}});
		
		
		mDialog.show();
 		
	}

	public String joinStrings(String[] a){
		String js="";
		for (int i=0;i<a.length;i++){
			if (i==0){
				js = a[i];
			} else{
				js = js + " " + a[i];
			}
		}
		return js;
	}
}