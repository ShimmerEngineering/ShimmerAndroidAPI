package com.shimmerresearch.android.guiUtilities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
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
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.shimmer4sdk.Shimmer4sdk;
import com.shimmerresearch.driverUtilities.AssembleShimmerConfig;
import com.shimmerresearch.driverUtilities.ConfigOptionDetails;
import com.shimmerresearch.driverUtilities.ConfigOptionDetailsSensor;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.SensorGroupingDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.exgConfig.ExGConfigOptionDetails;
import com.shimmerresearch.sensors.AbstractSensor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Lim on 16/06/2016.
 */

public class ShimmerDialogConfigurations {


    /**
     * This method is retained for compatibility with older applications that do not utilize ShimmerBLuetoothManagerAndroid
     * @param shimmerDevice
     * @param context
     */
    @Deprecated
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


    /**
     * Displays a dialog with a list of sensors supported by the Shimmer which can be enabled/disabled
     * @param shimmerDevice
     * @param context
     * @param bluetoothManager
     */
    public static void buildShimmerSensorEnableDetails(final ShimmerDevice shimmerDevice, final Context context,
                                                        final ShimmerBluetoothManagerAndroid bluetoothManager) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final ShimmerDevice shimmerDeviceClone = shimmerDevice.deepClone();
        Map<Integer,SensorDetails> sensorMap = shimmerDeviceClone.getSensorMap();
        int count = 0;

        for (SensorDetails sd:sensorMap.values()){
            if (shimmerDevice.isVerCompatibleWithAnyOf(sd.mSensorDetailsRef.mListOfCompatibleVersionInfo)) {
                count++;
            }
        }

