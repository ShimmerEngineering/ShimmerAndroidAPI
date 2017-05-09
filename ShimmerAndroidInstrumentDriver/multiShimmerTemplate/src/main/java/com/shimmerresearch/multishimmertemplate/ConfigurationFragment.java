package com.shimmerresearch.multishimmertemplate;


import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.BiMap;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.database.DatabaseHandler;
import com.shimmerresearch.database.ShimmerConfiguration;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driver.Configuration.Shimmer3;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.sensors.SensorGSR;
import com.shimmerresearch.sensors.SensorLSM303;
import com.shimmerresearch.sensors.SensorMPU9X50;
import com.shimmerresearch.service.MultiShimmerTemplateService;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ConfigurationFragment extends Fragment{
	
	public View rootView = null;
	public MultiShimmerTemplateService mService;
	DatabaseHandler db;
	final String[] ListofPressureResolution={"Low","Standard","High","Very High"};
	public static String mDone = "Done";
	String mAttribute;
	String deviceBluetoothAddress;
	String[] mValues={""};
	double mAttributeValue;
	double mShimmerVersion;
	TextView mTVAttribute;
	CheckBox mChechBox;
	int currentPosition;
	ShimmerConfiguration shimmerConfig;
	public Dialog enableSensorDialog;
	public Dialog heartRateDialog;
	public ListView enableSensorListView;
	public long enabledSensors;
	public int exgRes;
	public String [] compatibleSensors;
	public BiMap<String,String> sensorBitmaptoName=null;
	public String mSensorToHeartRate;
	public boolean modifyHRState = true;
	
	String mBluetoothAddress;
	CheckBox cBoxLowPowerMag;
	CheckBox cBoxLowPowerAccel;
	CheckBox cBoxLowPowerGyro;
	CheckBox cBox5VReg;
	CheckBox cBoxHeartRate;
	CheckBox cBoxInternalExpPower;
	Button buttonGyroRange;
	Button buttonMagRange;
	Button buttonGsr;
	Button buttonPressureResolution;
	Button buttonSampleRate;
	Button buttonAccRange;
	Button buttonBattVoltLimit;
	Button buttonDeviceName;
	Button buttonExgGain;
	Button buttonExgRes;
	Button buttonDone;
	Button buttonEnableSensorsConfiguration;
	Button buttonEnableSensor;
	Button buttonDoneHearRate;
	Button buttonCancelHearRate;
	EditText editTexNumberOfBeats;
	
	public ConfigurationFragment() {
	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		getActivity().invalidateOptionsMenu();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		rootView = inflater.inflate(R.layout.configuration_fragment, container, false);
		
		this.mService = ((MainActivity)getActivity()).mService;
		
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		if(mService!=null){
			setup();
		}
		
		//get the position of the device in the listView
		Bundle arg = getArguments();
		deviceBluetoothAddress = arg.getString("address");
		currentPosition = arg.getInt("position");
		currentPosition-=2;
        shimmerConfig = mService.mShimmerConfigurationList.get(currentPosition);
		
    	double mSamplingRateV = shimmerConfig.getSamplingRate();
    	int mAccelerometerRangeV = shimmerConfig.getAccelRange();
    	int mGSRRangeV = shimmerConfig.getGSRRange();
//    	final double batteryLimit = shimmerConfig.; there's nothing for the battery??
    	final String[] samplingRate = new String [] {"8","16","51.2","102.4","128","204.8","256","512","1024","2048"};
    	final String[] exgGain = new String [] {"6","1","2","3","4","8","12"};
    	final String[] exgResolution = new String [] {"16 bits","24 bits"};
        buttonPressureResolution = (Button) rootView.findViewById(R.id.buttonPressureAccuracy);
        // Set an EditText view to get user input 
        final EditText editTextBattLimit = new EditText(getActivity());
        final EditText editTextDeviceName = new EditText(getActivity());
        editTextBattLimit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        buttonGyroRange = (Button)  rootView.findViewById(R.id.buttonGyroRange);
        buttonMagRange = (Button)  rootView.findViewById(R.id.buttonMagRange);
        buttonGsr = (Button) rootView.findViewById(R.id.buttonGSR);
        buttonSampleRate = (Button) rootView.findViewById(R.id.buttonRate);
        buttonAccRange = (Button) rootView.findViewById(R.id.buttonAccel);
        buttonDeviceName = (Button) rootView.findViewById(R.id.buttonDeviceName);
        buttonExgGain = (Button) rootView.findViewById(R.id.buttonExgGain);
        buttonExgRes = (Button) rootView.findViewById(R.id.buttonExgRes);
        buttonBattVoltLimit = (Button) rootView.findViewById(R.id.buttonBattLimit);
        buttonDone = (Button) rootView.findViewById(R.id.buttonDone);
        buttonEnableSensorsConfiguration = (Button) rootView.findViewById(R.id.buttonEnableSensorsConfiguration);
        buttonDone = (Button) rootView.findViewById(R.id.buttonDone);
        cBox5VReg = (CheckBox) rootView.findViewById(R.id.checkBox5VReg);
        cBoxHeartRate = (CheckBox) rootView.findViewById(R.id.CheckBoxPPGToHeartRate);
		cBoxLowPowerMag = (CheckBox) rootView.findViewById(R.id.checkBoxLowPowerMag);
		cBoxLowPowerAccel = (CheckBox) rootView.findViewById(R.id.checkBoxLowPowerAccel);
		cBoxLowPowerGyro = (CheckBox) rootView.findViewById(R.id.checkBoxLowPowerGyro);
		cBoxInternalExpPower  = (CheckBox) rootView.findViewById(R.id.CheckBoxIntExpPow);
        
		mSensorToHeartRate="";
        final String[] accelRangeArray = {"+/- 1.5g","+/- 6g"};
        mBluetoothAddress = shimmerConfig.getBluetoothAddress();
        String rate = Double.toString(mSamplingRateV);
        buttonSampleRate.setText("Sampling Rate "+"\n ("+rate+" Hz)");
        buttonDeviceName.setText("Device Name"+"\n ("+shimmerConfig.getDeviceName()+")");
        
        if(mService.isHeartRateEnabled() && deviceBluetoothAddress.equals(mService.mBluetoothAddressToHeartRate))
        	cBoxHeartRate.setChecked(true);
        else
        	cBoxHeartRate.setChecked(false);

        if(shimmerConfig.getShimmerVersion()!=ShimmerVerDetails.HW_ID.SHIMMER_3){
        	buttonExgGain.setEnabled(false);
        	buttonExgRes.setEnabled(false);
        }
        else{
        	buttonExgGain.setEnabled(true);
        	buttonExgRes.setEnabled(true);
        }
        
        
        if (mAccelerometerRangeV==0){
        	if (shimmerConfig.getShimmerVersion()!=ShimmerVerDetails.HW_ID.SHIMMER_3){
        		buttonAccRange.setText("Accel Range"+"\n"+"(+/- 1.5g)");
        	} else {
        		buttonAccRange.setText("Accel Range"+"\n"+"(+/- 2g)");
        	}
        } else if (mAccelerometerRangeV==1){
        	buttonAccRange.setText("Accel Range"+"\n"+"(+/- 4g)");
        } else if (mAccelerometerRangeV==2){
        	buttonAccRange.setText("Accel Range"+"\n"+"(+/- 8g)");
        }
        else if (mAccelerometerRangeV==3){
        	if (shimmerConfig.getShimmerVersion()!=ShimmerVerDetails.HW_ID.SHIMMER_3){
        		buttonAccRange.setText("Accel Range"+"\n"+"(+/- 6g)");
        	} else {
        		buttonAccRange.setText("Accel Range"+"\n"+"(+/- 16g)");
        	}
        } 

        buttonGsr.setText("GSR Range"+"\n"+ SensorGSR.ListofGSRRangeResistance[mGSRRangeV]);
        
        if(shimmerConfig.getIntExpPower()==1){
  			cBoxInternalExpPower.setChecked(true);
  		} else {
  			cBoxInternalExpPower.setChecked(false);
  		}
  		
  		if (shimmerConfig.isLowPowerMagEnabled()){
    		cBoxLowPowerMag.setChecked(true);
    	}
    	
    	if (shimmerConfig.isLowPowerAccelEnabled()){
    		cBoxLowPowerAccel.setChecked(true);
    	}
    	
    	if (shimmerConfig.isLowPowerGyroEnabled()){
    		cBoxLowPowerGyro.setChecked(true);
    	}
    	
    	int gain = mService.getEXGGain(mBluetoothAddress);
    	if(gain!=-1)
    		buttonExgGain.setText("EXG Gain"+"\n ("+gain+")");
    	else
    		buttonExgGain.setText("EXG Gain"+"\n (no gain set)");
    	
    	exgRes = mService.getEXGResolution(mBluetoothAddress);
    	if(exgRes==16 || exgRes==24)
    		buttonExgRes.setText("EXG Res"+"\n ("+exgRes+" bit)");
    	else
    		buttonExgRes.setText("EXG Res"+"\n (no res. set)");
        
        if (shimmerConfig.getShimmerVersion()==ShimmerVerDetails.HW_ID.SHIMMER_3){
        	cBox5VReg.setEnabled(false);
        	String currentGyroRange = "("+ SensorMPU9X50.ListofGyroRange[shimmerConfig.getGyroRange()]+")";
        	buttonGyroRange.setText("Gyro Range"+"\n"+currentGyroRange);
        	String currentMagRange = "("+ SensorLSM303.ListofMagRange[shimmerConfig.getMagRange()-1]+")";
    		buttonMagRange.setText("Mag Range"+"\n"+currentMagRange);
    		String currentPressureResolution = "("+ListofPressureResolution[shimmerConfig.getPressureResolution()]+")";
    		buttonPressureResolution.setText("Pressure Res"+"\n"+currentPressureResolution);
        	
    		
        	if (shimmerConfig.getAccelRange()==0){
        		cBoxLowPowerAccel.setEnabled(false);
        	}
        	
        	//currently not supported for the moment 
    		buttonPressureResolution.setEnabled(true);

    		
    	} else {
    		cBoxInternalExpPower.setEnabled(false);
    		buttonPressureResolution.setEnabled(false);
    		buttonGyroRange.setEnabled(false);
    		cBoxLowPowerAccel.setEnabled(false);
    		cBoxLowPowerGyro.setEnabled(false);
    		String currentMagRange = "("+Configuration.Shimmer2.ListofMagRange[shimmerConfig.getMagRange()]+")";
    		buttonMagRange.setText("Mag Range"+"\n"+currentMagRange);
    	}
        
        if (mService.get5VReg(mBluetoothAddress)==1){  
  			cBox5VReg.setChecked(true);
  		}
                  
        final AlertDialog.Builder dialogSetName = new AlertDialog.Builder(getActivity());
        dialogSetName.setTitle("Device name").setMessage("Introduce the new device name")
        			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							if (mService.noDevicesStreaming()){
								String newName = editTextDeviceName.getText().toString();
	    						shimmerConfig.setDeviceName(newName);
	    						mService.mShimmerConfigurationList.set(currentPosition, shimmerConfig);
					            Toast.makeText(getActivity(), "Device name changed. New name = "+newName, Toast.LENGTH_SHORT).show();
					            buttonDeviceName.setText("Device name "+"\n"+"("+newName+")");
							}
							else
								Toast.makeText(getActivity(), "Please ensure no device is streaming.", Toast.LENGTH_LONG).show();	
						}
					});
        
        
        buttonDeviceName.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				// This is done in order to avoid an error when the dialog is displayed again after being cancelled
				if(editTextDeviceName.getParent()!=null){
					ViewGroup parentViewGroup = (ViewGroup)editTextDeviceName.getParent();
					parentViewGroup.removeView(editTextDeviceName);
				}
				
				dialogSetName.setView(editTextDeviceName);
				dialogSetName.show();				
			}
		});
    	
    	
        final AlertDialog.Builder dialogBattLimit = new AlertDialog.Builder(getActivity());
        dialogBattLimit.setTitle("Battery Limit").setMessage("Introduce the battery limit to be set")
        				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								double newLimit = Double.parseDouble(editTextBattLimit.getText().toString());
								mService.setBattLimitWarning(mBluetoothAddress, newLimit);
					            Toast.makeText(getActivity(), "Battery limit changed. New limit = "+newLimit+" V", Toast.LENGTH_SHORT).show();
					            buttonBattVoltLimit.setText("Batt Limit "+"\n"+"("+newLimit+" V)");
							}
        				});
        
        double batteryLimit = mService.getBattLimitWarning(mBluetoothAddress);
        buttonBattVoltLimit.setText("Batt Limit "+"\n"+"("+Double.toString(batteryLimit)+" V)");

        
    buttonBattVoltLimit.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				

				// This is done in order to avoid an error when the dialog is displayed again after being cancelled
				if(editTextBattLimit.getParent()!=null){
					ViewGroup parentViewGroup = (ViewGroup)editTextBattLimit.getParent();
					parentViewGroup.removeView(editTextBattLimit);
				}
				
				dialogBattLimit.setView(editTextBattLimit);
				dialogBattLimit.show();			
			}
    		
    	});	
    	
    	
    	
    	final AlertDialog.Builder dialogRate = new AlertDialog.Builder(getActivity());		 
        dialogRate.setTitle("Sampling Rate").setItems(samplingRate, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 Log.d("Shimmer",samplingRate[item]);
                		 double newRate = Double.valueOf(samplingRate[item]);
 						 shimmerConfig.setSamplingRate(newRate);
 						 mService.mShimmerConfigurationList.set(currentPosition, shimmerConfig);
 						 mService.writeSamplingRate(mBluetoothAddress, newRate);
                		 Toast.makeText(getActivity(), "Sample rate changed. New rate = "+newRate+" Hz", Toast.LENGTH_SHORT).show();
                		 buttonSampleRate.setText("Sampling Rate "+"\n"+"("+newRate+" Hz)");
                }
        });
    	
    	buttonSampleRate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub		        
		        dialogRate.show();
			}
		});
    	
    	
    	final AlertDialog.Builder dialogAccelShimmer2 = new AlertDialog.Builder(getActivity());		 
    	dialogAccelShimmer2.setTitle("Accelerometer Range").setItems(accelRangeArray, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 Log.d("Shimmer",accelRangeArray[item]);
	           		    int accelRange=0;

	         	  		    if (accelRangeArray[item]=="+/- 1.5g"){
	         	  		    	accelRange=0;
	         	  		    } else if (accelRangeArray[item]=="+/- 6g"){
	         	  		    	accelRange=3;
	         	  		    }
	         	  		    
	         	  		    shimmerConfig.setAccelRange(accelRange);
	         	  		    mService.mShimmerConfigurationList.set(currentPosition, shimmerConfig);
	         	  		    mService.writeAccelRange(mBluetoothAddress, accelRange);
	         	  		    Toast.makeText(getActivity(), "Accelerometer rate changed. New rate = "+accelRangeArray[item], Toast.LENGTH_SHORT).show();
	         	  		    buttonAccRange.setText("Accel Range"+"\n"+"("+accelRangeArray[item]+")");
                }
        });
        
    	
    	final AlertDialog.Builder dialogAccelShimmer3 = new AlertDialog.Builder(getActivity());		 
    	dialogAccelShimmer3.setTitle("Accelerometer Range").setItems(SensorLSM303.ListofAccelRange, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 Log.d("Shimmer",SensorLSM303.ListofAccelRange[item]);
	           		    int accelRange=0;
	           		    
	           		    	if (SensorLSM303.ListofAccelRange[item]=="+/- 2g"){
	         	  		    	accelRange=0;
	         	  		    } else if (SensorLSM303.ListofAccelRange[item]=="+/- 4g"){
	         	  		    	accelRange=1;
	         	  		    } else if (SensorLSM303.ListofAccelRange[item]=="+/- 8g"){
	         	  		    	accelRange=2;
	         	  		    } else if (SensorLSM303.ListofAccelRange[item]=="+/- 16g"){
	         	  		    	accelRange=3;
	         	  		    }

	           		    if(accelRange==0)
	           		    	cBoxLowPowerAccel.setEnabled(false);
	           		    else
	           		    	cBoxLowPowerAccel.setEnabled(true);
	           		    
	           		    shimmerConfig.setAccelRange(accelRange);
	           		    mService.mShimmerConfigurationList.set(currentPosition, shimmerConfig);
	           		    mService.writeAccelRange(mBluetoothAddress, accelRange);
	         	  		Toast.makeText(getActivity(), "Accelerometer rate changed. New rate = "+SensorLSM303.ListofAccelRange[item], Toast.LENGTH_SHORT).show();
	         	  		buttonAccRange.setText("Accel Range"+"\n"+"("+SensorLSM303.ListofAccelRange[item]+")");
	           		    
                }
        });
        
        
        buttonAccRange.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mService.getShimmerVersion(mBluetoothAddress)!=ShimmerVerDetails.HW_ID.SHIMMER_3)
					dialogAccelShimmer2.show();
				else
					dialogAccelShimmer3.show();
			}
		});
        
        
        final AlertDialog.Builder dialogGyroRangeShimmer3 = new AlertDialog.Builder(getActivity());		 
        dialogGyroRangeShimmer3.setTitle("Gyroscope Range").setItems(SensorMPU9X50.ListofGyroRange, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 Log.d("Shimmer",SensorMPU9X50.ListofGyroRange[item]);
             		    int gyroRange=0;
               		  
               		    if (SensorMPU9X50.ListofGyroRange[item]==SensorMPU9X50.ListofGyroRange[0]){
             	  		    	gyroRange=0;
             	  		    } else if (SensorMPU9X50.ListofGyroRange[item]==SensorMPU9X50.ListofGyroRange[1]){
             	  		    	gyroRange=1;
             	  		    } else if (SensorMPU9X50.ListofGyroRange[item]==SensorMPU9X50.ListofGyroRange[2]){
             	  		    	gyroRange=2;
             	  		    } else if (SensorMPU9X50.ListofGyroRange[item]==SensorMPU9X50.ListofGyroRange[3]){
             	  		    	gyroRange=3;
             	  		    }

               		    shimmerConfig.setGyroRange(gyroRange);
               		    mService.mShimmerConfigurationList.set(currentPosition, shimmerConfig);
               		    mService.writeGyroRange(mBluetoothAddress, gyroRange);
               		    Toast.makeText(getActivity(), "Gyroscope rate changed. New rate = "+SensorMPU9X50.ListofGyroRange[item], Toast.LENGTH_SHORT).show();
               		    buttonGyroRange.setText("Gyro Range"+"\n"+"("+SensorMPU9X50.ListofGyroRange[item]+")");
                }
        });
        
        buttonGyroRange.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialogGyroRangeShimmer3.show();
			}
		});

        
        
        final AlertDialog.Builder dialogMagRangeShimmer2 = new AlertDialog.Builder(getActivity());		 
        dialogMagRangeShimmer2.setTitle("Magnetometer Range").setItems(Configuration.Shimmer2.ListofMagRange, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 Log.d("Shimmer",Configuration.Shimmer2.ListofMagRange[item]);
                		 int magRange=0;
             		  
             		     
             		    	if (Configuration.Shimmer2.ListofMagRange[item]==Configuration.Shimmer2.ListofMagRange[0]){
             		    		magRange=0;
             		    	} else if (Configuration.Shimmer2.ListofMagRange[item]==Configuration.Shimmer2.ListofMagRange[1]){
             		    		magRange=1;
             		    	} else if (Configuration.Shimmer2.ListofMagRange[item]==Configuration.Shimmer2.ListofMagRange[2]){
             		    		magRange=2;
             		    	} else if (Configuration.Shimmer2.ListofMagRange[item]==Configuration.Shimmer2.ListofMagRange[3]){
             		    		magRange=3;
             		    	} else if (Configuration.Shimmer2.ListofMagRange[item]==Configuration.Shimmer2.ListofMagRange[4]){
             		    		magRange=4;
             		    	} else if (Configuration.Shimmer2.ListofMagRange[item]==Configuration.Shimmer2.ListofMagRange[5]){
             		    		magRange=5;
             		    	} else if (Configuration.Shimmer2.ListofMagRange[item]==Configuration.Shimmer2.ListofMagRange[6]){
             		    		magRange=6;
             		    	} else if (Configuration.Shimmer2.ListofMagRange[item]==Configuration.Shimmer2.ListofMagRange[7]){
             		    		magRange=7;

             		     }
 
             		    shimmerConfig.setMagRange(magRange);
	         	  		mService.mShimmerConfigurationList.set(currentPosition, shimmerConfig);
	         	  		mService.writeMagRange(mBluetoothAddress, magRange);
             		    Toast.makeText(getActivity(), "Magnometer rate changed. New rate = "+Configuration.Shimmer2.ListofMagRange[item], Toast.LENGTH_SHORT).show();
             		   buttonMagRange.setText("Mag Range"+"\n"+"("+Configuration.Shimmer2.ListofMagRange[item]+")");
           	      }
        });
        
        final AlertDialog.Builder dialogMagRangeShimmer3 = new AlertDialog.Builder(getActivity());		 
        dialogMagRangeShimmer3.setTitle("Magnetometer Range").setItems(SensorLSM303.ListofMagRange, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 Log.d("Shimmer",SensorLSM303.ListofMagRange[item]);
                		 int magRange=0;
             		  
             		    	if (SensorLSM303.ListofMagRange[item]==SensorLSM303.ListofMagRange[0]){
             		    		magRange=1;
             		    	} else if (SensorLSM303.ListofMagRange[item]==SensorLSM303.ListofMagRange[1]){
             		    		magRange=2;
             		    	} else if (SensorLSM303.ListofMagRange[item]==SensorLSM303.ListofMagRange[2]){
             		    		magRange=3;
             		    	} else if (SensorLSM303.ListofMagRange[item]==SensorLSM303.ListofMagRange[3]){
             		    		magRange=4;
             		    	} else if (SensorLSM303.ListofMagRange[item]==SensorLSM303.ListofMagRange[4]){
             		    		magRange=5;
             		    	} else if (SensorLSM303.ListofMagRange[item]==SensorLSM303.ListofMagRange[5]){
             		    		magRange=6;
             		    	} else if (SensorLSM303.ListofMagRange[item]==SensorLSM303.ListofMagRange[6]){
             		    		magRange=7;
             		    	}

             		    	shimmerConfig.setMagRange(magRange);
	         	  		    mService.mShimmerConfigurationList.set(currentPosition, shimmerConfig);
	         	  		    mService.writeMagRange(mBluetoothAddress, magRange);
                 		    Toast.makeText(getActivity(), "Magnometer rate changed. New rate = "+SensorLSM303.ListofMagRange[item], Toast.LENGTH_SHORT).show();
                 		    buttonMagRange.setText("Mag Range"+"\n"+"("+SensorLSM303.ListofMagRange[item]+")");
                	}
        });
        
        buttonMagRange.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if (mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3)
					 dialogMagRangeShimmer3.show();
				 else
					 dialogMagRangeShimmer2.show();
			}
		});


        
        final AlertDialog.Builder dialogPressureResolutionShimmer3 = new AlertDialog.Builder(getActivity());		 
        dialogPressureResolutionShimmer3.setTitle("Pressure Resolution").setItems(ListofPressureResolution, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 Log.d("Shimmer",ListofPressureResolution[item]);
                		 int pressureRes=0;
               		  
               		    if (ListofPressureResolution[item]==ListofPressureResolution[0]){
             	  		    	pressureRes=0;
             	  		    } else if (ListofPressureResolution[item]==ListofPressureResolution[1]){
             	  		    	pressureRes=1;
             	  		    } else if (ListofPressureResolution[item]==ListofPressureResolution[2]){
             	  		    	pressureRes=2;
             	  		    } else if (ListofPressureResolution[item]==ListofPressureResolution[3]){
             	  		    	pressureRes=3;
             	  		    } 

               		    shimmerConfig.setPressureResolution(pressureRes);
               		    mService.mShimmerConfigurationList.set(currentPosition, shimmerConfig);
               		    mService.writePressureResolution(mBluetoothAddress, pressureRes);
               		    Toast.makeText(getActivity(), "Pressure resolution changed. New resolution = "+ListofPressureResolution[item], Toast.LENGTH_SHORT).show();
               		    buttonPressureResolution.setText("Pressure Res"+"\n"+"("+ListofPressureResolution[item]+")");
           	      }
        });
        
        buttonPressureResolution.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialogPressureResolutionShimmer3.show();
			}
		});
        
        
        //The Gsr Range is the same for the Shimmer 3 and the Shimmer 2 so we only need to do one dialog
        final AlertDialog.Builder dialogGsrRange = new AlertDialog.Builder(getActivity());		 
        dialogGsrRange.setTitle("GSR Range").setItems(SensorGSR.ListofGSRRangeResistance, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 Log.d("Shimmer",SensorGSR.ListofGSRRangeResistance[item]);
             		    int gsrRange=0;
             		    if (SensorGSR.ListofGSRRangeResistance[item]==SensorGSR.ListofGSRRangeResistance[0]){
             		    	gsrRange=0;
             		    } else if (SensorGSR.ListofGSRRangeResistance[item]==SensorGSR.ListofGSRRangeResistance[1]){
             		    	gsrRange=1;
             		    } else if (SensorGSR.ListofGSRRangeResistance[item]==SensorGSR.ListofGSRRangeResistance[2]){
             		    	gsrRange=2;
             		    } else if (SensorGSR.ListofGSRRangeResistance[item]==SensorGSR.ListofGSRRangeResistance[3]){
             		    	gsrRange=3;
             		    } else if (SensorGSR.ListofGSRRangeResistance[item]==SensorGSR.ListofGSRRangeResistance[4]){
             		    	gsrRange=4;
             		    }

             		    shimmerConfig.setGSRRange(gsrRange);
        	  		    mService.mShimmerConfigurationList.set(currentPosition, shimmerConfig);
        	  		    mService.writeGSRRange(mBluetoothAddress, gsrRange);
             		    Toast.makeText(getActivity(), "Gsr range changed. New range = "+SensorGSR.ListofGSRRangeResistance[item], Toast.LENGTH_SHORT).show();
             		    buttonGsr.setText("GSR Range"+"\n"+"("+SensorGSR.ListofGSRRangeResistance[item]+")");
             		   
           	      }
        });
        
        buttonGsr.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialogGsrRange.show();
			}
		});
        
        
        final AlertDialog.Builder dialogExgGain = new AlertDialog.Builder(getActivity());		 
        dialogExgGain.setTitle("ExG Gain").setItems(exgGain, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 Log.d("Shimmer",exgGain[item]);
             		    int exgGainNew=0;
             		    
             		    if (exgGain[item]=="6"){
             		    	exgGainNew=0;
             		    } else if (exgGain[item]=="1"){
             		    	exgGainNew=1;
             		    } else if (exgGain[item]=="2"){
             		    	exgGainNew=2;
             		    } else if (exgGain[item]=="3"){
             		    	exgGainNew=3;
             		    } else if (exgGain[item]=="4"){
             		    	exgGainNew=4;
             		    } else if (exgGain[item]=="8"){
             		    	exgGainNew=5;
             		    } else if (exgGain[item]=="12"){
             		    	exgGainNew=6;
             		    }

             		    mService.writeEXGGainSetting(mBluetoothAddress, exgGainNew);
             		    Toast.makeText(getActivity(), "Exg gain changed. New gain = "+exgGain[item], Toast.LENGTH_SHORT).show();
             		    buttonExgGain.setText("EXG Gain"+"\n"+"("+exgGain[item]+")");
             		   
           	      }
        });
        
        buttonExgGain.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(mService.getShimmer(mBluetoothAddress).getFirmwareVersionCode()>2){
					dialogExgGain.show();
				}
				else
					Toast.makeText(getActivity(), "Operation not supported in this FW Version", Toast.LENGTH_SHORT).show();
			}
		});
        
        
        final AlertDialog.Builder dialogExgRes = new AlertDialog.Builder(getActivity());		 
        dialogExgRes.setTitle("ExG Resolution").setItems(exgResolution, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 Log.d("Shimmer",exgGain[item]);
                		 
             		    long enabledSensor=mService.getEnabledSensors(mBluetoothAddress);
             		    if (exgResolution[item]=="16 bits"){
             		    	if (((enabledSensor & Shimmer.SENSOR_EXG1_24BIT)>0 && (enabledSensor & Shimmer.SENSOR_EXG2_24BIT)>0)){//if 24bit sensors are enabled, change to 16 bit
	             		    	enabledSensor = mService.sensorConflictCheckandCorrection(enabledSensor,Shimmer.SENSOR_EXG1_16BIT,(int)mShimmerVersion);
	             		    	enabledSensor = mService.sensorConflictCheckandCorrection(enabledSensor,Shimmer.SENSOR_EXG2_16BIT,(int)mShimmerVersion);
             		    	}
             		    	exgRes = 16;
             		    }
             		    else{ 
             		    	if (((enabledSensor & Shimmer.SENSOR_EXG1_16BIT)>0 && (enabledSensor & Shimmer.SENSOR_EXG2_16BIT)>0)){//if 16bit sensors are enabled, change to 24 bit
	             		    	enabledSensor = mService.sensorConflictCheckandCorrection(enabledSensor,Shimmer.SENSOR_EXG1_24BIT,(int)mShimmerVersion);
	             		    	enabledSensor = mService.sensorConflictCheckandCorrection(enabledSensor,Shimmer.SENSOR_EXG2_24BIT,(int)mShimmerVersion);
             		    	}
             		    	exgRes = 24;
             		    }
						
//						shimmerConfig.setEnabledSensors(enabledSensor);
//        	  		    mService.mShimmerConfigurationList.set(currentPosition, shimmerConfig);					
             		    mService.setEnabledSensors(enabledSensor, mBluetoothAddress);
             		    Toast.makeText(getActivity(), "Exg resolution changed. New resolution = "+exgResolution[item], Toast.LENGTH_SHORT).show();
             		    buttonExgRes.setText("EXG Res"+"\n"+"("+exgResolution[item]+")");
             		   
           	      }
        });
        
        
        buttonExgRes.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialogExgRes.show();
			}
		});
        
        
        final AlertDialog.Builder dialogResetConfiguration = new AlertDialog.Builder(getActivity());		 
        dialogResetConfiguration.setTitle("Reset Configuration").setMessage("The device configuration will be reset.\\n Do you want to continue?")
        	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
        
        
        enableSensorDialog = new Dialog(getActivity());
		enableSensorDialog.setTitle("Enable Sensor");
		enableSensorDialog.setContentView(R.layout.enable_sensor_list);
		
		
		enableSensorListView = (ListView) enableSensorDialog.findViewById(R.id.listEnableSensor);
		enableSensorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				if(mService.getShimmerVersion(deviceBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && compatibleSensors[position].equals("ECG")){
					int exg1_24bits = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1"));
					int exg2_24bits = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2"));
					int exg1_16bits = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1 16Bit"));
					int exg2_16bits = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2 16Bit"));
					
					if(enableSensorListView.isItemChecked(position)){ //if the ECG is checked
						if(exgRes==16){ //resolution selected is 16
							if(!((enabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0)){ //if the sensors are not enabled yet, then enable them
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg1_16bits, mService.getShimmerVersion(deviceBluetoothAddress));
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg2_16bits, mService.getShimmerVersion(deviceBluetoothAddress));
							}
						}
						else{ //either the resolution is 24 or is not selected
							if (!((enabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0)){  //if the sensors are not enabled yet, then enable them
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg1_24bits, mService.getShimmerVersion(deviceBluetoothAddress));
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg2_24bits, mService.getShimmerVersion(deviceBluetoothAddress));
							}
						}
						mService.writeEXGSetting(deviceBluetoothAddress, 0);
						enableSensorListView.setItemChecked(position, true); //ECG
					}
					else{ //if the ECG is unchecked
						if(exgRes==16){ //resolution selected is 16
							if((enabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0){ //if the sensors are enabled, then disable them
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg1_16bits, mService.getShimmerVersion(deviceBluetoothAddress));
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg2_16bits, mService.getShimmerVersion(deviceBluetoothAddress));
							}
						}
						else{ //either the resolution is 24 or is not selected
							if ((enabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0){  //if the sensors are enabled, then disable them
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg1_24bits, mService.getShimmerVersion(deviceBluetoothAddress));
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg2_24bits, mService.getShimmerVersion(deviceBluetoothAddress));
							}
						}
					}
					
					enableSensorListView.setItemChecked(position+1, false); //EMG
					enableSensorListView.setItemChecked(position+2, false);// TEST SIGNAL
					
