package com.shimmerresearch.android;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExampleInstrumentedTest {
    @Test
    public void testA() throws Exception {
        // Context of the app under test.
        assertTrue(true);
        System.out.println("unit test a");
    }

    @Test
    public void testB() throws Exception {
        // Context of the app under test.
        assertTrue(true);
        System.out.println("unit test b");
    }

    @Test
    public void testC() throws Exception {
        // Context of the app under test.
        assertTrue(true);
        System.out.println("unit test c");
    }
}
