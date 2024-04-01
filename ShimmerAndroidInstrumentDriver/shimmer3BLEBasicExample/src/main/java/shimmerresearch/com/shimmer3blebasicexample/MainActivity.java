package shimmerresearch.com.shimmer3blebasicexample;

import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.EXTRA_DEVICE_ADDRESS;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog;
import com.shimmerresearch.androidradiodriver.Shimmer3BLEAndroid;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerMsg;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.sensors.kionix.SensorKionixAccel;

import java.io.IOException;
import java.util.Collection;

public class MainActivity extends AppCompatActivity {
    final static int REQUEST_CONNECT_SHIMMER = 2;
    protected Handler mHandler;
    private final static String LOG_TAG = "Shimmer3BLEBasicExample";
    Shimmer3BLEAndroid shimmer1;
    String macAddress = "E8:EB:1B:97:67:FC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
        boolean permissionGranted = true;
        {
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                permissionGranted = false;
            }
            permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                permissionGranted = false;
            }
            permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                permissionGranted = false;
            }
            permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                permissionGranted = false;
            }
        }
        if (!permissionGranted) {
            // Should we show an explanation?
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION}, 110);

        }


        setContentView(R.layout.activity_main);
        BleManager.getInstance().init(getApplication());

        this.mHandler = handler;


    }

    public void connectDevice(View v) {
        Intent pairedDevicesIntent = new Intent(getApplicationContext(), ShimmerBluetoothDialog.class);
        startActivityForResult(pairedDevicesIntent, REQUEST_CONNECT_SHIMMER);
        //device1.setProtocol(Configuration.COMMUNICATION_TYPE.BLUETOOTH, protocol1);
        //shimmer1.connect("E8:EB:1B:97:67:FC", "default");
    }

    public void disconnectDevice(View v) {
        Thread thread = new Thread(){
            public void run(){
                try {
                    shimmer1.disconnect();
                } catch (ShimmerException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();


    }

    public void startStreaming(View v) throws InterruptedException, IOException, ShimmerException {
        Thread thread = new Thread(){
            public void run(){
                try {
                    shimmer1.startStreaming();
                } catch (ShimmerException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }

    public void stopStreaming(View v) throws IOException, ShimmerException {
        Thread thread = new Thread(){
            public void run(){
                shimmer1.stopStreaming();
            }
        };

        thread.start();

    }
    public void startSpeedTest(View v) throws IOException, ShimmerException {
        Thread thread = new Thread(){
            public void run(){
                //shimmer1.startSpeedTest();
            }
        };

        thread.start();

    }
    public void stopSpeedTest(View v) throws IOException, ShimmerException {
        Thread thread = new Thread(){
            public void run(){
                //shimmer1.stopSpeedTest();
            }
        };

        thread.start();

    }


    /**
     * Messages from the Shimmer device including sensor data are received here
     */
    Handler handler = new Handler() {

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
                        allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X);
                        FormatCluster accelYCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(allFormats,"CAL"));
                        if (accelYCluster!=null) {
                            double accelYData = accelYCluster.mData;
                            Log.i(LOG_TAG, "Accel LN Y: " + accelYData);
                        }
                        allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X);
                        FormatCluster accelZCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(allFormats,"CAL"));
                        if (accelZCluster!=null) {
                            double accelZData = accelZCluster.mData;
                            Log.i(LOG_TAG, "Accel LN Z: " + accelZData);
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
     * Get the result from the paired devices dialog
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CONNECT_SHIMMER) {
            if (resultCode == Activity.RESULT_OK) {
                //Get the Bluetooth mac address of the selected device:
                String macAdd = data.getStringExtra(EXTRA_DEVICE_ADDRESS);
                shimmer1 = new Shimmer3BLEAndroid(macAdd, this.mHandler);
                SensorDataReceived sdr = this.new SensorDataReceived();
                sdr.setWaitForData(shimmer1);

                Thread thread = new Thread(){
                    public void run(){
                        shimmer1.connect(macAdd, "default");
                    }
                };

                thread.start();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public class SensorDataReceived extends BasicProcessWithCallBack {

        @Override
        protected void processMsgFromCallback(ShimmerMsg shimmerMSG) {
            // TODO Auto-generated method stub
            System.out.println(shimmerMSG.mIdentifier);


            // TODO Auto-generated method stub

            // TODO Auto-generated method stub
            int ind = shimmerMSG.mIdentifier;

            Object object = (Object) shimmerMSG.mB;

            if (ind == Shimmer.MSG_IDENTIFIER_STATE_CHANGE) {
                CallbackObject callbackObject = (CallbackObject)object;

                if (callbackObject.mState == ShimmerBluetooth.BT_STATE.CONNECTING) {
                } else if (callbackObject.mState == ShimmerBluetooth.BT_STATE.CONNECTED) {} else if (callbackObject.mState == ShimmerBluetooth.BT_STATE.DISCONNECTED
//						|| callbackObject.mState == BT_STATE.NONE
                        || callbackObject.mState == ShimmerBluetooth.BT_STATE.CONNECTION_LOST){

                }
            } else if (ind == Shimmer.MSG_IDENTIFIER_NOTIFICATION_MESSAGE) {
                CallbackObject callbackObject = (CallbackObject)object;
                int msg = callbackObject.mIndicator;
                if (msg== Shimmer.NOTIFICATION_SHIMMER_FULLY_INITIALIZED){}
                if (msg == Shimmer.NOTIFICATION_SHIMMER_STOP_STREAMING) {

                } else if (msg == Shimmer.NOTIFICATION_SHIMMER_START_STREAMING) {

                } else {}
            } else if (ind == Shimmer.MSG_IDENTIFIER_DATA_PACKET) {

                double accelX = 0;
                double accelY = 0;
                double accelZ = 0;
                FormatCluster formatx;
                FormatCluster formaty;
                FormatCluster formatz;

                int INVALID_RESULT = -1;

                ObjectCluster objc = (ObjectCluster) shimmerMSG.mB;

                Collection<FormatCluster> adcFormats = objc.getCollectionOfFormatClusters(SensorKionixAccel.ObjectClusterSensorName.ACCEL_LN_X);
                formatx = ((FormatCluster)ObjectCluster.returnFormatCluster(adcFormats, ChannelDetails.CHANNEL_TYPE.CAL.toString())); // retrieve the calibrated data

                adcFormats = objc.getCollectionOfFormatClusters(SensorKionixAccel.ObjectClusterSensorName.ACCEL_LN_Y);
                formaty = ((FormatCluster)ObjectCluster.returnFormatCluster(adcFormats, ChannelDetails.CHANNEL_TYPE.CAL.toString())); // retrieve the calibrated data

                adcFormats = objc.getCollectionOfFormatClusters(SensorKionixAccel.ObjectClusterSensorName.ACCEL_LN_Z);
                formatz = ((FormatCluster)ObjectCluster.returnFormatCluster(adcFormats, ChannelDetails.CHANNEL_TYPE.CAL.toString())); // retrieve the calibrated data

                if(formatx != null) {
                    System.out.println("X:"+formatx.mData +" Y:"+formaty.mData+" Z:"+formatz.mData);

                }
                else {
                    System.out.println("ERROR! FormatCluster is Null!");
                }

            } else if (ind == Shimmer.MSG_IDENTIFIER_PACKET_RECEPTION_RATE_OVERALL) {

            }





        }

    }
}