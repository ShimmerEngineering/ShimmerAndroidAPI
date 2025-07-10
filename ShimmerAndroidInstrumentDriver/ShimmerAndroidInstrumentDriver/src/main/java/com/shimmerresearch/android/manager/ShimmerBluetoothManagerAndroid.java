package com.shimmerresearch.android.manager;


import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.Shimmer4Android;
import com.shimmerresearch.android.VerisenseDeviceAndroid;
import com.shimmerresearch.android.protocol.VerisenseProtocolByteCommunicationAndroid;
import com.shimmerresearch.androidradiodriver.VerisenseBleAndroidRadioByteCommunication;
import com.shimmerresearch.androidradiodriver.Shimmer3BLEAndroid;
import com.shimmerresearch.androidradiodriver.ShimmerRadioInitializerAndroid;
import com.shimmerresearch.androidradiodriver.ShimmerSerialPortAndroid;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerRadioInitializer;
import com.shimmerresearch.comms.radioProtocol.CommsProtocolRadio;
import com.shimmerresearch.comms.radioProtocol.LiteProtocol;
import com.shimmerresearch.comms.serialPortInterface.AbstractSerialPortHal;
import com.shimmerresearch.comms.serialPortInterface.ErrorCodesSerialPort;
import com.shimmerresearch.driver.BasicProcessWithCallBack;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.shimmer4sdk.Shimmer4sdk;
import com.shimmerresearch.driverUtilities.BluetoothDeviceDetails;
import com.shimmerresearch.driverUtilities.ChannelDetails;
import com.shimmerresearch.driverUtilities.HwDriverShimmerDeviceDetails;
import com.shimmerresearch.driverUtilities.SensorDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.driverUtilities.ShimmerVerObject;
import com.shimmerresearch.exception.DeviceNotPairedException;

import com.shimmerresearch.exceptions.ConnectionExceptionListener;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.managers.bluetoothManager.ShimmerBluetoothManager;
import com.shimmerresearch.verisense.communication.VerisenseProtocolByteCommunication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by ASaez on 10-Aug-16.
 */

public class ShimmerBluetoothManagerAndroid extends ShimmerBluetoothManager {
    ProgressDialog mProgressDialog;
    private static final String TAG = ShimmerBluetoothManagerAndroid.class.getSimpleName();
    private static final String DEFAULT_SHIMMER_NAME = "ShimmerDevice";

    static final String VERISENSE_NAME_NO_PAIRING_REQUIRED = "Verisense-00";


    BluetoothAdapter mBluetoothAdapter;
    Context mContext;
    protected Handler mHandler;
    private boolean AllowAutoPairing = true;

    public enum BT_TYPE{
        BT_CLASSIC,
        BLE
    }

    public ShimmerBluetoothManagerAndroid(Context context, Handler handler) throws Exception {
        super();
        ShimmerRadioInitializer.useLegacyDelayBeforeBtRead(true);
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
        super.connectShimmerThroughBTAddress(bluetoothAddress);
    }

    /**
     * When true, will attempt to pair the device if the device is not paired. User will have to manually key in the pairing key.
     * @param enable
     */
    public void enablePairingOnConnect(boolean enable){
        AllowAutoPairing = enable;
    }

    public boolean checkIfDeviceRequiresPairing(String deviceName){
        if(deviceName.contains(VERISENSE_NAME_NO_PAIRING_REQUIRED)){
            return false;
        }
        return true;
    }

