package com.shimmerresearch.shimmerserviceexample;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
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
import com.clj.fastble.BleManager;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.guiUtilities.supportfragments.ConnectedShimmersListFragment;
import com.shimmerresearch.android.guiUtilities.supportfragments.DeviceConfigFragment;
import com.shimmerresearch.android.guiUtilities.supportfragments.PlotFragment;
import com.shimmerresearch.android.guiUtilities.supportfragments.SensorsEnabledFragment;
import com.shimmerresearch.android.guiUtilities.supportfragments.DataSyncFragment;
import com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog;
import com.shimmerresearch.android.guiUtilities.ShimmerDialogConfigurations;
import com.shimmerresearch.android.guiUtilities.supportfragments.SignalsToPlotFragment;
import com.shimmerresearch.android.shimmerService.ShimmerService;
import com.shimmerresearch.androidradiodriver.AndroidBleRadioByteCommunication;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.exceptions.ShimmerException;
import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.verisense.VerisenseDevice;
import com.shimmerresearch.verisense.communication.SyncProgressDetails;
import com.shimmerresearch.android.VerisenseDeviceAndroid;

import java.util.List;
import java.util.ArrayList;

import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.EXTRA_DEVICE_ADDRESS;
import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.EXTRA_DEVICE_NAME;

public class MainActivity extends AppCompatActivity implements ConnectedShimmersListFragment.OnShimmerDeviceSelectedListener, SensorsEnabledFragment.OnSensorsSelectedListener {

    ShimmerDialogConfigurations dialog;
    BluetoothAdapter btAdapter;
    ShimmerService mService;
    SensorsEnabledFragment sensorsEnabledFragment;
    ConnectedShimmersListFragment connectedShimmersListFragment;
    DeviceConfigFragment deviceConfigFragment;
    PlotFragment plotFragment;
    SignalsToPlotFragment signalsToPlotFragment;
    DataSyncFragment dataSyncFragment;
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

    boolean isServiceStarted = false;

