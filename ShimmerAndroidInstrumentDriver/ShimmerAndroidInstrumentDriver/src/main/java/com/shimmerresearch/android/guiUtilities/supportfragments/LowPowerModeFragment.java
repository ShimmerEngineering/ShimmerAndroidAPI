package com.shimmerresearch.android.guiUtilities.supportfragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.ListFragment;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.Shimmer4Android;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.android.shimmerService.ShimmerService;
import com.shimmerresearch.androidradiodriver.Shimmer3BLEAndroid;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.AssembleShimmerConfig;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.sensors.lis2dw12.SensorLIS2DW12;
import com.shimmerresearch.sensors.lisxmdl.SensorLIS3MDL;
import com.shimmerresearch.sensors.lsm6dsv.SensorLSM6DSV;
import com.shimmerresearch.verisense.VerisenseDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LowPowerModeFragment extends ListFragment {
    ShimmerDevice shimmerDeviceClone, originalShimmerDevice;
    ShimmerService shimmerService = null;
    TreeMap<Integer, SensorGroupingDetails> compatibleSensorGroupMap;
    int sensorKeys[];
    SensorsEnabledFragment.OnSensorsSelectedListener mCallback;

    final static String LOG_TAG = "SHIMMER";


    public LowPowerModeFragment() {
        // Required empty public constructor
    }

    public static LowPowerModeFragment newInstance(String param1, String param2) {
        LowPowerModeFragment fragment = new LowPowerModeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public interface OnSensorsSelectedListener {
        public void onSensorsSelected();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (SensorsEnabledFragment.OnSensorsSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnSensorSelectedListener");
        }

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
        String[] values = new String[] {"No device selected, sensors unavailable"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);
    }
    /**
     * Call this method to display the sensors from the selected Shimmer Device
     * @param shimmerDevice
     * @param activityContext
     */
    public void buildLowPowerModeList(final ShimmerDevice shimmerDevice, final Context activityContext,
                                 final ShimmerBluetoothManagerAndroid bluetoothManager) {

        originalShimmerDevice = shimmerDevice;
        shimmerDeviceClone = shimmerDevice.deepClone();
        String[] arrayLowPowerMode = new String[3];

        arrayLowPowerMode[0] = "Enable Mag LP Mode";
        arrayLowPowerMode[1] = "Enable WR Accel LP Mode";
        if(shimmerDeviceClone.getHardwareVersion() == ShimmerVerDetails.HW_ID.SHIMMER_3R){
            arrayLowPowerMode[2] = "Enable LN Accel and Gyro LP Mode";
        }else{
            arrayLowPowerMode[2] = "Enable Gyro LP Mode";
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activityContext, android.R.layout.simple_list_item_multiple_choice, arrayLowPowerMode);

        setListAdapter(adapter);
        final List<Integer> mSelectedItems = new ArrayList();  // Where we track the selected items
        Map<Integer, SensorDetails> sensorMap = shimmerDeviceClone.getSensorMap();
        final ListView listView = getListView();
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        if(listView.getFooterViewsCount() == 0) {   //Only add the button if there is no existing button
            //Create button in the ListView footer
            Button button = new Button(activityContext);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    Toast.makeText(activityContext, "Writing config to Shimmer...", Toast.LENGTH_SHORT).show();
                    if (shimmerDevice == null) {
                        Toast.makeText(activityContext, "Error! The Shimmer Device is null!", Toast.LENGTH_SHORT).show();
                    }
                    if (shimmerDeviceClone != null) {

                        List<ShimmerDevice> cloneList = new ArrayList<ShimmerDevice>();
                        cloneList.add(0, shimmerDeviceClone);
                        //TODO: Change this when AssembleShimmerConfig has been updated:
                        AssembleShimmerConfig.generateMultipleShimmerConfig(cloneList, Configuration.COMMUNICATION_TYPE.BLUETOOTH);

                        if (shimmerDevice instanceof Shimmer || shimmerDevice instanceof VerisenseDevice || shimmerDevice instanceof Shimmer3BLEAndroid) {

                            bluetoothManager.configureShimmer(shimmerDeviceClone);
                            mCallback.onSensorsSelected();

                        } else if (shimmerDevice instanceof Shimmer4Android) {

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

        final int lpModeCount = 3;
        updateCheckboxes(listView, lpModeCount);

        //Set the listener for ListView item clicks
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(android.R.id.text1);
                CharSequence cs = checkedTextView.getText();
                String enableLowPowerModeName = cs.toString();

                if(enableLowPowerModeName.contains("Mag")){
                    shimmerDeviceClone.setConfigValueUsingConfigLabel(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG, SensorLIS3MDL.GuiLabelConfig.LIS3MDL_MAG_LP, checkedTextView.isChecked());
                }else if(enableLowPowerModeName.contains("Gyro")){
                    shimmerDeviceClone.setConfigValueUsingConfigLabel(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM6DSV_GYRO, SensorLSM6DSV.GuiLabelConfig.LSM6DSV_GYRO_LPM, checkedTextView.isChecked());
                }else if(enableLowPowerModeName.contains("Accel")){
                    shimmerDeviceClone.setConfigValueUsingConfigLabel(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2DW12_ACCEL_WR, SensorLIS2DW12.GuiLabelConfig.LIS2DW12_ACCEL_LPM, checkedTextView.isChecked());
                }

                updateCheckboxes(listView, lpModeCount);
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

    public void setShimmerService(ShimmerService service) {
        shimmerService = service;
    }

    /**
     * Updates the state of the checkboxes in the ListView
     * @param listView
     * @param count
     */
    private void updateCheckboxes(ListView listView, int count) {

        boolean isLowPowerMagEnabled = Boolean.valueOf(shimmerDeviceClone.getConfigGuiValueUsingConfigLabel(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS3MDL_MAG, SensorLIS3MDL.GuiLabelConfig.LIS3MDL_MAG_LP));
        listView.setItemChecked(0, isLowPowerMagEnabled);
        boolean isLowPowerWRAccelEnabled = Boolean.valueOf(shimmerDeviceClone.getConfigGuiValueUsingConfigLabel(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LIS2DW12_ACCEL_WR, SensorLIS2DW12.GuiLabelConfig.LIS2DW12_ACCEL_LPM));
        listView.setItemChecked(1, isLowPowerWRAccelEnabled);
        boolean isLowPowerGyroEnabled = Boolean.valueOf(shimmerDeviceClone.getConfigGuiValueUsingConfigLabel(Configuration.Shimmer3.SENSOR_ID.SHIMMER_LSM6DSV_GYRO, SensorLSM6DSV.GuiLabelConfig.LSM6DSV_GYRO_LPM));
        listView.setItemChecked(2, isLowPowerGyroEnabled);
    }
}