    /**
     * See also {@link #connectShimmerThroughBTAddress(String)}.
     * @param bluetoothAddress in the form of XX:XX:XX:XX:XX:XX
     * @param context if the context is set, a progress dialog will show, otherwise a toast msg will show
     * @exception IllegalArgumentException if bluetoothAddress is invalid, note this will only occur when {@link #enablePairingOnConnect(boolean)} is enabled
     * @exception DeviceNotPairedException if the device is not paired
     */
    public void connectShimmerThroughBTAddress(final String bluetoothAddress, final String deviceName, Context context) {

        if(isDevicePaired(bluetoothAddress) || AllowAutoPairing) {
            if (!isDevicePaired(bluetoothAddress) && checkIfDeviceRequiresPairing(deviceName)){
                if (context!=null) {
                    //Toast.makeText(mContext, "Attempting to pair device, please wait...", Toast.LENGTH_LONG).show();
                    final ProgressDialog progress = new ProgressDialog(context);
                    progress.setTitle("Pairing Device");
                    progress.setMessage("Trying to pair device...");
                    progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                    progress.show();
                    mProgressDialog = progress;
                } else {
                    Toast.makeText(mContext, "Attempting to pair device, please wait...", Toast.LENGTH_LONG).show();
                }
            }
            addDiscoveredDevice(bluetoothAddress);
            //super.connectShimmerThroughBTAddress(bluetoothAddress);
            BluetoothDeviceDetails bdd = new BluetoothDeviceDetails("",bluetoothAddress,deviceName);
            super.connectShimmerThroughBTAddress(bdd);
            super.setConnectionExceptionListener(new ConnectionExceptionListener() {
                @Override
                public void onConnectionStart(String connectionHandle) {

                }

                @Override
                public void onConnectionException(Exception exception) {
                    if (mProgressDialog!=null) {
                        mProgressDialog.dismiss();
                    }
                    mHandler.obtainMessage(ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE, -1, -1,
                            new ObjectCluster("", bluetoothAddress, ShimmerBluetooth.BT_STATE.DISCONNECTED)).sendToTarget();

                }

                @Override
                public void onConnectStartException(String connectionHandle) {
                    if (mProgressDialog!=null) {
                        mProgressDialog.dismiss();
                    }
                    mHandler.obtainMessage(ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE, -1, -1,
                            new ObjectCluster("", bluetoothAddress, ShimmerBluetooth.BT_STATE.DISCONNECTED)).sendToTarget();

                }
            });
        }
        else{
            String msg = "Device " + bluetoothAddress + " not paired";
            throw new DeviceNotPairedException(bluetoothAddress, msg);
        }
    }

