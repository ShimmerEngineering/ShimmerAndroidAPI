package com.shimmerresearch.calibrate;

/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Ruaidhri Molloy
 * @date  May, 2014
 */

/**
 * This Activity sets up the navigation drawer and is used to direct the app to different fragments
 */

import java.util.ArrayList;

import com.shimmerresearch.adapter.NavDrawerListAdapter;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.slidingmenu.R;
import com.shimmerresearch.model.NavDrawerItem;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private static NavDrawerListAdapter adapter;
	
	private int navDrawPosition=0;
	
	public static Shimmer mShimmerDevice;
	public static Handler fragmentHandler;
	
	private SharedPreferences sp; 
	private Context context;
	private boolean isFirstStart;
	private Editor e;
	
	private boolean active = false;
	private boolean onBackPressed = false;
	
	private Dialog dialog;
	private RelativeLayout relativeLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		com.shimmerresearch.driver.Configuration.setTooLegacyObjectClusterSensorNames();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		context = getApplicationContext();
		sp= context.getSharedPreferences("yoursharedprefs", 0);
		isFirstStart = sp.getBoolean("key", true); 
		
		mTitle = mDrawerTitle = getTitle();
		dialog = new Dialog(this, R.style.myBackgroundStyle);
		//dialog = new Dialog(this, R.style.myBackgroundStyle);
		// load slide menu items
		
		if(mShimmerDevice!=null){
			if(mShimmerDevice.getShimmerVersion()==3) navMenuTitles = getResources().getStringArray(R.array.shimmer3_nav_drawer_items);
			else navMenuTitles = getResources().getStringArray(R.array.shimmer2r_nav_drawer_items);
		}
		else navMenuTitles = getResources().getStringArray(R.array.shimmer2r_nav_drawer_items);

		// nav drawer icons from resources
		navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		navDrawerItems = new ArrayList<NavDrawerItem>();
		setNavDrawerItems();
		
		// Recycle the typed array
		navMenuIcons.recycle();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
		mDrawerList.setAdapter(adapter);
		
		// enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, //nav menu toggle icon
				R.string.app_name, // nav drawer open - description for accessibility
				R.string.app_name // nav drawer close - description for accessibility
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				setNavDrawerItems();
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}

		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			// on first time display view for first nav item
			displayView(0);
			if(isFirstStart){
				mDrawerLayout.openDrawer(mDrawerList);
				e = sp.edit(); 
                // we save the value "false", indicating that it is no longer the first appstart
                e.putBoolean("key", false);
                e.commit();
			}
		}
		
		mShimmerDevice = new Shimmer(null, myHandler, "Shimmer", 51.2, 0, 4,  Shimmer.SENSOR_ACCEL | Shimmer.SENSOR_GYRO | Shimmer.SENSOR_MAG | Shimmer.SENSOR_DACCEL,false, false, false, false, 1, 1); // Member object for communication services	
	}
	
	public void onBackPressed() {
		onBackPressed=true;
		onPause();
    }  
	
	public void onPause() {
		super.onPause();

		if(onBackPressed){
			if (mShimmerDevice != null) mShimmerDevice.stop();
			finish();
		}
	}
	
	private void setNavDrawerItems() {
		
		if(mShimmerDevice!=null){
			if(mShimmerDevice.getShimmerVersion()==3) navMenuTitles = getResources().getStringArray(R.array.shimmer3_nav_drawer_items);
			else navMenuTitles = getResources().getStringArray(R.array.shimmer2r_nav_drawer_items);
		}
		else navMenuTitles = getResources().getStringArray(R.array.shimmer2r_nav_drawer_items);
		
		navDrawerItems.clear();
		navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
		
		// adding nav drawer items to array	
		
		// Home
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
		// Accel
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
		// Gyro
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
		// Mag
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
		
		if(mShimmerDevice!=null){
			if(mShimmerDevice.getShimmerVersion()==3){ 
				navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
				// If Shimmer3 add an extra navDrawerItem, one for Wide Range Accel
			}
		}
		
		// test fragment, to be removed/commented out before app release
		//navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1)));
		
		mDrawerList.setAdapter(adapter);	
	}
		
	private static Handler myHandler = new Handler(){
		
        @Override
        public void handleMessage(Message msg) {
            fragmentHandler.handleMessage(msg);
            switch (msg.what) {
            
            case Shimmer.MESSAGE_STATE_CHANGE:
            	
            	switch (msg.arg1) {/*
                case Shimmer.STATE_CONNECTED:
                    break;
                case Shimmer.STATE_CONNECTING:
                    break;
                case Shimmer.STATE_NONE:
                    // this also stops streaming
                    break;
                case Shimmer.MSG_STATE_FULLY_INITIALIZED:
                    break;
                case Shimmer.MSG_STATE_STREAMING:
                    break;
                case Shimmer.MSG_STATE_STOP_STREAMING:
                	break;
                default:
            		break;*/
                }
            }
            
        }  
    };
 
	public void setHandler(Handler handler){
		fragmentHandler=handler;
	}
	
	/**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id){
			// display view for selected nav drawer item
			navDrawPosition = position;
			displayView(position);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		int itemId = item.getItemId();
		
		if (itemId == R.id.manual) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(getResources().getString(R.string.manual_link)));
			startActivity(intent);
			return true;
		} 
		
		else if (itemId == R.id.about){
	    	dialog.setContentView(R.layout.about_dialog);
	    	relativeLayout = (RelativeLayout) dialog.findViewById(R.id.rlLayout);
	    	dialog.setCancelable(true);
	    	dialog.setTitle("ABOUT");
	    	dialog.getWindow().setTitleColor(getResources().getColor(R.color.app_background_color));
	    	dialog.getWindow().setBackgroundDrawableResource(R.color.dialog_background_color);
	    	
	    	relativeLayout.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					dialog.cancel();
				}
			});

	    	dialog.show();
			return true;
		}
		
		else {
			return super.onOptionsItemSelected(item);
		}
	}

	/* *
	 * Called when invalidateOptionsMenu() is triggered
	 */

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		//menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}
	

	public int getNavDrawerPosition(){
		return navDrawPosition;
	}

	/**
	 * Diplaying fragment view for selected nav drawer list item
	 * */
	private void displayView(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		switch (position) {
		case 0:
			fragment = new HomeFragment();
			active = false;
			break;
		case 4:
			if(mShimmerDevice!=null){
				if(mShimmerDevice.getShimmerVersion()==3){
					fragment = new CalibrateFragment();
					active = true;
				}
				else fragment = new TestFragment();
			}
			else fragment = new TestFragment();
			break;
		case 5:
			fragment = new TestFragment();
			break;	
		default:
			fragment = new CalibrateFragment();
			active = true;
			break;
		}

		if (fragment != null) {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			setTitle(navMenuTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
			
		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
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
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

}
