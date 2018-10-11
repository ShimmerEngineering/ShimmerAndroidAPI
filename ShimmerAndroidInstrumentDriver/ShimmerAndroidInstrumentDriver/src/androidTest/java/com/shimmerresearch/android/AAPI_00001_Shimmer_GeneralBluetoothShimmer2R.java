package com.shimmerresearch.android;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;


import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.exceptions.ShimmerException;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.Collection;

import static com.shimmerresearch.bluetooth.ShimmerBluetooth.NOTIFICATION_SHIMMER_FULLY_INITIALIZED;
import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class AAPI_00001_Shimmer_GeneralBluetoothShimmer2R {
    private boolean mFirstTimeConnection = true;
    final static String LOG_TAG = "ShimmerLegacyExample";
    final int DELAY_DURATION_MS = 3000;
    static Handler handler = null;
    final static Shimmer shimmer = new Shimmer (handler);
    public static final int SHIMMER_2R = 2;

    //btManager.connect("00:06:66:42:0B:54");
    //String macAddress = "00:06:66:42:0B:54";
    //shimmer.connect(00:06:66:42:0B:54, "default");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        shimmer.connect("00:06:66:42:0B:54", "default");
        Thread.sleep(10000);
    }

    @Test   //test Shimmer 2R connection
    public void testAConnect() throws Exception {
        //shimmer = new Shimmer(mHandler);

        if (shimmer.isConnected()) {
            assertTrue(true);
        } else {
            assertTrue(false);
        }
        System.out.println("GeneralBluetoothShimmer2RTest A : Connection");

    }

    @Test   //get hardware version of Shimmer 2R
    public void testBGetHW() throws Exception {
        System.out.println("GeneralBluetoothShimmer2RTest B : Hardware version");
        if (shimmer.getHardwareVersion() == ShimmerVerDetails.HW_ID.SHIMMER_2R) {
            assertTrue(true);
        } else {
            assertTrue(false);
        }

    }

    @Test   //get firmware version of Shimmer 2R
    public void testCGetFW() throws Exception {

        if (shimmer.getFirmwareIdentifier() == ShimmerVerDetails.FW_ID.BTSTREAM) {
            assertTrue(true);
        } else {
            assertTrue(false);
        }
        System.out.println("GeneralBluetoothShimmer2RTest C : Firmware version");
    }

    @Test   //write and read sampling rate of the hardware
    public void testDWriteReadSamplingRate() throws Exception {

        double samplingrate;
        shimmer.readSamplingRate();
        Thread.sleep(DELAY_DURATION_MS);
        System.out.println("Current Shimmer 2R sampling rate is:" + shimmer.getSamplingRateShimmer());

        if (shimmer.getSamplingRateShimmer() == 128) {
            samplingrate = 51.2;
        } else {
            samplingrate = 128;
        }
        shimmer.writeShimmerAndSensorsSamplingRate(samplingrate);
        Thread.sleep(DELAY_DURATION_MS);
        shimmer.readSamplingRate();
        Thread.sleep(DELAY_DURATION_MS);


        if (shimmer.getSamplingRateShimmer() == samplingrate) {
            assertTrue(true);
        } else {
            assertTrue(false);
        }
        System.out.println("GeneralBluetoothShimmer2RTest D : Sampling Rate");
    }

    @Test   //test Shimmer 2R start streaming
    public void testEStartStreaming() throws Exception {

        shimmer.startStreaming();
        Thread.sleep(DELAY_DURATION_MS);
        if (shimmer.isStreaming()) {
            assertTrue(true);
        } else {
            assertTrue(false);
        }
        System.out.println("GeneralBluetoothShimmer2RTest E : Start Streaming");
    }

    @Test   //test Shimmer 2R stop streaming
    public void testFStopStreaming() throws Exception {
        shimmer.stopStreaming();
        Thread.sleep(DELAY_DURATION_MS);
        if (shimmer.isStreaming()) {
            assertTrue(false);
        } else {
            assertTrue(true);
        }
        System.out.println("GeneralBluetoothShimmer2RTest F : Stop Streaming");
    }

    @Test      //test for multiple start and stop streaming
    public void testGMultipleStartStopStreaming() throws Exception {
        for (int i = 1; i < 8; i++) {

            //odd value of i will start streaming
            if (i % 2 != 0) {
                shimmer.startStreaming();
                Thread.sleep(DELAY_DURATION_MS);
                if (shimmer.isStreaming()) {
                    assertTrue(true);
                } else {
                    assertTrue(false);
                }
            }
            //even value of i will stop streaming
            else {
                shimmer.stopStreaming();
                Thread.sleep(DELAY_DURATION_MS);
                if (shimmer.isStreaming()) {
                    assertTrue(false);
                } else {
                    assertTrue(true);
                }
            }
        }
        System.out.println("GeneralBluetoothShimmer2RTest G : Multiple start and stop streaming");
    }

    @Test    //test Shimmer 2R disconnection
    public void testHDisconnect() {
        shimmer.disconnect();
        if (shimmer.isConnected()) {
            assertTrue(false);
        } else {
            assertTrue(true);
        }
        System.out.println("GeneralBluetoothShimmer2RTest H : Disconnect");
    }

}