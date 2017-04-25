package com.shimmerresearch.multishimmertemplate;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import pl.flex_it.androidplot.XYSeriesShimmer;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Dialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.DynamicTableModel;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XLayoutStyle;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.androidplot.xy.YLayoutStyle;
import com.shimmerresearch.adapters.CheckboxListAdapter;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.database.DatabaseHandler;
import com.shimmerresearch.database.ShimmerConfiguration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driverUtilities.ChannelDetails.CHANNEL_TYPE;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.service.MultiShimmerTemplateService;



public class PlotFragment extends Fragment {
	
//	ExpandableListView listViewShimmers;
    DatabaseHandler db;
	static String[] deviceNames;
	String[] deviceBluetoothAddresses;
	String[][] mEnabledSensorNames;
	boolean[][] mSelectedSignals;

	int numberofChilds[];
	public final int MSG_BLUETOOTH_ADDRESS=1;
	public final int MSG_CONFIGURE_SHIMMER=2;
	public final int MSG_CONFIGURE_SENSORS_SHIMMER=3;
	static int mCount=1;
	int tempPosition;
	View tempViewChild;
	static String mSensorView="";
	boolean firstTime=true;
	static List<Number> dataList = new ArrayList<Number>();
	static List<Number> dataTimeList = new ArrayList<Number>();
	static XYSeriesShimmer series1;
	static LineAndPointFormatter lineAndPointFormatter;
    private static XYPlot dynamicPlot;
    final static int X_AXIS_LENGTH = 500;
    static Dialog dialogSelectSensor;
    static private String[] mBluetoothAddressforPlot=new String[7];
    static private String[][] mSensorsforPlot=new String[7][Shimmer.MAX_NUMBER_OF_SIGNALS];
    static private String[][] mSensorsforPlotFormat=new String[7][Shimmer.MAX_NUMBER_OF_SIGNALS];
    public static HashMap<String, XYSeriesShimmer> mPlotSeriesMap = new HashMap<String, XYSeriesShimmer>(100);
    public static HashMap<String, LineAndPointFormatter> mPlotFormatMap = new HashMap<String, LineAndPointFormatter>(100);
    public static HashMap<String, List<Number>> mPlotDataMap = new HashMap<String, List<Number>>(10);
    public HashMap<String, List<SelectedSensors>> mSelectedSensors = new HashMap<String, List<SelectedSensors>>(7);
    View rootView;
    MultiShimmerTemplateService mService;
//	private boolean mServiceBind = false;
	public static final String ARG_ITEM_ID = "item_id";
	private Paint transparentPaint, outlinePaint;
	public Dialog sensorsDialog;
	public Button selectSensor;
	public Button doneSelectedSensors;
	public ListView listViewSensors;
	public ListView listViewDevices;
	public View lateralBar;
	public static int devicePosition;
//	public int[] sensorColor = {Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW, Color.DKGRAY, Color.CYAN, Color.GRAY, Color.BLACK, Color.MAGENTA, Color.LTGRAY};
	public int[] sensorColor;
	public static int indexColor=0;
	public int lastDeviceSelected=-1;
	public static TextView heartRateText;
	public ImageView hearRateImage;
	public static boolean enableHeartRate;
	public static int heartRateCont;
	public static int heartRateRefresh;
	public static String mBluetoothAddressToHeartRate;
	
	/**
	 * The dummy content this fragment is presenting.
	 */
//	private MenuContent.MenuItem mItem;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public PlotFragment() {	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		
		getActivity().invalidateOptionsMenu();
//		indexColor = 0;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		
		//this values should be loaded from a database, but for now this will do, when you exit this fragment this list should be saved to a database
		rootView = inflater.inflate(R.layout.plot_main, container, false);	
		
//		indexColor=0;
		Arrays.fill(mBluetoothAddressforPlot,"");
		for (String[] row: mSensorsforPlot){
			Arrays.fill(row,"");
		}

		for (String[] row: mSensorsforPlotFormat){
			Arrays.fill(row,CHANNEL_TYPE.UNCAL.toString());
		}
			
		
		int[] tmpColor = getActivity().getResources().getIntArray(R.array.plotColor);
		sensorColor = tmpColor;
//		for (int i = 0; i < 20; i++) {
//		    
//		    // Do something with the paint.
//		}