    /**
     * See also {@link #connectShimmerThroughBTAddress(String,Context)}.
     * @param bluetoothAddress in the form of XX:XX:XX:XX:XX:XX
     * @exception IllegalArgumentException if bluetoothAddress is invalid, note this will only occur when {@link #enablePairingOnConnect(boolean)} is enabled
     * @exception DeviceNotPairedException if the device is not paired
     */
    @Override
    public void connectVerisenseDevice(BluetoothDeviceDetails bdd) {
        VerisenseBleAndroidRadioByteCommunication radio1 = new VerisenseBleAndroidRadioByteCommunication(bdd.mShimmerMacId);
        VerisenseProtocolByteCommunicationAndroid protocol1 = new VerisenseProtocolByteCommunicationAndroid(radio1);
        final VerisenseDeviceAndroid verisenseDevice = new VerisenseDeviceAndroid(mHandler);
        verisenseDevice.setMacIdFromUart(bdd.mShimmerMacId);
        verisenseDevice.setProtocol(Configuration.COMMUNICATION_TYPE.BLUETOOTH, protocol1);
        initializeNewShimmerCommon(verisenseDevice);
        Thread thread = new Thread(){
            public void run(){

                try {
                    verisenseDevice.connect();
                } catch (ShimmerException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    public void connectShimmerThroughBTAddress(final String bluetoothAddress, final String deviceName, BT_TYPE btType) {
        if(btType.equals(BT_TYPE.BT_CLASSIC)){
            connectShimmerThroughBTAddress(bluetoothAddress);
        }else{
            connectShimmer3BLEThroughBTAddress(bluetoothAddress,deviceName,null);
        }
    }
    @Override
    public void connectShimmerThroughBTAddress(final String bluetoothAddress) {

        //scanLeDevice(bluetoothAddress);
        //doDiscovery();
        connectShimmerThroughBTAddress(bluetoothAddress,"", (Context) null);
    }

    public void connectShimmer3BLEThroughBTAddress(final String bluetoothAddress, final String deviceName, Context context){
        Shimmer3BLEAndroid shimmer3BLE;
        if (deviceName.contains(HwDriverShimmerDeviceDetails.DEVICE_TYPE.SHIMMER3R.toString())){
            shimmer3BLE = new Shimmer3BLEAndroid(ShimmerVerDetails.HW_ID.SHIMMER_3R,bluetoothAddress, mHandler);

        } else {
            shimmer3BLE = new Shimmer3BLEAndroid(ShimmerVerDetails.HW_ID.SHIMMER_3, bluetoothAddress, mHandler);

        }
        shimmer3BLE.setMacIdFromUart(bluetoothAddress);
        initializeNewShimmerCommon(shimmer3BLE);
        Thread thread = new Thread(){
            public void run(){
                shimmer3BLE.connect(bluetoothAddress, "default");
            }
        };
        thread.start();
    }

    //BT Classic Scan
    private void doDiscovery() {

        // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
    }

    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning;
    private Handler handler = new Handler();
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    List<BluetoothDevice> listScanBleDevice = new ArrayList<BluetoothDevice>();

    //BLE Scan
    private void scanLeDevice(String deviceMacAddress) {
        getScannedBleDevices();
        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            scanning = true;
            scanForAllBleDevices();
            //scanForSpecificBleDevices(deviceMacAddress);

        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }
    private void scanForAllBleDevices() {
        bluetoothLeScanner.startScan(leScanCallback);
    }
    private void scanForSpecificBleDevices(String deviceMacAddress) {
        List<ScanFilter> scanFilters = new ArrayList<>();
        ScanFilter filter = new ScanFilter.Builder().setDeviceAddress(deviceMacAddress).build();
        scanFilters.add(filter);
        ScanSettings scanSettings = new ScanSettings.Builder().build();
        bluetoothLeScanner.startScan(scanFilters, scanSettings, leScanCallback);
    }

        // Device scan callback.
    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    BluetoothDevice bledevice = result.getDevice();
                    if(!listScanBleDevice.contains(result.getDevice())){
                        listScanBleDevice.add(result.getDevice());
                    }
                }
            };

    private void getScannedBleDevices(){
        for(BluetoothDevice dev : listScanBleDevice)
        {
            System.out.println(dev.getAddress());
        }

    }

    /**
     * Using the specified bluetooth address in the form of XX:XX:XX:XX:XX:XX check if the device is currently paired to the android device
     * @param bluetoothAddress
     * @return true if the device is paired
     */
    public boolean isDevicePaired(String bluetoothAddress){
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


    protected Shimmer4sdk initializeNewShimmer4(ShimmerRadioInitializer shimmerRadioInitializer) {
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
        shimmerRadioInitializer.useLegacyDelayBeforeBtRead(true);
        if(mProgressDialog!=null){
            mProgressDialog.dismiss();
        }
        ShimmerSerialPortAndroid serialPort = (ShimmerSerialPortAndroid) shimmerRadioInitializer.getSerialCommPort();
        Shimmer shimmer = new Shimmer(mHandler, mContext);
        shimmer.setDelayForBtRespone(true);
        mMapOfBtConnectedShimmers.put(bluetoothAddress, shimmer);
        try {
            ShimmerVerObject sVO = shimmerRadioInitializer.readShimmerVerObject();
            if (sVO.isShimmerGen3() || sVO.isShimmerGen3R()) {
                return initializeShimmer3(serialPort, shimmer);
            } else if (sVO.isShimmerGen2()) {
                return initializeShimmer2r(serialPort, shimmer);
            }
        } catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
        return null;
    }

    protected ShimmerDevice initializeShimmer2r(AbstractSerialPortHal abstractSerialPortComm, ShimmerDevice shimmerDevice) {
        ShimmerSerialPortAndroid serialPort = (ShimmerSerialPortAndroid) abstractSerialPortComm;
        ((Shimmer) shimmerDevice).setRadio(serialPort.getBluetoothSocket());
        shimmerDevice.addCommunicationRoute(Configuration.COMMUNICATION_TYPE.BLUETOOTH);
        return shimmerDevice;
    }

    protected ShimmerDevice initializeShimmer3(AbstractSerialPortHal abstractSerialPortComm, ShimmerDevice shimmerDevice) {
        ShimmerSerialPortAndroid serialPort = (ShimmerSerialPortAndroid) abstractSerialPortComm;
        ((Shimmer) shimmerDevice).setRadio(serialPort.getBluetoothSocket());
        shimmerDevice.addCommunicationRoute(Configuration.COMMUNICATION_TYPE.BLUETOOTH);
        return shimmerDevice;
    }

    @Override
    public void configureShimmer(final ShimmerDevice shimmerClone) {
        Thread thread = new Thread(){
            public void run(){
                configureShimmers(Arrays.asList(shimmerClone));
            }
        };

        thread.start();

    }

    @Override
    protected Shimmer4sdk createNewShimmer4(String comport, String bluetoothAddress) {
        if(mProgressDialog!=null){
            mProgressDialog.dismiss();
        }
        Shimmer4Android shimmer = new Shimmer4Android(mHandler);
        mMapOfBtConnectedShimmers.put(bluetoothAddress, shimmer);
        return shimmer;
    }

    @Override
    protected Shimmer4sdk createNewShimmer4(ShimmerRadioInitializer shimmerRadioInitializer, String bluetoothAddress) {
        shimmerRadioInitializer.useLegacyDelayBeforeBtRead(true);
        ShimmerSerialPortAndroid serialPortComm = (ShimmerSerialPortAndroid) shimmerRadioInitializer.getSerialCommPort();
        Shimmer4sdk shimmer4 = createNewShimmer4("", bluetoothAddress); //first parameter is com port, but we do not know it in android
        if(serialPortComm!=null){
            CommsProtocolRadio commsProtocolRadio = new CommsProtocolRadio(serialPortComm, new LiteProtocol(bluetoothAddress));
            shimmer4.setCommsProtocolRadio(commsProtocolRadio);
        }

        return shimmer4;
    }

    private void connectExistingShimmer4(Shimmer4Android shimmer4, String btAddress){
        shimmer4.addCommunicationRoute(Configuration.COMMUNICATION_TYPE.BLUETOOTH);
        shimmer4.setMacIdFromUart(btAddress);

        if(shimmer4.isReadyToConnect()){
            ShimmerRadioInitializerAndroid radio = (ShimmerRadioInitializerAndroid) getRadioInitializer(btAddress);
            AbstractSerialPortHal abstractComport = radio.getSerialCommPort();
            shimmer4.setCommsProtocolRadio(new CommsProtocolRadio(abstractComport, new LiteProtocol(btAddress)));

            try {
                shimmer4.connect();
            } catch (ShimmerException e) {
                e.printStackTrace();
            }


        }
    }
    @Override
    protected BluetoothDeviceDetails getBluetoothDeviceDetails(String connectionHandle) {
        return (BluetoothDeviceDetails)this.mMapOfParsedBtComPorts.get(connectionHandle);
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

    public HashMap<String, Object> getHashMapOfShimmersConnected() {
        HashMap<String, Object> mMultiShimmer = new HashMap<String, Object>(7);
        List<ShimmerDevice> deviceList = getListOfConnectedDevices();
        for(ShimmerDevice i : deviceList) {
            mMultiShimmer.put(i.getMacId(), i);
        }
        return mMultiShimmer;
    }

    public void addHandler(Handler handler) {
        //Add the Handler to each connected Shimmer device
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();
        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();

        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if ((stemp.getBluetoothRadioState()!= ShimmerBluetooth.BT_STATE.DISCONNECTED)){
                stemp.addHandler(handler);
            }
        }
    }

    /*
       ------------------- Methods from ShimmerService below -------------------
     */
    public void toggleAllLEDS() {

        HashMap<String, Object> mMultiShimmer = new HashMap<String, Object>(7);
        mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if (stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.CONNECTED || stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.SDLOGGING){
                stemp.toggleLed();
            }
        }
    }

    public void toggleLED(String bluetoothAddress) {
        HashMap<String, Object> mMultiShimmer = new HashMap<String, Object>(7);
        mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if ((stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.CONNECTED || stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
                stemp.toggleLed();
            }
        }

    }

    public void setAllAccelRange(int accelRange) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if ((stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.CONNECTED || stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.SDLOGGING)){
                stemp.writeAccelRange(accelRange);
            }
        }
    }