    final static String LOG_TAG = "Shimmer";
    final static String SERVICE_TAG = "ShimmerService";
    final static int REQUEST_CONNECT_SHIMMER = 2;
    final static int PERMISSIONS_REQUEST_WRITE_STORAGE = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BleManager.getInstance().init(getApplication());
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter1 = new SectionsPagerAdapter1(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter1);
        mViewPager.setOffscreenPageLimit(5);    //Ensure none of the fragments has their view destroyed when off-screen

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        dialog = new ShimmerDialogConfigurations();

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

        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE )!= PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_STORAGE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.data_sync);
        if(selectedDeviceAddress != null){
            ShimmerDevice device = mService.getShimmer(selectedDeviceAddress);
            if(device instanceof VerisenseDeviceAndroid) {
                item.setVisible(true);
                return true;
            }
        }
        item.setVisible(false);
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
                    try {
                        mDevice1.startStreaming();
                    } catch (ShimmerException e) {
                        e.printStackTrace();
                    }
                    signalsToPlotFragment.buildSignalsToPlotList(this, mService, selectedDeviceAddress, dynamicPlot);
                }
                return true;
            case R.id.stop_streaming:
                if(selectedDeviceAddress != null) {
                    ShimmerDevice mDevice2 = mService.getShimmer(selectedDeviceAddress);
                    try {
                        mDevice2.stopStreaming();
                    } catch (ShimmerException e) {
                        e.printStackTrace();
                    }
                    sensorsEnabledFragment.buildSensorsList(mDevice2, this, mService.getBluetoothManager());
                    deviceConfigFragment.buildDeviceConfigList(mDevice2, this, mService.getBluetoothManager());
                }
                return true;
            case R.id.data_sync:
                if(selectedDeviceAddress != null) {
                    VerisenseDeviceAndroid mDevice3 = (VerisenseDeviceAndroid)mService.getShimmer(selectedDeviceAddress);
                    String participantName = DataSyncFragment.editTextParticipantName.getText().toString();
                    String trialName = DataSyncFragment.editTextTrialName.getText().toString();
                    String UUID = AndroidBleRadioByteCommunication.convertMacIDtoUUID(selectedDeviceAddress);
                    mDevice3.setTrialName(trialName);
                    mDevice3.setParticipantID(participantName);
                    mDevice3.getMapOfVerisenseProtocolByteCommunication().get(COMMUNICATION_TYPE.BLUETOOTH).setRootPathForBinFile(android.os.Environment.getExternalStorageDirectory().getAbsolutePath());
                    DataSyncFragment.TextViewDirectory.setText("Directory : " + String.format("%s/%s/%s/%s/BinaryFiles", android.os.Environment.getExternalStorageDirectory().getAbsolutePath(), trialName, participantName, UUID));
                    mDevice3.getMapOfVerisenseProtocolByteCommunication().get(COMMUNICATION_TYPE.BLUETOOTH).readLoggedData();
                }
                return true;
            case R.id.disconnect_all_devices:
                mService.disconnectAllDevices();
                connectedShimmersListFragment.buildShimmersConnectedListView(null, getApplicationContext());
                if(mSectionsPagerAdapter1.getCount() == 6)
                {
                    mSectionsPagerAdapter1.remove(3);
                    mSectionsPagerAdapter1.notifyDataSetChanged();
                }
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
            connectedShimmersListFragment.buildShimmersConnectedListView(mService.getListOfConnectedDevices(), getApplicationContext());

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
                String deviceName = data.getStringExtra(EXTRA_DEVICE_NAME);
                //mService.connectShimmer(macAdd,this);    //Connect to the selected device, and set context to show progress dialog when pairing

                mService.connectShimmer(macAdd,deviceName,this);    //Connect to the selected device, and set context to show progress dialog when pairing

            }
        }
    }

    public class SectionsPagerAdapter1 extends FragmentStatePagerAdapter {

        ArrayList<Fragment> fragmentArrayList = new ArrayList<Fragment>();
        ArrayList<String> fragmentTitle = new ArrayList<String>();

        public SectionsPagerAdapter1(FragmentManager fm) {
            super(fm);
            connectedShimmersListFragment = ConnectedShimmersListFragment.newInstance();
            sensorsEnabledFragment = SensorsEnabledFragment.newInstance(null, null);
            deviceConfigFragment = DeviceConfigFragment.newInstance();
            plotFragment = PlotFragment.newInstance();
            signalsToPlotFragment = SignalsToPlotFragment.newInstance();

            add(connectedShimmersListFragment, "Connected Devices");
            add(sensorsEnabledFragment, "Enable Sensors");
            add(deviceConfigFragment, "Device Configuration");
            add(plotFragment, "Plot");
            add(signalsToPlotFragment, "Signals to Plot");
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            if(position < fragmentArrayList.size()){
                if(position == 0){
                    connectedShimmersListFragment.buildShimmersConnectedListView(null, getApplicationContext());
                }
                return fragmentArrayList.get(position);
            }
            else{
                return null;
            }
        }

        @Override
        public int getCount() {
            // Show 5 total pages (Show 6 when a verisense device is selected)
            return fragmentArrayList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position < fragmentTitle.size()){
                return fragmentTitle.get(position);
            }
            return "";
        }

        public void add(Fragment fragment, String title, int index)
        {
            fragmentArrayList.add(index, fragment);
            fragmentTitle.add(index, title);
        }

        public void add(Fragment fragment, String title)
        {
            fragmentArrayList.add(fragment);
            fragmentTitle.add(title);
        }

        public void remove(int index)
        {
            if(fragmentArrayList.size() > 1 && fragmentTitle.size() > 1){
                fragmentArrayList.remove(index);
                fragmentTitle.remove(index);
            }
        }

        @Override
        public int getItemPosition(Object object) {
            // refresh all fragments when data set changed
            return PagerAdapter.POSITION_NONE;
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
                        connectedShimmersListFragment.buildShimmersConnectedListView(mService.getListOfConnectedDevices(), getApplicationContext());
                        if(selectedDeviceAddress != null){
                            ShimmerDevice mDevice = mService.getShimmer(selectedDeviceAddress);
                            deviceConfigFragment.buildDeviceConfigList(mDevice, getApplicationContext(), mService.getBluetoothManager());
                        }
                        if(dataSyncFragment != null){
                            DataSyncFragment.TextViewPayloadIndex.setText("");
                            DataSyncFragment.TextViewSpeed.setText("");
                            DataSyncFragment.TextViewDirectory.setText("");
                        }
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
                        connectedShimmersListFragment.buildShimmersConnectedListView(mService.getListOfConnectedDevices(), getApplicationContext());
                        break;
                    case STREAMING_LOGGED_DATA:
                        Toast.makeText(getApplicationContext(), "Data Sync: " + shimmerName + " " + macAddress, Toast.LENGTH_SHORT).show();
                        break;
                    case DISCONNECTED:
                        Toast.makeText(getApplicationContext(), "Device disconnected: " + shimmerName + " " + macAddress, Toast.LENGTH_SHORT).show();
                        connectedShimmersListFragment.buildShimmersConnectedListView(mService.getListOfConnectedDevices(), getApplicationContext()); //to be safe lets rebuild this
                        break;
                }
            }
            else if(msg.what == Shimmer.MSG_IDENTIFIER_SYNC_PROGRESS){
                SyncProgressDetails mDetails = (SyncProgressDetails)((CallbackObject)msg.obj).mMyObject;
                DataSyncFragment.TextViewPayloadIndex.setText("Current Payload Index : " + Integer.toString(mDetails.mPayloadIndex));
                DataSyncFragment.TextViewSpeed.setText("Speed(KBps) : " + Double.toString(mDetails.mTransferRateBytes/1024));
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

        //add and remove DataSyncFragment based on the type of device
        if(device instanceof VerisenseDeviceAndroid) {
            if(mSectionsPagerAdapter1.getCount() == 5)
            {
                dataSyncFragment = DataSyncFragment.newInstance();
                mSectionsPagerAdapter1.add(dataSyncFragment, "Data Sync", 3);
            }
        }
        else{
            if(mSectionsPagerAdapter1.getCount() == 6)
            {
                mSectionsPagerAdapter1.remove(3);
            }
        }
        mSectionsPagerAdapter1.notifyDataSetChanged();

        sensorsEnabledFragment.setShimmerService(mService);
        sensorsEnabledFragment.buildSensorsList(device, this, mService.getBluetoothManager());

        deviceConfigFragment.buildDeviceConfigList(device, this, mService.getBluetoothManager());

        plotFragment.setShimmerService(mService);
        plotFragment.clearPlot();
        plotFragment.setSelectedDeviceAddress(selectedDeviceAddress);
        dynamicPlot = plotFragment.getDynamicPlot();

        mService.stopStreamingAllDevices();
        signalsToPlotFragment.setDeviceNotStreamingView();
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
