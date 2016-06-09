//v0.3 -  4 Sept 2014

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

package com.shimmerresearch.shimmergraphandlogservice;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import pl.flex_it.androidplot.XYSeriesShimmer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.DynamicTableModel;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XLayoutStyle;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.androidplot.xy.YLayoutStyle;
import com.google.common.collect.BiMap;
import com.shimmerresearch.tools.PlotManagerAndroid;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.service.ShimmerService;
import com.shimmerresearch.service.ShimmerService.LocalBinder;
import com.shimmerresearch.tools.Logging;


public class ShimmerAndroidDynamicGraphExample extends ServiceActivity {

	private static Context context;
	static final int REQUEST_ENABLE_BT = 1;
	static final int REQUEST_CONNECT_SHIMMER = 2;
	static final int REQUEST_CONFIGURE_SHIMMER = 3;
	static final int REQUEST_CONFIGURE_VIEW_SENSOR = 4;
	static final int REQUEST_COMMAND_SHIMMER = 5;
	static final int REQUEST_LOGFILE_SHIMMER = 6;
	private static XYPlot dynamicPlot;
	final static int X_AXIS_LENGTH = 500;
	static LineAndPointFormatter lineAndPointFormatter1, lineAndPointFormatter2, lineAndPointFormatter3;
	private Paint LPFpaint, transparentPaint, outlinePaint;
	static boolean[] mArrayOfCheckedSignals = null;
	//private DataMethods DM;
	List<String[]> mListofChannels;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Name of the connected device
    private static String mBluetoothAddress = null;
    // Member object for communication services
    private String mSignaltoGraph;
    private static String mSensorView = ""; //The sensor device which should be viewed on the graph
    private static int mGraphSubSamplingCount = 0; //10 
    private static String mFileName = "Default";
//    static Logging log = new Logging(mFileName,"\t"); //insert file name
    private static boolean mEnableLogging = false;
    
    Button mButtonSetPlotSignalFilter;
    Button mButtonResetPlotSignalFilter;
    EditText mEditTextSignalFilter;
    
    Dialog mDialog;
    long dialogEnabledSensors=0;
    /** Called when the activity is first created. */
    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        Log.d("ShimmerActivity","On Create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.d("ShimmerActivity","On Create");

        mDialog = new Dialog(this);
        
        
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
        if (android.os.Build.VERSION.SDK_INT >= 13){
          	Display display = this.getWindowManager().getDefaultDisplay();
          	Point size = new Point();
          	display.getSize(size);
          	int widgetpositionx=size.x/4;
          	int widgetpositiony=(int)size.y/4;
          	dynamicPlot.getLegendWidget().setSize(new SizeMetrics(widgetpositiony, SizeLayoutType.ABSOLUTE, widgetpositionx, SizeLayoutType.ABSOLUTE)); // 210 might have to be changed with the SR30
          	dynamicPlot.position(dynamicPlot.getLegendWidget(), 0, XLayoutStyle.ABSOLUTE_FROM_RIGHT, 30, YLayoutStyle.ABSOLUTE_FROM_BOTTOM, AnchorPosition.RIGHT_BOTTOM);
          	int numberofsignals=(int)widgetpositiony/30;
          	dynamicPlot.getLegendWidget().setTableModel(new DynamicTableModel(1, numberofsignals));
          	//remove below to enable the viewing of the legend
          	//dynamicPlot.getLegendWidget().setSize(new SizeMetrics(width, SizeLayoutType.ABSOLUTE, height, SizeLayoutType.ABSOLUTE)); 
          } else {
        dynamicPlot.getLegendWidget().setSize(new SizeMetrics(300, SizeLayoutType.ABSOLUTE, 300, SizeLayoutType.ABSOLUTE));
          }
        // thin out domain/range tick labels so they dont overlap each other:
        dynamicPlot.setTicksPerDomainLabel(5);
        dynamicPlot.setTicksPerRangeLabel(3);
        dynamicPlot.disableAllMarkup();
        Paint gridLinePaint = new Paint();
        gridLinePaint.setColor(Color.parseColor("#D6D6D6"));
        dynamicPlot.setRangeBoundaries(-15, 15, BoundaryMode.AUTO); // freeze the range boundary:
        dynamicPlot.setDomainBoundaries(0, X_AXIS_LENGTH, BoundaryMode.FIXED); // freeze the domain boundary:
        dynamicPlot.getGraphWidget().setMargins(0, 20, 10, 10);
        dynamicPlot.setBorderStyle(Plot.BorderStyle.NONE, null, null);
        dynamicPlot.getBackgroundPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.setBackgroundColor(Color.TRANSPARENT);
        dynamicPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.getGraphWidget().setGridLinePaint(transparentPaint);
        dynamicPlot.getGraphWidget().setDomainOriginLabelPaint(gridLinePaint);
        dynamicPlot.getGraphWidget().setDomainOriginLinePaint(gridLinePaint);
        dynamicPlot.getGraphWidget().setDomainLabelPaint(gridLinePaint);
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
        
        
      
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		 switch(metrics.densityDpi){
	     case DisplayMetrics.DENSITY_LOW:
	                break;
	     case DisplayMetrics.DENSITY_MEDIUM:
	    	         break;
	     case DisplayMetrics.DENSITY_HIGH:
	    	         break;
	     case DisplayMetrics.DENSITY_XHIGH:
	    	 break;
		 }
		 
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
	      
	      if (mBluetoothAddress!=null){

	      }
		 
		 
		 if (mEnableLogging==false){

		      } else if (mEnableLogging==true){

	      }
		      
		 
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
        	Toast.makeText(this, "Device does not support Bluetooth\nExiting...", Toast.LENGTH_LONG).show();
        	finish();
        }
        
