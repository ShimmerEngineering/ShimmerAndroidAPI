package com.shimmerresearch.shimmerserviceexample;

import android.app.Activity;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.androidplot.xy.XYPlot;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.guiUtilities.supportfragments.ConnectedShimmersListFragment;
import com.shimmerresearch.android.guiUtilities.supportfragments.DeviceConfigFragment;
import com.shimmerresearch.android.guiUtilities.supportfragments.PlotFragment;
import com.shimmerresearch.android.guiUtilities.supportfragments.SensorsEnabledFragment;
import com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog;
import com.shimmerresearch.android.guiUtilities.ShimmerDialogConfigurations;
import com.shimmerresearch.android.guiUtilities.supportfragments.SignalsToPlotFragment;
import com.shimmerresearch.android.shimmerService.ShimmerService;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ConnectedShimmersListFragment.OnShimmerDeviceSelectedListener, SensorsEnabledFragment.OnSensorsSelectedListener {

    ShimmerDialogConfigurations dialog;
    BluetoothAdapter btAdapter;
    ShimmerService mService;
    SensorsEnabledFragment sensorsEnabledFragment;
    ConnectedShimmersListFragment connectedShimmersListFragment;
    DeviceConfigFragment deviceConfigFragment;
    PlotFragment plotFragment;
    SignalsToPlotFragment signalsToPlotFragment;
    public String selectedDeviceAddress, selectedDeviceName;
    boolean mServiceFirstTime;

    XYPlot dynamicPlot;


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter1 mSectionsPagerAdapter1;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;


    //Extra for intent from ShimmerBluetoothDialog
    public static final String EXTRA_DEVICE_ADDRESS = "device_address";

    boolean isServiceStarted = false;

    final static String LOG_TAG = "Shimmer";
    final static String SERVICE_TAG = "ShimmerService";
    final static int REQUEST_CONNECT_SHIMMER = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter1 = new SectionsPagerAdapter1(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter1);
        mViewPager.setOffscreenPageLimit(4);    //Ensure none of the fragments has their view destroyed when off-screen

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        dialog = new ShimmerDialogConfigurations();

        sensorsEnabledFragment = SensorsEnabledFragment.newInstance(null, null);
        connectedShimmersListFragment = ConnectedShimmersListFragment.newInstance();
        deviceConfigFragment = DeviceConfigFragment.newInstance();
        plotFragment = PlotFragment.newInstance();
        signalsToPlotFragment = SignalsToPlotFragment.newInstance();

        //Check if Bluetooth is enabled
        if (!btAdapter.isEnabled()) {
            int REQUEST_ENABLE_BT = 1;
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            Intent intent = new Intent(this, ShimmerService.class);
            startService(intent);
            getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            Log.d(LOG_TAG, "Shimmer Service started");
            Toast.makeText(this, "Shimmer Service started", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.connect_device:
                Intent pairedDevicesIntent = new Intent(getApplicationContext(), ShimmerBluetoothDialog.class);
                startActivityForResult(pairedDevicesIntent, REQUEST_CONNECT_SHIMMER);
                return true;
            case R.id.start_streaming:
                if(selectedDeviceAddress != null) {
                    ShimmerDevice mDevice1 = mService.getShimmer(selectedDeviceAddress);
                    mDevice1.startStreaming();
                    signalsToPlotFragment.buildSignalsToPlotList(this, mService, selectedDeviceAddress, dynamicPlot);
                }
                return true;
            case R.id.stop_streaming:
                if(selectedDeviceAddress != null) {
                    ShimmerDevice mDevice2 = mService.getShimmer(selectedDeviceAddress);
                    mDevice2.stopStreaming();
                    sensorsEnabledFragment.buildSensorsList(mDevice2, this, mService.getBluetoothManager());
                    deviceConfigFragment.buildDeviceConfigList(mDevice2, this, mService.getBluetoothManager());
                }
                return true;
            case R.id.disconnect_all_devices:
                mService.disconnectAllDevices();
                connectedShimmersListFragment.buildShimmersConnectedListView(null, getApplicationContext());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mService = ((ShimmerService.LocalBinder) service).getService();
            isServiceStarted = true;
            //Add this activity's Handler to the service's list of Handlers so we know when a Shimmer is connected/disconnected
            mService.addHandlerToList(mHandler);
            Log.d(SERVICE_TAG, "Shimmer Service Bound");

            //if there is a device connected display it on the fragment
            List<ShimmerDevice> deviceList = mService.getListOfConnectedDevices();
            connectedShimmersListFragment.buildShimmersConnectedListView(deviceList, getApplicationContext());

        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mService = null;
            isServiceStarted = false;
            Log.d(SERVICE_TAG, "Shimmer Service Disconnected");
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) { //The system Bluetooth enable dialog has returned a result
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, ShimmerService.class);
                startService(intent);
                getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                Log.d(LOG_TAG, "Shimmer Service started");
                Toast.makeText(this, "Shimmer Service started", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Please enable Bluetooth to proceed.", Toast.LENGTH_LONG).show();
                int REQUEST_ENABLE_BT = 1;
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                Toast.makeText(this, "Unknown Error! Your device may not support Bluetooth!", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == 2) { //The devices paired list has returned a result
            if (resultCode == Activity.RESULT_OK) {
                //Get the Bluetooth mac address of the selected device:
                String macAdd = data.getStringExtra(EXTRA_DEVICE_ADDRESS);
                mService.connectShimmer(macAdd,this);    //Connect to the selected device, and set context to show progress dialog when pairing
            }
        }
    }

    public class SectionsPagerAdapter1 extends FragmentPagerAdapter {

        public SectionsPagerAdapter1(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
                if(position == 0) {
                    connectedShimmersListFragment.buildShimmersConnectedListView(null, getApplicationContext());
                    return connectedShimmersListFragment;
                }
                else if(position == 1) {
                    return sensorsEnabledFragment;
                }
                else if (position == 2) {
                    return deviceConfigFragment;
                }
                else if (position == 3) {
                    return plotFragment;
                }
                else if (position == 4) {
                    return signalsToPlotFragment;
                }
                else {
                    return null;
                }
        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return "Connected Devices";
                case 1: return "Enable Sensors";
                case 2: return "Device Configuration";
                case 3: return "Plot";
                case 4: return "Signals to Plot";
                default: return "";
            }
        }

    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if(msg.what == ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE) {
                ShimmerBluetooth.BT_STATE state = null;
                String macAddress = "";
                String shimmerName = "";
                if (msg.obj instanceof ObjectCluster){
                    state = ((ObjectCluster)msg.obj).mState;
                    macAddress = ((ObjectCluster)msg.obj).getMacAddress();
                    shimmerName = ((ObjectCluster) msg.obj).getShimmerName();
                } else if(msg.obj instanceof CallbackObject){
                    state = ((CallbackObject)msg.obj).mState;
                    macAddress = ((CallbackObject)msg.obj).mBluetoothAddress;
                    shimmerName = "";
                }
                switch (state) {
                    case CONNECTED:
                        List<ShimmerDevice> deviceList = mService.getListOfConnectedDevices();
                        connectedShimmersListFragment.buildShimmersConnectedListView(deviceList, getApplicationContext());
                        break;
                    case CONNECTING:
                        break;
                    case STREAMING:
                        Toast.makeText(getApplicationContext(), "Device streaming: " + shimmerName + " " + macAddress, Toast.LENGTH_SHORT).show();
                        if(selectedDeviceAddress.contains(macAddress) && dynamicPlot != null) {
                            //If the selected device is the one that is now streaming, then show the list of signals available to be plotted
                            signalsToPlotFragment.buildSignalsToPlotList(getApplicationContext(), mService, macAddress, dynamicPlot);
                        }
                        break;
                    case STREAMING_AND_SDLOGGING:
                        if(selectedDeviceAddress.contains(macAddress) && dynamicPlot != null) {
                            signalsToPlotFragment.buildSignalsToPlotList(getApplicationContext(), mService, macAddress, dynamicPlot);
                        }
                        break;
                    case SDLOGGING:
                        break;
                    case DISCONNECTED:
                        Toast.makeText(getApplicationContext(), "Device disconnected: " + shimmerName + " " + macAddress, Toast.LENGTH_SHORT).show();
                        break;
                }

            }

            if(msg.arg1 == Shimmer.MSG_STATE_STOP_STREAMING) {
                signalsToPlotFragment.setDeviceNotStreamingView();
            }



        }
    };

    /**
     * This method is called when the ConnectedShimmersListFragment returns a selected Shimmer
     * @param macAddress
     */
    @Override
    public void onShimmerDeviceSelected(String macAddress, String deviceName) {
        Toast.makeText(this, "Selected Device: " + deviceName + "\n" + macAddress, Toast.LENGTH_SHORT).show();
        selectedDeviceAddress = macAddress;
        selectedDeviceName = deviceName;

        //Pass the selected device to the fragments
        ShimmerDevice device = mService.getShimmer(selectedDeviceAddress);

        sensorsEnabledFragment.setShimmerService(mService);
        sensorsEnabledFragment.buildSensorsList(device, this, mService.getBluetoothManager());

        deviceConfigFragment.buildDeviceConfigList(device, this, mService.getBluetoothManager());

        plotFragment.setShimmerService(mService);
        plotFragment.clearPlot();
        dynamicPlot = plotFragment.getDynamicPlot();

        mService.stopStreamingAllDevices();
    }

    @Override
    public void onSensorsSelected() {
        ShimmerDevice device = mService.getShimmer(selectedDeviceAddress);
        deviceConfigFragment.buildDeviceConfigList(device, this, mService.getBluetoothManager());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent=new Intent(this, ShimmerService.class);
        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

}
