package com.shimmerresearch.android;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;

import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.verisense.VerisenseDevice;

import java.util.ArrayList;
import java.util.List;

public class VerisenseDeviceAndroid extends VerisenseDevice {
    transient List<Handler> mHandlerList = new ArrayList<Handler>();

    public VerisenseDeviceAndroid(Handler handler) {
        super();
        mHandlerList.add(0, handler);
    }

    @Override
    protected void dataHandler(ObjectCluster ojc){
        sendMsgToHandlerListTarget(ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET, ojc);
    }

    private void sendMsgToHandlerListTarget(int what, Object object) {
        for(Handler handler : mHandlerList) {
            if (handler!=null) {
                handler.obtainMessage(what, object).sendToTarget();
            }
        }
    }
}
