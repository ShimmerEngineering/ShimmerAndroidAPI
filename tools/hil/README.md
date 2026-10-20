# Android HIL: the Android API on real Shimmers

Hardware-in-the-loop tests of the Android API. A phone on USB connects to real Shimmer3 and Shimmer3R
units, over Bluetooth Classic, BLE or both, through the same public API an app uses
(`ShimmerBluetoothManagerAndroid`, `ShimmerBluetooth`). One command builds the tests, installs them,
runs every scenario on every unit and transport, pulls the reports and prints pass or fail.

**Both transports matter, because on Android they are different code.**

| Transport | Device class | Parser | Pairing |
|---|---|---|---|
| Classic | the Android API's own `Shimmer` | the API's own copy, overriding the driver's | needed once |
| BLE | `Shimmer3BLEAndroid` | the Java driver's | none |

A defect in one transport says nothing about the other. DEV-1148 is Classic only, for example: the
driver has had its fix, DEV-1147, since 0.11.9_beta.

```powershell
.\tools\hil\run-android-hil.ps1
```

It exits 0 when everything passed, 1 when something failed, and 2 when the bench isn't set up.

## Before every Android release (the gate)

**Release Android API** won't publish a commit that hasn't passed this on hardware. Each round:

1. Release the Java API, if the driver changed.
2. On the bench, on `master` with a clean working tree and every unit in the rig, run:
   ```powershell
   git checkout master; git pull
   .\tools\hil\run-android-hil.ps1 -Report
   ```
   With `-Report`, the result is posted to GitHub as the commit status
   **`android-hil (driver X)`** on that commit: passed or failed, with the phone and each unit's
   firmware. It refuses to report a partial run or a run of uncommitted code:
   - every scenario, at least one Shimmer3 and one Shimmer3R, and both transports, Classic and BLE;
   - the default rate, duration, link CRC and loss limit, and a fresh build (no `-SkipBuild`);
   - this checkout's library source;
   - a clean working tree, pushed.
3. Run **Release Android API** with the same driver (blank picks the newest, as the script does).
   It looks up that status for the commit it's releasing and the driver it builds against, and
   stops before building anything unless it passed. The release notes then say what was tested.

If anything merges after step 2, the commit changes, so test again. In an emergency, put a Jira key
in **hil_waiver** to release without a pass; the release notes record the waiver. Dry runs only warn.

To find regressions before release day, schedule step 2 nightly with Windows Task Scheduler, without
`-Report`.

## What it tests

| Scenario | What it does | Fails when |
|---|---|---|
| `streamRest` | Connects, writes an accel-only config at `-Rate` (default 256 Hz), streams for `-Seconds` (default 30) with the unit lying still, then stops and disconnects. | It can't connect or configure; more than `-MaxLossPercent` (1 %) of packets are lost; a timestamp doesn't increase, or leaps more than a second ahead of when it arrived (a bad unwrap, not loss); the effective rate is more than 1 % off the configured one; accel is more than 1 m/s² from 9.81; or the API logs a "Packet syncing problem". |
| `midStreamSet` | While streaming, starts and stops SD logging and reads the battery, three times. | Logging doesn't start or stop (the DEV-1148 symptom with a link CRC on); a packet is lost around a battery read, or more than `-MaxLossPercent` away from the SD transitions; or there's a parser error. Packets missing around SD logging start/stop are reported, not failed: the unit itself pauses while it opens its file, which is for the firmware HIL bench to judge. Needs an SD card in the unit. |
| `reconnectLoop` | Connects, streams for 5 s and disconnects, five times. | Any cycle fails to connect within 30 s, or loses packets or logs a parser error. |

Every scenario also fails if an API call doesn't return within 10 s. The report then holds every thread's stack, which a phone without root can't otherwise give, and the scenario stops. If the whole test process still never finishes, the runner force-stops it after `-ScenarioTimeoutSeconds` (600) and records TIMEOUT.

The packet checks use the device's own timestamps, so a loss is a gap in the device's sequence, not
phone scheduling. They follow the firmware HIL harness in log-and-stream-common `Test/hil` (DEV-1165),
so this can later run there as a tier.

### What it does not test
Over BLE the tests run the driver's own streaming parser, the one the PC and MATLAB use. Over Classic
they run the Android API's copy of it (`Shimmer.java` overrides `processPacket`,
`processWhileStreaming`, `processInstreamResponse`, `clearSingleDataPacketFromBuffers`, `readBattery`
and `readStatusLogAndStream`). Neither covers:

- `shimmerdriverpc`: the serial port, BLE gRPC, and Shimmer3R USB;
- the SD and file parsers.