//					if (!((enabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0) && 
//							!((enabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0)){
//						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue1, mService.getShimmerVersion(deviceBluetoothAddress));
//						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue3, mService.getShimmerVersion(deviceBluetoothAddress));
//					}
//					
//					if(!enableSensorListView.isItemChecked(position)){
//						enableSensorListView.setItemChecked(position, false); //ECG
//						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue1, mService.getShimmerVersion(deviceBluetoothAddress));
//						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue3, mService.getShimmerVersion(deviceBluetoothAddress));
//					}
//					else
//						enableSensorListView.setItemChecked(position, true); //ECG
//					enableSensorListView.setItemChecked(position+1, false); //EMG
//					enableSensorListView.setItemChecked(position+2, false);// TEST SIGNAL
//					if(enableSensorListView.isItemChecked(position))
//						mService.writeEXGSetting(deviceBluetoothAddress, 0);

					
				}
				else if(mService.getShimmerVersion(deviceBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && compatibleSensors[position].equals("EMG")){
					int exg1_24bits = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1"));
					int exg2_24bits = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2"));
					int exg1_16bits = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1 16Bit"));
					int exg2_16bits = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2 16Bit"));
					
					if(enableSensorListView.isItemChecked(position)){ //if the EMG is checked
						if(exgRes==16){ //resolution selected is 16
							if(!((enabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0)){ //if the sensors are not enabled yet, then enable them
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg1_16bits, mService.getShimmerVersion(deviceBluetoothAddress));
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg2_16bits, mService.getShimmerVersion(deviceBluetoothAddress));
							}
						}
						else{ //either the resolution is 24 or is not selected
							if (!((enabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0)){  //if the sensors are not enabled yet, then enable them
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg1_24bits, mService.getShimmerVersion(deviceBluetoothAddress));
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg2_24bits, mService.getShimmerVersion(deviceBluetoothAddress));
							}
						}
						mService.writeEXGSetting(deviceBluetoothAddress, 1);
						enableSensorListView.setItemChecked(position, true); //EMG
					}
					else{ //if the ECG is unchecked
						if(exgRes==16){ //resolution selected is 16
							if((enabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0){ //if the sensors are enabled, then disable them
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg1_16bits, mService.getShimmerVersion(deviceBluetoothAddress));
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg2_16bits, mService.getShimmerVersion(deviceBluetoothAddress));
							}
						}
						else{ //either the resolution is 24 or is not selected
							if ((enabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0){  //if the sensors are enabled, then disable them
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg1_24bits, mService.getShimmerVersion(deviceBluetoothAddress));
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg2_24bits, mService.getShimmerVersion(deviceBluetoothAddress));
							}
						}
					}
					
					enableSensorListView.setItemChecked(position-1, false); //ECG
					enableSensorListView.setItemChecked(position+1, false); //TEST SIGNAL
					
