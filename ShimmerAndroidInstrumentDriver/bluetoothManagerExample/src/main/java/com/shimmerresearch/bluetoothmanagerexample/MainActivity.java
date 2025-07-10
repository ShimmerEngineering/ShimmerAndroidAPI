package com.shimmerresearch.bluetoothmanagerexample;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog;
import com.shimmerresearch.android.guiUtilities.ShimmerDialogConfigurations;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.exceptions.ShimmerException;

import java.io.IOException;
import java.util.Collection;

import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.EXTRA_DEVICE_ADDRESS;
import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.EXTRA_DEVICE_NAME;

/**
 * This example demonstrates the use of the {@link ShimmerBluetoothManagerAndroid} to:
 * <ul>
 *     <li>Connect to a Shimmer device</li>
 *     <li>Stream data from the Shimmer device</li>
 *     <li>Enable and disable sensors</li>
 *     <li>Modify individual sensor configurations</li>
 * </ul>
 */
public class MainActivity extends AppCompatActivity {

    ShimmerBluetoothManagerAndroid btManager;
    ShimmerDevice shimmerDevice;
    String shimmerBtAdd;

    ShimmerBluetoothManagerAndroid.BT_TYPE preferredBtType;
    final static String LOG_TAG = "BluetoothManagerExample";
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(new String[]{
                                Manifest.permission.BLUETOOTH_ADVERTISE,
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        0);
            }else{
                requestPermissions(new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        0);
            }
        }

// Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        boolean allPermissionsGranted = true;
        for (int result : grantResults){
            if(result != 0){
                allPermissionsGranted = false;
            }
        }
        if(!allPermissionsGranted){
            Toast.makeText(this, "Please allow all requested permissions", Toast.LENGTH_SHORT).show();
        }else{
            BleManager.getInstance().init(getApplication());
            try {
                btManager = new ShimmerBluetoothManagerAndroid(this, mHandler);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Couldn't create ShimmerBluetoothManagerAndroid. Error: " + e);
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }

    @Override
    protected void onStart() {
        /*
        //Connect the Shimmer using its Bluetooth Address
        try {
            btManager.connectShimmerThroughBTAddress(shimmerBtAdd);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error. Shimmer device not paired or Bluetooth is not enabled");
            Toast.makeText(this, "Error. Shimmer device not paired or Bluetooth is not enabled. " +
                            "Please close the app and pair or enable Bluetooth", Toast.LENGTH_LONG).show();
        }*/
        textView = (TextView) findViewById(R.id.textView);
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
                try {
                    shimmerDevice.stopStreaming();
                } catch (ShimmerException e) {
                    e.printStackTrace();
                }
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
                        Log.i(LOG_TAG, "Time Stamp: " + timeStampData);
                        allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X);
                        FormatCluster accelXCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(allFormats,"CAL"));
                        if (accelXCluster!=null) {
                            double accelXData = accelXCluster.mData;
                            Log.i(LOG_TAG, "Accel LN X: " + accelXData);
                        }
                    }
                    break;
                case Shimmer.MESSAGE_TOAST:
                    /** Toast messages sent from {@link Shimmer} are received here. E.g. device xxxx now streaming.
                     *  Note that display of these Toast messages is done automatically in the Handler in {@link com.shimmerresearch.android.shimmerService.ShimmerService} */
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Shimmer.TOAST), Toast.LENGTH_SHORT).show();
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
                            if(shimmerDevice == null) {
                                shimmerDevice = btManager.getShimmerDeviceBtConnectedFromMac(shimmerBtAdd);
                                Log.i(LOG_TAG, "Got the ShimmerDevice!");
                            }
                            break;
                        case DISCONNECTED:
                            Log.i(LOG_TAG, "Shimmer [" + macAddress + "] has been DISCONNECTED");
                            shimmerDevice = null;
                            break;
                    }
                    break;
            }

            super.handleMessage(msg);
        }
    };

    public void stopStreaming(View v){
        try {
            shimmerDevice.stopStreaming();
        } catch (ShimmerException e) {
            e.printStackTrace();
        }
    }

    public void startStreaming(View v){
        try {
            shimmerDevice.startStreaming();
        } catch (ShimmerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when the configurations button is clicked
     * @param v
     */
    public void openConfigMenu(View v){
        if(shimmerDevice != null) {
            if(!shimmerDevice.isStreaming() && !shimmerDevice.isSDLogging()) {
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
                ShimmerDialogConfigurations.buildShimmerSensorEnableDetails(shimmerDevice, MainActivity.this, btManager);
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
     * Called when the connect button is clicked
     * @param v
     */
    public void connectDevice(View v) {
        Intent intent = new Intent(getApplicationContext(), ShimmerBluetoothDialog.class);
        startActivityForResult(intent, ShimmerBluetoothDialog.REQUEST_CONNECT_SHIMMER);
    }

    /**
     * Called when the disconnect button is clicked
     * @param v
     */
    public void disconnectDevice(View v) {
        try {
            ((ShimmerBluetooth)shimmerDevice).disconnect();
        } catch (ShimmerException e) {
            throw new RuntimeException(e);
        }
    }

    public void startSDLogging(View v) {
        ((ShimmerBluetooth)shimmerDevice).writeConfigTime(System.currentTimeMillis());
        shimmerDevice.startSDLogging();
    }

    public void stopSDLogging(View v) {
        shimmerDevice.stopSDLogging();
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
                if(btManager==null){
                    try {
                        btManager = new ShimmerBluetoothManagerAndroid(this, mHandler);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Couldn't create ShimmerBluetoothManagerAndroid. Error: " + e);
                    }
                }
                btManager.disconnectAllDevices();   //Disconnect all devices first
                shimmerDevice = null;
                showBtTypeConnectionOption();
                //Get the Bluetooth mac address of the selected device:
                String macAdd = data.getStringExtra(EXTRA_DEVICE_ADDRESS);
                String deviceName = data.getStringExtra(EXTRA_DEVICE_NAME);
                btManager.connectShimmerThroughBTAddress(macAdd, deviceName, preferredBtType);   //Connect to the selected device
                shimmerBtAdd = macAdd;
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    Looper looper = Looper.myLooper();

    public void showBtTypeConnectionOption(){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setCancelable(false);
        alertDialog.setMessage("Choose preferred Bluetooth type");
        alertDialog.setButton( Dialog.BUTTON_POSITIVE, "BT CLASSIC", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                preferredBtType = ShimmerBluetoothManagerAndroid.BT_TYPE.BT_CLASSIC;
                looper.quit();
            };
        });
    alertDialog.setButton( Dialog.BUTTON_NEGATIVE, "BLE", new DialogInterface.OnClickListener()    {
                public void onClick(DialogInterface dialog, int which) {
                    preferredBtType = ShimmerBluetoothManagerAndroid.BT_TYPE.BLE;
                    looper.quit();
                };
    });
        alertDialog.show();
        try{ looper.loop(); }
        catch(RuntimeException e){}
    }
}
