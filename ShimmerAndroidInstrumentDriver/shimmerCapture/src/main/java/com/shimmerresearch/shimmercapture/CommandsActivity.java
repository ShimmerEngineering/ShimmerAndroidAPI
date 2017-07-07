package com.shimmerresearch.shimmercapture;



import java.util.ArrayList;
import java.util.List;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.shimmerService.ShimmerService;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;

import com.shimmerresearch.sensors.SensorGSR;
import com.shimmerresearch.sensors.lsm303.SensorLSM303;
import com.shimmerresearch.sensors.lsm303.SensorLSM303DLHC;
import com.shimmerresearch.sensors.mpu9x50.SensorMPU9X50;
import com.shimmersensing.shimmerconnect.R;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;


public class CommandsActivity extends ServiceActivity {
	public static String mDone = "Done";
	String mBluetoothAddress;
	CheckBox cBoxLowPowerMag;
	CheckBox cBoxLowPowerAccel;
	CheckBox cBoxLowPowerGyro;
	CheckBox cBox5VReg;
	CheckBox cBoxInternalExpPower;
	CheckBox cBoxPPGtoHR;
	CheckBox cBoxECGtoHR;
	Button buttonGyroRange;
	Button buttonMagRange;
	Button buttonGsr;
	Button buttonPressureResolution;
	Button buttonSampleRate;
	Button buttonAccRange;
	Button buttonBattVoltLimit;
	Button buttonToggleLED;
	Button buttonDone;
	public boolean isLedChecked;
	AlertDialog.Builder dialogPPGtoHR;
	AlertDialog.Builder dialogECGtoHR;
	final String[] ListofPressureResolution={"Low","Standard","High","Very High"};
	final Integer[] ListofPressureResolutionConfigValues={0,1,2,3};
    

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	setContentView(R.layout.shimmer_commands);
        
    	Bundle extras = getIntent().getExtras();
        mBluetoothAddress = extras.getString("BluetoothAddress");
    	double mSamplingRateV = extras.getDouble("SamplingRate");
    	int mAccelerometerRangeV = extras.getInt("AccelerometerRange");
    	int mGSRRangeV = extras.getInt("GSRRange");
    	final double batteryLimit = extras.getDouble("BatteryLimit");
    	final String[] samplingRate = new String [] {"8","16","51.2","102.4","128","204.8","256","512","1024","2048"};
        buttonPressureResolution = (Button) findViewById(R.id.buttonPressureAccuracy);
        // Set an EditText view to get user input 
        final EditText editTextBattLimit = new EditText(getApplicationContext());
        editTextBattLimit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editTextBattLimit.setTextColor(getResources().getColor(android.R.color.black));
        buttonGyroRange = (Button)  findViewById(R.id.buttonGyroRange);
        buttonMagRange = (Button)  findViewById(R.id.buttonMagRange);
        buttonGsr = (Button) findViewById(R.id.buttonGSR);
        buttonSampleRate = (Button) findViewById(R.id.buttonRate);
        buttonAccRange = (Button) findViewById(R.id.buttonAccel);
        buttonToggleLED = (Button) findViewById(R.id.buttonToggleLED);
        buttonBattVoltLimit = (Button) findViewById(R.id.buttonBattLimit);
        buttonDone = (Button) findViewById(R.id.buttonDone);
        
        final String[] accelRangeArray = {"+/- 1.5g","+/- 6g"};
        
        String rate = Double.toString(mSamplingRateV);
        buttonSampleRate.setText("SAMPLING RATE "+"\n"+"("+rate+" HZ)");
        
