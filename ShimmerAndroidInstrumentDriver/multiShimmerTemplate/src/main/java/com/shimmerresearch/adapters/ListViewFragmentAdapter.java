package com.shimmerresearch.adapters;

import java.util.ArrayList;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;
import com.shimmerresearch.database.ShimmerConfiguration;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.multishimmertemplate.ConfigurationFragment;
import com.shimmerresearch.multishimmertemplate.DevicesFragment;
import com.shimmerresearch.multishimmertemplate.R;
import com.shimmerresearch.service.MultiShimmerTemplateService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListViewFragmentAdapter extends ArrayAdapter<String>{

	
	private final Context context;
	private String[] bluetooth_addresses;
	private String[] devices_names;
	private BT_STATE[] devices_states;
	private int currentPosition;
	private static DevicesFragment dF;
	
	
	public ListViewFragmentAdapter(Context context, String[] bluetoothAddresses, String[] devicesNames, BT_STATE[] devicesStates, DevicesFragment dF) {
		super(context, R.layout.devices_fragment_item, bluetoothAddresses);
		// TODO Auto-generated constructor stub
		this.context = context;
	    this.bluetooth_addresses = bluetoothAddresses;
	    this.devices_names = devicesNames;
	    this.devices_states = devicesStates;
	    this.dF = dF;
	}
	
	 @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
		 
	    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.devices_fragment_item, parent, false);
	    
	    TextView bluetoothAddress = (TextView) rowView.findViewById(R.id.bluetooth_address);
	    TextView deviceName = (TextView) rowView.findViewById(R.id.device_name);
	    ImageView state = (ImageView) rowView.findViewById(R.id.image_state);
	    ImageView popUpMenu = (ImageView) rowView.findViewById(R.id.image_menu);
	    popUpMenu.setId(position);
	    
	    bluetoothAddress.setText(bluetooth_addresses[position]);
	    deviceName.setText(devices_names[position]);
	    if(!bluetooth_addresses[position].equals("")){
	    	switch(devices_states[position]){
		    	case DISCONNECTED: // DISCONNECTED
		    		state.setImageResource(R.drawable.circle_red);
		    	break;
		    	case CONNECTING: // CONNECTING
		    		state.setImageResource(R.drawable.circle_yellow);
		    	break;
		    	case CONNECTED: // CONNECTED
		    		state.setImageResource(R.drawable.circle_green);
		    	break;
		    	case STREAMING: // STREAMING
		    		state.setImageResource(R.drawable.circle_blue);
		    	break;
		    	default:
		    		state.setImageResource(R.drawable.circle_red);
		    	break;
	    	}
	    	
	    		
	    }
	    
	    

	    final AlertDialog.Builder removeAllDevicesDialog = new AlertDialog.Builder(DevicesFragment.mActivity).setTitle("Remove all devices").
				setMessage("Are you sure you want to delete all devices?").setPositiveButton("Ok",new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dF.deleteAllShimmerFromList();
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
				String optionSelected = dF.arrayAdapter.getItem(which);
				if(currentPosition==1){ // All devices
					if(optionSelected.equals(dF.CONNECT))
						dF.connectAllShimmer();
					else if(optionSelected.equals(dF.DISCONNECT))
						dF.disconnectAllShimmer();
					else if(optionSelected.equals(dF.START_STREAMING))
						dF.startStreamingAllDevices();
					else if(optionSelected.equals(dF.STOP_STREAMING))
						dF.stopStreamingAllDevices();
					else if(optionSelected.equals(dF.TOGGLE_LED))
						dF.toggleAllLeds();
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
					else if(optionSelected.equals(dF.DELETE))
						removeAllDevicesDialog.show();
				}
				else{
					if(optionSelected.equals(dF.CONNECT)){
						if(dF.deviceBluetoothAddresses[currentPosition].equals("")) // if the bluetooh address is not set, display a dialog for setting it up
							dF.setBluetoothAddress(currentPosition);
						else
							dF.connectShimmer(currentPosition-2);
					}
					else if(optionSelected.equals(dF.DISCONNECT))
						dF.disconnectShimmer(currentPosition-2);
					else if(optionSelected.equals(dF.START_STREAMING))
						dF.startStreaming(currentPosition-2);
					else if(optionSelected.equals(dF.STOP_STREAMING))
						dF.stopStreaming(currentPosition-2);
					else if(optionSelected.equals(dF.TOGGLE_LED))
						dF.toggleLed(currentPosition-2);
					else if(optionSelected.equals(dF.CONFIGURATION)){
						Bundle args = new Bundle();
				        args.putInt("position", currentPosition);
				        args.putString("address", dF.deviceBluetoothAddresses[currentPosition]);
						Fragment fragment = new ConfigurationFragment();
						fragment.setArguments(args);
						FragmentManager fragmentManager = dF.getFragmentManager();
						fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, "Configuration").commit();
						String title = "Configuration";
						dF.getActivity().getActionBar().setTitle(title); //set the title of the window
					}
					else if(optionSelected.equals(dF.ENABLE_SENSOR)){
						Shimmer shimmer = dF.mService.getShimmer(dF.deviceBluetoothAddresses[currentPosition]);
						dF.compatibleSensors = shimmer.getListofSupportedSensors();
						
						if(shimmer.getShimmerVersion() == ShimmerVerDetails.HW_ID.SHIMMER_3){ //replace EXG1, EXG2, EXG1 16 bit and EXG2 16 bit for ECG,EMG and test signal
							ArrayList<String> tmp = new ArrayList<String>();
							for(int i=0;i<dF.compatibleSensors.length;i++)
								if(!dF.compatibleSensors[i].equals("EXG1") && !dF.compatibleSensors[i].equals("EXG2") &&
										!dF.compatibleSensors[i].equals("EXG1 16Bit") && !dF.compatibleSensors[i].equals("EXG2 16Bit")){
									tmp.add(dF.compatibleSensors[i]);
								}
							tmp.add("ECG");
							tmp.add("EMG");
							tmp.add("Test signal");
							dF.compatibleSensors = new String[tmp.size()];
							for(int i=0;i<tmp.size();i++)
								dF.compatibleSensors[i] = tmp.get(i);
//							System.arraycopy(tmp,0, compatibleSensors, 0, tmp.size());
//							compatibleSensors =  (String[]) tmp.toArray();
						}
						dF.enabledSensors=dF.mService.getEnabledSensors(dF.deviceBluetoothAddresses[currentPosition]);
// 						List<SelectedSensors> listEnableSensors = createListOfEnableSensor(enabledSensors);						
						dF.enableSensorListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 						ArrayAdapter<String> adapterSensorNames = new ArrayAdapter<String>(dF.getActivity(), android.R.layout.simple_list_item_multiple_choice, dF.compatibleSensors);
 						dF.enableSensorListView.setAdapter(adapterSensorNames);
 						dF.sensorBitmaptoName = Shimmer.generateBiMapSensorIDtoSensorName(dF.mService.getShimmerVersion(dF.deviceBluetoothAddresses[currentPosition]));
 						//check the enabled sensors
 						for (int i=0;i<dF.compatibleSensors.length;i++){
 							if(dF.mService.getShimmerVersion(dF.deviceBluetoothAddresses[currentPosition])==ShimmerVerDetails.HW_ID.SHIMMER_3 && dF.compatibleSensors[i].equals("ECG")){
 	 							if(dF.mService.isEXGUsingECG16Configuration(dF.deviceBluetoothAddresses[currentPosition]) ||
 	 									dF.mService.isEXGUsingECG24Configuration(dF.deviceBluetoothAddresses[currentPosition])){ 
 	 								dF.enableSensorListView.setItemChecked(i, true);
 	 							}
 							}
 							else if(dF.mService.getShimmerVersion(dF.deviceBluetoothAddresses[currentPosition])==ShimmerVerDetails.HW_ID.SHIMMER_3 && dF.compatibleSensors[i].equals("EMG")){
 	 							if(dF.mService.isEXGUsingEMG16Configuration(dF.deviceBluetoothAddresses[currentPosition]) ||
 	 									dF.mService.isEXGUsingEMG24Configuration(dF.deviceBluetoothAddresses[currentPosition])){ 
 	 								dF.enableSensorListView.setItemChecked(i, true);
 	 							}
 							}
 							else if(dF.mService.getShimmerVersion(dF.deviceBluetoothAddresses[currentPosition])==ShimmerVerDetails.HW_ID.SHIMMER_3 && dF.compatibleSensors[i].equals("Test signal")){
 	 							if(dF.mService.isEXGUsingTestSignal16Configuration(dF.deviceBluetoothAddresses[currentPosition]) || 
 	 									dF.mService.isEXGUsingTestSignal24Configuration(dF.deviceBluetoothAddresses[currentPosition])){ 
 	 								dF.enableSensorListView.setItemChecked(i, true);
 	 							}
 							} 							
 							else{
 								int iDBMValue = Integer.parseInt(dF.sensorBitmaptoName.inverse().get(dF.compatibleSensors[i]));	
 	 							if( (iDBMValue & dF.enabledSensors) >0){
 	 								dF.enableSensorListView.setItemChecked(i, true);
 	 							}
 							}
 						}
 						dF.enableSensorDialog.show();
					}
					else if(optionSelected.equals(dF.DELETE))
						dF.deleteShimmerFromList(currentPosition-2);
				}
				dialog.dismiss();
			}
		}
	    
	    
	    final AlertDialog.Builder setDeviceNameDialog = new AlertDialog.Builder(DevicesFragment.mActivity);
	    final EditText inputDeviceName = new EditText(DevicesFragment.mActivity);
	   
	    
	    final AlertDialog.Builder nameRepitedDialog = new AlertDialog.Builder(DevicesFragment.mActivity).setTitle("Invalid Name").
				setMessage("This name is already used. Please, select a different name");
	    nameRepitedDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				if(inputDeviceName.getParent()!=null){
					ViewGroup parentViewGroup = (ViewGroup)inputDeviceName.getParent();
					parentViewGroup.removeView(inputDeviceName);
				}
				
				setDeviceNameDialog.setView(inputDeviceName);
				setDeviceNameDialog.show();				
			}
		});
	    
