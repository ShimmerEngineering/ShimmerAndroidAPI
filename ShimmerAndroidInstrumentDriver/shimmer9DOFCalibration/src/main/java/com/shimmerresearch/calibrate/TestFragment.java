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
 * This Fragment is used to test code and is not current created in the app, it's just an example!
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import pl.flex_it.androidplot.XYSeriesShimmer9DOF;

import com.androidplot.series.XYSeries;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.slidingmenu.R;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class TestFragment extends Fragment {
	
	public Button mConnectDisconnect, mStartStop, mSensor, mSensorRange;
	static final int REQUEST_ENABLE_BT = 1, REQUEST_CONNECT_SHIMMER = 2, REQUEST_CONFIGURE_SHIMMER = 3, REQUEST_CONFIGURE_VIEW_SENSOR = 4;
	private static TextView mTitle, mValueSensor1, mValueSensor2, mValueSensor3, mTextSensor1, mTextSensor2, mTextSensor3;
	private static String mSensorView = ""; //The sensor device which should be viewed on the graph
    private static Shimmer mShimmerDevice = null; // Member object for communication services
    private BluetoothAdapter mBluetoothAdapter = null;
    private Intent mIntent;
    private static XYPlot xyPlot;
    private XYSeriesShimmer9DOF series;
    private LineAndPointFormatter series1Format;
    private Button button1, button2, button3;
    private int mMagCalibrationSampleSize=5000, mNavDrawerItemPosition;
    private double[][] mMagDataXYZ = new double [mMagCalibrationSampleSize][3];
    private static int counter=0;
    private Boolean mCalibrateFlag=false, flag=false;
    private ArrayList<Number> data1, data2;
    private LinearLayout plot, button, llplots, lltextviews;
	
	public TestFragment(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		if(getActivity() instanceof MainActivity) {
			mNavDrawerItemPosition = ((MainActivity) getActivity()).getNavDrawerPosition();
		}
	
		View rootView = inflater.inflate(R.layout.fragment_mess, container, false);
        
        // Import plot from the layout
        //xyPlot = (XYPlot) rootView.findViewById(R.id.plot);
        
        //xyPlot.setDomainBoundaries(-600, 600, BoundaryMode.AUTO); // freeze the domain boundary:
        //xyPlot.setRangeBoundaries(-600, 600, BoundaryMode.AUTO);
        
        //button1 = (Button) rootView.findViewById(R.id.btn1);
        //button2 = (Button) rootView.findViewById(R.id.btn2);
        //button3 = (Button) rootView.findViewById(R.id.btn3);
        
        //plot = (LinearLayout) rootView.findViewById(R.id.llplot);
        //button = (LinearLayout) rootView.findViewById(R.id.llbtn);
        //llplots = (LinearLayout) rootView.findViewById(R.id.llplots);
        //lltextviews = (LinearLayout) rootView.findViewById(R.id.lltextviews);


        
	     LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	     
	     // adjust button weights based on active navigation drawer, magnetometer i.e. navigation drawer 3 has different UI
	    /*
        button1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click
				flag=!flag;
				
				if(flag){
					llplots.setVisibility(View.GONE);
					lltextviews.setVisibility(View.VISIBLE);
				}
				else{
					lltextviews.setVisibility(View.GONE);
					llplots.setVisibility(View.VISIBLE);
				}
			}
		});
        */
        /*
 		data1 = new ArrayList<Number>();
 		data2 = new ArrayList<Number>();
 		data1.clear();
 		data2.clear();
        series = new XYSeriesShimmer(data1, data2, 0, "MagPlot");
        series1Format = new LineAndPointFormatter(Color.TRANSPARENT, Color.BLACK, null); // line color, point color, fill color
        xyPlot.addSeries(series, series1Format);
        */
        
        
        return rootView;
    }
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		updateUI(mNavDrawerItemPosition);
	}

    private void updateUI(int mNavDrawerItemPosition) {
    	
	}

	private void clearPlot(){
		data1.clear();
		data2.clear();
		xyPlot.redraw();
    }
	/*
    private Handler testHandler = new Handler() {
		
        @Override
        public void handleMessage(Message msg) {
        	
        	switch (msg.what) {
            
            case Shimmer.MESSAGE_READ:
        	    if ((msg.obj instanceof ObjectCluster)){
        	    ObjectCluster objectCluster =  (ObjectCluster) msg.obj; 
        		int[] data = new int[0];
        		String[] sensorName = new String[0];
        		int sampleSize = 0;
        		
        		sampleSize = mMagCalibrationSampleSize;

        		//mSensorView determines which sensor to graph
        		
        		sensorName = new String[3]; // for x y and z axis
        		data = new int[3];
        		sensorName[0] = "Magnetometer X";
        		sensorName[1] = "Magnetometer Y";
        		sensorName[2] = "Magnetometer Z";

        		String deviceName = objectCluster.mMyName;
        		Collection<FormatCluster> ofFormats = null;
        		FormatCluster formatCluster;

        		//seriesName = getActivity().getResources().getStringArray(R.array.sensor_calibration_strings)[RadioButtonListActivity.mRadioButtonSensorPosition];
        		//log data
   		     	
   		     		if (deviceName=="Shimmer" && sensorName.length!=0){  // Device 1 is the assigned user id, see constructor of the Shimmer
            			
   		     			if(mCalibrateFlag){
   		     				
   		     				//if(counter<sampleSize){
					 	    			
   		     						ofFormats = objectCluster.mPropertyCluster.get(sensorName[0]);  // first retrieve all the possible formats for the current sensor device
   		     						//formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"CAL"));
   		     						data[0] = (int) ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"RAW")).mData; 
   		     						if(data[0]!=0){
   		     							mMagDataXYZ[counter][0] = data[0];	
   		     						}
					 	    	
   		     						ofFormats = objectCluster.mPropertyCluster.get(sensorName[1]);  // first retrieve all the possible formats for the current sensor device
   		     						//formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"CAL")); 
   		     						data[1] =(int) ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"RAW")).mData; 	
   		     						if(data[1]!=0){
   		     							mMagDataXYZ[counter][1] = data[1];
   		     						}
				 	    		
   		     						ofFormats = objectCluster.mPropertyCluster.get(sensorName[2]);
   		     						data[2] =(int) ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"RAW")).mData; 	
   		     						if(data[2]!=0){
   		     							mMagDataXYZ[counter][2] = data[2];
   		     						}
				 	    			counter++;
				 	    			
				 	    			if(RadioButtonListActivity.mRadioButtonMagPlotPosition==1){ // XY Plot
				 	    				data1.add(data[0]);
					 	    			data2.add(data[1]);
				 	    			}
				 	    			else if(RadioButtonListActivity.mRadioButtonMagPlotPosition==2){ // YZ Plot
				 	    				data1.add(data[1]);
					 	    			data2.add(data[2]);
				 	    			}
				 	    			else{ // ZX Plot
				 	    				data1.add(data[2]);
					 	    			data2.add(data[0]);
				 	    			}

				 	    			series.updateData(data1, data2);
   		     			}
   		     			xyPlot.redraw();
   		     		}
        	    }

            case Shimmer.MESSAGE_ACK_RECEIVED:  	
            	break;
      
            default:
        		break;
            }
        }
    };
	*/
	 
}

