
package com.shimmerresearch.shimmergraphandlogservice;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.shimmergraphandlogservice.R;
import com.shimmerresearch.service.ShimmerService;
import com.shimmerresearch.service.ShimmerService.LocalBinder;

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
	final String[] ListofPressureResolution={"Low","Standard","High","Very High"};

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
        buttonGyroRange = (Button)  findViewById(R.id.buttonGyroRange);
        buttonMagRange = (Button)  findViewById(R.id.buttonMagRange);
        buttonGsr = (Button) findViewById(R.id.buttonGSR);
        buttonSampleRate = (Button) findViewById(R.id.buttonRate);
        buttonAccRange = (Button) findViewById(R.id.buttonAccel);
        buttonToggleLED = (Button) findViewById(R.id.buttonToggleLED);
        buttonBattVoltLimit = (Button) findViewById(R.id.buttonBattLimit);
        buttonDone = (Button) findViewById(R.id.buttonDone);
        
        final String[] accelRangeArray = {"+/- 1.5g","+/- 6g"};
        
        String rate = "("+Double.toString(mSamplingRateV)+")";
        buttonSampleRate.setText("Sampling Rate "+"\n"+rate+" Hz");
        
        if (mAccelerometerRangeV==0){
        	if (mService.getShimmerVersion(mBluetoothAddress)!=ShimmerVerDetails.HW_ID.SHIMMER_3){
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
        	if (mService.getShimmerVersion(mBluetoothAddress)!=ShimmerVerDetails.HW_ID.SHIMMER_3){
        		buttonAccRange.setText("Accel Range"+"\n"+"(+/- 6g)");
        	} else {
        		buttonAccRange.setText("Accel Range"+"\n"+"(+/- 16g)");
        	}
        } 
        
        if (mGSRRangeV==0) {
        	buttonGsr.setText("GSR Range"+"\n"+"(10kOhm to 56kOhm)");
        } else if (mGSRRangeV==1) {
        	buttonGsr.setText("GSR Range"+"\n"+"(56kOhm to 220kOhm)");
        } else if (mGSRRangeV==2) {
        	buttonGsr.setText("GSR Range"+"\n"+"(220kOhm to 680kOhm)");
        } else if (mGSRRangeV==3) {
        	buttonGsr.setText("GSR Range"+"\n"+"(680kOhm to 4.7MOhm)");
        } else if (mGSRRangeV==4) {
        	buttonGsr.setText("GSR Range"+"\n"+"(Auto Range)");
        }
                  
        
        buttonToggleLED.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub    				
				ServiceActivity.mService.toggleLED(mBluetoothAddress);
				Toast.makeText(getApplicationContext(), "Toggle LED", Toast.LENGTH_SHORT).show();
			}
		});
    	
    	
        final AlertDialog.Builder dialogBattLimit = new AlertDialog.Builder(this);
        dialogBattLimit.setTitle("Battery Limit").setMessage("Introduce the battery limit to be set")
        				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								double newLimit = Double.parseDouble(editTextBattLimit.getText().toString());
					            ServiceActivity.mService.setBattLimitWarning(mBluetoothAddress, newLimit);
					            Toast.makeText(getApplicationContext(), "Battery limit changed. New limit = "+newLimit+" V", Toast.LENGTH_SHORT).show();
					            buttonBattVoltLimit.setText("Set Batt Limit "+"\n"+"("+newLimit+" V)");
							}
        				});
        
        
        buttonBattVoltLimit.setText("Set Batt Limit "+"\n"+"("+Double.toString(batteryLimit)+" V)");




        
    buttonBattVoltLimit.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
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
    	
    	
    	
    	final AlertDialog.Builder dialogRate = new AlertDialog.Builder(this);		 
        dialogRate.setTitle("Sample Rate").setItems(samplingRate, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 Log.d("Shimmer",samplingRate[item]);
                		 double newRate = Double.valueOf(samplingRate[item]);
                		 ServiceActivity.mService.writeSamplingRate(mBluetoothAddress, newRate);
                		 Toast.makeText(getApplicationContext(), "Sample rate changed. New rate = "+newRate+" Hz", Toast.LENGTH_SHORT).show();
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
    	
    	
    	final AlertDialog.Builder dialogAccelShimmer2 = new AlertDialog.Builder(this);		 
    	dialogAccelShimmer2.setTitle("Accelerometer range").setItems(accelRangeArray, new DialogInterface.OnClickListener() {
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
	         	  		    buttonAccRange.setText("Accel Range"+"\n"+"("+accelRangeArray[item]+")");
                }
        });
        
    	
    	final AlertDialog.Builder dialogAccelShimmer3 = new AlertDialog.Builder(this);		 
    	dialogAccelShimmer3.setTitle("Accelerometer range").setItems(Configuration.Shimmer3.ListofAccelRange, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 Log.d("Shimmer",Configuration.Shimmer3.ListofAccelRange[item]);
	           		    int accelRange=0;
	           		    
	           		    	if (Configuration.Shimmer3.ListofAccelRange[item]=="+/- 2g"){
	         	  		    	accelRange=0;
	         	  		    } else if (Configuration.Shimmer3.ListofAccelRange[item]=="+/- 4g"){
	         	  		    	accelRange=1;
	         	  		    } else if (Configuration.Shimmer3.ListofAccelRange[item]=="+/- 8g"){
	         	  		    	accelRange=2;
	         	  		    } else if (Configuration.Shimmer3.ListofAccelRange[item]=="+/- 16g"){
	         	  		    	accelRange=3;
	         	  		    }

	           		    if(accelRange==0)
	           		    	cBoxLowPowerAccel.setEnabled(false);
	           		    else
	           		    	cBoxLowPowerAccel.setEnabled(true);
	           		    
	           		    ServiceActivity.mService.writeAccelRange(mBluetoothAddress, accelRange);
	         	  		Toast.makeText(getApplicationContext(), "Accelerometer rate changed. New rate = "+Configuration.Shimmer3.ListofAccelRange[item], Toast.LENGTH_SHORT).show();
	         	  		buttonAccRange.setText("Accel Range"+"\n"+"("+Configuration.Shimmer3.ListofAccelRange[item]+")");	
	           		    
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
        dialogGyroRangeShimmer3.setTitle("Gyroscope Range").setItems(Configuration.Shimmer3.ListofGyroRange, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 Log.d("Shimmer",Configuration.Shimmer3.ListofGyroRange[item]);
             		    int gyroRange=0;
               		  
               		    if (Configuration.Shimmer3.ListofGyroRange[item]=="250dps"){
             	  		    	gyroRange=0;
             	  		    } else if (Configuration.Shimmer3.ListofGyroRange[item]=="500dps"){
             	  		    	gyroRange=1;
             	  		    } else if (Configuration.Shimmer3.ListofGyroRange[item]=="1000dps"){
             	  		    	gyroRange=2;
             	  		    } else if (Configuration.Shimmer3.ListofGyroRange[item]=="2000dps"){
             	  		    	gyroRange=3;
             	  		    }

               		    ServiceActivity.mService.writeGyroRange(mBluetoothAddress, gyroRange);
               		    Toast.makeText(getApplicationContext(), "Gyroscope rate changed. New rate = "+Configuration.Shimmer3.ListofGyroRange[item], Toast.LENGTH_SHORT).show();
               		    buttonGyroRange.setText("Gyro Range"+"\n"+"("+Configuration.Shimmer3.ListofGyroRange[item]+")");
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
             		   buttonMagRange.setText("Mag Range"+"\n"+"("+Configuration.Shimmer2.ListofMagRange[item]+")");
           	      }
        });
        
        final AlertDialog.Builder dialogMagRangeShimmer3 = new AlertDialog.Builder(this);		 
        dialogMagRangeShimmer3.setTitle("Magnetometer Range").setItems(Configuration.Shimmer3.ListofMagRange, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 Log.d("Shimmer",Configuration.Shimmer3.ListofMagRange[item]);
                		 int magRange=0;
             		  
             		    	if (Configuration.Shimmer3.ListofMagRange[item]=="+/- 1.3Ga"){
             		    		magRange=1;
             		    	} else if (Configuration.Shimmer3.ListofMagRange[item]=="+/- 1.9Ga"){
             		    		magRange=2;
             		    	} else if (Configuration.Shimmer3.ListofMagRange[item]=="+/- 2.5Ga"){
             		    		magRange=3;
             		    	} else if (Configuration.Shimmer3.ListofMagRange[item]=="+/- 4.0Ga"){
             		    		magRange=4;
             		    	} else if (Configuration.Shimmer3.ListofMagRange[item]=="+/- 4.7Ga"){
             		    		magRange=5;
             		    	} else if (Configuration.Shimmer3.ListofMagRange[item]=="+/- 5.6Ga"){
             		    		magRange=6;
             		    	} else if (Configuration.Shimmer3.ListofMagRange[item]=="+/- 8.1Ga"){
             		    		magRange=7;
             		    	}

             		    	ServiceActivity.mService.writeMagRange(mBluetoothAddress, magRange);
                 		    Toast.makeText(getApplicationContext(), "Magnometer rate changed. New rate = "+Configuration.Shimmer3.ListofMagRange[item], Toast.LENGTH_SHORT).show();
                 		    buttonMagRange.setText("Mag Range"+"\n"+"("+Configuration.Shimmer3.ListofMagRange[item]+")");
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


        
        final AlertDialog.Builder dialogPressureResolutionShimmer3 = new AlertDialog.Builder(this);		 
        dialogPressureResolutionShimmer3.setTitle("Pressure Resolution").setItems(ListofPressureResolution, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 Log.d("Shimmer",ListofPressureResolution[item]);
                		 int pressureRes=0;
               		  
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
        final AlertDialog.Builder dialogGsrRange = new AlertDialog.Builder(this);		 
        dialogGsrRange.setTitle("Gsr Range").setItems(Configuration.Shimmer3.ListofGSRRange, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int item) {
                		 Log.d("Shimmer",Configuration.Shimmer3.ListofGSRRange[item]);
             		    int gsrRange=0;
             		    if (Configuration.Shimmer3.ListofGSRRange[item]=="10kOhm to 56kOhm"){
             		    	gsrRange=0;
             		    } else if (Configuration.Shimmer3.ListofGSRRange[item]=="56kOhm to 220kOhm"){
             		    	gsrRange=1;
             		    } else if (Configuration.Shimmer3.ListofGSRRange[item]=="220kOhm to 680kOhm"){
             		    	gsrRange=2;
             		    } else if (Configuration.Shimmer3.ListofGSRRange[item]=="680kOhm to 4.7MOhm"){
             		    	gsrRange=3;
             		    } else if (Configuration.Shimmer3.ListofGSRRange[item]=="Auto Range"){
             		    	gsrRange=4;
             		    }

             		    ServiceActivity.mService.writeGSRRange(mBluetoothAddress, gsrRange);
             		    Toast.makeText(getApplicationContext(), "Gsr range changed. New range = "+Configuration.Shimmer3.ListofGSRRange[item], Toast.LENGTH_SHORT).show();
             		    buttonGsr.setText("GSR Range"+"\n"+"("+Configuration.Shimmer3.ListofGSRRange[item]+")");
             		   
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
      		LocalBinder binder = (ShimmerService.LocalBinder) service;
      		mService = binder.getService();
      		cBox5VReg = (CheckBox) findViewById(R.id.checkBox5VReg);
    		cBoxLowPowerMag = (CheckBox) findViewById(R.id.checkBoxLowPowerMag);
    		cBoxLowPowerAccel = (CheckBox) findViewById(R.id.checkBoxLowPowerAccel);
    		cBoxLowPowerGyro = (CheckBox) findViewById(R.id.checkBoxLowPowerGyro);
    		cBoxInternalExpPower  = (CheckBox) findViewById(R.id.CheckBoxIntExpPow);
    		
      		final Shimmer shimmer = mService.getShimmer(mBluetoothAddress);
        	
      		if (shimmer.getInternalExpPower()==1){
      			cBoxInternalExpPower.setChecked(true);
      		} else {
      			cBoxInternalExpPower.setChecked(false);
      		}
      		
      		if (shimmer.isLowPowerMagEnabled()){
        		cBoxLowPowerMag.setChecked(true);
        	}
        	
        	if (shimmer.isLowPowerAccelEnabled()){
        		cBoxLowPowerAccel.setChecked(true);
        	}
        	
        	if (shimmer.isLowPowerGyroEnabled()){
        		cBoxLowPowerGyro.setChecked(true);
        	}
        	
        	
        	
        	if (mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3){
            	buttonGsr.setVisibility(View.VISIBLE);
            	cBox5VReg.setEnabled(false);
            	String currentGyroRange = "("+Configuration.Shimmer3.ListofGyroRange[shimmer.getAccelRange()]+")";
            	buttonGyroRange.setText("Gyro Range"+"\n"+currentGyroRange);
            	String currentMagRange = "("+Configuration.Shimmer3.ListofMagRange[shimmer.getMagRange()-1]+")";
        		buttonMagRange.setText("Mag Range"+"\n"+currentMagRange);
        		String currentPressureResolution = "("+ListofPressureResolution[shimmer.getPressureResolution()]+")";
        		buttonPressureResolution.setText("Pressure Res"+"\n"+currentPressureResolution);
            	
            	if (shimmer.getAccelRange()==0){
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
        		String currentMagRange = "("+Configuration.Shimmer2.ListofMagRange[shimmer.getMagRange()]+")";
        		buttonMagRange.setText("Mag Range"+"\n"+currentMagRange);
        	}
        	
      		
      		mServiceBind = true;
      		//update the view
      		
      		if (mService.get5VReg(mBluetoothAddress)==1){
      			cBox5VReg.setChecked(true);
      		}
      		
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
    					shimmer.enableLowPowerAccel(true);
    				} else {
    					shimmer.enableLowPowerAccel(false);
    				}
    			}
        		
        	});
      		
      		cBoxLowPowerGyro.setOnCheckedChangeListener(new OnCheckedChangeListener(){

    			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
    				// TODO Auto-generated method stub
    				if (checked){
    					shimmer.enableLowPowerGyro(true);
    				} else {
    					shimmer.enableLowPowerGyro(false);
    				}
    			}
        		
        	});
      		
      		cBoxInternalExpPower.setOnCheckedChangeListener(new OnCheckedChangeListener(){

    			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
    				// TODO Auto-generated method stub
    				if (checked){
    					shimmer.writeInternalExpPower(1);
    				} else {
    					shimmer.writeInternalExpPower(0);
    				}
    			}
        		
        	});

  
      		cBoxLowPowerMag.setChecked(mService.isLowPowerMagEnabled(mBluetoothAddress));
      		
      		cBoxLowPowerMag.setOnCheckedChangeListener(new OnCheckedChangeListener(){

    			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
    				// TODO Auto-generated method stub
    				if (checked){
    					mService.enableLowPowerMag(mBluetoothAddress, true);
    				} else {
    					mService.enableLowPowerMag(mBluetoothAddress, false);
    				}
    			}
        		
        	});
      		
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
	
}