//					int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1"));
//					int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2"));
//					if (!((enabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0) && 
//							!((enabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0)){
//						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue1, mService.getShimmerVersion(deviceBluetoothAddress));
//						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue3, mService.getShimmerVersion(deviceBluetoothAddress));
//					}
//					if(!enableSensorListView.isItemChecked(position)){
//						enableSensorListView.setItemChecked(position, false); //EMG
//						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue1, mService.getShimmerVersion(deviceBluetoothAddress));
//						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue3, mService.getShimmerVersion(deviceBluetoothAddress));
//					}
//					else
//						enableSensorListView.setItemChecked(position, true); //EMG
//					enableSensorListView.setItemChecked(position-1, false); //ECG
//					enableSensorListView.setItemChecked(position+1, false); //TEST SIGNAL
//					if(enableSensorListView.isItemChecked(position))
//						mService.writeEXGSetting(deviceBluetoothAddress, 1);

						
				}
				else if(mService.getShimmerVersion(deviceBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && compatibleSensors[position].equals("Test signal")){
					int exg1_24bits = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1"));
					int exg2_24bits = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2"));
					int exg1_16bits = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1 16Bit"));
					int exg2_16bits = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2 16Bit"));
					
					if(enableSensorListView.isItemChecked(position)){ //if the TEST SIGNAL is checked
						if(exgRes==16){ //resolution selected is 16
							if(!((enabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0)){ //if the sensors are not enabled yet, then enable them
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg1_16bits, mService.getShimmerVersion(deviceBluetoothAddress));
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg2_16bits, mService.getShimmerVersion(deviceBluetoothAddress));
							}
						}
						else{ //either the resolution is 24 or is not selected
							if (!((enabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0)){  //if the sensors are not enabled yet, then enable them
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg1_24bits, mService.getShimmerVersion(deviceBluetoothAddress));
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg2_24bits, mService.getShimmerVersion(deviceBluetoothAddress));
							}
						}
						mService.writeEXGSetting(deviceBluetoothAddress, 2);
						enableSensorListView.setItemChecked(position, true); //TEST SIGNAL
					}
					else{ //if the ECG is unchecked
						if(exgRes==16){ //resolution selected is 16
							if((enabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0){ //if the sensors are enabled, then disable them
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg1_16bits, mService.getShimmerVersion(deviceBluetoothAddress));
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg2_16bits, mService.getShimmerVersion(deviceBluetoothAddress));
							}
						}
						else{ //either the resolution is 24 or is not selected
							if ((enabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0){  //if the sensors are enabled, then disable them
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg1_24bits, mService.getShimmerVersion(deviceBluetoothAddress));
								enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,exg2_24bits, mService.getShimmerVersion(deviceBluetoothAddress));
							}
						}
					}
					
					enableSensorListView.setItemChecked(position-1, false); //EMG
					enableSensorListView.setItemChecked(position-2, false); //ECG
					
					
