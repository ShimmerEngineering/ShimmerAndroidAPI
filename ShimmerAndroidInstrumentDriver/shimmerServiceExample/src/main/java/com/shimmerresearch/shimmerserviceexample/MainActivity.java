package com.shimmerresearch.shimmerserviceexample;

import android.Manifest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;


import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.android.shimmerService.ShimmerService;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;
import com.shimmerresearch.exceptions.ShimmerException;

import com.shimmerresearch.driver.Configuration.COMMUNICATION_TYPE;
import com.shimmerresearch.tools.FileUtils;
import com.shimmerresearch.verisense.VerisenseDevice;
import com.shimmerresearch.verisense.communication.SyncProgressDetails;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.EXTRA_DEVICE_ADDRESS;
import static com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog.EXTRA_DEVICE_NAME;

public class MainActivity extends AppCompatActivity implements ConnectedShimmersListFragment.OnShimmerDeviceSelectedListener, SensorsEnabledFragment.OnSensorsSelectedListener {

    private static final int PERMISSION_FILE_REQUEST_SHIMMER = 99;
    private static final int PERMISSION_FILE_REQUEST_VERISENSE = 100;

    final static String LOG_TAG = "Shimmer";
    final static String SERVICE_TAG = "ShimmerService";
    final static int REQUEST_CONNECT_SHIMMER = 2;

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
    XYPlot dynamicPlot;
    boolean isServiceStarted = false;

    private SectionsPagerAdapter1 mSectionsPagerAdapter1;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
        boolean permissionGranted = true;
        {
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                permissionGranted = false;
            }
            permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                permissionGranted = false;
            }
            permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                permissionGranted = false;
            }
            permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                permissionGranted = false;
            }
        }
        if (!permissionGranted) {
            // Should we show an explanation?
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION}, 110);

        } else {
            startServiceandBTManager();
        }

        setContentView(R.layout.activity_main);
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
        /*if (!btAdapter.isEnabled()) {
            int REQUEST_ENABLE_BT = 1;
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {*/

        //}


