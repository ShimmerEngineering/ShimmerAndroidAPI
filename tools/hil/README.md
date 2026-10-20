# Android HIL: the Android API on real Shimmers

Hardware-in-the-loop tests of the Android API. A phone on USB connects to real Shimmer3 and Shimmer3R
units over Bluetooth Classic, through the same public API an app uses
(`ShimmerBluetoothManagerAndroid`, `ShimmerBluetooth`). One command builds the tests, installs them,
runs every scenario against every unit, pulls the reports and prints pass or fail.

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
   - every scenario, at least one Shimmer3 and one Shimmer3R;
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
| `streamRest` | Connects, writes an accel-only config at `-Rate` (default 256 Hz), streams for `-Seconds` (default 30) with the unit lying still, then stops and disconnects. | It can't connect or configure; more than `-MaxLossPercent` (1 %) of packets are lost; a timestamp doesn't increase; the effective rate is more than 1 % off the configured one; accel is more than 1 m/s² from 9.81; or the API logs a "Packet syncing problem". |
| `midStreamSet` | While streaming, starts and stops SD logging and reads the battery, three times. | Any packet is lost within 1 s of a command; logging doesn't start or stop; or there's a parser error. It's the DEV-1148 regression and needs a link CRC on and an SD card in the unit. |
| `reconnectLoop` | Connects, streams for 5 s and disconnects, five times. | Any cycle fails to connect within 30 s, or loses packets or logs a parser error. |

The packet checks use the device's own timestamps, so a loss is a gap in the device's sequence, not
phone scheduling. They follow the firmware HIL harness in log-and-stream-common `Test/hil` (DEV-1165),
so this can later run there as a tier.

### What it does not test
The Android API replaces the driver's streaming parser with its own (`Shimmer.java` overrides
`processPacket`, `processWhileStreaming`, `processInstreamResponse`, `clearSingleDataPacketFromBuffers`,
`readBattery` and `readStatusLogAndStream`). So a pass here doesn't cover:

- the driver's own versions of those, which the PC and MATLAB use;
- `shimmerdriverpc`: the serial port, BLE gRPC, and Shimmer3R USB;
- the SD and file parsers.

It does cover the driver's connect sequence, configuration, calibration and decoding, since Android
uses those unchanged.

## Choosing what to test

| | Default | Option |
|---|---|---|
| Android API | this checkout's library source | `-AndroidApiVersion 3.2.10_beta`: the published `.aar`, as a customer app gets it |
| Java driver | the newest `API_v` release | `-DriverVersion 0.11.8_beta` |
| Units | every unit in the rig file | `-Unit s3r-15bc` |
| Scenarios | all three | `-Scenario midStreamSet` |
| Link CRC | ask for 2 bytes (the driver may settle on 1 or off) | `-Crc off`, `-Crc 1` |
| Record the result for the release gate | no | `-Report` (full run on a clean, pushed commit only) |

## One-time setup

1. **Phone.** Turn on Developer options, then USB debugging, and "Stay awake while charging". On
   Xiaomi phones also turn on "Install via USB" and "USB debugging (Security settings)": without
   them, installing or granting permissions over USB fails. Connect it to the PC and accept the
   debugging prompt.
2. **Pair each unit with the phone over Bluetooth Classic,** in Settings → Bluetooth, PIN `1234`.
   A Shimmer3R can end up bonded over BLE instead (DEV-970); unpair it and pair again if it does.
3. **Prepare each unit.**
   - **SD sync and Bluetooth disable off.** Either one keeps Bluetooth off (DEV-1228). Check in
     Consensys.
   - **SD card in,** for `midStreamSet`.
   - **Lying still** during `streamRest`.
4. **List the units:** copy `rig-android.example.json` to `rig-android.json` and fill in each
   name and MAC. That file is gitignored. You can read the MACs off the phone:
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
   ships in its `jbr` folder.

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

Without `-e mac`, every test is skipped. That's how CI can compile the tests without hardware.
