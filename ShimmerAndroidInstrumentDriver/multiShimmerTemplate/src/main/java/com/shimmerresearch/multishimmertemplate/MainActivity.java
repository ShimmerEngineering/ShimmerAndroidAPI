package com.shimmerresearch.multishimmertemplate;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.Configuration;

import com.shimmerresearch.adapters.NavDrawerListAdapter;
import com.shimmerresearch.database.DatabaseHandler;
import com.shimmerresearch.model.NavDrawerItem;
import com.shimmerresearch.service.MultiShimmerTemplateService;
import com.shimmerresearch.service.MultiShimmerTemplateService.LocalBinder;
import com.shimmerresearch.tools.Logging;

public class MainActivity extends Activity{
	
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    
    private CharSequence mDrawerTitle;	// nav drawer title
    private CharSequence mTitle;    	// used to store app title
    // slide menu items
 	private TypedArray navMenuIcons;
    private String[] mOptionsMenu;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private static NavDrawerListAdapter adapter;
    Dialog dialog;
    public static MultiShimmerTemplateService mService;
	boolean mServiceBind=false;
	protected boolean mServiceFirstTime=true;
	private SharedPreferences sp; 
	private Editor e;
	private boolean isFirstStart;
	protected String tempConfigurationName="Temp";
	DatabaseHandler db;
	public static final String ARG_ITEM_ID = "item_id";
	public static boolean mWrite=false;
	String nameFile="";
	AlertDialog.Builder writeNameFileDialog;
	private List<String> connectedShimmers = new ArrayList<String>();
	private List<String> streamingShimmers = new ArrayList<String>();
	private static HashMap<String,Logging> mLog;
	private Menu mMenu;
	private static int fragmentPosition;
	Dialog menuWriteDialog;
	Button writeDataButton, setFileNameButton;
	TextView text;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        com.shimmerresearch.driver.Configuration.setTooLegacyObjectClusterSensorNames();
        Intent intent=new Intent(this, MultiShimmerTemplateService.class);
       	startService(intent);
        
       	sp= getApplicationContext().getSharedPreferences("yoursharedprefs", 0);
       	isFirstStart = sp.getBoolean("key", true); 
       	dialog = new Dialog(this);
        mTitle = mDrawerTitle = getTitle();
        mOptionsMenu = getResources().getStringArray(R.array.options_menu);
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        nameFile="msttest";
        menuWriteDialog = new Dialog(this);
        text = new TextView(this);
        text.setText("File already exist in file system. Would you like to overwrite it?");
        
        
        navDrawerItems = new ArrayList<NavDrawerItem>();
        setNavDrawerItems();
        
