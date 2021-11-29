package shimmerresearch.com.shimmerconnectiontest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import shimmerresearch.com.shimmerconnectiontest.BuildConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.EXTRA_DEVICE_ADDRESS;

public class MainActivity extends Activity {

    private final static String LOG_TAG = "ShimmerConnectionTest";

    private EditText editTextShimmerStatus;
    private EditText editTextInterval;
    private EditText editTextSuccessCount;
    private EditText editTextFailureCount;
    private EditText editTextTotalIteration;
    private EditText editTextTestProgress;
    private EditText editTextFirmware;
    private TextView textViewVersion;

    private int successCount = 0;
    private int failureCount = 0;
    private int totalIteration = 0;
    private int currentIteration = 0;

    private Shimmer shimmer;
    private String macAdd;
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
        textViewVersion = (TextView) findViewById(R.id.version);

        textViewVersion.setText("Shimmer3 BT Connection Test v" + BuildConfig.VERSION_NAME);
        editTextTotalIteration.setText("100");
        editTextInterval.setText("10");
    }

    public void startTest(View v){
        if(!isTestStarted){
            Intent intent = new Intent(getApplicationContext(), ShimmerBluetoothDialog.class);
            startActivityForResult(intent, ShimmerBluetoothDialog.REQUEST_CONNECT_SHIMMER);
        }
    }

    public void stopTest(View v){
        if(isTestStarted){
            editTextInterval.setEnabled(true);
            editTextTotalIteration.setEnabled(true);
            if(shimmer != null){
                shimmer.disconnect();
            }
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
                            editTextShimmerStatus.setText("CONNECTED");
                            editTextFirmware.setText(shimmer.getFirmwareVersionParsed());
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
                shimmer = new Shimmer(mHandler);

                totalIteration = Integer.parseInt(editTextTotalIteration.getText().toString());
                currentIteration = 0;
                successCount = 0;
                failureCount = 0;
                editTextFailureCount.setText(String.valueOf(failureCount));
                editTextSuccessCount.setText(String.valueOf(successCount));
                editTextInterval.setEnabled(false);
                editTextTotalIteration.setEnabled(false);
                isTestStarted = true;

                timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if(currentIteration > 0){
                            if(shimmer.getBluetoothRadioState() != ShimmerBluetooth.BT_STATE.CONNECTED){
                                failureCount += 1;
                                runOnUiThread(new  Runnable() {
                                    public void run() {
                                        editTextFailureCount.setText(String.valueOf(failureCount));
                                    }
                                });
                            }
                            else{
                                successCount += 1;
                                runOnUiThread(new  Runnable() {
                                    public void run() {
                                        editTextSuccessCount.setText(String.valueOf(successCount));
                                    }
                                });
                            }
                        }

                        if(shimmer != null){
                            shimmer.disconnect();
                        }

                        if(currentIteration == totalIteration){
                            runOnUiThread(new  Runnable() {
                                public void run() {
                                    editTextInterval.setEnabled(true);
                                    editTextTotalIteration.setEnabled(true);
                                }
                            });
                            timer.cancel();
                            isTestStarted = false;
                            return;
                        }

                        runOnUiThread(new  Runnable() {
                            public void run() {
                                editTextTestProgress.setText(String.valueOf(currentIteration + 1) + " of " + String.valueOf(totalIteration));
                                currentIteration += 1;
                            }
                        });

                        shimmer.connect(macAdd, "default");
                    }
                }, 0, Integer.parseInt(editTextInterval.getText().toString()) * 1000);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
