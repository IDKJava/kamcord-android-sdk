#!/usr/bin/env bash
if [ -z "$ANDROID_SDK" ]; then
    ANDROID_SDK=$1
fi
echo "sdk.dir=$ANDROID_SDK" > local.properties
if [ -z "$BUILD_TAG" ]; then
    BUILD_TAG="ZX1G22S7X2"
fi
REPORT_FOLDER=/Library/WebServer/Documents/reports/$BUILD_TAG
ADB_BIN=$ANDROID_SDK"/platform-tools/adb"
$ADB_BIN devices
#DEVICE_ID="047e1d53de4a0dac"
if [ -z "$DEVICE_ID" ]; then
    DEVICE_ID="ZX1G22S7X2"
fi
$ADB_BIN uninstall com.kamcord.app
$ADB_BIN uninstall com.kamcord.app.test
$ADB_BIN uninstall com.kamcord.ripples
$ADB_BIN install -r app/src/androidTest/res/RippleActivity.apk
$ADB_BIN push app/src/androidTest/res/enable.sh /sdcard/enable.sh
$ADB_BIN push app/src/androidTest/res/disable.sh /sdcard/disable.sh
rm -rf $REPORT_FOLDER
mkdir -p $REPORT_FOLDER
gradle -PspoonClassName=com.kamcord.app.application.ProfileTest -PtargetDeviceId="$DEVICE_ID" spoon
FAILED=$?
cp -R app/build/spoon/debug $REPORT_FOLDER/ProfileTest
gradle -PspoonClassName=com.kamcord.app.application.GameListTest -PtargetDeviceId="$DEVICE_ID" spoon
if [ $? -ne 0 ]; then
    FAILED=1
fi
cp -R app/build/spoon/debug $REPORT_FOLDER/GameListTest
gradle -PspoonClassName=com.kamcord.app.application.LoginLogoutTest -PtargetDeviceId="$DEVICE_ID" spoon
if [ $? -ne 0 ]; then
    FAILED=1
fi
cp -R app/build/spoon/debug $REPORT_FOLDER/LoginLogoutTest
gradle -PspoonClassName=com.kamcord.app.application.MemoryTest -PtargetDeviceId="$DEVICE_ID" spoon
if [ $? -ne 0 ]; then
    FAILED=1
fi
cp -R app/build/spoon/debug $REPORT_FOLDER/MemoryTest
gradle -PspoonClassName=com.kamcord.app.application.RecordingTestShort -PtargetDeviceId="$DEVICE_ID" spoon
if [ $? -ne 0 ]; then
    FAILED=1
fi
cp -R app/build/spoon/debug $REPORT_FOLDER/RecordingTestShort
gradle -PspoonClassName=com.kamcord.app.application.RecordingTestMedium -PtargetDeviceId="$DEVICE_ID" spoon
if [ $? -ne 0 ]; then
    FAILED=1
fi
cp -R app/build/spoon/debug $REPORT_FOLDER/RecordingTestMedium
gradle -PspoonClassName=com.kamcord.app.application.RecordingTestLong -PtargetDeviceId="$DEVICE_ID" spoon
if [ $? -ne 0 ]; then
    FAILED=1
fi
cp -R app/build/spoon/debug $REPORT_FOLDER/RecordingTestLong
#exit with the error code.
exit $FAILED