		/** --Plot set up-- **/
		
		// get handles to our View defined in layout.xml:
        dynamicPlot = (XYPlot) rootView.findViewById(R.id.dynamicPlot);
        // only display whole numbers in domain labels
        dynamicPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
        transparentPaint = new Paint();
        transparentPaint.setColor(Color.TRANSPARENT);
        //lineAndPointFormatter1.setLinePaint(p);
        dynamicPlot.setDomainStepMode(XYStepMode.SUBDIVIDE);
        //dynamicPlot.setDomainStepValue(series1.size());
        dynamicPlot.getLegendWidget().setSize(new SizeMetrics(0, SizeLayoutType.ABSOLUTE, 0, SizeLayoutType.ABSOLUTE));
        // thin out domain/range tick labels so they dont overlap each other:
        dynamicPlot.setTicksPerDomainLabel(5);
        dynamicPlot.setTicksPerRangeLabel(3);
        dynamicPlot.disableAllMarkup();
        Paint gridLinePaint = new Paint();
        gridLinePaint.setColor(getResources().getColor(R.color.text_color));
        dynamicPlot.setRangeBoundaries(-15, 15, BoundaryMode.AUTO); // freeze the range boundary:
        dynamicPlot.setDomainBoundaries(0, X_AXIS_LENGTH, BoundaryMode.FIXED); // freeze the domain boundary:
        dynamicPlot.getGraphWidget().setMargins(0, 20, 10, 10);
        dynamicPlot.setBorderStyle(Plot.BorderStyle.NONE, null, null);
        dynamicPlot.getBackgroundPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.setBackgroundColor(Color.TRANSPARENT);
        dynamicPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.getGraphWidget().setGridLinePaint(transparentPaint);
        dynamicPlot.getGraphWidget().setDomainOriginLabelPaint(gridLinePaint);
        dynamicPlot.getGraphWidget().setDomainOriginLinePaint(gridLinePaint);
        dynamicPlot.getGraphWidget().setDomainLabelPaint(gridLinePaint);
        dynamicPlot.getGraphWidget().getDomainLabelPaint().setTextSize(20);
        dynamicPlot.getDomainLabelWidget().pack();
        outlinePaint = dynamicPlot.getGraphWidget().getDomainOriginLinePaint();
        outlinePaint.setStrokeWidth(3);
        dynamicPlot.getGraphWidget().setClippingEnabled(false);
        dynamicPlot.getGraphWidget().setRangeOriginLabelPaint(gridLinePaint);
        dynamicPlot.getGraphWidget().setRangeOriginLinePaint(gridLinePaint);
        dynamicPlot.getGraphWidget().setRangeLabelPaint(gridLinePaint);
        dynamicPlot.getGraphWidget().getRangeLabelPaint().setTextSize(20);
        dynamicPlot.getRangeLabelWidget().pack();
        outlinePaint = dynamicPlot.getGraphWidget().getRangeOriginLinePaint();
        outlinePaint.setStrokeWidth(3);
        dynamicPlot.getLayoutManager().remove(dynamicPlot.getRangeLabelWidget());
        dynamicPlot.getLayoutManager().remove(dynamicPlot.getDomainLabelWidget());
        dynamicPlot.clear();
        
		
		Log.d("ShimmerH","OnCreate");
		firstTime=true;
		
		this.mService = ((MainActivity)getActivity()).mService;

		if(mService!=null){
			setup();
		}
		
		final AlertDialog.Builder dialogNoStreaming = new AlertDialog.Builder(getActivity()).setTitle("Error").setMessage("No device streaming")
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						arg0.dismiss();
					}
				});
		
		
		dialogSelectSensor = new Dialog(getActivity());
//		dialogSelectSensor.setTitle("Select a device");
		
		dialogSelectSensor.setContentView(R.layout.plot_sensors_selection);
		
		
		listViewDevices = (ListView) dialogSelectSensor.findViewById(R.id.listPlotDevices);
		listViewDevices.setChoiceMode(1);
		listViewDevices.setItemChecked(0, true);
		listViewSensors = (ListView) dialogSelectSensor.findViewById(R.id.listPlotSensors);
