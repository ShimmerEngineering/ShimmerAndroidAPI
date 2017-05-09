package com.shimmerresearch.android.manager;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.Shimmer4Android;
import com.shimmerresearch.androidradiodriver.ShimmerRadioInitializerAndroid;
import com.shimmerresearch.androidradiodriver.ShimmerSerialPortAndroid;
import com.shimmerresearch.bluetooth.ShimmerRadioInitializer;
import com.shimmerresearch.comms.radioProtocol.CommsProtocolRadio;
import com.shimmerresearch.comms.radioProtocol.LiteProtocol;
import com.shimmerresearch.comms.serialPortInterface.AbstractSerialPortHal;
import com.shimmerresearch.comms.serialPortInterface.ErrorCodesSerialPort;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.shimmer4sdk.Shimmer4;
import com.shimmerresearch.driverUtilities.BluetoothDeviceDetails;
import com.shimmerresearch.driverUtilities.HwDriverShimmerDeviceDetails;
import com.shimmerresearch.exception.DeviceNotPairedException;

import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.managers.bluetoothManager.ShimmerBluetoothManager;

import java.util.Set;
import java.util.TreeMap;

/**
 * Created by ASaez on 10-Aug-16.
 */

public class ShimmerBluetoothManagerAndroid extends ShimmerBluetoothManager {

    private static final String TAG = ShimmerBluetoothManagerAndroid.class.getSimpleName();
    private static final String DEFAULT_SHIMMER_NAME = "ShimmerDevice";

    BluetoothAdapter mBluetoothAdapter;
    Context mContext;
    Handler mHandler;

    public ShimmerBluetoothManagerAndroid(Context context, Handler handler) throws Exception {
        super();
        this.mContext = context;
        this.mHandler = handler;
        if(mHandler==null){ throw new Exception("Handler is NULL"); }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(checkBtEnabled(mBluetoothAdapter)==false) { throw new Exception("Bluetooth not Enabled"); }
        loadBtShimmers();
    }


    public Handler getHandler(){
        return mHandler;
    }

    public void setHandler(Handler handler){
        this.mHandler = handler;
    }

    public void connectBluetoothDevice(BluetoothDevice device){
        String bluetoothAddress = device.getAddress();
        addDiscoveredDevice(bluetoothAddress);
        super.connectShimmerTroughBTAddress(bluetoothAddress);
    }

    @Override
    public void connectShimmerTroughBTAddress(String blueoothAddress) {

        if(isDevicePaired(blueoothAddress)) {
            addDiscoveredDevice(blueoothAddress);
            super.connectShimmerTroughBTAddress(blueoothAddress);
        }
        else{
            String msg = "Device " + blueoothAddress + " not paired";
            throw new DeviceNotPairedException(blueoothAddress, msg);
        }
    }

    private boolean isDevicePaired(String bluetoothAddress){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for(BluetoothDevice device: pairedDevices){
            if(device.getAddress().equals(bluetoothAddress)){
                return true;
            }
        }
        return false;
    }

    public TreeMap<Integer, String> getMapOfErrorCodes(int[] ints) {

        TreeMap<Integer,String> mapOfErrorCodes = new TreeMap<Integer,String>();
        mapOfErrorCodes.putAll(ErrorCodesSerialPort.mMapOfErrorCodes);
        return mapOfErrorCodes;
    }

    @Override
    public ShimmerDevice getShimmerGlobalMap(String bluetoothAddress) {
        return mMapOfBtConnectedShimmers.get(bluetoothAddress);
    }

    @Override
    public void putShimmerGlobalMap(String bluetoothAdrres, ShimmerDevice shimmerDevice) {
        mMapOfBtConnectedShimmers.put(bluetoothAdrres, shimmerDevice);
    }

    @Override
    protected AbstractSerialPortHal createNewSerialPortComm(String comPort, String bluetoothAddress) {
        return new ShimmerSerialPortAndroid(bluetoothAddress);
    }

    @Override
    public void printMessage(String msg) {
        Log.i(TAG, msg);
    }


    protected Shimmer4 initializeNewShimmer4(ShimmerRadioInitializer shimmerRadioInitializer) {
        Shimmer4Android shimmer = new Shimmer4Android(mHandler);
//        shimmer.setShimmerUserAssignedName(selectedDevice);
        mMapOfBtConnectedShimmers.put(shimmer.getBtConnectionHandle(), shimmer);
//        return initializeShimmer4(shimmerRadioInitializer.getSerialCommPort(), shimmer);
        return null;
    }

    private ShimmerRadioInitializer getRadioInitializer(String bluetoothAddress) {
        return new ShimmerRadioInitializerAndroid(bluetoothAddress);
    }

    @Override
    protected void connectExistingShimmer(Object... objects) {
        ShimmerDevice shimmerDevice = (ShimmerDevice) objects[0];
        String btAddress = (String) objects[1];

        //TODO remove shimmer from map of connected devices??

        if(shimmerDevice instanceof  Shimmer){
            Shimmer shimmer = (Shimmer) shimmerDevice;
            shimmer.connect(btAddress, "default");
        }
        else if (shimmerDevice instanceof  Shimmer4Android){
            connectExistingShimmer4((Shimmer4Android) shimmerDevice, btAddress);
        }

    }

