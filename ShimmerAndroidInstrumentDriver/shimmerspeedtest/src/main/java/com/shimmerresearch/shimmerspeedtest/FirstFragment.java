package com.shimmerresearch.shimmerspeedtest;

import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.EXTRA_DEVICE_ADDRESS;
import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.EXTRA_DEVICE_NAME;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.fragment.NavHostFragment;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog;
import com.shimmerresearch.androidradiodriver.Shimmer3BleAndroidRadioByteCommunication;
import com.shimmerresearch.androidradiodriver.Shimmer3RAndroidRadioByteCommunication;
import com.shimmerresearch.androidradiodriver.VerisenseBleAndroidRadioByteCommunication;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.shimmer3.communication.SpeedTestProtocol;
import com.shimmerresearch.verisense.communication.ByteCommunicationListener;

public class FirstFragment extends Fragment {
    SpeedTestProtocol protocol;
    Shimmer shimmer;
    TextView tv;
    final static int REQUEST_CONNECT_SHIMMER = 2;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    long time = System.currentTimeMillis();

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pairedDevicesIntent = new Intent(getActivity().getApplicationContext(), ShimmerBluetoothDialog.class);
                startActivityForResult(pairedDevicesIntent, REQUEST_CONNECT_SHIMMER);
            }
        });

        view.findViewById(R.id.button_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                protocol.startSpeedTest();
            }
        });

        view.findViewById(R.id.button_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                protocol.stopSpeedTest();
            }
        });

        view.findViewById(R.id.button_dc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread(){
                    public void run(){

                        if (protocol!=null){
                            try {
                                protocol.disconnect();
                                shimmer.disconnect();
                            } catch (ShimmerException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };

                thread.start();
            }
        });

        tv = view.findViewById(R.id.textView);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) { //The devices paired list has returned a result
            if (resultCode == Activity.RESULT_OK) {
                //Get the Bluetooth mac address of the selected device:
                String macAdd = data.getStringExtra(EXTRA_DEVICE_ADDRESS);
                String deviceName = data.getStringExtra(EXTRA_DEVICE_NAME);
                shimmer = new Shimmer(mHandler);
                VerisenseBleAndroidRadioByteCommunication port = null;
                if (deviceName.toUpperCase().contains("SHIMMER3-")) {
                    port = new Shimmer3BleAndroidRadioByteCommunication(macAdd);
                } else if (deviceName.toUpperCase().contains("SHIMMER3R-")){
                    port = new Shimmer3RAndroidRadioByteCommunication(macAdd);
                } else {
                    port = new VerisenseBleAndroidRadioByteCommunication(macAdd);
                }

                protocol = new SpeedTestProtocol(port);

                protocol.setListener(new SpeedTestProtocol.SpeedTestResult() {
                    @Override
                    public void onNewResult(String s) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if ((System.currentTimeMillis()-time)>1000) {
                                    time = System.currentTimeMillis();
                                    tv.setText(s);
                                }
                            }
                        });
                    }

                    @Override
                    public void onConnected() {
                        Toast.makeText(getActivity().getApplicationContext(), "Device Connected", Toast.LENGTH_LONG).show();
                        tv.setText("Device Connected");
                    }

                    @Override
                    public void onDisconnected() {
                        Toast.makeText(getActivity().getApplicationContext(), "Device Disconnected", Toast.LENGTH_LONG).show();
                        tv.setText("Device Disconnected");
                    }
                });

                try {

                    protocol.connect();
                } catch (ShimmerException e) {
                    e.printStackTrace();
                }
            }
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
                    }
                    break;
                case Shimmer.MESSAGE_TOAST:
                case ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE:
                    ShimmerBluetooth.BT_STATE state = null;
                    if (msg.obj instanceof ObjectCluster) {
                        state = ((ObjectCluster) msg.obj).mState;
                    } else if (msg.obj instanceof CallbackObject) {
                        state = ((CallbackObject) msg.obj).mState;
                    }

                    switch (state) {
                        case CONNECTED:
                            break;
                        case CONNECTING:
                            break;
                        case STREAMING:
                        case STREAMING_AND_SDLOGGING:
                            break;
                        case SDLOGGING:
                            break;
                        case DISCONNECTED:
                            Toast.makeText(getActivity().getApplicationContext(), "Device Disconnected", Toast.LENGTH_LONG).show();
                            tv.setText("Device Disconnected");
                            break;
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };
}