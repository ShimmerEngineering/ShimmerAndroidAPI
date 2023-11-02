package shimmerresearch.com.verisenseblebasicexample;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.VerisenseDeviceAndroid;
import com.shimmerresearch.androidradiodriver.BluetoothLeService;
import com.shimmerresearch.androidradiodriver.VerisenseBLERadioByteCommunication;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.verisense.communication.VerisenseProtocolByteCommunication;
import com.shimmerresearch.verisense.sensors.SensorLIS2DW12;

import java.io.IOException;
import java.util.Collection;
import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.EXTRA_DEVICE_ADDRESS;

public class MainActivity extends AppCompatActivity {

    private final static String LOG_TAG = "VerisenseBLEBasicEx";
    VerisenseDeviceAndroid device1a;
    VerisenseBLERadioByteCommunication radio1a;
    VerisenseProtocolByteCommunication protocol1a;
    final static String SERVICE_TAG = "BluetoothLeService";
    BluetoothLeService mService;
    boolean isServiceStarted = false;
    BluetoothAdapter btAdapter;
    //String deviceAddress = "C9:61:17:53:74:02";
    String deviceAddress = "C0:04:19:85:9A:D5";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!= PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
            }
        }*/

        //Check if Bluetooth is enabled

        if (!btAdapter.isEnabled()) {
            int REQUEST_ENABLE_BT = 1;
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            Intent intent = new Intent(this, BluetoothLeService.class);
            startService(intent);
            getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            Log.d(LOG_TAG, "BluetoothLe Service started");
            Toast.makeText(this, "BluetoothLe Service started", Toast.LENGTH_SHORT).show();
        }
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mService = ((BluetoothLeService.LocalBinder) service).getService();

            radio1a = new VerisenseBLERadioByteCommunication(deviceAddress,mService);
            protocol1a = new VerisenseProtocolByteCommunication(radio1a);
            device1a = new VerisenseDeviceAndroid(mHandler);

            isServiceStarted = true;
            Log.d(SERVICE_TAG, "BluetoothLe Service Bound");
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mService = null;
            isServiceStarted = false;
            Log.d(SERVICE_TAG, "BluetoothLe Service Disconnected");
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                //connected = true;
                //updateConnectionState(R.string.connected);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                //connected = false;
                //updateConnectionState(R.string.disconnected);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mService.getSupportedGattServices());
            }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mService != null) {
            final boolean result = mService.connect(deviceAddress);
            Log.d(LOG_TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    public void connectDevice(View v) {

        Thread thread = new Thread(){
            public void run(){

                //device1.setProtocol(Configuration.COMMUNICATION_TYPE.BLUETOOTH, protocol1);
                device1a.setProtocol(Configuration.COMMUNICATION_TYPE.BLUETOOTH, protocol1a);
                try {
                    //device1.connect();
                    device1a.connect();
                } catch (ShimmerException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }
        };

        thread.start();
    }

    public void disconnectDevice(View v) {
        Thread thread = new Thread(){
            public void run(){
                try {
                    device1a.disconnect();
                } catch (ShimmerException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();


    }

    public void readOpConfig(View v) {
        Thread thread = new Thread(){
            public void run(){
                try {
                    protocol1a.readOperationalConfig();
                } catch (ShimmerException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();




    }

    public void readProdConfig(View v)  {
        Thread thread = new Thread(){
            public void run(){
                try {
                    protocol1a.readProductionConfig();
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
                    protocol1a.startStreaming();
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
                try {
                    protocol1a.stopStreaming();
                } catch (ShimmerException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();

    }
    public void startSpeedTest(View v) throws IOException, ShimmerException {
        Thread thread = new Thread(){
            public void run(){
                try {
                    protocol1a.startSpeedTest();
                } catch (ShimmerException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();

    }
    public void stopSpeedTest(View v) throws IOException, ShimmerException {
        Thread thread = new Thread(){
            public void run(){
                try {
                    protocol1a.stopSpeedTest();
                } catch (ShimmerException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();

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
                        allFormats = objectCluster.getCollectionOfFormatClusters(SensorLIS2DW12.ObjectClusterSensorName.LIS2DW12_ACC_X);
                        FormatCluster accelXCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(allFormats,"CAL"));
                        if (accelXCluster!=null) {
                            double accelXData = accelXCluster.mData;
                            Log.i(LOG_TAG, "Accel X: " + accelXData);
                        }
                        allFormats = objectCluster.getCollectionOfFormatClusters(SensorLIS2DW12.ObjectClusterSensorName.LIS2DW12_ACC_Y);
                        FormatCluster accelYCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(allFormats,"CAL"));
                        if (accelXCluster!=null) {
                            double accelYData = accelYCluster.mData;
                            Log.i(LOG_TAG, "Accel Y: " + accelYData);
                        }
                        allFormats = objectCluster.getCollectionOfFormatClusters(SensorLIS2DW12.ObjectClusterSensorName.LIS2DW12_ACC_Z);
                        FormatCluster accelZCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(allFormats,"CAL"));
                        if (accelZCluster!=null) {
                            double accelZData = accelZCluster.mData;
                            Log.i(LOG_TAG, "Accel Z: " + accelZData);
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
        if(requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                //Get the Bluetooth mac address of the selected device:
                String macAdd = data.getStringExtra(EXTRA_DEVICE_ADDRESS);

            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}