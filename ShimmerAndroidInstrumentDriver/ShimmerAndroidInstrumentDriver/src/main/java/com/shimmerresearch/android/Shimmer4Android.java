package com.shimmerresearch.android;

import android.os.Handler;

import com.shimmerresearch.driver.*;

/**
 * Created by Lim on 06/06/2016.
 */

public class Shimmer4Android extends Shimmer4 {

    public final Handler mHandler;

    @Override
    public void sendCallBackMsg(int msgid,Object obj){
        mHandler.obtainMessage(msgid, obj).sendToTarget();
    }

    public Shimmer4Android(Handler handler){
        mHandler = handler;
    }



}