        final String[] arraySensors = new String[count];
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
        builder.setTitle("Sensors");
        // Specify the list array, the items to be selected by default (null for none),
        // and the listener through which to receive callbacks when items are selected
        final DialogInterface.OnMultiChoiceClickListener onClick =
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which, final boolean isChecked) {

                        if(isChecked == true) {
                            shimmerDeviceClone.setSensorEnabledState(sensorKeys[which], true);
                        } else {
                            shimmerDeviceClone.setSensorEnabledState(sensorKeys[which], false);
                        }

                        final AlertDialog alertDialog = (AlertDialog) dialog;
                        final ListView listView = alertDialog.getListView();

                        for(int i=0; i<listView.getAdapter().getCount(); i++) {
                            if(shimmerDeviceClone.isSensorEnabled(sensorKeys[i])) {
                                listView.setItemChecked(i, true);
                            } else {
                                listView.setItemChecked(i, false);
                            }
                        }

                    }
                };


        builder.setMultiChoiceItems(arraySensors, null, onClick)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        List<ShimmerDevice> cloneList = new ArrayList<ShimmerDevice>();
                        cloneList.add(0, shimmerDeviceClone);
                        AssembleShimmerConfig.generateMultipleShimmerConfig(cloneList, Configuration.COMMUNICATION_TYPE.BLUETOOTH);

                        if (shimmerDeviceClone instanceof Shimmer) {
                            bluetoothManager.configureShimmer(shimmerDeviceClone);
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

        final ListView listView = ad.getListView();

        for(int i=0; i<listView.getCount(); i++) {
            if(shimmerDeviceClone.isSensorEnabled(sensorKeys[i])) {
                listView.setItemChecked(i, true);
            } else {
                listView.setItemChecked(i, false);
            }
        }
    }


    /**
     * Displays a dialog with a list of Shimmer device configuration options
     * @param shimmerDevice
     * @param context
     * @param bluetoothManager
     */
    public static void buildShimmerConfigOptions(final ShimmerDevice shimmerDevice, final Context context,
                                                 final ShimmerBluetoothManagerAndroid bluetoothManager){
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
                        //buildConfigOptionDetailsSensor(cs[which].toString(),configOptionsMap,context, shimmerDevice, shimmerDeviceClone);
                        buildConfigOptionDetailsSensor(cs[which].toString(), configOptionsMap, context, shimmerDevice, shimmerDeviceClone, bluetoothManager);
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


    /**
     * This method is retained for compatibility with older applications that do not utilize the ShimmerBluetoothManagerAndroid
     * @param key
     * @param configOptionsMap
     * @param context
     * @param shimmerDevice
     * @param shimmerDeviceClone
     */
    @Deprecated
    public static void buildConfigOptionDetailsSensor(final String key, Map<String, ConfigOptionDetailsSensor> configOptionsMap, final Context context, final ShimmerDevice shimmerDevice, final ShimmerDevice shimmerDeviceClone) {
        final ConfigOptionDetailsSensor cods = configOptionsMap.get(key);
        final CharSequence[] cs = cods.getGuiValues();
        String title = getConfigValueLabelFromConfigLabel(key, shimmerDeviceClone);
        if (cods.mGuiComponentType == ConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX) {
            Object returnedValue = shimmerDevice.getConfigValueUsingConfigLabel(key);

            if(returnedValue != null) {
//                int configValue = (int) returnedValue;
//                int itemIndex = Arrays.asList(configOptionsMap.get(key).getConfigValues()).indexOf(configValue);
//                title = Arrays.asList(configOptionsMap.get(key).getGuiValues()).get(itemIndex);
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
                                    ((Shimmer)shimmerDevice).writeConfigBytes(shimmerDeviceClone.getShimmerConfigBytes());
                                } else if (shimmerDevice instanceof Shimmer4Android){
                                    ((Shimmer4Android)shimmerDevice).writeConfigBytes(shimmerDeviceClone.getShimmerConfigBytes());
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
                            ((Shimmer)shimmerDevice).writeConfigBytes(shimmerDeviceClone.getShimmerConfigBytes());
                        } else if (shimmerDevice instanceof Shimmer4Android){
                            ((Shimmer4Android)shimmerDevice).writeConfigBytes(shimmerDeviceClone.getShimmerConfigBytes());
                        }
                    }
                });
                builder.create().show();
            }
        }
    }


    /**
     * Displays a dialog with a list of Shimmer sensor configuration options
     * @param key
     * @param configOptionsMap
     * @param context
     * @param shimmerDevice
     * @param shimmerDeviceClone
     * @param bluetoothManager
     */
    public static void buildConfigOptionDetailsSensor(final String key, Map<String, ConfigOptionDetailsSensor> configOptionsMap,
                                                       final Context context, final ShimmerDevice shimmerDevice,
                                                       final ShimmerDevice shimmerDeviceClone,
                                                       final ShimmerBluetoothManagerAndroid bluetoothManager) {
        final ConfigOptionDetailsSensor cods = configOptionsMap.get(key);
        final CharSequence[] cs = cods.getGuiValues();
        String title = getConfigValueLabelFromConfigLabel(key, shimmerDeviceClone);
        if (cods.mGuiComponentType == ConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX) {
            Object returnedValue = shimmerDevice.getConfigValueUsingConfigLabel(key);

            if(returnedValue != null) {
//                int configValue = (int) returnedValue;
//                int itemIndex = Arrays.asList(configOptionsMap.get(key).getConfigValues()).indexOf(configValue);
//                title = Arrays.asList(configOptionsMap.get(key).getGuiValues()).get(itemIndex);
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
                                List<ShimmerDevice> cloneList = new ArrayList<ShimmerDevice>();
                                cloneList.add(0, shimmerDeviceClone);
                                AssembleShimmerConfig.generateMultipleShimmerConfig(cloneList, Configuration.COMMUNICATION_TYPE.BLUETOOTH);

                                //shimmerDeviceClone.refreshShimmerInfoMemBytes();
                                if (shimmerDevice instanceof Shimmer) {
                                    bluetoothManager.configureShimmer(shimmerDeviceClone);
                                    //((Shimmer)shimmerDevice).configureShimmer(shimmerDeviceClone);
                                } else if (shimmerDevice instanceof Shimmer4Android){
                                    bluetoothManager.configureShimmer(shimmerDeviceClone);
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
                            ((Shimmer)shimmerDevice).writeConfigBytes(shimmerDeviceClone.getShimmerConfigBytes());
                        } else if (shimmerDevice instanceof Shimmer4Android){
                            ((Shimmer4Android)shimmerDevice).writeConfigBytes(shimmerDeviceClone.getShimmerConfigBytes());
                        }
                    }
                });
                builder.create().show();
            }
        }
    }

    public static void buildConfigOptionDetailsSensor(final String key, Map<String, ConfigOptionDetailsSensor> configOptionsMap,
                                                      final Context context,
                                                      final ShimmerDevice shimmerDeviceClone,
                                                      final ShimmerBluetoothManagerAndroid bluetoothManager,
                                                      final ShimmerDialogConfigurations shimmerDialogConfigurations) {
        final ConfigOptionDetailsSensor cods = configOptionsMap.get(key);
        final CharSequence[] cs = cods.getGuiValues();
        String title = getConfigValueLabelFromConfigLabel(key, shimmerDeviceClone);
        if (cods.mGuiComponentType == ConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX) {
            Object returnedValue = shimmerDeviceClone.getConfigValueUsingConfigLabel(key);

            if(returnedValue != null) {
//                int configValue = (int) returnedValue;
//                int itemIndex = Arrays.asList(configOptionsMap.get(key).getConfigValues()).indexOf(configValue);
//                title = Arrays.asList(configOptionsMap.get(key).getGuiValues()).get(itemIndex);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                shimmerDeviceClone.getConfigValueUsingConfigLabel(key);
                builder.setTitle(title);
                builder.setItems(cs,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item
                                Toast.makeText(context, cs[which], Toast.LENGTH_SHORT).show();
                                shimmerDeviceClone.setConfigValueUsingConfigLabel(key,cods.mConfigValues[which]);
                                shimmerDialogConfigurations.writeConfigToShimmer(shimmerDeviceClone, bluetoothManager);
                            }
                        });

                //Empty method which can be overwritten to interact with the Builder (e.g. to customize the look of the dialog)
                shimmerDialogConfigurations.setADBuilderTheme(context, builder, title);

                builder.create().show();
            }
        } else if (cods.mGuiComponentType == ConfigOptionDetails.GUI_COMPONENT_TYPE.TEXTFIELD){
            Object returnedValue = shimmerDeviceClone.getConfigValueUsingConfigLabel(key);

            if(returnedValue != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle((String)returnedValue);
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
                        shimmerDialogConfigurations.writeConfigToShimmer(shimmerDeviceClone, bluetoothManager);

                    }
                });

                //Empty method which can be overwritten to interact with the Builder (e.g. to customize the look of the dialog)
                shimmerDialogConfigurations.setADBuilderTheme(context, builder, (String) returnedValue);

                builder.create().show();
            }
        }
    }

    public void writeConfigToShimmer(ShimmerDevice clone, ShimmerBluetoothManagerAndroid bluetoothManager) {

        AssembleShimmerConfig.generateSingleShimmerConfig(clone, Configuration.COMMUNICATION_TYPE.BLUETOOTH);

        if (clone instanceof Shimmer) {
            bluetoothManager.configureShimmer(clone);
        } else if (clone instanceof Shimmer4Android){
            bluetoothManager.configureShimmer(clone);
        }

    }

    private static String getConfigValueLabelFromConfigLabel(String label, ShimmerDevice cloneDevice){
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

    private static int getConfigValueIntFromConfigGuiIndex(int configGuiIndex, String currentConfigKey, ShimmerDevice cloneDevice) {
        ConfigOptionDetailsSensor cods = cloneDevice.getConfigOptionsMap().get(currentConfigKey);
        Integer[] values = cods.getConfigValues();
        return values[configGuiIndex];
    }

    //Additional variables for custom signals and filtered signals for the SelectSensorPlot dialog
    static protected List<String[]> mAdditionalSignalsList = null;
    static protected List<String[]> mFilteredSignalsList = null;

    /**
     * Call this to display the select signals to plot dialog with additional custom signals
     * @param context
     * @param shimmerService
     * @param bluetoothAddress
     * @param dynamicPlot
     * @param additionalSignalsList Should be in the format: Array[0] = Shimmer device name | Array[1] = Signal/channel name | Array[2] = Format (CAL/UNCAL) | Array[3] = Units
     */
    public static void showSelectSensorPlotWithAddSignals(Context context, final ShimmerService shimmerService, final String bluetoothAddress, final XYPlot dynamicPlot, List<String[]> additionalSignalsList) {
        mAdditionalSignalsList = additionalSignalsList;
        showSelectSensorPlot(context, shimmerService, bluetoothAddress, dynamicPlot);
    }

    /**
     * Call this to display the select signals to plot dialog with specific signals filtered out
     * @param context
     * @param shimmerService
     * @param bluetoothAddress
     * @param dynamicPlot
     * @param filteredSignalsList   Should be in the format: Array[0] = Shimmer device name | Array[1] = Signal/channel name | Array[2] = Format (CAL/UNCAL) | Array[3] = Units
     */
    public static void showSelectSensorPlotWithFilter(Context context, final ShimmerService shimmerService, final String bluetoothAddress, final XYPlot dynamicPlot, List<String[]> filteredSignalsList) {
        mFilteredSignalsList = filteredSignalsList;
        showSelectSensorPlot(context, shimmerService, bluetoothAddress, dynamicPlot);
    }

    /**
     * Call this to display the select signals to plot dialog with additional custom signals and specific signals filtered out
     * @param context
     * @param shimmerService
     * @param bluetoothAddress
     * @param dynamicPlot
     * @param additionalSignalsList Should be in the format: Array[0] = Shimmer device name | Array[1] = Signal/channel name | Array[2] = Format (CAL/UNCAL) | Array[3] = Units
     * @param filteredSignalsList   Should be in the format: Array[0] = Shimmer device name | Array[1] = Signal/channel name | Array[2] = Format (CAL/UNCAL) | Array[3] = Units
     */
    public static void showSelectSensorPlotWithFilterAddSignals(Context context, final ShimmerService shimmerService, final String bluetoothAddress, final XYPlot dynamicPlot, List<String[]> additionalSignalsList, List<String[]> filteredSignalsList) {
        mFilteredSignalsList = filteredSignalsList;
        mAdditionalSignalsList = additionalSignalsList;
        showSelectSensorPlot(context, shimmerService, bluetoothAddress, dynamicPlot);
    }

    /**
     * Call this to display the select signals to plot dialog
     * @param context
     * @param shimmerService
     * @param bluetoothAddress
     * @param dynamicPlot
     */
    public static void showSelectSensorPlot(Context context, final ShimmerService shimmerService, final String bluetoothAddress, final XYPlot dynamicPlot){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(com.shimmerresearch.androidinstrumentdriver.R.layout.dialog_sensor_view);
        final Button buttonSetPlotSignalFilter = (Button) dialog.findViewById(com.shimmerresearch.androidinstrumentdriver.R.id.ButtonFilterPlotSignal);
        final Button buttonResetPlotSignalFilter = (Button) dialog.findViewById(com.shimmerresearch.androidinstrumentdriver.R.id.buttonResetFilterPlotSignal);
        final Button buttonDone = (Button) dialog.findViewById(com.shimmerresearch.androidinstrumentdriver.R.id.button_done);
        final EditText editTextSignalFilter = (EditText) dialog.findViewById(com.shimmerresearch.androidinstrumentdriver.R.id.editTextFilterPlotSignal);
        dialog.setCanceledOnTouchOutside(true);
        TextView title = (TextView) dialog.findViewById(android.R.id.title);
        if(title != null) {
            title.setText("Select Signal");
        } else {
            Log.e("DialogConfigurations", "Title TextView is null!");
        }
        final ListView listView = (ListView) dialog.findViewById(android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        //Temporary list so we have the option of adding additional custom channels
        List<String[]> listOfChannels = shimmerService.getListofEnabledSensorSignals(bluetoothAddress);

        if(mAdditionalSignalsList != null && mAdditionalSignalsList.size() > 0) {
            for (String[] addSignal : mAdditionalSignalsList) {
                listOfChannels.add(addSignal);      //Add the custom channel to the list of channels
            }
        }

        //Create a temporary list of channels to remove the filter signals while iterating through the original list of channels
        List<String[]> tempListOfChannelsWithSignalsFiltered = new ArrayList<String[]>();
        for(String[] temp : listOfChannels) {
            tempListOfChannelsWithSignalsFiltered.add(temp);
        }

        if(mFilteredSignalsList != null && mFilteredSignalsList.size() > 0) {
            for(String[] signalToFilter : mFilteredSignalsList) {
                for(String[] signal : listOfChannels) {
                    //Check if the channel name and format (CAL/UNCAL) matches the signal to be filtered
                    if(signal[1].equals(signalToFilter[1]) && signal[2].equals(signalToFilter[2])) {
                        tempListOfChannelsWithSignalsFiltered.remove(signal);
                    }
                }
            }
        }

        //Assign the filtered list back to the original list
        listOfChannels = tempListOfChannelsWithSignalsFiltered;

        //Join the Strings in each individual array in the list of channels in order to pass them to the ArrayAdapter
        List<String> sensorList = new ArrayList<String>();
        for(int i=0;i<listOfChannels.size();i++) {
            sensorList.add(joinStrings(listOfChannels.get(i)));
        }

        final String[] sensorNames = sensorList.toArray(new String[sensorList.size()]);

        final ArrayAdapter<String> adapterSensorNames = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, sensorNames);
        listView.setAdapter(adapterSensorNames);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        for (int p=0;p<listOfChannels.size();p++){
            if (shimmerService.mPlotManager.checkIfPropertyExist(listOfChannels.get(p))){
                listView.setItemChecked(p, true);
            }
        }

        final List<String[]> listOfChannelsFinal = listOfChannels;   //Final list so inner methods can access it

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

                for (int i=listOfChannelsFinal.size()-1;i>-1;i--){
                    String signal = joinStrings(listOfChannelsFinal.get(i));
                    if (!signal.toLowerCase().contains(plotSignaltoFilter.toLowerCase())){

                        listOfChannelsFinal.remove(i);
                    }

                }

                for(int i=0;i<listOfChannelsFinal.size();i++) {
                    sensorList.add(joinStrings(listOfChannelsFinal.get(i)));
                }

                final String[] sensorNames = sensorList.toArray(new String[sensorList.size()]);
                ArrayAdapter<String> adapterSensorNames = new ArrayAdapter<String>(dialog.getContext(), android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, sensorNames);
                listView.setAdapter(adapterSensorNames);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                for (int p=0;p<listOfChannelsFinal.size();p++){
                    if (shimmerService.mPlotManager.checkIfPropertyExist(listOfChannelsFinal.get(p))){
                        listView.setItemChecked(p, true);
                    }
                }

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int index,
                                            long arg3) {
                        CheckedTextView cb = (CheckedTextView) arg1;
                        if (!shimmerService.mPlotManager.checkIfPropertyExist(listOfChannelsFinal.get(index))){
                            try {
                                shimmerService.mPlotManager.addSignal(listOfChannelsFinal.get(index), dynamicPlot);
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        } else {
                            shimmerService.mPlotManager.removeSignal(listOfChannelsFinal.get(index));
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
                if (!shimmerService.mPlotManager.checkIfPropertyExist(listOfChannelsFinal.get(index))){
                    try {
                        shimmerService.mPlotManager.addSignal(listOfChannelsFinal.get(index), dynamicPlot);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    shimmerService.mPlotManager.removeSignal(listOfChannelsFinal.get(index));
                }
            }

        });

        dialog.show();

    }


    /**
     * This displays a dialog populated by the list of Shimmers connected via Shimmer Bluetooth Manager.
     * @param
     */
    public void buildShimmersConnectedList(final List<ShimmerDevice> deviceList, final Context context,
                                           final ShimmerBluetoothManagerAndroid bluetoothManager) {
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
                buildSensorOrConfigOptions(shimmerDevice, context, bluetoothManager);
                //dialogInterface.cancel();
            }
        });

        builder.create().show();
    }


    /**
     * Displays a dialog allowing for selection of either enable sensors or configure device
     * @param shimmerDevice
     * @param context
     * @param bluetoothManager
     */
    public void buildSensorOrConfigOptions(final ShimmerDevice shimmerDevice, final Context context,
                                           final ShimmerBluetoothManagerAndroid bluetoothManager) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        CharSequence[] items = new CharSequence[2];
        items[0] = "Enable/Disable Sensors";
        items[1] = "Device Configuration";

        builder.setTitle("Options");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0) {
                    buildShimmerSensorEnableDetails(shimmerDevice, context, bluetoothManager);
                }
                else if(i == 1) {
                    buildShimmerConfigOptions(shimmerDevice, context, bluetoothManager);
                }
            }
        });

        builder.create().show();
    }


    public void buildSamplingRateDialog(final ShimmerDevice shimmerDeviceClone, final Context context, final ShimmerBluetoothManagerAndroid bluetoothManager) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        double currentSamplingRate = shimmerDeviceClone.getSamplingRateShimmer();

        final String[] presets = new String[] {"8","16","51.2","102.4","128","204.8","256","512","1024"};
        final EditText editText = new EditText(context);
        final Button validateButton = new Button(context);
        final ListView listView = new ListView(context);
        final TextView textView = new TextView(context);

        validateButton.setText("Validate");
        textView.setText("Custom: ");
        textView.setTextSize(18);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, presets);
        listView.setAdapter(listAdapter);

        validateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = editText.getText().toString();
                double samplingRate = -1;
                if(text != null && !text.isEmpty()) {
                    try {
                        samplingRate = Double.parseDouble(text);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }

                if(samplingRate != -1) {
                    shimmerDeviceClone.setShimmerAndSensorsSamplingRate(samplingRate);
                }

                //Get the adjusted sampling rate back from the clone and display it in EditText
                double newSamplingRate = shimmerDeviceClone.getSamplingRateShimmer();
                editText.setText(Double.toString(newSamplingRate));
            }
        });

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(400, ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout verticalLinearLayout = new LinearLayout(context, null);
        LinearLayout horizontalLinearLayout = new LinearLayout(context, null);

        verticalLinearLayout.setOrientation(LinearLayout.VERTICAL);
        horizontalLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        editText.setLayoutParams(layoutParams);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER |  InputType.TYPE_NUMBER_FLAG_DECIMAL);

        horizontalLinearLayout.addView(textView);
        horizontalLinearLayout.addView(editText);
        horizontalLinearLayout.addView(validateButton);

        //Add the ListView first, then the EditText and Button for setting a custom sampling rate
        verticalLinearLayout.addView(listView);
        verticalLinearLayout.addView(horizontalLinearLayout);

        builder.setView(verticalLinearLayout);

        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, shimmerDeviceClone.getSamplingRateShimmer() + "Hz selected", Toast.LENGTH_SHORT).show();
                writeConfigToShimmer(shimmerDeviceClone, bluetoothManager);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        //Empty method which can be overwritten to customize the builder theme
        setADBuilderTheme(context, builder, Double.toString(currentSamplingRate) + "Hz");

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialogInterface) {

                Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

                //Empty method which can be overwritten to customize the dialog and buttons theme
                setDialogAndButtonsTheme(context, dialog, positiveButton, negativeButton);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Double value = Double.parseDouble(presets[position]);
                        shimmerDeviceClone.setShimmerAndSensorsSamplingRate(value);
                        Toast.makeText(context, value + "Hz selected", Toast.LENGTH_SHORT).show();
                        writeConfigToShimmer(shimmerDeviceClone, bluetoothManager);
                        dialogInterface.dismiss();
                    }
                });
            }
        });

        dialog.show();

    }

    /**
     * Override this method to interact with and customize the AlertDialog Builder
     * @param context
     * @param builder
     */
    public void setADBuilderTheme(Context context, AlertDialog.Builder builder, String title) {
        //Insert code to edit and customize the builder here
    }

    /**
     * Override this method to interact with and customize the Dialog and its buttons
     * @param context
     * @param dialog
     * @param positiveButton
     * @param negativeButton
     */
    public void setDialogAndButtonsTheme(Context context, AlertDialog dialog, Button positiveButton, Button negativeButton) {
        //Insert code to edit and customize the dialog and buttons here
    }





    /**
     * Combines the strings in an array into a single string
     * @param a
     * @return
     */
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