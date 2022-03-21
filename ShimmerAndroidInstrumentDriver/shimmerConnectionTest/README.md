# Shimmer Connection Test Example
This example is only applicable for Shimmer3 devices onwards. 

This shows how to implement the retry logic for a successful connection on the first try. Note the important part are that a new shimmer instance be created everytime an attempt to connect is made (_this also applies if you aren't using the Bluetooth Manager_). Also note that the method setMacIdFromUart is called in order to maintain functionality of the BluetoothManager (_e.g. public void startStreaming(String bluetoothAddress)_)
```
Shimmer shimmer = new Shimmer(mHandler);
shimmer.setMacIdFromUart(macAdd);
btManager.removeShimmerDeviceBtConnected(macAdd);
btManager.putShimmerGlobalMap(macAdd, shimmer);
