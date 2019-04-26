package com.shimmerresearch.efficientdataarrayexample;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.guiUtilities.FileListActivity;
import com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driverUtilities.ChannelDetails;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.EXTRA_DEVICE_ADDRESS;
import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.REQUEST_CONNECT_SHIMMER;

/**
 * This example demonstrates the use of the arrays data structure, {@link ObjectCluster#sensorDataArray}, in the following scenarios:
 * <ul>
 * <li>Enabling the arrays data structure</li>
 * <li>Different methods of retrieving signal data from the arrays</li>
 * <li>Writing the signal data to a CSV file</li>
 * </ul>
 * Note: The arrays data structure is an alternative to the standard Multimap ({@link ObjectCluster#mPropertyCluster}) data structure.
 * Switching to using the arrays can improve packet reception rate on slower Android devices.
 */
public class MainActivity extends Activity {

    ShimmerBluetoothManagerAndroid btManager;
    private String bluetoothAdd = "";
    private final static String LOG_TAG = "ArraysExample";
    private final static String CSV_FILE_NAME_PREFIX = "Data";
    private final static String APP_FOLDER_NAME = "ShimmerArraysExample";
    private final static String APP_DIR_PATH = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + APP_FOLDER_NAME + File.separator;
    /**
     * This can be found in the Manifest
     */
    private final static String APP_FILE_PROVIDER_AUTHORITY = "com.shimmerresearch.efficientdataarrayexample.fileprovider";
    private final static int PERMISSIONS_REQUEST_WRITE_STORAGE = 5;

    //Write to CSV variables
    private FileWriter fw;
    private BufferedWriter bw;
    private File file;
    boolean firstTimeWrite = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            btManager = new ShimmerBluetoothManagerAndroid(this, mHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Check if permission to write to external storage has been granted
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_WRITE_STORAGE);
            }
        }

    }

    public void connectDevice(View v) {
        Intent intent = new Intent(getApplicationContext(), ShimmerBluetoothDialog.class);
        startActivityForResult(intent, REQUEST_CONNECT_SHIMMER);
    }

    public void startStreaming(View v) {
        Shimmer shimmer = (Shimmer) btManager.getShimmer(bluetoothAdd);
        if (shimmer != null) {   //this is null if Shimmer device is not connected
            setupCSV();
            //Disable PC timestamps for better performance. Disabling this takes the timestamps on every full packet received instead of on every byte received.
            shimmer.enablePCTimeStamps(false);
            //Enable the arrays data structure. Note that enabling this will disable the Multimap/FormatCluster data structure
            shimmer.enableArraysDataStructure(true);
            btManager.startStreaming(bluetoothAdd);
        } else {
            Toast.makeText(this, "Can't start streaming\nShimmer device is not connected", Toast.LENGTH_SHORT).show();
        }
    }

    public void stopStreaming(View v) {
        if (btManager.getShimmer(bluetoothAdd) != null) {
            try {   //Stop CSV writing
                bw.flush();
                bw.close();
                fw.close();
                firstTimeWrite = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            btManager.stopStreaming(bluetoothAdd);
        } else {
            Toast.makeText(this, "Can't stop streaming\nShimmer device is not connected", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get the result from the paired devices dialog
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                //Get the Bluetooth mac address of the selected device:
                bluetoothAdd = data.getStringExtra(EXTRA_DEVICE_ADDRESS);
                btManager.connectShimmerThroughBTAddress(bluetoothAdd); //Connect to the selected device
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET:
                    if ((msg.obj instanceof ObjectCluster)) {
                        ObjectCluster objc = (ObjectCluster) msg.obj;

                        /**
                         * ---------- Printing a channel to Logcat ----------
                         */
                        //Method 1 - retrieve data from the ObjectCluster using get method
                        double data = objc.getFormatClusterValue(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X, ChannelDetails.CHANNEL_TYPE.CAL.toString());
                        Log.i(LOG_TAG, Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X + " data: " + data);

                        //Method 2a - retrieve data from the ObjectCluster by manually parsing the arrays
                        int index = -1;
                        for (int i = 0; i < objc.sensorDataArray.mSensorNames.length; i++) {
                            if (objc.sensorDataArray.mSensorNames[i] != null) {
                                if (objc.sensorDataArray.mSensorNames[i].equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X)) {
                                    index = i;
                                }
                            }
                        }
                        if (index != -1) {
                            //Index was found
                            data = objc.sensorDataArray.mCalData[index];
                            Log.w(LOG_TAG, Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X + " data: " + data);
                        }

                        //Method 2b - retrieve data from the ObjectCluster by getting the index, then accessing the arrays
                        index = objc.getIndexForChannelName(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X);
                        if (index != -1) {
                            data = objc.sensorDataArray.mCalData[index];
                            Log.e(LOG_TAG, Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X + " data: " + data);
                        }

                        /**
                         * ---------- Writing all channels of CAL data to CSV file ----------
                         */
                        if (firstTimeWrite) {
                            //Write headers on first-time
                            for (String channelName : objc.sensorDataArray.mSensorNames) {
                                try {
                                    bw.write(channelName + ",");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                bw.write("\n");
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }
                            firstTimeWrite = false;
                        }
                        for (double calData : objc.sensorDataArray.mCalData) {
                            String dataString = String.valueOf(calData);
                            try {
                                bw.write(dataString + ",");
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                        try {
                            bw.write("\n");
                        } catch (IOException e2) {
                            e2.printStackTrace();
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
                    switch (state) {
                        case CONNECTED:
                            break;
                        case CONNECTING:
                            break;
                        case STREAMING:
                            break;
                        case STREAMING_AND_SDLOGGING:
                            break;
                        case SDLOGGING:
                            break;
                        case DISCONNECTED:
                            break;
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * Permission request callback
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_WRITE_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Error! Permission not granted. App will now close", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    /**
     * Setup CSV writing
     */
    private void setupCSV() {
        File dir = new File(APP_DIR_PATH);
        if (!dir.exists()) {
            //Create the directory if it doesn't already exist
            dir.mkdir();
        }

        String fileName = CSV_FILE_NAME_PREFIX + "_" + new SimpleDateFormat("dd-MM-yy_HHmm").format(new Date()) + ".csv";
        String filePath = APP_DIR_PATH + File.separator + fileName;
        file = new File(filePath);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file.getAbsoluteFile());
            bw = new BufferedWriter(fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Launch the files list activity, which is themed as a dialog in the Android Manifest
     *
     * @param v
     */
    public void openLogFilesList(View v) {
        Intent intent = new Intent(getApplicationContext(), FileListActivity.class);
        intent.putExtra(FileListActivity.INTENT_EXTRA_DIR_PATH, APP_DIR_PATH);
        intent.putExtra(FileListActivity.INTENT_EXTRA_PROVIDER_AUTHORITY, APP_FILE_PROVIDER_AUTHORITY);
        startActivityForResult(intent, REQUEST_CONNECT_SHIMMER);
    }

}
