package com.shimmerresearch.multishimmertemplate;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.BiMap;
import com.shimmerresearch.adapters.ListViewFragmentAdapter;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.database.DatabaseHandler;
import com.shimmerresearch.database.ShimmerConfiguration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.service.MultiShimmerTemplateService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DevicesFragment extends Fragment{
	

	public static MultiShimmerTemplateService mService;
	boolean mServiceBind=false;
	public View rootView = null;
	public ListView listViewShimmers;
	public Dialog menuDialog;
	public static ListViewFragmentAdapter mAdapter;
	
	public static Activity mActivity;
	DatabaseHandler db;
    public String[] deviceNames;
	public static String[] deviceBluetoothAddresses;
	public static BT_STATE[] devicesStates;
	public static boolean[] fully_initialized;
    String[] shimmerVersions;
	String[][] mShimmerCommands;
	ImageButton mButtonAddDevice;
	public static int currentPosition;
	public static int currentPositionListView;
	public final int MSG_BLUETOOTH_ADDRESS=1;
	public final int MSG_CONFIGURE_SHIMMER=2;
	public final int MSG_CONFIGURE_SENSORS_SHIMMER=3;
	int tempPosition;
	boolean firstTime=true;
	static int countDisplayPRR=0;
	public static List<String> connectedShimmerAddresses = new ArrayList<String>();
	public static List<String> streamingShimmerAddresses = new ArrayList<String>();
	
	public AlertDialog.Builder menuListViewDialog;
	public ArrayAdapter<String> arrayAdapter;
	public String CONNECT = "Connect";
	public String DISCONNECT = "Disconnect";
	public String START_STREAMING = "Start streaming";
	public String STOP_STREAMING = "Stop streaming";
	public String TOGGLE_LED = "Toggle LED";
	public String DELETE = "Delete";
	public String CONFIGURATION = "Configuration";
	public String ADD_DEVICE = "Add device";
	public String ENABLE_SENSOR = "Enable Sensor";
	
	public Button connectButton;
	public Button streamingButton;
	public Button toggleLEDButton;
	public Button configurationButton;
	public Button deleteButton;
	public Button enableSensorButton;
	public ListView enableSensorListView;
	public Dialog enableSensorDialog;
	public BiMap<String,String> sensorBitmaptoName=null;
	public String [] compatibleSensors;
	public long enabledSensors;
	
	public Dialog deviceListDialog;
	public BluetoothAdapter mBtAdapter;
	public ArrayAdapter<String> mPairedDevicesArrayAdapter;
	public ArrayAdapter<String> mNewDevicesArrayAdapter;
	public int positionDeviceList;
	public View deviceListView;

	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		connectButton = new Button(getActivity());
		menuDialog = new Dialog(getActivity());
		menuListViewDialog =  new AlertDialog.Builder(getActivity());
		deviceListDialog = new Dialog(getActivity());
		mActivity = getActivity();
//		getActivity().invalidateOptionsMenu();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.devices_fragment, container, false);
		
		listViewShimmers = (ListView) rootView.findViewById(R.id.listDevices);
		listViewShimmers.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		listViewShimmers.setAdapter(mAdapter);
		
		menuDialog.setContentView(R.layout.devices_fragment_dialog);
		menuDialog.setTitle("Please Select Option");
		menuDialog.setCancelable(true);
		
		menuListViewDialog.setTitle("Select an option");
		
		
		final AlertDialog.Builder setDeviceNameDialog = new AlertDialog.Builder(getActivity()).setTitle("Device Name").
				setMessage("Introduce the name of the new device");
		
		// Set an EditText view to get user input 
		final EditText inputDeviceName = new EditText(getActivity());
//		setDeviceNameDialog.setView(inputDeviceName);
		setDeviceNameDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				// TODO Auto-generated method stub
				String deviceName = inputDeviceName.getText().toString();
				boolean isRepetead = false;
				for(int i=0; i<deviceNames.length; i++)
					if(deviceNames[i].equals(deviceName)){
						isRepetead = true;
						break;
					}
				
				if(isRepetead){
//					dialog.dismiss();
//					nameRepitedDialog.show();
				}
				else{
					mService.mShimmerConfigurationList.add(new ShimmerConfiguration(deviceName, "", mService.mShimmerConfigurationList.size(), Shimmer.SENSOR_ACCEL, 51.2, -1, -1, -1,-1,-1,-1,-1,-1,0,-1));
					updateShimmerListView(mService.mShimmerConfigurationList);
					dialog.dismiss();
					setBluetoothAddress(deviceNames.length-1);
				}
			}
		});
		setDeviceNameDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		
		
		
		
		enableSensorDialog = new Dialog(getActivity());
		enableSensorDialog.setTitle("Enable Sensor");
		enableSensorDialog.setContentView(R.layout.enable_sensor_list);
		
		
		final AlertDialog.Builder removeAllDevicesDialog = new AlertDialog.Builder(getActivity()).setTitle("Remove all devices").
								setMessage("Are you sure you want to delete all devices?").setPositiveButton("Ok",new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										deleteAllShimmerFromList();
										dialog.dismiss();
									}
								}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										dialog.dismiss();
									}
								});
		
		class OnOptionSelected implements DialogInterface.OnClickListener{
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				String optionSelected = arrayAdapter.getItem(which);
				if(currentPositionListView==1){ // All devices
					if(optionSelected.equals(CONNECT))
						connectAllShimmer();
					else if(optionSelected.equals(DISCONNECT))
						disconnectAllShimmer();
					else if(optionSelected.equals(START_STREAMING))
						startStreamingAllDevices();
					else if(optionSelected.equals(STOP_STREAMING))
						stopStreamingAllDevices();
					else if(optionSelected.equals(TOGGLE_LED))
						toggleAllLeds();
//					else if(optionSelected.equals(ADD_DEVICE)){
//						// This is done in order to avoid an error when the dialog is displayed again after being cancelled
//						if(inputDeviceName.getParent()!=null){
//							ViewGroup parentViewGroup = (ViewGroup)inputDeviceName.getParent();
//							parentViewGroup.removeView(inputDeviceName);
//						}
//						
//						setDeviceNameDialog.setView(inputDeviceName);
//						setDeviceNameDialog.show();
//					}
					else if(optionSelected.equals(DELETE))
						removeAllDevicesDialog.show();
				}
				else{
					if(optionSelected.equals(CONNECT)){
						if(deviceBluetoothAddresses[currentPosition].equals("")) // if the bluetooh address is not set, display a dialog for setting it up
							setBluetoothAddress(currentPosition);
						else
							connectShimmer(currentPosition-2);
					}
					else if(optionSelected.equals(DISCONNECT))
						disconnectShimmer(currentPosition-2);
					else if(optionSelected.equals(START_STREAMING))
						startStreaming(currentPosition-2);
					else if(optionSelected.equals(STOP_STREAMING))
						stopStreaming(currentPosition-2);
					else if(optionSelected.equals(TOGGLE_LED))
						toggleLed(currentPosition-2);
					else if(optionSelected.equals(CONFIGURATION)){
						Bundle args = new Bundle();
				        args.putInt("position", currentPosition);
						Fragment fragment = new ConfigurationFragment();
						fragment.setArguments(args);
						FragmentManager fragmentManager = getFragmentManager();
						fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, "Configuration").commit();
						String title = "Configuration";
						getActivity().getActionBar().setTitle(title); //set the title of the window
					}
					else if(optionSelected.equals(ENABLE_SENSOR)){
						Shimmer shimmer = mService.getShimmer(deviceBluetoothAddresses[currentPosition]);
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
//							System.arraycopy(tmp,0, compatibleSensors, 0, tmp.size());
//							compatibleSensors =  (String[]) tmp.toArray();
						}
 						enabledSensors=mService.getEnabledSensors(deviceBluetoothAddresses[currentPosition]);
// 						List<SelectedSensors> listEnableSensors = createListOfEnableSensor(enabledSensors);						
 						enableSensorListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 						ArrayAdapter<String> adapterSensorNames = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_multiple_choice, compatibleSensors);
 						enableSensorListView.setAdapter(adapterSensorNames);
 						sensorBitmaptoName = Shimmer.generateBiMapSensorIDtoSensorName(mService.getShimmerVersion(deviceBluetoothAddresses[currentPosition]));
 						//check the enabled sensors
 						for (int i=0;i<compatibleSensors.length;i++){
 							if(mService.getShimmerVersion(deviceBluetoothAddresses[currentPosition])==ShimmerVerDetails.HW_ID.SHIMMER_3 && compatibleSensors[i].equals("ECG")){
 	 							if(mService.isEXGUsingECG16Configuration(deviceBluetoothAddresses[currentPosition]) ||
 	 									mService.isEXGUsingECG24Configuration(deviceBluetoothAddresses[currentPosition])){ 
 	 								enableSensorListView.setItemChecked(i, true);
 	 							}
 							}
 							else if(mService.getShimmerVersion(deviceBluetoothAddresses[currentPosition])==ShimmerVerDetails.HW_ID.SHIMMER_3 && compatibleSensors[i].equals("EMG")){
 	 							if(mService.isEXGUsingEMG16Configuration(deviceBluetoothAddresses[currentPosition]) ||
 	 									mService.isEXGUsingEMG24Configuration(deviceBluetoothAddresses[currentPosition])){ 
 	 								enableSensorListView.setItemChecked(i, true);
 	 							}
 							}
 							else if(mService.getShimmerVersion(deviceBluetoothAddresses[currentPosition])==ShimmerVerDetails.HW_ID.SHIMMER_3 && compatibleSensors[i].equals("Test signal")){
 	 							if(mService.isEXGUsingTestSignal16Configuration(deviceBluetoothAddresses[currentPosition]) || 
 	 									mService.isEXGUsingTestSignal24Configuration(deviceBluetoothAddresses[currentPosition])){ 
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
					else if(optionSelected.equals(DELETE))
						deleteShimmerFromList(currentPosition-2);
				}
				dialog.dismiss();
			}
		}
		
		
		enableSensorButton = (Button) enableSensorDialog.findViewById(R.id.buttonEnableSensor);
		enableSensorButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mService.setEnabledSensors(enabledSensors, deviceBluetoothAddresses[currentPosition]);
				enableSensorDialog.dismiss();
			}
		});
		
		enableSensorListView = (ListView) enableSensorDialog.findViewById(R.id.listEnableSensor);
		enableSensorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub

				if(mService.getShimmerVersion(deviceBluetoothAddresses[currentPosition])==ShimmerVerDetails.HW_ID.SHIMMER_3 && compatibleSensors[position].equals("ECG")){
					int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1"));
					int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2"));
					if (!((enabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0) && 
							!((enabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0)){
						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue1, mService.getShimmerVersion(deviceBluetoothAddresses[currentPosition]));
						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue3, mService.getShimmerVersion(deviceBluetoothAddresses[currentPosition]));
					}
					
					if(!enableSensorListView.isItemChecked(position)){
						enableSensorListView.setItemChecked(position, false); //ECG
						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue1, mService.getShimmerVersion(deviceBluetoothAddresses[currentPosition]));
						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue3, mService.getShimmerVersion(deviceBluetoothAddresses[currentPosition]));
					}
					else
						enableSensorListView.setItemChecked(position, true); //ECG
					enableSensorListView.setItemChecked(position+1, false); //EMG
					enableSensorListView.setItemChecked(position+2, false);// TEST SIGNAL
					if(enableSensorListView.isItemChecked(position))
						mService.writeEXGSetting(deviceBluetoothAddresses[currentPosition], 0);

					
				}
				else if(mService.getShimmerVersion(deviceBluetoothAddresses[currentPosition])==ShimmerVerDetails.HW_ID.SHIMMER_3 && compatibleSensors[position].equals("EMG")){
					int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1"));
					int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2"));
					if (!((enabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0) && 
							!((enabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0)){
						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue1, mService.getShimmerVersion(deviceBluetoothAddresses[currentPosition]));
						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue3, mService.getShimmerVersion(deviceBluetoothAddresses[currentPosition]));
					}
					if(!enableSensorListView.isItemChecked(position)){
						enableSensorListView.setItemChecked(position, false); //EMG
						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue1, mService.getShimmerVersion(deviceBluetoothAddresses[currentPosition]));
						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue3, mService.getShimmerVersion(deviceBluetoothAddresses[currentPosition]));
					}
					else
						enableSensorListView.setItemChecked(position, true); //EMG
					enableSensorListView.setItemChecked(position-1, false); //ECG
					enableSensorListView.setItemChecked(position+1, false); //TEST SIGNAL
					if(enableSensorListView.isItemChecked(position))
						mService.writeEXGSetting(deviceBluetoothAddresses[currentPosition], 1);

						
				}
				else if(mService.getShimmerVersion(deviceBluetoothAddresses[currentPosition])==ShimmerVerDetails.HW_ID.SHIMMER_3 && compatibleSensors[position].equals("Test signal")){
					int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1"));
					int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2"));
					if (!((enabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0) && 
							!((enabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0)){
						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue1, mService.getShimmerVersion(deviceBluetoothAddresses[currentPosition]));
						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue3, mService.getShimmerVersion(deviceBluetoothAddresses[currentPosition]));
					}
					if(!enableSensorListView.isItemChecked(position)){
						enableSensorListView.setItemChecked(position, false); //TEST SIGNAL
						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue1, mService.getShimmerVersion(deviceBluetoothAddresses[currentPosition]));
						enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,iDBMValue3, mService.getShimmerVersion(deviceBluetoothAddresses[currentPosition]));
					}
					else
						enableSensorListView.setItemChecked(position, true); //TEST SIGNAL
					enableSensorListView.setItemChecked(position-1, false); //EMG
					enableSensorListView.setItemChecked(position-2, false); //ECG
					if(enableSensorListView.isItemChecked(position))
						mService.writeEXGSetting(deviceBluetoothAddresses[currentPosition], 2);
				
						
				}
				else{
					int sensorIdentifier = Integer.parseInt(sensorBitmaptoName.inverse().get(compatibleSensors[position]));
					//check and remove any old daughter boards (sensors) which will cause a conflict with sensorIdentifier 
					enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors,sensorIdentifier, mService.getShimmerVersion(deviceBluetoothAddresses[currentPosition]));
					//update the checkbox accordingly 
					//since the last three elements (ECG,EMG,TestSignal) in Shimmer 3 are not signals,
					//we treat them in a different way and they are not updated like the rest
					int end=0;
					if(mService.getShimmerVersion(deviceBluetoothAddresses[currentPosition])==ShimmerVerDetails.HW_ID.SHIMMER_3)
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

		
		listViewShimmers.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				// TODO Auto-generated method stub
				//get the item selected and open a dialog wiht the options
				arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item);
				currentPositionListView = position;
				if(currentPositionListView==1){ // if the item selected is "All Devices"
					


				}
				else if(currentPositionListView==0){
					// This is done in order to avoid an error when the dialog is displayed again after being cancelled

				}
				else{
					
					if(mService.isShimmerConnected(deviceBluetoothAddresses[currentPositionListView])){
						if(!mService.deviceStreaming(deviceBluetoothAddresses[currentPositionListView])){
							Bundle args = new Bundle();
					        args.putInt("position", currentPositionListView);
					        args.putString("address", deviceBluetoothAddresses[currentPositionListView]);
							Fragment fragment = new ConfigurationFragment();
							fragment.setArguments(args);
							FragmentManager fragmentManager = getFragmentManager();
							fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, "Configure").commit();
							String title = "Configuration";
							getActivity().getActionBar().setTitle(title); //set the title of the window
						}
						else
							Toast.makeText(getActivity(), "Device streaming. Stop streaming to configure it", Toast.LENGTH_SHORT).show();
					}
					else{
						Toast.makeText(getActivity(), "Device no connected. Use right menu to connect it.", Toast.LENGTH_SHORT).show();
					}
					
					
				}
				
			}
		});
		
		this.mService = ((MainActivity)getActivity()).mService;
		
		if(mService!=null){
			setup();
		}

		return rootView;
	}
	
	
	public void connectShimmer(int position){
//    	mService.connectandConfigureShimmer(mService.mShimmerConfigurationList.get(position));
    	mService.connectShimmer(mService.mShimmerConfigurationList.get(position).getBluetoothAddress(), mService.mShimmerConfigurationList.get(position).getDeviceName());
    }
    
    public void connectAllShimmer(){
    	mService.connectAllShimmerSequentiallyWithConfig(mService.mShimmerConfigurationList);
    	mService.setHandler(mHandler);
    }
    public void disconnectAllShimmer(){
    	mService.disconnectAllDevices();
    }
    public void disconnectShimmer(int position){
    	mService.disconnectShimmerNew(mService.mShimmerConfigurationList.get(position).getBluetoothAddress());
    } 
    public void toggleLed(int position){
    	mService.toggleLED(mService.mShimmerConfigurationList.get(position).getBluetoothAddress());
    }
    public void toggleAllLeds(){
    	mService.toggleAllLEDS();
    }
    
    public void startStreamingAllDevices(){
     	mService.startStreamingAllDevices();
    }
    
    public void stopStreamingAllDevices(){
    	mService.stopStreamingAllDevices();
    	
    }
    
    public void startStreaming(int position){
    	mService.startStreaming(mService.mShimmerConfigurationList.get(position).getBluetoothAddress());
    }
    
    public void stopStreaming(int position){	    	
    	mService.stopStreaming(mService.mShimmerConfigurationList.get(position).getBluetoothAddress());
    }
	    
	
    private static Handler mHandler = new Handler() {
 	   

    	public void handleMessage(Message msg) {
    		
			switch (msg.what) {
		        case Shimmer.MESSAGE_PACKET_LOSS_DETECTED:
		        	Log.d("SHIMMERPACKETRR3","Detected");
		        	if (countDisplayPRR%1000==0){ //this is to prevent the UI from having to overwork when the PRR is very low
		        		countDisplayPRR++;
		        		mAdapter.notifyDataSetChanged();
		        	}
		        break;
		        case Shimmer.MESSAGE_TOAST:
		        	Toast.makeText(mActivity, msg.getData().getString(Shimmer.TOAST), Toast.LENGTH_SHORT).show();
		        break;
		        case Shimmer.MESSAGE_STATE_CHANGE:
                    switch (((ObjectCluster)msg.obj).mState) {
                    /*case CONNECTED:
                     	//check to see if there are other Shimmer Devices which need to be connected
    		           	//sendBroadcast(intent);
    		       	 	connectedShimmerAddresses.add(((ObjectCluster)msg.obj).mBluetoothAddress);
    		       	 		
                        break;*/
                    case CONNECTED:
                    	connectedShimmerAddresses.add(((ObjectCluster)msg.obj).getMacAddress());
                    	int indexFully=0;
//		            	if(currentPosition==1){
		            		String addressInitialized = ((ObjectCluster) msg.obj).getMacAddress();
			        		for(int i=0; i<deviceBluetoothAddresses.length; i++)
			        			if(deviceBluetoothAddresses[i].equals(addressInitialized)){
			        				indexFully=i;
			        				break;
			        			}

		           	Log.d("ShimmerCA","Fully Initialized");
		           	devicesStates[indexFully] = ((ObjectCluster) msg.obj).mState;
		           	fully_initialized[indexFully] = true;
		            mActivity.runOnUiThread(new Runnable() {
		    		    public void run() {
		    		    	//update the children array in mAdapter first
		    		    	mAdapter.notifyDataSetChanged();
		    		    }
		    		});
		            
		            mService.readAndSaveConfiguration(indexFully-2, addressInitialized);
		        	
                        break;
                    case CONNECTING:

		            	int indexConnecting=0;
//			    		if(currentPosition==1){
			    			String addressConnecting = ((ObjectCluster) msg.obj).getMacAddress();
			        		for(int i=0; i<deviceBluetoothAddresses.length; i++)
			        			if(deviceBluetoothAddresses[i].equals(addressConnecting)){
			        				indexConnecting=i;
			        				break;
			        			} 
//			        	}
//			    		else if(currentPosition==0)
//		            		indexConnecting=deviceBluetoothAddresses.length-1;
//			    		else
//			    			indexConnecting = currentPosition;
			            devicesStates[indexConnecting] = BT_STATE.CONNECTING;
			            fully_initialized[indexConnecting] = false;
			            mActivity.runOnUiThread(new Runnable() {
			    		    public void run() {
			    		    	//update the children array in mAdapter first
			    		    	mAdapter.notifyDataSetChanged();
			    		    }
			    		});
                        break;
                    case STREAMING:
                    	int indexStreaming=0;
		            	if(currentPosition==1){
		            		String addressStreaming = ((ObjectCluster) msg.obj).getMacAddress();
			        		for(int i=0; i<deviceBluetoothAddresses.length; i++)
			        			if(deviceBluetoothAddresses[i].equals(addressStreaming)){
			        				indexStreaming=i;
			        				break;
			        			}
			        	}
		            	else
		            		indexStreaming=currentPosition;
			           	Log.d("ShimmerCA","Streaming");
			           	streamingShimmerAddresses.add(((ObjectCluster)msg.obj).getMacAddress());
			           	devicesStates[indexStreaming] = ((ObjectCluster)msg.obj).mState;
			           	mAdapter.notifyDataSetChanged();
                    	break;
                    case STREAMING_AND_SDLOGGING:
                    	break;
                    case SDLOGGING:
                   	 break;
                    case DISCONNECTED:

		            	int indexNone=0;
//		            	if(currentPosition==1){
		            		String addressNone = ((ObjectCluster) msg.obj).getMacAddress();
			        		for(int i=0; i<deviceBluetoothAddresses.length; i++)
			        			if(deviceBluetoothAddresses[i].equals(addressNone)){
			        				indexNone=i;
			        				break;
			        			}
//			        	}
//		            	else if(currentPosition==0)
//		            		indexNone=deviceBluetoothAddresses.length-1;
//		            	else
//		            		indexNone=currentPosition;
		            connectedShimmerAddresses.remove(((ObjectCluster)msg.obj).getMacAddress());
		           	Log.d("Shimmer","NO_State" + ((ObjectCluster)msg.obj).getMacAddress());
		           	devicesStates[indexNone] = BT_STATE.DISCONNECTED;
		           	fully_initialized[indexNone] = false;
		            mActivity.runOnUiThread(new Runnable() {
		    		    public void run() {
		    		    	//update the children array in mAdapter first
		    		    	mAdapter.notifyDataSetChanged();
		    		    }
		    		});

                        break;
                    }
               	 
		        	/*
		       	 switch (msg.arg1) {
		       	 	case Shimmer.STATE_CONNECTED:
		           	//check to see if there are other Shimmer Devices which need to be connected
		           	//sendBroadcast(intent);
		       	 		
		       	 		connectedShimmerAddresses.add(((ObjectCluster)msg.obj).mBluetoothAddress);
		            break;
		            case Shimmer.STATE_CONNECTING:
		            	int indexConnecting=0;
//			    		if(currentPosition==1){
			    			String addressConnecting = ((ObjectCluster) msg.obj).mBluetoothAddress;
			        		for(int i=0; i<deviceBluetoothAddresses.length; i++)
			        			if(deviceBluetoothAddresses[i].equals(addressConnecting)){
			        				indexConnecting=i;
			        				break;
			        			} 
//			        	}
//			    		else if(currentPosition==0)
//		            		indexConnecting=deviceBluetoothAddresses.length-1;
//			    		else
//			    			indexConnecting = currentPosition;
			            devicesStates[indexConnecting] = 1;
			            fully_initialized[indexConnecting] = false;
			            mActivity.runOnUiThread(new Runnable() {
			    		    public void run() {
			    		    	//update the children array in mAdapter first
			    		    	mAdapter.notifyDataSetChanged();
			    		    }
			    		});
		            break;
		            case Shimmer.STATE_NONE:
		            	int indexNone=0;
//		            	if(currentPosition==1){
		            		String addressNone = ((ObjectCluster) msg.obj).mBluetoothAddress;
			        		for(int i=0; i<deviceBluetoothAddresses.length; i++)
			        			if(deviceBluetoothAddresses[i].equals(addressNone)){
			        				indexNone=i;
			        				break;
			        			}
//			        	}
//		            	else if(currentPosition==0)
//		            		indexNone=deviceBluetoothAddresses.length-1;
//		            	else
//		            		indexNone=currentPosition;
		            connectedShimmerAddresses.remove(((ObjectCluster)msg.obj).mBluetoothAddress);
		           	Log.d("Shimmer","NO_State" + ((ObjectCluster)msg.obj).mBluetoothAddress);
		           	devicesStates[indexNone] = 0;
		           	fully_initialized[indexNone] = false;
		            mActivity.runOnUiThread(new Runnable() {
		    		    public void run() {
		    		    	//update the children array in mAdapter first
		    		    	mAdapter.notifyDataSetChanged();
		    		    }
		    		});

		                break;
		            case Shimmer.MSG_STATE_FULLY_INITIALIZED:
		            	int indexFully=0;
//		            	if(currentPosition==1){
		            		String addressInitialized = ((ObjectCluster) msg.obj).mBluetoothAddress;
			        		for(int i=0; i<deviceBluetoothAddresses.length; i++)
			        			if(deviceBluetoothAddresses[i].equals(addressInitialized)){
			        				indexFully=i;
			        				break;
			        			}

		           	Log.d("ShimmerCA","Fully Initialized");
		           	devicesStates[indexFully] = 2;
		           	fully_initialized[indexFully] = true;
		            mActivity.runOnUiThread(new Runnable() {
		    		    public void run() {
		    		    	//update the children array in mAdapter first
		    		    	mAdapter.notifyDataSetChanged();
		    		    }
		    		});
		            
		            mService.readAndSaveConfiguration(indexFully-2, addressInitialized);
		        	
		           		break;
		            case Shimmer.MSG_STATE_STREAMING:
		            	int indexStreaming=0;
		            	if(currentPosition==1){
		            		String addressStreaming = ((ObjectCluster) msg.obj).mBluetoothAddress;
			        		for(int i=0; i<deviceBluetoothAddresses.length; i++)
			        			if(deviceBluetoothAddresses[i].equals(addressStreaming)){
			        				indexStreaming=i;
			        				break;
			        			}
			        	}
		            	else
		            		indexStreaming=currentPosition;
			           	Log.d("ShimmerCA","Streaming");
			           	streamingShimmerAddresses.add(((ObjectCluster)msg.obj).mBluetoothAddress);
			           	devicesStates[indexStreaming] = 3;
			           	mAdapter.notifyDataSetChanged();
			    		break;
		            case Shimmer.MSG_STATE_STOP_STREAMING:
		            	int indexStopStreaming=0;
		            	if(currentPosition==1){
		            		String addressStopStreaming = ((ObjectCluster) msg.obj).mBluetoothAddress;
			        		for(int i=0; i<deviceBluetoothAddresses.length; i++)
			        			if(deviceBluetoothAddresses[i].equals(addressStopStreaming)){
			        				indexStopStreaming=i;
			        				break;
			        			}
			        	}
		            	else
		            		indexStopStreaming=currentPosition;
			           	Log.d("ShimmerCA","Streaming");
			           	streamingShimmerAddresses.remove(((ObjectCluster)msg.obj).mBluetoothAddress);
			           	devicesStates[indexStopStreaming] = 2;
			           	mAdapter.notifyDataSetChanged();
			    		break;
		        }
		        */
		       	 break;
	        
			}    
    	}
    };
    
    
    public void onPause(){
    	super.onPause();
    	if (mService!=null)
    		db.saveShimmerConfigurations("Temp", mService.mShimmerConfigurationList);

    }
    

    
    public void updateShimmerListView(List<ShimmerConfiguration> shimmerConfigurationList){
    	deviceNames=new String[shimmerConfigurationList.size()+2]; //+2 to include All Devices && Add New Device
    	devicesStates=new BT_STATE[shimmerConfigurationList.size()+2]; //+2 to include All Devices && Add New Device
    	fully_initialized=new boolean[shimmerConfigurationList.size()+2]; //+2 to include All Devices && Add New Device
    	shimmerVersions=new String[shimmerConfigurationList.size()+2]; //+2 to include All Devices && Add New Device
    	deviceBluetoothAddresses=new String[shimmerConfigurationList.size()+2]; //+2 to include All Devices && Add New Device
  		mShimmerCommands= new String[shimmerConfigurationList.size()+2][5];
//  		numberofChilds=new int[shimmerConfigurationList.size()+1];
//  		viewArray = new View[shimmerConfigurationList.size()+1];
//  		Arrays.fill(numberofChilds,5);
  		//initialize all Devices
  		deviceNames[1]="All Devices";
  		deviceBluetoothAddresses[1]="";
  		deviceNames[0]="Add New Device";
  		deviceBluetoothAddresses[0]="";

  		//fill in the rest of the devices 
  		int pos=2;
  		for (ShimmerConfiguration sc:shimmerConfigurationList){
  			deviceNames[pos]=sc.getDeviceName();
  			shimmerVersions[pos]=Integer.toString(sc.getShimmerVersion());
      		deviceBluetoothAddresses[pos]=sc.getBluetoothAddress();
      		BT_STATE state = mService.getShimmerState(sc.getBluetoothAddress());

      		
      		devicesStates[pos] = state;
      		if(state == BT_STATE.CONNECTED || state == BT_STATE.STREAMING)
      			fully_initialized[pos] = true;
      		else
      			fully_initialized[pos] = false;

      		pos++;
  		}
     
  		mAdapter = new ListViewFragmentAdapter(getActivity(), deviceBluetoothAddresses, deviceNames, devicesStates, this);
  		listViewShimmers = (ListView) rootView.findViewById(R.id.listDevices);
        listViewShimmers.setAdapter(mAdapter);
  		
    }
    
    
    public void setup(){
    	db=mService.mDataBase;
  		mService.setHandler(mHandler);
  		listViewShimmers = (ListView) rootView.findViewById(R.id.listDevices);
  		
  		mService.mShimmerConfigurationList = db.getShimmerConfigurations("Temp");
  		updateShimmerListView(mService.mShimmerConfigurationList);
  		for (ShimmerConfiguration sc:mService.mShimmerConfigurationList){
  			Log.d("ShimmerDB",sc.getDeviceName());
  			Log.d("ShimmerDB",Double.toString(sc.getSamplingRate()));
  		}
  		//now connect the sensor nodes
  		mService.enableGraphingHandler(false);
  		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	        if(mBluetoothAdapter == null) {
	        	Toast.makeText(getActivity(), "Bluetooth not supported on device.", Toast.LENGTH_LONG).show();
	        } else {
	        	if (!mBluetoothAdapter.isEnabled()) {
	        	    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	        	    startActivityForResult(enableBtIntent, -1);
	          }
	        }
    
    }
    
    
    public void deleteAllShimmerFromList(){
    	
    	if(mService.allShimmersDisconnected()){
    		mService.mShimmerConfigurationList.clear();
    		updateShimmerListView(mService.mShimmerConfigurationList);
    	}
    	else
    		Toast.makeText(getActivity(), "Please disconnect all the devices first", Toast.LENGTH_SHORT).show();
//    		dialogError.show();
    }
    
    
    public void deleteShimmerFromList(final int position){
    	// custom dialog
		final Builder dialog = new AlertDialog.Builder(getActivity());
//		dialog.setContentView(R.layout.dialogverify);
    	dialog.setTitle("Verification");
    	dialog.setMessage("Delete Shimmer From List?");
    	dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//first check to see if there is a connection to the device
				if (!mService.isShimmerConnected(mService.mShimmerConfigurationList.get(position).getBluetoothAddress())){
					mService.mShimmerConfigurationList.remove(position);
					updateShimmerListView(mService.mShimmerConfigurationList);
//			    	mService.removeExapandableStates("ControlActivity");
//					mService.removeExapandableStates("PlotActivity");
				} else {
					Toast.makeText(getActivity(), "Please disconnect from device first.", Toast.LENGTH_LONG).show();	
				}
			}

		});
    	
    	dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
    				
    		dialog.show();
    	
    }
    
    
    
    public void setBluetoothAddress(int position){
//    	if (mService.noDevicesStreaming()){
	    	Intent mainCommandIntent=new Intent(getActivity(),DeviceListActivity.class);
	    	mainCommandIntent.putExtra("Position",position);
		    startActivityForResult(mainCommandIntent, MSG_BLUETOOTH_ADDRESS);
//    	} else {
//    		Toast.makeText(getActivity(), "Please ensure no device is streaming.", Toast.LENGTH_LONG).show();	
//    	}
	    
    }
    
    
    
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	switch (requestCode) {	
	    	case MSG_BLUETOOTH_ADDRESS:
	        // When DeviceListActivity returns with a device to connect
	        if (resultCode == Activity.RESULT_OK) {
	            String address = data.getExtras()
	                    .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
	            Log.d("SHimmerps",address);
	            boolean newAddress=true;
	            
	            //first check if the Bluetooth Address has been previously selected
	            for (ShimmerConfiguration sc:mService.mShimmerConfigurationList){
	            	if (sc.getBluetoothAddress().equals(address)){
	            		newAddress=false;
	            	}
	            }
	            
	            if (newAddress){
		            int position=data.getExtras().getInt("Position");
		            ShimmerConfiguration sc = mService.mShimmerConfigurationList.get(position-2);
					sc.setBluetoothAddress(address);
					sc.setShimmerVersion(-1); 
					mService.mShimmerConfigurationList.set(position-2, sc);
					db.saveShimmerConfigurations("Temp", mService.mShimmerConfigurationList);
					updateShimmerListView(mService.mShimmerConfigurationList);
					connectShimmer(position-2);
				} else {
					TextView textSelectedAddress = new TextView(mActivity);
					textSelectedAddress.setText("The bluetooth address selected is already in the list.\n Would you like to select a new address?");
					AlertDialog.Builder selectedAddressDialog = new AlertDialog.Builder(mActivity).setTitle("Bluetooth Address Already Selected");
//					Toast.makeText(getActivity(), "Bluetooth Address already selected in list", Toast.LENGTH_LONG).show();	
					selectedAddressDialog.setView(textSelectedAddress);
					selectedAddressDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							setBluetoothAddress(deviceNames.length-1);
						}
					});
					selectedAddressDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							mService.mShimmerConfigurationList.remove(mService.mShimmerConfigurationList.size()-1);
							updateShimmerListView(mService.mShimmerConfigurationList);
						}
					});
					selectedAddressDialog.show();
				}
	        }
	        break;
    	}
    }


}
