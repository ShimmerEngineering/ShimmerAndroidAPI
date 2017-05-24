package com.shimmerresearch.shimmerserviceexample;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.shimmerresearch.driver.ShimmerDevice;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConnectedShimmersListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConnectedShimmersListFragment extends ListFragment {

    OnShimmerDeviceSelectedListener mCallBack;
    Activity mActivity;

    public ConnectedShimmersListFragment() {
        // Required empty public constructor
    }

    //Container Activity must implement this interface
    public interface OnShimmerDeviceSelectedListener {
        public void onShimmerDeviceSelected(String macAddress, String deviceName);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //Ensure that the container activity has implemented the callback interface.
        try {
            mCallBack = (OnShimmerDeviceSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnShimmerDeviceSelectedListener");
        }
        mActivity = activity;
    }

    public static ConnectedShimmersListFragment newInstance() {
        ConnectedShimmersListFragment fragment = new ConnectedShimmersListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void buildShimmersConnectedListView(final List<ShimmerDevice> deviceList, final Context context) {
        if(deviceList == null) {    //No Shimmers connected
            String[] displayList = {"Service not yet initialised"};
            ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, displayList);
            setListAdapter(listAdapter);
        }
        else if(deviceList.isEmpty()) {
            String[] displayList = {"No Shimmers connected"};
            ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, displayList);
            setListAdapter(listAdapter);
        }
        else {
            final String[] nameList = new String[deviceList.size()];
            final String[] macList = new String[deviceList.size()];
            final String[] displayList = new String[deviceList.size()];

            for (int i = 0; i < nameList.length; i++) {
                nameList[i] = deviceList.get(i).getShimmerUserAssignedName();
                macList[i] = deviceList.get(i).getMacId();
                displayList[i] = nameList[i] + "\n" + macList[i];
            }

            ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, displayList);

            //Set the list of devices to be displayed in the Fragment
            setListAdapter(listAdapter);

            final ListView listView = getListView();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //Highlight the currently selected ShimmerDevice
                    view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_light));

                    //Set all other backgrounds to white (clearing previous highlight, if any)
                    for (int i = 0; i < listView.getAdapter().getCount(); i++) {
                        if (i != position) {
                            View v = listView.getChildAt(i);
                            v.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
                        }
                    }

                    try {
                        mCallBack.onShimmerDeviceSelected(macList[position], nameList[position]);
                    } catch (ClassCastException cce) {

                    }
                }
            });
        }

    }

}
