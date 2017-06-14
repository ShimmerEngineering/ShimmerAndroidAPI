package com.shimmerresearch.shimmerserviceexample;

import android.app.AlertDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.Shimmer4Android;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.android.shimmerService.ShimmerService;
import com.shimmerresearch.bluetooth.BluetoothProgressReportAll;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.shimmer4sdk.Shimmer4;
import com.shimmerresearch.driverUtilities.AssembleShimmerConfig;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.exgConfig.ExGConfigOptionDetails;
import com.shimmerresearch.managers.bluetoothManager.ShimmerBluetoothManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class SensorsEnabledFragment extends ListFragment {

    ShimmerDevice cloneDevice, originalShimmerDevice;
    ShimmerService shimmerService = null;
    TreeMap<Integer, SensorGroupingDetails> compatibleSensorGroupMap;
    int sensorKeys[];

    final static String LOG_TAG = "SHIMMER";


    public SensorsEnabledFragment() {
        // Required empty public constructor
    }

    public static SensorsEnabledFragment newInstance(String param1, String param2) {
        SensorsEnabledFragment fragment = new SensorsEnabledFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * Displays a default message when the fragment is first displayed
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String[] values = new String[] {"No Shimmer selected", "Sensors unavailable"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);
    }

    /**
     * Call this method to display the sensors from the selected Shimmer Device
     * @param device
     * @param activityContext
     */
    public void buildSensorsList(final ShimmerDevice device, final Context activityContext) {

        originalShimmerDevice = device;
        cloneDevice = device.deepClone();

        //Get the list of sensor groups the device is compatible with and store it in an ArrayList
        compatibleSensorGroupMap = new TreeMap<Integer, SensorGroupingDetails>();
        TreeMap<Integer, SensorGroupingDetails> groupMap = cloneDevice.getSensorGroupingMap();
        for(Map.Entry<Integer, SensorGroupingDetails> entry : groupMap.entrySet()) {
            if(isSensorGroupCompatible(cloneDevice, entry.getValue())) {
              compatibleSensorGroupMap.put(entry.getKey(), entry.getValue());
            }
        }

        final List<Integer> mSelectedItems = new ArrayList();  // Where we track the selected items
        Map<Integer, SensorDetails> sensorMap = cloneDevice.getSensorMap();
        int count = 0;
        for (SensorDetails sd : sensorMap.values()) {
            if (cloneDevice.isVerCompatibleWithAnyOf(sd.mSensorDetailsRef.mListOfCompatibleVersionInfo)) {
                count++;
            }

        }
        String[] arraySensors = new String[count];
        final boolean[] listEnabled = new boolean[count];
        sensorKeys = new int[count];
        count = 0;

        for (int key : sensorMap.keySet()) {
            SensorDetails sd = sensorMap.get(key);
            if (cloneDevice.isVerCompatibleWithAnyOf(sd.mSensorDetailsRef.mListOfCompatibleVersionInfo)) {
                arraySensors[count] = sd.mSensorDetailsRef.mGuiFriendlyLabel;
                listEnabled[count] = sd.isEnabled();
                sensorKeys[count] = key;
                count++;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activityContext, android.R.layout.simple_list_item_multiple_choice, arraySensors);
        setListAdapter(adapter);

        final ListView listView = getListView();
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        if(listView.getFooterViewsCount() == 0) {   //Only add the button if there is no existing button
            //Create button in the ListView footer
            Button button = new Button(activityContext);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    Toast.makeText(activityContext, "Writing config to Shimmer...", Toast.LENGTH_SHORT).show();
                    if (device == null) {
                        Toast.makeText(activityContext, "Error! The Shimmer Device is null!", Toast.LENGTH_SHORT).show();
                    }
                    if (cloneDevice != null) {
                        for (int selected : mSelectedItems) {
                            cloneDevice.setSensorEnabledState((int) sensorKeys[selected], listEnabled[selected]);
                        }

                        List<ShimmerDevice> cloneList = new ArrayList<ShimmerDevice>();
                        cloneList.add(0, cloneDevice);
                        AssembleShimmerConfig.generateMultipleShimmerConfig(cloneList, Configuration.COMMUNICATION_TYPE.BLUETOOTH);

                        if (device instanceof Shimmer) {
                            //((Shimmer)device).writeConfigBytes(shimmerDeviceClone.getShimmerInfoMemBytes());
                            /*try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }*/

                            //((Shimmer) device).writeEnabledSensors(cloneDevice.getEnabledSensors());

                            configureShimmers(cloneList);

                        } else if (device instanceof Shimmer4Android) {
                            //((Shimmer4Android)device).writeConfigBytes(shimmerDeviceClone.getShimmerInfoMemBytes());
                        }
                    } else {
                        Toast.makeText(activityContext, "Error! Shimmer Device clone is null!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            button.setText("Write config");
            button.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.WRAP_CONTENT, ListView.LayoutParams.WRAP_CONTENT));
            listView.addFooterView(button);
        }

        //Set sensors which are already enabled in the Shimmer clone to be checked in the ListView
        updateCheckboxes(listView, count);
        final int countUpdate = count;

        //Set the listener for ListView item clicks
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                List<SensorDetails> enabledSensorsList = cloneDevice.getListOfEnabledSensors();
                CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(android.R.id.text1);
                CharSequence cs = checkedTextView.getText();
                String sensorName = cs.toString();

                if(checkedTextView.isChecked()) {
                    cloneDevice.setSensorEnabledState(sensorKeys[position], true);
                } else {
                    cloneDevice.setSensorEnabledState(sensorKeys[position], false);
                }

                updateCheckboxes(listView, countUpdate);

            }
        });

    }

    /**
     * Method to get the View from a position in the ListView, taking into account the constantly
     * changing index of the ListView as it is scrolled.
     * @param pos
     * @param listView
     * @return
     */
    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    protected boolean isSensorGroupCompatible(ShimmerDevice device, SensorGroupingDetails groupDetails) {
        List<ShimmerVerObject> listOfCompatibleVersionInfo = groupDetails.mListOfCompatibleVersionInfo;
        return device.isVerCompatibleWithAnyOf(listOfCompatibleVersionInfo);
    }

    public void setShimmerService(ShimmerService service) {
        shimmerService = service;
    }

    /**
     * Updates the state of the checkboxes in the ListView
     * @param listView
     * @param count
     */
    private void updateCheckboxes(ListView listView, int count) {
        for(int i=0; i<count; i++) {
            View v = getViewByPosition(i, listView);
            CheckedTextView cTextView = (CheckedTextView) v.findViewById(android.R.id.text1);
            if(cTextView != null) {
                if (cloneDevice.isSensorEnabled(sensorKeys[i])) {
                    listView.setItemChecked(i, true);
                }
                else {
                    listView.setItemChecked(i, false);
                }
            } else {
                Log.e(LOG_TAG, "CheckedTextView is null!");
            }
        }
    }

    /**
     * Writes the config from the clone device to the physical device
     * @param listOfShimmerClones
     */
    public void configureShimmers(List<ShimmerDevice> listOfShimmerClones){

        for (ShimmerDevice cloneShimmer:listOfShimmerClones){
            if (cloneShimmer instanceof ShimmerBluetooth){
                ShimmerBluetooth cloneShimmerCast = (ShimmerBluetooth) cloneShimmer;

                if (cloneShimmerCast.getHardwareVersion()== ShimmerVerDetails.HW_ID.SHIMMER_3){

                    //ShimmerBluetooth originalShimmer = getShimmerDevice(cloneShimmerCast.getComPort());
                    if (originalShimmerDevice instanceof ShimmerBluetooth){
                        ShimmerBluetooth originalShimmer = (ShimmerBluetooth) originalShimmerDevice;
                        originalShimmer.operationPrepare();
                        originalShimmer.setSendProgressReport(true);

                        if(originalShimmer.isUseInfoMemConfigMethod()){
                            originalShimmer.writeConfigBytes(cloneShimmerCast.getShimmerInfoMemBytes());
                            // Hack because infomem is getting updated but
                            // enabledsensors aren't getting updated on the Shimmer
                            // and we need an inquiry() to determine packet format
                            // for legacy code
                            originalShimmer.writeEnabledSensors(cloneShimmerCast.getEnabledSensors());
                        }
                        else {
                            //TODO below is writing accel, gyro, mag rate + ExG bytes -> for the moment moved to be the first command and then overwrite other rates below
                            originalShimmer.writeShimmerAndSensorsSamplingRate(cloneShimmerCast.getSamplingRateShimmer());// s3 = 4

                            originalShimmer.writeAccelRange(cloneShimmerCast.getAccelRange());
                            originalShimmer.writeGSRRange(cloneShimmerCast.getGSRRange());
                            originalShimmer.writeGyroRange(cloneShimmerCast.getGyroRange());
                            originalShimmer.writeMagRange(cloneShimmerCast.getMagRange());
                            originalShimmer.writePressureResolution(cloneShimmerCast.getPressureResolution());

                            //set the low power modes here
                            originalShimmer.enableLowPowerAccel(cloneShimmerCast.isLowPowerAccelWR());//3
                            originalShimmer.enableLowPowerGyro(cloneShimmerCast.isLowPowerGyroEnabled());
                            originalShimmer.enableLowPowerMag(cloneShimmerCast.isLowPowerMagEnabled());

                            //TODO Already done in enableLowPowerAccel, enableLowPowerMag and enableLowPowerGyro
                            originalShimmer.writeAccelSamplingRate(cloneShimmerCast.getLSM303DigitalAccelRate());
                            originalShimmer.writeGyroSamplingRate(cloneShimmerCast.getMPU9150GyroAccelRate());
                            originalShimmer.writeMagSamplingRate(cloneShimmerCast.getLSM303MagRate());

                            //						System.out.println("Register1\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(cloneShimmerCast.getEXG1RegisterArray()));
                            //						System.out.println("Register2\t" + UtilShimmer.bytesToHexStringWithSpacesFormatted(cloneShimmerCast.getEXG2RegisterArray()));

                            originalShimmer.writeEXGConfiguration(cloneShimmerCast.getEXG1RegisterArray(), ExGConfigOptionDetails.EXG_CHIP_INDEX.CHIP1);
                            originalShimmer.writeEXGConfiguration(cloneShimmerCast.getEXG2RegisterArray(), ExGConfigOptionDetails.EXG_CHIP_INDEX.CHIP2);

                            originalShimmer.writeInternalExpPower(cloneShimmerCast.getInternalExpPower());
                            originalShimmer.writeShimmerUserAssignedName(cloneShimmerCast.getShimmerUserAssignedName());
                            originalShimmer.writeExperimentName(cloneShimmerCast.getTrialName());
                            originalShimmer.writeConfigTime(cloneShimmerCast.getConfigTime());

                            originalShimmer.writeDerivedChannels(cloneShimmerCast.getDerivedSensors());
                            //originalShimmer.writeDerivedChannels(BTStreamDerivedSensors.ECG2HR_CHIP1_CH2|BTStreamDerivedSensors.ECG2HR_CHIP1_CH1);
                            //setContinuousSync(mContinousSync);

                            originalShimmer.writeEnabledSensors(cloneShimmerCast.getEnabledSensors()); //this should always be the last command
                            //						System.out.println(cloneShimmerCast.getEnabledSensors());
                        }

                        originalShimmer.writeCalibrationDump(cloneShimmerCast.calibByteDumpGenerate());

                        //get instruction stack size
                        originalShimmer.operationStart(ShimmerBluetooth.BT_STATE.CONFIGURING);
                    }
                }
            }
            else if(cloneShimmer instanceof Shimmer4){
                Shimmer4 cloneShimmerCast = (Shimmer4) cloneShimmer;
                if(originalShimmerDevice instanceof Shimmer4){
                    Shimmer4 originalShimmer = (Shimmer4) originalShimmerDevice;

                    originalShimmer.operationPrepare();
//					originalShimmer.setSendProgressReport(true);

                    originalShimmer.writeConfigBytes(cloneShimmerCast.getShimmerInfoMemBytes());
                    originalShimmer.writeCalibrationDump(cloneShimmerCast.calibByteDumpGenerate());

                    originalShimmer.operationStart(ShimmerBluetooth.BT_STATE.CONFIGURING);
                }
            }
        }
    }



}