    @Override
    protected ShimmerDevice createNewShimmer3(String comPort, String bluetoothAddress) {
        return null;
    }

    @Override
    protected ShimmerDevice createNewShimmer3(ShimmerRadioInitializer shimmerRadioInitializer, String bluetoothAddress) {
        ShimmerSerialPortAndroid serialPort = (ShimmerSerialPortAndroid) shimmerRadioInitializer.getSerialCommPort();
        Shimmer shimmer = new Shimmer(mContext, mHandler,DEFAULT_SHIMMER_NAME,true);
        shimmer.setDelayForBtRespone(true);
        mMapOfBtConnectedShimmers.put(bluetoothAddress, shimmer);
        return initializeShimmer3(serialPort, shimmer);
    }

    protected ShimmerDevice initializeShimmer3(AbstractSerialPortHal abstractSerialPortComm, ShimmerDevice shimmerDevice) {
        ShimmerSerialPortAndroid serialPort = (ShimmerSerialPortAndroid) abstractSerialPortComm;
        ((Shimmer) shimmerDevice).setRadio(serialPort.getBluetoothSocket());
        shimmerDevice.addCommunicationRoute(Configuration.COMMUNICATION_TYPE.BLUETOOTH);
        return shimmerDevice;
    }

    @Override
    protected Shimmer4 createNewShimmer4(String comport, String bluetoothAddress) {
        Shimmer4Android shimmer = new Shimmer4Android(mHandler);
        mMapOfBtConnectedShimmers.put(bluetoothAddress, shimmer);
        return shimmer;
    }

    @Override
    protected Shimmer4 createNewShimmer4(ShimmerRadioInitializer shimmerRadioInitializer, String bluetoothAddress) {

        ShimmerSerialPortAndroid serialPortComm = (ShimmerSerialPortAndroid) shimmerRadioInitializer.getSerialCommPort();
        Shimmer4 shimmer4 = createNewShimmer4("", bluetoothAddress); //first parameter is com port, but we do not know it in android
        if(serialPortComm!=null){
            CommsProtocolRadio commsProtocolRadio = new CommsProtocolRadio(serialPortComm, new LiteProtocol(bluetoothAddress));
            shimmer4.setRadio(commsProtocolRadio);
        }

        return shimmer4;
    }

    private void connectExistingShimmer4(Shimmer4Android shimmer4, String btAddress){
        shimmer4.addCommunicationRoute(Configuration.COMMUNICATION_TYPE.BLUETOOTH);
        shimmer4.setMacIdFromUart(btAddress);

        if(shimmer4.isReadyToConnect()){
            ShimmerRadioInitializerAndroid radio = (ShimmerRadioInitializerAndroid) getRadioInitializer(btAddress);
            AbstractSerialPortHal abstractComport = radio.getSerialCommPort();
            shimmer4.setRadio(new CommsProtocolRadio(abstractComport, new LiteProtocol(btAddress)));

            try {
                shimmer4.connect();
            } catch (ShimmerException e) {
                e.printStackTrace();
            }


        }
    }


    private void addDiscoveredDevice(String bluetoothAddress){
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(bluetoothAddress);
        BluetoothDeviceDetails portDetails = new BluetoothDeviceDetails("", device.getAddress(), device.getName());

        mMapOfParsedBtComPortsDeepCopy.put(bluetoothAddress, portDetails);
        mMapOfParsedBtComPorts.put(bluetoothAddress, portDetails);
    }

    @Override
    public void loadBtShimmers(Object... params) {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        TreeMap<String, BluetoothDeviceDetails> pairedCompatibleDevices = mapBTDevicesToComPortDetails(pairedDevices);

        mMapOfParsedBtComPortsDeepCopy.clear();
        mMapOfParsedBtComPorts.clear();

        mMapOfParsedBtComPortsDeepCopy.putAll(pairedCompatibleDevices);
        mMapOfParsedBtComPorts.putAll(pairedCompatibleDevices);
    }

    private TreeMap<String, BluetoothDeviceDetails> mapBTDevicesToComPortDetails(Set<BluetoothDevice> pairedDevices){

        TreeMap<String, BluetoothDeviceDetails> mapOfShimmerDevices = new TreeMap<String, BluetoothDeviceDetails>();
        for (BluetoothDevice device : pairedDevices) {
            BluetoothDeviceDetails portDetails = new BluetoothDeviceDetails("", device.getAddress(), device.getName());
            if(portDetails.mDeviceTypeDetected != HwDriverShimmerDeviceDetails.DEVICE_TYPE.UNKOWN){
                mapOfShimmerDevices.put(device.getAddress(), portDetails);
            }
        }

        return mapOfShimmerDevices;
    }

    @Override
    public void addCallBack(BasicProcessWithCallBack basicProcessWithCallBack) {

    }

    boolean checkBtEnabled(BluetoothAdapter btAdapter) {
        if(!btAdapter.isEnabled()) {
            return false;
        }
        return true;
    }

}
