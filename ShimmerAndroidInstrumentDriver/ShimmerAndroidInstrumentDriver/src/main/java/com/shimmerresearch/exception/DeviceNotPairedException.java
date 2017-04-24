package com.shimmerresearch.exception;


/**
 * Created by ASaez on 07-Oct-16.
 */

public class DeviceNotPairedException extends RuntimeException{

    String btAddress;

    public DeviceNotPairedException(String btAddress){
        super();
        this.btAddress = btAddress;
    }

    public DeviceNotPairedException(String btAddress, String message){
        super(message);
        this.btAddress = btAddress;
    }

    public DeviceNotPairedException(String btAddress, String message, Throwable cause){
        super(message, cause);
        this.btAddress = btAddress;
    }

    public String getBluetoothAddress(){
        return btAddress;
    }
}