Both cover the driver's connect sequence, configuration, calibration, decoding and timestamp unwrap,
since Android uses those unchanged.

## Choosing what to test

| | Default | Option |
|---|---|---|
| Android API | this checkout's library source | `-AndroidApiVersion 3.2.10_beta`: the published `.aar`, as a customer app gets it |
| Java driver | the newest `API_v` release | `-DriverVersion 0.11.8_beta` |
| Units | every unit in the rig file | `-Unit s3r-15bc` |
| Transports | every transport each unit lists | `-Transport ble` |
| Scenarios | all three | `-Scenario midStreamSet` |
| Link CRC | ask for 2 bytes (the driver may settle on 1 or off) | `-Crc off`, `-Crc 1` |
| Pause between runs | 10 s | `-SettleSeconds 60` |
| Record the result for the release gate | no | `-Report` (full run on a clean, pushed commit only) |

## One-time setup

1. **Phone.** Turn on Developer options, then USB debugging, and "Stay awake while charging".
   Connect it to the PC and accept the debugging prompt. On Xiaomi phones also turn on "Install via
   USB". On Android 12 and later also turn on "USB debugging (Security settings)", which lets the
   tests be granted `BLUETOOTH_CONNECT`. Up to Android 11 nothing needs granting: connecting to a
   paired unit uses install-time permissions only.
2. **Pair each unit you test over Classic** with the phone, in Settings → Bluetooth, PIN `1234`. BLE
   needs no pairing. A Shimmer3R usually bonds over BLE instead (DEV-970). The phone then reaches
   the unit, but the serial channel is refused, and the Classic test reports that it "did not
   connect" within 2 s. Unpair it, then either pair again with the unit's BLE turned off, or connect
   once from a classic serial-terminal app so that Android makes a Bluetooth Classic bond. Until
   then, test that Shimmer3R over BLE only.
3. **Prepare each unit.**
   - **SD sync and Bluetooth disable off.** Either one keeps Bluetooth off (DEV-1228). Check in
     Consensys.
   - **SD card in,** for `midStreamSet`.
   - **Lying still** during `streamRest`.
4. **List the units:** copy `rig-android.example.json` to `rig-android.json` and fill in each name,
   MAC, `type` (`Shimmer3` or `Shimmer3R`) and `transports` (`["classic"]`, `["ble"]` or both;
   Classic if left out). That file is gitignored. You can read the MACs of paired units off the
   phone:
   ```powershell
   adb shell dumpsys bluetooth_manager | Select-String -Context 0,6 'Bonded devices'
   ```
5. **Credentials for GitHub Packages,** where the driver comes from. Either set `gpr.usr` and
   `gpr.key` in `%USERPROFILE%\.gradle\gradle.properties`, or let the script use your `gh` login
   after granting it the packages scope once:
   ```powershell
   gh auth refresh -h github.com -s read:packages
   ```
6. **JDK 17 or newer.** The script uses `JAVA_HOME` if it qualifies, otherwise the JDK Android Studio
   ships in its `jbr` folder. It also finds the Android SDK (`ANDROID_HOME`, or Android Studio's default
   location) when the checkout has no `local.properties`.

A Shimmer holds one Bluetooth link at a time. Don't run this while Consensys, the PC HIL harness or
another phone is connected to the same unit.

## Output

The run's folder, `tools/hil/out/<timestamp>/`, holds:
- `summary.json` and the table the script prints;
- `reports/`: per scenario and unit, a JSON report (versions, CRC mode actually in use, loss,
  rate, accel, parser errors, a timeline of events and the failures) and, for `streamRest`, a CSV of
  every sample;
- `*.logcat.txt` and `*.instrument.txt`: the phone's log and the test runner's output for each run;
- `build.log`.

## Running a single test by hand

```powershell
cd ShimmerAndroidInstrumentDriver
.\gradlew :shimmerHilTest:assembleDebug :shimmerHilTest:assembleDebugAndroidTest -PdriverVersion=0.11.9_beta -Pandroid.useAndroidX=true
adb install -r -g shimmerHilTest\build\outputs\apk\debug\shimmerHilTest-debug.apk
adb install -r -g shimmerHilTest\build\outputs\apk\androidTest\debug\shimmerHilTest-debug-androidTest.apk
adb shell am instrument -w -e class com.shimmerresearch.hiltest.HilScenariosTest#streamRest -e mac 00:06:66:XX:XX:XX com.shimmerresearch.hiltest.test/androidx.test.runner.AndroidJUnitRunner
```

Add `-e transport ble -e type Shimmer3R` to run the same test over BLE against a Shimmer3R.

Without `-e mac`, every test is skipped. That's how CI can compile the tests without hardware.
