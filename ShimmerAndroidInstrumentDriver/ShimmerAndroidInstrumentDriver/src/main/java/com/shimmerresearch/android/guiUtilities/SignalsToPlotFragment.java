package com.shimmerresearch.android.guiUtilities;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.androidplot.xy.XYPlot;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.android.shimmerService.ShimmerService;
import com.shimmerresearch.androidinstrumentdriver.R;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Class which displays a list of available signals that can be selected for plotting.
 */
public class SignalsToPlotFragment extends ListFragment {

    ListView listView;
    LinkedHashMap<String, ChannelDetails> channelsMap;
    List<String[]> listOfEnabledChannelsAndFormats = new ArrayList<String[]>();
    List<String[]> mList = new ArrayList<String[]>();
    ShimmerService shimmerService;
    XYPlot dynamicPlot;
    Context context;
    String bluetoothAddress;
    ShimmerDevice shimmerDevice = null;

    final static String LOG_TAG = "SignalsToPlotFragment";

    public SignalsToPlotFragment() {
        // Required empty public constructor
    }

    /**
     * Display a default message when the fragment is first created
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        listView = getListView();
        super.onActivityCreated(savedInstanceState);
        String[] defaultMessage = {"No device selected, signals unavailable"};
        ArrayAdapter<String> defaultAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, defaultMessage);
        setListAdapter(defaultAdapter);
    }

    public static SignalsToPlotFragment newInstance() {

        Bundle args = new Bundle();

        SignalsToPlotFragment fragment = new SignalsToPlotFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        if(shimmerDevice != null) {

        }

        super.onResume();
    }

    /**
     * Displays the list of signals which are available for plotting
     * @param context
     * @param service
     * @param bluetoothAddress
     * @param plot
     */
    public void buildSignalsToPlotList(Context context, final ShimmerService service, final String bluetoothAddress, final XYPlot plot) {

        this.context = context;
        shimmerService = service;
        dynamicPlot = plot;
        ShimmerBluetoothManagerAndroid btManager = shimmerService.getBluetoothManager();
        final ShimmerDevice device = btManager.getShimmer(bluetoothAddress);

        if(device.isStreaming()) {

            device.getListofEnabledChannelSignalsandFormats();
            channelsMap = device.getMapOfEnabledChannelsForStreaming();
            List<String> sensorList = new ArrayList<String>();
            List<String[]> listOfChannels = shimmerService.getListofEnabledSensorSignals(bluetoothAddress);
            List<String> sensorList2 = new ArrayList<String>();

            Iterator<ChannelDetails> iterator = channelsMap.values().iterator();
            while(iterator.hasNext()) {
                ChannelDetails details = iterator.next();
                listOfEnabledChannelsAndFormats.addAll(details.getListOfChannelSignalsAndFormats());
            }

            for(int i=0;i<listOfChannels.size();i++) {
                sensorList2.add(joinStrings(listOfChannels.get(i)));
            }

            int p=0;
            String deviceName = device.getShimmerUserAssignedName();
            final List<String[]> mList = new ArrayList<>();
            for(ChannelDetails details : channelsMap.values()) {

                List<ChannelDetails.CHANNEL_TYPE> listOfChannelTypes = details.mListOfChannelTypes;

                for(int a=0; a<listOfChannelTypes.size(); a++) {
                    String format = listOfChannelTypes.get(a).name();
                    if(format.contains("UNCAL")) {
                        String[] temp = new String[] {deviceName, details.getChannelObjectClusterName(), format, details.mDefaultUncalUnit};
                        mList.add(p, temp);
                        p++;
                    } else {
                        String[] temp = new String[] {deviceName, details.getChannelObjectClusterName(), format, details.mDefaultCalUnits};
                        mList.add(p, temp);
                        p++;
                    }
                }
            }

            for(int i=0; i<mList.size(); i++) {
                String[] array = mList.get(i);
                //Remove the device name and units before putting into sensorList:
                String s = array[1] + " " + array[2];
                sensorList.add(i, s);
            }

            this.mList = mList;

            //final String[] sensorNames = sensorList.toArray(new String[sensorList.size()]);
            final String[] sensorNames = sensorList.toArray(new String[sensorList.size()]);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.simple_list_item_multiple_choice_force_black_text, android.R.id.text1, sensorNames);
            setListAdapter(adapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            updateCheckboxes();

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    if(!shimmerService.mPlotManager.checkIfPropertyExist(mList.get(position))) {
                        try {
                            if(dynamicPlot == null) {
                                Log.e(LOG_TAG, "dynamicPlot is null!");
                            }
                            shimmerService.mPlotManager.addSignal(mList.get(position), dynamicPlot);
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Error! Could not add signal: " + e);
                            e.printStackTrace();
                        }
                    }
                    else {
                        shimmerService.mPlotManager.removeSignal(mList.get(position));
                    }
                }
            });

        }

/*
        else {
            Toast.makeText(context, "Error! Device is not streaming!", Toast.LENGTH_SHORT).show();
        }
*/

    }

    public void updateCheckboxes() {
        for(int i=0; i<mList.size(); i++) {
            if(shimmerService.mPlotManager.checkIfPropertyExist(mList.get(i))) {
                listView.setItemChecked(i, true);
            } else {
                listView.setItemChecked(i, false);
            }
        }
    }

    public static String joinStrings(String[] a){
        String js="";
        for (int i=0;i<a.length;i++){
            if (i==0){
                js = a[i];
            } else{
                js = js + " " + a[i];
            }
        }
        return js;
    }

    public void setDeviceNotStreamingView() {
        String[] notStreamingMsg = new String[]{"Device not streaming", "Signals to plot can only be displayed when device is streaming"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, notStreamingMsg);
        setListAdapter(adapter);
    }


}
