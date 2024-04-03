# Introduction
This API can be used to communicate with a Shimmer3 device via Bluetooth. A good starting point will be the [quick start guide](https://github.com/ShimmerEngineering/ShimmerAndroidAPI/wiki/Quick-Start-Guide) and the [wiki](https://github.com/ShimmerEngineering/ShimmerAndroidAPI/wiki)


# ShimmerAndroidAPI 

The Shimmer Android API is currently in a BETA development state, users are free to use and provide feedback. 

The latest version is [3.0.84Beta](https://github.com/ShimmerEngineering/ShimmerAndroidAPI/releases/tag/AndroidAPI_v3.0.84beta).

We recommend first time users go through the [wiki](https://github.com/ShimmerEngineering/ShimmerAndroidAPI/wiki). 


# Important ~ Migration to Github Packages
With JFrog removing services we have migrated to using Github Packages. This would be a good [starting point](https://docs.github.com/en/packages/learn-github-packages/installing-a-package).
https://github.com/ShimmerEngineering?tab=packages&repo_name=ShimmerAndroidAPI 
with the following above we have made available the last two versions which were on JFrog 3.0.73 and 3.0.74

the following is also relevant that the Shimmer Java Android API is not a public repository
https://github.com/ShimmerEngineering?tab=packages&repo_name=Shimmer-Java-Android-API

# Important ~ Migration to JFrog
With Bintray being sunset, we have migrated to the use of JFrog. Should you face any problems please do not hesitate to contact us. We apologize for any inconvenience caused.

# JFrog Gradle Settings
The gradle file for the example ShimmerBasicExample has been updated accordingly to the following:-

repositories:-

```
maven {
 url 'https://shimmersensing.jfrog.io/artifactory/ShimmerAPI'
}
```
dependencies:-
```
compile(group: 'com.shimmersensing', name: 'ShimmerAndroidInstrumentDriver', version: '3.0.71Beta', ext: 'aar')
implementation (group: 'com.shimmersensing', name: 'ShimmerBluetoothManager', version:'0.9.42beta'){
    // excluding org.json which is provided by Android
    exclude group: 'io.netty'
    exclude group: 'com.google.protobuf'
    exclude group: 'org.apache.commons.math'
}
implementation (group: 'com.shimmersensing', name: 'ShimmerDriver', version:'0.9.138beta'){
    // excluding org.json which is provided by Android
    exclude group: 'io.netty'
    exclude group: 'com.google.protobuf'
    exclude group: 'org.apache.commons.math'
}
```

For further info
https://shimmersensing.jfrog.io/ui/repos/tree/General/ShimmerAPI%2Fcom%2Fshimmersensing%2FShimmerBluetoothManager
https://shimmersensing.jfrog.io/ui/repos/tree/General/ShimmerAPI%2Fcom%2Fshimmersensing%2FShimmerDriver
https://shimmersensing.jfrog.io/ui/repos/tree/General/ShimmerAPI%2Fcom%2Fshimmersensing%2FShimmerAndroidInstrumentDriver

# Bintray Sunset
The following ways of getting the library is **deprecated**
repositories:-

```
 maven {
        url  "http://dl.bintray.com/shimmerengineering/Shimmer"
    }
```
dependencies:-
```
compile 'ShimmerAndroidInstrumentDriver:ShimmerAndroidInstrumentDriver:3.0.69Beta_AA-245_AA-246'
```

# Frequently Asked Questions

### Getting Started with the Shimmer Android API
**1) Clone the repository from GitHub**

You can clone the Android API repository by navigating over to the repository's GitHub main page, and clicking the green "Clone or Download" button on the right.

You then have the option of:

**A) Downloading the repository as a zip file.** 

You would then need to unzip it and import the contents of the unzipped folder into Android Studio as a project.

**B) Copying the link provided.**

You would then need to choose the option "Check out project from Version Control" on the Android Studio start screen. After that, click on "GitHub" and copy the link into the popup dialog.

**2) Importing via Gradle**

The api can also be imported using gradle. Please include the following repository in your build.gradle file 
```
allprojects {
    repositories {
        jcenter()
        maven {
            url  "http://dl.bintray.com/shimmerengineering/Shimmer"
        }
    }
}
```
Most recent uploaded library can be found here:-
https://bintray.com/shimmerengineering/Shimmer/shimmerandroiddriver and should be added as one of the dependencies

*Note: do not download any versions of the library marked as "test". These are internal releases, and may not be fully functional.*

