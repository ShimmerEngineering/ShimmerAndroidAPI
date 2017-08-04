package com.shimmerresearch.android.guiUtilities.supportfragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.shimmerresearch.androidinstrumentdriver.R;
import com.shimmerresearch.driver.ShimmerDevice;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConnectedShimmersListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConnectedShimmersListFragment extends ListFragment {

    OnShimmerDeviceSelectedListener mCallBack;
    String selectedDeviceAddress, selectedDeviceName;
    final static String LOG_TAG = "SHIMMER";
    ListView savedListView = null;
    ArrayAdapter<String> savedListAdapter = null;
    int selectedItemPos = -1;
    List<ShimmerDevice> shimmerDeviceList;
    Context context;
    int selectedDevicePos = -1;


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
    }

    public static ConnectedShimmersListFragment newInstance() {
        ConnectedShimmersListFragment fragment = new ConnectedShimmersListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void buildShimmersConnectedListView(final List<ShimmerDevice> deviceList, final Context context) {
        if(isVisible()){
        shimmerDeviceList = deviceList;
        this.context = context;
        if(deviceList == null) {
            //String[] displayList = {"Service not yet initialised"};
            String[] displayList = {"No devices connected"};
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

            ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(context, R.layout.simple_list_item_multiple_choice_force_black_text, displayList);

            //Set the list of devices to be displayed in the Fragment
            setListAdapter(listAdapter);

            final ListView listView = getListView();
            listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectedItemPos = position;

                    selectedDeviceAddress = macList[position];
                    selectedDeviceName = nameList[position];
                    selectedDevicePos = position;

                    try {
                        mCallBack.onShimmerDeviceSelected(macList[position], nameList[position]);
                    } catch (ClassCastException cce) {

                    }
                }
            });

            //Save the listView so that it can be restored in onCreateView when returning to the Fragment.
            savedListView = listView;
            savedListAdapter = listAdapter;

            //Ensure that the selected item's checkbox is checked
            if (selectedDeviceAddress != null) {
                for (int i = 0; i < listView.getAdapter().getCount(); i++) {

                    View view = getViewByPosition(i, listView);
                    CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(android.R.id.text1);
                    if (checkedTextView != null) {
                        String text = checkedTextView.getText().toString();
                        if (text.contains(selectedDeviceAddress)) {
                            listView.setItemChecked(i, true);
                        } else {
                            listView.setItemChecked(i, false);
                        }
                    }

                }
            }

        }
        }
    }

    @Override
    public void onResume() {
        if(savedListView != null && savedListAdapter != null) {
            buildShimmersConnectedListView(shimmerDeviceList, context);
        } else {
            buildShimmersConnectedListView(null, getActivity().getApplicationContext());
        }
        super.onResume();
    }

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

}
