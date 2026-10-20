package com.shimmerresearch.androidradiodriver;

import android.bluetooth.BluetoothDevice;
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

/**
 * Each instance of this class represents a ble radio that is used to communicate with a verisense device
 */
public class VerisenseBleAndroidRadioByteCommunication extends AbstractByteCommunication {
    BleDevice mBleDevice;
    protected String TxID = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
    protected String RxID = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";
    protected String ServiceID = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
    protected UUID sid;
    protected UUID txid;
    protected UUID rxid;
    String mMac;
    String uuid;

    //"DA:A6:19:F0:4A:D7"
    //"E7:45:2C:6D:6F:14"
    /**
     * Initialize a ble radio
     * @param mac  mac address of the verisense device e.g. d0:2b:46:3d:a2:bb
     */
    public VerisenseBleAndroidRadioByteCommunication(String mac) {
        mMac = mac;
        sid = UUID.fromString(ServiceID);
        txid = UUID.fromString(TxID);
        rxid = UUID.fromString(RxID);

    }
    TaskCompletionSource<String> mTaskConnect = new TaskCompletionSource<>();
    TaskCompletionSource<String> mTaskMTU = new TaskCompletionSource<>();

    /**
     * Connect to the verisense device
     */
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
                connectionFailed(bleDevice, "Connect Failure: " + exception);
            }

            @Override
            public void onConnectSuccess(final BleDevice bleDevice, final BluetoothGatt gatt, int status) {
                mBleDevice = bleDevice;
                // This attempt's own task, so a late MTU result can't complete a later attempt's
                final TaskCompletionSource<String> taskMTU = new TaskCompletionSource<>();
                mTaskMTU = taskMTU;
                // FastBle runs this and the MTU callback on the main thread, so waiting here for the
                // MTU would block the UI and always time out. Setup continues from the callback instead.
                BleManager.getInstance().setMtu(bleDevice, 251, new BleMtuChangedCallback() {
                    @Override
                    public void onSetMTUFailure(BleException exception) {
                        System.out.println("MTU Failure: " + exception);
                        if (taskMTU.trySetResult("MTU Failure")) {
                            onMtuSet(bleDevice, gatt);
                        }
                    }

                    @Override
                    public void onMtuChanged(int mtu) {
                        System.out.println("MTU Changed: " + mtu);
                        if (taskMTU.trySetResult("MTU Changed: " + mtu)) {
                            onMtuSet(bleDevice, gatt);
                        }
                    }
                });
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

    /**
     * Continues connect once the MTU request has finished, whether or not it succeeded.
     * Connected is reported once the RX notification is enabled, see {@link #onNotifyEnabled(BleDevice)}
     */
    private void onMtuSet(BleDevice bleDevice, BluetoothGatt gatt) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
        }

        startServiceS(bleDevice);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            gatt.setPreferredPhy(BluetoothDevice.PHY_LE_2M_MASK, BluetoothDevice.PHY_LE_2M_MASK, BluetoothDevice.PHY_OPTION_NO_PREFERRED);
        }
    }

    /**
     * Finishes connect once the RX notification is enabled, as no reply from the device can arrive before then
     */
    private void onNotifyEnabled(BleDevice bleDevice) {
        if (!mTaskConnect.trySetResult("Connected")) {
            return;
        }
        System.out.println(bleDevice.getMac() + " Connected");
        if (mByteCommunicationListener != null) {
            mByteCommunicationListener.eventConnected();
        }
    }

    /**
     * The connection attempt failed: drop the link and tell the listener
     */
    private void connectionFailed(BleDevice bleDevice, String reason) {
        System.out.println(mMac + " " + reason);
        if (!mTaskConnect.trySetResult(reason)) {
            return;
        }
        BleManager.getInstance().disconnect(bleDevice);
        if (mByteCommunicationListener != null) {
            mByteCommunicationListener.eventDisconnected();
        }
    }

    /**
     * Get services and start notify for characteristics changed
     * @param bleDevice  BLE device
     */
    public void startServiceS(BleDevice bleDevice){
        List<BluetoothGattService> services = BleManager.getInstance().getBluetoothGattServices(bleDevice);
        boolean isRxFound = false;
        if (services != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {


            for (BluetoothGattService service:services) {
                System.out.println(service.getUuid());
                if (service.getUuid().compareTo(sid)==0){
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        if (characteristic.getUuid().compareTo(txid)==0){
                            //newConnectedBLEDevice(bleDevice,characteristic);
                            CharSequence text = "TXID!";
                        }
                        else if (characteristic.getUuid().compareTo(rxid)==0){
                            isRxFound = true;
                            newConnectedBLEDevice(bleDevice,characteristic);
                            CharSequence text = "RxID!";
                        }
                    }
                }
            }


        }
        if (!isRxFound) {
            connectionFailed(bleDevice, "RX characteristic not found");
        }
    }

    /**
     * Start notify for characteristics changed
     * @param bleDevice  BLE device
     * @param characteristic
     */
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
                            System.out.println("Notify Success");
                            onNotifyEnabled(bleDevice);
                        }

                        @Override
                        public void onNotifyFailure(BleException exception) {
                            connectionFailed(bleDevice, "Notify Failure: " + exception);
                        }

                        @Override
                        public void onCharacteristicChanged(byte[] data) {
                            if (mByteCommunicationListener != null) {
                                try{
                                    mByteCommunicationListener.eventNewBytesReceived(data);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        }
    }

    /**
     * Disconnect from the verisense device
     */
    @Override
    public void disconnect() {
        BleManager.getInstance().disconnect(mBleDevice);
    }

    /**
     * Write bytes to the verisense device
     * @param bytes  byte array to be written
     */
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

    /**
     * Convert mac address to uuid
     * @param MacID  e.g. d0:2b:46:3d:a2:bb
     * @return uuid e.g. 00000000-0000-0000-0000-d02b463da2bb
     */
    public String convertMacIDtoUUID(String MacID) {
        String uuid = "00000000-0000-0000-0000-";
        return uuid + MacID.replace(":", "");
    }

    /**
     * @return uuid e.g. 00000000-0000-0000-0000-d02b463da2bb
     */
    public String getUuid() {
        return convertMacIDtoUUID(this.mMac);
    }
}
