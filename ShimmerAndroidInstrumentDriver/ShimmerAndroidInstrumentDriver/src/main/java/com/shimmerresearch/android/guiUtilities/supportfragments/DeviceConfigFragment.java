package com.shimmerresearch.android.guiUtilities.supportfragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.androidinstrumentdriver.R;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.AssembleShimmerConfig;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeviceConfigFragment extends Fragment {

    DeviceConfigListAdapter expandListAdapter;
    ExpandableListView expandListView;
    ShimmerDevice shimmerDeviceClone;

    public DeviceConfigFragment() {
        // Required empty public constructor
    }

    public static DeviceConfigFragment newInstance() {
        DeviceConfigFragment fragment = new DeviceConfigFragment();
        return fragment;
    }

    public void buildDeviceConfigList(final ShimmerDevice shimmerDevice, final Context context,
                                      final ShimmerBluetoothManagerAndroid bluetoothManager) {

        final Map<String, ConfigOptionDetailsSensor> configOptionsMap = shimmerDevice.getConfigOptionsMap();
        shimmerDeviceClone = shimmerDevice.deepClone();
        Map<Integer, SensorDetails> sensorMap = shimmerDevice.getSensorMap();
        List<String> listOfKeys = new ArrayList<String>();
        for (SensorDetails sd:sensorMap.values()) {
            if (sd.mSensorDetailsRef.mListOfConfigOptionKeysAssociated!=null && sd.isEnabled()) {
                listOfKeys.addAll(sd.mSensorDetailsRef.mListOfConfigOptionKeysAssociated);
            }
        }

        List<String> keysToRemove = new ArrayList<String>();

        for(String key : listOfKeys) {
            ConfigOptionDetailsSensor cods = configOptionsMap.get(key);
            if(cods == null) {
                keysToRemove.add(key);
            }
        }

        for(String key : keysToRemove) {
            listOfKeys.remove(key);
        }

        //        final CharSequence[] cs = listOfKeys.toArray(new CharSequence[listOfKeys.size()]);

        expandListAdapter = new DeviceConfigListAdapter(context, listOfKeys, configOptionsMap, shimmerDevice, shimmerDeviceClone);
        expandListView = (ExpandableListView) getView().findViewById(R.id.expandable_listview);
        expandListView.setAdapter(expandListAdapter);

        expandListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                final int editTextGroupPosition = groupPosition;
                if(v.findViewById(R.id.expandedListItem) != null) { //The item that was clicked is a checkbox
                    CheckedTextView checkedTextView = (CheckedTextView) v.findViewById(R.id.expandedListItem);
                    if(checkedTextView.isChecked()) {
                        checkedTextView.setChecked(false);
                    } else {
                        checkedTextView.setChecked(true);
                    }

                    String newSetting = (String) expandListAdapter.getChild(groupPosition, childPosition);
                    String keySetting = (String) expandListAdapter.getGroup(groupPosition);

                    //Write the setting to the Shimmer Clone
                    final ConfigOptionDetailsSensor cods = configOptionsMap.get(keySetting);

                    shimmerDeviceClone.setConfigValueUsingConfigLabel(keySetting, cods.mConfigValues[childPosition]);

                    expandListAdapter.replaceCurrentSetting(keySetting, newSetting);
                    expandListAdapter.notifyDataSetChanged();   //Tells the list to redraw itself with the new information

                }
                return false;
            }
        });

//        expandListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
//            @Override
//            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
//                if (v.findViewById(R.id.saveButton) != null) {
//                    Button writeConfigButton = (Button) v.findViewById(R.id.saveButton);
//                    Button resetListButton = (Button) v.findViewById(R.id.resetButton);
//
//                    writeConfigButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Toast.makeText(context, "Writing config to Shimmer...", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//                    resetListButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            shimmerDeviceClone = shimmerDevice.deepClone();
//                            expandListAdapter.updateCloneDevice(shimmerDeviceClone);
//                            expandListAdapter.notifyDataSetChanged();
//                            Toast.makeText(context, "Settings have been reset", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
//
//                return false;
//            }
//        });

        //Only add the buttons if they haven't been added before:
        if(expandListView.getFooterViewsCount() == 0) {
            LinearLayout buttonLayout = new LinearLayout(context);
            buttonLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
            Button writeConfigButton = new Button(context);
            Button resetListButton = new Button(context);
            writeConfigButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Writing config to Shimmer...", Toast.LENGTH_SHORT).show();
                    List<ShimmerDevice> cloneList = new ArrayList<ShimmerDevice>();
                    cloneList.add(0, shimmerDeviceClone);
                    AssembleShimmerConfig.generateMultipleShimmerConfig(cloneList, Configuration.COMMUNICATION_TYPE.BLUETOOTH);

                    if(shimmerDeviceClone instanceof Shimmer) {
                        bluetoothManager.configureShimmer(shimmerDeviceClone);
                    }

                }
            });
            resetListButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shimmerDeviceClone = shimmerDevice.deepClone();
                    expandListAdapter.updateCloneDevice(shimmerDeviceClone);
                    expandListAdapter.notifyDataSetChanged();
                    Toast.makeText(context, "Settings have been reset", Toast.LENGTH_SHORT).show();
                }
            });
            writeConfigButton.setText("Write config to Shimmer");
            resetListButton.setText("Reset settings");
            buttonLayout.addView(resetListButton);
            buttonLayout.addView(writeConfigButton);
            expandListView.addFooterView(buttonLayout);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_config, null);
    }
}