//	    final AlertDialog.Builder setDeviceNameDialog = new AlertDialog.Builder(DevicesFragment.mActivity);
//	    final Dialog nameRepitedDialog = new Dialog(DevicesFragment.mActivity);
//	    nameRepitedDialog.setTitle("Invalid Name");
//	    nameRepitedDialog.setContentView(R.layout.invalid_name_dialog);
//	    nameRepitedDialog.setCancelable(true);
//	    Button button_name_repited = (Button) nameRepitedDialog.findViewById(R.id.button_invalid_name);
//	    button_name_repited.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				nameRepitedDialog.dismiss();
////				if(editTextDeviceName.getParent()!=null){
////					ViewGroup parentViewGroup = (ViewGroup)editTextDeviceName.getParent();
////					parentViewGroup.removeView(editTextDeviceName);
////				}
//				
//				setDeviceNameDialog.show();
//			}
//		});


	    setDeviceNameDialog.setTitle("Device Name").setMessage("Introduce the name of the new device");
		// Set an EditText view to get user input 
//		setDeviceNameDialog.setView(inputDeviceName);
		setDeviceNameDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				// TODO Auto-generated method stub
				String deviceName = inputDeviceName.getText().toString();
				boolean isRepetead = false;
				for(int i=0; i<dF.deviceNames.length; i++)
					if(dF.deviceNames[i].equals(deviceName)){
						isRepetead = true;
						break;
					}
				
				if(isRepetead){
					dialog.cancel();
					nameRepitedDialog.show();
				}
				else{
					
					dF.mService.mShimmerConfigurationList.add(new ShimmerConfiguration(deviceName, "", dF.mService.mShimmerConfigurationList.size(), Shimmer.SENSOR_ACCEL, 51.2, -1, -1, -1,-1,-1,-1,-1,-1,0,-1));
					dF.updateShimmerListView(dF.mService.mShimmerConfigurationList);
					dialog.dismiss();
					dF.setBluetoothAddress(dF.deviceNames.length-1);
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
	    
	    
	    popUpMenu.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dF.currentPosition = currentPosition = v.getId();
				dF.arrayAdapter = new ArrayAdapter<String>(DevicesFragment.mActivity, android.R.layout.select_dialog_item);
				
				if(currentPosition==1){ // if the item selected is "All Devices"
					if(dF.deviceNames.length>1)
						dF.arrayAdapter.add(dF.CONNECT);
					if(!dF.mService.allShimmersDisconnected()){
						dF.arrayAdapter.add(dF.DISCONNECT);
						dF.arrayAdapter.add(dF.TOGGLE_LED);
						if(!dF.mService.allDevicesStreaming())
							dF.arrayAdapter.add(dF.START_STREAMING);
						if(!dF.mService.noDevicesStreaming())
							dF.arrayAdapter.add(dF.STOP_STREAMING);
					}
					if(dF.deviceNames.length>1)
						dF.arrayAdapter.add(dF.DELETE);
					
					OnOptionSelected oos = new OnOptionSelected();
					dF.menuListViewDialog.setTitle("All Devices");
					dF.menuListViewDialog.setAdapter(dF.arrayAdapter, oos);
					dF.menuListViewDialog.setCancelable(true);
					dF.menuListViewDialog.show();
				}
				else if(currentPosition==0){
					// This is done in order to avoid an error when the dialog is displayed again after being cancelled
					if(inputDeviceName.getParent()!=null){
						ViewGroup parentViewGroup = (ViewGroup)inputDeviceName.getParent();
						parentViewGroup.removeView(inputDeviceName);
					}
					
					setDeviceNameDialog.setView(inputDeviceName);
					setDeviceNameDialog.setCancelable(true);
					setDeviceNameDialog.show();
				}
				else{
					
					switch(dF.mService.getShimmerState(dF.deviceBluetoothAddresses[currentPosition])){							
						case DISCONNECTED: // DISCONNECTED
							dF.arrayAdapter.add(dF.CONNECT);
							dF.arrayAdapter.add(dF.DELETE);
						break;
						case CONNECTING: // CONNECTING
							Toast.makeText(dF.getActivity(), "The device is trying to connect", Toast.LENGTH_SHORT).show();
						break;
						case CONNECTED: // CONNECTED
							dF.arrayAdapter.add(dF.DISCONNECT);
							
							dF.arrayAdapter.add(dF.START_STREAMING);
							dF.arrayAdapter.add(dF.TOGGLE_LED);
//							dF.arrayAdapter.add(dF.ENABLE_SENSOR);
							dF.arrayAdapter.add(dF.CONFIGURATION);
						break;
						case STREAMING: // CONNECTED
							dF.arrayAdapter.add(dF.STOP_STREAMING);
							dF.arrayAdapter.add(dF.TOGGLE_LED);
							break;
						default:
							dF.arrayAdapter.add(dF.CONNECT);
							dF.arrayAdapter.add(dF.DELETE);
						break;
					}
					OnOptionSelected oos = new OnOptionSelected();
					dF.menuListViewDialog.setTitle(dF.deviceNames[currentPosition]);
					dF.menuListViewDialog.setAdapter(dF.arrayAdapter, oos);
					dF.menuListViewDialog.setCancelable(true);
					dF.menuListViewDialog.show();
				}
				

			}
		});
	    
	    
	    
	 
	    return rowView;
	 }
	 
	 


	
}