        if (mAccelerometerRangeV==0){
        	if (mService.getShimmerVersion(mBluetoothAddress)!=ShimmerVerDetails.HW_ID.SHIMMER_3){
        		buttonAccRange.setText("ACCEL RANGE"+"\n"+"(+/- 1.5g)");
        	} else {
        		buttonAccRange.setText("WR ACCEL RANGE"+"\n"+"(+/- 2g)");
        	}
        } else if (mAccelerometerRangeV==1){
        	buttonAccRange.setText("WR ACCEL RANGE"+"\n"+"(+/- 4g)");
        } else if (mAccelerometerRangeV==2){
        	buttonAccRange.setText("WR ACCEL RANGE"+"\n"+"(+/- 8g)");
        }
        else if (mAccelerometerRangeV==3){
        	if (mService.getShimmerVersion(mBluetoothAddress)!=ShimmerVerDetails.HW_ID.SHIMMER_3){
        		buttonAccRange.setText("ACCEL RANGE"+"\n"+"(+/- 6g)");
        	} else {
        		buttonAccRange.setText("WR ACCEL RANGE"+"\n"+"(+/- 16g)");
        	}
        } 
        
        buttonGsr.setText("GSR Range"+"\n"+ SensorGSR.ListofGSRRangeResistance[mGSRRangeV]);