//		listViewSensors.setVisibility(View.GONE);
		lateralBar = (View) dialogSelectSensor.findViewById(R.id.lateral_bar);
//		lateralBar.setVisibility(View.GONE);
				
		listViewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
				// TODO Auto-generated method stub	
//				arg1.setBackgroundResource(R.color.shimmer_orange);
//				if(lastDeviceSelected!=-1 && lastDeviceSelected!=position)
//					listViewDevices.getChildAt(lastDeviceSelected).setBackgroundResource(Color.TRANSPARENT);
				
				lastDeviceSelected = position;
				dialogSelectSensor.setTitle("Device selected: "+deviceNames[position]);
				CheckboxListAdapter adapterSensors = new CheckboxListAdapter(getActivity().getLayoutInflater(),
																mSelectedSensors.get(deviceBluetoothAddresses[position]));	
				listViewSensors.setAdapter(adapterSensors);
//				doneSelectedSensors.setVisibility(View.VISIBLE);
//				dialogSelectSensor.findViewById(R.id.layoutButtonPlotDone).setVisibility(View.VISIBLE);
//				listViewSensors.setVisibility(View.VISIBLE);
//				lateralBar.setVisibility(View.VISIBLE);
			}
		});
				
		
		selectSensor = (Button) rootView.findViewById(R.id.buttonSelectSensor);
		selectSensor.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ArrayAdapter<String> adapterDevices = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_activated_1);
				adapterDevices.addAll(deviceNames);
//				listViewDevices.setItemChecked(0, true);
				listViewDevices.setAdapter(adapterDevices);
				listViewDevices.setItemChecked(0, true);
				if(mService.noDevicesStreaming())
					dialogNoStreaming.show();
				else{
					dialogSelectSensor.setTitle("Device selected: "+deviceNames[0]);
					CheckboxListAdapter adapterSensors = new CheckboxListAdapter(getActivity().getLayoutInflater(),
							mSelectedSensors.get(deviceBluetoothAddresses[0]));	
					listViewSensors.setAdapter(adapterSensors);
//					listViewDevices.setSelector(R.drawable.selector_listdevices);
					dialogSelectSensor.show();
				}
			}
		});
		
		doneSelectedSensors = (Button) dialogSelectSensor.findViewById(R.id.buttonPlotDone);
//		doneSelectedSensors.setVisibility(View.GONE);
		doneSelectedSensors.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				plotSignals();
				dialogSelectSensor.dismiss();
			}
		});

