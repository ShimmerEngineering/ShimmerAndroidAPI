# Shimmer Basic Example
This example is only applicable for Shimmer3 devices onwards. For Shimmer2 devices, please see the Legacy Example

- This example demonstrates connecting and streaming data from a Shimmer directly, without the use of the Bluetooth Manager
- The build.gradle file shows how to retrieve the library from bintray. Take note of the following:-

```
compile 'ShimmerAndroidInstrumentDriver:ShimmerAndroidInstrumentDriver:3.0.65Beta'
```

- Note you will need to specify the url for the shimmer bintray repository, this is done in the build.gradle file (root projects folder)
```
allprojects {
    repositories {
        google()
        jcenter()
        maven {
            url  "http://dl.bintray.com/shimmerengineering/Shimmer"
        }
    }
}
```