        buttonToggleLED.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ServiceActivity.mService.toggleLED(mBluetoothAddress);
				Toast.makeText(getApplicationContext(), "Toggle LED", Toast.LENGTH_SHORT).show();
			}
		});
    	
    	
        final AlertDialog.Builder dialogBattLimit = new AlertDialog.Builder(this);
        dialogBattLimit.setTitle("Battery Limit").setMessage("Set battery limit")
        				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								double newLimit = Double.parseDouble(editTextBattLimit.getText().toString());
					            ServiceActivity.mService.setBattLimitWarning(mBluetoothAddress, newLimit);
					            Toast.makeText(getApplicationContext(), "Battery limit changed. New limit = "+newLimit+" V", Toast.LENGTH_SHORT).show();
					            buttonBattVoltLimit.setText("SET BATT LIMIT "+"\n"+"("+newLimit+" V)");
							}
        				});
        
        
        buttonBattVoltLimit.setText("SET BATT LIMIT "+"\n"+"("+Double.toString(batteryLimit)+" V)");

        
    buttonBattVoltLimit.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				// This is done in order to avoid an error when the dialog is displayed again after being cancelled
				if(editTextBattLimit.getParent()!=null){
					ViewGroup parentViewGroup = (ViewGroup)editTextBattLimit.getParent();
					parentViewGroup.removeView(editTextBattLimit);
					editTextBattLimit.setPadding(0, 5, 0, 7);
				}
				
				dialogBattLimit.setView(editTextBattLimit);
				dialogBattLimit.show();			
			}
    		
    	});	
    	
    	
    	
    	final AlertDialog.Builder dialogRate = new AlertDialog.Builder(this);		 
        dialogRate.setTitle("Sampling Rate").setItems(samplingRate, new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int item) {
        		Log.d("Shimmer",samplingRate[item]);
        		double newRate = Double.valueOf(samplingRate[item]);
        		if (cBoxECGtoHR.isChecked() && newRate<128){
        			Toast.makeText(getApplicationContext(), "Please use a sampling rate of 128Hz or higher when using the ECG to HR algorithm", Toast.LENGTH_LONG).show();
					
        		} else {
        			ServiceActivity.mService.writeSamplingRate(mBluetoothAddress, newRate);
        			Toast.makeText(getApplicationContext(), "Sample rate changed. New rate = "+newRate+" Hz", Toast.LENGTH_SHORT).show();
        			buttonSampleRate.setText("SAMPLING RATE "+"\n"+"("+newRate+" HZ)");
        		}

        	}
        });
    	
    	buttonSampleRate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub		        
		        dialogRate.show();
			}
		});
    	
    	
    	final AlertDialog.Builder dialogAccelShimmer2 = new AlertDialog.Builder(this);		 
    	dialogAccelShimmer2.setTitle("Accelerometer Range").setItems(accelRangeArray, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 	Log.d("Shimmer",accelRangeArray[item]);
	           		    	int accelRange=0;

	         	  		    if (accelRangeArray[item]=="+/- 1.5g"){
	         	  		    	accelRange=0;
	         	  		    } else if (accelRangeArray[item]=="+/- 6g"){
	         	  		    	accelRange=3;
	         	  		    }
	           		   

	         	  		    ServiceActivity.mService.writeAccelRange(mBluetoothAddress, accelRange);
	         	  		    Toast.makeText(getApplicationContext(), "Accelerometer rate changed. New rate = "+accelRangeArray[item], Toast.LENGTH_SHORT).show();
	         	  		    buttonAccRange.setText("ACCEL RANGE"+"\n"+"("+accelRangeArray[item]+")");
                }
        });
        
    	
    	final AlertDialog.Builder dialogAccelShimmer3 = new AlertDialog.Builder(this);		 
    	dialogAccelShimmer3.setTitle("Accelerometer Range").setItems(SensorLSM303DLHC.ListofLSM303AccelRange, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 Log.d("Shimmer",SensorLSM303.ListofLSM303AccelRange[item]);
	           		    int accelRange=0;
	           		    
	           		    	if (SensorLSM303.ListofLSM303AccelRange[item]=="+/- 2g"){
	         	  		    	accelRange=0;
	         	  		    } else if (SensorLSM303.ListofLSM303AccelRange[item]=="+/- 4g"){
	         	  		    	accelRange=1;
	         	  		    } else if (SensorLSM303.ListofLSM303AccelRange[item]=="+/- 8g"){
	         	  		    	accelRange=2;
	         	  		    } else if (SensorLSM303.ListofLSM303AccelRange[item]=="+/- 16g"){
	         	  		    	accelRange=3;
	         	  		    }

	           		    if(accelRange==0){
	           		    	cBoxLowPowerAccel.setEnabled(false);
	           		    	cBoxLowPowerAccel.setTextColor(getResources().getColor(R.color.shimmer_grey_checkbox));
	           		    }
	           		    else{
	           		    	cBoxLowPowerAccel.setEnabled(true);
	           		    	cBoxLowPowerAccel.setTextColor(getResources().getColor(R.color.black));
	           		    }
	           		    
	           		    ServiceActivity.mService.writeAccelRange(mBluetoothAddress, accelRange);
	         	  		Toast.makeText(getApplicationContext(), "Accelerometer rate changed. New rate = "+SensorLSM303.ListofLSM303AccelRange[item], Toast.LENGTH_SHORT).show();
	         	  		buttonAccRange.setText("WR ACCEL RANGE"+"\n"+"("+SensorLSM303.ListofLSM303AccelRange[item]+")");
	           		    
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
        
        
        final AlertDialog.Builder dialogGyroRangeShimmer3 = new AlertDialog.Builder(this);		 
        dialogGyroRangeShimmer3.setTitle("Gyroscope Range").setItems(SensorMPU9X50.ListofGyroRange, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 Log.d("Shimmer", SensorMPU9X50.ListofGyroRange[item]);
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

               		    ServiceActivity.mService.writeGyroRange(mBluetoothAddress, gyroRange);
               		    Toast.makeText(getApplicationContext(), "Gyroscope rate changed. New rate = "+SensorMPU9X50.ListofGyroRange[item], Toast.LENGTH_SHORT).show();
               		    buttonGyroRange.setText("GYRO RANGE"+"\n"+"("+SensorMPU9X50.ListofGyroRange[item]+")");
                }
        });
        
        

        
        buttonGyroRange.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialogGyroRangeShimmer3.show();
			}
		});

        
        
        final AlertDialog.Builder dialogMagRangeShimmer2 = new AlertDialog.Builder(this);		 
        dialogMagRangeShimmer2.setTitle("Magnetometer Range").setItems(Configuration.Shimmer2.ListofMagRange, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 Log.d("Shimmer",Configuration.Shimmer2.ListofMagRange[item]);
                		 int magRange=0;
             		  
             		     
             		    	if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 0.8Ga"){
             		    		magRange=0;
             		    	} else if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 1.3Ga"){
             		    		magRange=1;
             		    	} else if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 1.9Ga"){
             		    		magRange=2;
             		    	} else if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 2.5Ga"){
             		    		magRange=3;
             		    	} else if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 4.0Ga"){
             		    		magRange=4;
             		    	} else if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 4.7Ga"){
             		    		magRange=5;
             		    	} else if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 5.6Ga"){
             		    		magRange=6;
             		    	} else if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 8.1Ga"){
             		    		magRange=7;

             		     }
 
             		    ServiceActivity.mService.writeMagRange(mBluetoothAddress, magRange);
             		    Toast.makeText(getApplicationContext(), "Magnometer rate changed. New rate = "+Configuration.Shimmer2.ListofMagRange[item], Toast.LENGTH_SHORT).show();
             		   buttonMagRange.setText("MAG RANGE"+"\n"+"("+Configuration.Shimmer2.ListofMagRange[item]+")");
           	      }
        });
        
        final AlertDialog.Builder dialogMagRangeShimmer3 = new AlertDialog.Builder(this);		 
        dialogMagRangeShimmer3.setTitle("Magnetometer Range").setItems(SensorLSM303DLHC.ListofLSM303DLHCMagRange, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 Log.d("Shimmer",SensorLSM303DLHC.ListofLSM303DLHCMagRange[item]);
                		 int magRange=0;
             		  
             		    	if (SensorLSM303DLHC.ListofLSM303DLHCMagRange[item]==SensorLSM303DLHC.ListofLSM303DLHCMagRange[0]){
             		    		magRange=1;
             		    	} else if (SensorLSM303DLHC.ListofLSM303DLHCMagRange[item]==SensorLSM303DLHC.ListofLSM303DLHCMagRange[1]){
             		    		magRange=2;
             		    	} else if (SensorLSM303DLHC.ListofLSM303DLHCMagRange[item]==SensorLSM303DLHC.ListofLSM303DLHCMagRange[2]){
             		    		magRange=3;
             		    	} else if (SensorLSM303DLHC.ListofLSM303DLHCMagRange[item]==SensorLSM303DLHC.ListofLSM303DLHCMagRange[3]){
             		    		magRange=4;
             		    	} else if (SensorLSM303DLHC.ListofLSM303DLHCMagRange[item]==SensorLSM303DLHC.ListofLSM303DLHCMagRange[4]){
             		    		magRange=5;
             		    	} else if (SensorLSM303DLHC.ListofLSM303DLHCMagRange[item]==SensorLSM303DLHC.ListofLSM303DLHCMagRange[5]){
             		    		magRange=6;
             		    	} else if (SensorLSM303DLHC.ListofLSM303DLHCMagRange[item]==SensorLSM303DLHC.ListofLSM303DLHCMagRange[6]){
             		    		magRange=7;
             		    	}

             		    	ServiceActivity.mService.writeMagRange(mBluetoothAddress, magRange);
                 		    Toast.makeText(getApplicationContext(), "Magnometer rate changed. New rate = "+SensorLSM303DLHC.ListofLSM303DLHCMagRange[item], Toast.LENGTH_SHORT).show();
                 		    buttonMagRange.setText("MAG RANGE"+"\n"+"("+SensorLSM303DLHC.ListofLSM303DLHCMagRange[item]+")");
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

        dialogPPGtoHR = new AlertDialog.Builder(this);
        long enabledSensors = mService.getShimmer(mBluetoothAddress).getEnabledSensors();
        List<String> list = new ArrayList<String>();
        if ((enabledSensors & ShimmerObject.SENSOR_INT_ADC_A13) > 0){
			list.add(Configuration.Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A13);
		}
		if ((enabledSensors & ShimmerObject.SENSOR_INT_ADC_A1) > 0){
			list.add(Configuration.Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A1);
		}
		if ((enabledSensors & ShimmerObject.SENSOR_INT_ADC_A12) > 0){
			list.add(Configuration.Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A12);
		}
		if ((enabledSensors & ShimmerObject.SENSOR_INT_ADC_A14) > 0){
			list.add(Configuration.Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A14);
		} 
		//No way it will reach here without auto A13 enabling
		if (list.size()==0){
			list.add(Configuration.Shimmer3.ObjectClusterSensorName.INT_EXP_ADC_A13);
		}
        String[] arrayPPG = new String[list.size()];
        arrayPPG = list.toArray(arrayPPG);
        dialogPPGtoHR.setCancelable(false);
        dialogPPGtoHR.setTitle("PPG to HR : Select PPG signal source").setItems(arrayPPG, new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int item) {

        		ListView lw = ((AlertDialog)dialog).getListView();	
        		Object checkedItem = lw.getAdapter().getItem(item);
        		mService.setPPGtoHRSignal((String)checkedItem);

        	}
        });

        dialogECGtoHR = new AlertDialog.Builder(this);
        dialogECGtoHR.setCancelable(false);
        final AlertDialog.Builder dialogPressureResolutionShimmer3 = new AlertDialog.Builder(this);
        //dialogPressureResolutionShimmer3.setTitle("Pressure Resolution").setItems(Configuration.Shimmer3.ListofPressureResolution, new DialogInterface.OnClickListener() {
		dialogPressureResolutionShimmer3.setTitle("Pressure Resolution").setItems(ListofPressureResolution, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 int pressureRes=0;
               		  /*
               		    if (Configuration.Shimmer3.ListofPressureResolution[item]=="Low"){
             	  		    	pressureRes=0;
             	  		    } else if (Configuration.Shimmer3.ListofPressureResolution[item]=="Standard"){
             	  		    	pressureRes=1;
             	  		    } else if (Configuration.Shimmer3.ListofPressureResolution[item]=="High"){
             	  		    	pressureRes=2;
             	  		    } else if (Configuration.Shimmer3.ListofPressureResolution[item]=="Very High"){
             	  		    	pressureRes=3;
             	  		    } 
*/
                		 if (ListofPressureResolution[item]=="Low"){
          	  		    	pressureRes=0;
          	  		    } else if (ListofPressureResolution[item]=="Standard"){
          	  		    	pressureRes=1;
          	  		    } else if (ListofPressureResolution[item]=="High"){
          	  		    	pressureRes=2;
          	  		    } else if (ListofPressureResolution[item]=="Very High"){
          	  		    	pressureRes=3;
          	  		    } 
               		    ServiceActivity.mService.writePressureResolution(mBluetoothAddress, pressureRes);
               		    Toast.makeText(getApplicationContext(), "Pressure resolution changed. New resolution = "+ListofPressureResolution[item], Toast.LENGTH_SHORT).show();
               		    buttonPressureResolution.setText("PRESSURE RES"+"\n"+"("+ListofPressureResolution[item]+")");
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
        final AlertDialog.Builder dialogGsrRange = new AlertDialog.Builder(this);		 
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

             		    ServiceActivity.mService.writeGSRRange(mBluetoothAddress, gsrRange);
             		    Toast.makeText(getApplicationContext(), "Gsr range changed. New range = "+SensorGSR.ListofGSRRangeResistance[item], Toast.LENGTH_SHORT).show();
             		    buttonGsr.setText("GSR RANGE"+"\n"+"("+SensorGSR.ListofGSRRangeResistance[item]+")");
             		   
           	      }
        });
        
        buttonGsr.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialogGsrRange.show();
			}
		});
        
        buttonDone.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
        
	}
    protected ServiceConnection mTestServiceConnection = new ServiceConnection() {

      	public void onServiceConnected(ComponentName arg0, IBinder service) {
      		// TODO Auto-generated method stub
      		Log.d("ShimmerService", "service connected");
      		ShimmerService.LocalBinder binder = (ShimmerService.LocalBinder) service;
      		mService = binder.getService();
      		cBox5VReg = (CheckBox) findViewById(R.id.checkBox5VReg);
    		cBoxLowPowerMag = (CheckBox) findViewById(R.id.checkBoxLowPowerMag);
    		cBoxLowPowerAccel = (CheckBox) findViewById(R.id.checkBoxLowPowerAccel);
    		cBoxLowPowerGyro = (CheckBox) findViewById(R.id.checkBoxLowPowerGyro);
    		cBoxInternalExpPower = (CheckBox) findViewById(R.id.CheckBoxIntExpPow);
    		cBoxPPGtoHR = (CheckBox) findViewById(R.id.CheckBoxPPGtoHR);
    		cBoxECGtoHR = (CheckBox) findViewById(R.id.CheckBoxECGtoHR);
    		cBox5VReg.setTextColor(getResources().getColor(R.color.black));
    		cBoxLowPowerMag.setTextColor(getResources().getColor(R.color.black));
    		cBoxLowPowerAccel.setTextColor(getResources().getColor(R.color.black));
    		cBoxLowPowerGyro.setTextColor(getResources().getColor(R.color.black));
    		cBoxInternalExpPower.setTextColor(getResources().getColor(R.color.black));
    		
      		final ShimmerDevice shimmerDevice = mService.getShimmer(mBluetoothAddress);

			if(shimmerDevice instanceof Shimmer) {
				final Shimmer shimmer = (Shimmer) shimmerDevice;
				if (shimmer.getInternalExpPower() == 1) {
					cBoxInternalExpPower.setChecked(true);
				} else {
					cBoxInternalExpPower.setChecked(false);
				}

				if (mService.isPPGtoHREnabled()) {
					cBoxPPGtoHR.setChecked(true);
				} else {
					cBoxPPGtoHR.setChecked(false);
				}

				if (mService.isECGtoHREnabled()) {
					cBoxECGtoHR.setChecked(true);
				} else {
					cBoxECGtoHR.setChecked(false);
				}


				if (shimmer.isLowPowerMagEnabled()) {
					cBoxLowPowerMag.setChecked(true);
				}

				if (shimmer.isLowPowerAccelEnabled()) {
					cBoxLowPowerAccel.setChecked(true);
				}

				if (shimmer.isLowPowerGyroEnabled()) {
					cBoxLowPowerGyro.setChecked(true);
				}


				cBox5VReg.setChecked(false);
				if (mService.getShimmerVersion(mBluetoothAddress) == ShimmerVerDetails.HW_ID.SHIMMER_3) {
					buttonGsr.setVisibility(View.VISIBLE);
					cBox5VReg.setEnabled(false);
					cBox5VReg.setTextColor(getResources().getColor(R.color.shimmer_grey_checkbox));
					String currentGyroRange = "(" + SensorMPU9X50.ListofGyroRange[shimmer.getGyroRange()] + ")";
					buttonGyroRange.setText("GYRO RANGE" + "\n" + currentGyroRange);
					String currentMagRange = "(" + SensorLSM303DLHC.ListofLSM303DLHCMagRange[shimmer.getMagRange() - 1] + ")";
					buttonMagRange.setText("MAG RANGE" + "\n" + currentMagRange);
					String currentPressureResolution = "(" + ListofPressureResolution[shimmer.getPressureResolution()] + ")";
					buttonPressureResolution.setText("PRESSURE RES" + "\n" + currentPressureResolution);

					if (shimmer.getAccelRange() == 0) {
						cBoxLowPowerAccel.setEnabled(false);
						cBoxLowPowerAccel.setTextColor(getResources().getColor(R.color.shimmer_grey_checkbox));
					}

					//currently not supported for the moment
					buttonPressureResolution.setEnabled(true);


				} else {
					cBoxInternalExpPower.setEnabled(false);
					cBoxInternalExpPower.setTextColor(getResources().getColor(R.color.shimmer_grey_checkbox));
					buttonPressureResolution.setEnabled(false);
					buttonGyroRange.setEnabled(false);
					cBoxLowPowerAccel.setEnabled(false);
					cBoxLowPowerAccel.setTextColor(getResources().getColor(R.color.shimmer_grey_checkbox));
					cBoxLowPowerGyro.setEnabled(false);
					cBoxLowPowerGyro.setTextColor(getResources().getColor(R.color.shimmer_grey_checkbox));
					String currentMagRange = "(" + Configuration.Shimmer2.ListofMagRange[shimmer.getMagRange()] + ")";
					buttonMagRange.setText("Mag Range" + "\n" + currentMagRange);
				}


				mServiceBind = true;
				//update the view

				if (mService.get5VReg(mBluetoothAddress) == 1) {
					cBox5VReg.setChecked(true);
				}

				cBox5VReg.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton arg0, boolean checked) {
						// TODO Auto-generated method stub
						if (checked) {
							mService.write5VReg(mBluetoothAddress, 1);
						} else {
							mService.write5VReg(mBluetoothAddress, 0);
						}
					}

				});


				cBoxLowPowerAccel.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton arg0, boolean checked) {
						// TODO Auto-generated method stub
						if (checked) {
							shimmer.enableLowPowerAccel(true);
						} else {
							shimmer.enableLowPowerAccel(false);
						}
					}

				});

				cBoxLowPowerGyro.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton arg0, boolean checked) {
						// TODO Auto-generated method stub
						if (checked) {
							shimmer.enableLowPowerGyro(true);
						} else {
							shimmer.enableLowPowerGyro(false);
						}
					}

				});

				cBoxInternalExpPower.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton arg0, boolean checked) {
						// TODO Auto-generated method stub
						if (checked) {
							shimmer.writeInternalExpPower(1);
						} else {
							shimmer.writeInternalExpPower(0);
						}
					}

				});


				cBoxPPGtoHR.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton arg0, boolean checked) {
						// TODO Auto-generated method stub
						mService.enablePPGtoHR(mBluetoothAddress, checked);
						if (checked) {
							if (cBoxECGtoHR.isChecked()) {
								cBoxECGtoHR.setChecked(false);
							}
							if (cBoxInternalExpPower.isChecked()) {
							} else {
								Toast.makeText(getApplicationContext(), "Enabling Int Exp Power", Toast.LENGTH_LONG).show();
								cBoxInternalExpPower.setChecked(true);
							}
							long enabledSensors = mService.getEnabledSensors(mBluetoothAddress);
							if ((enabledSensors & ShimmerObject.SENSOR_INT_ADC_A13) > 0) {
								Toast.makeText(getApplicationContext(), "Int ADC A13 is Enabled", Toast.LENGTH_LONG).show();
							}
							if ((enabledSensors & ShimmerObject.SENSOR_INT_ADC_A1) > 0) {
								Toast.makeText(getApplicationContext(), "Int ADC A1 is Enabled", Toast.LENGTH_LONG).show();
							}
							if ((enabledSensors & ShimmerObject.SENSOR_INT_ADC_A12) > 0) {
								Toast.makeText(getApplicationContext(), "Int ADC A12 is Enabled", Toast.LENGTH_LONG).show();
							}
							if ((enabledSensors & ShimmerObject.SENSOR_INT_ADC_A14) > 0) {
								Toast.makeText(getApplicationContext(), "Int ADC A14 is Enabled", Toast.LENGTH_LONG).show();
							}
							if (((enabledSensors & ShimmerObject.SENSOR_INT_ADC_A13) == 0) &&
									((enabledSensors & ShimmerObject.SENSOR_INT_ADC_A1) == 0) &&
									((enabledSensors & ShimmerObject.SENSOR_INT_ADC_A12) == 0) &&
									((enabledSensors & ShimmerObject.SENSOR_INT_ADC_A14) == 0)) {

								Toast.makeText(getApplicationContext(), "Enabling Int ADC A13, please attach PPG sensor to Int ADC A13", Toast.LENGTH_LONG).show();
								long newES = mService.sensorConflictCheckandCorrection(mBluetoothAddress, enabledSensors, ShimmerObject.SENSOR_INT_ADC_A13);
								mService.setEnabledSensors(newES, mBluetoothAddress);
							}
							dialogPPGtoHR.show();
						}
					}

				});

				cBoxECGtoHR.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton arg0, boolean checked) {
						// TODO Auto-generated method stub

						if (checked) {
							if (mService.getSamplingRate(mBluetoothAddress) >= 128) {
								long enabledSensors = mService.getEnabledSensors(mBluetoothAddress);
								if (mService.isEXGUsingECG24Configuration(mBluetoothAddress) || mService.isEXGUsingECG16Configuration(mBluetoothAddress)) {
									mService.enableECGtoHR(mBluetoothAddress, checked);
									if (cBoxPPGtoHR.isChecked()) {
										cBoxPPGtoHR.setChecked(false);
									}
									final String[] arrayECG = {Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT, Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT, Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT};
									final String[] arrayECG16 = {Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT, Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT, Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT};
									if (mService.isEXGUsingECG16Configuration(mBluetoothAddress)) {
										dialogECGtoHR.setTitle("ECG to HR : Select ECG signal source").setItems(arrayECG16, new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int item) {

												mService.setECGtoHRSignal(arrayECG16[item]);

											}
										});
										dialogECGtoHR.show();

									} else {

										dialogECGtoHR.setTitle("ECG to HR : Select ECG signal source").setItems(arrayECG, new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int item) {

												mService.setECGtoHRSignal(arrayECG[item]);

											}
										});
										dialogECGtoHR.show();

									}

								} else {
									Toast.makeText(getApplicationContext(), "Please enable ECG", Toast.LENGTH_LONG).show();
									cBoxECGtoHR.setChecked(false);

								}

							} else {
								Toast.makeText(getApplicationContext(), "Please use a sampling rate of 128Hz or higher", Toast.LENGTH_LONG).show();
								cBoxECGtoHR.setChecked(false);
							}
						} else {
							mService.enableECGtoHR(mBluetoothAddress, checked);
						}
					}

				});

				cBoxLowPowerMag.setChecked(mService.isLowPowerMagEnabled(mBluetoothAddress));

				cBoxLowPowerMag.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton arg0, boolean checked) {
						// TODO Auto-generated method stub
						if (checked) {
							mService.enableLowPowerMag(mBluetoothAddress, true);
						} else {
							mService.enableLowPowerMag(mBluetoothAddress, false);
						}
					}

				});
			}
      		}

      	public void onServiceDisconnected(ComponentName arg0) {
      		// TODO Auto-generated method stub
      		mServiceBind = false;
      	}
    };
	
	
	public void onPause() {
		super.onPause();
		Log.d("Shimmer","On Pause");
		if(mServiceBind == true){
  		  getApplicationContext().unbindService(mTestServiceConnection);
  	  }
	}

	public void onResume() {
		super.onResume();
		Log.d("Shimmer","On Resume");
		Intent intent=new Intent(this, ShimmerService.class);
  	  	Log.d("ShimmerH","on Resume");
  	  	getApplicationContext().bindService(intent,mTestServiceConnection, Context.BIND_AUTO_CREATE);
	}
	
	public String[] concat(String[] a, String[] b) {
		   int aLen = a.length;
		   int bLen = b.length;
		   String[] c= new String[aLen+bLen];
		   System.arraycopy(a, 0, c, 0, aLen);
		   System.arraycopy(b, 0, c, aLen, bLen);
		   return c;
		}
	
}