        ShimmerAndroidDynamicGraphExample.context = getApplicationContext();
        
    }
    
    @Override
    public void onStart() {
    	super.onStart();

    	if(!mBluetoothAdapter.isEnabled()) {     	
        	Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    	}
    	else {
    		
    		
    		
    	}
    }
    
    
    
    @Override
	public void onPause() {
		super.onPause();
		Log.d("ShimmerActivity","On Pause");
		//finish();
		 
    	 
		if(mServiceBind == true){
  		  //getApplicationContext().unbindService(mTestServiceConnection); 
  	  }
	}

	public void onResume() {
		super.onResume();
		ShimmerAndroidDynamicGraphExample.context = getApplicationContext();
		Log.d("ShimmerActivity","On Resume");
		Intent intent=new Intent(this, ShimmerService.class);
  	  	Log.d("ShimmerH","on Resume");
  	  	getApplicationContext().bindService(intent,mTestServiceConnection, Context.BIND_AUTO_CREATE);
  	  	
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
	      		LocalBinder binder = (LocalBinder) service;
	      		mService = binder.getService();
	      		if (mService.mPlotManager==null){ 
	      			mService.mPlotManager = new PlotManagerAndroid(false);
	      		}
	      		mService.mPlotManager.updateDynamicPlot(dynamicPlot);
	      		mServiceBind = true;
	      		mService.setGraphHandler(mHandler);
	      		
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
            
            case Shimmer.MESSAGE_STATE_CHANGE:

                switch (((ObjectCluster)msg.obj).mState) {
                case CONNECTED:
                	Log.d("ShimmerActivity","Message Fully Initialized Received from Shimmer driver");
            		mBluetoothAddress=((ObjectCluster)msg.obj).getMacAddress();
            		mService.enableGraphingHandler(true);
            		mArrayOfCheckedSignals = null;
                    break;
                /*case INITIALISED:
                	
                    break;*/
                case CONNECTING:
                	Log.d("ShimmerActivity","Driver is attempting to establish connection with Shimmer device");

                    break;
                case STREAMING:
                	break;
                case STREAMING_AND_SDLOGGING:
                	break;
                case SDLOGGING:
               	 break;
                case DISCONNECTED:
            		Log.d("ShimmerActivity","Shimmer No State");

            		mBluetoothAddress=null;
            		// this also stops streaming
            		mService.mPlotManager.removeAllSignals();
            		mArrayOfCheckedSignals = null;
                    break;
                }
           	 
            	/*
            	switch (msg.arg1) {
            	case Shimmer.STATE_CONNECTED:
            		//this has been deprecated
            		break;
            	case Shimmer.MSG_STATE_FULLY_INITIALIZED:
            		Log.d("ShimmerActivity","Message Fully Initialized Received from Shimmer driver");
            		mTitle.setText(R.string.title_connected_to);
            		mBluetoothAddress=((ObjectCluster)msg.obj).mBluetoothAddress;
            		mTitle.append(mBluetoothAddress);    
            		mService.enableGraphingHandler(true);
            		mArrayOfCheckedSignals = null;

            		break;
            	case Shimmer.STATE_CONNECTING:
            		Log.d("ShimmerActivity","Driver is attempting to establish connection with Shimmer device");
            		mTitle.setText(R.string.title_connecting);
            		break;
            	case Shimmer.STATE_NONE:
            		Log.d("ShimmerActivity","Shimmer No State");
            		mTitle.setText(R.string.title_not_connected);;
            		mBluetoothAddress=null;
            		// this also stops streaming
            		mService.mPlotManager.removeAllSignals();
            		mArrayOfCheckedSignals = null;
            		break;
            	}
            	*/
                break;
            case Shimmer.MESSAGE_READ:
            	    if ((msg.obj instanceof ObjectCluster)){
            	    	try {
							mService.mPlotManager.filterDataAndPlot((ObjectCluster)msg.obj);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
            	    }
				
                break;
            case Shimmer.MESSAGE_ACK_RECEIVED:
            	
            	break;
            case Shimmer.MESSAGE_DEVICE_NAME:
                // save the connected device's name
                
                Toast.makeText(getContext(), "Connected to "
                               + mBluetoothAddress, Toast.LENGTH_SHORT).show();
                break;
       
            	
            case Shimmer.MESSAGE_TOAST:
                Toast.makeText(getContext(), msg.getData().getString(Shimmer.TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
           
            }
        }
    };
	
    private static Context getContext(){
    	return ShimmerAndroidDynamicGraphExample.context;
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
          		mService.connectShimmer(address, "ShimmerDevice1");
          		mBluetoothAddress = address;
          		mService.setGraphHandler(mHandler);
                
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
    		break;
    	case REQUEST_LOGFILE_SHIMMER:
    		if (resultCode == Activity.RESULT_OK) {
    			
    			//set the filename in the LogFile
    			mFileName=data.getExtras().getString("LogFileName");
    			if(!mFileName.equals(mService.mLogFileName)){
    				if(mService.DeviceIsStreaming(mBluetoothAddress)){
	    				mService.closeAndRemoveFile(mBluetoothAddress);
	    				Toast.makeText(getApplicationContext(), "Writing data to the new file: "+mFileName, Toast.LENGTH_SHORT).show();
    				}
    			}
    			mService.setLoggingName(mFileName);
    			mEnableLogging = data.getExtras().getBoolean("LogFileEnableLogging");
    			if (mEnableLogging==true){
    				mService.setEnableLogging(mEnableLogging);
    				if(mService.DeviceIsStreaming(mBluetoothAddress))
    					if(mFileName.equals("Default"))
    						Toast.makeText(getApplicationContext(), "Writing data to the defult file.\n The file can be found in the ShimmerGraphAndLog folder", Toast.LENGTH_SHORT).show();
    					else
    						Toast.makeText(getApplicationContext(), "Writing data to "+mFileName+".\n The file can be found in the ShimmerGraphAndLog folder", Toast.LENGTH_SHORT).show();
    			}
    			else{
					mService.setEnableLogging(mEnableLogging);
					mService.closeAndRemoveFile(mBluetoothAddress);
				}
    			
    			
    			
    			if (mEnableLogging==false){

    	        } else if (mEnableLogging==true){

    	        }
    			
    		}
    		break;
        }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		MenuItem streamItem = menu.findItem(R.id.stream);
		streamItem.setEnabled(false);
		MenuItem settingsItem = menu.findItem(R.id.settings);
		settingsItem.setEnabled(false);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	
		//disable graph edit for sensors which are not enabled
		
		MenuItem scanItem = menu.findItem(R.id.scan);
		MenuItem streamItem = menu.findItem(R.id.stream);
		MenuItem settingsItem = menu.findItem(R.id.settings);
		MenuItem commandsItem = menu.findItem(R.id.commands);
		MenuItem viewItem = menu.findItem(R.id.viewsensor);
		if((mService.DevicesConnected(mBluetoothAddress) == true)){
			scanItem.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			scanItem.setTitle(R.string.disconnect);
			streamItem.setIcon(R.drawable.ic_menu_play_clip);
			streamItem.setTitle(R.string.startstream);
			streamItem.setEnabled(true);
			settingsItem.setEnabled(true);
			commandsItem.setEnabled(true);
			viewItem.setEnabled(true);
		}
		else {
			scanItem.setIcon(android.R.drawable.ic_menu_search);
			scanItem.setTitle(R.string.connect);
			streamItem.setIcon(R.drawable.ic_menu_play_clip);
			streamItem.setEnabled(false);
			settingsItem.setEnabled(false);
			commandsItem.setEnabled(false);
			viewItem.setEnabled(false);
		}
		if(mService.DeviceIsStreaming(mBluetoothAddress) == true && mService.DevicesConnected(mBluetoothAddress) == true){
			streamItem.setIcon(R.drawable.ic_menu_stop);
			streamItem.setTitle(R.string.stopstream);
			
		}
		if(mService.DeviceIsStreaming(mBluetoothAddress) == false && mService.DevicesConnected(mBluetoothAddress) == true && mService.GetInstructionStatus(mBluetoothAddress)==true){
			streamItem.setIcon(R.drawable.ic_menu_play_clip);
			streamItem.setTitle(R.string.startstream);
		}	
		if (mService.GetInstructionStatus(mBluetoothAddress)==false || (mService.GetInstructionStatus(mBluetoothAddress)==false)){ 
			streamItem.setEnabled(false);
			settingsItem.setEnabled(false);
			commandsItem.setEnabled(false);
		}
		if (mService.DeviceIsStreaming(mBluetoothAddress)){
			settingsItem.setEnabled(false);
			commandsItem.setEnabled(false);
		}
		if (mService.GetInstructionStatus(mBluetoothAddress)==false)
		{
			streamItem.setEnabled(false);
			settingsItem.setEnabled(false);
			commandsItem.setEnabled(false);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.scan) {
			if ((mService.DevicesConnected(mBluetoothAddress) == true)) {
				if(mEnableLogging){
					mService.setEnableLogging(false);
					mService.closeAndRemoveFile(mBluetoothAddress);
				}
				mService.disconnectAllDevices();
				mService.mPlotManager.removeAllSignals();
				mArrayOfCheckedSignals = null;
			} else {
				Intent serverIntent = new Intent(this, DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_SHIMMER);
			}
			return true;
		} else if (itemId == R.id.stream) {
			if (mService.DeviceIsStreaming(mBluetoothAddress) == true) {
				mService.stopStreaming(mBluetoothAddress);
				mService.mPlotManager.removeAllSignals();
				mArrayOfCheckedSignals = null;
				if(mEnableLogging){
					mService.setEnableLogging(false);
					mService.closeAndRemoveFile(mBluetoothAddress);
				}
			} else {
				mService.startStreaming(mBluetoothAddress);
				//set the enable logging regarding the user selection
                mService.setEnableLogging(mEnableLogging);
			}
			return true;
		} else if (itemId == R.id.settings) {
     		Shimmer shimmer = mService.getShimmer(mBluetoothAddress);
			showEnableSensors(shimmer.getListofSupportedSensors(),mService.getEnabledSensors(mBluetoothAddress));
			return true;
		} else if (itemId == R.id.viewsensor) {
			showSelectSensorPlot();
			return true;
		} else if (itemId == R.id.commands) {
			Intent commandIntent=new Intent(this, CommandsActivity.class);
			commandIntent.putExtra("BluetoothAddress",mBluetoothAddress);
			commandIntent.putExtra("SamplingRate",mService.getSamplingRate(mBluetoothAddress));
			commandIntent.putExtra("AccelerometerRange",mService.getAccelRange(mBluetoothAddress));
			commandIntent.putExtra("GSRRange",mService.getGSRRange(mBluetoothAddress));
			commandIntent.putExtra("BatteryLimit",mService.getBattLimitWarning(mBluetoothAddress));
			startActivityForResult(commandIntent, REQUEST_COMMAND_SHIMMER);
			return true;
		} else if (itemId == R.id.logfile) {
			Intent logfileIntent=new Intent(this, LogFileActivity.class);
			logfileIntent.putExtra("LogFileEnableLogging",mEnableLogging);
			logfileIntent.putExtra("LogFileName",mFileName);
			startActivityForResult(logfileIntent, REQUEST_LOGFILE_SHIMMER);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
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
	
	public void showSelectSensorPlot(){
		mDialog.setContentView(R.layout.dialog_sensor_view);
		mDialog.setCanceledOnTouchOutside(true);
		TextView title = (TextView) mDialog.findViewById(android.R.id.title);
		title.setText("Select Signal");
		final ListView listView = (ListView) mDialog.findViewById(android.R.id.list);
		mButtonSetPlotSignalFilter = (Button) mDialog.findViewById(R.id.ButtonFilterPlotSignal);
        mButtonResetPlotSignalFilter = (Button) mDialog.findViewById(R.id.buttonResetFilterPlotSignal);
        mEditTextSignalFilter = (EditText) mDialog.findViewById(R.id.editTextFilterPlotSignal);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		mListofChannels = mService.getListofEnabledSensorSignals(mBluetoothAddress);
		List<String> sensorList = new ArrayList<String>();
		for(int i=0;i<mListofChannels.size();i++) {
			sensorList.add(joinStrings(mListofChannels.get(i)));
		}

		final String[] sensorNames = sensorList.toArray(new String[sensorList.size()]);

		final ArrayAdapter<String> adapterSensorNames = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, sensorNames);
		listView.setAdapter(adapterSensorNames);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		//listView.setItemChecked(position, value);
		
		for (int p=0;p<mListofChannels.size();p++){
			if (mService.mPlotManager.checkIfPropertyExist(mListofChannels.get(p))){
				listView.setItemChecked(p, true);
			}
		}
		
		mButtonSetPlotSignalFilter.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				List<String> sensorList = new ArrayList<String>();
				String plotSignaltoFilter  = mEditTextSignalFilter.getText().toString();
				
				for (int i=mListofChannels.size()-1;i>-1;i--){
					String signal = joinStrings(mListofChannels.get(i));
					if (!signal.toLowerCase().contains(plotSignaltoFilter.toLowerCase())){
						
						mListofChannels.remove(i);
					}
					
				}
				
				for(int i=0;i<mListofChannels.size();i++) {
					sensorList.add(joinStrings(mListofChannels.get(i)));
				}
				
				final String[] sensorNames = sensorList.toArray(new String[sensorList.size()]);
				ArrayAdapter<String> adapterSensorNames = new ArrayAdapter<String>(mDialog.getContext(), android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, sensorNames);
				listView.setAdapter(adapterSensorNames);
				listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
				
				for (int p=0;p<mListofChannels.size();p++){
					if (mService.mPlotManager.checkIfPropertyExist(mListofChannels.get(p))){
						listView.setItemChecked(p, true);
					}
				}
				
				listView.setOnItemClickListener(new OnItemClickListener(){

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int index,
							long arg3) {
						CheckedTextView cb = (CheckedTextView) arg1;
						if (cb.isChecked()){
							
							try {
								mService.mPlotManager.addSignal(mListofChannels.get(index), dynamicPlot);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							
							mService.mPlotManager.removeSignal(mListofChannels.get(index));
						}
					}

				});

			}
		});

		mButtonResetPlotSignalFilter.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mListofChannels = mService.getListofEnabledSensorSignals(mBluetoothAddress);
				List<String> sensorList = new ArrayList<String>();
				for(int i=0;i<mListofChannels.size();i++) {
					sensorList.add(joinStrings(mListofChannels.get(i)));
				}

				final String[] sensorNames = sensorList.toArray(new String[sensorList.size()]);

				final ArrayAdapter<String> adapterSensorNames = new ArrayAdapter<String>(mDialog.getContext(), android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, sensorNames);
				listView.setAdapter(adapterSensorNames);
				listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
				//listView.setItemChecked(position, value);
				
				for (int p=0;p<mListofChannels.size();p++){
					if (mService.mPlotManager.checkIfPropertyExist(mListofChannels.get(p))){
						listView.setItemChecked(p, true);
					}
				}
				
				listView.setOnItemClickListener(new OnItemClickListener(){

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int index,
							long arg3) {
						CheckedTextView cb = (CheckedTextView) arg1;
						if (!cb.isChecked()){
							
							try {
								mService.mPlotManager.addSignal(mListofChannels.get(index), dynamicPlot);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							
							mService.mPlotManager.removeSignal(mListofChannels.get(index));
						}
					}

				});

				
			}
		});
		
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index,
					long arg3) {
				CheckedTextView cb = (CheckedTextView) arg1;
				if (!cb.isChecked()){
					
					try {
						mService.mPlotManager.addSignal(mListofChannels.get(index), dynamicPlot);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					
					mService.mPlotManager.removeSignal(mListofChannels.get(index));
				}
			}

		});

		mDialog.show();

	}
	
	
	public void showEnableSensors(final String[] sensorNames, long enabledSensors){
		dialogEnabledSensors=enabledSensors;
		mDialog.setContentView(R.layout.dialog_enable_sensor_view);
		TextView title = (TextView) mDialog.findViewById(android.R.id.title);
 		title.setText("Select Signal");
 		final ListView listView = (ListView) mDialog.findViewById(android.R.id.list);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		ArrayAdapter<String> adapterSensorNames = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, sensorNames);
		listView.setAdapter(adapterSensorNames);
		final BiMap<String,String> sensorBitmaptoName;
		sensorBitmaptoName = Shimmer.generateBiMapSensorIDtoSensorName(mService.getShimmerVersion(mBluetoothAddress));
		for (int i=0;i<sensorNames.length;i++){
			int iDBMValue = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[i]));	
			if( (iDBMValue & enabledSensors) >0){
				listView.setItemChecked(i, true);
			}
		}
				
		listView.setOnItemClickListener(new OnItemClickListener(){
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int clickIndex,
					long arg3) {
					int sensorIdentifier = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[clickIndex]));
					//check and remove any old daughter boards (sensors) which will cause a conflict with sensorIdentifier 
					dialogEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress,dialogEnabledSensors,sensorIdentifier);
					//update the checkbox accordingly
					for (int i=0;i<sensorNames.length;i++){
						int iDBMValue = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[i]));	
						if( (iDBMValue & dialogEnabledSensors) >0){
							listView.setItemChecked(i, true);
						} else {
							listView.setItemChecked(i, false);
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
			}});
		
		
		mDialog.show();
 		
	}
	
	
}