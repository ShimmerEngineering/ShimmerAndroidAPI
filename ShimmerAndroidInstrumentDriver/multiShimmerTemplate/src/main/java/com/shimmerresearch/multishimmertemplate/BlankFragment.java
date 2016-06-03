package com.shimmerresearch.multishimmertemplate;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.database.DatabaseHandler;
import com.shimmerresearch.service.MultiShimmerTemplateService;


/**
 * A fragment representing a single Item detail screen. This fragment is either
 * contained in a {@link ItemListActivity} in two-pane mode (on tablets) or a
 * {@link ItemDetailActivity} on handsets.
 */
public class BlankFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	ExpandableListView listViewShimmers;
    DatabaseHandler db;
	String[] deviceNames;
	String[] deviceBluetoothAddresses;
	String[][] mEnabledSensorNames;
	int numberofChilds[];
	public final int MSG_BLUETOOTH_ADDRESS=1;
	public final int MSG_CONFIGURE_SHIMMER=2;
	public final int MSG_CONFIGURE_SENSORS_SHIMMER=3;
	static String mSensorView="";
	boolean firstTime=true;
	static Dialog dialog;
    View rootView;
    MultiShimmerTemplateService mService;
	public static final String ARG_ITEM_ID = "item_id";
	static TextView mTVmsgreceived;
	/**
	 * The dummy content this fragment is presenting.
	 */
	

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public BlankFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		getActivity().invalidateOptionsMenu();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		
		rootView = inflater.inflate(R.layout.blank_main, container, false);			
		
		this.mService = ((MainActivity)getActivity()).mService;
		
		if(mService!=null){
			setup();
		}

		return rootView;
	}
	

	    public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        Log.d("Activity Name",activity.getClass().getSimpleName());
	        if (!isMyServiceRunning()){
	        	Intent intent=new Intent(getActivity(), MultiShimmerTemplateService.class);
	        	getActivity().startService(intent);
	        }
	    }
	

	    protected boolean isMyServiceRunning() {
	        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
	        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	            if ("com.shimmerresearch.service.MultiShimmerTemplateService".equals(service.service.getClassName())) {
	                return true;
	            }
	        }
	        return false;
	    }
	    
	   
	    
	    
	  
	    
	    
	    private static Handler mHandler = new Handler() {

			public void handleMessage(Message msg) {
	        	
	            switch (msg.what) {
	            case Shimmer.MESSAGE_TOAST:
	            	Log.d("toast",msg.getData().getString(Shimmer.TOAST));
	            	
	            case Shimmer.MESSAGE_READ:
					if (mTVmsgreceived.getText().toString().equals("Data Received")){
						
					} else {
						mTVmsgreceived.setText("Data Received");
					}
					
	                break;
	            }
	        }
	    };

	 
	     
	     public void onResume(){
	    	 super.onResume();
	    	 firstTime=true;
	    	
	    	
	     }
	     
	     public void onPause(){
	    	 super.onPause();
	    	
	    	 
	    	
      	 }
	     @Override
		    public void onStop() {
		        super.onStop();
		    }
	     
	     public void setup(){
	    	 db=mService.mDataBase;
	    	 mService.mShimmerConfigurationList = db.getShimmerConfigurations("Temp");
	    	 mTVmsgreceived = (TextView) rootView.findViewById(R.id.textViewDataReceived);
	    	 mService.setGraphHandler(mHandler,"");
	    	 mService.enableGraphingHandler(true);

	     }
}
