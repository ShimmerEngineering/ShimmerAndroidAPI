package com.shimmerresearch.androidradiodriver;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import com.clj.fastble.callback.BleMtuChangedCallback;
import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.verisense.communication.AbstractByteCommunication;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

import org.apache.commons.codec.binary.Hex;

import bolts.TaskCompletionSource;

public class AndroidBleRadioByteCommunication extends AbstractByteCommunication {
    BleDevice mBleDevice;
    String TxID = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
    String RxID = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";
    String ServiceID = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
    UUID sid = UUID.fromString(ServiceID);
    UUID txid = UUID.fromString(TxID);
    UUID rxid = UUID.fromString(RxID);
    String mMac;
    String uuid;

    //"DA:A6:19:F0:4A:D7"
    //"E7:45:2C:6D:6F:14"
    public AndroidBleRadioByteCommunication(String mac) {
        mMac = mac;
    }
    TaskCompletionSource<String> mTaskConnect = new TaskCompletionSource<>();
    TaskCompletionSource<String> mTaskMTU = new TaskCompletionSource<>();
    @Override
    public void connect() {
        mTaskConnect = new TaskCompletionSource<>();
        BleManager.getInstance().connect(mMac, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                System.out.println("Connecting");
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                if (mByteCommunicationListener != null) {
                    mByteCommunicationListener.eventDisconnected();
                }
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                mTaskMTU= new TaskCompletionSource<>();
                BleManager.getInstance().setMtu(bleDevice, 251, new BleMtuChangedCallback() {
                    @Override
                    public void onSetMTUFailure(BleException exception) {
                        System.out.println("MTU Failure");

                    }

                    @Override
                    public void onMtuChanged(int mtu) {
                        System.out.println("MTU Changed: " + mtu);
                        mTaskMTU.setResult("MTU Changed: " + mtu);
                    }
                });

                try {
                    boolean result = mTaskMTU.getTask().waitForCompletion(3, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (mByteCommunicationListener != null) {
                    mByteCommunicationListener.eventConnected();
                }
                mBleDevice = bleDevice;
                startServiceS(bleDevice);
                System.out.println(bleDevice.getMac() + " Connected");
                mTaskConnect.setResult("Connected");
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                System.out.println();
            }
        });


        try {
            boolean result = mTaskConnect.getTask().waitForCompletion(5, TimeUnit.SECONDS);
            Thread.sleep(200);
            if (!result) {
                System.out.println("Connect fail");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startServiceS(BleDevice bleDevice){
        List<BluetoothGattService> services = BleManager.getInstance().getBluetoothGattServices(bleDevice);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {


            for (BluetoothGattService service:services) {
                System.out.println(service.getUuid());
                if (service.getUuid().compareTo(sid)==0){
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        if (characteristic.getUuid().compareTo(txid)==0){
                            //newConnectedBLEDevice(bleDevice,characteristic);
                            CharSequence text = "TXID!";
                        }
                        else if (characteristic.getUuid().compareTo(rxid)==0){
                            newConnectedBLEDevice(bleDevice,characteristic);
                            CharSequence text = "RxID!";
                        }
                    }
                }
            }


        }
    }

    public void newConnectedBLEDevice(final BleDevice bleDevice, final BluetoothGattCharacteristic characteristic) {


        int count = 1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BleManager.getInstance().notify(
                    bleDevice,
                    characteristic.getService().getUuid().toString(),
                    characteristic.getUuid().toString(),
                    new BleNotifyCallback() {


                        @Override
                        public void onNotifySuccess() {

                        }

                        @Override
                        public void onNotifyFailure(BleException exception) {

                        }

                        @Override
                        public void onCharacteristicChanged(byte[] data) {
                            if (mByteCommunicationListener != null) {
                                mByteCommunicationListener.eventNewBytesReceived(data);
                            }
                        }
                    });
        }
    }

    @Override
    public void disconnect() {
        BleManager.getInstance().disconnect(mBleDevice);
    }

    @Override
    public void writeBytes(byte[] bytes) {
        BleManager.getInstance().write(mBleDevice, sid.toString(), txid.toString(), bytes, false, new BleWriteCallback() {
            @Override
            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                System.out.println("Write Success " + UtilShimmer.bytesToHexStringWithSpacesFormatted(justWrite));
            }

            @Override
            public void onWriteFailure(BleException exception) {

                System.out.println("Write Fail");
            }
        });
    }

    @Override
    public void stop() {

    }
    public String convertMacIDtoUUID(String MacID) {
        String uuid = "00000000-0000-0000-0000-";
        return uuid + MacID.replace(":", "");
    }

    public String getUuid() {
        //"00000000-0000-0000-0000-E7EC37A0D234"
        return convertMacIDtoUUID(this.mMac);
    }
}
