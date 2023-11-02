package com.shimmerresearch.androidradiodriver;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.shimmerresearch.driverUtilities.UtilShimmer;
import com.shimmerresearch.verisense.communication.ByteCommunicationListener;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import bolts.TaskCompletionSource;

public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    TaskCompletionSource<Boolean> mTaskConnect = new TaskCompletionSource<Boolean>();
    TaskCompletionSource<Boolean> mTaskWrite = new TaskCompletionSource<Boolean>();
    TaskCompletionSource<String> mTaskMTU = new TaskCompletionSource<>();
    String TxID = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
    String RxID = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";
    String ServiceID = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
    UUID CCCD_ID = UUID.fromString("000002902-0000-1000-8000-00805f9b34fb");
    UUID sid = UUID.fromString(ServiceID);
    UUID txid = UUID.fromString(TxID);
    UUID rxid = UUID.fromString(RxID);
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    private ByteCommunicationListener mByteCommunicationListener;
    private BluetoothGattService mBluetoothGattService;
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;

                //mTaskMTU= new TaskCompletionSource<>();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //gatt.requestMtu(251);
                    /*
                    try {
                        boolean result = mTaskMTU.getTask().waitForCompletion(3, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                }
                if (mByteCommunicationListener != null) {
                    mByteCommunicationListener.eventConnected();
                }

                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                if (mByteCommunicationListener != null) {
                    mByteCommunicationListener.eventDisconnected();
                }
                broadcastUpdate(intentAction);
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = getSupportedGattServices();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    for (BluetoothGattService service:services) {
                        System.out.println(service.getUuid());
                        if (service.getUuid().compareTo(sid)==0){
                            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                                if (characteristic.getUuid().compareTo(txid)==0){
                                    CharSequence text = "TXID!";
                                }
                                else if (characteristic.getUuid().compareTo(rxid)==0){
                                    setCharacteristicNotification(characteristic, true);
                                    CharSequence text = "RxID!";
                                }
                            }
                        }
                    }
                }
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                mBluetoothGattService = mBluetoothGatt.getService(sid);
                if (mBluetoothGattService != null) {
                    Log.i(TAG, "Service characteristic UUID found: " + mBluetoothGattService.getUuid().toString());
                } else {
                    Log.i(TAG, "Service characteristic not found for UUID: " + sid);
                }
                mTaskConnect.setResult(true);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }

        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.d(TAG, "onCharacteristicRead characteristic:" + characteristic);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharacteristicRead charac.getvalue():" + characteristic.getValue() + "status: " + status);
                setCharacteristicNotification(characteristic, true);
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            Log.d(TAG, "HEREEE onCharacteristicChanged charac.getvalue():" + UtilShimmer.bytesToHexStringWithSpacesFormatted(characteristic.getValue()));
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status){
            Log.d(TAG, "MTU Changed: " + mtu);
            //mTaskMTU.setResult("MTU Changed: " + mtu);
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharacteristicWrite charac.getvalue():" + UtilShimmer.bytesToHexStringWithSpacesFormatted(characteristic.getValue()) + "status: " + status);
                //setCharacteristicNotification(characteristic, true);
                //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }else{
                Log.d(TAG, "onCharacteristicWrite not GATT_SUCCESS, status: " + status);
            }
            mTaskWrite.setResult(true);
        }
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml

        /*
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
        int flag = characteristic.getProperties();
        int format = -1;
        if ((flag & 0x01) != 0) {
            format = BluetoothGattCharacteristic.FORMAT_UINT16;
            Log.d(TAG, "Heart rate format UINT16.");
        } else {
            format = BluetoothGattCharacteristic.FORMAT_UINT8;
            Log.d(TAG, "Heart rate format UINT8.");
        }
        final int heartRate = characteristic.getIntValue(format, 1);
        Log.d(TAG, String.format("Received heart rate: %d", heartRate));
        intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
    } else {
         */
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                try{
                    System.out.println("To call eventNewBytesReceived" + UtilShimmer.bytesToHexStringWithSpacesFormatted(data));

                    //mByteCommunicationListener.eventNewBytesReceived(data);
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    for(byte byteChar : data)
                        stringBuilder.append(String.format("%02X ", byteChar));

                    intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
                }catch(Exception ex){
                    System.out.println(ex.getMessage());
                }

            }

        sendBroadcast(intent);
    }
    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }
    private IBinder mBinder = new LocalBinder();
    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize(ByteCommunicationListener mByteCommunicationListener) {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        this.mByteCommunicationListener = mByteCommunicationListener;
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }
    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        mTaskConnect = new TaskCompletionSource<Boolean>();

        // Previously connected device.  Try to reconnect.
        /*
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
               /* try {
                    boolean result = mTaskConnect.getTask().waitForCompletion(5, TimeUnit.SECONDS);
                    Thread.sleep(200);
                    if (!result) {
                        System.out.println("Connect fail");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            } else {
                return false;
            }
        }*/
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        try {
            boolean result = mTaskConnect.getTask().waitForCompletion(5, TimeUnit.SECONDS);
            Thread.sleep(200);
            if (!result) {
                System.out.println("Connect fail");
                mConnectionState = STATE_DISCONNECTED;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }
    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }
    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }
    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        /*
        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }
         */
    }
    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }
    public boolean writeCharacteristic(UUID serviceUuid, UUID charUuid, byte[] data){
        //check mBluetoothGatt is available
        if (mBluetoothGatt == null) {
            Log.e(TAG, "lost connection");
            return false;
        }
        if(mConnectionState == STATE_DISCONNECTED){
            return false;
        }
        BluetoothGattService Service = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (mBluetoothGattService != null) {
                Service = mBluetoothGattService;
            }else{
                Service = mBluetoothGatt.getService(serviceUuid);
            }
        }
        if (Service == null) {
            Log.e(TAG, "service not found!");
            return false;
        }

        BluetoothGattCharacteristic charac = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            charac = Service
                    .getCharacteristic(charUuid);
        }
        if (charac == null) {
            Log.e(TAG, "char not found!");
            return false;
        }

        //byte[] value = new byte[1];
        //value[0] = (byte) (21 & 0xFF);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            charac.setValue(data);
        }
        setCharacteristicNotification(charac, true);
        //Enabled remote notifications
        BluetoothGattDescriptor desc = charac.getDescriptor(CCCD_ID);
        if(desc != null){
            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(desc);
        }else{
            System.out.println("DESC null, charac: " + UtilShimmer.bytesToHexStringWithSpacesFormatted(charac.getValue()));
        }

        boolean status = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mTaskWrite = new TaskCompletionSource<Boolean>();
            System.out.println("WRITEEE" + UtilShimmer.bytesToHexStringWithSpacesFormatted(charac.getValue()));
            //status = mBluetoothGatt.writeCharacteristic(charac);
            status = mBluetoothGatt.writeCharacteristic(charac);

            try {
                boolean result = mTaskWrite.getTask().waitForCompletion(5, TimeUnit.SECONDS);
                Thread.sleep(200);
                if (!result) {
                    System.out.println("Write fail");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return status;
    }
}