    public void setAllGSRRange(int gsrRange) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if ((stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.CONNECTED || stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.SDLOGGING)){
                stemp.writeGSRRange(gsrRange);
            }
        }
    }

    public void setAllEnabledSensors(int enabledSensors) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if ((stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.CONNECTED || stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.SDLOGGING)){
                stemp.writeEnabledSensors(enabledSensors);
            }
        }
    }

    public void writePMux(String bluetoothAddress,int setBit) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if ((stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.CONNECTED || stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
                stemp.writePMux(setBit);
            }
        }
    }

    public void write5VReg(String bluetoothAddress,int setBit) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if ((stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.CONNECTED || stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
                stemp.writeFiveVoltReg(setBit);
            }
        }
    }

    public List<String[]> getListofEnabledSensorSignals(String bluetoothAddress) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        List<String[]> listofSensors = new ArrayList<String[]>();
        Collection<Object> colS = mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            ShimmerDevice stemp = (ShimmerDevice) iterator.next();
            String address = stemp.getMacId();
            address = address.replace(":", "");
            bluetoothAddress = bluetoothAddress.replace(":", "");

            ShimmerBluetooth.BT_STATE btState = stemp.getBluetoothRadioState();
            if ((btState == ShimmerBluetooth.BT_STATE.STREAMING || btState == ShimmerBluetooth.BT_STATE.STREAMING_AND_SDLOGGING) && address.equals(bluetoothAddress)) {
                if (stemp.getShimmerVerObject().getHardwareVersion() == ShimmerVerDetails.HW_ID.SHIMMER_2R) {
                    return ((Shimmer) stemp).getListofEnabledChannelSignalsandFormats();
                } else {
                    List<SensorDetails> listOfEnabledSensorDetails = stemp.getListOfEnabledSensors();
                    for (SensorDetails sd : listOfEnabledSensorDetails) {
                        for (ChannelDetails cd : sd.mListOfChannels) {
                            for (ChannelDetails.CHANNEL_TYPE ct : cd.mListOfChannelTypes) {
                                String[] sensor = new String[4];
                                sensor[0] = stemp.getShimmerUserAssignedName();
                                sensor[1] = cd.mObjectClusterName;
                                sensor[2] = ct.toString();
                                sensor[3] = "";
                                if (ct.equals(ChannelDetails.CHANNEL_TYPE.UNCAL)) {
//									sensor[3] = cd.mDefaultUnit;
                                } else if (ct.equals(ChannelDetails.CHANNEL_TYPE.CAL)) {
//									sensor[3] = cd.mDefaultCalibratedUnits;
                                }
                                listofSensors.add(sensor);
                            }
                        }
                    }
                }
            }
        }
        return listofSensors;
    }

    public long getEnabledSensors(String bluetoothAddress) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        long enabledSensors=0;
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if ((stemp.getBluetoothRadioState()!= ShimmerBluetooth.BT_STATE.DISCONNECTED)){
                enabledSensors = stemp.getEnabledSensors();
            }
        }
        return enabledSensors;
    }

    public void writeAccelRange(String bluetoothAddress,int accelRange) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if ((stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.CONNECTED || stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
                stemp.writeAccelRange(accelRange);
            }
        }
    }

    public void writeGyroRange(String bluetoothAddress,int range) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if ((stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.CONNECTED || stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
                stemp.writeGyroRange(range);
            }
        }
    }

    public void writePressureResolution(String bluetoothAddress,int resolution) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if ((stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.CONNECTED || stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
                //currently not supported
                stemp.writePressureResolution(resolution);
            }
        }
    }

    public void writeMagRange(String bluetoothAddress,int range) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if ((stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.CONNECTED || stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
                stemp.writeMagRange(range);
            }
        }
    }

    public void writeGSRRange(String bluetoothAddress,int gsrRange) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if ((stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.CONNECTED || stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
                stemp.writeGSRRange(gsrRange);
            }
        }
    }

    public double getSamplingRate(String bluetoothAddress) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        double SRate=-1;
        while (iterator.hasNext()) {
            ShimmerDevice stemp=(ShimmerDevice) iterator.next();
            String address = stemp.getMacId();
            address = address.replace(":","");
            bluetoothAddress = bluetoothAddress.replace(":","");
            if (address.equals(bluetoothAddress)){
                SRate= stemp.getSamplingRateShimmer(Configuration.COMMUNICATION_TYPE.BLUETOOTH);
            }
        }
        return SRate;
    }

    public int getAccelRange(String bluetoothAddress) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        int aRange=-1;
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
                aRange = stemp.getAccelRange();
            }
        }
        return aRange;
    }

    public ShimmerBluetooth.BT_STATE getShimmerState(String bluetoothAddress){
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        ShimmerBluetooth.BT_STATE status= ShimmerBluetooth.BT_STATE.DISCONNECTED;
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
                status = stemp.getBluetoothRadioState();

            }
        }
        return status;

    }

    public int getGSRRange(String bluetoothAddress) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        int gRange=-1;
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
                gRange = stemp.getGSRRange();
            }
        }
        return gRange;
    }

    public int get5VReg(String bluetoothAddress) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        int fiveVReg=-1;
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
                fiveVReg = stemp.get5VReg();
            }
        }
        return fiveVReg;
    }

    public boolean isLowPowerMagEnabled(String bluetoothAddress) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        boolean enabled=false;
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
                enabled = stemp.isLowPowerMagEnabled();
            }
        }
        return enabled;
    }

    public int getpmux(String bluetoothAddress) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        int pmux=-1;
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
                pmux = stemp.getPMux();
            }
        }
        return pmux;
    }

    public void startLogging(String bluetoothAddress) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if ((stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.CONNECTED || stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.STREAMING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
                stemp.startSDLogging();
            }
        }
    }

    public void stopLogging(String bluetoothAddress) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if ((stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.STREAMING_AND_SDLOGGING || stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
                stemp.stopSDLogging();
            }
        }
    }

    public void startLoggingAndStreaming(String bluetoothAddress) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
                stemp.startDataLogAndStreaming();
            }
        }
    }

    public long sensorConflictCheckandCorrection(String bluetoothAddress, long enabledSensors, int sensorToCheck) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        long newSensorBitmap = 0;
        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
                newSensorBitmap = stemp.sensorConflictCheckandCorrection(enabledSensors,sensorToCheck);
            }
        }
        return newSensorBitmap;
    }

    public List<String> getListofEnabledSensors(String bluetoothAddress) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        List<String> listofSensors = null;
        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            ShimmerDevice stemp=(ShimmerDevice) iterator.next();
            if (stemp.getMacId().equals(bluetoothAddress)){
                if (stemp instanceof Shimmer) {
                    listofSensors = ((Shimmer)stemp).getListofEnabledSensors();
                } else if(stemp instanceof Shimmer4Android){
                    listofSensors = new ArrayList<String>();
                }
            }
        }
        return listofSensors;
    }

    public boolean bluetoothAddressComparator(String bluetoothAddress, String address){
        address = address.replace(":","");
        bluetoothAddress = bluetoothAddress.replace(":","");
        if (address.equals(bluetoothAddress)){
            return true;
        } else {
            return false;
        }
    }

    public void setBlinkLEDCMD(String bluetoothAddress) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
        if (stemp!=null){
            if ((stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.CONNECTED || stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
                if (stemp.getCurrentLEDStatus()==0){
                    stemp.writeLEDCommand(1);
                } else {
                    stemp.writeLEDCommand(0);
                }
            }
        }

    }

    public void enableLowPowerMag(String bluetoothAddress,boolean enable) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
        if (stemp!=null){
            if ((stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.CONNECTED || stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
                stemp.enableLowPowerMag(enable);
            }
        }
    }

    public void setBattLimitWarning(String bluetoothAddress, double limit) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
        if (stemp!=null){
            if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
                stemp.setBattLimitWarning(limit);
            }
        }

    }

    public double getBattLimitWarning(String bluetoothAddress) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        double limit=-1;
        Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
        if (stemp!=null){
            if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
                limit=stemp.getBattLimitWarning();
            }
        }
        return limit;
    }

    public double getPacketReceptionRate(String bluetoothAddress) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        double rate=-1;
        Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
        if (stemp!=null){
            if (stemp.getBluetoothAddress().equals(bluetoothAddress)){
                rate=stemp.getPacketReceptionRate();
            }
        }
        return rate;
    }

    public boolean DevicesConnected(String bluetoothAddress){
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        if (bluetoothAddress==null){
            return false;
        }
        boolean deviceConnected=false;
        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            ShimmerDevice stemp=(ShimmerDevice) iterator.next();
            String address = stemp.getMacId();
            address = address.replace(":","");
            bluetoothAddress = bluetoothAddress.replace(":","");
            if (stemp.getBluetoothRadioState()!= ShimmerBluetooth.BT_STATE.DISCONNECTED && address.equals(bluetoothAddress)){
                deviceConnected=true;
            }
        }
        return deviceConnected;
    }

    public boolean DeviceIsLogging(String bluetoothAddress){
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        boolean deviceLogging=false;
        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            ShimmerDevice stemp=(ShimmerDevice) iterator.next();
            String address = stemp.getMacId();
            address = address.replace(":","");
            bluetoothAddress = bluetoothAddress.replace(":","");
            if ((stemp.mBluetoothRadioState == ShimmerBluetooth.BT_STATE.SDLOGGING || stemp.mBluetoothRadioState == ShimmerBluetooth.BT_STATE.STREAMING_AND_SDLOGGING)   && address.equals(bluetoothAddress)){
                deviceLogging=true;
            }
        }
        return deviceLogging;
    }

    public boolean DeviceIsStreaming(String bluetoothAddress){
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        boolean deviceStreaming=false;
        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            ShimmerDevice stemp=(ShimmerDevice) iterator.next();
            String address = stemp.getMacId();
            address = address.replace(":","");
            bluetoothAddress = bluetoothAddress.replace(":","");
            ShimmerBluetooth.BT_STATE btState = stemp.getBluetoothRadioState();
            if ((btState== ShimmerBluetooth.BT_STATE.STREAMING || btState== ShimmerBluetooth.BT_STATE.STREAMING_AND_SDLOGGING)  && address.equals(bluetoothAddress)){
                deviceStreaming=true;
            }
        }
        return deviceStreaming;
    }

    public String getFWVersion (String bluetoothAddress){
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        String version="";
        ShimmerDevice stemp=(ShimmerDevice) mMultiShimmer.get(bluetoothAddress);
        if (stemp!=null){
            version=stemp.getFirmwareVersionMajor()+"."+stemp.getFirmwareVersionMinor();
        }
        return version;
    }

    public int getShimmerVersion (String bluetoothAddress){
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        int version=0;
        ShimmerDevice stemp=(ShimmerDevice) mMultiShimmer.get(bluetoothAddress);
        if (stemp!=null){
            version=stemp.getHardwareVersion();
        }
        return version;
    }

    public ShimmerDevice getShimmer(String bluetoothAddress){
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        ShimmerDevice shimmer = null;
        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            shimmer=(ShimmerDevice) iterator.next();
            String address = shimmer.getMacId();
            address = address.replace(":","");
            bluetoothAddress = bluetoothAddress.replace(":","");
            if (address.equals(bluetoothAddress)){
                return shimmer;
            }
        }
        return shimmer;
    }

    public void writeEXGSetting(String bluetoothAddress,int setting) {
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Collection<Object> colS=mMultiShimmer.values();
        Iterator<Object> iterator = colS.iterator();
        while (iterator.hasNext()) {
            Shimmer stemp=(Shimmer) iterator.next();
            if ((stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.CONNECTED || stemp.getBluetoothRadioState()== ShimmerBluetooth.BT_STATE.SDLOGGING) && stemp.getBluetoothAddress().equals(bluetoothAddress)){
                if (setting==0){
                    stemp.enableDefaultECGConfiguration();
                } else if (setting==1){
                    stemp.enableDefaultEMGConfiguration();
                } else if (setting==2){
                    stemp.enableEXGTestSignal();

                }

            } else {

            }
        }
    }

    public boolean isUsingLogAndStreamFW(String bluetoothAddress){
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        boolean logAndStream = false;
        ShimmerDevice stemp=(ShimmerDevice) mMultiShimmer.get(bluetoothAddress);
        if (stemp!=null){
            if(stemp.getFirmwareIdentifier()==3)
                logAndStream = true;
        }
        return logAndStream;

    }

    public void readStatusLogAndStream(String bluetoothAddress){
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
        if (stemp!=null){
            if(stemp.getFirmwareIdentifier()==3)
                stemp.readStatusLogAndStream();
        }
    }

    public boolean isSensing(String bluetoothAddress){
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
        if (stemp!=null){
            if(stemp.getFirmwareIdentifier()==3)
                return stemp.isSensing();
        }

        return false;
    }

    public boolean isDocked(String bluetoothAddress){
        HashMap<String, Object> mMultiShimmer = getHashMapOfShimmersConnected();

        Shimmer stemp=(Shimmer) mMultiShimmer.get(bluetoothAddress);
        if (stemp!=null){
            if(stemp.getFirmwareIdentifier()==3)
                return stemp.isDocked();
        }

        return false;
    }

}
