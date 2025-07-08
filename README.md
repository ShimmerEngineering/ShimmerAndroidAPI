# Introduction
This API can be used to communicate with a Shimmer3 device via Bluetooth. A good starting point will be the [quick start guide](https://github.com/ShimmerEngineering/ShimmerAndroidAPI/wiki/Quick-Start-Guide) and the [wiki](https://github.com/ShimmerEngineering/ShimmerAndroidAPI/wiki). Also other important resource will be the [start guide for Android Studio 2024.2.1 Patch 2](https://github.com/ShimmerEngineering/ShimmerAndroidAPI/wiki/Guide-for-Android-Studio-2024.2.1-Patch-2).


# ShimmerAndroidAPI 

The Shimmer Android API is currently in a BETA development state, users are free to use and provide feedback. 

The latest version can be found [here](https://github.com/ShimmerEngineering/ShimmerAndroidAPI/releases).

We recommend first time users go through the [wiki](https://github.com/ShimmerEngineering/ShimmerAndroidAPI/wiki). 

# Shimmer3R

Integration [notes](https://github.com/ShimmerEngineering/ShimmerAndroidAPI/wiki/Shimmer3R-Integration-Notes)

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






