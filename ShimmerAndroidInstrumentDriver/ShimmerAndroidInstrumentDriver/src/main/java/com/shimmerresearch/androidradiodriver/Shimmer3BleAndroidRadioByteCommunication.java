package com.shimmerresearch.androidradiodriver;

import java.util.UUID;

public class Shimmer3BleAndroidRadioByteCommunication extends VerisenseBleAndroidRadioByteCommunication{



    /**
     * Initialize a ble radio
     *
     * @param mac mac address of the verisense device e.g. d0:2b:46:3d:a2:bb
     */
    public Shimmer3BleAndroidRadioByteCommunication(String mac) {
        super(mac);
        TxID = "49535343-8841-43f4-a8d4-ecbe34729bb3";
        RxID = "49535343-1e4d-4bd9-ba61-23c647249616";
        ServiceID = "49535343-fe7d-4ae5-8fa9-9fafd205e455";
        sid = UUID.fromString(ServiceID);
        txid = UUID.fromString(TxID);
        rxid = UUID.fromString(RxID);
    }
}
