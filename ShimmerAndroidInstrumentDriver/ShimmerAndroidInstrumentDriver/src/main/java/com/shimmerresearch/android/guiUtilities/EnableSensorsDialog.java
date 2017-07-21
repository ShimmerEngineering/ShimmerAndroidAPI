package com.shimmerresearch.android.guiUtilities;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.Shimmer4Android;
import com.shimmerresearch.bluetoothmanager.guiUtilities.AbstractEnableSensorsDialog;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.AssembleShimmerConfig;
import com.shimmerresearch.managers.bluetoothManager.ShimmerBluetoothManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 21/7/2017.
 */

public class EnableSensorsDialog extends AbstractEnableSensorsDialog {
    AlertDialog.Builder builder;
    final List<Integer> mSelectedItems = new ArrayList();  // Where we track the selected items

    public EnableSensorsDialog(ShimmerDevice shimmerPC, ShimmerBluetoothManager btManager, Context context) {
        super(shimmerPC,btManager);
        builder = new AlertDialog.Builder(context);
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
                        ShimmerDevice shimmerDeviceClone = shimmer.deepClone();
                        for (int selected:mSelectedItems) {
                            shimmerDeviceClone.setSensorEnabledState((int)sensorKeys[selected],listEnabled[selected]);
                        }
                        //shimmerDeviceClone.refreshShimmerInfoMemBytes();
                        List<ShimmerDevice> cloneList = new ArrayList<ShimmerDevice>();
                        cloneList.add(0, shimmerDeviceClone);
                        writeConfiguration();
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

    @Override
    protected void createCheckBox(String sensorName, boolean state, int count) {

    }
}
