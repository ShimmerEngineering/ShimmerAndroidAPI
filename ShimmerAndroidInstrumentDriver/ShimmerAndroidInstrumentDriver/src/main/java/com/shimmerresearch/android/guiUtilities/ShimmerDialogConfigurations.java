package com.shimmerresearch.android.guiUtilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.Shimmer4Android;
import com.shimmerresearch.driver.ShimmerDevice;
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
                        shimmerDeviceClone.infoMemByteArrayGenerate(true);
                        if (shimmerDevice instanceof Shimmer) {
                            ((Shimmer)shimmerDevice).writeConfigurationToInfoMem(shimmerDeviceClone.getShimmerInfoMemBytes());
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            ((Shimmer)shimmerDevice).writeEnabledSensors(shimmerDeviceClone.getEnabledSensors());
                        } else if (shimmerDevice instanceof Shimmer4Android){
                            ((Shimmer4Android)shimmerDevice).writeConfigurationToInfoMem(shimmerDeviceClone.getShimmerInfoMemBytes());
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
        builder.create().show();
    }

    public static void buildConfigOptionDetailsSensor(final String key,Map<String, ConfigOptionDetailsSensor> configOptionsMap, final Context context, final ShimmerDevice shimmerDevice, final ShimmerDevice shimmerDeviceClone) {
        final ConfigOptionDetailsSensor cods = configOptionsMap.get(key);
        final CharSequence[] cs = cods.getGuiValues();
        String title = "";
        if (cods.mGuiComponentType == ConfigOptionDetails.GUI_COMPONENT_TYPE.COMBOBOX) {
            Object returnedValue = shimmerDevice.getConfigValueUsingConfigLabel(key);

            if(returnedValue != null) {
                int configValue = (int) returnedValue;
                int itemIndex = Arrays.asList(configOptionsMap.get(key).getConfigValues()).indexOf(configValue);
                title = Arrays.asList(configOptionsMap.get(key).getGuiValues()).get(itemIndex);
            }
        }
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
                        shimmerDeviceClone.refreshShimmerInfoMemBytes();
                        if (shimmerDevice instanceof Shimmer) {
                            ((Shimmer)shimmerDevice).writeConfigurationToInfoMem(shimmerDeviceClone.getShimmerInfoMemBytes());
                        } else if (shimmerDevice instanceof Shimmer4Android){
                            ((Shimmer4Android)shimmerDevice).writeConfigurationToInfoMem(shimmerDeviceClone.getShimmerInfoMemBytes());
                        }
                    }
                });
        builder.create().show();
    }

}
