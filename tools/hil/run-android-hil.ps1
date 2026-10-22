<#
.SYNOPSIS
Runs the Android API hardware-in-the-loop (HIL) scenarios on a USB-attached phone against every
Shimmer in a rig file, pulls the reports and prints a pass/fail table. Exits 1 if anything failed.

.DESCRIPTION
One-time setup is in tools/hil/README.md: list each unit, with the transports to test it over, in
tools/hil/rig-android.json (copy rig-android.example.json), pair it with the phone if it is tested over
Bluetooth Classic, and give Gradle a token that can read GitHub Packages.

.EXAMPLE
.\tools\hil\run-android-hil.ps1
Every scenario on every unit, against the newest Java API (driver) release and this checkout's library.

.EXAMPLE
.\tools\hil\run-android-hil.ps1 -AndroidApiVersion 3.2.10_beta -DriverVersion 0.11.9_beta
The published .aar, as a customer app gets it.

.EXAMPLE
.\tools\hil\run-android-hil.ps1 -Scenario midStreamSet -Unit s3r-15bc -Crc 1

.EXAMPLE
.\tools\hil\run-android-hil.ps1 -Report
Before "Release Android API": the full run on this commit, recorded on GitHub as the commit status
"android-hil (driver X)". The release refuses to publish that commit with driver X without a passing one.
#>
param(
    [string]$Rig = (Join-Path $PSScriptRoot 'rig-android.json'),
    [ValidateSet('streamRest', 'midStreamSet', 'reconnectLoop', 'inStream', 'connectReady', 'resetUnit')]
    [string[]]$Scenario = @('streamRest', 'midStreamSet', 'reconnectLoop', 'inStream', 'connectReady'),
    [string[]]$Unit,
    [ValidateSet('classic', 'ble')]
    [string[]]$Transport,
    [string]$DriverVersion,
    [string]$AndroidApiVersion,
    [string]$Serial,
    [double]$Rate = 256,
    [int]$Seconds = 30,
    [ValidateSet('off', '1', '2')]
    [string]$Crc = '2',
    [double]$MaxLossPercent = 1.0,
    [switch]$SkipBuild,
    [switch]$Report,
    [int]$ScenarioTimeoutSeconds = 600,
    # Pause between runs, so the unit has advertised again and the phone's Bluetooth stack has let go
    # of the last connection before the next one starts.
    [int]$SettleSeconds = 10
)

$ErrorActionPreference = 'Continue'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$gradleRoot = Join-Path $repo 'ShimmerAndroidInstrumentDriver'
$package = 'com.shimmerresearch.hiltest'
$runner = "$package.test/androidx.test.runner.AndroidJUnitRunner"
$deviceReports = "/sdcard/Android/data/$package/files/hil"

function Fail([string]$message) {
    Write-Host "ERROR: $message" -ForegroundColor Red
    exit 2
}

# --- Rig ---------------------------------------------------------------------------------------
if (-not (Test-Path $Rig)) {
    Fail "No rig file at $Rig. Copy tools\hil\rig-android.example.json to rig-android.json and list your units."
}
$rigData = Get-Content $Rig -Raw | ConvertFrom-Json
$units = @($rigData.units)
if ($Unit) { $units = @($units | Where-Object { $Unit -contains $_.name }) }
if ($units.Count -eq 0) { Fail "No units to test (rig: $Rig, -Unit: $Unit)." }
if (-not $Serial -and $rigData.phone) { $Serial = $rigData.phone }

# Each unit runs over the transports it lists ("classic" needs it paired with the phone; "ble"
# doesn't). They are different code: Classic goes through the Android API's own Shimmer class and
# parser, BLE through Shimmer3BLEAndroid and the driver's parser.
$targets = @(foreach ($u in $units) {
    $unitTransports = @($u.transports)
    if (-not $u.transports) { $unitTransports = @('classic') }
    foreach ($t in $unitTransports) {
        if ($t -ne 'classic' -and $t -ne 'ble') { Fail "Unit $($u.name): transport '$t' is not classic or ble." }
        if (-not $Transport -or $Transport -contains $t) { [pscustomobject]@{ Unit = $u; Transport = $t } }
    }
})
if ($targets.Count -eq 0) { Fail "Nothing to run: no unit lists the transport(s) $($Transport -join ', ')." }

