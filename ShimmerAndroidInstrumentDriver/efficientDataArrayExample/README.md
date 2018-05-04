# Efficient Data Array Example
This example is only applicable for Shimmer3 devices onwards

It demonstrates the use of the following:
- New arrays data structure - Switching to using the new arrays data structure reduces the processing load on the Android device, which can benefit packet reception rate, especially on older devices.
This example demonstrates how to enable the arrays data structure and retrieve data from it.
- Disabling PC Timestamps -  This improves performance on Android devices, at the cost of slightly less accurate timestamps. 
When this is disabled, the timestamps will be taken on each full packet received, rather than on each byte received. 
- CSV file writing

Note that the performance benefits from the above will only be apparent on lower-end or older Android devices.
