package com.shimmerresearch.shimmerserviceexample;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog;
import com.shimmerresearch.android.guiUtilities.ShimmerDialogConfigurations;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.android.shimmerService.ShimmerService;

public class MainActivity extends AppCompatActivity {

    ShimmerBluetoothManagerAndroid btManager;
    BluetoothAdapter btAdapter;
    ShimmerService mService;
    Handler mHandler;

    final static String LOG_TAG = "Shimmer";
    final static String SERVICE_TAG = "ShimmerService";
    final static int REQUEST_CONNECT_SHIMMER = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btAdapter = BluetoothAdapter.getDefaultAdapter();


//        else {
//            try {
//                btManager = new ShimmerBluetoothManagerAndroid(this, mHandler);
//            } catch (Exception e) {
//                Log.e(LOG_TAG, "Couldn't create ShimmerBluetoothManagerAndroid. Error: " + e);
//            }
//        }
    }


    @Override
    protected void onStart() {
        //Check if Bluetooth is enabled
        if (!btAdapter.isEnabled()) {
            int REQUEST_ENABLE_BT = 1;
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            //Start the Shimmer service
            Intent intent = new Intent(this, ShimmerService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            Log.d(LOG_TAG, "Shimmer Service started");
            Toast.makeText(this, "Shimmer Service started", Toast.LENGTH_SHORT).show();
        }
        super.onStart();
    }



    @Override
    protected void onDestroy() {
        //Stop the Shimmer service
        Intent intent = new Intent(this, ShimmerService.class);
        stopService(intent);
        Log.d(LOG_TAG, "Shimmer Service stopped");
        Toast.makeText(this, "Shimmer Service stopped", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    void openMenu(View view) {
        Intent serverIntent = new Intent(getApplicationContext(), ShimmerBluetoothDialog.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_SHIMMER);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mService = ((ShimmerService.LocalBinder)service).getService();

            // Tell the user about this for our demo.
            Log.d(SERVICE_TAG, "Shimmer Service Bound");
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mService = null;
            Log.d(SERVICE_TAG, "Shimmer Service Disconnected");
        }
    };


//    protected ServiceConnection mTestServiceConnection = new ServiceConnection() {
//
//        public void onServiceConnected(ComponentName arg0, IBinder service) {
//            Log.d(SERVICE_TAG, "Shimmer Service is connected");
//            ShimmerService.LocalBinder binder = (com.shimmerresearch.shimmerService.MultiShimmerTemplateService.LocalBinder) service;
//            mService = binder.getService();
//            db=mService.mDataBase;
//            mServiceBind = true;
//            //update the view
//
//            // this is needed because sometimes when there is an actitivity switch the service is not connecte yet, before the fragment was created, thus the fragment has no access to the service
//            ConfigurationFragment configF = (ConfigurationFragment)getFragmentManager().findFragmentByTag("Configure");
//            PlotFragment plotF = (PlotFragment)getFragmentManager().findFragmentByTag("Plot");
//            DevicesFragment deviceF = (DevicesFragment)getFragmentManager().findFragmentByTag("Devices");
//
//
//            if (configF!=null){
//                configF.mService = mService;
//                configF.setup();
//            } else if (plotF!=null){
//                plotF.mService = mService;
//                plotF.setup();
//            } else if (deviceF!=null){
//                deviceF.mService = mService;
//                deviceF.setup();
//            } else {
//
//            }
//
//
//        }
//
//        public void onServiceDisconnected(ComponentName arg0) {
//            mServiceBind = false;
//        }
//    };


    //If Bluetooth was not on, get the result from the activity to switch on Bluetooth
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                Intent intent = new Intent(this, ShimmerService.class);
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                Log.d(LOG_TAG, "Shimmer Service started");
                Toast.makeText(this, "Shimmer Service started", Toast.LENGTH_SHORT).show();
            }
            else if(resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Please enable Bluetooth to proceed.", Toast.LENGTH_LONG).show();
                int REQUEST_ENABLE_BT = 1;
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else {
                Toast.makeText(this, "Unknown Error! Your device may not support Bluetooth!", Toast.LENGTH_LONG).show();
            }
        }
    }



}
