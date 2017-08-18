package com.shimmerresearch.android.guiUtilities;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ListView;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.Shimmer4Android;
import com.shimmerresearch.bluetoothmanager.guiUtilities.AbstractEnableSensorsDialog;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.AssembleShimmerConfig;
import com.shimmerresearch.managers.bluetoothManager.ShimmerBluetoothManager;

import java.util.ArrayList;
import java.util.List;

import static com.shimmerresearch.driver.Configuration.Shimmer3.SENSOR_ID.HOST_PPG_DUMMY;

/**
 * Created by User on 21/7/2017.
 */

public class EnableSensorsDialog extends AbstractEnableSensorsDialog {
    protected AlertDialog.Builder builder;

    public EnableSensorsDialog(ShimmerDevice shimmerDevice, ShimmerBluetoothManager btManager, Context context) {
        super(shimmerDevice, btManager);
        builder = new AlertDialog.Builder(context);

        List<Integer> sensorKeysToFilter = new ArrayList<Integer>();
        sensorKeysToFilter.add(Configuration.Shimmer3.SENSOR_ID.HOST_PPG_DUMMY);
        sensorKeysToFilter.add(Configuration.Shimmer3.SENSOR_ID.HOST_EXG_CUSTOM);
        setSensorKeysFilter(sensorKeysToFilter, true);

        initialize();
    }

    @Override
    protected void createWriteButton() {

    }

    @Override
    protected void createFrame() {

    }

    @Override
    protected void showFrame() {

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
                            clone.setSensorEnabledState(sensorKeys[which], true);
                        } else {
                            clone.setSensorEnabledState(sensorKeys[which], false);
                        }

                        final AlertDialog alertDialog = (AlertDialog) dialog;
                        final ListView listView = alertDialog.getListView();

                        for(int i=0; i<listView.getAdapter().getCount(); i++) {
                            if(clone.isSensorEnabled(sensorKeys[i])) {
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

                        if (clone instanceof Shimmer) {
                            writeConfiguration();
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
        setDialogTheme(ad);
        ad.show();

        final ListView listView = ad.getListView();

        for(int i=0; i<listView.getCount(); i++) {
            if(clone.isSensorEnabled(sensorKeys[i])) {
                listView.setItemChecked(i, true);
            } else {
                listView.setItemChecked(i, false);
            }
        }

    }


    @Override
    protected void createCheckBox(String sensorName, boolean state, int count) {

    }

    /**
     * Override this method to customise the style and theme of the dialog
     * @param ad
     */
    protected void setDialogTheme(AlertDialog ad) {
        //Add custom style here
    }
}