/*
        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE )!= PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_STORAGE);
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ){
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            startServiceandBTManager();
        }
    }

    protected void startServiceandBTManager(){
        BleManager.getInstance().init(getApplication());
        Intent intent = new Intent(this, ShimmerService.class);
        startService(intent);
        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Log.d(LOG_TAG, "Shimmer Service started");
        Toast.makeText(this, "Shimmer Service started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item1 = menu.findItem(R.id.data_sync);
        MenuItem item2 = menu.findItem(R.id.disable_logging);
        MenuItem item3 = menu.findItem(R.id.erase_data);
        if(selectedDeviceAddress != null){
            ShimmerDevice device = mService.getShimmer(selectedDeviceAddress);
            if(device instanceof VerisenseDevice) {
                item1.setVisible(true);
                item2.setVisible(true);
                item3.setVisible(true);
                if (((VerisenseDevice)device).isRecordingEnabled()){
                    item2.setTitle("Disable Logging");
                    return true;
                }
                item2.setTitle("Enable Logging");
                return true;
            }
        }
        item1.setVisible(false);
        item2.setVisible(false);
        item3.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        final Toast otherTaskOngoingToast = Toast.makeText(this, "Please wait until current task is finished", Toast.LENGTH_LONG);
        switch (item.getItemId()) {
            case R.id.connect_device:
                Intent pairedDevicesIntent = new Intent(getApplicationContext(), ShimmerBluetoothDialog.class);
                startActivityForResult(pairedDevicesIntent, REQUEST_CONNECT_SHIMMER);
                return true;
            case R.id.start_streaming:
                if(selectedDeviceAddress != null) {

                    ShimmerDevice mDevice = mService.getShimmer(selectedDeviceAddress);
                    try {
                        mDevice.startStreaming();
                        mViewPager.setCurrentItem(mDevice instanceof VerisenseDevice ? 5 : 4);
                    } catch (ShimmerException e) {
                        if(e.getMessage() == "A task is still ongoing"){
                            otherTaskOngoingToast.show();
                        }

                        e.printStackTrace();
                    }
                    signalsToPlotFragment.buildSignalsToPlotList(this, mService, selectedDeviceAddress, dynamicPlot);
                }
                return true;
            case R.id.stop_streaming:
                if(selectedDeviceAddress != null) {
                    ShimmerDevice mDevice = mService.getShimmer(selectedDeviceAddress);
                    try {
                        mDevice.stopStreaming();
                    } catch (ShimmerException e) {
                        if(e.getMessage() == "A task is still ongoing"){
                            otherTaskOngoingToast.show();
                        }
                        e.printStackTrace();
                    }
                    sensorsEnabledFragment.buildSensorsList(mDevice, this, mService.getBluetoothManager());
                    deviceConfigFragment.buildDeviceConfigList(mDevice, this, mService.getBluetoothManager());
                }
                return true;
            case R.id.data_sync:
                if(selectedDeviceAddress != null) {
                    mViewPager.setCurrentItem(5);
                }
                return true;
            case R.id.disable_logging:
                if(selectedDeviceAddress != null) {
                    VerisenseDevice mDevice = (VerisenseDevice)mService.getShimmer(selectedDeviceAddress);
                    VerisenseDevice mDeviceClone = mDevice.deepClone();
                    boolean logging = !mDevice.isRecordingEnabled();
                    mDeviceClone.setRecordingEnabled(logging);
                    byte[] opConfig = mDeviceClone.configBytesGenerate(true, COMMUNICATION_TYPE.BLUETOOTH);
                    try {
                        mDevice.getMapOfVerisenseProtocolByteCommunication().get(COMMUNICATION_TYPE.BLUETOOTH).writeAndReadOperationalConfig(opConfig);
                        Toast.makeText(this, logging?"Logging Enabled":"Logging Disabled", Toast.LENGTH_SHORT).show();
                    } catch (ShimmerException e) {
                        if(e.getMessage() == "A task is still ongoing"){
                            otherTaskOngoingToast.show();
                        }
                        e.printStackTrace();
                    }
                }
                return true;
            case R.id.erase_data:
                if(selectedDeviceAddress != null) {
                    final VerisenseDevice mDevice = (VerisenseDevice)mService.getShimmer(selectedDeviceAddress);
                    final ProgressDialog progress = new ProgressDialog(this);
                    progress.setTitle("Erasing data");
                    progress.setMessage("Please wait for the operation to complete...");
                    progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                    progress.show();
                    new Thread(){
                        public void run(){
                            try {
                                mDevice.getMapOfVerisenseProtocolByteCommunication().get(COMMUNICATION_TYPE.BLUETOOTH).eraseDataTask().waitForCompletion(60, TimeUnit.SECONDS);
                                progress.dismiss();
                            } catch (ShimmerException | InterruptedException e) {
                                progress.dismiss();
                                if(e.getMessage() == "A task is still ongoing"){
                                    otherTaskOngoingToast.show();
                                }
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
                return true;
            case R.id.disconnect_all_devices:
                mService.disconnectAllDevices();
                connectedShimmersListFragment.buildShimmersConnectedListView(null, getApplicationContext());
                mViewPager.setCurrentItem(0);
                selectedDeviceAddress = null;
                selectedDeviceName = null;
                connectedShimmersListFragment.removeSelectedDevice();
                return true;
            case R.id.enable_write_to_csv:
                Intent intent =new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

                startActivityForResult(intent, PERMISSION_FILE_REQUEST_SHIMMER);

                mService.setEnableLogging(true);
                return true;
            case R.id.disable_write_to_csv:
                mService.setEnableLogging(false);
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

            if (resultCode == RESULT_OK && requestCode == PERMISSION_FILE_REQUEST_VERISENSE) {
                if (data != null) {
                    Uri treeUri = data.getData();
                    VerisenseDevice mDevice = (VerisenseDevice) mService.getShimmer(selectedDeviceAddress);
                    mDevice.setTrialName(dataSyncFragment.editTextTrialName.getText().toString());
                    mDevice.setParticipantID(dataSyncFragment.editTextParticipantName.getText().toString());
                    FileUtils futils = new FileUtils(MainActivity.this);
                    File file = new File(futils.getPath(treeUri, FileUtils.UriType.FOLDER));
                    mDevice.getMapOfVerisenseProtocolByteCommunication().get(COMMUNICATION_TYPE.BLUETOOTH).setRootPathForBinFile(file.getAbsolutePath());
                    try {
                        mDevice.getMapOfVerisenseProtocolByteCommunication().get(COMMUNICATION_TYPE.BLUETOOTH).readLoggedData();
                    } catch (Exception e) {
                        if (e.getMessage() == "A task is still ongoing") {
                            //otherTaskOngoingToast.show();
                        }
                        e.printStackTrace();
                    }
                }
            }

            if (resultCode == RESULT_OK && requestCode == PERMISSION_FILE_REQUEST_SHIMMER) {
                if (data != null) {
                    Uri treeUri = data.getData();
                    mService.mFileURI = treeUri;
                    mService.mResolver = getContentResolver();
                    mService.mContext = this;





                    /*
                    DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);
                    DocumentFile newF = pickedDir.createFile("text/comma-separated-values", "newshimmer.csv");
                    try {
                        OutputStream outputStream = getContentResolver().openOutputStream(newF.getUri());
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                        writer.write("test");
                        writer.close();
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    */
                }
            }
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

                showBtTypeConnectionOption();

                //mService.connectShimmer(macAdd,this);    //Connect to the selected device, and set context to show progress dialog when pairing

                mService.connectShimmer(macAdd,deviceName,preferredBtType,this);    //Connect to the selected device, and set context to show progress dialog when pairing

            }
        }
    }

    public class SectionsPagerAdapter1 extends FragmentStatePagerAdapter {

        final static int VERISENSE_PAGE_COUNT = 6;
        final static int SHIMMER3_PAGE_COUNT = 5;
        ArrayList<Fragment> fragmentArrayList = new ArrayList<Fragment>();
        ArrayList<String> fragmentTitle = new ArrayList<String>();

        public SectionsPagerAdapter1(FragmentManager fm) {
            super(fm);
            dataSyncFragment = DataSyncFragment.newInstance();
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
            add(dataSyncFragment, "Verisense Sync");
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
    ShimmerBluetoothManagerAndroid.BT_TYPE preferredBtType;
    Looper looper = Looper.myLooper();

    public void showBtTypeConnectionOption(){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setCancelable(false);
        alertDialog.setMessage("Choose preferred Bluetooth type");
        alertDialog.setButton( Dialog.BUTTON_POSITIVE, "BT CLASSIC", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                preferredBtType = ShimmerBluetoothManagerAndroid.BT_TYPE.BT_CLASSIC;
                looper.quit();
            };
        });
        alertDialog.setButton( Dialog.BUTTON_NEGATIVE, "BLE", new DialogInterface.OnClickListener()    {
            public void onClick(DialogInterface dialog, int which) {
                preferredBtType = ShimmerBluetoothManagerAndroid.BT_TYPE.BLE;
                looper.quit();
            };
        });
        alertDialog.show();
        try{ looper.loop(); }
        catch(RuntimeException e){}
    }

    int mNumberOfCurrentlyConnectedDevices=0;
    public boolean isNumberOfConnectedDevicesChanged(){
        if(mService.getListOfConnectedDevices().size()!=mNumberOfCurrentlyConnectedDevices){
            mNumberOfCurrentlyConnectedDevices = mService.getListOfConnectedDevices().size();
            return true;
        }
        return false;
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
                        if(isNumberOfConnectedDevicesChanged()) {
                            connectedShimmersListFragment.buildShimmersConnectedListView(mService.getListOfConnectedDevices(), getApplicationContext());
                            if (selectedDeviceAddress != null) {
                                ShimmerDevice mDevice = mService.getShimmer(selectedDeviceAddress);
                                deviceConfigFragment.buildDeviceConfigList(mDevice, getApplicationContext(), mService.getBluetoothManager());
                            }
                        }
                        if(dataSyncFragment != null){
                            dataSyncFragment.TextViewPayloadIndex.setText("");
                            dataSyncFragment.TextViewSpeed.setText("");
                            dataSyncFragment.editTextTrialName.setEnabled(true);
                            dataSyncFragment.editTextParticipantName.setEnabled(true);
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
                        dataSyncFragment.editTextTrialName.setEnabled(false);
                        dataSyncFragment.editTextParticipantName.setEnabled(false);
                        break;
                    case DISCONNECTED:
                        isNumberOfConnectedDevicesChanged();
                        Toast.makeText(getApplicationContext(), "Device disconnected: " + shimmerName + " " + macAddress, Toast.LENGTH_SHORT).show();
                        connectedShimmersListFragment.buildShimmersConnectedListView(mService.getListOfConnectedDevices(), getApplicationContext()); //to be safe lets rebuild this
                        break;
                }
            }
            else if(msg.what == Shimmer.MSG_IDENTIFIER_SYNC_PROGRESS){
                SyncProgressDetails mDetails = (SyncProgressDetails)((CallbackObject)msg.obj).mMyObject;
                dataSyncFragment.TextViewPayloadIndex.setText("Current Payload Index : " + Integer.toString(mDetails.mPayloadIndex));
                dataSyncFragment.TextViewSpeed.setText("Speed(KBps) : " + String.format("%.2f", mDetails.mTransferRateBytes/1024));
                dataSyncFragment.TextViewDirectory.setText("Bin file path : " + mDetails.mBinFilePath);
            }
            else if(msg.what == Shimmer.MSG_IDENTIFIER_NOTIFICATION_MESSAGE){
                if(((CallbackObject)msg.obj).mIndicator == Shimmer.NOTIFICATION_SHIMMER_FULLY_INITIALIZED){
                    Toast.makeText(getApplicationContext(), "Device fully initialized: ", Toast.LENGTH_SHORT).show();
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
    public void onShimmerDeviceSelected(String macAddress, String deviceName, Boolean selected) {
        if(selected){
            Toast.makeText(this, "Selected Device: " + deviceName + "\n" + macAddress, Toast.LENGTH_SHORT).show();
            selectedDeviceAddress = macAddress;
            selectedDeviceName = deviceName;

            //Pass the selected device to the fragments
            ShimmerDevice device = mService.getShimmer(selectedDeviceAddress);

            //add and remove DataSyncFragment based on the type of device
            /*
            if(device instanceof VerisenseDeviceAndroid) {
                if(mSectionsPagerAdapter1.getCount() == mSectionsPagerAdapter1.SHIMMER3_PAGE_COUNT)
                {
                    dataSyncFragment = DataSyncFragment.newInstance();
                    mSectionsPagerAdapter1.add(dataSyncFragment, "Data Sync", 3);
                }
            }
            else{
                if(mSectionsPagerAdapter1.getCount() == mSectionsPagerAdapter1.VERISENSE_PAGE_COUNT)
                {
                    mSectionsPagerAdapter1.remove(3);
                }
            }*/

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

            if (device instanceof VerisenseDevice) {
                if (dataSyncFragment.editTextParticipantName.getText().toString().isEmpty()) {
                    dataSyncFragment.editTextParticipantName.setText("Default Participant");
                }
                if (dataSyncFragment.editTextTrialName.getText().toString().isEmpty()) {
                    dataSyncFragment.editTextTrialName.setText("Default trial");
                }

                dataSyncFragment.ButtonDataSync.setVisibility(View.VISIBLE);
                dataSyncFragment.ButtonDataSync.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

                        startActivityForResult(intent, PERMISSION_FILE_REQUEST_VERISENSE);
                    }
                });
            }


        }
        else{
            mSectionsPagerAdapter1.notifyDataSetChanged();
        }
    }

    @Override
    public void onSensorsSelected() {
        ShimmerDevice device = mService.getShimmer(selectedDeviceAddress);
        deviceConfigFragment.buildDeviceConfigList(device, this, mService.getBluetoothManager());
    }

    @Override
    protected void onResume() {
        super.onResume();
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(this, ShimmerService.class);
            getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

}