# --- adb ---------------------------------------------------------------------------------------
$sdk = $env:ANDROID_HOME
if (-not $sdk) { $sdk = $env:ANDROID_SDK_ROOT }
if (-not $sdk) { $sdk = Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
$adbExe = Join-Path $sdk 'platform-tools\adb.exe'
if (-not (Test-Path $adbExe)) { Fail "adb not found at $adbExe. Set ANDROID_HOME." }
$adbTarget = @()
if ($Serial) { $adbTarget = @('-s', $Serial) }
function Adb { & $adbExe @adbTarget @args }

$phones = @(& $adbExe devices | Select-String '\tdevice$' | ForEach-Object { ($_ -split "`t")[0] })
if (-not $Serial -and $phones.Count -ne 1) {
    Fail "Expected one phone on adb, found $($phones.Count): $($phones -join ', '). Pass -Serial."
}
$phoneModel = (Adb shell getprop ro.product.model) -join ''
$phoneAndroid = (Adb shell getprop ro.build.version.release) -join ''

# --- Versions ----------------------------------------------------------------------------------
if (-not $DriverVersion) {
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    $releases = Invoke-RestMethod 'https://api.github.com/repos/ShimmerResearch/Shimmer-Java-Android-API/releases?per_page=100'
    $newest = $releases | Where-Object { $_.tag_name -like 'API_v*' } | Sort-Object created_at -Descending | Select-Object -First 1
    if (-not $newest) { Fail 'Could not find an API_v release; pass -DriverVersion.' }
    $DriverVersion = $newest.tag_name.Substring(5)
}
$apiLabel = $AndroidApiVersion
if (-not $apiLabel) { $apiLabel = 'source ' + ((git -C $repo rev-parse --short HEAD) -join '') }

# --- What -Report vouches for -----------------------------------------------------------------
# The release gate trusts this status, so only report a run that tested exactly what the release
# would build: this commit's library source, unmodified and pushed, every scenario, both device types.
$commit = $null
$githubRepo = $null
if ($Report) {
    if ($AndroidApiVersion) { Fail '-Report tests the library source at a commit; drop -AndroidApiVersion.' }
    if ($PSBoundParameters.ContainsKey('Scenario') -or $Unit -or $Transport) {
        Fail '-Report needs every scenario on every unit and transport in the rig; drop -Scenario, -Unit and -Transport.'
    }
    # The gate's pass criteria are the defaults; a run that changed them, or reused an old build, is
    # not evidence about this commit.
    $shaping = @('Rate', 'Seconds', 'Crc', 'MaxLossPercent', 'SkipBuild') | Where-Object { $PSBoundParameters.ContainsKey($_) }
    if ($shaping) {
        Fail "-Report runs with the default settings and a fresh build; drop $((@($shaping) | ForEach-Object { "-$_" }) -join ', ')."
    }
    $types = @($units | ForEach-Object { $_.type })
    if (-not ($types -contains 'Shimmer3') -or -not ($types -contains 'Shimmer3R')) {
        Fail "-Report needs at least one Shimmer3 and one Shimmer3R in the rig (types found: $($types -join ', '))."
    }
    $transportsCovered = @($targets | ForEach-Object { $_.Transport }) | Sort-Object -Unique
    if (-not ($transportsCovered -contains 'classic') -or -not ($transportsCovered -contains 'ble')) {
        Fail "-Report needs both transports in the rig, Classic and BLE: they run different parsers (found: $($transportsCovered -join ', '))."
    }
    $dirty = (git -C $repo status --porcelain) -join "`n"
    if ($dirty) { Fail "-Report needs a clean working tree, so the tested code is exactly the commit:`n$dirty" }
    $commit = ((git -C $repo rev-parse HEAD) -join '').Trim()
    $origin = ((git -C $repo remote get-url origin) -join '').Trim()
    if ($origin -notmatch 'github\.com[:/](.+?)(\.git)?$') { Fail "origin is not a GitHub remote: $origin" }
    $githubRepo = $Matches[1]
    if (-not (Get-Command gh -ErrorAction SilentlyContinue)) { Fail '-Report posts the result with gh; install it and log in.' }
    gh api "repos/$githubRepo/commits/$commit" --silent 2>$null
    if ($LASTEXITCODE -ne 0) { Fail "Commit $commit is not on GitHub ($githubRepo). Push it first: the release checks the commit it builds." }
}

$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$outDir = Join-Path $PSScriptRoot "out\$stamp"
New-Item -ItemType Directory -Force $outDir | Out-Null

Write-Host "Android HIL: $phoneModel (Android $phoneAndroid), Android API $apiLabel, driver $DriverVersion"
Write-Host "Results: $outDir"

# --- Build -------------------------------------------------------------------------------------
$apk = Join-Path $gradleRoot 'shimmerHilTest\build\outputs\apk\debug\shimmerHilTest-debug.apk'
$testApk = Join-Path $gradleRoot 'shimmerHilTest\build\outputs\apk\androidTest\debug\shimmerHilTest-debug-androidTest.apk'
if (-not $SkipBuild) {
    # Gradle needs JDK 17 or newer (AGP 8). Use JAVA_HOME if it qualifies, else a JDK Android Studio ships.
    $saved = @{ JAVA_HOME = $env:JAVA_HOME; ANDROID_HOME = $env:ANDROID_HOME
        GPR_USR = ${env:ORG_GRADLE_PROJECT_gpr.usr}; GPR_KEY = ${env:ORG_GRADLE_PROJECT_gpr.key} }
    try {
        # A fresh checkout has no local.properties (Android Studio writes it), so tell Gradle where
        # the SDK is: the one adb came from.
        if (-not $env:ANDROID_HOME -and -not $env:ANDROID_SDK_ROOT -and -not (Test-Path (Join-Path $gradleRoot 'local.properties'))) {
            $env:ANDROID_HOME = $sdk
        }
        $javaOk = $false
        if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'release'))) {
            $javaOk = (Get-Content (Join-Path $env:JAVA_HOME 'release') -Raw) -match 'JAVA_VERSION="(1[7-9]|[2-9]\d)'
        }
        if (-not $javaOk) {
            $candidates = @(Get-ChildItem "$env:ProgramFiles\Android" -Directory -ErrorAction SilentlyContinue |
                ForEach-Object { Join-Path $_.FullName 'jbr' } | Where-Object { Test-Path (Join-Path $_ 'bin\java.exe') })
            if ($candidates.Count -eq 0) { Fail 'Need JDK 17 or newer: set JAVA_HOME (Android Studio ships one in its jbr folder).' }
            $env:JAVA_HOME = $candidates[-1]
            Write-Host "Using JAVA_HOME=$env:JAVA_HOME"
        }

        # GitHub Packages: gpr.usr/gpr.key from ~/.gradle/gradle.properties if set, else the gh login's
        # token, passed to this Gradle run only (it needs the read:packages scope).
        $userProps = Join-Path $env:USERPROFILE '.gradle\gradle.properties'
        $hasGpr = (Test-Path $userProps) -and ((Get-Content $userProps -Raw) -match '(?m)^\s*gpr\.key\s*=')
        if (-not $hasGpr -and -not $env:TOKEN) {
            if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
                Fail 'No GitHub Packages credentials: set gpr.usr/gpr.key in ~/.gradle/gradle.properties, or install and log in to gh.'
            }
            if (-not ((gh auth status 2>&1 | Out-String) -match 'read:packages')) {
                Fail "The gh login cannot read GitHub Packages. Run: gh auth refresh -h github.com -s read:packages"
            }
            ${env:ORG_GRADLE_PROJECT_gpr.usr} = (gh api user --jq .login)
            ${env:ORG_GRADLE_PROJECT_gpr.key} = (gh auth token)
        }

        $gradleArgs = @(':shimmerHilTest:assembleDebug', ':shimmerHilTest:assembleDebugAndroidTest',
            "-PdriverVersion=$DriverVersion", '-Pandroid.useAndroidX=true',
            '-Dorg.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g', '--console=plain')
        if ($AndroidApiVersion) { $gradleArgs += "-PandroidApiVersion=$AndroidApiVersion" }
        Write-Host 'Building the test APKs...'
        Push-Location $gradleRoot
        & .\gradlew.bat @gradleArgs *> (Join-Path $outDir 'build.log')
        $buildExit = $LASTEXITCODE
        Pop-Location
        if ($buildExit -ne 0) { Fail "Build failed; see $(Join-Path $outDir 'build.log')" }
    } finally {
        $env:JAVA_HOME = $saved.JAVA_HOME
        $env:ANDROID_HOME = $saved.ANDROID_HOME
        ${env:ORG_GRADLE_PROJECT_gpr.usr} = $saved.GPR_USR
        ${env:ORG_GRADLE_PROJECT_gpr.key} = $saved.GPR_KEY
    }
}

