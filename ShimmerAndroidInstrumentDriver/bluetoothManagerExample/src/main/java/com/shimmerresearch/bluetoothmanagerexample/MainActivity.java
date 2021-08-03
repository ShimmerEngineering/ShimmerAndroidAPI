package com.shimmerresearch.bluetoothmanagerexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog;
import com.shimmerresearch.android.guiUtilities.ShimmerDialogConfigurations;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.Configuration.CHANNEL_UNITS;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Random;

import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.EXTRA_DEVICE_ADDRESS;

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
    String shimmerBtAdd = "00:00:00:00:00:00";  //Put the address of the Shimmer device you want to connect here

    final static String LOG_TAG = "BluetoothManagerExample";
    TextView textView;

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
            btManager.connectShimmerThroughBTAddress(shimmerBtAdd);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error. Shimmer device not paired or Bluetooth is not enabled");
            Toast.makeText(this, "Error. Shimmer device not paired or Bluetooth is not enabled. " +
                            "Please close the app and pair or enable Bluetooth", Toast.LENGTH_LONG).show();
        }
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void startStreaming(View v){
//        shimmerDevice.startStreaming();
        startSendingOjcBroadcast();
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
                btManager.disconnectAllDevices();   //Disconnect all devices first
                shimmerDevice = null;
                //Get the Bluetooth mac address of the selected device:
                String macAdd = data.getStringExtra(EXTRA_DEVICE_ADDRESS);
                btManager.connectShimmerThroughBTAddress(macAdd);   //Connect to the selected device
                shimmerBtAdd = macAdd;
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void startSendingOjcBroadcast() {
//        for(int i =0;i<1920000;i++) {
        for(int i =0;i<600000;i++) {

//            busySleep(125000); // 8KHz
//            busySleep(1953125); // 512 Hz
            busySleep(500000); // 2KHz

            ObjectCluster objectCluster = new ObjectCluster();
            objectCluster.setShimmerName("DummyShimmer");
            objectCluster.setMacAddress("0000");
            objectCluster.setTimeStampMilliSecs(System.currentTimeMillis());

            //GSR_Skin_Conductance
            //System_Timestamp
            //PPGtoHR
            //Fusion_Response
            objectCluster.addDataToMap("System_Timestamp", CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLISECONDS,System.currentTimeMillis());
            objectCluster.addDataToMap("GSR_Skin_Conductance", CHANNEL_TYPE.CAL.toString(), CHANNEL_UNITS.U_SIEMENS,1.0+getRandomNumberUsingInts(1,10)/10.0);
            objectCluster.addDataToMap("PPGtoHR",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.BEATS_PER_MINUTE,getRandomNumberUsingInts(50,60));
            objectCluster.addDataToMap("Fusion_Response",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.NO_UNITS,getRandomNumberUsingInts(0,3));

            objectCluster.addDataToMap("ECG_LA-RA_24BIT",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS, getRandomNumberUsingInts(1000,2000));
            objectCluster.addDataToMap("ECG_LL-RA_24BIT",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS, getRandomNumberUsingInts(1000,2000));
            objectCluster.addDataToMap("ECG_LL-LA_24BIT",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS, getRandomNumberUsingInts(1000,2000));
            objectCluster.addDataToMap("ECG_Vx-RL_24BIT",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS, getRandomNumberUsingInts(1000,2000));

            objectCluster.addDataToMap("ECG_LA-RA_16BIT",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS, getRandomNumberUsingInts(1000,2000));
            objectCluster.addDataToMap("ECG_LL-RA_16BIT",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS, getRandomNumberUsingInts(1000,2000));
            objectCluster.addDataToMap("ECG_LL-LA_16BIT",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS, getRandomNumberUsingInts(1000,2000));
            objectCluster.addDataToMap("ECG_Vx-RL_16BIT",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS, getRandomNumberUsingInts(1000,2000));

            objectCluster.addDataToMap("ECG_LA-RA_8BIT",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS, getRandomNumberUsingInts(1000,2000));
            objectCluster.addDataToMap("ECG_LL-RA_8BIT",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS, getRandomNumberUsingInts(1000,2000));
            objectCluster.addDataToMap("ECG_LL-LA_8BIT",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS, getRandomNumberUsingInts(1000,2000));
            objectCluster.addDataToMap("ECG_Vx-RL_8BIT",CHANNEL_TYPE.CAL.toString(),CHANNEL_UNITS.MILLIVOLTS, getRandomNumberUsingInts(1000,2000));

//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            ObjectOutputStream out = null;
            try {
//                out = new ObjectOutputStream(bos);
//                out.writeObject(objectCluster.buildProtoBufMsg());
//                out.flush();
//                byte[] bytes = bos.toByteArray();
//                out.close();

                Intent intent = new Intent();
                intent.setAction("com.example.broadcast.MY_NOTIFICATION");
//                intent.putExtra("ObjectCluster", bytes);
                intent.putExtra("ObjectCluster", objectCluster.serialize());
                sendOrderedBroadcast(intent, null);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void busySleep(long nanos) {
        long elapsed;
        final long startTime = System.nanoTime();
        do {
            elapsed = System.nanoTime() - startTime;
        } while (elapsed < nanos);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static int getRandomNumberUsingInts(int min, int max) {
        Random random = new Random();
        return random.ints(min, max)
                .findFirst()
                .getAsInt();
    }

}
