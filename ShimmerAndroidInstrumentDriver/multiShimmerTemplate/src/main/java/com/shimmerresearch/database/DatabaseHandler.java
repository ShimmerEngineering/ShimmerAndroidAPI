package com.shimmerresearch.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.shimmerresearch.android.Shimmer;

public class DatabaseHandler extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 9; // now includes the Shimmer version identifier

	// Database Name
	private static final String DATABASE_NAME = "Settings";

	private static final String TABLE_CURRENT_CONFIGURATION = "CurrentConfigurations";

	private static final String TABLE_CONFIGURATION = "Configurations";
	private static final String CONFIG_ID = "ConfigID";
	private static final String CONFIG_NAME = "ConfigName";
	private int INDEX_FOR_CURRENT_CONFIGURATION=1;

	private static final String TABLE_LICENSE = "License";
	private static final String LICENSE_TYPE = "LicenseType";


	private static final String TABLE_CONFIGURATION_DETAILS = "ConfigurationDetails";
	private static final String DEVICE_NAME = "DeviceName";
	private static final String BLUETOOTH_ADDRESS = "BluetoothAddress";
	private static final String SHIMMER_ROW_NUMBER = "ShimmerRowNumber";
	private static final String ENABLED_SENSORS = "EnabledSensors";
	private static final String SAMPLING_RATE = "SamplingRate";
	private static final String ACCEL_RANGE = "AccelRange";
	private static final String GYRO_RANGE = "GyroRange";
	private static final String MAG_RANGE = "MagRange";
	private static final String GSR_RANGE = "GsrRange";
	private static final String ACCEL_LOW_POWER = "AccelLowPower";
	private static final String GYRO_LOW_POWER = "GyroLowPower";
	private static final String MAG_LOW_POWER = "MagLowPower";
	private static final String PRESSURE_RESOLUTION = "PressureResolution";
	private static final String INTERNAL_EXP_POWER = "InternalExpPower";
	private static final String SHIMMER_VERSION = "ShimmerVersion";
	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CONFIGURATIONS_TABLE = "CREATE TABLE " + TABLE_CONFIGURATION + "("
				+ CONFIG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ CONFIG_NAME + " TEXT" + ")";

		String CREATE_LICENSE_TABLE = "CREATE TABLE " + TABLE_LICENSE + "("
				+ LICENSE_TYPE + " TEXT" + ")";

		String CREATE_CONFIGURATION_DETAILS_TABLE = "CREATE TABLE " + TABLE_CONFIGURATION_DETAILS + "("
				+ CONFIG_ID + " INTEGER,"
				+ DEVICE_NAME + " TEXT,"
				+ BLUETOOTH_ADDRESS + " TEXT,"
				+ SHIMMER_ROW_NUMBER + " INTEGER,"
				+ ENABLED_SENSORS + " INTEGER,"
				+ SAMPLING_RATE + " REAL,"
				+ ACCEL_RANGE + " INTEGER,"
				+ GSR_RANGE + " INTEGER,"
				+ GYRO_RANGE + " INTEGER,"
				+ MAG_RANGE + " INTEGER," 
				+ ACCEL_LOW_POWER + " INTEGER,"
				+ GYRO_LOW_POWER + " INTEGER," 
				+ MAG_LOW_POWER + " INTEGER,"
				+ SHIMMER_VERSION + " INTEGER," 
				+ PRESSURE_RESOLUTION + " INTEGER,"
				+ INTERNAL_EXP_POWER + " INTEGER,"
				+ " FOREIGN KEY ("+CONFIG_ID+") REFERENCES "+TABLE_CONFIGURATION+" ("+CONFIG_ID+") ON DELETE CASCADE"
				+ ")";

		String CREATE_CURRENT_CONFIGURATION_TABLE = "CREATE TABLE " + TABLE_CURRENT_CONFIGURATION + "("
				+ CONFIG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ CONFIG_NAME + " TEXT" + ")";

		if (!db.isReadOnly()) {
			db.execSQL("PRAGMA foreign_keys = ON;");
		}

		db.execSQL(CREATE_CONFIGURATIONS_TABLE);
		db.execSQL(CREATE_LICENSE_TABLE);
		db.execSQL(CREATE_CONFIGURATION_DETAILS_TABLE);
		db.execSQL(CREATE_CURRENT_CONFIGURATION_TABLE);

		//Insert License None
		ContentValues values = new ContentValues();
		values.put(LICENSE_TYPE, "None");
		db.insert(TABLE_LICENSE, null, values);

		//DEFAULT CONFIGURATION
		values = new ContentValues();
		values.put(CONFIG_NAME, "Temp"); 

		long rowID = db.insert(TABLE_CONFIGURATION, null, values);
		values.put(CONFIG_ID, INDEX_FOR_CURRENT_CONFIGURATION); 
		db.insert(TABLE_CURRENT_CONFIGURATION, null, values);


		Log.d("ShimmerDB", Long.toString(rowID));

