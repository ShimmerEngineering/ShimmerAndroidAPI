# ShimmerAndroidAPI

Android wrapper over the Java driver, plus eleven example apps. Public repo; BETA per its README.

## It wraps the driver — except for the Bluetooth stream parser

`Shimmer` extends `ShimmerBluetooth`, `Shimmer4Android` extends `Shimmer4sdk`, and
`VerisenseDeviceAndroid` extends `VerisenseDevice` — all from `Shimmer-Java-Android-API`. Apart from
the parser below, what lives here is Android-specific: radio/transport drivers
(`androidradiodriver/`), a foreground `ShimmerService`, GUI fragments and plotting.

**`Shimmer.java` carries its own copy of the Bluetooth stream parser.** It overrides
`processPacket()`, `processWhileStreaming()`, `processInstreamResponse()` and
`clearSingleDataPacketFromBuffers()` from `ShimmerBluetooth`, and the copy has drifted: it resyncs a
byte at a time where the driver skips to the next packet. So a streaming-parse fix in the driver
never reaches `Shimmer`, even after a republish and pin bump (below). Check every driver parser fix
against `Shimmer.java`, and expect to port it.

Packet decoding, calibration and timestamps (`processDataPacket()` → `buildMsg()`) and `checkCrc()`
are still the driver's: a fix there belongs upstream and needs no port. `Shimmer3BLEAndroid`
overrides only BLE transport, state and callbacks, so it uses the driver's parser.

## But upstream fixes do not arrive for free

The driver comes in as **published Maven artifacts pinned to an exact version**, from GitHub
Packages — not as source, a submodule or a sibling project. The pin is in the repo-root
`ShimmerAndroidInstrumentDriver/build.gradle`, injected into *every* subproject:

```groovy
allprojects { subprojects { afterEvaluate { dependencies {
    implementation (group: 'com.shimmerresearch', name: 'shimmerbluetoothmanager', version:'0.11.7_beta')
    implementation (group: 'com.shimmerresearch', name: 'shimmerdriver',           version:'0.11.7_beta')
} } } }
```

So an upstream fix reaches this repo only when it is **published and the pin is bumped**. As of
2026-09-30 that has not kept up:

| | pinned here | source in Shimmer-Java-Android-API | published |
|---|---|---|---|
| `shimmerdriver` | 0.11.7_beta | 0.11.8_beta | **0.11.8_beta** (2026-08-05) |
| `shimmerbluetoothmanager` | 0.11.7_beta | 0.11.8_beta | 0.11.7_beta only — **0.11.8 was never published** |

Bumping the pin therefore needs `shimmerbluetoothmanager` 0.11.8_beta published first. Check both
before assuming a driver fix is live on Android.

## Build

**Build from `ShimmerAndroidInstrumentDriver/`, not the repo root** — that is where `settings.gradle`
is. A same-named folder nests inside it (`ShimmerAndroidInstrumentDriver/ShimmerAndroidInstrumentDriver/`)
and is the library module, not the build root.

```
cd ShimmerAndroidInstrumentDriver && ./gradlew :ShimmerAndroidInstrumentDriver:assembleRelease
```

AGP 8.2.2, Gradle 8.5, Java 11, `compileSdk`/`targetSdk` 33, `minSdk` 14.

Both wrapper directories carry a wrapper, at **different versions** — 8.5 at the build root and
8.14.3 in the module. Gradle walks upward for `settings.gradle`, so invoking the inner one still
builds the same project with a different Gradle. Use the build root's.

### A token is required before anything resolves

The driver artifacts are private packages, so Gradle needs a GitHub PAT with **`read:packages`**.
Put it in `~/.gradle/gradle.properties` (not in the repo):

```
gpr.usr=<github-username>
gpr.key=<personal-access-token>
```

Without it the build fails at dependency resolution, which reads like a network or repository fault
rather than a missing credential. Note the property is `gpr.usr` — a commented-out block in the
module's `build.gradle` says `gpra.usr`, which is dead code.

## No CI

There are no workflows. Nothing here is built on push or PR, so a change that does not compile can
merge. Build locally before claiming it works.

## Modules

`ShimmerAndroidInstrumentDriver` is the library. The rest are examples, and are the practical
documentation: `shimmerBasicExample`, `bluetoothManagerExample`, `shimmerServiceExample`,
`shimmerLegacyExample`, `shimmer3BLEBasicExample`, `verisenseBLEBasicExample`,
`multiverisenseblebasicexample`, `efficientDataArrayExample`, `shimmer3DOrientationExample`,
`shimmerConnectionTest`, `shimmerspeedtest`.

`libs/` holds three checked-in jars — `ShimmerBiophysicalProcessingLibrary_Rev_0_11.jar` (closed
source), `AndroidBluetoothLibrary.jar` and `androidplot-core-0.5.0`.

## README links point at a dead org

Every link in the README — wiki, releases, quick-start, Shimmer3R notes — targets
`ShimmerEngineering/ShimmerAndroidAPI`. That org no longer exists; the links survive only on
GitHub's rename redirect. Repoint them if you are editing the README anyway.