# --- Install -----------------------------------------------------------------------------------
# -g pre-grants runtime permissions. Some phones (Xiaomi) refuse it without "USB debugging (Security
# settings)"; the tests need no runtime permission before Android 12, so install without it there.
function Install-Apk([string]$file, [string[]]$flags) {
    ((& $adbExe @adbTarget install @flags $file 2>&1) | ForEach-Object { "$_" }) -join "`n"
}
foreach ($file in @($apk, $testApk)) {
    $result = Install-Apk $file @('-r', '-g')
    if ($result -match 'INSTALL_GRANT_RUNTIME_PERMISSIONS') {
        Write-Host "The phone refused to pre-grant permissions (-g); installing $(Split-Path $file -Leaf) without."
        $result = Install-Apk $file @('-r')
    }
    if ($result -notmatch 'Success') {
        Fail "Installing $(Split-Path $file -Leaf) failed:`n$($result.Trim())`nOn Xiaomi, turn on 'Install via USB' (and on Android 12+ 'USB debugging (Security settings)') in Developer options."
    }
}
# Clear the last run's reports, and make sure they went: the collect step takes any report it finds
# as this run's evidence.
Adb shell rm -rf $deviceReports | Out-Null
$left = (Adb shell "ls $deviceReports 2>/dev/null") -join ''
if ($left.Trim()) { Fail "Could not clear the old reports on the phone ($deviceReports); remove them and run again." }

