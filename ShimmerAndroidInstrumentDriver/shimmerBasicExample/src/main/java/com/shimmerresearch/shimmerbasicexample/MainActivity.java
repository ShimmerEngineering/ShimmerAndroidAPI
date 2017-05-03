package com.shimmerresearch.shimmerbasicexample;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;


public class MainActivity extends AppCompatActivity {

    ShimmerBluetoothManagerAndroid btManager;
    Handler mHandler;
    BluetoothDevice shimmerBtDevice;
    final static String shimmerBtAdd = "00:06:66:66:96:86";  //Put the address of the Shimmer device you want to connect here
    //final static String shimmerBtAdd = "00:06:66:88:DB:79";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize Handler
        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };


        btManager = new ShimmerBluetoothManagerAndroid(this, mHandler);

    }

    @Override
    protected void onStart() {

        //Connect the Shimmer through Bluetooth Address
        btManager.connectShimmerTroughBTAddress(shimmerBtAdd);

        super.onStart();
    }

    @Override
    protected void onStop() {

        //Disconnect Shimmer device when app is stopped
        btManager.disconnectAllDevices();

        super.onStop();
    }




}