//		listViewShimmers = (ExpandableListView) rootView.findViewById(R.id.expandableListViewReport);


		heartRateText = (TextView) rootView.findViewById(R.id.floating_text);
		hearRateImage = (ImageView) rootView.findViewById(R.id.floating_heart);

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
	    
	    
	/*
	 public ServiceConnection mConnection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName arg0, IBinder service) {
				// TODO Auto-generated method stub
				Log.d("Shimmer","SERRRVVVIIICE");
				Log.d("ShimmerService", "service connected from main activity");
	      		com.shimmerresearch.service.MultiShimmerTemplateService.LocalBinder binder = (com.shimmerresearch.service.MultiShimmerTemplateService.LocalBinder) service;
	      		mService = binder.getService();
	      		mServiceBind = true;
	      		
			}

			@Override
			public void onServiceDisconnected(ComponentName arg0) {
				// TODO Auto-generated method stub
				mServiceBind=false;
			}
	    };
*/
	    protected boolean isMyServiceRunning() {
	        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
	        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	            if ("com.shimmerresearch.service.MultiShimmerTemplateService".equals(service.service.getClassName())) {
	                return true;
	            }
	        }
	        return false;
	    }
	    
	    public void updateShimmerConfigurationList(List<ShimmerConfiguration> shimmerConfigurationList){
			//save configuration settings 
	    	db.saveShimmerConfigurations("Temp", mService.mShimmerConfigurationList);
	    	//query service get the deviceNames and Bluetooth addresses which are streaming
	  		shimmerConfigurationList=mService.getStreamingDevices();
	  		deviceNames=new String[shimmerConfigurationList.size()]; 
	  		deviceBluetoothAddresses=new String[shimmerConfigurationList.size()]; 
	  		mEnabledSensorNames= new String[shimmerConfigurationList.size()][Shimmer.MAX_NUMBER_OF_SIGNALS];// up to 9 (eg accel x, accel y , accel z, gyro...,mag...,ExpB0,ExpB7 
	  		numberofChilds=new int[shimmerConfigurationList.size()];
	  		mSelectedSignals = mService.getPlotSelectedSignals();
	  		
	  		
	  		
	  		int pos=0;
	  		for (ShimmerConfiguration sc:shimmerConfigurationList){
	  			deviceNames[pos]=sc.getDeviceName();
	      		deviceBluetoothAddresses[pos]=sc.getBluetoothAddress();
	      		Shimmer shimmer = mService.getShimmer(deviceBluetoothAddresses[pos]);
	      		mEnabledSensorNames[pos]=shimmer.getListofEnabledChannelSignals();
	      		numberofChilds[pos]=getNumberofChildren(sc.getEnabledSensors(),sc.getBluetoothAddress());
	      		ArrayList<SelectedSensors> sensors = new ArrayList<SelectedSensors>();
	      		if(mSelectedSignals!=null)
	      			for(int i=0; i<shimmer.getListofEnabledChannelSignals().length;i++)
	      				sensors.add(new SelectedSensors(mEnabledSensorNames[pos][i], mSelectedSignals[pos][i]));
	      		else
	      			for(int i=0; i<shimmer.getListofEnabledChannelSignals().length;i++)
	      				sensors.add(new SelectedSensors(mEnabledSensorNames[pos][i], false));
	      		
	      		mSelectedSensors.put(deviceBluetoothAddresses[pos], sensors);
	      		pos++;
	  		}
	  		
	  		//if there are signals selected, plot the signals 
	  		if(mSelectedSignals!=null && !mService.noDevicesStreaming())
	  			plotSignals();
	  		
	    }
	    
	    
	  
	    
	    public int getNumberofChildren(long enabledSensors, String bluetoothAddress){
	    	int count=1; //timestamp
	    	int shimmerVersion = mService.getShimmerVersion(bluetoothAddress);
	    	if (shimmerVersion==ShimmerVerDetails.HW_ID.SHIMMER_3 || shimmerVersion==ShimmerVerDetails.HW_ID.SHIMMER_SR30){
	    		if (((enabledSensors & 0xFF)& Shimmer.SENSOR_ACCEL) > 0){
	 		    	count=count+3;
	 		    }
	    		if (((enabledSensors & 0xFFFF)& Shimmer.SENSOR_DACCEL) > 0){
	 		    	count=count+3;
	 		    }
	    		if (((enabledSensors & 0xFF)& Shimmer.SENSOR_GYRO) > 0) {
	 				count=count+3;
	 			}
	 			if (((enabledSensors & 0xFF)& Shimmer.SENSOR_MAG) > 0) {
	 				count=count+3;
	 			}
	 			if (((enabledSensors & 0xFFFF)& Shimmer.SENSOR_BATT) > 0) {
	 				count=count+1;
	 			}
	 			if (((enabledSensors & 0xFFFFFF)& Shimmer.SENSOR_EXT_ADC_A15) > 0) {
	 				count=count+1;
	 			}
	 			if (((enabledSensors & 0xFFFFFF)& Shimmer.SENSOR_EXT_ADC_A7) > 0) {
	 				count=count+1;
	 			}
	 			if (((enabledSensors & 0xFFFFFF)& Shimmer.SENSOR_EXT_ADC_A6) > 0) {
	 				count=count+1;
	 			}
	 			if (((enabledSensors & 0xFFFFFF)& Shimmer.SENSOR_INT_ADC_A1) > 0) {
	 				count=count+1;
	 			}
	 			if (((enabledSensors & 0xFFFFFF)& Shimmer.SENSOR_INT_ADC_A12) > 0) {
	 				count=count+1;
	 			}
	 			if (((enabledSensors & 0xFFFFFF)& Shimmer.SENSOR_INT_ADC_A13) > 0) {
	 				count=count+1;
	 			}
	 			if (((enabledSensors & 0xFFFFFF)& Shimmer.SENSOR_INT_ADC_A14) > 0) {
	 				count=count+1;
	 			}
	 			if (((enabledSensors & 0xFFFFFF)& Shimmer.SENSOR_GSR) > 0) {
	 				count=count+1;
	 			}
	 			if ((enabledSensors& Shimmer.SENSOR_BMP180) > 0) {
	 				count=count+2;
	 			}
	 			if ((enabledSensors & 0x10) > 0) {
	 				count=count+3;
	 			}
	 			if ((enabledSensors & 0x08) > 0) {
	 				count=count+3;
	 			}
	 			if ((enabledSensors & 0x080000) > 0) {
	 				count=count+3;
	 			}
	 			if ((enabledSensors & 0x100000) > 0) {
	 				count=count+3;
	 			}
	 			if ((((enabledSensors & 0xFF)& Shimmer.SENSOR_ACCEL) > 0 || (((enabledSensors & 0xFFFF)& Shimmer.SENSOR_DACCEL) > 0)) && ((enabledSensors & 0xFF)& Shimmer.SENSOR_GYRO) > 0 && ((enabledSensors & 0xFF)& Shimmer.SENSOR_MAG) > 0 && mService.is3DOrientationEnabled(bluetoothAddress)){
	 				count=count+8; //axis angle and quartenion
	 			}
	    	} else {
			    if (((enabledSensors & 0xFF)& Shimmer.SENSOR_ACCEL) > 0){
			    	count=count+3;
			    }
				if (((enabledSensors & 0xFF)& Shimmer.SENSOR_GYRO) > 0) {
					count=count+3;
				}
				if (((enabledSensors & 0xFF)& Shimmer.SENSOR_MAG) > 0) {
					count=count+3;
				}
				if (((enabledSensors & 0xFF) & Shimmer.SENSOR_GSR) > 0) {
					count=count+1;
				}
				if (((enabledSensors & 0xFF) & Shimmer.SENSOR_ECG) > 0) {
					count=count+2;
				}
				if (((enabledSensors & 0xFF) & Shimmer.SENSOR_EMG) > 0) {
					count++;
				}
				if (((enabledSensors & 0xFF00) & Shimmer.SENSOR_BRIDGE_AMP) > 0) { //because there is strain gauge high and low add twice
					count++;
					count++;
				}
				if (((enabledSensors & 0xFF00) & Shimmer.SENSOR_HEART) > 0) {
					count++;
				}
				if (((enabledSensors & 0xFF) & Shimmer.SENSOR_EXP_BOARD_A0) > 0) {
					count++;
				}
				if (((enabledSensors & 0xFF) & Shimmer.SENSOR_EXP_BOARD_A7) > 0) {
					count++;
				}
				if (((enabledSensors & 0xFF)& Shimmer.SENSOR_ACCEL) > 0 && ((enabledSensors & 0xFF)& Shimmer.SENSOR_GYRO) > 0 && ((enabledSensors & 0xFF)& Shimmer.SENSOR_MAG) > 0 && mService.is3DOrientationEnabled(bluetoothAddress)){
					count=count+8; //axis angle and quartenion
				}
	    	}
	    	Shimmer shimmer = mService.getShimmer(bluetoothAddress);
	    	if (shimmer!=null){
	    		
	    	}
			return count;
	    
	    }
	    
	    private static Handler mHandler = new Handler() {

			public void handleMessage(Message msg) {
	        	
	            switch (msg.what) {
	            case Shimmer.MESSAGE_TOAST:
	            	Log.d("toast",msg.getData().getString(Shimmer.TOAST));
	            	
	            case ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET:
	            	    if ((msg.obj instanceof ObjectCluster)){
	            	    	
	            	    ObjectCluster objectCluster =  (ObjectCluster) msg.obj; 
	            		//log data
	            	    if(enableHeartRate){
	            	    	if(objectCluster.getMacAddress().equals(mBluetoothAddressToHeartRate)){
		            	    	if(heartRateCont==heartRateRefresh){
			            	    	Collection<FormatCluster> ofFormatstemp = objectCluster.getCollectionOfFormatClusters("Heart Rate");  // first retrieve all the possible formats for the current sensor device
						 	    	FormatCluster formatClustertemp = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormatstemp,"CAL"));
						 	    	if (formatClustertemp != null) {
						 	    		int bmp = (int) formatClustertemp.mData;
						 	    		heartRateText.setText(bmp + " Bmp"); 	
						 	    	}
						 	    	heartRateCont = 0;
		            	    	}
		            	    	else
		            	    		heartRateCont++;
		            	    			 	    			
    		 	    			
	            	    	}
	            	    }
					 	 
	            	    //first check what signals have been selected in the checkbox
	            	    
	            	    //iterate through every bluetooth address 
	            	    for (int i=0;i<mBluetoothAddressforPlot.length;i++){
	            	    	//if it is the corresponding datapacket
		            	    if (!mBluetoothAddressforPlot[i].equals("")){
		            	    	// for every bluetooth address look through the sensors if it is the correct packet
		            	    	if (objectCluster.getMacAddress().equals(mBluetoothAddressforPlot[i])) {
		            	    	for (int k=0;k<mSensorsforPlot[0].length;k++){
		            	    		if (!mSensorsforPlot[i][k].equals("")){
//		            	    			Log.d("ShimmerPLOT",mSensorsforPlot[i][k]);
		            	    			Collection<FormatCluster> ofFormats = objectCluster.getCollectionOfFormatClusters(mSensorsforPlot[i][k]);  // first retrieve all the possible formats for the current sensor device
//		            	    			Log.d("ShimmerPLOT",mSensorsforPlotFormat[i][k]);
		            	    			FormatCluster formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(ofFormats,mSensorsforPlotFormat[i][k])); 
		            		 	    	if (formatCluster != null) {
//		            		 	    		Log.d("ShimmerPLOT",mBluetoothAddressforPlot[i] + " : " + mSensorsforPlot[i][k]);
		            		 	    		String seriesName=deviceNames[i] + " : " + mSensorsforPlot[i][k];
		            		 	    		
		            		 	    		//first check if there is data
		            		 	    		
		            		 	    		List<Number> data;
	            		 	    			if (mPlotDataMap.get(seriesName)!=null){
	            		 	    				data = mPlotDataMap.get(seriesName);
	            		 	    			} else {
	            		 	    				data = new ArrayList<Number>();
	            		 	    			}
	            		 	    			if (data.size()>X_AXIS_LENGTH){
	            		 	    				data.clear();
	            		 	    			}
	            		 	    			data.add(formatCluster.mData);
	            		 	    			mPlotDataMap.put(seriesName, data);
	            		 	    			
	            		 	    			//next check if the series exist 
	            		 	    			LineAndPointFormatter lapf;
	            		 	    			if (mPlotSeriesMap.get(seriesName)!=null){
	            		 	    				//if the series exist get the line format
		            		 	    			//dynamicPlot.removeSeries(mPlotSeriesMap.get(seriesName));
	            		 	    				mPlotSeriesMap.get(seriesName).updateData(data);
		            		 	    			lapf=mPlotFormatMap.get(seriesName);
		            		 	    		} else {
		            		 	    			//generate a random line and point format
		            		 	    			//lapf = new LineAndPointFormatter(Color.rgb((int) (255*Math.random()), (int) (255*Math.random()), (int) (255*Math.random())), null, null);
		            		 	    			XYSeriesShimmer series = new XYSeriesShimmer(data, 0, seriesName);
			            		 	    		mPlotSeriesMap.put(seriesName, series);
		            			 	    		//mPlotFormatMap.put(seriesName, lapf);
			            		 	    		lapf=mPlotFormatMap.get(seriesName);
		            		 	    			dynamicPlot.addSeries(mPlotSeriesMap.get(seriesName), lapf);
		            		 	    			
		            		 	    			//change the font color on the CheckBox
			            		 	    	}
	            		 	    			
		            		 	    		
		            		 	    	}
		            	    		}
		            	    	}
		            	    }
	            	    }
	            	    }
	            	    dynamicPlot.redraw();			 	    	
	            	}
					
	                break;
	            }
	        }
	    };
	    
	    public void plotSignals(){
	    	
	    	int numberOfSignalsToPlot=0;
//	    	indexColor=0;
//	    	mPlotFormatMap.clear();
	    	
	    	for(int i=0;i<deviceBluetoothAddresses.length;i++){
	    		String deviceAddress = deviceBluetoothAddresses[i];
	    		List<SelectedSensors> list = mSelectedSensors.get(deviceAddress);
	    		for(int j=0; j<list.size();j++){
	    			String sensorName = list.get(j).getNameSensor();
	    			if(list.get(j).isSelected()){
	    				setFilteredSignals(i, j, deviceAddress, sensorName);
	    				setSensorsforPlotFormat(i, j, true); //always show the calibrated data
	    				setPlotFormat(deviceNames[i], sensorName, 0);
	    				numberOfSignalsToPlot++;
	    			}
	    			else{
	    				setFilteredSignals(i, j, "", "");
	    			}
	    				
	    		}
	    	}
	    	mPlotDataMap.clear();
	    	
	    	//Set the plot legend
	    	
	    	if(numberOfSignalsToPlot!=0){
	    		dynamicPlot.getLegendWidget().setTableModel(new DynamicTableModel(1, numberOfSignalsToPlot));
	    		
	    		Paint textStyle = new Paint();
	    		textStyle.setColor(Color.WHITE);
	    		textStyle.setAlpha(140);
	    		textStyle.setTextSize(16);
	    		dynamicPlot.getLegendWidget().setTextPaint(textStyle);
	    		 
		        // adjust the legend size so there is enough room
		        // to draw the new legend grid:
		        dynamicPlot.getLegendWidget().setSize(new SizeMetrics(numberOfSignalsToPlot*30, SizeLayoutType.ABSOLUTE, 325, SizeLayoutType.ABSOLUTE));
		        
		 
		        // add a semi-transparent black background to the legend
		        // so it's easier to see overlaid on top of our plot:
		        Paint bgPaint = new Paint();
		        bgPaint.setColor(Color.BLACK);
		        bgPaint.setStyle(Paint.Style.FILL);
		        bgPaint.setAlpha(140);
		        dynamicPlot.getLegendWidget().setBackgroundPaint(bgPaint);
		 
		        // adjust the padding of the legend widget to look a little nicer:
		        dynamicPlot.getLegendWidget().setPadding(10, 2, 2, 2);       
		 
		        // reposition the grid so that it rests above the bottom-left
		        // edge of the graph widget:
		        dynamicPlot.position(
		        		dynamicPlot.getLegendWidget(),
		                5,
		                XLayoutStyle.ABSOLUTE_FROM_RIGHT,
		                35,
		                YLayoutStyle.ABSOLUTE_FROM_BOTTOM,
		                AnchorPosition.RIGHT_BOTTOM);
	    	}
	   
	    }


	    public void setFilteredSignals(int groupPosition,int childPostion, String bluetoothAddress, String signalName){
	 	   
	 	   //the hashmap entry <bluetoothaddress> : <signalname>
	 	   if (bluetoothAddress.equals("")){
//	 		   String seriesName=mBluetoothAddressforPlot[groupPosition] + " : " + mSensorsforPlot[groupPosition][childPostion];
	 		  String seriesName=deviceNames[groupPosition] + " : " + mSensorsforPlot[groupPosition][childPostion];
	 		   if (mPlotDataMap.get(seriesName)!=null){
	 			   dynamicPlot.removeSeries(mPlotSeriesMap.get(seriesName));
	 			   mPlotSeriesMap.remove(seriesName);
	 			   mPlotFormatMap.remove(seriesName);
	 		   }
	 	   } else {
	 		   //a checkbox has been selected, clear the data so it can start afresh
//	 		   mPlotDataMap.clear();
	 	   }
	 	   mSensorsforPlot[groupPosition][childPostion]=signalName;
	 	   
	 	   //if no sensors remove the address as well
	 	   boolean noOtherSensor=true;
	 	   for (int i=0;i<mSensorsforPlot[groupPosition].length;i++){
	 		      if (!mSensorsforPlot[groupPosition][i].equals("")){
	 		    	  noOtherSensor=false;
	 		      }
	 	   }
	 	   if (noOtherSensor){
	 		   mBluetoothAddressforPlot[groupPosition]="";
	 	   } else if (!bluetoothAddress.equals("")){
	 		   mBluetoothAddressforPlot[groupPosition]=bluetoothAddress;
	 	   }
	    }
	    
	    public void setSensorsforPlotFormat(int groupPosition, int childPosition, boolean calibrated){
	 	   if (calibrated){
	 		   mSensorsforPlotFormat[groupPosition][childPosition]="CAL";
	 	   } else {
	 		   mSensorsforPlotFormat[groupPosition][childPosition]=CHANNEL_TYPE.UNCAL.toString();
	 	   }
	 	   
	    }
	     
	     public void setPlotFormat(String bluetoothAddress, String signal, int color){
	     	String seriesName=bluetoothAddress + " : " + signal;
	     	if(mPlotFormatMap.get(seriesName)==null){
	     		mPlotFormatMap.put(seriesName, new LineAndPointFormatter(sensorColor[indexColor], null, null));
	     		indexColor=(indexColor+1)%sensorColor.length;
	     	}
	     }
	     
	     public void onResume(){
	    	 super.onResume();
	    	 firstTime=true;
	    	 
	    	 //this is needed if you switch off the screen to make sure the selected signals are plotted
//	    	 if (getActivity().getClass().getSimpleName().equals("ItemListActivity")){
//	 			this.mService=((ItemListActivity)getActivity()).mService;	
//	 		
//	 		} else {
//	 			this.mService=((ItemDetailActivity)getActivity()).mService;
//	 		
//	 		}
	    	 
	    	 this.mService = ((MainActivity)getActivity()).mService;
	    	 
	 		if(mService!=null){
	 			setup();
	 		}
	 		
	 		enableHeartRate = mService.isHeartRateEnabled() || mService.isHeartRateEnabledECG();
			if(enableHeartRate){
				hearRateImage.setVisibility(View.VISIBLE);
				heartRateText.setVisibility(View.VISIBLE);
				heartRateCont=0;
				mBluetoothAddressToHeartRate = mService.mBluetoothAddressToHeartRate;
				heartRateRefresh = (int) mService.getSamplingRate(mBluetoothAddressToHeartRate);
			}
			else{
				hearRateImage.setVisibility(View.GONE);
				heartRateText.setVisibility(View.GONE);
			}
			
			
	     }
	     
	     public void onPause(){
	    	 super.onPause();
	    	 mPlotSeriesMap.clear();
	    	 mPlotFormatMap.clear();
	    	 mPlotDataMap.clear();
	    	 dynamicPlot.clear();
	    	 mBluetoothAddressforPlot=new String[7];
	    	 mSensorsforPlot=new String[7][Shimmer.MAX_NUMBER_OF_SIGNALS];
	    	 Arrays.fill(mBluetoothAddressforPlot,"");
	    	 for (String[] row: mSensorsforPlot){
	    		 Arrays.fill(row,"");
	    	 }
	    	 
	    	 if(mSelectedSignals==null)
	    		 mSelectedSignals = new boolean[7][Shimmer.MAX_NUMBER_OF_SIGNALS];
	    		 
	    	 for(int i=0;i<deviceBluetoothAddresses.length;i++){
	    		 List<SelectedSensors> tmp = mSelectedSensors.get(deviceBluetoothAddresses[i]);
	    		 	for(int j=0;j<tmp.size();j++)
	    		 		mSelectedSignals[i][j] = tmp.get(j).isSelected();
	    	 }
	    	 
    	  	 mService.storePlotSelectedSignals(mSelectedSignals);
      	 }
	     @Override
		    public void onStop() {
		        super.onStop();
//		        getActivity().unbindService(mConnection);
//		        mServiceBind = false;
		    }
	     
	     public void setup(){
	    	 db=mService.mDataBase;
	    	 mService.mShimmerConfigurationList = db.getShimmerConfigurations("Temp");
	    	 if (firstTime){
	    		 updateShimmerConfigurationList(mService.mShimmerConfigurationList);
	    		 firstTime=false;
	    	 }
	    	 //now connect the sensor nodes

	    	 mService.setGraphHandler(mHandler,"");
	    	 mService.enableGraphingHandler(true);
	    	 BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	    	 if(mBluetoothAdapter == null) { 
	    		 Toast.makeText(getActivity(), "Bluetooth not supported on device.", Toast.LENGTH_LONG).show();
	    	 } else {
	    		 if (!mBluetoothAdapter.isEnabled()) {
	    			 Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	    			 startActivityForResult(enableBtIntent, -1);
	    		 }
	    	 }
	     }
}