### Common Errors
- **DexArchiveMergerException** *e.g. ':app:transformDexArchiveWithExternalLibsDexMergerForDebug'.>
java.lang.RuntimeException: java.lang.RuntimeException:
com.android.builder.dexing.DexArchiveMergerException: Unable to merge dex*

To resolve this, enable Multidex in your project's build.gradle file:
```
android {
    defaultConfig {
        ...
        multiDexEnabled true
        ...
    }
```

- **Errors with buildToolsVersion and/or MultiDex Incremental**
These options were removed from the build.gradle files as they are unnecessary in the latest version of Android Studio and Gradle Plugin 3.0 and above. Should you encounter issues with these settings, simply add them back in:
```
android {
    ...
    buildToolsVersion x.x.x
    
    dexOptions {
        incremental true
    }
    ...

}
```


# Changelog 
28 February 2020 (3.0.65Beta)
- Fixed bug with Bluetooth Manager Example & Service Example where upon Bluetooth connection, SD logging devices would not show up as connected
- Added getNumShimmersConnected to ConnectedShimmersListFragment 

9 July 2018 (3.0.64Beta)
- Addition of FilesListActivity to display list of files for a folder. Files can be selected to be opened in an external application. An example of how to use the FilesListActivity can be found in Efficient Data Arrays Example.
- Added option to log to CSV file in Logging & ShimmerService
- Examples updated to demonstrate how to receive and display Shimmer status messages (Toast messages)
- Minor refactoring & Javadoc comments

13 June 2018 (3.0.61Beta)
- Added method to get clone device from Device Sensor Config Fragment
- Added check to Device Config List Adapter so it doesn't crash when an invalid config value is returned

12 March 2018 (3.0.56Beta)
- Add 9DOF channels to arrays data structure (only added if 3D Orientation is enabled)
- Bugfix for EMG CH2 disappearing from arrays

22 February 2018 (3.0.51Beta)
- Updated GSR calibration parameters
- GSR signal bug fixed
- Graph Handler in ShimmerService now accessible as a protected variable

14 February 2018 (3.0.47Internal)
- Bug with GSR signal. Fix in progress...
- Added alternative Arrays data structure to ObjectCluster, accessible via sensorDataArray data class.
Enabling this data structure can improve packet reception rate performance significantly, especially on older devices.
- Added method to get Arrays index for a channel, getIndexForChannelName, in ObjectCluster
- Updated getFormatClusterValue in ObjectCluster to return Arrays data if Arrays data structure is enabled
- Refactored Shimmer Basic Example name to Bluetooth Manager Example
- Added new Shimmer Basic Example, demonstrating connecting and streaming from Shimmer without Bluetooth Manager
- Added new Efficient Data Array Example, demonstrating the use of the alternative Arrays data structure
- Cleaned up build.gradle files for all project modules

(Beta) Rev 3.00
- Support for Shimmer3 devices using updated IMU sensors
- Change to Handler MSGs

| Deprecated  | Updated |
| ------------- | ------------- |
| ~~Shimmer.MESSAGE_STATE_CHANGE~~  | ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE  |
| ~~Shimmer.MESSAGE_READ~~  | ShimmerBluetooth.MSG_IDENTIFIER_DATA_PACKET  |

- ~~Shimmer.MSG_STATE_FULLY_INITIALIZED~~, ~~Shimmer.STATE_CONNECTING~~, ~~Shimmer.STATE_NONE~~, ~~Shimmer.STATE_CONNECTED~~, is deprecated the following code is the recommended use for monitoring the states of a Shimmer device
```
case ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE:
                    ShimmerBluetooth.BT_STATE state = null;
                    String macAddress = "";
                    if (msg.obj instanceof ObjectCluster) { //ONLY APPLICABLE FOR ANDROID
                        state = ((ObjectCluster) msg.obj).mState;
                        macAddress = ((ObjectCluster) msg.obj).getMacAddress();
                    } else if (msg.obj instanceof CallbackObject) {
                        state = ((CallbackObject) msg.obj).mState;
                        macAddress = ((CallbackObject) msg.obj).mBluetoothAddress;
                    }

                   switch (state) {
                        case CONNECTED:
                            break;
                        case CONNECTING:
                            break;
                        case STREAMING:
                            break;
                        case STREAMING_AND_SDLOGGING:
                            break;
                        case SDLOGGING:
                            break;
                        case DISCONNECTED:
                            break;
                    }
                    break;

```


Rev 2.11
- 3 byte timestamp support

4 June 2015 (Beta 2.10)
 - fixes to MultiShimmerTemaplate and ShimmerGraphandLogService (A0 and A7)
 - update to filter (BSF) --> coefficients[(nTaps/2)] = coefficients[(nTaps/2)] +1;

