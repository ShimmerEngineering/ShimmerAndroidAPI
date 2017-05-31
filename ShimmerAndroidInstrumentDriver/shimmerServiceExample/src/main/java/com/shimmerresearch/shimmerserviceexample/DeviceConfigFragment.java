package com.shimmerresearch.shimmerserviceexample;


import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.view.KeyEvent.KEYCODE_ENTER;

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

    public void buildDeviceConfigList(ShimmerDevice shimmerDevice, final Context context) {

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

        //expandListView.addFooterView(BUTTON);   //TODO: Add the button here...
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
                    shimmerDeviceClone.setConfigValueUsingConfigLabel(keySetting, newSetting);
                    expandListAdapter.replaceCurrentSetting(keySetting, newSetting);
                    expandListAdapter.notifyDataSetChanged();   //Tells the list to redraw itself with the new information

                    int selectedIndex = parent.indexOfChild(v);
                    Toast.makeText(context, "Selected Child at index: " + selectedIndex, Toast.LENGTH_SHORT).show();
//                    expandListAdapter.getChildrenCount(groupPosition);

//                    for(int i=expandListView.getFirstVisiblePosition(); i<expandListView.getLastVisiblePosition(); i++) {
//                        View child = expandListView.getChildAt(i);
//                        CheckedTextView cTextView = ((CheckedTextView) child.findViewById(R.id.expandedListItem));
//                        if (cTextView != null) {
//                            cTextView.setChecked(false);
//                        }
//                    }

                    //parent.getCheckedItemPositions();

                }
                else if(v.findViewById(R.id.editText) != null){    //The item that was clicked is a text field
                    final EditText editText = (EditText) v.findViewById(R.id.editText);
                    final String keySetting = (String) expandListAdapter.getGroup(groupPosition);
                    editText.setOnKeyListener(new View.OnKeyListener() {
                        @Override
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                            if(keyCode == KEYCODE_ENTER) {
                                expandListAdapter.replaceCurrentSetting(keySetting, editText.getText().toString());
                                expandListAdapter.notifyDataSetChanged();
                            }
                            return false;
                        }
                    });

                }
                return false;
            }
        });


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_config, null);
    }




    public View getViewByPosition(int pos, ExpandableListView listView) {
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