//		values = new ContentValues();
//		values.put(DEVICE_NAME, "Device");
//		values.put(BLUETOOTH_ADDRESS, "");
//		values.put(CONFIG_ID, rowID);
//		values.put(SHIMMER_ROW_NUMBER, 0);
//		values.put(ENABLED_SENSORS, 0);
//		values.put(SAMPLING_RATE, 51.2);
//		values.put(ACCEL_RANGE, -1);
//		values.put(GSR_RANGE, -1);
//		values.put(GYRO_RANGE, -1);
//		values.put(MAG_RANGE, -1);
//		values.put(ACCEL_LOW_POWER, 0);
//		values.put(GYRO_LOW_POWER, 0);
//		values.put(MAG_LOW_POWER, 0);
//		values.put(SHIMMER_VERSION, -1);
//		values.put(PRESSURE_RESOLUTION, -1);
//		values.put(INTERNAL_EXP_POWER, -1);
//		db.insert(TABLE_CONFIGURATION_DETAILS, null, values);
		Log.d("ShimmerDB", Long.toString(rowID));

	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONFIGURATION);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONFIGURATION_DETAILS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CURRENT_CONFIGURATION);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LICENSE);
		// Create tables again
		onCreate(db);
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 */


	public boolean findLicense(String license){
		boolean found=false;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_LICENSE, new String[] {LICENSE_TYPE}, LICENSE_TYPE + "=?",
				new String[] {license}, null, null, null, null);
		if (cursor.moveToFirst()) {
			found = true;
		}
		return found;
	}

	public boolean setLicense(String license){
		boolean found=true;
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues values = new ContentValues();
		values.put(LICENSE_TYPE, license);
		db.update(TABLE_LICENSE, values, LICENSE_TYPE + "=?",
				new String[] {"None"});
		return found;
	}

	public int getConfigID(String configName){
		int configID=-1;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_CONFIGURATION, new String[] {CONFIG_ID}, CONFIG_NAME + "=?",
				new String[] {configName}, null, null, null, null);
		if (cursor.moveToFirst()) {
			configID=Integer.parseInt(cursor.getString(0));
		}
		return configID;
	}

	public String getConfigName(long configID){
		String configName="";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_CONFIGURATION, new String[] {CONFIG_NAME}, CONFIG_ID + "=?",
				new String[] {Long.toString(configID)}, null, null, null, null);
		if (cursor.moveToFirst()) {
			configName=cursor.getString(0);
		}
		return configName;
	}



	public String getCurrentConfiguration(){
		String configName="";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_CURRENT_CONFIGURATION, new String[] {CONFIG_NAME}, CONFIG_ID + "=?",
				new String[] {Integer.toString(INDEX_FOR_CURRENT_CONFIGURATION)}, null, null, null, null);
		if (cursor.moveToFirst()) {
			configName=(cursor.getString(0));
		}
		return configName;
	}


	public List<ShimmerConfiguration> getShimmerConfigurations(String configName) {
		SQLiteDatabase db = this.getReadableDatabase();
		int configID = getConfigID(configName);

		List<ShimmerConfiguration> shimmerConfigurationList = new ArrayList<ShimmerConfiguration>();
		if (configID!=-1){
			Cursor cursor = db.query(TABLE_CONFIGURATION_DETAILS, new String[] {DEVICE_NAME, BLUETOOTH_ADDRESS, SHIMMER_ROW_NUMBER, ENABLED_SENSORS, SAMPLING_RATE, ACCEL_RANGE, GSR_RANGE, SHIMMER_VERSION, ACCEL_LOW_POWER, GYRO_LOW_POWER, MAG_LOW_POWER, GYRO_RANGE, MAG_RANGE, PRESSURE_RESOLUTION, INTERNAL_EXP_POWER}, CONFIG_ID + "=?",
					new String[] { String.valueOf(configID) }, null, null, null, null);
			if (cursor != null)
				if (cursor.moveToFirst()) {
					do {
						ShimmerConfiguration shimmerConfiguration = new ShimmerConfiguration(cursor.getString(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3), cursor.getDouble(4), cursor.getInt(5), cursor.getInt(6), cursor.getInt(7), cursor.getInt(8), cursor.getInt(9), cursor.getInt(10), cursor.getInt(11), cursor.getInt(12), cursor.getInt(13), cursor.getInt(14));
						shimmerConfigurationList.add(shimmerConfiguration);
					} while (cursor.moveToNext());
				}
		}
		db.close();
		return shimmerConfigurationList;
	}

	public void deleteFromConfigurationDetailsTable(int configID) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_CONFIGURATION_DETAILS, CONFIG_ID + " = ?",
				new String[] {Integer.toString(configID)});
		db.close();
	}

	public void deleteConfiguration(String configName){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_CONFIGURATION, CONFIG_NAME + " = ?",
				new String[] {configName});
		db.close();
	}

	public void saveShimmerConfigurations(String configName, List<ShimmerConfiguration> shimmerConfigurationList){
		// check to see if exist 
		SQLiteDatabase db = this.getWritableDatabase();
		long configID = getConfigID(configName);
		if (configID != -1){
			// if exist delete old configuration details, because they will be replaced
			deleteFromConfigurationDetailsTable(getConfigID(configName));
		} else {
			//if does not exist create a new entry
			ContentValues values = new ContentValues();
			values.put(CONFIG_NAME, configName); 
			configID = db.insert(TABLE_CONFIGURATION, null, values);
		}

		// save new configurations
		insertConfigurationDetails(configID, shimmerConfigurationList);
	}
	private void insertConfigurationDetails(long configID, List<ShimmerConfiguration> shimmerConfigurationList){
		SQLiteDatabase db = this.getWritableDatabase();
		for (ShimmerConfiguration sc:shimmerConfigurationList){
			ContentValues values = new ContentValues(); 
			values = new ContentValues();
			values.put(DEVICE_NAME, sc.getDeviceName());
			values.put(BLUETOOTH_ADDRESS, sc.getBluetoothAddress());
			values.put(CONFIG_ID, configID);
			values.put(SHIMMER_ROW_NUMBER, sc.getShimmerRowNumber());
			values.put(ENABLED_SENSORS, sc.getEnabledSensors());
			values.put(GSR_RANGE, sc.getGSRRange());
			values.put(ACCEL_RANGE, sc.getAccelRange());
			values.put(SAMPLING_RATE, sc.getSamplingRate());
			values.put(SHIMMER_VERSION, sc.getShimmerVersion());
			values.put(GYRO_RANGE, sc.getGyroRange());
			values.put(MAG_RANGE, sc.getMagRange());
			values.put(ACCEL_LOW_POWER, sc.getLowPowerAccelEnabled());
			values.put(GYRO_LOW_POWER, sc.getLowPowerGyroEnabled());
			values.put(MAG_LOW_POWER, sc.getLowPowerMagEnabled());
			values.put(PRESSURE_RESOLUTION, sc.getPressureResolution());
			values.put(INTERNAL_EXP_POWER, sc.getIntExpPower());
			db.insert(TABLE_CONFIGURATION_DETAILS, null, values);
		}
		db.close();
	}

	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (!db.isReadOnly()) {
			// Enable foreign key constraints
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}

	public String[] getArrayofSettings() {
		SQLiteDatabase db = this.getReadableDatabase();
		List<String> shimmerConfigurationList = new ArrayList<String>();
		Cursor cursor = db.query(TABLE_CONFIGURATION, new String[] {CONFIG_ID}, null,
				null, null, null, null, null);
		if (cursor != null){
			if (cursor.moveToFirst()) {
				do {
					if (!getConfigName(Integer.parseInt(cursor.getString(0))).equals("Temp")){
						shimmerConfigurationList.add(getConfigName(Integer.parseInt(cursor.getString(0))));
					}
				} while (cursor.moveToNext());
			}
		}
		String[] settingNames = shimmerConfigurationList.toArray(new String[shimmerConfigurationList.size()]);
		return settingNames;
	}

	public void createNewConfiguration(String newConfigName){
		SQLiteDatabase db = this.getWritableDatabase();
		//if already exist delete first
		db.delete(TABLE_CONFIGURATION, CONFIG_NAME + " = ?", new String[] {newConfigName});
		ContentValues values = new ContentValues();
		values.put(CONFIG_NAME, newConfigName); 
		long rowID = db.insert(TABLE_CONFIGURATION, null, values);
		values = new ContentValues();
		values.put(DEVICE_NAME, "Device");
		values.put(BLUETOOTH_ADDRESS, "");
		values.put(CONFIG_ID, rowID);
		values.put(SHIMMER_ROW_NUMBER, 0);
		values.put(ENABLED_SENSORS, 0);
		values.put(SAMPLING_RATE, 51.2);
		values.put(ACCEL_RANGE, -1);
		values.put(GSR_RANGE, -1);
		values.put(GYRO_RANGE, -1);
		values.put(MAG_RANGE, -1);
		values.put(ACCEL_LOW_POWER, 0);
		values.put(GYRO_LOW_POWER, 0);
		values.put(MAG_LOW_POWER, 0);
		values.put(PRESSURE_RESOLUTION,-1);
		values.put(INTERNAL_EXP_POWER,-1);
		db.insert(TABLE_CONFIGURATION_DETAILS, null, values);
		Log.d("ShimmerDB", Long.toString(rowID));
	}

	public void setCurrentConfiguration(String configName){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(CONFIG_NAME, configName); 
		db.update(TABLE_CURRENT_CONFIGURATION, values,CONFIG_ID, new String[] {Long.toString(INDEX_FOR_CURRENT_CONFIGURATION)});
	}


}