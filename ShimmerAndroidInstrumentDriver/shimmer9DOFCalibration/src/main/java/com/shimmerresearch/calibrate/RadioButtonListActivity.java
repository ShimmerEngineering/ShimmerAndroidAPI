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
 * This Activity populates a radiogroup depending on the active navigation drawer i.e. accel, gyro, mag
 */

import com.shimmerresearch.slidingmenu.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;
 
public class RadioButtonListActivity extends Activity {
 
            private RadioGroup mRadioGroup;
            private RadioButton mRadioButton;
            private String mIntentMsg;
            private int mRadioButtonCounter=0;
            static int mRadioButtonSensorPosition=0;
            static int mRadioButtonAccelCalPosition, mRadioButtonGyroCalPosition, mRadioButtonMagCalPosition, mRadioButtonWRAccelCalPosition=0;
            static int mRadioButtonAccelRangePosition, mRadioButtonGyroRangePosition, mRadioButtonMagRangePosition=0;
            static int mRadioButtonMagPlotPosition=0;
            static int mRadioButtonPlotFormatPosition=0;
            private String[] mCalibrationSettings;
            
            @Override
            public void onCreate(Bundle savedInstanceState) {
 
                super.onCreate(savedInstanceState);
                setContentView(R.layout.radio_button_list);
                mRadioGroup = (RadioGroup) findViewById(R.id.radioGroupSensors);    
                mRadioGroup.setClickable(true);
                
                View title = getWindow().findViewById(android.R.id.title);
                View titleBar = (View) title.getParent();
                titleBar.setBackgroundColor(getResources().getColor(R.color.dialog_background_color));
                
                Intent intent = getIntent();
                mIntentMsg = intent.getExtras().getString("intentMsg"); 
                
                if(mIntentMsg.equals("Sensor")){
                	setTitle(getResources().getString(R.string.sensor_select));
                	
                	if(HomeFragment.SHIMMER_VERSION==3){ 
                		mCalibrationSettings = getResources().getStringArray(R.array.shimmer3_calibration_strings);
                		addRadioButtons(4, mRadioButtonSensorPosition);
                	}
                	else{ 
                		mCalibrationSettings = getResources().getStringArray(R.array.shimmer2r_calibration_strings);
                		addRadioButtons(3, mRadioButtonSensorPosition);
                	}
                	
                }
                else if(mIntentMsg.equals("SensorRange")){
                	
                	setTitle(getResources().getString(R.string.range_select));
                	
                	switch (mRadioButtonSensorPosition) {
            		case 0:
            			if(HomeFragment.SHIMMER_VERSION==2){
            				mCalibrationSettings = getResources().getStringArray(R.array.shimmer2r_accel_range_strings);
            				addRadioButtons(2, mRadioButtonAccelRangePosition);
            			}
            			else{
            				mCalibrationSettings = getResources().getStringArray(R.array.shimmer2_accel_range_strings);
            				addRadioButtons(4, mRadioButtonAccelRangePosition);
            			}
            			break;
            		case 1:
                    	mCalibrationSettings = getResources().getStringArray(R.array.gyro_range_strings);
                    	addRadioButtons(4, mRadioButtonGyroRangePosition);
            			break;
            		case 2:
            			switch(HomeFragment.SHIMMER_VERSION){
            				case 1:
            					mCalibrationSettings = getResources().getStringArray(R.array.shimmer2_mag_range_strings);	
            					addRadioButtons(8, mRadioButtonMagRangePosition);
            				break;
            				
            				case 2:
            					mCalibrationSettings = getResources().getStringArray(R.array.shimmer2r_mag_range_strings);	
            					addRadioButtons(8, mRadioButtonMagRangePosition);
            				break;
            				
            				case 3:
            					mCalibrationSettings = getResources().getStringArray(R.array.shimmer3_mag_range_strings);	
            					addRadioButtons(7, mRadioButtonMagRangePosition);
            				break;
            			}
            			break;
            		case 3:
                    	mCalibrationSettings = getResources().getStringArray(R.array.shimmer3_accel_range_strings);
                    	addRadioButtons(4, mRadioButtonAccelRangePosition);
                    	break;
                	}   	
                }
                else if(mIntentMsg.equals("Accel")){
                	mCalibrationSettings = getResources().getStringArray(R.array.accel_calibration_strings);
                	setTitle(getResources().getString(R.string.accel_select));
                	addRadioButtons(6, mRadioButtonAccelCalPosition);
                }
                else if(mIntentMsg.equals("Gyro")){
                	mCalibrationSettings = getResources().getStringArray(R.array.gyro_calibration_strings);
                	setTitle(getResources().getString(R.string.gyro_select));
                	addRadioButtons(4, mRadioButtonGyroCalPosition);
                }
                else if(mIntentMsg.equals("WRAccel")){
                	mCalibrationSettings = getResources().getStringArray(R.array.accel_calibration_strings);
                	setTitle(getResources().getString(R.string.accel_select));
                	addRadioButtons(6, mRadioButtonWRAccelCalPosition);
                }
                else if(mIntentMsg.equals("MagPlot")){
                	mCalibrationSettings = getResources().getStringArray(R.array.mag_plot_strings);
                	setTitle(getResources().getString(R.string.mag_plot_select));
                	addRadioButtons(3, mRadioButtonMagPlotPosition);
                }
                else if(mIntentMsg.equals("RawCal")){
                	mCalibrationSettings = getResources().getStringArray(R.array.raw_cal_plot_strings);
                	setTitle(getResources().getString(R.string.plot_format_select));
                	addRadioButtons(2, mRadioButtonPlotFormatPosition);
                }
                
                addListenerOnButton();
                
            }
            
