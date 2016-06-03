package com.shimmerresearch.database;

public class ShimmerConfiguration {

	private double mSamplingRate;
	private long mEnabledSensors;
	private int mAccelRange;
	private int mShimmerRowNumber;
	private int mGSRRange;
	private String mBluetoothAddress;
	private String mDeviceName;
	private int mShimmerVersion;
	private int mGyroRange;
	private int mMagRange;
	private int mLowPowerAccel;
	private int mLowPowerGyro;
	private int mLowPowerMag;
	private int mPressureResolution;
	private int mIntExpPower;



	public ShimmerConfiguration(String deviceName, String bluetoothAddress, int shimmerRowNumber, long enabledSensors, double samplingRate,  int accelRange, int gsrRange, int shimmerVersion, int lowPowerAccel, int lowPowerGyro, int lowPowerMag, int gyroRange, int magRange, int pressureResolution, int intExpPower){
		mDeviceName = deviceName;
		mBluetoothAddress = bluetoothAddress;
		mSamplingRate = samplingRate;
		mEnabledSensors = enabledSensors;
		mAccelRange = accelRange;
		mGSRRange= gsrRange;
		mShimmerRowNumber = shimmerRowNumber;
		mShimmerVersion = shimmerVersion;
		mGyroRange = gyroRange;
		mMagRange = magRange;
		mLowPowerAccel = lowPowerAccel;
		mLowPowerGyro = lowPowerGyro;
		mLowPowerMag = lowPowerMag;
		mPressureResolution = pressureResolution;
		mIntExpPower = intExpPower;
	}

	public double getSamplingRate() {
		return mSamplingRate;
	}

	public int getShimmerVersion() {
		return mShimmerVersion;
	}

	public void setShimmerVersion(int shimmerVersion) {
		mShimmerVersion = shimmerVersion;
	}

	public void setSamplingRate(double samplingRate) {
		this.mSamplingRate = samplingRate;
	}

	public long getEnabledSensors() {
		return mEnabledSensors;
	}

	public void setEnabledSensors(long enabledSensors) {
		this.mEnabledSensors = enabledSensors;
	}

	public int getAccelRange() {
		return mAccelRange;
	}

	public int getPressureResolution(){
		return mPressureResolution;
	}



	public int getGyroRange() {
		return mGyroRange;
	}

	public int getMagRange() {
		return mMagRange;
	}

	public int getLowPowerAccelEnabled(){
		return mLowPowerAccel;
	}

	public void setLowPowerAccelEnabled(int lowPowerAccel){
		mLowPowerAccel = lowPowerAccel;
	}

	public void setPressureResolution(int setting){
		mPressureResolution = setting;
	}

	public void setLowPowerGyroEnabled(int lowPowerGyro){
		mLowPowerGyro = lowPowerGyro;
	}

	public void setLowPowerMagEnabled(int lowPowerMag){
		mLowPowerMag = lowPowerMag;
	}

	public int getLowPowerGyroEnabled(){
		return mLowPowerGyro;
	}

	public boolean isLowPowerGyroEnabled(){
		if(mLowPowerGyro==1){
			return true;
		} else {
			return false;
		}
	}

	public boolean isLowPowerAccelEnabled(){
		if(mLowPowerAccel==1){
			return true;
		} else {
			return false;
		}
	}

	public boolean isLowPowerMagEnabled(){
		if(mLowPowerMag==1){
			return true;
		} else {
			return false;
		}
	}

	public int getLowPowerMagEnabled(){
		return mLowPowerMag;
	}


	public void setAccelRange(int accelRange) {
		this.mAccelRange = accelRange;
	}

	public int getShimmerRowNumber() {
		return mShimmerRowNumber;
	}

	public void setShimmerRowNumber(int shimmerRowNumber) {
		this.mShimmerRowNumber = shimmerRowNumber;
	}

	public int getGSRRange() {
		return mGSRRange;
	}

	public void setGSRRange(int gsrRange) {
		this.mGSRRange = gsrRange;
	}

	public String getBluetoothAddress() {
		return mBluetoothAddress;
	}

	public void setBluetoothAddress(String bluetoothAddress) {
		this.mBluetoothAddress = bluetoothAddress;
	}

	public String getDeviceName() {
		return mDeviceName;
	}

	public void setDeviceName(String deviceName) {
		this.mDeviceName = deviceName;
	}

	public String getGSRRangeText(){
		String answer="";
		if (mGSRRange==0) {
			answer="GSR Range : 10kOhm to 56kOhm";
		} else if (mGSRRange==1) {
			answer="GSR Range : 56kOhm to 220kOhm";
		} else if (mGSRRange==2) {
			answer="GSR Range : 220kOhm to 680kOhm";
		} else if (mGSRRange==3) {
			answer="GSR Range : 680kOhm to 4.7MOhm"; 
		} else if (mGSRRange==4) {
			answer="GSR Range : Auto Range";
		}	 
		return answer;
	}

	public String getAccelRangeText(){
		String answer="";
		if (mAccelRange == 0){
			answer = "Accel Range : +/- 1.5g";
		} else if (mAccelRange == 3){
			answer = "Accel Range : +/- 6g";
		}
		return answer;

	}

	public void setGyroRange(int range) {
		mGyroRange =range;
	}

	public void setMagRange(int range) {
		mMagRange =range;
	}
	
	public void setIntExpPower(int intExpPower){
		mIntExpPower = intExpPower;
	}
	
	public int getIntExpPower(){
		return mIntExpPower;
	}
}
