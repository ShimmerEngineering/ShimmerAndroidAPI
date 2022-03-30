package com.shimmerresearch.androidmodule;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import java.util.Map;
import java.util.HashMap;

public class VerisenseAndroidModule extends ReactContextBaseJavaModule {
    VerisenseAndroidModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "VerisenseDeviceAndroid";
    }

    @ReactMethod
    public void connect() {
        System.out.println("connect");
    }

    @ReactMethod
    public void disconnect() {
        System.out.println("disconnect");
    }

    @ReactMethod
    public void startStreaming() {
        System.out.println("startStreaming");
    }

    @ReactMethod
    public void stopStreaming() {
        System.out.println("stopStreaming");
    }
}