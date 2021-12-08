package shimmerresearch.com.shimmerconnectiontest;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.exceptions.ShimmerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import android.widget.EditText;

import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.EXTRA_DEVICE_ADDRESS;

public class MainActivity extends Activity {

    private final static String LOG_TAG = "ShimmerConnectionTest";

    ShimmerBluetoothManagerAndroid btManager;

    private EditText editTextShimmerStatus;
    private EditText editTextInterval;
    private EditText editTextSuccessCount;
    private EditText editTextFailureCount;
    private EditText editTextTotalIteration;
    private EditText editTextTestProgress;
    private EditText editTextFirmware;
    private EditText editTextAndroidDeviceModel;
    private EditText editTextShimmerDeviceName;
    private EditText editTextRetryCountLimit;
    private EditText editTextRetryCount;
    private EditText editTextTotalRetries;

    private int interval = 0;
    private int successCount = 0;
    private int failureCount = 0;
    private int totalIteration = 0;
    private int currentIteration = 0;
    private int retryCount = 0;
    private int retryCountLimit = 5;
    private int totalRetries = 0;
    private boolean wasConnecting = false;


    //private Shimmer shimmer;
    private String macAdd;
    private boolean isCurrentIterationSuccess;
    private boolean isTestStarted = false;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextShimmerStatus = (EditText) findViewById(R.id.shimmerStatus);
        editTextInterval = (EditText) findViewById(R.id.interval);
        editTextSuccessCount = (EditText) findViewById(R.id.success);
        editTextFailureCount = (EditText) findViewById(R.id.fail);
        editTextTotalIteration = (EditText) findViewById(R.id.testIterations);
        editTextTestProgress = (EditText) findViewById(R.id.testProgress);
        editTextFirmware = (EditText) findViewById(R.id.firmware);
        editTextAndroidDeviceModel = (EditText) findViewById(R.id.androidDeviceModel);
        editTextShimmerDeviceName = (EditText) findViewById(R.id.shimmerDeviceName);
        editTextRetryCountLimit = (EditText) findViewById(R.id.retryCountLimit);
        editTextRetryCount = (EditText) findViewById(R.id.retryCount);
        editTextTotalRetries = (EditText) findViewById(R.id.totalRetries);

