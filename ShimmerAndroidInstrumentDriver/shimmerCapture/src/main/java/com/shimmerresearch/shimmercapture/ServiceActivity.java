//This is just a generic case for using a service in an activity. 

package com.shimmerresearch.shimmercapture;

import com.shimmerresearch.android.shimmerService.ShimmerService;
import com.shimmerresearch.android.shimmerService.ShimmerService.LocalBinder;



import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class ServiceActivity extends Activity {
	static ShimmerService mService;
	boolean mServiceBind=false;
	protected boolean mServiceFirstTime=true;
   
    protected ServiceConnection mTestServiceConnection = new ServiceConnection() {

      	public void onServiceConnected(ComponentName arg0, IBinder service) {
      		// TODO Auto-generated method stub
      		Log.d("ShimmerService", "service connected");
      		LocalBinder binder = (ShimmerService.LocalBinder) service;
      		mService = binder.getService();
      		mServiceBind = true;
      		//update the view
      		}

      	public void onServiceDisconnected(ComponentName arg0) {
      		// TODO Auto-generated method stub
      		mServiceBind = false;
      	}
    };
    protected boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.shimmerresearch.service.ShimmerServiceCBBC".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
      
    
}
