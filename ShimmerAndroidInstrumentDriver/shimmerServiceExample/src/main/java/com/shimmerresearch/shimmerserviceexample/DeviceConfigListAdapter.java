package com.shimmerresearch.shimmerserviceexample;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
    Button writeConfigButton, resetListButton;

    //HashMap where <Key(sensor option name), Current setting>
    private HashMap<String, String> currentSettingsMap = new HashMap<String, String>();


    private String getConfigValueLabelFromConfigLabel(String label){
        ConfigOptionDetailsSensor cods = cloneDevice.getConfigOptionsMap().get(label);
        int currentConfigInt = (int) cloneDevice.getConfigValueUsingConfigLabel(label);
        int index = -1;
        Integer[] values = cods.getConfigValues();
        String[] valuelabels = cods.getGuiValues();
        for (int i=0;i<values.length;i++){
            if (currentConfigInt==values[i]){
                index=i;
            }
        }
        if (index==-1){
            System.out.println();
            return "";
        }
        return valuelabels[index];
    }

    public DeviceConfigListAdapter(Context activityContext, List<String> list, Map<String, ConfigOptionDetailsSensor> configOptionsMap, ShimmerDevice device, ShimmerDevice shimmerDeviceClone) {
        context = activityContext;
        expandableListTitle = list;
        cloneDevice = shimmerDeviceClone;
        shimmerDevice = device;
        //TODO: expandableListTitle.add(index, "FOOTER");

        for(String key : list) {    //TODO: Place this in DeviceConfigFragment
            ConfigOptionDetailsSensor cods = configOptionsMap.get(key);
            String[] cs = cods.getGuiValues();
            if(cods.mGuiComponentType == ConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX) {
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
                    Log.e("SHIMMER", "cs is null!!! with key " + key);
                }
            }
            else if(cods.mGuiComponentType == ConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD) {
                //A text field is needed as this config setting can be assigned any value
                String value = (String) shimmerDevice.getConfigValueUsingConfigLabel(key);
                String[] textField = {"TEXTFIELD"};
                List<String> csList = Arrays.asList(textField);
                expandableListDetail.put(key, csList);
                currentSettingsMap.put(key, value);
            }
        }

        int index = expandableListTitle.size();
        expandableListTitle.add(index, "BUTTON");

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
//            String textFieldValue = (String) currentSettingsMap.get(getGroup(groupPosition));
//            editText.setText(textFieldValue);
//            editText.append("");

            Object textFieldValue = cloneDevice.getConfigValueUsingConfigLabel((String)getGroup(groupPosition));
            editText.setText((String)textFieldValue);
            editText.append("");

            editText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    Log.i("JOS", "Key presssed");
                    Log.i("Key pressed", "Key pressed");
                    if(keyCode == KEYCODE_ENTER) {
                        cloneDevice.setConfigValueUsingConfigLabel((String)getGroup(groupPosition), editText.getText().toString());
                        Log.e("JOS", "getText().toString()  " + editText.getText().toString());
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
            int currentConfigInt = (int) cloneDevice.getConfigValueUsingConfigLabel(configValueLabel);
            String currentConfigValue = Integer.toString(currentConfigInt);
            Log.e("JOS", "currentConfigValue: " + currentConfigValue);

            String valuelabel = getConfigValueLabelFromConfigLabel(configValueLabel);

            /*if(configValueLabel == "Wide Range Accel Rate") {
                if(currentConfigInt == 9) {
                    currentConfigInt = 8;
                }
            }

            if(expandedListText == childPosition) {
                expandedListTextView.setChecked(true);
            } else {
                expandedListTextView.setChecked(false);
            }*/

            if(valuelabel.equals(expandedListText)) {
                expandedListTextView.setChecked(true);
            } else {
                expandedListTextView.setChecked(false);
            }


//            if (currentSettingsMap.get(getGroup(groupPosition)) == expandedListText) {
//                Log.i("JOS", currentSettingsMap.get(getGroup(groupPosition)) + " == " + expandedListText);
//                Log.i("JOS", "groupPosition: " + groupPosition + "\tchildPosition: " + childPosition);
//                expandedListTextView.setChecked(true);
//            } else {
//                expandedListTextView.setChecked(false);
//            }
//
//            expandedListTextView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    CheckedTextView checkedTextView = (CheckedTextView) v.findViewById(R.id.expandedListItem);
//                    if(checkedTextView.isChecked()) {
//                        checkedTextView.setChecked(false);
//                    } else {
//                        checkedTextView.setChecked(true);
//                    }
//                    String groupName = (String) getGroup(groupPosition);
//                    //An item is selected, so reset the state of the other checkboxes
////                    for(int i=0; i<getChildrenCount(groupPosition); i++) {
////                        View childView = parentView.getChildAt(i);
////                        CheckedTextView childCheckedTextView =  (CheckedTextView) childView.findViewById(R.id.expandedListItem);
////                        childCheckedTextView.setChecked(false);
////                    }
//                    int num = parentView.indexOfChild(v);
//                    Toast.makeText(context, "You clicked on group??!! " + groupName + " Index " + num, Toast.LENGTH_SHORT).show();
//                }
//            });

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

        if(listTitle == "BUTTON") {
            if(convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_group_button, null);
            }  else if(convertView.findViewById(R.id.saveButton) == null) {
                convertView = layoutInflater.inflate(R.layout.list_group_button, null);
            }
            writeConfigButton = (Button) convertView.findViewById(R.id.saveButton);
            resetListButton = (Button) convertView.findViewById(R.id.resetButton);
        } else {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_group, null);
            } else if(convertView.findViewById(R.id.listTitle) == null) {
                convertView = layoutInflater.inflate(R.layout.list_group, null);
            }
            TextView listTitleTextView = (TextView) convertView.findViewById(R.id.listTitle);
            listTitleTextView.setTypeface(null, Typeface.BOLD);
            listTitleTextView.setText(listTitle);
        }

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

    public void setButtonsOnClickListener(View.OnClickListener writeButtonListener, View.OnClickListener resetButtonListener) {
        writeConfigButton.setOnClickListener(writeButtonListener);
        resetListButton.setOnClickListener(resetButtonListener);
    }

    public void updateCloneDevice(ShimmerDevice newShimmerCloneDevice) {
        cloneDevice = newShimmerCloneDevice;
    }

}