# --- Run ---------------------------------------------------------------------------------------
# One scenario, with a time limit: the tests guard every API call, but if the whole process still
# never finishes, force-stop it so an unattended run cannot hang.
function Invoke-Instrument([string[]]$arguments, [int]$timeoutSeconds) {
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $adbExe
    $psi.Arguments = (@($adbTarget) + $arguments | ForEach-Object { if ("$_" -match '\s') { '"' + $_ + '"' } else { "$_" } }) -join ' '
    $psi.UseShellExecute = $false
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $p = [System.Diagnostics.Process]::Start($psi)
    $stdout = $p.StandardOutput.ReadToEndAsync()
    $stderr = $p.StandardError.ReadToEndAsync()
    $finished = $p.WaitForExit($timeoutSeconds * 1000)
    if (-not $finished) {
        & $adbExe @adbTarget shell am force-stop $package 2>&1 | Out-Null
        if (-not $p.WaitForExit(15000)) { $p.Kill() }
    }
    [pscustomobject]@{ Output = $stdout.Result + $stderr.Result; TimedOut = (-not $finished) }
}

$results = @()
$runs = 0
foreach ($target in $targets) {
    $u = $target.Unit
    $t = $target.Transport
    foreach ($s in $Scenario) {
        if ($runs++ -gt 0) { Start-Sleep -Seconds $SettleSeconds }
        $label = "$($u.name) $t $s"
        Write-Host "Running $label..." -NoNewline
        Adb logcat -c | Out-Null
        $instrumentArgs = @('shell', 'am', 'instrument', '-w', '-r',
            '-e', 'class', "$package.HilScenariosTest#$s",
            '-e', 'mac', $u.mac, '-e', 'unit', $u.name, '-e', 'type', $u.type, '-e', 'transport', $t,
            '-e', 'rate', $Rate, '-e', 'seconds', $Seconds,
            '-e', 'crc', $Crc, '-e', 'maxLossPercent', $MaxLossPercent,
            '-e', 'driverVersion', $DriverVersion, '-e', 'androidApi', ($apiLabel -replace ' ', '_'),
            $runner)
        $run = Invoke-Instrument $instrumentArgs $ScenarioTimeoutSeconds
        $output = $run.Output
        $safe = "$($u.name)_$($t)_$s" -replace '[^\w.-]', '_'
        $output | Set-Content (Join-Path $outDir "$safe.instrument.txt")
        Adb logcat -d -v threadtime | Set-Content (Join-Path $outDir "$safe.logcat.txt")

        $codes = [regex]::Matches($output, 'INSTRUMENTATION_STATUS_CODE: (-?\d+)')
        $code = if ($codes.Count -gt 0) { [int]$codes[$codes.Count - 1].Groups[1].Value } else { $null }
        $detail = ''
        switch ($code) {
            0 { $verdict = 'PASS' }
            -2 { $verdict = 'FAIL' }
            -4 { $verdict = 'SKIP' }
            default { $verdict = 'ERROR' }
        }
        $m = [regex]::Match($output, '(?:AssertionError|AssumptionViolatedException|Exception): ([^\r\n]+)')
        if ($run.TimedOut) {
            $verdict = 'TIMEOUT'
            $detail = "did not finish within $ScenarioTimeoutSeconds s; force-stopped (see the logcat)"
        }
        elseif ($m.Success) { $detail = $m.Groups[1].Value }
        elseif ($verdict -eq 'ERROR') { $detail = ($output -split "`n" | Select-Object -Last 3) -join ' ' }
        Write-Host " $verdict" -ForegroundColor $(if ($verdict -eq 'PASS') { 'Green' } elseif ($verdict -eq 'SKIP') { 'Yellow' } else { 'Red' })
        $results += [pscustomobject]@{ Unit = $u.name; Transport = $t; Scenario = $s; Result = $verdict; Detail = $detail }
    }
}

