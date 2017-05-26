package com.shimmerresearch.shimmerserviceexample;


import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceConfigFragment extends Fragment {

    DeviceConfigListAdapter expandListAdapter;
    ExpandableListView expandListView;

    public DeviceConfigFragment() {
        // Required empty public constructor
    }

    public static DeviceConfigFragment newInstance() {
        DeviceConfigFragment fragment = new DeviceConfigFragment();
        return fragment;
    }

    public void buildDeviceConfigList(ShimmerDevice shimmerDevice, Context context) {

        final Map<String, ConfigOptionDetailsSensor> configOptionsMap = shimmerDevice.getConfigOptionsMap();
        final ShimmerDevice shimmerDeviceClone = shimmerDevice.deepClone();
        Map<Integer, SensorDetails> sensorMap = shimmerDevice.getSensorMap();
        List<String> listOfKeys = new ArrayList<String>();
        for (SensorDetails sd:sensorMap.values()) {
            if (sd.mSensorDetailsRef.mListOfConfigOptionKeysAssociated!=null && sd.isEnabled()) {
                listOfKeys.addAll(sd.mSensorDetailsRef.mListOfConfigOptionKeysAssociated);
            }
        }
        final CharSequence[] cs = listOfKeys.toArray(new CharSequence[listOfKeys.size()]);

        expandListAdapter = new DeviceConfigListAdapter(context, listOfKeys, configOptionsMap, shimmerDevice, shimmerDeviceClone);
        expandListView = (ExpandableListView) getView().findViewById(R.id.expandable_listview);
        expandListView.setAdapter(expandListAdapter);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_config, null);
    }

}
