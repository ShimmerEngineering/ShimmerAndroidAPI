package com.shimmerresearch.android.guiUtilities;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.androidinstrumentdriver.R;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.KeyEvent.KEYCODE_ENTER;

/**
 * Created by Jos on 25/5/2017.
 */

public class DeviceConfigListAdapter extends BaseExpandableListAdapter {

    private Context context;
    List<String> expandableListTitle;
    ShimmerDevice cloneDevice;
    ShimmerDevice shimmerDevice;
    private HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

    //HashMap where <Key(sensor option name), Current setting>
    private HashMap<String, String> currentSettingsMap = new HashMap<String, String>();


    private String getConfigValueLabelFromConfigLabel(String label){
        ConfigOptionDetailsSensor cods = cloneDevice.getConfigOptionsMap().get(label);
        int currentConfigInt = (int) cloneDevice.getConfigValueUsingConfigLabel(label);
        int index = -1;
        Integer[] values = cods.getConfigValues();
        String[] valueLabels = cods.getGuiValues();
        for (int i=0;i<values.length;i++){
            if (currentConfigInt==values[i]){
                index=i;
            }
        }
        if (index==-1){
            System.out.println();
            return "";
        }
        return valueLabels[index];
    }

    public DeviceConfigListAdapter(Context activityContext, List<String> list, Map<String, ConfigOptionDetailsSensor> configOptionsMap, ShimmerDevice device, ShimmerDevice shimmerDeviceClone) {
        context = activityContext;
        expandableListTitle = list;
        cloneDevice = shimmerDeviceClone;
        shimmerDevice = device;

        for(String key : list) {    //TODO: Place this in DeviceConfigFragment
            ConfigOptionDetailsSensor cods = configOptionsMap.get(key);

            if (cods != null) {
                String[] cs = cods.getGuiValues();
                if (cods.mGuiComponentType == ConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX) {
                    if (cs != null) {
                        //Put the expandable list child values in the HashMap
                        List<String> csList = Arrays.asList(cs);
                        expandableListDetail.put(key, csList);

                        //Get the current setting from the configOptionsMap
                        Object returnedValue = shimmerDevice.getConfigValueUsingConfigLabel(key);
                        if (returnedValue != null) {
                            int configValue = (int) returnedValue;
                            int itemIndex = Arrays.asList(configOptionsMap.get(key).getConfigValues()).indexOf(configValue);
                            String currentSetting = Arrays.asList(configOptionsMap.get(key).getGuiValues()).get(itemIndex);
                            currentSettingsMap.put(key, currentSetting);
                        }
                    } else {
                        Log.e("SHIMMER", "cs is null for key " + key);
                    }
                } else if (cods.mGuiComponentType == ConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD) {
                    //A text field is needed as this config setting can be assigned any value
                    String value = (String) shimmerDevice.getConfigValueUsingConfigLabel(key);
                    String[] textField = {"TEXTFIELD"};
                    List<String> csList = Arrays.asList(textField);
                    expandableListDetail.put(key, csList);
                    currentSettingsMap.put(key, value);
                }
            }
        }

    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ViewGroup parentView = parent;
        final String expandedListText = (String) getChild(groupPosition, childPosition);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(expandedListText.contains("TEXTFIELD")) {    //The config option can be any value, needs a TextField
            if(convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_item_textfield, null);
            } else {
                if(convertView.findViewById(R.id.editText) == null) {
                    convertView = layoutInflater.inflate(R.layout.list_item_textfield, null);
                }
            }
            final EditText editText = (EditText) convertView.findViewById(R.id.editText);

            Object textFieldValue = cloneDevice.getConfigValueUsingConfigLabel((String)getGroup(groupPosition));
            editText.setText((String)textFieldValue);
            editText.append("");

            editText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if(keyCode == KEYCODE_ENTER) {
                        cloneDevice.setConfigValueUsingConfigLabel((String)getGroup(groupPosition), editText.getText().toString());
                        notifyDataSetChanged();
                    }
                    return false;
                }
            });

        }
        else {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_item, null);
            } else {
                if(convertView.findViewById(R.id.expandedListItem) == null) {
                    convertView = layoutInflater.inflate(R.layout.list_item, null);
                }
            }
            CheckedTextView expandedListTextView = (CheckedTextView) convertView.findViewById(R.id.expandedListItem);
            expandedListTextView.setText(expandedListText);

            String configValueLabel = (String) getGroup(groupPosition);
            String valueLabel = getConfigValueLabelFromConfigLabel(configValueLabel);

            if(valueLabel.equals(expandedListText)) {
                expandedListTextView.setChecked(true);
            } else {
                expandedListTextView.setChecked(false);
            }

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
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_group, null);
            } else if(convertView.findViewById(R.id.listTitle) == null) {
                convertView = layoutInflater.inflate(R.layout.list_group, null);
            }
            TextView listTitleTextView = (TextView) convertView.findViewById(R.id.listTitle);
            listTitleTextView.setTypeface(null, Typeface.BOLD);
            listTitleTextView.setText(listTitle);

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public void replaceCurrentSetting(String key, String newSetting) {
        currentSettingsMap.remove(key);
        currentSettingsMap.put(key, newSetting);
    }

    public void updateCloneDevice(ShimmerDevice newShimmerCloneDevice) {
        cloneDevice = newShimmerCloneDevice;
    }

}
