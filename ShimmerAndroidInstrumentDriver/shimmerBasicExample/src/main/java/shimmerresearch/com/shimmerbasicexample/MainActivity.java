package shimmerresearch.com.shimmerbasicexample;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.exceptions.ShimmerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.EXTRA_DEVICE_ADDRESS;

public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener {

    private final static String LOG_TAG = "ShimmerBasicExample";
    Shimmer shimmer;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean permissionGranted = true;
        int permissionCheck = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                permissionGranted = false;
            }
            permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                permissionGranted = false;
            }
        } else {
            permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                permissionGranted = false;
            }
        }
        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            permissionGranted = false;
        }
        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            permissionGranted = false;
        }


        if (!permissionGranted) {
            // Should we show an explanation?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 110);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 110);
            }
        } else {

        }
        shimmer = new Shimmer(mHandler,MainActivity.this);
        spinner = (Spinner) findViewById(R.id.crcSpinner);
        spinner.setEnabled(false);
        // Spinner click listener
        spinner.setOnItemSelectedListener(this);
        List<String> categories = new ArrayList<String>();
        categories.add("Disable crc");
        categories.add("Enable 1 byte CRC");
        categories.add("Enable 2 byte CRC");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(spinner.isEnabled()){
            switch(position) {
                case 0:
                    shimmer.disableBtCommsCrc();
                    break;
                case 1:
                    shimmer.enableBtCommsOneByteCrc();
                    break;
                case 2:
                    shimmer.enableBtCommsTwoByteCrc();
                    break;
                default:
            }
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void connectDevice(View v) {
        Intent intent = new Intent(getApplicationContext(), ShimmerBluetoothDialog.class);
        startActivityForResult(intent, ShimmerBluetoothDialog.REQUEST_CONNECT_SHIMMER);
    }

    public void disconnectDevice(View v) {
        if (shimmer!=null){
            shimmer.disconnect();
        }
    }

    public void startStreaming(View v) throws InterruptedException, IOException{
        try {
            shimmer.startStreaming();
        } catch (ShimmerException e) {
            e.printStackTrace();
        }
    }

    public void stopStreaming(View v) throws IOException{
        shimmer.stopStreaming();
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

                        Collection<FormatCluster> allFormatsPRR = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.PACKET_RECEPTION_RATE_OVERALL);
                        FormatCluster prrCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(allFormatsPRR,"CAL"));
                        double PRR = prrCluster.mData;
                        Log.i(LOG_TAG, "Packet Reception Rate: " + PRR);

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
                            if(shimmer.getFirmwareVersionCode() >= 8){
                                if(!spinner.isEnabled()){
                                    switch(shimmer.getCurrentBtCommsCrcMode()) {
                                        case OFF:
                                            spinner.setSelection(0);
                                            break;
                                        case ONE_BYTE_CRC:
                                            spinner.setSelection(1);
                                            break;
                                        case TWO_BYTE_CRC:
                                            spinner.setSelection(2);
                                            break;
                                        default:
                                    }
                                }
                                spinner.setEnabled(true);
                            }
                            break;
                        case CONNECTING:
                            break;
                        case STREAMING:
                            spinner.setEnabled(false);
                            break;
                        case STREAMING_AND_SDLOGGING:
                            break;
                        case SDLOGGING:
                            break;
                        case DISCONNECTED:
                            spinner.setEnabled(false);
                            spinner.setSelection(0);
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
                shimmer = new Shimmer(mHandler,MainActivity.this);
                shimmer.connect(macAdd, "default");                  //Connect to the selected device
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