# --- Collect -----------------------------------------------------------------------------------
$reportDir = Join-Path $outDir 'reports'
$pullOutput = & $adbExe @adbTarget pull $deviceReports $reportDir 2>&1 | Out-String
$pullFailed = ($LASTEXITCODE -ne 0)
# Every run that reached its verdict wrote a report, and the gate's status rests on them: a run whose
# report did not come back is an error, not a pass.
$macByUnit = @{}
foreach ($u in $units) { $macByUnit[$u.name] = $u.mac }
foreach ($r in $results) {
    if ($r.Result -ne 'PASS' -and $r.Result -ne 'FAIL') { continue }
    $prefix = ($r.Scenario -creplace '([A-Z])', '_$1').ToLower() + "_$($r.Transport)_$($macByUnit[$r.Unit] -replace ':', '')_"
    $found = @(Get-ChildItem $reportDir -Filter "$prefix*.json" -ErrorAction SilentlyContinue)
    if ($found.Count -eq 0) {
        $why = if ($pullFailed) { "adb pull failed: $($pullOutput.Trim())" } else { 'the phone had no report for it' }
        $r.Result = 'ERROR'
        $r.Detail = (("no report collected ($why). " + $r.Detail).Trim())
    }
}
$summary = [pscustomobject]@{
    phone = "$phoneModel (Android $phoneAndroid)"; androidApi = $apiLabel; driverVersion = $DriverVersion
    rate = $Rate; seconds = $Seconds; crc = $Crc; settleSeconds = $SettleSeconds; results = $results
}
$summary | ConvertTo-Json -Depth 4 | Set-Content (Join-Path $outDir 'summary.json')

Write-Host ''
$results | Format-Table -AutoSize -Wrap | Out-String -Width 200 | Write-Host
# A skipped scenario did not run, so it is not a pass: the exit code and the gate's status agree on that.
$failed = @($results | Where-Object { $_.Result -ne 'PASS' })
$passed = @($results | Where-Object { $_.Result -eq 'PASS' })

if ($Report) {
    # Firmware per unit, from the reports the tests wrote, with the transports each ran over.
    $firmware = @{}
    Get-ChildItem $reportDir -Filter '*.json' -ErrorAction SilentlyContinue | ForEach-Object {
        $r = Get-Content $_.FullName -Raw | ConvertFrom-Json
        if ($r.firmware -and $r.args.unit) { $firmware[$r.args.unit] = "$($r.hardware) $($r.firmware)" }
    }
    $tested = @($units | ForEach-Object {
        $name = $_.name
        $over = (@($targets | Where-Object { $_.Unit.name -eq $name } | ForEach-Object { $_.Transport }) -join '+')
        $what = if ($firmware[$name]) { $firmware[$name] } else { $_.type }
        "$what ($over)"
    }) | Sort-Object -Unique
    $state = if ($passed.Count -eq $results.Count) { 'success' } else { 'failure' }
    $description = "$($passed.Count)/$($results.Count) passed on $phoneModel`: $($tested -join ', ')"
    if ($description.Length -gt 140) { $description = $description.Substring(0, 137) + '...' }
    $context = "android-hil (driver $DriverVersion)"
    gh api -X POST "repos/$githubRepo/statuses/$commit" -f state=$state -f context=$context -f description=$description --silent
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Could not post the commit status; the release will not see this run." -ForegroundColor Red
        exit 2
    }
    Write-Host "Posted '$context' = $state on $githubRepo@$($commit.Substring(0, 8)): $description"
}

if ($failed.Count -gt 0) {
    Write-Host "$($failed.Count) of $($results.Count) did not pass. Reports, logcat and instrument output: $outDir" -ForegroundColor Red
    exit 1
}
Write-Host "All $($results.Count) passed. Reports: $outDir" -ForegroundColor Green
exit 0
