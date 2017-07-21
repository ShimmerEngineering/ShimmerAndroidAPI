package com.shimmerresearch.android.guiUtilities;


import com.shimmerresearch.bluetoothmanager.guiUtilities.AbstractEnableSensorsDialog;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.managers.bluetoothManager.ShimmerBluetoothManager;

/**
 * Created by User on 21/7/2017.
 */

public class EnableSensorsDialog extends AbstractEnableSensorsDialog {

    public EnableSensorsDialog(ShimmerDevice shimmerPC, ShimmerBluetoothManager btManager) {
        super(shimmerPC,btManager);
    }

    @Override
    protected void createWriteButton() {

    }

    @Override
    protected void createFrame() {

    }

    @Override
    protected void showFrame() {

    }

    @Override
    protected void createCheckBox(String sensorName, boolean state, int count) {

    }
}