     // set up the drawer's list view with items and click listener
//        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
//                R.layout.drawer_list_item, mOptionsMenu));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
        adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
		mDrawerList.setAdapter(adapter);
        
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true); 
        
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
	        	public void onDrawerClosed(View view) {
	                getActionBar().setTitle(mTitle);
	                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            }
	
	            public void onDrawerOpened(View drawerView) {
	                getActionBar().setTitle(mDrawerTitle);
	                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            }
	        };
	   mDrawerLayout.setDrawerListener(mDrawerToggle);

	        if (savedInstanceState == null) {
	            displayView(0);
	            if(isFirstStart){
					mDrawerLayout.openDrawer(mDrawerList);
					e = sp.edit(); 
	                // we save the value "false", indicating that it is no longer the first appstart
	                e.putBoolean("key", false);
	                e.commit();
				}
	        }
	        
	  writeNameFileDialog = new AlertDialog.Builder(this).setTitle("No file name")
	        		.setMessage("Set the name file in order to write the data.").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    				
	    				@Override
	    				public void onClick(DialogInterface dialog, int which) {
	    					// TODO Auto-generated method stub
	    					dialog.dismiss();
	    				}
	    			});
	  
	   
    }
    
    public MultiShimmerTemplateService getService(){
		return mService;
	}
	
	public void test(){
		Log.d("ShimmerTest","test");
	}
	
	
	protected ServiceConnection mTestServiceConnection = new ServiceConnection() {

      	public void onServiceConnected(ComponentName arg0, IBinder service) {
      		// TODO Auto-generated method stub
      		Log.d("ShimmerService", "service connected");
      		LocalBinder binder = (com.shimmerresearch.service.MultiShimmerTemplateService.LocalBinder) service;
      		mService = binder.getService();
      		db=mService.mDataBase;
      		mServiceBind = true;
      		//update the view
      		
      		// this is needed because sometimes when there is an actitivity switch the service is not connecte yet, before the fragment was created, thus the fragment has no access to the service
        	ConfigurationFragment configF = (ConfigurationFragment)getFragmentManager().findFragmentByTag("Configure");
        	PlotFragment plotF = (PlotFragment)getFragmentManager().findFragmentByTag("Plot");
        	DevicesFragment deviceF = (DevicesFragment)getFragmentManager().findFragmentByTag("Devices");
        	

        	if (configF!=null){
    			configF.mService = mService;
    			configF.setup();
    		} else if (plotF!=null){
    			plotF.mService = mService;
    			plotF.setup();
    		} else if (deviceF!=null){
    			deviceF.mService = mService;
    			deviceF.setup();
    		} else {
    			
    		}
      		
      		
      		}

      	public void onServiceDisconnected(ComponentName arg0) {
      		// TODO Auto-generated method stub
      		mServiceBind = false;
      	}
    };
    
    protected boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.shimmerresearch.service.MultiShimmerTemplateService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
      
    public void onPause(){
    	  super.onPause();
    	  if(mServiceBind == true){
    		  getApplicationContext().unbindService(mTestServiceConnection);
    	  }
	}
      
	public void onResume(){
		super.onResume();
	  	Intent intent=new Intent(this, MultiShimmerTemplateService.class);
	  	Log.d("ShimmerH","on Resume");
	  	getApplicationContext().bindService(intent,mTestServiceConnection, Context.BIND_AUTO_CREATE);
	  	if (isMyServiceRunning())
        {
        	Log.d("ShimmerH","Started");
        	
        } else {
        	Log.d("ShimmerH","Not Started");
        }
	  	
	}
	
	private void setNavDrawerItems() {
		
		navDrawerItems.clear();
		navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
		
		// adding nav drawer items to array	
		// Devices
		navDrawerItems.add(new NavDrawerItem(mOptionsMenu[0], navMenuIcons.getResourceId(0, -1)));
		// plot
		navDrawerItems.add(new NavDrawerItem(mOptionsMenu[1], navMenuIcons.getResourceId(1, -1)));
		// Blank
		navDrawerItems.add(new NavDrawerItem(mOptionsMenu[2], navMenuIcons.getResourceId(2, -1)));
		
		mDrawerList.setAdapter(adapter);	
	}
	
	
	private Handler mHandler = new Handler() {
    	
		public void handleMessage(Message msg) {
            switch (msg.what) {
	            case MultiShimmerTemplateService.MESSAGE_WRITING_STOPED:
	            	mWrite=false;
	                MenuItem item = mMenu.getItem(0);
	        		item.setIcon(R.drawable.ic_action_write);
	        		mService.enableWritingHandler(false);
	        		mService.closeAndRemoveFiles();
	        		Toast.makeText(getApplicationContext(), "Files save successfully. You can find them in the MultiShimmerTemplate folder", Toast.LENGTH_LONG).show();
	            break;
            }
		}
    };
	
    
    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            displayView(position);
        }
    }
    
    
    /**
	 * Diplaying fragment view for selected nav drawer list item
	 * */
	private void displayView(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		String tag=null;
		switch (position) {
		case 0:
			fragment = new DevicesFragment();
			tag = "Devices";
			break;
		case 1:
			fragment = new PlotFragment();
			tag = "Plot";
			break;
		case 2:
			fragment = new BlankFragment();
			tag = "Blank";
			break;
		default:
			fragment = new DevicesFragment(); 
			tag = "Devices";
		break;

		}

		if (fragment != null) {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, tag).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			setTitle(mOptionsMenu[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
			fragmentPosition=position;
		} 
	}
	
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	mMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        MenuItem item = menu.getItem(0);
        if(!mWrite){
        	item.setIcon(R.drawable.ic_action_write);
        }
        else{
        	item.setIcon(R.drawable.ic_action_stop_write);
        }
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        if(drawerOpen)
        	setTitle(mDrawerTitle);
        else
        	setTitle(mOptionsMenu[fragmentPosition]);
        
        return super.onCreateOptionsMenu(menu);
    }
    
    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.help).setVisible(!drawerOpen);
        menu.findItem(R.id.write).setVisible(!drawerOpen);
        
        return super.onPrepareOptionsMenu(menu);
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {  
    	case R.id.help:
    		PlotFragment plotFragment = (PlotFragment)getFragmentManager().findFragmentByTag("Plot");
    		if (plotFragment!=null){
    			showTextDialog("To plot the calculated HR from the PPG, the Internal Exp Power should be enabled within configuration panel. " +
    						"You also must enable the HR calculation option. The signal can be got from Int ADC 1, 12, 13 or 14 so the sensor desired must be enabled. " +
    						"Note that performance at high sampling rate varies across different Android operating systems. " +
    						" Once the device starts streaming the calculated Heart Rate value will show after the training duration (it can take 20 seconds)."
    						,"PPG to Heart Rate Conversion");
    		}
    		else
    			showTextDialog("The floppy disk icon offers the possibily to write the streaming data to a file. One file for each device is created.\n The data will be save in the MultiShimmerTemplate folder with either the default name 'Default' or the name set by the user.","Write Data To File Instructions");
    		
            return true;
    	case R.id.write:
    		if(mWrite){
    			mWrite=false;
        		item.setIcon(R.drawable.ic_action_write);
        		mService.enableWritingHandler(false);
        		mService.closeAndRemoveFiles();
        		Toast.makeText(getApplicationContext(), "Files save successfully. You can find them in the MultiShimmerTemplate folder", Toast.LENGTH_LONG).show();
    		}
    		else{
    			connectedShimmers = DevicesFragment.connectedShimmerAddresses;
          		streamingShimmers = DevicesFragment.streamingShimmerAddresses;
    			
    				if (connectedShimmers.size()>0) {
						if (streamingShimmers.contains(connectedShimmers.get(0))) {
							showWriteDataDialog();
						} else {
							mWrite=false;
							Toast.makeText(getApplicationContext(), "Connected Device Is Not Streaming", Toast.LENGTH_LONG).show();
							mService.enableWritingHandler(false);
						}
					} else {
						mWrite=false;
						Toast.makeText(getApplicationContext(), "No Device Connected", Toast.LENGTH_LONG).show();
						mService.enableWritingHandler(false);
					}
    			
    		}
    		
    	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    
    @Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}
    
    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
   

    
    public void showWriteDataDialog(){
    	
    	menuWriteDialog.setContentView(R.layout.write_data_menu);
		menuWriteDialog.setTitle("Write Data To File");
		menuWriteDialog.setCancelable(true);
		
        writeDataButton = (Button) menuWriteDialog.findViewById(R.id.buttonStartWriteData);
        setFileNameButton = (Button) menuWriteDialog.findViewById(R.id.buttonSetFileName);
          
		writeDataButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
						mService.setWriteHandler(mHandler);
						mWrite=true;
						MenuItem item = mMenu.getItem(0);
						item.setIcon(R.drawable.ic_action_stop_write);
						mService.enableWritingHandler(true);
						menuWriteDialog.dismiss();
						Toast.makeText(getApplicationContext(), "Writing data...", Toast.LENGTH_SHORT).show();
    			
			}
		});
		
		
		setFileNameButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showSetPathDialog();
			}
		});
		
		menuWriteDialog.show();
    }
	  
	
	  public void showSetPathDialog(){
		dialog.setContentView(R.layout.save_dialog);
		dialog.setTitle("Set the file to write:");
	     final EditText et = (EditText) dialog.findViewById(R.id.editTextConfigName);
	     et.setText(mService.mLogFileName);
	     
		  Button buttonSave = (Button) dialog.findViewById(R.id.buttonSave);
	      
		  buttonSave.setOnClickListener(new View.OnClickListener(){
	
			@Override
			public void onClick(View arg0) {
				
				mService.mLogFileName = et.getText().toString();
				Toast.makeText(getApplicationContext(), "New file name: "+mService.mLogFileName, Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
			  
		  });
		  
		dialog.setCancelable(true);
		dialog.show();
	    
	}
	
	
	public void showTextDialog(String text,String title){
		dialog.setContentView(R.layout.text_dialog);
		dialog.setTitle(title);
		dialog.setCancelable(true);
		dialog.show();
		TextView TVNew = (TextView) dialog.findViewById(R.id.textViewDialog);
		TVNew.setText(text);
	}
	
	public void showReplaceDialog(String text){
		dialog.setContentView(R.layout.log_popup);
	    dialog.setTitle("Replace File?");
	       
	    TextView tv = (TextView) dialog.findViewById(R.id.textViewDialog);
	    tv.setText(text);
	       
	    Button buttonYes = (Button) dialog.findViewById(R.id.ButtonYes);
	    Button buttonNo = (Button) dialog.findViewById(R.id.ButtonNo);
	    buttonYes.setOnClickListener(new OnClickListener(){
	    	@Override
	    	public void onClick(View arg0) {
	    		mWrite=true;
	    		mService.enableWritingHandler(true);
	    		MenuItem item = mMenu.getItem(0);
	            item.setIcon(R.drawable.ic_action_stop_write);
	    		dialog.dismiss();
	    		menuWriteDialog.dismiss();
	    	}
	    });
	       
	    buttonNo.setOnClickListener(new OnClickListener(){
	    	@Override
	    	public void onClick(View arg0) {
	    		mWrite=false;
	    		mService.enableWritingHandler(false);
	    		dialog.dismiss();
	    	}
	     });

	     dialog.setCancelable(true);
	     dialog.show();
	    }
    
}
    