            private void addRadioButtons(int numButtons, int setPosition) {  
            	        	
            	for(mRadioButtonCounter=0; mRadioButtonCounter < numButtons; mRadioButtonCounter++){
            	    //instantiate...
            		mRadioButton = new RadioButton(this);

            	    //set the values that you would otherwise hardcode in the xml...
            	  	mRadioButton.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));	
            	    //label the button...
            	  	mRadioButton.setText("" + mCalibrationSettings[mRadioButtonCounter]);
            	  	mRadioButton.setTextColor(getResources().getColor(R.color.text_color));
            	  	mRadioButton.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
            	  	mRadioButton.setPadding(12, 0, 0, 0);
            	  	
            	  	// Position radio button to the right of the text
            	  	mRadioButton.setButtonDrawable(new StateListDrawable());
            	  	mRadioButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.btn_radio, 0);

            	  	mRadioButton.setId(mRadioButtonCounter);
            	  	mRadioButton.setOnClickListener(radioClickListener);
            	  	
            	  	if(mRadioButtonCounter==setPosition){ 
            	  		mRadioButton.setChecked(true);
            	  	}
            	  	else{
            	  		mRadioButton.setChecked(false);
            	  	}
            	
            	    //add it to the group.
            	  	mRadioGroup.addView(mRadioButton, mRadioButtonCounter);
            	}
            }
         
            OnClickListener radioClickListener = new OnClickListener()
            {
            	
                public void onClick(View v)
                {       //The first condition check if we have clicked on an already selected radioButton
    	        	if(mIntentMsg.equals("Sensor")){
    	        		mRadioButtonSensorPosition = v.getId();
    	        	}
                    
    	        	else if(mIntentMsg.equals("SensorRange"))
                    	switch (mRadioButtonSensorPosition) {
                		case 0:
                			mRadioButtonAccelRangePosition = v.getId();
                			break;
                		case 1:
                			mRadioButtonGyroRangePosition = v.getId();
                			break;
                		case 2:
                			mRadioButtonMagRangePosition = v.getId();
                			break;
                		case 3:
                			mRadioButtonAccelRangePosition = v.getId();
                        	break;
                    	}   	

                    else if (mIntentMsg.equals("Accel"))mRadioButtonAccelCalPosition = v.getId();
                    else if (mIntentMsg.equals("WRAccel")){
                    	mRadioButtonWRAccelCalPosition = v.getId();
                    	Log.d("Ruaidhri", Integer.toString(v.getId()));
                    }
                    else if (mIntentMsg.equals("Gyro"))mRadioButtonGyroCalPosition = v.getId();
                    else if (mIntentMsg.equals("Mag")) mRadioButtonMagCalPosition = v.getId();
    	        	else if (mIntentMsg.equals("MagPlot")) mRadioButtonMagPlotPosition = v.getId();
    	        	else if (mIntentMsg.equals("RawCal")) mRadioButtonPlotFormatPosition = v.getId();

    	        	//mRadioButton = (RadioButton) findViewById(v.getId());
                    setResult(Activity.RESULT_OK);
                    RadioButtonListActivity.this.finish();
                }
            };
            
            public void addListenerOnButton() {     
  
            	mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() 
            	    {

            	        public void onCheckedChanged(RadioGroup group, int checkedId) {
            	            // checkedId is the RadioButton selected
            	        	
            	        	Log.d("Ruaidhri", mIntentMsg);
            	        	
            	        	if(mIntentMsg.equals("Sensor")){
            	        		mRadioButtonSensorPosition = checkedId;
            	        	}
                            
            	        	else if(mIntentMsg.equals("SensorRange"))
                            	switch (mRadioButtonSensorPosition) {
                        		case 0:
                        			mRadioButtonAccelRangePosition = checkedId;
                        			break;
                        		case 1:
                        			mRadioButtonGyroRangePosition = checkedId;
                        			break;
                        		case 2:
                        			mRadioButtonMagRangePosition = checkedId;
                        			break;
                        		case 3:
                        			mRadioButtonAccelRangePosition = checkedId;
                                	break;
                            	}   	

                            else if (mIntentMsg.equals("Accel"))  mRadioButtonAccelCalPosition = checkedId;
                            else if (mIntentMsg.equals("WRAccel"))mRadioButtonWRAccelCalPosition = checkedId;
                            else if (mIntentMsg.equals("Gyro"))   mRadioButtonGyroCalPosition = checkedId;
                            else if (mIntentMsg.equals("Mag"))	  mRadioButtonMagCalPosition = checkedId;
                            else if (mIntentMsg.equals("MagPlot"))mRadioButtonMagPlotPosition = checkedId;
                            else if (mIntentMsg.equals("RawCal")) mRadioButtonPlotFormatPosition = checkedId;

            	        	mRadioButton = (RadioButton) findViewById(checkedId);
                            Toast.makeText(RadioButtonListActivity.this, mRadioButton.getText(), Toast.LENGTH_SHORT).show();
                            setResult(Activity.RESULT_OK);
                            RadioButtonListActivity.this.finish();
            	        }
            	    });
            	   
            }
}