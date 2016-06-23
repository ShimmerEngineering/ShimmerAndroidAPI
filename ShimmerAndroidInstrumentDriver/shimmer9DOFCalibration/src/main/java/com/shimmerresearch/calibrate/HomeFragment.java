package com.shimmerresearch.calibrate;

/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Ruaidhri Molloy
 * @date  May, 2014
 */

/**
 * This Fragment handles the Shimmer configuration including connect/disconnect/start/stop/configure actions
 * This Fragments also displays the data numerically and visually via a single plot 
 */


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import pl.flex_it.androidplot.XYSeriesShimmer9DOF;

import com.androidplot.Plot;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.slidingmenu.R;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.Configuration.Shimmer2;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class HomeFragment extends Fragment{
	
	private Button mConnectDisconnect, mStartStop, mSensor, mSensorRange, mMagPlot, mRawCal;
	static final int REQUEST_ENABLE_BT = 1, REQUEST_CONNECT_SHIMMER = 2, REQUEST_CONFIGURE_SHIMMER = 3, REQUEST_CONFIGURE_VIEW_SENSOR = 4;
	public static int SHIMMER_VERSION = -1;
	private static TextView mLegendNameA, mLegendNameB, mLegendNameC;
	private static TextView mSensorNameA, mSensorNameB, mSensorNameC, SensorValueA, SensorValueB, SensorValueC, SensorUnitA, SensorUnitB, SensorUnitC;
	private ImageView mCircleX, mCircleY, mCircleZ;
	private static String mSensorView = "", mConnectedDeviceName = null; //The sensor device which should be viewed on the graph
    private BluetoothAdapter mBluetoothAdapter = null;
    private static int mGraphSubSamplingCount = 0;
    private static Context context;
	static XYSeriesShimmer9DOF series1, series2, series3, magSeries;
	static LineAndPointFormatter lineAndPointFormatter1, lineAndPointFormatter2, lineAndPointFormatter3, magLineAndPointFormatter1, magLineAndPointFormatter2, magLineAndPointFormatter3;
    private static XYPlot dynamicPlot;
    final static int X_AXIS_LENGTH = 500;
	static List<Number> data1, data2, data3;
	private Paint LPFpaint, transparentPaint, outlinePaint;
	
	public HomeFragment(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        
		if(getActivity() instanceof MainActivity) {
			((MainActivity) getActivity()).setHandler(HomeHandler);
		}

		context = getActivity().getApplicationContext();
		
		// Import elements from the layout xml
		mConnectDisconnect = (Button) rootView.findViewById(R.id.buttonConnectDisconnect);
		mStartStop = (Button) rootView.findViewById(R.id.buttonStartStop);
		mSensor = (Button) rootView.findViewById(R.id.buttonSensor);
		mMagPlot = (Button) rootView.findViewById(R.id.buttonMagPlot);
		mRawCal = (Button) rootView.findViewById(R.id.buttonRawCal);
		
		dynamicPlot = (XYPlot) rootView.findViewById(R.id.dynamicPlot);
		
		mSensorRange = (Button) rootView.findViewById(R.id.buttonSensorRange);
		mSensorNameA = (TextView) rootView.findViewById(R.id.tvSensorNameA);
		mSensorNameB = (TextView) rootView.findViewById(R.id.tvSensorNameB);
		mSensorNameC = (TextView) rootView.findViewById(R.id.tvSensorNameC);
		SensorValueA = (TextView) rootView.findViewById(R.id.tvSensorValueA);
		SensorValueB = (TextView) rootView.findViewById(R.id.tvSensorValueB);
		SensorValueC = (TextView) rootView.findViewById(R.id.tvSensorValueC);
		SensorUnitA = (TextView) rootView.findViewById(R.id.tvSensorUnitA);
		SensorUnitB = (TextView) rootView.findViewById(R.id.tvSensorUnitB);
		SensorUnitC = (TextView) rootView.findViewById(R.id.tvSensorUnitC);
		
		mLegendNameA = (TextView) rootView.findViewById(R.id.tvLegendNameA);
		mLegendNameB = (TextView) rootView.findViewById(R.id.tvLegendNameB);
		mLegendNameC = (TextView) rootView.findViewById(R.id.tvLegendNameC);
		
		mCircleX = (ImageView) rootView.findViewById(R.id.ivCircleX);
		mCircleY = (ImageView) rootView.findViewById(R.id.ivCircleY);
		mCircleZ = (ImageView) rootView.findViewById(R.id.ivCircleZ);
		
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        if(mBluetoothAdapter == null) {
        	Toast.makeText(getActivity(), "Device does not support Bluetooth\nExiting...", Toast.LENGTH_LONG).show();
        	getActivity().finish();
        }
		
		// Register buttons for click event
		
		mConnectDisconnect.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
            	// Perform action on click
            	if((MainActivity.mShimmerDevice != null) && (MainActivity.mShimmerDevice.getBluetoothRadioState() == BT_STATE.CONNECTED)){
            		MainActivity.mShimmerDevice.stop();
            	}
            	else{
            		Intent intent = new Intent(getActivity(), DeviceListActivity.class);
            		startActivityForResult(intent, REQUEST_CONNECT_SHIMMER);
            	}
             }
         });
		
		mStartStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	if (MainActivity.mShimmerDevice.getStreamingStatus() == true) {
            		MainActivity.mShimmerDevice.stopStreaming();
    			} else {
    				MainActivity.mShimmerDevice.startStreaming();
    			}
            }
        });
		
		mSensor.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	Intent intent = new Intent(getActivity(), RadioButtonListActivity.class);
            	intent.putExtra("intentMsg", "Sensor");
                startActivityForResult(intent, REQUEST_CONFIGURE_VIEW_SENSOR);
            }
        });
		
		mSensorRange.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	Intent intent = new Intent(getActivity(), RadioButtonListActivity.class);
            	intent.putExtra("intentMsg", "SensorRange");
                startActivityForResult(intent, REQUEST_CONFIGURE_SHIMMER);
            }
        });
		
		mMagPlot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	Intent intent = new Intent(getActivity(), RadioButtonListActivity.class);
            	intent.putExtra("intentMsg", "MagPlot");
            	startActivityForResult(intent, REQUEST_CONFIGURE_VIEW_SENSOR);
            }
        });
		
		mRawCal.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	Intent intent = new Intent(getActivity(), RadioButtonListActivity.class);
            	intent.putExtra("intentMsg", "RawCal");
            	startActivityForResult(intent, REQUEST_CONFIGURE_VIEW_SENSOR);
            	
            }
        });
		
		initPlot();
		updateUI();
				
        return rootView;
    }
	
	// Ensure Bluetooth is powered ON on app startup
	
    @Override
    public void onStart() {
    	super.onStart();
    	if(!mBluetoothAdapter.isEnabled()) {     	
        	Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    	}
    	else {
    		if(MainActivity.mShimmerDevice == null); //setupMain();
    	}
    }
   
    private void clearPlot(){
    	Log.d("Thresh", "plotCLEAR!");
		data1.clear();
		data2.clear();
		data3.clear();
		dynamicPlot.redraw();
    }
    
    private void initPlot(){
    	
		// create arraylists to store data to pass to the plot
		
 		data1= new ArrayList<Number>();
 		data2= new ArrayList<Number>();
 		data3= new ArrayList<Number>();
 		data1.clear();
 		data2.clear();
 		data3.clear();
 		series1 = new XYSeriesShimmer9DOF(data1, 1, "Series1");
 		series2 = new XYSeriesShimmer9DOF(data2, 2, "Series2");
 		series3 = new XYSeriesShimmer9DOF(data3, 3, "Series3");
 		magSeries = new XYSeriesShimmer9DOF(data1, data2, 4, "MagPlot");
		// get handles to our View defined in layout.xml:
       
        // only display whole numbers in domain labels
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
        magLineAndPointFormatter1 = new LineAndPointFormatter(Color.TRANSPARENT, Color.rgb(51, 153, 255), null);
        magLineAndPointFormatter2 = new LineAndPointFormatter(Color.TRANSPARENT, Color.rgb(245, 146, 107), null);
        magLineAndPointFormatter3 = new LineAndPointFormatter(Color.TRANSPARENT, Color.rgb(150, 150, 150), null);
        transparentPaint = new Paint();
        transparentPaint.setColor(Color.TRANSPARENT);
        //lineAndPointFormatter1.setLinePaint(p);
        dynamicPlot.setDomainStepMode(XYStepMode.SUBDIVIDE);
        //dynamicPlot.setDomainStepValue(series1.size());
        dynamicPlot.getLegendWidget().setSize(new SizeMetrics(0, SizeLayoutType.ABSOLUTE, 0, SizeLayoutType.ABSOLUTE));
        // thin out domain/range tick labels so they dont overlap each other:
        dynamicPlot.setTicksPerDomainLabel(3);
        dynamicPlot.setTicksPerRangeLabel(3);
        dynamicPlot.disableAllMarkup();
        Paint gridLinePaint = new Paint();
        gridLinePaint.setColor(getResources().getColor(R.color.text_color));
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
        dynamicPlot.clear();
        
        if (RadioButtonListActivity.mRadioButtonSensorPosition==2){
        	dynamicPlot.setRangeBoundaries(-15, 15, BoundaryMode.AUTO); // freeze the range boundary:
            dynamicPlot.setDomainBoundaries(-15, 15, BoundaryMode.AUTO); // freeze the domain boundary:
            
            if(RadioButtonListActivity.mRadioButtonMagPlotPosition==0) dynamicPlot.addSeries(magSeries, magLineAndPointFormatter1);
            else if (RadioButtonListActivity.mRadioButtonMagPlotPosition==1) dynamicPlot.addSeries(magSeries, magLineAndPointFormatter2);
            else dynamicPlot.addSeries(magSeries, magLineAndPointFormatter3);
        }
        else{
        	dynamicPlot.setRangeBoundaries(-15, 15, BoundaryMode.AUTO); // freeze the range boundary:
            dynamicPlot.setDomainBoundaries(0, X_AXIS_LENGTH, BoundaryMode.FIXED); // freeze the domain boundary:
        	dynamicPlot.addSeries(series1, lineAndPointFormatter1);
            dynamicPlot.addSeries(series2, lineAndPointFormatter2);
            dynamicPlot.addSeries(series3, lineAndPointFormatter3);
        }
    }
    
	private void updateUI(){
	
		//RadioButtonListActivity.mRadioButtonPlotFormatPosition

		if (RadioButtonListActivity.mRadioButtonSensorPosition==2){ // mag sensor selected
			mCircleX.setVisibility(View.INVISIBLE);
			mCircleZ.setVisibility(View.INVISIBLE);
			mLegendNameA.setVisibility(View.INVISIBLE);
			mLegendNameC.setVisibility(View.INVISIBLE);
			if (RadioButtonListActivity.mRadioButtonMagPlotPosition==0) mCircleY.setBackgroundResource(R.drawable.circle_blue);
			else if (RadioButtonListActivity.mRadioButtonMagPlotPosition==1) mCircleY.setBackgroundResource(R.drawable.circle_orange);
			else mCircleY.setBackgroundResource(R.drawable.circle_grey);
		}
		else{
			mCircleX.setVisibility(View.VISIBLE);
			mCircleZ.setVisibility(View.VISIBLE);
			mLegendNameA.setVisibility(View.VISIBLE);
			mLegendNameC.setVisibility(View.VISIBLE);
			mCircleY.setBackgroundResource(R.drawable.circle_orange);
		}
		
		mRawCal.setText(getResources().getStringArray(R.array.raw_cal_plot_strings)[RadioButtonListActivity.mRadioButtonPlotFormatPosition]);
		mMagPlot.setText(getResources().getStringArray(R.array.mag_plot_strings)[RadioButtonListActivity.mRadioButtonMagPlotPosition]);
		mMagPlot.setEnabled(false);
		
		if(MainActivity.mShimmerDevice != null){
			
			SHIMMER_VERSION = MainActivity.mShimmerDevice.getShimmerVersion();
			
				if(SHIMMER_VERSION==3) mSensor.setText(getResources().getStringArray(R.array.shimmer3_calibration_strings)[RadioButtonListActivity.mRadioButtonSensorPosition]);
				else mSensor.setText(getResources().getStringArray(R.array.shimmer2r_calibration_strings)[RadioButtonListActivity.mRadioButtonSensorPosition]);	
		}
		
		if(MainActivity.mShimmerDevice != null && MainActivity.mShimmerDevice.getBluetoothRadioState() == BT_STATE.CONNECTED){
				
			switch (RadioButtonListActivity.mRadioButtonSensorPosition){
			
				case 0: // accel/ln accel selected
					
					switch (SHIMMER_VERSION){
					
					case 1:
						mSensorRange.setEnabled(true);
						mMagPlot.setEnabled(false);
						mSensorRange.setText(getResources().getStringArray(R.array.shimmer2_accel_range_strings)[RadioButtonListActivity.mRadioButtonAccelRangePosition]);
					break;
				
					case 2:
						mSensorRange.setEnabled(true);
						mMagPlot.setEnabled(false);
						mSensorRange.setText(getResources().getStringArray(R.array.shimmer2r_accel_range_strings)[RadioButtonListActivity.mRadioButtonAccelRangePosition]);
					break;
				
					default:
						mSensorRange.setEnabled(false);
						mMagPlot.setEnabled(false);
						mSensorRange.setText(getResources().getString(R.string.not_applicable));
					break;
					}
					
				break;
				
				case 1: // gyro selected
					
					if(SHIMMER_VERSION==3){
						mSensorRange.setText(getResources().getStringArray(R.array.gyro_range_strings)[RadioButtonListActivity.mRadioButtonGyroRangePosition]);
						mSensorRange.setEnabled(true);
					}
					else{
						mSensorRange.setText(getResources().getString(R.string.not_applicable));
						mSensorRange.setEnabled(false);
					}
						mMagPlot.setEnabled(false);
				break;
				
				case 2: // mag selected
					
					switch(SHIMMER_VERSION){
					
					case 1:
						mSensorRange.setText(getResources().getStringArray(R.array.shimmer2_mag_range_strings)[RadioButtonListActivity.mRadioButtonMagRangePosition]);
					break;
					
					case 2:
						mSensorRange.setText(getResources().getStringArray(R.array.shimmer2r_mag_range_strings)[RadioButtonListActivity.mRadioButtonMagRangePosition]);
					break;
					
					case 3:
						mSensorRange.setText(getResources().getStringArray(R.array.shimmer3_mag_range_strings)[RadioButtonListActivity.mRadioButtonMagRangePosition]);
					break;
					}
					mSensorRange.setEnabled(true);
					mMagPlot.setEnabled(true);
				break;
				
				case 3: // WR selected
					
					if(SHIMMER_VERSION==3){
						mSensorRange.setText(getResources().getStringArray(R.array.shimmer3_accel_range_strings)[RadioButtonListActivity.mRadioButtonAccelRangePosition]);
						mSensorRange.setEnabled(true);
					}
					mMagPlot.setEnabled(false);
				break;
			
			}
		}
		else{
			mSensorRange.setEnabled(false);
		}
		
		if(MainActivity.mShimmerDevice != null && MainActivity.mShimmerDevice.getBluetoothRadioState() == BT_STATE.CONNECTED){
			mConnectDisconnect.setText("DISCONNECT");
			mConnectDisconnect.setEnabled(true);
			mStartStop.setEnabled(true);
			mSensor.setEnabled(true);
			mRawCal.setEnabled(true);
			//mSensorRange.setEnabled(true);
		}
		else if (MainActivity.mShimmerDevice != null && MainActivity.mShimmerDevice.getBluetoothRadioState() == BT_STATE.CONNECTING){ 
			mConnectDisconnect.setText("CONNECTING");
			mConnectDisconnect.setEnabled(false);
			mStartStop.setEnabled(false);
			mSensor.setEnabled(false);
			mRawCal.setEnabled(false);
		}
		else if (MainActivity.mShimmerDevice != null && MainActivity.mShimmerDevice.getBluetoothRadioState() == BT_STATE.DISCONNECTED){ 
			mConnectDisconnect.setText("CONNECT");
			mConnectDisconnect.setEnabled(true);
			mStartStop.setEnabled(false);
			mSensor.setEnabled(false);
			mRawCal.setEnabled(false);
			clearPlot();

			mLegendNameA.setText(getResources().getStringArray(R.array.legend_strings)[0]);
			mSensorNameA.setText(getResources().getStringArray(R.array.sensor_strings)[0]);
			SensorValueA.setText(getResources().getStringArray(R.array.value_strings)[0]);
			SensorUnitA.setText(getResources().getStringArray(R.array.unit_strings)[0]);

			mLegendNameB.setText(getResources().getStringArray(R.array.legend_strings)[1]);
			mSensorNameB.setText(getResources().getStringArray(R.array.sensor_strings)[1]);
			SensorValueB.setText(getResources().getStringArray(R.array.value_strings)[1]);
			SensorUnitB.setText(getResources().getStringArray(R.array.unit_strings)[1]);
			
			mLegendNameC.setText(getResources().getStringArray(R.array.legend_strings)[2]);
			mSensorNameC.setText(getResources().getStringArray(R.array.sensor_strings)[2]);
			SensorValueC.setText(getResources().getStringArray(R.array.value_strings)[2]);
			SensorUnitC.setText(getResources().getStringArray(R.array.unit_strings)[2]);
			
			mCircleX.setVisibility(View.VISIBLE);
			mCircleZ.setVisibility(View.VISIBLE);
			mLegendNameA.setVisibility(View.VISIBLE);
			mLegendNameC.setVisibility(View.VISIBLE);
			mCircleY.setBackgroundResource(R.drawable.circle_orange);
			
		}
		
		if(MainActivity.mShimmerDevice != null && MainActivity.mShimmerDevice.getStreamingStatus()==true){ 
			mStartStop.setText("STOP");
			mConnectDisconnect.setEnabled(true);
			mSensor.setEnabled(false);
			mSensorRange.setEnabled(false);
			mRawCal.setEnabled(true);
		}
		else{ 
			mStartStop.setText("START");
		}

	}

	private Handler HomeHandler = new Handler(){

	        @Override
	        public void handleMessage(Message msg) {

	        	Log.d("msg", Integer.toString(msg.what));
	        	
	        	switch (msg.what) {
	            
	            case Shimmer.MESSAGE_STATE_CHANGE:
	            	 switch (((ObjectCluster)msg.obj).mState) {
	                
	                case CONNECTED:
	                	updateUI();
	                    break;
	                case CONNECTING:
	                	Toast.makeText(context, R.string.title_connecting, Toast.LENGTH_LONG).show();
	                	updateUI();
	                    break;
	                case DISCONNECTED:
	                	Toast.makeText(context, R.string.title_not_connected, Toast.LENGTH_LONG).show();
	                	updateUI();
	                    // this also stops streaming
	                    break;
	                /*case Shimmer.MSG_STATE_FULLY_INITIALIZED:
	                	updateUI();
	                    break;*/
	                case STREAMING:
	                	updateUI();
	                    break;
	                /*case Shimmer.MSG_STATE_STOP_STREAMING:
	                	updateUI();
                    	break;*/
	                default:
	            		break;
	                }
	                break;
	            case Shimmer.MESSAGE_READ:
	        	    if ((msg.obj instanceof ObjectCluster)){
	        	    ObjectCluster objectCluster =  (ObjectCluster) msg.obj; 
	        		double[] dataArray = new double[0];
	        		String[] sensorName = new String[0];
	        		String[] legendName = new String[0];
	        		String[] sensorNameShort = new String[3];
	        		String units="";
	        		int scaleFactor=1;
	        		
	        		if (RadioButtonListActivity.mRadioButtonSensorPosition==2) scaleFactor = 3;
	        		
	        		//mSensorView determines which sensor to graph
	        		if (RadioButtonListActivity.mRadioButtonSensorPosition==0){
	        			sensorName = new String[3]; // for x y and z axis
	        			sensorNameShort = new String[3]; // for x y and z axis
	        			legendName = new String[3];
	        			dataArray = new double[3];
	        			
	        			if(SHIMMER_VERSION==3){
	        				sensorName[0] = Shimmer3.ObjectClusterSensorName.ACCEL_LN_X;
		        			sensorName[1] = Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y;
		        			sensorName[2] = Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z;
		        			sensorNameShort[0] = "LN Accel X";
		        			sensorNameShort[1] = "LN Accel Y";
		        			sensorNameShort[2] = "LN Accel Z";
		        			legendName[0] = "LN Accel X";
		        			legendName[1] = "LN Accel Y";
		        			legendName[2] = "LN Accel Z";
	        			}
	        			else{
	        				sensorName[0] = Shimmer2.ObjectClusterSensorName.ACCEL_X;
		        			sensorName[1] = Shimmer2.ObjectClusterSensorName.ACCEL_Y;
		        			sensorName[2] = Shimmer2.ObjectClusterSensorName.ACCEL_Z;
		        			sensorNameShort[0] = "Accel X";
		        			sensorNameShort[1] = "Accel Y";
		        			sensorNameShort[2] = "Accel Z";
		        			legendName[0] = "Accel X";
		        			legendName[1] = "Accel Y";
		        			legendName[2] = "Accel Z";
	        			}
	        			units="u12"; // units are just merely an indicator to correct the graph
	        		}
	        		if (RadioButtonListActivity.mRadioButtonSensorPosition==1){
	        			sensorName = new String[3]; // for x y and z axis
	        			legendName = new String[3];
	        			dataArray = new double[3];
	        			if(SHIMMER_VERSION==3){
		        			sensorName[0] = Shimmer3.ObjectClusterSensorName.GYRO_X;
		        			sensorName[1] = Shimmer3.ObjectClusterSensorName.GYRO_Y;
		        			sensorName[2] = Shimmer3.ObjectClusterSensorName.GYRO_Z;
	        			} else {
	        				sensorName[0] = Shimmer2.ObjectClusterSensorName.GYRO_X;
		        			sensorName[1] = Shimmer2.ObjectClusterSensorName.GYRO_Y;
		        			sensorName[2] = Shimmer2.ObjectClusterSensorName.GYRO_Z;
	        			}
	        			sensorNameShort[0] = "Gyro X";
	        			sensorNameShort[1] = "Gyro Y";
	        			sensorNameShort[2] = "Gyro Z";
	        			legendName[0] = "Gyro X";
	        			legendName[1] = "Gyro Y";
	        			legendName[2] = "Gyro Z";
	        			units="i16";
	        		}
	        		if (RadioButtonListActivity.mRadioButtonSensorPosition==2){
	        			sensorName = new String[3]; // for x y and z axis
	        			legendName = new String[3];
	        			dataArray = new double[3];
	        			if(SHIMMER_VERSION==3){
		        			sensorName[0] = Shimmer3.ObjectClusterSensorName.MAG_X;
		        			sensorName[1] = Shimmer3.ObjectClusterSensorName.MAG_Y;
		        			sensorName[2] = Shimmer3.ObjectClusterSensorName.MAG_Z;
	        			} else {
	        				sensorName[0] = Shimmer2.ObjectClusterSensorName.MAG_X;
		        			sensorName[1] = Shimmer2.ObjectClusterSensorName.MAG_Y;
		        			sensorName[2] = Shimmer2.ObjectClusterSensorName.MAG_Z;
	        			}
	        			sensorNameShort[0] = "Mag X";
	        			sensorNameShort[1] = "Mag Y";
	        			sensorNameShort[2] = "Mag Z";
	        			legendName[0] = "";
	        			legendName[1] = "";
	        			legendName[2] = "";
			 	    	if (RadioButtonListActivity.mRadioButtonMagPlotPosition==0) legendName[1] = "Mag XY";
			 	    	else if (RadioButtonListActivity.mRadioButtonMagPlotPosition==1) legendName[1] = "Mag YZ";
			 	    	else legendName[1] = "Mag ZX";
	        			units="i12";
	        		}
	        		if (RadioButtonListActivity.mRadioButtonSensorPosition==3){
	        			sensorName = new String[3]; // for x y and z axis
	        			legendName = new String[3];
	        			dataArray = new double[3];
	        			sensorName[0] = Shimmer3.ObjectClusterSensorName.ACCEL_WR_X;
	        			sensorName[1] = Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y;
	        			sensorName[2] = Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z;
	        			sensorNameShort[0] = "WR Accel X";
	        			sensorNameShort[1] = "WR Accel Y";
	        			sensorNameShort[2] = "WR Accel Z";
	        			legendName[0] = "WR Accel X";
	        			legendName[1] = "WR Accel X";
	        			legendName[2] = "WR Accel X";
	        			units="i16";
	        		}
	        		
	        		String deviceName = objectCluster.getShimmerName();
	        		Collection<FormatCluster> ofFormats;
	        		FormatCluster formatCluster;

	        		//log data
	            		//if (deviceName=="Shimmer" && sensorName.length!=0){  // Device 1 is the assigned user id, see constructor of the Shimmer
	        		if (sensorName.length!=0){  // Device 1 is the assigned user id, see constructor of the Shimmer
	        			ofFormats = objectCluster.getCollectionOfFormatClusters(sensorName[0]);  // first retrieve all the possible formats for the current sensor device

	        			if (RadioButtonListActivity.mRadioButtonPlotFormatPosition==0) formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,CHANNEL_TYPE.UNCAL.toString())); 
	        			else formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,CHANNEL_TYPE.CAL.toString()));

	        			if (formatCluster != null) {

	        				dataArray[0] = formatCluster.mData;
	        				units = formatCluster.mUnits;

	        				if (data1.size()>(X_AXIS_LENGTH)*scaleFactor){
	        					data1.clear();
	        				}

	        				data1.add(dataArray[0]); //Obtain data for graph
	        			}

	        			ofFormats = objectCluster.getCollectionOfFormatClusters(sensorName[1]);  // first retrieve all the possible formats for the current sensor device

	        			if (RadioButtonListActivity.mRadioButtonPlotFormatPosition==0) formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,CHANNEL_TYPE.UNCAL.toString()));
	        			else formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,CHANNEL_TYPE.CAL.toString()));

	        			if (formatCluster != null ) {
	        				dataArray[1] = formatCluster.mData;

	        				if (data2.size()>(X_AXIS_LENGTH)*scaleFactor){
	        					data2.clear();
	        				}

	        				data2.add(dataArray[1]); //Obtain data for graph
	        			}

	        			ofFormats = objectCluster.getCollectionOfFormatClusters(sensorName[2]);  // first retrieve all the possible formats for the current sensor device

	        			if (RadioButtonListActivity.mRadioButtonPlotFormatPosition==0) formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,CHANNEL_TYPE.UNCAL.toString()));
	        			else formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,CHANNEL_TYPE.CAL.toString())); 
	        			if (formatCluster != null) {
	        				dataArray[2] = formatCluster.mData;

	        				if (data3.size()>(X_AXIS_LENGTH)*scaleFactor){
	        					data3.clear();
	        				}

	        				data3.add(dataArray[2]); //Obtain data for graph
	        			}	

	        			if (RadioButtonListActivity.mRadioButtonSensorPosition==2){ // mag sensor selected
	        				if (RadioButtonListActivity.mRadioButtonMagPlotPosition==0) magSeries.updateData(data1, data2);
	        				else if (RadioButtonListActivity.mRadioButtonMagPlotPosition==1) magSeries.updateData(data2, data3);
	        				else if	(RadioButtonListActivity.mRadioButtonMagPlotPosition==2) magSeries.updateData(data3, data1);
	        			}
	        			else{
	        				series1.updateData(data1);
	        				series2.updateData(data2);
	        				series3.updateData(data3);
	        			}

	        			//in order to prevent LAG the number of data points plotted is REDUCED
	        			int maxNumberofSamplesPerSecond=50; //Change this to increase/decrease the number of samples which are graphed
	        			int subSamplingCount=0;
	        			if (MainActivity.mShimmerDevice.getSamplingRateShimmer()>maxNumberofSamplesPerSecond){
	        				subSamplingCount=(int) (MainActivity.mShimmerDevice.getSamplingRateShimmer()/maxNumberofSamplesPerSecond);
	        				mGraphSubSamplingCount++;
	        			}
	        			if (mGraphSubSamplingCount==subSamplingCount){
	        				//mGraph.setDataWithAdjustment(dataArray,"Shimmer : " + deviceName,units);
	        				if (dataArray.length>0) {
	        					mSensorNameA.setText(sensorNameShort[0]);
	        					SensorValueA.setText(String.format("%.2f",dataArray[0]));
	        					SensorUnitA.setText(units);
	        					mLegendNameA.setText(legendName[0]);
	        				}
	        				if (dataArray.length>1) {
	        					mSensorNameB.setText(sensorNameShort[1]);
	        					SensorValueB.setText(String.format("%.2f",dataArray[1]));
	        					SensorUnitB.setText(units);
	        					mLegendNameB.setText(legendName[1]);
	        				}
	        				if (dataArray.length>2) {
	        					mSensorNameC.setText(sensorNameShort[2]);
	        					SensorValueC.setText(String.format("%.2f",dataArray[2]));
	        					SensorUnitC.setText(units);
	        					mLegendNameC.setText(legendName[2]);
	        				}
	        				mGraphSubSamplingCount=0;
	        			}
	        		}
	            		
	            		dynamicPlot.redraw();
	            	}
	                break;
	            case Shimmer.MESSAGE_ACK_RECEIVED:  	
	            	break;
	            case Shimmer.MESSAGE_DEVICE_NAME:
	                // save the connected device's name
	                mConnectedDeviceName = MainActivity.mShimmerDevice.getBluetoothAddress();
	                Toast.makeText(context, "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
	                break;
	            case Shimmer.MESSAGE_TOAST:
	                break;
	            default:
	        		break;
	            }
	        }
	    };
	
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
    	switch (requestCode) {
    	case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
            	if(MainActivity.mShimmerDevice == null) //setupMain();
                Toast.makeText(HomeFragment.this.getActivity(), "Bluetooth is now enabled", Toast.LENGTH_SHORT).show();
            } else {
                // User did not enable Bluetooth or an error occured
            	Toast.makeText(HomeFragment.this.getActivity(), "Bluetooth not enabled\nExiting...", Toast.LENGTH_SHORT).show();
                getActivity().finish();     
            }
            break;
    	case REQUEST_CONNECT_SHIMMER:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                //BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                MainActivity.mShimmerDevice.connect(address,"default");
                Log.d("bt", address);
            }
            break;
    	case REQUEST_CONFIGURE_SHIMMER:
            // When DeviceListActivity returns with a range setting for device
            if (resultCode == Activity.RESULT_OK) {
            	if(RadioButtonListActivity.mRadioButtonSensorPosition==0)MainActivity.mShimmerDevice.writeAccelRange(RadioButtonListActivity.mRadioButtonAccelRangePosition);
            	else if(RadioButtonListActivity.mRadioButtonSensorPosition==1)MainActivity.mShimmerDevice.writeGyroRange(RadioButtonListActivity.mRadioButtonGyroRangePosition);
            	else if (RadioButtonListActivity.mRadioButtonSensorPosition==2)MainActivity.mShimmerDevice.writeMagRange(RadioButtonListActivity.mRadioButtonMagRangePosition);
            	else MainActivity.mShimmerDevice.writeAccelRange(RadioButtonListActivity.mRadioButtonAccelRangePosition);
            	updateUI();
            }
            break;
    	case REQUEST_CONFIGURE_VIEW_SENSOR:
    		if (resultCode == Activity.RESULT_OK) {
    			updateUI();
    			clearPlot();
    			initPlot();
    		}
    		break;
    	default:
    		break;
    	}
	}
}

	

