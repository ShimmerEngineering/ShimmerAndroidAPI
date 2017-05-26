package com.shimmerresearch.shimmerserviceexample;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jos on 25/5/2017.
 */

public class DeviceConfigListAdapter extends BaseExpandableListAdapter {

    private Context context;
    List<String> expandableListTitle;
    private HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

    //HashMap where <Key(sensor config option), Current setting>
    private HashMap<String, String> currentSettingsMap = new HashMap<String, String>();


    public DeviceConfigListAdapter(Context activityContext, List<String> list, Map<String, ConfigOptionDetailsSensor> configOptionsMap, ShimmerDevice shimmerDevice, ShimmerDevice shimmerDeviceClone) {
        context = activityContext;
        expandableListTitle = list;

        for(String key : list) {    //TODO: Place this in DeviceConfigFragment
            ConfigOptionDetailsSensor cods = configOptionsMap.get(key);
            String[] cs = cods.getGuiValues();
            if(cs != null) {
                //Put the expandable list child values in the HashMap
                List<String> csList = Arrays.asList(cs);
                expandableListDetail.put(key, csList);

                //Get the current setting from the configOptionsMap
                Object returnedValue = shimmerDevice.getConfigValueUsingConfigLabel(key);
                if(returnedValue != null) {
                    int configValue = (int) returnedValue;
                    int itemIndex = Arrays.asList(configOptionsMap.get(key).getConfigValues()).indexOf(configValue);
                    String currentSetting = Arrays.asList(configOptionsMap.get(key).getGuiValues()).get(itemIndex);
                    currentSettingsMap.put(key, currentSetting);
                }


            } else {
                Log.e("SHIMMER", "cs is null!!! with key " + key);
            }
        }


    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String expandedListText = (String) getChild(groupPosition, childPosition);
        if(convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item, null);
        }
        CheckedTextView expandedListTextView = (CheckedTextView) convertView.findViewById(R.id.expandedListItem);
        expandedListTextView.setText(expandedListText);

        if(currentSettingsMap.get(getGroup(groupPosition)) == expandedListText) {
            Log.i("JOS", currentSettingsMap.get(getGroup(groupPosition)) + " == " + expandedListText);
            Log.i("JOS", "groupPosition: " + groupPosition + "\tchildPosition: " + childPosition);
            expandedListTextView.setChecked(true);
        } else {
            expandedListTextView.setChecked(false);
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public int getGroupCount() {
        return expandableListTitle.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return expandableListDetail.get(expandableListTitle.get(groupPosition)).size();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return expandableListDetail.get(expandableListTitle.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return expandableListTitle.get(groupPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(groupPosition);
        if(convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, null);
        }
        TextView listTitleTextView = (TextView) convertView.findViewById(R.id.listTitle);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);

        return convertView;
    }


}