        editTextAndroidDeviceModel.setText(Build.MANUFACTURER + " " + Build.PRODUCT);
        editTextRetryCountLimit.setText(Integer.toString(retryCountLimit));
        editTextTotalIteration.setText("5");
        editTextInterval.setText("1");
    }

    public void startTest(View v){
        if(!isTestStarted){
            totalRetries = 0;
            if (btManager.getShimmer(macAdd)!=null) {
                btManager.disconnectShimmer(macAdd);
            }
            Intent intent = new Intent(getApplicationContext(), ShimmerBluetoothDialog.class);
            startActivityForResult(intent, ShimmerBluetoothDialog.REQUEST_CONNECT_SHIMMER);
        }
    }

    public void stopTest(View v){
        if(isTestStarted){
            editTextInterval.setEnabled(true);
            editTextTotalIteration.setEnabled(true);
            btManager.disconnectShimmer(macAdd);
            btManager.removeShimmerDeviceBtConnected(macAdd);
            editTextRetryCountLimit.setEnabled(true);

            if(timer != null){
                timer.cancel();
            }
            isTestStarted = false;
        }
    }

    /**
     * Messages from the Shimmer device including sensor data are received here
     */
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case ShimmerBluetooth.MSG_IDENTIFIER_NOTIFICATION_MESSAGE:
                    if (((CallbackObject)msg.obj).mIndicator==ShimmerBluetooth.NOTIFICATION_SHIMMER_FULLY_INITIALIZED) {

                            editTextFirmware.setText(btManager.getShimmer(macAdd).getFirmwareVersionParsed());
                            successCount += 1;
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    editTextSuccessCount.setText(String.valueOf(successCount));
                                }
                            });
                            Log.i(LOG_TAG, "Success Count: " + successCount);
                            btManager.disconnectShimmer(macAdd);
                        if (isTestStarted) {
                            timer = new Timer();
                            timer.schedule(new ConnectTask(), Integer.parseInt(editTextInterval.getText().toString()) * 1000);
                        }
                    }
                    break;
                case ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET:
                    if ((msg.obj instanceof ObjectCluster)) {

                        //Print data to Logcat
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

                    switch (state) {
                        case CONNECTED:
                            wasConnecting = false;
                            editTextShimmerStatus.setText("CONNECTED");
                            if (btManager.getShimmer(macAdd)!=null) {
                                editTextFirmware.setText(btManager.getShimmer(macAdd).getFirmwareVersionParsed());
                            }
                            editTextShimmerDeviceName.setText(((ObjectCluster) msg.obj).getShimmerName());
                            break;
                        case CONNECTING:
                            editTextShimmerStatus.setText("CONNECTING");
                            break;
                        case STREAMING:
                            editTextShimmerStatus.setText("STREAMING");
                            break;
                        case STREAMING_AND_SDLOGGING:
                            editTextShimmerStatus.setText("STREAMING AND LOGGING");
                            break;
                        case SDLOGGING:
                            editTextShimmerStatus.setText("SDLOGGING");
                            break;
                        case DISCONNECTED:
                            editTextShimmerStatus.setText("DISCONNECTED");
                            if (wasConnecting){
                                if (retryCount<retryCountLimit) {
                                    retryCount++;
                                    totalRetries++;
                                    editTextRetryCount.setText(Integer.toString(retryCount));
                                    editTextTotalRetries.setText(Integer.toString(totalRetries));
                                    Log.i(LOG_TAG, "Retry Count: " + Integer.toString(retryCount) + "; Total number of retries:" + totalRetries);
                                    //Toast.makeText(getApplicationContext(), "Retry Count " + Integer.toString(retryCount), Toast.LENGTH_SHORT).show();
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    btManager.removeShimmerDeviceBtConnected(macAdd);
                                    btManager.connectShimmerThroughBTAddress(macAdd);
                                    wasConnecting = true;
                                } else {
                                    retryCount = 0;
                                    failureCount += 1;
                                    Log.i(LOG_TAG, "Failure Count: " + failureCount);
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            editTextFailureCount.setText(String.valueOf(failureCount));
                                        }
                                    });
                                    timer.cancel();
                                    if (isTestStarted) {
                                        timer = new Timer();
                                        timer.schedule(new ConnectTask(), Integer.parseInt(editTextInterval.getText().toString()) * 1000);
                                    }
                                }
                            }
                            break;
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

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
                //Get the Bluetooth mac address of the selected device:
                macAdd = data.getStringExtra(EXTRA_DEVICE_ADDRESS);
                try {
                    btManager = new ShimmerBluetoothManagerAndroid(this, mHandler);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                totalIteration = Integer.parseInt(editTextTotalIteration.getText().toString());
                retryCountLimit = Integer.parseInt(editTextRetryCountLimit.getText().toString());
                currentIteration = 0;
                successCount = 0;
                failureCount = 0;
                editTextFailureCount.setText(String.valueOf(failureCount));
                editTextSuccessCount.setText(String.valueOf(successCount));
                editTextTotalRetries.setText(String.valueOf(totalRetries));
                editTextTestProgress.setText("0 of" + String.valueOf(totalIteration));
                editTextInterval.setEnabled(false);
                editTextTotalIteration.setEnabled(false);
                editTextRetryCountLimit.setEnabled(false);
                isCurrentIterationSuccess = true;
                isTestStarted = true;

                timer = new Timer();
                timer.schedule(new ConnectTask(),0);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public class ConnectTask extends  TimerTask{
        @Override
        public void run() {
            Log.i(LOG_TAG, "Current Iteration: " + currentIteration);
            if (isTestStarted) {
                Log.i(LOG_TAG, "Current Iteration: " + currentIteration);
                retryCount = 0;
                editTextRetryCount.setText(Integer.toString(retryCount));

                if (btManager.getShimmer(macAdd) != null) {
                    try {
                        btManager.getShimmer(macAdd).disconnect();
                    } catch (ShimmerException e) {
                        e.printStackTrace();
                    }
                }

                if (currentIteration == totalIteration) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            editTextInterval.setEnabled(true);
                            editTextTotalIteration.setEnabled(true);
                        }
                    });
                    timer.cancel();
                    isTestStarted = false;
                    return;
                }
                if (currentIteration == totalIteration) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            editTextInterval.setEnabled(true);
                            editTextTotalIteration.setEnabled(true);
                        }
                    });
                    timer.cancel();
                    isTestStarted = false;
                    return;
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            editTextTestProgress.setText(String.valueOf(currentIteration + 1) + " of " + String.valueOf(totalIteration));
                            currentIteration += 1;
                        }
                    });
                }
                //isCurrentIterationSuccess = false;
                btManager.removeShimmerDeviceBtConnected(macAdd);
                btManager.connectShimmerThroughBTAddress(macAdd);
                wasConnecting = true;
            }
        }
    }


}