//					int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1"));
//					int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2"));
//					if (!((enabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0) && 
//							!((enabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0)){
//						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue1, mService.getShimmerVersion(deviceBluetoothAddress));
//						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue3, mService.getShimmerVersion(deviceBluetoothAddress));
//					}
//					if(!enableSensorListView.isItemChecked(position)){
//						enableSensorListView.setItemChecked(position, false); //TEST SIGNAL
//						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue1, mService.getShimmerVersion(deviceBluetoothAddress));
//						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue3, mService.getShimmerVersion(deviceBluetoothAddress));
//					}
//					else
//						enableSensorListView.setItemChecked(position, true); //TEST SIGNAL
//					enableSensorListView.setItemChecked(position-1, false); //EMG
//					enableSensorListView.setItemChecked(position-2, false); //ECG
//					if(enableSensorListView.isItemChecked(position))
//						mService.writeEXGSetting(deviceBluetoothAddress, 2);
				
						
				}
				else{
					int sensorIdentifier = Integer.parseInt(sensorBitmaptoName.inverse().get(compatibleSensors[position]));
					//check and remove any old daughter boards (sensors) which will cause a conflict with sensorIdentifier 
					enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,sensorIdentifier, mService.getShimmerVersion(deviceBluetoothAddress));
					//update the checkbox accordingly 
					//since the last three elements (ECG,EMG,TestSignal) in Shimmer 3 are not signals,
					//we treat them in a different way and they are not updated like the rest
					int end=0;
					if(mService.getShimmerVersion(deviceBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3)
						end=compatibleSensors.length-3;
					else
						end=compatibleSensors.length;
					
					for (int i=0;i<end;i++){
						int iDBMValue = Integer.parseInt(sensorBitmaptoName.inverse().get(compatibleSensors[i]));	
						if( (iDBMValue & enabledSensors) >0){
							enableSensorListView.setItemChecked(i, true);
						} else {
							enableSensorListView.setItemChecked(i, false);
						}
					}
				}
			}
		});
		
        
        
        buttonEnableSensorsConfiguration.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				
				Shimmer shimmer = mService.getShimmer(deviceBluetoothAddress);
				compatibleSensors = shimmer.getListofSupportedSensors();
				
				if(shimmer.getShimmerVersion() == ShimmerVerDetails.HW_ID.SHIMMER_3){ //replace EXG1, EXG2, EXG1 16 bit and EXG2 16 bit for ECG,EMG and test signal
					ArrayList<String> tmp = new ArrayList<String>();
					for(int i=0;i<compatibleSensors.length;i++)
						if(!compatibleSensors[i].equals("EXG1") && !compatibleSensors[i].equals("EXG2") &&
								!compatibleSensors[i].equals("EXG1 16Bit") && !compatibleSensors[i].equals("EXG2 16Bit")){
							tmp.add(compatibleSensors[i]);
						}
					tmp.add("ECG");
					tmp.add("EMG");
					tmp.add("Test signal");
					compatibleSensors = new String[tmp.size()];
					for(int i=0;i<tmp.size();i++)
						compatibleSensors[i] = tmp.get(i);
//					System.arraycopy(tmp,0, compatibleSensors, 0, tmp.size());
//					compatibleSensors =  (String[]) tmp.toArray();
				}
					enabledSensors=mService.getEnabledSensors(deviceBluetoothAddress);
