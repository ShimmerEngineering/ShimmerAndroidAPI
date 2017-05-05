package com.shimmerresearch.shimmerserviceexample;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.android.shimmerService.ShimmerService;

public class MainActivity extends AppCompatActivity {

    ShimmerBluetoothManagerAndroid btManager;
    ShimmerService mService;
    Handler mHandler;

    final static String LOG_TAG = "SHIMMER";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            btManager = new ShimmerBluetoothManagerAndroid(this, mHandler);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Couldn't create ShimmerBluetoothManagerAndroid. Error: " + e);
        }

        //Start the Shimmer service
//        mService = new ShimmerService();
//        Intent intent = new Intent(this, ShimmerService.class);
//        mService.startService(intent);

        Intent intent = new Intent(this, ShimmerService.class);
        startService(intent);

        Log.d(LOG_TAG, "Shimmer Service started");
        Toast.makeText(this, "Shimmer Service started", Toast.LENGTH_SHORT).show();

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


}
