package com.shimmerresearch.android;

import android.os.Handler;

import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.driver.shimmer4sdk.Shimmer4;

/**
 * Created by Lim on 06/06/2016.
 */

public class Shimmer4Android extends Shimmer4 {

    transient public final Handler mHandler;

    @Override
    public void sendCallBackMsg(int msgid,Object obj){
        mHandler.obtainMessage(msgid, obj).sendToTarget();
    }

    public Shimmer4Android(Handler handler){
        mHandler = handler;
    }

    public void configureShimmer(ShimmerDevice cloneShimmer) {

        Shimmer4 cloneShimmerCast = (Shimmer4) cloneShimmer;
        operationPrepare();
        writeConfigBytes(cloneShimmerCast.getShimmerInfoMemBytes());
        writeCalibrationDump(cloneShimmerCast.calibByteDumpGenerate());
        operationStart(ShimmerBluetooth.BT_STATE.CONFIGURING);

    }


}
