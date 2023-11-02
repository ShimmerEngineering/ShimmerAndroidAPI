package com.shimmerresearch.androidradiodriver;

import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.verisense.communication.AbstractByteCommunication;
import java.util.UUID;

public class VerisenseBLERadioByteCommunication extends AbstractByteCommunication {
    String TxID = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
    String RxID = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";
    String ServiceID = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
    UUID sid = UUID.fromString(ServiceID);
    UUID txid = UUID.fromString(TxID);
    UUID rxid = UUID.fromString(RxID);
    String mMac;
    String uuid;
    BluetoothLeService bluetoothLeService;
    public VerisenseBLERadioByteCommunication(String mac, BluetoothLeService bluetoothLeService) {
        mMac = mac;
        this.bluetoothLeService = bluetoothLeService;
    }

    @Override
    public void connect() throws ShimmerException {
        bluetoothLeService.initialize(mByteCommunicationListener);
        bluetoothLeService.connect(mMac);
    }

    @Override
    public void disconnect() throws ShimmerException {
        bluetoothLeService.disconnect();
    }

    @Override
    public void writeBytes(byte[] bytes) {
        bluetoothLeService.writeCharacteristic(sid, txid, bytes);
    }

    @Override
    public void stop() {

    }

    @Override
    public String getUuid() {
        return null;
    }

}

