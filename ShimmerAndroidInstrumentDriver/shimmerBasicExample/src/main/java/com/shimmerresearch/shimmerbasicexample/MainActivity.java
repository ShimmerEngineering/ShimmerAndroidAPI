package com.shimmerresearch.shimmerbasicexample;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog;
import com.shimmerresearch.android.guiUtilities.ShimmerDialogConfigurations;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.EXTRA_DEVICE_ADDRESS;


public class MainActivity extends AppCompatActivity {

    ShimmerBluetoothManagerAndroid btManager;
    ShimmerDevice shimmerDevice;
    final static String shimmerBtAdd = "00:06:66:66:96:86";  //Put the address of the Shimmer device you want to connect here
    //00:06:66:43:b4:8b
    //final static String shimmerBtAdd = "00:06:66:66:96:A9";  //Put the address of the Shimmer device you want to connect here
    //final static String shimmerBtAdd = "00:06:66:43:B4:8B";  //Put the address of the Shimmer device you want to connect here
    //final static String shimmerBtAdd = "00:06:66:8C:A5:04";  //Put the address of the Shimmer device you want to connect here

    //TODO: Add this into the ShimmerBluetoothDialog class
    final static int REQUEST_CONNECT_SHIMMER = 2;


    final static String LOG_TAG = "SHIMMER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            btManager = new ShimmerBluetoothManagerAndroid(this, mHandler);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Couldn't create ShimmerBluetoothManagerAndroid. Error: " + e);
        }

    }

    @Override
    protected void onStart() {
        //Connect the Shimmer using its Bluetooth Address
        try {
            btManager.connectShimmerTroughBTAddress(shimmerBtAdd);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error. Shimmer device not paired or Bluetooth is not enabled");
            Toast.makeText(this, "Error. Shimmer device not paired or Bluetooth is not enabled. " +
                            "Please close the app and pair or enable Bluetooth", Toast.LENGTH_LONG).show();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        //Disconnect the Shimmer device when app is stopped
        if(shimmerDevice != null) {
            if(shimmerDevice.isSDLogging()) {
                shimmerDevice.stopSDLogging();
                Log.d(LOG_TAG, "Stopped Shimmer Logging");
            }
            else if(shimmerDevice.isStreaming()) {
                shimmerDevice.stopStreaming();
                Log.d(LOG_TAG, "Stopped Shimmer Streaming");
            }
            else {
                shimmerDevice.stopStreamingAndLogging();
                Log.d(LOG_TAG, "Stopped Shimmer Streaming and Logging");
            }
        }
        btManager.disconnectAllDevices();
        Log.i(LOG_TAG, "Shimmer DISCONNECTED");
        super.onStop();
    }

    /**
     * Messages from the Shimmer device including sensor data are received here
     */
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET:
                    if ((msg.obj instanceof ObjectCluster)) {

                        ObjectCluster objectCluster = (ObjectCluster) msg.obj;

                        //Retrieve all possible formats for the current sensor device:
                        Collection<FormatCluster> allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP);
                        FormatCluster timeStampCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(allFormats,"CAL"));
                        double timeStampData = timeStampCluster.mData;

                        allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X);
                        FormatCluster accelXCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(allFormats,"CAL"));
                        double accelXData = accelXCluster.mData;

                        Log.i(LOG_TAG, "Time Stamp: " + timeStampData);
                        Log.i(LOG_TAG, "Accel LN X: " + accelXData);

                    }
                    break;
                case ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE:
                    ShimmerBluetooth.BT_STATE state = null;
                    String macAddress = "";

                    if (msg.obj instanceof ObjectCluster) {
                        state = ((ObjectCluster) msg.obj).mState;
                        macAddress = ((ObjectCluster) msg.obj).getMacAddress();
                    } else if (msg.obj instanceof CallbackObject) {
                        state = ((CallbackObject) msg.obj).mState;
                        macAddress = ((CallbackObject) msg.obj).mBluetoothAddress;
                    }

                    Log.d(LOG_TAG, "Shimmer state changed! Shimmer = " + macAddress + ", new state = " + state);

                    switch (state) {
                        case CONNECTED:
                            Log.i(LOG_TAG, "Shimmer [" + macAddress + "] is now CONNECTED");
                            shimmerDevice = btManager.getShimmerDeviceBtConnectedFromMac(shimmerBtAdd);
                            if(shimmerDevice != null) { Log.i(LOG_TAG, "Got the ShimmerDevice!"); }
                            else { Log.i(LOG_TAG, "ShimmerDevice returned is NULL!"); }
                            break;
                        case CONNECTING:
                            Log.i(LOG_TAG, "Shimmer [" + macAddress + "] is CONNECTING");
                            break;
                        case STREAMING:
                            Log.i(LOG_TAG, "Shimmer [" + macAddress + "] is now STREAMING");
                            break;
                        case STREAMING_AND_SDLOGGING:
                            Log.i(LOG_TAG, "Shimmer [" + macAddress + "] is now STREAMING AND LOGGING");
                            break;
                        case SDLOGGING:
                            Log.i(LOG_TAG, "Shimmer [" + macAddress + "] is now SDLOGGING");
                            break;
                        case DISCONNECTED:
                            Log.i(LOG_TAG, "Shimmer [" + macAddress + "] has been DISCONNECTED");
                            break;
                    }
                    break;
            }

            super.handleMessage(msg);
        }
    };

    public void stopStreaming(View v){
        shimmerDevice.stopStreaming();
    }

    public void startStreaming(View v){
        shimmerDevice.startStreaming();
    }

    /**
     * Called when the configurations button is clicked
     * @param v
     */
    public void openConfigMenu(View v){
        if(shimmerDevice != null) {
            if(!shimmerDevice.isStreaming() && !shimmerDevice.isSDLogging()) {
                //TODO:
                ShimmerDialogConfigurations.buildShimmerConfigOptions(shimmerDevice, MainActivity.this, btManager);
            }
            else {
                Log.e(LOG_TAG, "Cannot open menu! Shimmer device is STREAMING AND/OR LOGGING");
                Toast.makeText(MainActivity.this, "Cannot open menu! Shimmer device is STREAMING AND/OR LOGGING", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Log.e(LOG_TAG, "Cannot open menu! Shimmer device is not connected");
            Toast.makeText(MainActivity.this, "Cannot open menu! Shimmer device is not connected", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called when the menu button is clicked
     * @param v
     * @throws IOException
     */
    public void openMenu(View v) throws IOException {

        if(shimmerDevice != null) {
            if(!shimmerDevice.isStreaming() && !shimmerDevice.isSDLogging()) {
                //ShimmerDialogConfigurations.buildShimmerSensorEnableDetails(shimmerDevice, MainActivity.this);
                ShimmerDialogConfigurations.buildShimmerSensorEnableDetails2(shimmerDevice, MainActivity.this);
            }
            else {
                Log.e(LOG_TAG, "Cannot open menu! Shimmer device is STREAMING AND/OR LOGGING");
                Toast.makeText(MainActivity.this, "Cannot open menu! Shimmer device is STREAMING AND/OR LOGGING", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Log.e(LOG_TAG, "Cannot open menu! Shimmer device is not connected");
            Toast.makeText(MainActivity.this, "Cannot open menu! Shimmer device is not connected", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called when the paired devices button is clicked
     * @param v
     */
    public void pairedDevices(View v) {
        Intent intent = new Intent(getApplicationContext(), ShimmerBluetoothDialog.class);
        startActivityForResult(intent, REQUEST_CONNECT_SHIMMER);
    }

    /**
     * Get the result from the paired devices dialog
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                btManager.disconnectAllDevices();   //Disconnect all devices first
                //Get the Bluetooth mac address of the selected device:
                String macAdd = data.getStringExtra(EXTRA_DEVICE_ADDRESS);
                btManager.connectShimmerTroughBTAddress(macAdd);   //Connect to the selected device
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
