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
 * This Fragment handles the calibration of the accelerometer(s), gyroscope and magnetometer
 */

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.RealMatrix;

import pl.flex_it.androidplot.XYSeriesShimmer9DOF;

import com.androidplot.Plot;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.driver.Configuration.Shimmer2;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.slidingmenu.R;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class CalibrateFragment extends Fragment {

	public Button mButtonCalibrate, mButtonCalibrateSetting, mButtonSave;
	private EditText mOffsetA, mOffsetB, mOffsetC; 
	private EditText mSensitivityA1, mSensitivityA2, mSensitivityA3, mSensitivityB1, mSensitivityB2, mSensitivityB3, mSensitivityC1, mSensitivityC2, mSensitivityC3;
	private EditText mAlignmentA1, mAlignmentA2, mAlignmentA3, mAlignmentB1, mAlignmentB2, mAlignmentB3, mAlignmentC1, mAlignmentC2, mAlignmentC3;
	private TextView mShimmerOffsetA, mShimmerOffsetB, mShimmerOffsetC; 
	private TextView mShimmerSensitivityA1, mShimmerSensitivityA2, mShimmerSensitivityA3, mShimmerSensitivityB1, mShimmerSensitivityB2, mShimmerSensitivityB3, mShimmerSensitivityC1, mShimmerSensitivityC2, mShimmerSensitivityC3;
	private TextView mShimmerAlignmentA1, mShimmerAlignmentA2, mShimmerAlignmentA3, mShimmerAlignmentB1, mShimmerAlignmentB2, mShimmerAlignmentB3, mShimmerAlignmentC1, mShimmerAlignmentC2, mShimmerAlignmentC3;
	private int mNavDrawerItemPosition, row, col=0;
	private int mCalibrationSampleSize=100, mDataValueX=0, mDataValueY=0, mDataValueZ=0;
	private int mGyroCalibrationSampleSize=1536;
	private int mMagCalibrationSampleSize=1536;
	private View rootView;
	private Intent mIntent;
	private Context context;
	private double[] mLNXplusG, mLNXminusG, mLNYplusG, mLNYminusG, mLNZplusG, mLNZminusG;
	private double[] mWRXplusG, mWRXminusG, mWRYplusG, mWRYminusG, mWRZplusG, mWRZminusG;
	RealMatrix accel_Ua_plus, accel_Ua_minus, accel_Ua_sum, accel_Ua_diff, accel_KR, accel_KRT, accel_KRxKRT, accel_Ka, accel_KaInv, accel_Ra;
	RealMatrix gyro_W, gyro_Angle, gyro_KR, gyro_KRT, gyro_KRxKRT, gyro_K, gyro_KInv, gyro_R;
	private final double[] gyro_angle1 = {360, 0, 0};
	private final double[] gyro_angle2 = {0, 360, 0};
	private final double[] gyro_angle3 = {0, 0, 360};
	private double[] mTimestamp, mGyroStationary, mGyroRotateX, mGyroRotateY, mGyroRotateZ, mGyroRotateXYZsum, mGyroRotateXsum, mGyroRotateYsum, mGyroRotateZsum;
	private double[] mTimestampArray = new double[mGyroCalibrationSampleSize];
	private double[] mTimestampDiffArray;
	private double[][] mGyroDataXYZ = new double [mGyroCalibrationSampleSize][3];
	private double[] mMagX, mMagY, mMagZ;
	private double[] mMagOptimisation = new double [6];
	private double[][] mMagDataXYZ = new double [mMagCalibrationSampleSize][3];
	private double[][] mMagRotateXYZ;
	private static int[] mOffset = new int[3];
	private int[] mShimmerOffset = {0, 0, 0};
	private byte[] mOffsetBytes = new byte [6];
	private static double[][] mSensitivity = new double[3][3];
	private double[][] mShimmerSensitivity = new double[3][3];
	private byte[] mSensitivityBytes = new byte [6];
	private int SensitivityScaleFactor=1;
	private static double[][] mAlignment = new double[3][3];
	private double[][] mShimmerAlignment = new double[3][3];
	private byte[] mAlignmentBytes = new byte [9];
	private byte[] mCalibrationBytes = new byte [21];
	private double[] mDataArrayMean = new double[3];
	private double[][] mOffsetTemp = new double[3][3];
	private static Boolean mCalibrateFlag=false, magViewFlag=false;
	private static int counter=0, refreshCounter=1, mRefreshLimit =  10;
	
	private static XYPlot magPlot1, magPlot2, magPlot3;
	private static XYSeriesShimmer9DOF magSeries1, magSeries2, magSeries3;
	private static LineAndPointFormatter magLineAndPointFormatter1, magLineAndPointFormatter2, magLineAndPointFormatter3;
    final static int X_AXIS_LENGTH = 500;
	static List<Number> magData1, magData2, magData3;
	private Paint LPFpaint, transparentPaint, outlinePaint;
	
	private LinearLayout textViews, plots;
	
	public CalibrateFragment(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {	
		
		if(getActivity() instanceof MainActivity) {
			mNavDrawerItemPosition = ((MainActivity) getActivity()).getNavDrawerPosition();
			((MainActivity) getActivity()).setHandler(CalibrateHandler);
		}
		
		context = getActivity().getApplicationContext();
		
		// initialize offset, sensitivity, alignment to zero when fragment opened
		
		for(int i=0; i<3; i++){
			mOffset[i] = 0;
			for(int j=0; j<3; j++){
				mSensitivity[i][j] = 0;
				mAlignment[i][j] = 0;
			}
		}
		
			 //rootView = inflater.inflate(R.layout.fragment_calibrate, container, false);
			 rootView = inflater.inflate(R.layout.fragment_layouts, container, false);
			
			 // import buttons from xml 
			
			 textViews = (LinearLayout) rootView.findViewById(R.id.lltextViews);
			 plots = (LinearLayout) rootView.findViewById(R.id.llxyPlots);
			 
			 if(mNavDrawerItemPosition!=3){
				 textViews.setVisibility(View.VISIBLE);
				 plots.setVisibility(View.GONE);
			 }
			 else{
				 textViews.setVisibility(View.GONE);
				 plots.setVisibility(View.VISIBLE);
			 }
			 
			 mButtonCalibrateSetting = (Button) rootView.findViewById(R.id.buttonCalibrateSetting);
			 mButtonCalibrate = (Button) rootView.findViewById(R.id.buttonCalibrate);
		     mButtonSave = (Button) rootView.findViewById(R.id.buttonSave);
		     
		     magPlot1 = (XYPlot) rootView.findViewById(R.id.xyPlotMag1);
		     magPlot2 = (XYPlot) rootView.findViewById(R.id.xyPlotMag2);
		     magPlot3 = (XYPlot) rootView.findViewById(R.id.xyPlotMag3);

		     LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		     
		     // adjust button weights based on active navigation drawer, magnetometer i.e. navigation drawer 3 has different UI
		     
		     //if(mNavDrawerItemPosition!=3){
		    	 params.weight = 1;
		    	 int marginDP =  dpToPx(8);
		    	 params.setMargins(marginDP, marginDP, marginDP, marginDP);
		    	 mButtonCalibrateSetting.setLayoutParams(params);
		    	 mButtonCalibrate.setLayoutParams(params);
		     //}
		     /*
		     else{
		    	 params.weight = 2;
		    	 mButtonCalibrateSetting.setLayoutParams(params);
		     }
			*/
		     // import elements from xml 

		     mOffsetA = (EditText) rootView.findViewById(R.id.etOffsetA);
		     mOffsetB = (EditText) rootView.findViewById(R.id.etOffsetB);
		     mOffsetC = (EditText) rootView.findViewById(R.id.etOffsetC);
				
		     mSensitivityA1 = (EditText) rootView.findViewById(R.id.etSensitivityA1);
		     mSensitivityA2 = (EditText) rootView.findViewById(R.id.etSensitivityA2);
		     mSensitivityA3 = (EditText) rootView.findViewById(R.id.etSensitivityA3);
		     mSensitivityB1 = (EditText) rootView.findViewById(R.id.etSensitivityB1);
		     mSensitivityB2 = (EditText) rootView.findViewById(R.id.etSensitivityB2);
		     mSensitivityB3 = (EditText) rootView.findViewById(R.id.etSensitivityB3);
		     mSensitivityC1 = (EditText) rootView.findViewById(R.id.etSensitivityC1);
		     mSensitivityC2 = (EditText) rootView.findViewById(R.id.etSensitivityC2);
		     mSensitivityC3 = (EditText) rootView.findViewById(R.id.etSensitivityC3);
				
		     mAlignmentA1 = (EditText) rootView.findViewById(R.id.etAlignmentA1);
		     mAlignmentA2 = (EditText) rootView.findViewById(R.id.etAlignmentA2);
		     mAlignmentA3 = (EditText) rootView.findViewById(R.id.etAlignmentA3);
		     mAlignmentB1 = (EditText) rootView.findViewById(R.id.etAlignmentB1);
		     mAlignmentB2 = (EditText) rootView.findViewById(R.id.etAlignmentB2);
		     mAlignmentB3 = (EditText) rootView.findViewById(R.id.etAlignmentB3);
		     mAlignmentC1 = (EditText) rootView.findViewById(R.id.etAlignmentC1);
		     mAlignmentC2 = (EditText) rootView.findViewById(R.id.etAlignmentC2);
		     mAlignmentC3 = (EditText) rootView.findViewById(R.id.etAlignmentC3);
		     
		     mShimmerOffsetA = (TextView) rootView.findViewById(R.id.tvShimmerOffsetA);
		     mShimmerOffsetB = (TextView) rootView.findViewById(R.id.tvShimmerOffsetB);
		     mShimmerOffsetC = (TextView) rootView.findViewById(R.id.tvShimmerOffsetC);
		     
		     mShimmerSensitivityA1 = (TextView) rootView.findViewById(R.id.tvShimmerSensitivityA1);
		     mShimmerSensitivityA2 = (TextView) rootView.findViewById(R.id.tvShimmerSensitivityA2);
		     mShimmerSensitivityA3 = (TextView) rootView.findViewById(R.id.tvShimmerSensitivityA3);
		     mShimmerSensitivityB1 = (TextView) rootView.findViewById(R.id.tvShimmerSensitivityB1);
		     mShimmerSensitivityB2 = (TextView) rootView.findViewById(R.id.tvShimmerSensitivityB2);
		     mShimmerSensitivityB3 = (TextView) rootView.findViewById(R.id.tvShimmerSensitivityB3);
		     mShimmerSensitivityC1 = (TextView) rootView.findViewById(R.id.tvShimmerSensitivityC1);
		     mShimmerSensitivityC2 = (TextView) rootView.findViewById(R.id.tvShimmerSensitivityC2);
		     mShimmerSensitivityC3 = (TextView) rootView.findViewById(R.id.tvShimmerSensitivityC3);
				
		     mShimmerAlignmentA1 = (TextView) rootView.findViewById(R.id.tvShimmerAlignmentA1);
		     mShimmerAlignmentA2 = (TextView) rootView.findViewById(R.id.tvShimmerAlignmentA2);
		     mShimmerAlignmentA3 = (TextView) rootView.findViewById(R.id.tvShimmerAlignmentA3);
		     mShimmerAlignmentB1 = (TextView) rootView.findViewById(R.id.tvShimmerAlignmentB1);
		     mShimmerAlignmentB2 = (TextView) rootView.findViewById(R.id.tvShimmerAlignmentB2);
		     mShimmerAlignmentB3 = (TextView) rootView.findViewById(R.id.tvShimmerAlignmentB3);
		     mShimmerAlignmentC1 = (TextView) rootView.findViewById(R.id.tvShimmerAlignmentC1);
		     mShimmerAlignmentC2 = (TextView) rootView.findViewById(R.id.tvShimmerAlignmentC2);
		     mShimmerAlignmentC3 = (TextView) rootView.findViewById(R.id.tvShimmerAlignmentC3);
		     
		     mLNXplusG = new double[]  {0, 0, 0};
		     mLNXminusG = new double[] {0, 0, 0};
		     mLNYplusG = new double[]  {0, 0, 0};
		     mLNYminusG = new double[] {0, 0, 0};
		     mLNZplusG = new double[]  {0, 0, 0};
		     mLNZminusG = new double[] {0, 0, 0};
		     
		     mWRXplusG = new double[]  {0, 0, 0};
		     mWRXminusG = new double[] {0, 0, 0};
		     mWRYplusG = new double[]  {0, 0, 0};
		     mWRYminusG = new double[] {0, 0, 0};
		     mWRZplusG = new double[]  {0, 0, 0};
		     mWRZminusG = new double[] {0, 0, 0};
		     
		     accel_Ua_plus = MatrixUtils.createRealMatrix(3,3);
		     accel_Ua_minus = MatrixUtils.createRealMatrix(3,3);
		     accel_Ua_sum  = MatrixUtils.createRealMatrix(3,3);
		     accel_Ua_diff = MatrixUtils.createRealMatrix(3,3);
		     accel_KR = MatrixUtils.createRealMatrix(3,3);
		     accel_Ka = MatrixUtils.createRealMatrix(3,3);
		     accel_KaInv = MatrixUtils.createRealMatrix(3,3);
		     accel_Ra = MatrixUtils.createRealMatrix(3,3);
		     
		     mGyroStationary = new double[mCalibrationSampleSize];
		     mGyroRotateXYZsum = new double[] {0, 0, 0};
		     mGyroRotateXsum = new double[] {0, 0, 0};
		     mGyroRotateYsum = new double[] {0, 0, 0};
		     mGyroRotateZsum = new double[] {0, 0, 0};
		     
		     gyro_W = MatrixUtils.createRealMatrix(3,3);
		     gyro_Angle = MatrixUtils.createRealMatrix(3,3);
		     gyro_Angle.setColumn(0, gyro_angle1);
		     gyro_Angle.setColumn(1, gyro_angle2);
		     gyro_Angle.setColumn(2, gyro_angle3);
		     gyro_KR = MatrixUtils.createRealMatrix(3,3);
		     gyro_KRT = MatrixUtils.createRealMatrix(3,3);
		     gyro_KRxKRT = MatrixUtils.createRealMatrix(3,3);
		     gyro_K = MatrixUtils.createRealMatrix(3,3);
		     gyro_KInv = MatrixUtils.createRealMatrix(3,3);
		     gyro_R = MatrixUtils.createRealMatrix(3,3);
		     
		     getCalibrationParameters(mNavDrawerItemPosition);
		     updateUI(mNavDrawerItemPosition);
		     initPlots();
		        
		        mButtonCalibrateSetting.setOnClickListener(new View.OnClickListener() {
		            public void onClick(View v) {
		                // Perform action on click
		            	mIntent = new Intent(getActivity(), RadioButtonListActivity.class);
		            	if(mNavDrawerItemPosition == 1) mIntent.putExtra("intentMsg", "Accel");
		            	else if (mNavDrawerItemPosition == 2) mIntent.putExtra("intentMsg", "Gyro");
		            	else if (mNavDrawerItemPosition == 3){ 
		            		
		            		magViewFlag = !magViewFlag;
		            		if(magViewFlag){
		            			plots.setVisibility(View.GONE);
								textViews.setVisibility(View.VISIBLE);
		            		}
		            		else{
		            			plots.setVisibility(View.VISIBLE);
								textViews.setVisibility(View.GONE);
		            		}	
		            		updateUI(mNavDrawerItemPosition);
		            	}
		            	else mIntent.putExtra("intentMsg", "WRAccel");
		            	
		            	if (mNavDrawerItemPosition != 3)startActivityForResult(mIntent, mNavDrawerItemPosition);
		            }
		        });
		
				mButtonCalibrate.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						// Perform action on click
						mCalibrateFlag = !mCalibrateFlag;
						if(mCalibrateFlag){
							if(MainActivity.mShimmerDevice.getStreamingStatus()!=true){
								MainActivity.mShimmerDevice.startStreaming();
							}
							if(mNavDrawerItemPosition!=3) Toast.makeText(context, "Calibrating: "+mButtonCalibrateSetting.getText().toString(), Toast.LENGTH_LONG).show();
							else Toast.makeText(context, "Calibrating: MAG XYX", Toast.LENGTH_LONG).show();
							updateUI(mNavDrawerItemPosition);
							clearPlots();
						}
						else{
							MainActivity.mShimmerDevice.stopStreaming();
		     				updateCalibrationDataArray(mTimestampArray, mDataArrayMean, mGyroDataXYZ, mMagDataXYZ, counter);
		     				counter=0;
						}
					}
				});
				
				mButtonSave.setOnClickListener(new View.OnClickListener(){
					public void onClick(View v) {
					// Perform action on click
					Log.d("SaveFlag", "SaveFlag");
					writeCalibrationParameters();
					}
				});
				
        return rootView;
    }

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		updateUI(mNavDrawerItemPosition);
	}
	
	public int dpToPx(int dp) {
	    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
	    int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));       
	    return px;
	}
	
	private void updateCalibrationDataArray(double[] timestampArray, double[] dataArrayMean, double[][] gyroData, double[][] magData, int counter){
		
		if(mNavDrawerItemPosition==1){
			
			if(mButtonCalibrateSetting.getText().toString().equals("X+g")){
				mLNXplusG = Arrays.copyOf(dataArrayMean, dataArrayMean.length);
			}
			else if(mButtonCalibrateSetting.getText().toString().equals("X-g")){
				mLNXminusG = Arrays.copyOf(dataArrayMean, dataArrayMean.length);
			}
			else if(mButtonCalibrateSetting.getText().toString().equals("Y+g")){
				mLNYplusG = Arrays.copyOf(dataArrayMean, dataArrayMean.length);
			}
			else if(mButtonCalibrateSetting.getText().toString().equals("Y-g")){
				mLNYminusG = Arrays.copyOf(dataArrayMean, dataArrayMean.length);
			}
			else if(mButtonCalibrateSetting.getText().toString().equals("Z+g")){
				mLNZplusG = Arrays.copyOf(dataArrayMean, dataArrayMean.length);
			}
			else{
				mLNZminusG = Arrays.copyOf(dataArrayMean, dataArrayMean.length);
			}
					
			accel_Ua_plus.setColumn(0, mLNXplusG);
			accel_Ua_plus.setColumn(1, mLNYplusG);
			accel_Ua_plus.setColumn(2, mLNZplusG);
			
			accel_Ua_minus.setColumn(0, mLNXminusG);
			accel_Ua_minus.setColumn(1, mLNYminusG);
			accel_Ua_minus.setColumn(2, mLNZminusG);
			
			calibrateAccel();
		}

		else if(mNavDrawerItemPosition==2){
			
			mTimestamp = new double[counter];
			mTimestamp = Arrays.copyOf(timestampArray, counter);
			
		    mTimestampDiffArray = new double[counter];
		    mGyroRotateX = new double[counter];
		    mGyroRotateY = new double[counter];
		    mGyroRotateZ = new double[counter];
			
		    mGyroRotateXYZsum = new double[] {0, 0, 0};
		    
			for(int i=0; i<mTimestamp.length-1; i++){
				mTimestampDiffArray[i] = mTimestamp[i+1] - mTimestamp[i];
			}
			
			if(mButtonCalibrateSetting.getText().toString().equals("STATIONARY")){
				mGyroStationary = Arrays.copyOf(dataArrayMean, dataArrayMean.length);
			}
				
			else{
				for(int i=0; i<counter; i++){
					mGyroRotateX[i] = (gyroData[i][0] - mGyroStationary[0]) * mTimestampDiffArray[i];
					mGyroRotateY[i] = (gyroData[i][1] - mGyroStationary[1]) * mTimestampDiffArray[i];
					mGyroRotateZ[i] = (gyroData[i][2] - mGyroStationary[2]) * mTimestampDiffArray[i];
					
					mGyroRotateXYZsum[0] +=mGyroRotateX[i];
					mGyroRotateXYZsum[1] +=mGyroRotateY[i];
					mGyroRotateXYZsum[2] +=mGyroRotateZ[i];
				}
				if(mButtonCalibrateSetting.getText().toString().equals("ROTATE X")){
						mGyroRotateXsum = Arrays.copyOf(mGyroRotateXYZsum, mGyroRotateXYZsum.length);
				}else if(mButtonCalibrateSetting.getText().toString().equals("ROTATE Y")){
						mGyroRotateYsum = Arrays.copyOf(mGyroRotateXYZsum, mGyroRotateXYZsum.length);
				}else 	mGyroRotateZsum = Arrays.copyOf(mGyroRotateXYZsum, mGyroRotateXYZsum.length);
			}
			
			gyro_W.setColumn(0, mGyroRotateXsum);
			gyro_W.setColumn(1, mGyroRotateYsum);
			gyro_W.setColumn(2, mGyroRotateZsum);
			
			calibrateGyro();
		}
		
		else if(mNavDrawerItemPosition==3){
					
				mMagRotateXYZ = new double [counter][3];
				mMagX = new double [counter];
				mMagY = new double [counter];
				mMagZ = new double [counter];
				
				mMagRotateXYZ = Arrays.copyOf(magData, magData.length);
				
				for(int i=0; i<counter; i++){
					mMagX[i] = mMagRotateXYZ[i][0];
					mMagY[i] = mMagRotateXYZ[i][1];
					mMagZ[i] = mMagRotateXYZ[i][2];
				}
				
				Arrays.sort(mMagX);
				Arrays.sort(mMagY);
				Arrays.sort(mMagZ);	
				
				int magXzero=0, magYzero=0, magZzero=0;
				
				mMagOptimisation[0] = (mMagX[0]+mMagX[mMagX.length-1])/2; // OffsetX = (Max+Min)/2
				mMagOptimisation[1] = (mMagY[0]+mMagY[mMagY.length-1])/2; // OffsetY
				mMagOptimisation[2] = (mMagZ[0]+mMagZ[mMagZ.length-1])/2; // OffsetZ
				mMagOptimisation[3] = (Math.abs(mMagX[0]-mMagX[mMagX.length-1]))/2; // SensitivityX = (Abs(Max-Min))/2
				mMagOptimisation[4] = (Math.abs(mMagY[0]-mMagY[mMagY.length-1]))/2; // SensitivityY
				mMagOptimisation[5] = (Math.abs(mMagZ[0]-mMagZ[mMagZ.length-1]))/2;	// SensitivityZ				
				
				calibrateMag();
		}
		
		else if(mNavDrawerItemPosition==4){
				
				if(mButtonCalibrateSetting.getText().toString().equals("X+g")){
					mWRXplusG = Arrays.copyOf(dataArrayMean, dataArrayMean.length);
				}
				else if(mButtonCalibrateSetting.getText().toString().equals("X-g")){
					mWRXminusG = Arrays.copyOf(dataArrayMean, dataArrayMean.length);
				}
				else if(mButtonCalibrateSetting.getText().toString().equals("Y+g")){
					mWRYplusG = Arrays.copyOf(dataArrayMean, dataArrayMean.length);
				}
				else if(mButtonCalibrateSetting.getText().toString().equals("Y-g")){
					mWRYminusG = Arrays.copyOf(dataArrayMean, dataArrayMean.length);
				}
				else if(mButtonCalibrateSetting.getText().toString().equals("Z+g")){
					mWRZplusG = Arrays.copyOf(dataArrayMean, dataArrayMean.length);
				}
				else{
					mWRZminusG = Arrays.copyOf(dataArrayMean, dataArrayMean.length);
				}
				
				accel_Ua_plus.setColumn(0, mWRXplusG);
				accel_Ua_plus.setColumn(1, mWRYplusG);
				accel_Ua_plus.setColumn(2, mWRZplusG);
				
				accel_Ua_minus.setColumn(0, mWRXminusG);
				accel_Ua_minus.setColumn(1, mWRYminusG);
				accel_Ua_minus.setColumn(2, mWRZminusG);
				
				calibrateAccel();
			}
		}
	
	private void calibrateAccel(){
		
		// Accelerometer Offset Calibration
		accel_Ua_sum = accel_Ua_plus.add(accel_Ua_minus);
		
		for(int i=0; i<3; i++){
			for(int j=0; j<3; j++){
				mOffsetTemp[i][j] = (accel_Ua_sum.getData()[i][j])/2;	
			}
			mOffset[i] = (int) Math.round((mOffsetTemp[i][0]+mOffsetTemp[i][1]+mOffsetTemp[i][2])/3);
		}
		
		// Accelerometer Sensitivity Calibration
		accel_Ua_diff = accel_Ua_plus.subtract(accel_Ua_minus);
		
		for(int i=0; i<3; i++){
			for(int j=0; j<3; j++){
				accel_Ua_diff.getEntry(i, j);
				accel_KR.setEntry(i, j, accel_Ua_diff.getEntry(i, j)/19.6);
			}
		}

		accel_KRT = accel_KR.transpose();
		accel_KRxKRT = accel_KR.multiply(accel_KRT);
		
		boolean zeroFlag=true;
		
		for(int i=0; i<3; i++){
			accel_Ka.setEntry(i, i, Math.sqrt(accel_KRxKRT.getEntry(i, i)));
			mSensitivity[i][i] = Math.round(Math.sqrt(accel_KRxKRT.getEntry(i, i)));
			Log.d("Thresh", Double.toString(Math.sqrt(accel_KRxKRT.getEntry(i, i))));
			if(Math.sqrt(accel_KRxKRT.getEntry(i, i))==0)zeroFlag=!zeroFlag;
		}
		
		// Accelerometer Alignment Calibration
		if(zeroFlag){ // don't get inverse of array if contains zeros as a singular matrix exception will occur
			accel_KaInv = new LUDecompositionImpl(accel_Ka).getSolver().getInverse();
			accel_Ra = accel_KaInv.multiply(accel_KR);
		
			for(int i=0; i<3; i++){
				for(int j=0; j<3; j++){
					mAlignment[i][j] = Math.round(accel_Ra.getEntry(i, j)*100)/100.0d;	
				}
			}
		}
		updateUI(mNavDrawerItemPosition);
	}
	

	private void calibrateGyro() {
		// TODO Auto-generated method stub
		
		for(int i=0; i<3; i++){
			mOffset[i] = (int) mGyroStationary[i];
		}
		
		// Gyro Sensitivity Calibration
		
		gyro_KR = gyro_W.multiply(gyro_Angle.inverse());
		
		gyro_KRT = gyro_KR.transpose();
		
		gyro_KRxKRT = gyro_KR.multiply(gyro_KRT);
		
		for(int i=0; i<3; i++){
			gyro_K.setEntry(i, i, Math.sqrt(gyro_KRxKRT.getEntry(i, i)));
			mSensitivity[i][i] = (Math.round(gyro_K.getEntry(i, i) * 100.0) / 100.0);
		}
		
		// Gyro Alignment Calibration

		if(!mButtonCalibrateSetting.getText().toString().equals("STATIONARY")){

			gyro_KInv = new LUDecompositionImpl(gyro_K).getSolver().getInverse();
			gyro_R = gyro_KInv.multiply(gyro_KR);
			
			for(int i=0; i<3; i++){
				for(int j=0; j<3; j++){
				mAlignment[i][j] = Math.round(gyro_R.getEntry(i, j)*100)/100.0d;	
				}
			}
		
		}

		updateUI(mNavDrawerItemPosition);
	}

	private void calibrateMag() {
		
		for(int i=0; i<3; i++){
			mOffset[i] = (int) mMagOptimisation[i];
			mSensitivity[i][i] = mMagOptimisation[i+3];
		} 
		
		// Default Alignment Calibration
		
		boolean nonZero=false;
		
		for(int i=0; i<3; i++){
			for(int j=0; j<3; j++){
				if(!mAlignment.equals(0)) nonZero=true;
			}
		}
		
		if(nonZero){
			if(MainActivity.mShimmerDevice.getShimmerVersion()==3){
				for(int i=0; i<3; i++){
					for(int j=0; j<3; j++){
						mAlignment[0][0] =  1;
						mAlignment[1][1] = -1;
						mAlignment[2][2] =  1;
					}
				}
			}
			else{
				for(int i=0; i<3; i++){
						mAlignment[0][0] =  1;
						mAlignment[1][1] =  1;
						mAlignment[2][2] = -1;
				}
			}
		}	
		
		updateUI(mNavDrawerItemPosition);
	}
	
	private void writeCalibrationParameters() {
		
		mOffsetBytes[0] = (byte) ((mOffset[0] >> 8) & 0xFF);  // high byte
		mOffsetBytes[1] = (byte) (mOffset[0] & 0xFF); // low byte
		mOffsetBytes[2] = (byte) ((mOffset[1] >> 8) & 0xFF);  // high byte
		mOffsetBytes[3] = (byte) (mOffset[1] & 0xFF); // low byte
		mOffsetBytes[4] = (byte) ((mOffset[2] >> 8) & 0xFF);  // high byte
		mOffsetBytes[5] = (byte) (mOffset[2] & 0xFF); // low byte
		
		if (mNavDrawerItemPosition==2) SensitivityScaleFactor=100;
		else SensitivityScaleFactor=1;
		
		mSensitivityBytes[0] = (byte) ((((int)Math.abs(mSensitivity[0][0]*SensitivityScaleFactor)) >> 8) & 0xFF);  // high byte
		mSensitivityBytes[1] = (byte) (((int)Math.abs(mSensitivity[0][0]*SensitivityScaleFactor)) & 0xFF); // low byte
		mSensitivityBytes[2] = (byte) ((((int)Math.abs(mSensitivity[1][1]*SensitivityScaleFactor)) >> 8) & 0xFF);  // high byte
		mSensitivityBytes[3] = (byte) (((int)Math.abs(mSensitivity[1][1]*SensitivityScaleFactor)) & 0xFF); // low byte
		mSensitivityBytes[4] = (byte) ((((int)Math.abs(mSensitivity[2][2]*SensitivityScaleFactor))  >> 8) & 0xFF);  // high byte
		mSensitivityBytes[5] = (byte) (((int)Math.abs(mSensitivity[2][2]*SensitivityScaleFactor)) & 0xFF); // low byte
		
		int alignmentCounter=0;
		
		for(int i=0; i<3; i++){
			for(int j=0; j<3; j++){
				mAlignmentBytes[alignmentCounter] = (byte) (mAlignment[i][j]*100);
				alignmentCounter++;
			}
		}
		
		for(int counter=0, i=0, j=0, k=0; counter<mCalibrationBytes.length; counter++){
			if(counter<6){
				mCalibrationBytes[counter] = mOffsetBytes[i];
				i++;
			}
			else if(counter<12){
				mCalibrationBytes[counter] = mSensitivityBytes[j];
				j++;
			}
			else{
				mCalibrationBytes[counter] = mAlignmentBytes[k];
				k++;
			}
		}
		
		switch (mNavDrawerItemPosition){
		
		case 0:
			break;
		
		case 1:
			MainActivity.mShimmerDevice.writeAccelCalibrationParameters(mCalibrationBytes);
			try {
				Thread.sleep(500);	// Wait to ensure that we dont missed any bytes which need to be cleared
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Toast.makeText(context,"LN Accel calibration parameters saved", Toast.LENGTH_SHORT).show();
			getCalibrationParameters(mNavDrawerItemPosition); 
			break;
		
		case 2:
			MainActivity.mShimmerDevice.writeGyroCalibrationParameters(mCalibrationBytes);
			try {
				Thread.sleep(500);	// Wait to ensure that we dont missed any bytes which need to be cleared
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Toast.makeText(context,"Gyro calibration parameters saved", Toast.LENGTH_SHORT).show();
			getCalibrationParameters(mNavDrawerItemPosition);
			break;
		
		case 3:
			MainActivity.mShimmerDevice.writeMagCalibrationParameters(mCalibrationBytes);
			try {
				Thread.sleep(500);	// Wait to ensure that we dont missed any bytes which need to be cleared
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Toast.makeText(context,"Mag calibration parameters saved", Toast.LENGTH_SHORT).show();
			getCalibrationParameters(mNavDrawerItemPosition);
			break;
		
		case 4:
			MainActivity.mShimmerDevice.writeWRAccelCalibrationParameters(mCalibrationBytes);
			try {
				Thread.sleep(500);	// Wait to ensure that we dont missed any bytes which need to be cleared
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Toast.makeText(context, "WR Accel calibration parameters saved", Toast.LENGTH_SHORT).show();
			getCalibrationParameters(mNavDrawerItemPosition);
			break;
		
		default:
			break;
		}			
	}
	
	private void updateUI(int position){
		
		if (MainActivity.mShimmerDevice!=null && MainActivity.mShimmerDevice.getStreamingStatus() ){
			
			if(mCalibrateFlag){ 
				mButtonCalibrate.setText("CAPTURING");
				if((mNavDrawerItemPosition==2 && !mButtonCalibrateSetting.getText().toString().equals("STATIONARY")) || mNavDrawerItemPosition==3){
					mButtonCalibrate.setEnabled(true);
					mButtonCalibrateSetting.setEnabled(true);
				}
				else{
					mButtonCalibrate.setEnabled(false);
					mButtonCalibrateSetting.setEnabled(false);
				}
			}
			else{
				mButtonCalibrateSetting.setEnabled(true);
				mButtonCalibrate.setEnabled(true);
				mButtonCalibrate.setText("CAPTURE");
			}
			mButtonSave.setEnabled(false);
		}
		else if (MainActivity.mShimmerDevice!=null && MainActivity.mShimmerDevice.getBluetoothRadioState() == BT_STATE.CONNECTED){
			mButtonCalibrate.setText("CAPTURE");
			mButtonCalibrate.setEnabled(true);
			mButtonCalibrateSetting.setEnabled(true);
			mButtonSave.setEnabled(true);
		}	
		else{
			mButtonCalibrate.setText("CAPTURE");
			mButtonCalibrate.setEnabled(false);
			mButtonCalibrateSetting.setEnabled(false);
			mButtonSave.setEnabled(false);
			Toast.makeText(context, R.string.shimmer_disconnected_msg, Toast.LENGTH_SHORT).show();
		}
		
		switch (position) {
		
		case 0:
			mButtonCalibrateSetting.setText("");
			break;
		
		case 1:
			mButtonCalibrateSetting.setText(getResources().getStringArray(R.array.accel_calibration_strings)[RadioButtonListActivity.mRadioButtonAccelCalPosition]);
			break;
		
		case 2:
			mButtonCalibrateSetting.setText(getResources().getStringArray(R.array.gyro_calibration_strings)[RadioButtonListActivity.mRadioButtonGyroCalPosition]);
			break;
		
		case 3:
			if(magViewFlag) mButtonCalibrateSetting.setText(getResources().getStringArray(R.array.mag_view_strings)[0]);
			else mButtonCalibrateSetting.setText(getResources().getStringArray(R.array.mag_view_strings)[1]);
			break;
			
		case 4:
			mButtonCalibrateSetting.setText(getResources().getStringArray(R.array.accel_calibration_strings)[RadioButtonListActivity.mRadioButtonWRAccelCalPosition]);
			break;
		
		case 5:
			mButtonCalibrateSetting.setText("TEST");
			break;
		
		default:
			break;
		}
		
		mOffsetA.setText(Integer.toString(mOffset[0]));
		mOffsetB.setText(Integer.toString(mOffset[1]));
		mOffsetC.setText(Integer.toString(mOffset[2]));
		
		if(mNavDrawerItemPosition!=2){
			mSensitivityA1.setText(Integer.toString((int)mSensitivity[0][0]));
			mSensitivityA2.setText(Integer.toString((int)mSensitivity[1][0]));
			mSensitivityA3.setText(Integer.toString((int)mSensitivity[2][0]));
			mSensitivityB1.setText(Integer.toString((int)mSensitivity[0][1]));
			mSensitivityB2.setText(Integer.toString((int)mSensitivity[1][1]));
			mSensitivityB3.setText(Integer.toString((int)mSensitivity[2][1]));
			mSensitivityC1.setText(Integer.toString((int)mSensitivity[0][2]));
			mSensitivityC2.setText(Integer.toString((int)mSensitivity[1][2]));
			mSensitivityC3.setText(Integer.toString((int)mSensitivity[2][2]));
		}
		else{
			mSensitivityA1.setText(Double.toString(mSensitivity[0][0]));
			mSensitivityA2.setText(Double.toString(mSensitivity[1][0]));
			mSensitivityA3.setText(Double.toString(mSensitivity[2][0]));
			mSensitivityB1.setText(Double.toString(mSensitivity[0][1]));
			mSensitivityB2.setText(Double.toString(mSensitivity[1][1]));
			mSensitivityB3.setText(Double.toString(mSensitivity[2][1]));
			mSensitivityC1.setText(Double.toString(mSensitivity[0][2]));
			mSensitivityC2.setText(Double.toString(mSensitivity[1][2]));
			mSensitivityC3.setText(Double.toString(mSensitivity[2][2]));
		}

		mAlignmentA1.setText(Double.toString(mAlignment[0][0]));
		mAlignmentA2.setText(Double.toString(mAlignment[1][0]));
		mAlignmentA3.setText(Double.toString(mAlignment[2][0]));
		mAlignmentB1.setText(Double.toString(mAlignment[0][1]));
		mAlignmentB2.setText(Double.toString(mAlignment[1][1]));
		mAlignmentB3.setText(Double.toString(mAlignment[2][1]));
		mAlignmentC1.setText(Double.toString(mAlignment[0][2]));
		mAlignmentC2.setText(Double.toString(mAlignment[1][2]));
		mAlignmentC3.setText(Double.toString(mAlignment[2][2]));
		
		mShimmerOffsetA.setText(Integer.toString(mShimmerOffset[0]));
		mShimmerOffsetB.setText(Integer.toString(mShimmerOffset[1]));
		mShimmerOffsetC.setText(Integer.toString(mShimmerOffset[2]));
		
		if(mNavDrawerItemPosition!=2){
			mShimmerSensitivityA1.setText(Integer.toString((int)mShimmerSensitivity[0][0]));
			mShimmerSensitivityA2.setText(Integer.toString((int)mShimmerSensitivity[1][0]));
			mShimmerSensitivityA3.setText(Integer.toString((int)mShimmerSensitivity[2][0]));
			mShimmerSensitivityB1.setText(Integer.toString((int)mShimmerSensitivity[0][1]));
			mShimmerSensitivityB2.setText(Integer.toString((int)mShimmerSensitivity[1][1]));
			mShimmerSensitivityB3.setText(Integer.toString((int)mShimmerSensitivity[2][1]));
			mShimmerSensitivityC1.setText(Integer.toString((int)mShimmerSensitivity[0][2]));
			mShimmerSensitivityC2.setText(Integer.toString((int)mShimmerSensitivity[1][2]));
			mShimmerSensitivityC3.setText(Integer.toString((int)mShimmerSensitivity[2][2]));
		}
		else{
			mShimmerSensitivityA1.setText(Double.toString(mShimmerSensitivity[0][0]));
			mShimmerSensitivityA2.setText(Double.toString(mShimmerSensitivity[1][0]));
			mShimmerSensitivityA3.setText(Double.toString(mShimmerSensitivity[2][0]));
			mShimmerSensitivityB1.setText(Double.toString(mShimmerSensitivity[0][1]));
			mShimmerSensitivityB2.setText(Double.toString(mShimmerSensitivity[1][1]));
			mShimmerSensitivityB3.setText(Double.toString(mShimmerSensitivity[2][1]));
			mShimmerSensitivityC1.setText(Double.toString(mShimmerSensitivity[0][2]));
			mShimmerSensitivityC2.setText(Double.toString(mShimmerSensitivity[1][2]));
			mShimmerSensitivityC3.setText(Double.toString(mShimmerSensitivity[2][2]));
		}
		
		mShimmerAlignmentA1.setText(Double.toString(mShimmerAlignment[0][0]));
		mShimmerAlignmentA2.setText(Double.toString(mShimmerAlignment[1][0]));
		mShimmerAlignmentA3.setText(Double.toString(mShimmerAlignment[2][0]));
		mShimmerAlignmentB1.setText(Double.toString(mShimmerAlignment[0][1]));
		mShimmerAlignmentB2.setText(Double.toString(mShimmerAlignment[1][1]));
		mShimmerAlignmentB3.setText(Double.toString(mShimmerAlignment[2][1]));
		mShimmerAlignmentC1.setText(Double.toString(mShimmerAlignment[0][2]));
		mShimmerAlignmentC2.setText(Double.toString(mShimmerAlignment[1][2]));
		mShimmerAlignmentC3.setText(Double.toString(mShimmerAlignment[2][2]));
	}

	private Handler CalibrateHandler = new Handler() {
		
        @Override
        public void handleMessage(Message msg) {
        	
        	switch (msg.what) {
            
            case ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET:
        	    if ((msg.obj instanceof ObjectCluster)){
        	    ObjectCluster objectCluster =  (ObjectCluster) msg.obj; 
        		int[] data = new int[0];
        		double[] timestampData = new double[0];
        		String[] sensorName = new String[0];
        		int sampleSize = 0;
        		
        		if(mNavDrawerItemPosition==2 && !mButtonCalibrateSetting.getText().toString().equals("STATIONARY")) sampleSize = mGyroCalibrationSampleSize;
        		else if(mNavDrawerItemPosition==3) sampleSize = mMagCalibrationSampleSize;
        		else sampleSize = mCalibrationSampleSize;
        		
        		//mSensorView determines which sensor to graph
        		
        		if (mNavDrawerItemPosition==1){
        			sensorName = new String[4]; // for x y and z axis
        			data = new int[3];
        			timestampData = new double[sampleSize];
        			
        			if(HomeFragment.SHIMMER_VERSION==3){
        				sensorName[0] = Shimmer3.ObjectClusterSensorName.TIMESTAMP;
        				sensorName[1] = Shimmer3.ObjectClusterSensorName.ACCEL_LN_X;
	        			sensorName[2] = Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y;
	        			sensorName[3] = Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z;
        			}
        			else{
        				sensorName[0] = Shimmer2.ObjectClusterSensorName.TIMESTAMP;
        				sensorName[1] = Shimmer2.ObjectClusterSensorName.ACCEL_X;
	        			sensorName[2] = Shimmer2.ObjectClusterSensorName.ACCEL_Y;
	        			sensorName[3] = Shimmer2.ObjectClusterSensorName.ACCEL_Z;
        			}
        		}
        		if (mNavDrawerItemPosition==2){
        			sensorName = new String[4]; // for x y and z axis
        			data = new int[3];
        			timestampData = new double[sampleSize];
        			if(HomeFragment.SHIMMER_VERSION==3){
        				sensorName[0] = Shimmer2.ObjectClusterSensorName.TIMESTAMP;
        				sensorName[1] = Shimmer3.ObjectClusterSensorName.GYRO_X;
	        			sensorName[2] = Shimmer3.ObjectClusterSensorName.GYRO_Y;
	        			sensorName[3] = Shimmer3.ObjectClusterSensorName.GYRO_Z;
        			} else {
        				sensorName[0] = Shimmer2.ObjectClusterSensorName.TIMESTAMP;
        				sensorName[1] = Shimmer2.ObjectClusterSensorName.GYRO_X;
	        			sensorName[2] = Shimmer2.ObjectClusterSensorName.GYRO_Y;
	        			sensorName[3] = Shimmer2.ObjectClusterSensorName.GYRO_Z;
        			}
        		}
        		if (mNavDrawerItemPosition==3){
        			sensorName = new String[4]; // for x y and z axis
        			data = new int[3];
        			timestampData = new double[sampleSize];
        			if(HomeFragment.SHIMMER_VERSION==3){
        				sensorName[0] = Shimmer3.ObjectClusterSensorName.TIMESTAMP;
        				sensorName[1] = Shimmer3.ObjectClusterSensorName.MAG_X;
	        			sensorName[2] = Shimmer3.ObjectClusterSensorName.MAG_Y;
	        			sensorName[3] = Shimmer3.ObjectClusterSensorName.MAG_Z;
        			} else {
        				sensorName[0] = Shimmer2.ObjectClusterSensorName.TIMESTAMP;
        				sensorName[1] = Shimmer2.ObjectClusterSensorName.MAG_X;
	        			sensorName[2] = Shimmer2.ObjectClusterSensorName.MAG_Y;
	        			sensorName[3] = Shimmer2.ObjectClusterSensorName.MAG_Z;
        			}
        		}
        		if (mNavDrawerItemPosition==4){
        			sensorName = new String[4]; // for x y and z axis
        			data = new int[3];
        			timestampData = new double[sampleSize];
    				sensorName[0] = Shimmer3.ObjectClusterSensorName.TIMESTAMP;
	        		sensorName[1] = Shimmer3.ObjectClusterSensorName.ACCEL_WR_X;
	        		sensorName[2] = Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y;
	        		sensorName[3] = Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z;
        		}
        		
        		//int[] mCalibrateDataArray = new int[mCalibrationSampleSize];
        		
        		String deviceName = objectCluster.getShimmerName();
        		Collection<FormatCluster> ofFormats = null;
        		FormatCluster formatCluster;

        		//seriesName = getActivity().getResources().getStringArray(R.array.sensor_calibration_strings)[RadioButtonListActivity.mRadioButtonSensorPosition];
        		//log data
   		     	
   		     		//if (deviceName=="Shimmer" && sensorName.length!=0){  // Device 1 is the assigned user id, see constructor of the Shimmer
        			if (sensorName.length!=0){  // Device 1 is the assigned user id, see constructor of the Shimmer
   		     			if(mCalibrateFlag){
   		     				
   		     				if(counter<sampleSize){
   		     				
   		     					ofFormats = objectCluster.getCollectionOfFormatClusters(sensorName[0]);  // first retrieve all the possible formats for the current sensor device
   		     					//formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"CAL")); 
   		     					
   		     					timestampData[0] = (double)((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,CHANNEL_TYPE.CAL.toString())).mData;			
   		     					if(timestampData[0]!=0) mTimestampArray[counter] = timestampData[0]/1000; // convert timestamp data to seconds
					 	    			
					 	    	ofFormats = objectCluster.getCollectionOfFormatClusters(sensorName[1]);  // first retrieve all the possible formats for the current sensor device
					 	    	//formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"CAL"));
					 	    	data[0] = (int) ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,CHANNEL_TYPE.UNCAL.toString())).mData; 
					 	    	if(data[0]!=0){
					 	    		if (mNavDrawerItemPosition==3){ 
					 	    			mMagDataXYZ[counter][0] = data[0];
					 	    			magData1.add(data[0]);
					 	    		}
					 	    		else if (mNavDrawerItemPosition==2 && !mButtonCalibrateSetting.getText().toString().equals("STATIONARY")){
					 	    			mDataValueX += data[0];
					 	    			mGyroDataXYZ[counter][0] = data[0];
					 	    		}
					 	    		else mDataValueX += data[0]; 
					 	    			
					 	    	}
					 	    	
					 	    	ofFormats = objectCluster.getCollectionOfFormatClusters(sensorName[2]);  // first retrieve all the possible formats for the current sensor device
					 	    	//formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,"CAL")); 
				 	    		data[1] =(int) ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,CHANNEL_TYPE.UNCAL.toString())).mData; 	
				 	    		if(data[1]!=0){
				 	    			if (mNavDrawerItemPosition==3){
				 	    				mMagDataXYZ[counter][1] = data[1];
				 	    				magData2.add(data[1]);
				 	    			}
				 	    			else if (mNavDrawerItemPosition==2 && !mButtonCalibrateSetting.getText().toString().equals("STATIONARY")){
				 	    				mDataValueY += data[1];
				 	    				mGyroDataXYZ[counter][1] = data[1];
				 	    			}
				 	    			else mDataValueY += data[1]; 
				 	    		}
				 	    		
				 	    		ofFormats = objectCluster.getCollectionOfFormatClusters(sensorName[3]);
				 	    		data[2] =(int) ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,CHANNEL_TYPE.UNCAL.toString())).mData; 	
				 	    		if(data[2]!=0){
				 	    			if (mNavDrawerItemPosition==3){
				 	    				mMagDataXYZ[counter][2] = data[2];
				 	    				magData3.add(data[2]);
				 	    			}
				 	    			else if (mNavDrawerItemPosition==2 && !mButtonCalibrateSetting.getText().toString().equals("STATIONARY")){
				 	    				mDataValueY += data[2];
				 	    				mGyroDataXYZ[counter][2] = data[2];
				 	    			}
				 	    			else mDataValueZ += data[2]; 
				 	    		}
				 	    			counter++;

				 	    			if (mNavDrawerItemPosition==3){
				 	    				
				 	    				magSeries1.updateData(magData1, magData2);
				 	    				magSeries2.updateData(magData2, magData3);
				 	    				magSeries3.updateData(magData3, magData1);
				 	    				
			   		     				magPlot1.redraw();
			   		     				magPlot2.redraw();
			   		     				magPlot3.redraw();
				 	    			}
					 	    }	
   		     					
   		     				else{ 
   		     					mDataArrayMean[0] = mDataValueX/sampleSize;
   		     					mDataArrayMean[1] = mDataValueY/sampleSize;
   		     					mDataArrayMean[2] = mDataValueZ/sampleSize;
   		     					mDataValueX=0;
   		     					mDataValueY=0;
   		     					mDataValueZ=0;
   		     					mCalibrateFlag=false;
   		     					MainActivity.mShimmerDevice.stopStreaming();
   		     					updateCalibrationDataArray(mTimestampArray, mDataArrayMean, mGyroDataXYZ, mMagDataXYZ, counter);
   		     					counter=0;
   		     				}
   		     				
   		     			}
   		     		}
        	    }

            case Shimmer.MESSAGE_ACK_RECEIVED:  	
            	break;
      
            default:
            	updateUI(mNavDrawerItemPosition);
        		break;
            }
        }
    };
	
    private void getCalibrationParameters(int position) {
		// initialize button text depending on navigation drawer selection		
		
		row=0; 
		col=0;
		
		switch (position) {
		
		case 0:
			break;
		
		case 1:
			if (MainActivity.mShimmerDevice!=null && (MainActivity.mShimmerDevice.getBluetoothRadioState() == BT_STATE.CONNECTED)){
				for(row=0; row<3; row++){
						mShimmerOffset[row] = (int)MainActivity.mShimmerDevice.getOffsetVectorMatrixAccel()[row][0];
				}
				
				for(row=0; row<3; row++){
					for(col=0; col<3; col++){
						mShimmerSensitivity[row][col] = MainActivity.mShimmerDevice.getSensitivityMatrixAccel()[col][row];
					}
				}
				
				for(row=0; row<3; row++){
					for(col=0; col<3; col++){
						mShimmerAlignment[row][col] = (MainActivity.mShimmerDevice.getAlignmentMatrixAccel()[row][col]);
					}
				}
				updateUI(position);
			}
			break;
		
		case 2:
			if (MainActivity.mShimmerDevice!=null && (MainActivity.mShimmerDevice.getBluetoothRadioState() == BT_STATE.CONNECTED)){
				for(row=0; row<3; row++){
						mShimmerOffset[row] = (int)MainActivity.mShimmerDevice.getOffsetVectorMatrixGyro()[row][0];
				}
				
				for(row=0; row<3; row++){
					for(col=0; col<3; col++){
						mShimmerSensitivity[row][col] = (MainActivity.mShimmerDevice.getSensitivityMatrixGyro()[col][row]);
					}
				}
				
				for(row=0; row<3; row++){
					for(col=0; col<3; col++){
						mShimmerAlignment[row][col] = (MainActivity.mShimmerDevice.getAlignmentMatrixGyro()[col][row]);
					}
				}
				updateUI(position);
			}
			break;
		
		case 3:	
			if (MainActivity.mShimmerDevice!=null && (MainActivity.mShimmerDevice.getBluetoothRadioState() == BT_STATE.CONNECTED)){
				for(row=0; row<3; row++){
						mShimmerOffset[row] = (int)MainActivity.mShimmerDevice.getOffsetVectorMatrixMag()[row][0];
				}
				
				for(int row=0; row<3; row++){
					for(col=0; col<3; col++){
						mShimmerSensitivity[row][col] = MainActivity.mShimmerDevice.getSensitivityMatrixMag()[col][row];
					}
				}
				
				for(row=0; row<3; row++){
					for(col=0; col<3; col++){
						mShimmerAlignment[row][col] = (MainActivity.mShimmerDevice.getAlignmentMatrixMag()[col][row]);
					}
				}
				updateUI(position);
			}
			break;
		
		case 4:

			if (MainActivity.mShimmerDevice!=null && (MainActivity.mShimmerDevice.getBluetoothRadioState() == BT_STATE.CONNECTED)){
				for(row=0; row<3; row++){
						mShimmerOffset[row] = (int)MainActivity.mShimmerDevice.getOffsetVectorMatrixWRAccel()[row][0];
				}
				
				for(row=0; row<3; row++){
					for(col=0; col<3; col++){
						mShimmerSensitivity[row][col] = MainActivity.mShimmerDevice.getSensitivityMatrixWRAccel()[col][row];
					}
				}
				
				for(row=0; row<3; row++){
					for(col=0; col<3; col++){
						mShimmerAlignment[row][col] = (MainActivity.mShimmerDevice.getAlignmentMatrixWRAccel()[col][row]);
					}
				}
				updateUI(position);
			}
			break;
		
		case 5:
			break;
		
		default:
			break;
		}
	}
    
    private void clearPlots(){
    	magData1.clear();
    	magPlot1.redraw();
    	magData2.clear();
    	magPlot2.redraw();
    	magData3.clear();
    	magPlot3.redraw();	
    }

	private void initPlots() {
		
		// create arraylists to store data to pass to the plot
		
		 		magData1= new ArrayList<Number>();
		 		magData2= new ArrayList<Number>();
		 		magData3= new ArrayList<Number>();
		 		magData1.clear();
		 		magData2.clear();
		 		magData3.clear();
		 		magSeries1 = new XYSeriesShimmer9DOF(magData1, magData2, 1, "MagPlot");
		 		magSeries2 = new XYSeriesShimmer9DOF(magData2, magData3, 2, "MagPlot");
		 		magSeries3 = new XYSeriesShimmer9DOF(magData3, magData1, 3, "MagPlot");
				// get handles to our View defined in layout.xml:
		       
		        // only display whole numbers in domain labels
		        magPlot1.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
		        magPlot2.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
		        magPlot3.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
		        
		        magLineAndPointFormatter1 = new LineAndPointFormatter(Color.TRANSPARENT, Color.rgb(51, 153, 255), null); // line color, point color, fill color
		        magLineAndPointFormatter2 = new LineAndPointFormatter(Color.TRANSPARENT, Color.rgb(245, 146, 107), null);
		        magLineAndPointFormatter3 = new LineAndPointFormatter(Color.TRANSPARENT, Color.rgb(150, 150, 150), null);
		        
		        LPFpaint =  magLineAndPointFormatter1.getLinePaint();
		        LPFpaint.setStrokeWidth(3);
		        magLineAndPointFormatter1.setLinePaint(LPFpaint);
		        LPFpaint = magLineAndPointFormatter2.getLinePaint();
		        LPFpaint.setStrokeWidth(3);
		        magLineAndPointFormatter2.setLinePaint(LPFpaint);
		        LPFpaint = magLineAndPointFormatter3.getLinePaint();
		        LPFpaint.setStrokeWidth(3);
		        magLineAndPointFormatter3.setLinePaint(LPFpaint);
		        
		        transparentPaint = new Paint();
		        transparentPaint.setColor(Color.TRANSPARENT);
		        //lineAndPointFormatter1.setLinePaint(p);
		        magPlot1.setDomainStepMode(XYStepMode.SUBDIVIDE);
		        magPlot2.setDomainStepMode(XYStepMode.SUBDIVIDE);
		        magPlot3.setDomainStepMode(XYStepMode.SUBDIVIDE);
		        //dynamicPlot.setDomainStepValue(series1.size());
		        magPlot1.getLegendWidget().setSize(new SizeMetrics(0, SizeLayoutType.ABSOLUTE, 0, SizeLayoutType.ABSOLUTE));
		        magPlot2.getLegendWidget().setSize(new SizeMetrics(0, SizeLayoutType.ABSOLUTE, 0, SizeLayoutType.ABSOLUTE));
		        magPlot3.getLegendWidget().setSize(new SizeMetrics(0, SizeLayoutType.ABSOLUTE, 0, SizeLayoutType.ABSOLUTE));
		        // thin out domain/range tick labels so they dont overlap each other:
		        magPlot1.setTicksPerDomainLabel(3);
		        magPlot2.setTicksPerDomainLabel(3);
		        magPlot3.setTicksPerDomainLabel(3);
		        magPlot1.setTicksPerRangeLabel(3);
		        magPlot2.setTicksPerRangeLabel(3);
		        magPlot3.setTicksPerRangeLabel(3);
		        magPlot1.disableAllMarkup();
		        magPlot2.disableAllMarkup();
		        magPlot3.disableAllMarkup();
		        Paint gridLinePaint = new Paint();
		        gridLinePaint.setColor(getResources().getColor(R.color.text_color));
		        magPlot1.getGraphWidget().setMargins(0, 20, 10, 10);
		        magPlot2.getGraphWidget().setMargins(0, 20, 10, 10);
		        magPlot3.getGraphWidget().setMargins(0, 20, 10, 10);
		        magPlot1.setBorderStyle(Plot.BorderStyle.NONE, null, null);
		        magPlot2.setBorderStyle(Plot.BorderStyle.NONE, null, null);
		        magPlot3.setBorderStyle(Plot.BorderStyle.NONE, null, null);
		        magPlot1.getBackgroundPaint().setColor(Color.TRANSPARENT);
		        magPlot2.getBackgroundPaint().setColor(Color.TRANSPARENT);
		        magPlot3.getBackgroundPaint().setColor(Color.TRANSPARENT);
		        magPlot1.setBackgroundColor(Color.TRANSPARENT);
		        magPlot2.setBackgroundColor(Color.TRANSPARENT);
		        magPlot3.setBackgroundColor(Color.TRANSPARENT);
		        magPlot1.getGraphWidget().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
		        magPlot2.getGraphWidget().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
		        magPlot3.getGraphWidget().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
		        magPlot1.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
		        magPlot2.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
		        magPlot3.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
		        magPlot1.getGraphWidget().setGridLinePaint(transparentPaint);
		        magPlot2.getGraphWidget().setGridLinePaint(transparentPaint);
		        magPlot3.getGraphWidget().setGridLinePaint(transparentPaint);
		        magPlot1.getGraphWidget().setDomainOriginLabelPaint(gridLinePaint);
		        magPlot2.getGraphWidget().setDomainOriginLabelPaint(gridLinePaint);
		        magPlot3.getGraphWidget().setDomainOriginLabelPaint(gridLinePaint);
		        magPlot1.getGraphWidget().setDomainOriginLinePaint(gridLinePaint);
		        magPlot2.getGraphWidget().setDomainOriginLinePaint(gridLinePaint);
		        magPlot3.getGraphWidget().setDomainOriginLinePaint(gridLinePaint);
		        magPlot1.getGraphWidget().setDomainLabelPaint(gridLinePaint);
		        magPlot2.getGraphWidget().setDomainLabelPaint(gridLinePaint);
		        magPlot3.getGraphWidget().setDomainLabelPaint(gridLinePaint);
		        magPlot1.getGraphWidget().getDomainLabelPaint().setTextSize(20);
		        magPlot2.getGraphWidget().getDomainLabelPaint().setTextSize(20);
		        magPlot3.getGraphWidget().getDomainLabelPaint().setTextSize(20);
		        magPlot1.getDomainLabelWidget().pack();
		        magPlot2.getDomainLabelWidget().pack();
		        magPlot3.getDomainLabelWidget().pack();
		        outlinePaint = magPlot1.getGraphWidget().getDomainOriginLinePaint();
		        outlinePaint.setStrokeWidth(3);
		        magPlot1.getGraphWidget().setClippingEnabled(false);
		        magPlot2.getGraphWidget().setClippingEnabled(false);
		        magPlot3.getGraphWidget().setClippingEnabled(false);
		        magPlot1.getGraphWidget().setRangeOriginLabelPaint(gridLinePaint);
		        magPlot2.getGraphWidget().setRangeOriginLabelPaint(gridLinePaint);
		        magPlot3.getGraphWidget().setRangeOriginLabelPaint(gridLinePaint);
		        magPlot1.getGraphWidget().setRangeOriginLinePaint(gridLinePaint);
		        magPlot2.getGraphWidget().setRangeOriginLinePaint(gridLinePaint);
		        magPlot3.getGraphWidget().setRangeOriginLinePaint(gridLinePaint);
		        magPlot1.getGraphWidget().setRangeLabelPaint(gridLinePaint);
		        magPlot2.getGraphWidget().setRangeLabelPaint(gridLinePaint);
		        magPlot3.getGraphWidget().setRangeLabelPaint(gridLinePaint);
		        magPlot1.getGraphWidget().getRangeLabelPaint().setTextSize(20);
		        magPlot2.getGraphWidget().getRangeLabelPaint().setTextSize(20);
		        magPlot3.getGraphWidget().getRangeLabelPaint().setTextSize(20);
		        magPlot1.getRangeLabelWidget().pack();
		        magPlot2.getRangeLabelWidget().pack();
		        magPlot3.getRangeLabelWidget().pack();
		        outlinePaint = magPlot1.getGraphWidget().getRangeOriginLinePaint();
		        outlinePaint.setStrokeWidth(3);
		        magPlot1.getLayoutManager().remove(magPlot1.getRangeLabelWidget());
		        magPlot2.getLayoutManager().remove(magPlot2.getRangeLabelWidget());
		        magPlot3.getLayoutManager().remove(magPlot3.getRangeLabelWidget());
		        magPlot1.getLayoutManager().remove(magPlot1.getDomainLabelWidget());
		        magPlot2.getLayoutManager().remove(magPlot2.getDomainLabelWidget());
		        magPlot3.getLayoutManager().remove(magPlot3.getDomainLabelWidget());
		        magPlot1.clear();
		        magPlot2.clear();
		        magPlot3.clear();
		        
		        magPlot1.setRangeBoundaries(-15, 15, BoundaryMode.AUTO); // freeze the range boundary:
		        magPlot1.setDomainBoundaries(-15, 15, BoundaryMode.AUTO); // freeze the domain boundary:
		        magPlot2.setRangeBoundaries(-15, 15, BoundaryMode.AUTO); // freeze the range boundary:
		        magPlot2.setDomainBoundaries(-15, 15, BoundaryMode.AUTO); // freeze the domain boundary:
		        magPlot3.setRangeBoundaries(-15, 15, BoundaryMode.AUTO); // freeze the range boundary:
		        magPlot3.setDomainBoundaries(-15, 15, BoundaryMode.AUTO); // freeze the domain boundary:
		            
		        magPlot1.addSeries(magSeries1, magLineAndPointFormatter1);
		        magPlot2.addSeries(magSeries2, magLineAndPointFormatter2);
		        magPlot3.addSeries(magSeries3, magLineAndPointFormatter3);
		            
		}
    	
}
