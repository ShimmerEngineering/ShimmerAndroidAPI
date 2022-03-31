package com.shimmerresearch.androidmodule;


import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.clj.fastble.BleManager
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.shimmerresearch.android.VerisenseDeviceAndroid
import com.shimmerresearch.androidradiodriver.AndroidBleRadioByteCommunication
import com.shimmerresearch.bluetooth.ShimmerBluetooth
import com.shimmerresearch.driver.CallbackObject
import com.shimmerresearch.driver.Configuration
import com.shimmerresearch.driver.FormatCluster
import com.shimmerresearch.driver.ObjectCluster
import com.shimmerresearch.verisense.communication.VerisenseProtocolByteCommunication

class VerisenseAndroidModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private var mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET -> if (msg.obj is ObjectCluster) {

                    val objectCluster = msg.obj as ObjectCluster;
                    var allFormats = objectCluster.getCollectionOfFormatClusters(Configuration.Shimmer3.ObjectClusterSensorName.TIMESTAMP);
                    val timeStampCluster = ObjectCluster.returnFormatCluster(allFormats, "CAL") as FormatCluster;
                    val timeStampData = timeStampCluster.mData;
                    var params3: WritableMap = Arguments.createMap();
                    params3.putDouble("eventProperty", timeStampData);
                    sendEvent(reactApplicationContext, "streaming", params3);
                }
                ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE -> {
                    var state: ShimmerBluetooth.BT_STATE? = null
                    var macAddress: String? = ""
                    if (msg.obj is ObjectCluster) {
                        state = (msg.obj as ObjectCluster).mState
                        macAddress = (msg.obj as ObjectCluster).macAddress
                    } else if (msg.obj is CallbackObject) {
                        state = (msg.obj as CallbackObject).mState
                        macAddress = (msg.obj as CallbackObject).mBluetoothAddress
                    }
                    when (state) {
                        ShimmerBluetooth.BT_STATE.CONNECTED -> {
                            var params2: WritableMap = Arguments.createMap();
                            params2.putString("eventProperty", "connected");
                            sendEvent(reactApplicationContext, "status", params2);
                        }
                        ShimmerBluetooth.BT_STATE.CONNECTING -> {
                            var params3: WritableMap = Arguments.createMap();
                            params3.putString("eventProperty", "connecting");
                            sendEvent(reactApplicationContext, "status", params3);
                        }
                        ShimmerBluetooth.BT_STATE.STREAMING -> {
                            var params3: WritableMap = Arguments.createMap();
                            params3.putString("eventProperty", "streaming");
                            sendEvent(reactApplicationContext, "status", params3);
                        }
                        ShimmerBluetooth.BT_STATE.STREAMING_AND_SDLOGGING -> {
                        }
                        ShimmerBluetooth.BT_STATE.SDLOGGING -> {
                        }
                        ShimmerBluetooth.BT_STATE.DISCONNECTED -> {
                            var params3: WritableMap = Arguments.createMap();
                            params3.putString("eventProperty", "disconnected");
                            sendEvent(reactApplicationContext, "status", params3);
                        }
                    }
                }
            }
            super.handleMessage(msg)
        }
    }

    var device: VerisenseDeviceAndroid = VerisenseDeviceAndroid(mHandler);

    override fun getName(): String {
        return "VerisenseDeviceAndroidModule"
    }

    fun sendEvent(reactContext: ReactApplicationContext, eventName: String, params: WritableMap?) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params);
    }

    @ReactMethod
    fun addListener(type: String?) {
        // Keep: Required for RN built in Event Emitter Calls.
    }

    @ReactMethod
    fun removeListeners(type: String?) {
        // Keep: Required for RN built in Event Emitter Calls.
    }

    @ReactMethod
    fun connect() {
        val radio1 = AndroidBleRadioByteCommunication("CB:3F:94:5F:84:E6")
        val protocol1 = VerisenseProtocolByteCommunication(radio1)

        BleManager.getInstance().init(reactApplicationContext.applicationContext as Application)
        device = VerisenseDeviceAndroid(mHandler);
        device.setProtocol(Configuration.COMMUNICATION_TYPE.BLUETOOTH, protocol1)
        device.connect();
    }

    @ReactMethod
    fun disconnect() {
        device.disconnect();
    }

    @ReactMethod
    fun startStreaming() {
        device.startStreaming();
    }

    @ReactMethod
    fun stopStreaming() {
        device.stopStreaming();
    }
}