package com.shimmerresearch.android.guiUtilities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.XYPlot;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.Shimmer4Android;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.android.shimmerService.ShimmerService;
import com.shimmerresearch.androidinstrumentdriver.R;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.AssembleShimmerConfig;
import com.shimmerresearch.driverUtilities.ConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.sensors.AbstractSensor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Lim on 16/06/2016.
 */

public class ShimmerDialogConfigurations {

    public static void buildShimmerSensorEnableDetails(final ShimmerDevice shimmerDevice, final Context context){

        final List<Integer> mSelectedItems = new ArrayList();  // Where we track the selected items
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        Map<Integer,SensorDetails> sensorMap = shimmerDevice.getSensorMap();
        int count = 0;
        for (SensorDetails sd:sensorMap.values()){
            if (shimmerDevice.isVerCompatibleWithAnyOf(sd.mSensorDetailsRef.mListOfCompatibleVersionInfo)) {
                count++;
            }

        }
        String[] arraySensors = new String[count];
        final boolean[] listEnabled = new boolean[count];
        final int[] sensorKeys = new int[count];
        count = 0;

        for (int key:sensorMap.keySet()){
            SensorDetails sd = sensorMap.get(key);
            if (shimmerDevice.isVerCompatibleWithAnyOf(sd.mSensorDetailsRef.mListOfCompatibleVersionInfo)) {
                arraySensors[count] = sd.mSensorDetailsRef.mGuiFriendlyLabel;
                listEnabled[count] = sd.isEnabled();
                sensorKeys[count] = key;
                count++;
            }
        }
        // Set the dialog title
        builder.setTitle("Sensors")
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(arraySensors, listEnabled,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (mSelectedItems.contains(which)){
                                    mSelectedItems.remove(Integer.valueOf(which));
                                } else{
                                    mSelectedItems.add(which);
                                }


                            }
                        })
                // Set the action buttons
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog
                        ShimmerDevice shimmerDeviceClone = shimmerDevice.deepClone();
                        for (int selected:mSelectedItems) {
                            shimmerDeviceClone.setSensorEnabledState((int)sensorKeys[selected],listEnabled[selected]);
                        }
                        //shimmerDeviceClone.refreshShimmerInfoMemBytes();
                        List<ShimmerDevice> cloneList = new ArrayList<ShimmerDevice>();
                        cloneList.add(0, shimmerDeviceClone);
                        AssembleShimmerConfig.generateMultipleShimmerConfig(cloneList, Configuration.COMMUNICATION_TYPE.BLUETOOTH);

                        if (shimmerDevice instanceof Shimmer) {
                            //((Shimmer)shimmerDevice).writeConfigBytes(shimmerDeviceClone.getShimmerInfoMemBytes());
                            /*try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }*/
                            ((Shimmer)shimmerDevice).writeEnabledSensors(shimmerDeviceClone.getEnabledSensors());
                        } else if (shimmerDevice instanceof Shimmer4Android){
                            //((Shimmer4Android)shimmerDevice).writeConfigBytes(shimmerDeviceClone.getShimmerInfoMemBytes());
                        }
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                       dialog.cancel();
                    }
                });

        AlertDialog ad = builder.create();
        ad.show();
    }

    public static void buildShimmerConfigOptions(final ShimmerDevice shimmerDevice, final Context context){
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
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Configuration");
        builder.setItems(cs,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        Toast.makeText(context, cs[which], Toast.LENGTH_SHORT).show();
                        buildConfigOptionDetailsSensor(cs[which].toString(),configOptionsMap,context, shimmerDevice, shimmerDeviceClone);
                    }
                });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }

    public static void buildConfigOptionDetailsSensor(final String key, Map<String, ConfigOptionDetailsSensor> configOptionsMap, final Context context, final ShimmerDevice shimmerDevice, final ShimmerDevice shimmerDeviceClone) {
        final ConfigOptionDetailsSensor cods = configOptionsMap.get(key);
        final CharSequence[] cs = cods.getGuiValues();
        String title = "";
        if (cods.mGuiComponentType == ConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX) {
            Object returnedValue = shimmerDevice.getConfigValueUsingConfigLabel(key);

            if(returnedValue != null) {
                int configValue = (int) returnedValue;
                int itemIndex = Arrays.asList(configOptionsMap.get(key).getConfigValues()).indexOf(configValue);
                title = Arrays.asList(configOptionsMap.get(key).getGuiValues()).get(itemIndex);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                shimmerDevice.getConfigValueUsingConfigLabel(key);
                builder.setTitle(title);
                builder.setItems(cs,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item
                                Toast.makeText(context, cs[which], Toast.LENGTH_SHORT).show();
                                shimmerDeviceClone.setConfigValueUsingConfigLabel(key,cods.mConfigValues[which]);

                                //shimmerDeviceClone.refreshShimmerInfoMemBytes();
                                List<ShimmerDevice> cloneList = new ArrayList<ShimmerDevice>();
                                cloneList.add(0, shimmerDeviceClone);
                                AssembleShimmerConfig.generateMultipleShimmerConfig(cloneList, Configuration.COMMUNICATION_TYPE.BLUETOOTH);

                                if (shimmerDevice instanceof Shimmer) {
                                    ((Shimmer)shimmerDevice).writeConfigBytes(shimmerDeviceClone.getShimmerInfoMemBytes());
                                } else if (shimmerDevice instanceof Shimmer4Android){
                                    ((Shimmer4Android)shimmerDevice).writeConfigBytes(shimmerDeviceClone.getShimmerInfoMemBytes());
                                }
                            }
                        });
                builder.create().show();
            }
        } else if (cods.mGuiComponentType == ConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD){
            Object returnedValue = shimmerDevice.getConfigValueUsingConfigLabel(key);

            if(returnedValue != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle((String)returnedValue);
                LinearLayout.LayoutParams tv1Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                final EditText et = new EditText(context);
                et.setText((String)returnedValue);
                LinearLayout layout = new LinearLayout(context);
                LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setLayoutParams(parms);
                layout.setGravity(Gravity.CLIP_VERTICAL);
                layout.setPadding(2, 2, 2, 2);
                layout.addView(et, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                builder.setView(layout);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        shimmerDeviceClone.setConfigValueUsingConfigLabel(key,et.getText().toString());

                        //shimmerDeviceClone.refreshShimmerInfoMemBytes();
                        List<ShimmerDevice> cloneList = new ArrayList<ShimmerDevice>();
                        cloneList.add(0, shimmerDeviceClone);
                        AssembleShimmerConfig.generateMultipleShimmerConfig(cloneList, Configuration.COMMUNICATION_TYPE.BLUETOOTH);
                        if (shimmerDevice instanceof Shimmer) {
                            ((Shimmer)shimmerDevice).writeConfigBytes(shimmerDeviceClone.getShimmerInfoMemBytes());
                        } else if (shimmerDevice instanceof Shimmer4Android){
                            ((Shimmer4Android)shimmerDevice).writeConfigBytes(shimmerDeviceClone.getShimmerInfoMemBytes());
                        }
                    }
                });
                builder.create().show();
            }
        }
    }
    public static void showSelectSensorPlot(Context context,final ShimmerService shimmerService, final String bluetoothAddress, final XYPlot dynamicPlot){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_sensor_view);
        final Button buttonSetPlotSignalFilter = (Button) dialog.findViewById(R.id.ButtonFilterPlotSignal);
        final Button buttonResetPlotSignalFilter = (Button) dialog.findViewById(R.id.buttonResetFilterPlotSignal);
        final Button buttonDone = (Button) dialog.findViewById(R.id.button_done);
        final EditText editTextSignalFilter = (EditText) dialog.findViewById(R.id.editTextFilterPlotSignal);
        dialog.setCanceledOnTouchOutside(true);
        TextView title = (TextView) dialog.findViewById(android.R.id.title);
        //title.setText("Select Signal");
        final ListView listView = (ListView) dialog.findViewById(android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final List<String[]> listofChannels = shimmerService.getListofEnabledSensorSignals(bluetoothAddress);

        List<String> sensorList = new ArrayList<String>();
        for(int i=0;i<listofChannels.size();i++) {
            sensorList.add(joinStrings(listofChannels.get(i)));
        }

        final String[] sensorNames = sensorList.toArray(new String[sensorList.size()]);

        final ArrayAdapter<String> adapterSensorNames = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, sensorNames);
        listView.setAdapter(adapterSensorNames);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        //listView.setItemChecked(position, value);

        for (int p=0;p<listofChannels.size();p++){
            if (shimmerService.mPlotManager.checkIfPropertyExist(listofChannels.get(p))){
                listView.setItemChecked(p, true);
            }
        }

        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.hide();
            }
        });

        buttonSetPlotSignalFilter.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                List<String> sensorList = new ArrayList<String>();
                String plotSignaltoFilter  = editTextSignalFilter.getText().toString();

                for (int i=listofChannels.size()-1;i>-1;i--){
                    String signal = joinStrings(listofChannels.get(i));
                    if (!signal.toLowerCase().contains(plotSignaltoFilter.toLowerCase())){

                        listofChannels.remove(i);
                    }

                }

                for(int i=0;i<listofChannels.size();i++) {
                    sensorList.add(joinStrings(listofChannels.get(i)));
                }

                final String[] sensorNames = sensorList.toArray(new String[sensorList.size()]);
                ArrayAdapter<String> adapterSensorNames = new ArrayAdapter<String>(dialog.getContext(), android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, sensorNames);
                listView.setAdapter(adapterSensorNames);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                for (int p=0;p<listofChannels.size();p++){
                    if (shimmerService.mPlotManager.checkIfPropertyExist(listofChannels.get(p))){
                        listView.setItemChecked(p, true);
                    }
                }

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int index,
                                            long arg3) {
                        CheckedTextView cb = (CheckedTextView) arg1;
                        if (!shimmerService.mPlotManager.checkIfPropertyExist(listofChannels.get(index))){
                            try {
                                shimmerService.mPlotManager.addSignal(listofChannels.get(index), dynamicPlot);
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        } else {
                            shimmerService.mPlotManager.removeSignal(listofChannels.get(index));
                        }
                    }

                });

            }
        });

        buttonResetPlotSignalFilter.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                final List<String[]> listofChannels = shimmerService.getListofEnabledSensorSignals(bluetoothAddress);
                List<String> sensorList = new ArrayList<String>();
                for(int i=0;i<listofChannels.size();i++) {
                    sensorList.add(joinStrings(listofChannels.get(i)));
                }

                final String[] sensorNames = sensorList.toArray(new String[sensorList.size()]);

                final ArrayAdapter<String> adapterSensorNames = new ArrayAdapter<String>(dialog.getContext(), android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, sensorNames);
                listView.setAdapter(adapterSensorNames);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                //listView.setItemChecked(position, value);

                for (int p=0;p<listofChannels.size();p++){
                    if (shimmerService.mPlotManager.checkIfPropertyExist(listofChannels.get(p))){
                        listView.setItemChecked(p, true);
                    }
                }

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int index,
                                            long arg3) {
                        CheckedTextView cb = (CheckedTextView) arg1;
                        if (!shimmerService.mPlotManager.checkIfPropertyExist(listofChannels.get(index))){
                            try {
                                shimmerService.mPlotManager.addSignal(listofChannels.get(index), dynamicPlot);
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        } else {
                            shimmerService.mPlotManager.removeSignal(listofChannels.get(index));
                        }
                    }

                });


            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index,
                                    long arg3) {
                CheckedTextView cb = (CheckedTextView) arg1;
                if (!shimmerService.mPlotManager.checkIfPropertyExist(listofChannels.get(index))){
                    try {
                        shimmerService.mPlotManager.addSignal(listofChannels.get(index), dynamicPlot);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    shimmerService.mPlotManager.removeSignal(listofChannels.get(index));
                }
            }

        });

        dialog.show();

    }

    /**
     * This displays a popup dialog populated by the list of Shimmers connected via Shimmer Bluetooth Manager.
     * @param
     */
    public void buildShimmersConnectedList(final List<ShimmerDevice> deviceList, final Context context) {
        //List<ShimmerDevice> deviceList = btManager.getListOfConnectedDevices();
        CharSequence[] nameList = new CharSequence[deviceList.size()];
        CharSequence[] macList = new CharSequence[deviceList.size()];
        CharSequence[] displayList = new CharSequence[deviceList.size()];

        for(int i=0; i<nameList.length; i++) {
            nameList[i] = deviceList.get(i).getShimmerUserAssignedName();
            macList[i] = deviceList.get(i).getMacId();
            displayList[i] = nameList[i] + "\n" + macList[i];
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Connected Shimmers");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setItems(displayList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ShimmerDevice shimmerDevice = deviceList.get(i);
                buildSensorOrConfigOptions(shimmerDevice, context);
                //dialogInterface.cancel();
            }
        });

        builder.create().show();
    }

    public void buildSensorOrConfigOptions(final ShimmerDevice shimmerDevice, final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        CharSequence[] items = new CharSequence[2];
        items[0] = "Enable/Disable Sensors";
        items[1] = "Device Configuration";

        builder.setTitle("Options");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0) {
                    buildShimmerSensorEnableDetails(shimmerDevice, context);
                }
                else if(i == 1) {
                    buildShimmerConfigOptions(shimmerDevice, context);
                }
            }
        });

        builder.create().show();
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

}