//					List<SelectedSensors> listEnableSensors = createListOfEnableSensor(enabledSensors);						
					enableSensorListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
					ArrayAdapter<String> adapterSensorNames = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_multiple_choice, compatibleSensors);
					enableSensorListView.setAdapter(adapterSensorNames);
					sensorBitmaptoName = Shimmer.generateBiMapSensorIDtoSensorName(mService.getShimmerVersion(deviceBluetoothAddress));
					//check the enabled sensors
					for (int i=0;i<compatibleSensors.length;i++){
						if(mService.getShimmerVersion(deviceBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && compatibleSensors[i].equals("ECG")){
							if(mService.isEXGUsingECG16Configuration(deviceBluetoothAddress) ||
									mService.isEXGUsingECG24Configuration(deviceBluetoothAddress)){ 
								enableSensorListView.setItemChecked(i, true);
							}
						}
						else if(mService.getShimmerVersion(deviceBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && compatibleSensors[i].equals("EMG")){
							if(mService.isEXGUsingEMG16Configuration(deviceBluetoothAddress) ||
									mService.isEXGUsingEMG24Configuration(deviceBluetoothAddress)){ 
								enableSensorListView.setItemChecked(i, true);
							}
						}
						else if(mService.getShimmerVersion(deviceBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && compatibleSensors[i].equals("Test signal")){
							if(mService.isEXGUsingTestSignal16Configuration(deviceBluetoothAddress) || 
									mService.isEXGUsingTestSignal24Configuration(deviceBluetoothAddress)){ 
								enableSensorListView.setItemChecked(i, true);
							}
						} 							
						else{
							int iDBMValue = Integer.parseInt(sensorBitmaptoName.inverse().get(compatibleSensors[i]));	
							if( (iDBMValue & enabledSensors) >0){
								enableSensorListView.setItemChecked(i, true);
							}
						}
					}
					enableSensorDialog.show();
			}
		});
        
        buttonEnableSensor = (Button) enableSensorDialog.findViewById(R.id.buttonEnableSensor);
        buttonEnableSensor.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				mService.setEnabledSensors(enabledSensors, deviceBluetoothAddress);
				enableSensorDialog.dismiss();
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				int gain = mService.getEXGGain(mBluetoothAddress);
		    	if(gain!=-1)
		    		buttonExgGain.setText("EXG Gain"+"\n ("+gain+")");
		    	else
		    		buttonExgGain.setText("EXG Gain"+"\n (no gain set)");
		    	
		    	exgRes = mService.getEXGResolution(mBluetoothAddress);
		    	if(exgRes==16 || exgRes==24)
		    		buttonExgRes.setText("EXG Res"+"\n ("+exgRes+" bit)");
		    	else
		    		buttonExgRes.setText("EXG Res"+"\n (no res. set)");
			}
		});
        
        buttonDone.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mService.isHeartRateEnabled()){
					mService.resetHearRateConfiguration(mSensorToHeartRate);
					Log.d("button done", "change the cofiguration");
				}
				getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
				db.saveShimmerConfigurations("Temp", mService.mShimmerConfigurationList);
				Fragment fragment = new DevicesFragment();
				FragmentManager fragmentManager = getFragmentManager();
				String tag = "Devices";
				fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, tag).commit();
				getActivity().getActionBar().setTitle(tag); //set the title of the window
			}
		});
        
		
		cBox5VReg.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
				// TODO Auto-generated method stub
				if (checked){
					mService.write5VReg(mBluetoothAddress, 1);
				} else {
					mService.write5VReg(mBluetoothAddress, 0);
				}
			}
    		
    	});
    	
  		
  		cBoxLowPowerAccel.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
				// TODO Auto-generated method stub
				if (checked){
					mService.setAccelLowPower(mBluetoothAddress, 1);
					shimmerConfig.setLowPowerAccelEnabled(1);
					mService.mShimmerConfigurationList.set(currentPosition, shimmerConfig);
				} else {
					mService.setAccelLowPower(mBluetoothAddress, 0);
					shimmerConfig.setLowPowerAccelEnabled(0);
					mService.mShimmerConfigurationList.set(currentPosition, shimmerConfig);
				}
			}
    		
    	});
  		
  		cBoxLowPowerGyro.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
				// TODO Auto-generated method stub
				if (checked){
					mService.setGyroLowPower(mBluetoothAddress, 1);
					shimmerConfig.setLowPowerGyroEnabled(1);
					mService.mShimmerConfigurationList.set(currentPosition, shimmerConfig);
				} else {
					mService.setGyroLowPower(mBluetoothAddress, 0);
					shimmerConfig.setLowPowerGyroEnabled(0);
					mService.mShimmerConfigurationList.set(currentPosition, shimmerConfig);
				}
			}
    		
    	});
  		
  		cBoxInternalExpPower.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
				// TODO Auto-generated method stub
				if (checked){
					mService.writeIntExpPower(mBluetoothAddress, 1);
					shimmerConfig.setIntExpPower(1);
					mService.mShimmerConfigurationList.set(currentPosition, shimmerConfig);
				} else {
					mService.writeIntExpPower(mBluetoothAddress, 0);
					shimmerConfig.setIntExpPower(0);
					mService.mShimmerConfigurationList.set(currentPosition, shimmerConfig);
				}
			}
    		
    	});