25 May 2015 (Beta 2.9)
 - fix to shimmersetsamplingrate method
 - clean up to tcpip example (ShimmerTCPExample/ShimmerPCTCPExample/ShimmerTCPReceiver)

20 May 2015 (Beta 2.8)
  - updated filter/ecgtohr/ppgtohr algorithms
  - ecgtohr algorithm examples added to MultiShimmerTemplate (Android) and ShimmerCapture (PC)
  - various bug fixes to ShimmerCapture and ShimmerConnect
  - updates to GSR coefficients
  
20 October 2014 (Beta 2.7)
- support for LogAndStream Firmware
- support for Baud Rate modification
- support for reading the expansion board
- minor bugs fixed
- moved logging and plotting functionality to the Android Instrument Driver.
- added functionalities for MSS API
04 July 2014 (Beta 2.6)
- Support for Shimmer3 bridge amplifier
- added getExGConfiguration methods
- New Shimmer initialize. Now: get HW Version -> get FW Version -> Initialization
- Firmware Version divide into Major Firmware Version and Minor Firmware Version
- Firmware code added in order to identify the features in the different firmwares
- Change Firmware Version checks, now is Firmware Code checks
- Configurable filter, see Filter.java : 1.Low Pass, 2.High Pass, 3.Band Pass, 4.Band Stop.
- Sensor conflict handling for Shimmer3
- Shimmer PPG->HR jar v0.4 
- Updated 3dorientation to work with wide range accel, defaults to low noise if both are enabled
- Added DataProcessing interface
- Moved response timer to ShimmerBluetooth
- Bug fix to allow continual use of gerdavax library if needed
- Added support for BlueCove, see ShimmerConnectBCove

23 Jan 2014 (Beta 2.2)
- Separate the drivers into Shimmer Android Instrument Driver and Shimmer Driver, please read user guide for further details.

15 March 2013 (Beta 0.7)
- a more accurate GSR calibration method is implemented. the polynomial equation is replaced with a linear one.
- the state STATE_CONNECTED is now deprecated. users should use MSG_STATE_FULLY_INITIALIZED as a replacement.
- note that since the Android 4.2 update, Google has changed the Bluetooth Stack. It has been well documented that the reliability of the stack is less than desirable.
- Battery voltage monitoring is now enabled, see manual guideline for further information.
- Definition of the properties, formats and units, used, have been updated to be more concise and consistent across the various instrument drivers
- For further changes please refer to the Shimmer.java file, which can be found as part of the instrument driver

27 September 2012 (Beta 0.6)
- Additional handler msgs for stop streaming has been added.
- An optional AndroidBluetoothLibrary has been added. Please see license. The reason for this is because some stock firmware were not providing the full bluetooth stack. If bluetooth problems persist please consider using an aftermarket firmware.
- The connect method has been modified to accomodate the new library. Examples have been updated as well.
- Object Cluster now accepts the bluetooth id of the device
- Stop Streaming Command which was failing to receive an ACK intermittently has been fixed.
- createInsecureRfcommSocketToServiceRecord can be used for insecure connections (only for the default library). Benefit is that you wont have to key in the pin everytime you connect to a Shimmer device, which is a requirment for some Android devices. 
- the 'Nan' bug has been fixed, this bug occurs when the Shimmer device attempts to use the default calibration parameters

18 July 2012 (Beta 0.5)
- ShimmerGraph has been updated to deal with the following warning message 'the following handler class should be static or leaks might occur'

18 July 2012 (Beta 0.4)
- Added com.shimmerresearch.tools which has a Logging class (logs data onto the Android device)
- Fixed a bug with function retrievecalibrationparametersfrompacket

10 July 2012 (Beta 0.3b)
- Added locks to ensure commands can only be transmitted after the previous one is finished
- Updated the command transmission timeout

4 July 2012 (Beta 0.3)
- Fix a bug with the start streaming command which was failing intermittently 
- Rename ShimmerHandleClass to Shimmer

26 June 2012 (Beta 0.2)
-Added additional log messages to clearly show the communication transactions between the Shimmer and Android device
-Added an addtional constructor, which allows the android device to set Shimmer settings as soon as connection is made
-Manual updated explaining communication transactions and the use of the constructors

 # The following license applies to the source code within this repository/project.
Copyright (c) 2017, Shimmer Research, Ltd. All rights reserved

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above
   copyright notice, this list of conditions and the following
   disclaimer in the documentation and/or other materials provided
   with the distribution.
 * Neither the name of Shimmer Research, Ltd. nor the names of its
   contributors may be used to endorse or promote products derived
   from this software without specific prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
