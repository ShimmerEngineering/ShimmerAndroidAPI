package com.shimmerresearch.shimmerserviceexample;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.renderscript.ScriptGroup;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog;
import com.shimmerresearch.android.guiUtilities.ShimmerDialogConfigurations;
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid;
import com.shimmerresearch.android.shimmerService.ShimmerService;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerDevice;

import java.util.List;

public class MainActivity extends AppCompatActivity implements Handler.Callback {

    ShimmerDialogConfigurations dialog;
    BluetoothAdapter btAdapter;
    ShimmerService mService;

    //Drawer stuff
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;
    public String selectedDeviceAdd;


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

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

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.content_frame);
        mActivityTitle = getTitle().toString();

        String[] startArray = {""};
        addDrawerItems(startArray);
        setupDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        dialog = new ShimmerDialogConfigurations();
    }

    private void addDrawerItems(String[] stringArray) {
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stringArray);
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String viewText = (String) mDrawerList.getItemAtPosition(position);
                if (viewText.contains("\n")) {
                    selectedDeviceAdd = viewText.substring(viewText.indexOf("\n"));
                    Toast.makeText(MainActivity.this, "Selected device: " + viewText, Toast.LENGTH_SHORT).show();
                }
                //Highlight the selected device
                view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_orange_light));
                //Set all other backgrounds to white (clearing previous highlight, if any)
                for (int i = 0; i < mDrawerList.getAdapter().getCount(); i++) {
                    if (i != position) {
                        View v = mDrawerList.getChildAt(i);
                        v.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.white));
                        Log.e(SERVICE_TAG, "Cleared background color...");
                    }
                }
            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                String[] list = getStringListOfDevicesConnected();
                if (list != null) {
                    addDrawerItems(list);
                }
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Connected Shimmers");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
    }

    private String[] getStringListOfDevicesConnected() {
        List<ShimmerDevice> deviceList = mService.getListOfConnectedDevices();
        if (deviceList != null) {
            String[] nameList = new String[deviceList.size()];
            for (int i = 0; i < deviceList.size(); i++) {
                ShimmerDevice device = deviceList.get(i);
                nameList[i] = device.getShimmerUserAssignedName() + "\n" + device.getMacId();
            }
            return nameList;
        } else {
            Log.w(SERVICE_TAG, "Error! No Shimmers connected. Cannot retrieve List of devices");
            return null;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
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
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.paired_devices:
                Intent serverIntent = new Intent(getApplicationContext(), ShimmerBluetoothDialog.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_SHIMMER);
                return true;
            case R.id.test_button:
                ShimmerBluetoothManagerAndroid btManager2 = mService.getBluetoothManager();
                btManager2.startStreamingAllDevices();
                return true;
            case R.id.connect_shimmer:
                if (isServiceStarted) {
                    mService.connectShimmer("00:06:66:66:96:86");
                } else {
                    Toast.makeText(this, "ERROR! Service not started.", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.add_handler:
                ShimmerBluetoothManagerAndroid btManager = mService.getBluetoothManager();
                btManager.addHandler(mHandler);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        //Check if Bluetooth is enabled
        if (!btAdapter.isEnabled()) {
            int REQUEST_ENABLE_BT = 1;
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            //Start the Shimmer service
            Intent intent = new Intent(this, ShimmerService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            Log.d(LOG_TAG, "Shimmer Service started");
            Toast.makeText(this, "Shimmer Service started", Toast.LENGTH_SHORT).show();
        }
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        //Stop the Shimmer service
        Intent intent = new Intent(this, ShimmerService.class);
        stopService(intent);
        Log.d(LOG_TAG, "Shimmer Service stopped");
        Toast.makeText(this, "Shimmer Service stopped", Toast.LENGTH_SHORT).show();
        super.onDestroy();
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

            // Tell the user about this for our demo.
            Log.d(SERVICE_TAG, "Shimmer Service Bound");
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
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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
                mService.connectShimmer(macAdd);    //Connect to the selected device
            }
        }
    }

    boolean checkBtEnabled() {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!btAdapter.isEnabled()) {
            return false;
        }
        return true;
    }

    void testButton(View view) {
        ShimmerDevice sDevice = mService.getShimmer("00:06:66:66:96:86");
        dialog.buildShimmersConnectedList(mService.getBluetoothManager().getListOfConnectedDevices(), this);
        //dialog.buildShimmerConfigOptions(sDevice, this);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 2) {
                return PlotFragment.newInstance("Hi", "Hello");
            } else if (position == 0) {
                return EnabledSensorsFragment.newInstance();
            } else {
                return DeviceConfigFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Sensors";
                case 1:
                    return "Device Configuration";
                case 2:
                    return "Plot";
            }
            return null;
        }
    }

    public boolean handleMessage(Message msg) {
        Toast.makeText(this, "Message received", Toast.LENGTH_SHORT).show();
        return true;
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
                        Toast.makeText(getApplicationContext(), "Device connected: " + shimmerName + " " + macAddress, Toast.LENGTH_SHORT).show();
                        break;
                    case CONNECTING:
                        break;
                    case STREAMING:
                        Toast.makeText(getApplicationContext(), "Device streaming: " + shimmerName + " " + macAddress, Toast.LENGTH_SHORT).show();
                        break;
                    case STREAMING_AND_SDLOGGING:
                        break;
                    case SDLOGGING:
                        break;
                    case DISCONNECTED:
                        Toast.makeText(getApplicationContext(), "Device disconnected: " + shimmerName + " " + macAddress, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };


}