//  		cBoxLowPowerMag.setChecked(shimmerConfig.isLowPowerMagEnabled());
  		
  		cBoxLowPowerMag.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
				// TODO Auto-generated method stub
				if (checked){
					mService.setMagLowPower(mBluetoothAddress, 1);
					shimmerConfig.setLowPowerMagEnabled(1);
					mService.mShimmerConfigurationList.set(currentPosition, shimmerConfig);
				} else {
					mService.setMagLowPower(mBluetoothAddress, 0);
					shimmerConfig.setLowPowerMagEnabled(0);
					mService.mShimmerConfigurationList.set(currentPosition, shimmerConfig);
				}
			}
    		
    	});
  		
  		
  		heartRateDialog = new Dialog(getActivity());
        heartRateDialog.setTitle("Heart Rate");
        heartRateDialog.setCancelable(false);
  		heartRateDialog.setContentView(R.layout.heart_rate_dialog);
  		Spinner spinnerSensors = (Spinner) heartRateDialog.findViewById(R.id.spinnerSensors);
  		final long enableSensors = mService.getEnabledSensors(mBluetoothAddress);
  		if (((Shimmer.SENSOR_EXG1_24BIT & enableSensors)>0 || (Shimmer.SENSOR_EXG1_16BIT & enableSensors)>0) && (mService.isEXGUsingECG24Configuration(mBluetoothAddress)||mService.isEXGUsingECG16Configuration(mBluetoothAddress))){
  			final List<String> list=new ArrayList<String>();
  	        list.add(Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT);
  	        list.add(Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT);
			list.add(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT);
  	        list.add(Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT);
  	        ArrayAdapter<String> adp= new ArrayAdapter<String>(getActivity(),
  	        		android.R.layout.simple_list_item_1,list);
  	        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
  	        spinnerSensors.setAdapter(adp);
  		}
  		
  		
  		spinnerSensors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				// TODO Auto-generated method stub
				
				if(parent.getItemAtPosition(pos).toString().equals("A1")){
					if((Shimmer.SENSOR_INT_ADC_A1 & enableSensors) >0){
						mSensorToHeartRate = "Internal ADC A1";
  					}
					else{
						Toast.makeText(getActivity(), "WARNING: The sensor A1 is not enabled", Toast.LENGTH_SHORT).show();
						mSensorToHeartRate="";
					}
				}
				else if(parent.getItemAtPosition(pos).toString().equals("A12")){
					if((Shimmer.SENSOR_INT_ADC_A12 & enableSensors) >0){
						mSensorToHeartRate = "Internal ADC A12";
  					}
					else{
						Toast.makeText(getActivity(), "WARNING: The sensor A12 is not enabled", Toast.LENGTH_SHORT).show();
						mSensorToHeartRate="";
					}
					
				}
				else if(parent.getItemAtPosition(pos).toString().equals("A13")){
					if((Shimmer.SENSOR_INT_ADC_A13 & enableSensors) >0){
						mSensorToHeartRate = "Internal ADC A13";
  					}
					else{
						Toast.makeText(getActivity(), "WARNING: The sensor A13 is not enabled", Toast.LENGTH_SHORT).show();
						mSensorToHeartRate="";
					}
					
				}
				else if(parent.getItemAtPosition(pos).toString().equals("A14")){
					if((Shimmer.SENSOR_INT_ADC_A14 & enableSensors) >0){
						mSensorToHeartRate = "Internal ADC A14";
  					}
					else{
						Toast.makeText(getActivity(), "WARNING: The sensor A14 is not enabled", Toast.LENGTH_SHORT).show();
						mSensorToHeartRate="";
					}
				}
				else if(parent.getItemAtPosition(pos).toString().equals(Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT)){
					if(((Shimmer.SENSOR_EXG1_24BIT & enableSensors)>0 || (Shimmer.SENSOR_EXG1_16BIT & enableSensors)>0) && (mService.isEXGUsingECG24Configuration(mBluetoothAddress)||mService.isEXGUsingECG16Configuration(mBluetoothAddress))){
						mSensorToHeartRate = Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT;
  					}
					else{
						Toast.makeText(getActivity(), "WARNING: ECG is not enabled", Toast.LENGTH_SHORT).show();
						mSensorToHeartRate="";
					}
					
				}
				else if(parent.getItemAtPosition(pos).toString().equals(Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT)){
					if(((Shimmer.SENSOR_EXG1_24BIT & enableSensors)>0 || (Shimmer.SENSOR_EXG1_16BIT & enableSensors)>0) && (mService.isEXGUsingECG24Configuration(mBluetoothAddress)||mService.isEXGUsingECG16Configuration(mBluetoothAddress))){
						mSensorToHeartRate = Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT;
  					}
					else{
						Toast.makeText(getActivity(), "WARNING: ECG is not enabled", Toast.LENGTH_SHORT).show();
						mSensorToHeartRate="";
					}
					
				}
				else if(parent.getItemAtPosition(pos).toString().equals(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT)) {
					if(((Shimmer.SENSOR_EXG1_24BIT & enableSensors)>0 || (Shimmer.SENSOR_EXG1_16BIT & enableSensors)>0) && (mService.isEXGUsingECG24Configuration(mBluetoothAddress)||mService.isEXGUsingECG16Configuration(mBluetoothAddress))){
						mSensorToHeartRate = Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT;
					}
					else{
						Toast.makeText(getActivity(), "WARNING: ECG is not enabled", Toast.LENGTH_SHORT).show();
						mSensorToHeartRate="";
					}
				}
				else if(parent.getItemAtPosition(pos).toString().equals(Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT)){
					if(((Shimmer.SENSOR_EXG1_24BIT & enableSensors)>0 || (Shimmer.SENSOR_EXG1_16BIT & enableSensors)>0) && (mService.isEXGUsingECG24Configuration(mBluetoothAddress)||mService.isEXGUsingECG16Configuration(mBluetoothAddress))){
						mSensorToHeartRate = Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT;
  					}
					else{
						Toast.makeText(getActivity(), "WARNING: ECG is not enabled", Toast.LENGTH_SHORT).show();
						mSensorToHeartRate="";
					}
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
  		
//  		final AlertDialog.Builder newDialog = new AlertDialog.Builder(getActivity()).setTitle("Heart Rate");
//  		LinearLayout layout =
//  		newDialog.setView(view)
  		
  		editTexNumberOfBeats = (EditText) heartRateDialog.findViewById(R.id.editTextNumberOfBeats);
  		editTexNumberOfBeats.setText(""+mService.getNumberOfBeatsToAverage());
  		buttonCancelHearRate = (Button) heartRateDialog.findViewById(R.id.buttonCancelHearRate);
  		buttonCancelHearRate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mSensorToHeartRate="";
				cBoxHeartRate.setChecked(false);
				heartRateDialog.dismiss();
			}
		});
  		
  		buttonDoneHearRate = (Button) heartRateDialog.findViewById(R.id.buttonDoneHearRate);
  		buttonDoneHearRate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(editTexNumberOfBeats.getText().toString().equals("")){
					Toast.makeText(getActivity(), "The number of beats introduced is incorrect", Toast.LENGTH_SHORT).show();
				}
				else{
					int numberOfBeats = Integer.parseInt(editTexNumberOfBeats.getText().toString());
					mService.setNumberOfBeatsToAverage(numberOfBeats);
					if(mSensorToHeartRate.equals("")){
						cBoxHeartRate.setChecked(false);
						Toast.makeText(getActivity(), "The sensor selected is disabled. Please, enable the sensor in order to calculate the Heart Rate.", Toast.LENGTH_LONG).show();
//						heartRateDialog.dismiss();
					}
					else{
						if (mSensorToHeartRate.equals(Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT) ||
								mSensorToHeartRate.equals(Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT) || 
										mSensorToHeartRate.equals(Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT) ||
										mSensorToHeartRate.equals(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT)){
							if(mService.getSamplingRate(mBluetoothAddress)>128){
								Toast.makeText(getActivity(), "A lower sampling rate (e.g. 128 Hz) is recommended due to Android device processing limitations", Toast.LENGTH_LONG).show();	
							}
							mService.enableHeartRateECG(deviceBluetoothAddress, true, mSensorToHeartRate);
						} else {
							mService.enableHeartRate(deviceBluetoothAddress, true, mSensorToHeartRate);	
						}
						
						
						
						Toast.makeText(getActivity(), "Heart Rate enabled", Toast.LENGTH_SHORT).show();
//						heartRateDialog.dismiss();
					}
					heartRateDialog.dismiss();
				}
				
//				mService.enableHeartRate(deviceBluetoothAddress, true, mSensorToHeartRate);
//				heartRateDialog.dismiss();
//				cBoxHeartRate.setChecked(false);
			}
		});
  		String messageDialog = "The Heart Rate calculation is already enabled for other Shimmer.";// +
//  				" To calculate the Heart Rate in multiple Shimmers, please upgrade the App from our website "
//  				+" http://www.shimmersensing.com/shop/multi-shimmer-sync-for-android \n"
//  				+"You can also disable the other Shimmer and enable this one.";
  		final AlertDialog.Builder heartRateEnabledDialog = new AlertDialog.Builder(getActivity()).setTitle("Heart Rate").
  				setMessage(messageDialog).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						arg0.dismiss();
						modifyHRState = false;
						cBoxHeartRate.setChecked(false);
					}
				});
  		
  		cBoxHeartRate.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					if(mService.isHeartRateEnabled()){
						heartRateEnabledDialog.show();
					}
					else{
						heartRateDialog.show();
						
					}
				}
				else{
					if(modifyHRState)
						mService.enableHeartRate("", false, "");
				}
//				cBoxHeartRate.setChecked(false);
//				heartRateDialog.show();
			}
		});
  		
  		
		return rootView;
		
	}
	
	public void onPause(){
    	super.onPause();
    	if (mService!=null){
    		db.saveShimmerConfigurations("Temp", mService.mShimmerConfigurationList);
    	}

    }
	
	 public void setup(){
		db=mService.mDataBase;
	  	mService.enableGraphingHandler(false);
//	  	mService.setHandler(mHandler);
	  	mService.mShimmerConfigurationList = db.getShimmerConfigurations("Temp");
	 }
}
