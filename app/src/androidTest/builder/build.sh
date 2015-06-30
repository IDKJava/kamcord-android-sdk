#!/usr/bin/env bash
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
TESTDOC_FOLDER="/Library/WebServer/Documents/documentation/android-app"
REPORT_FOLDER="/Library/WebServer/Documents/reports/"$BUILD_TAG
SPOON_FOLDER="app/build/spoon/debug"
DOXYGEN_FOLDER="app/build/testDocs/html"
ADB_BIN=$ANDROID_SDK"/platform-tools/adb"

function testPrepDevice(){
    echo "Install com.kamcord.ripples"
    $ADB_BIN -s $DEVICE_ID install -r app/src/androidTest/res/RippleActivity.apk
    sleep 5
    echo "Push IP Tables Script Enable"
    $ADB_BIN -s $DEVICE_ID push app/src/androidTest/res/enable.sh /sdcard/enable.sh
    sleep 5
    echo "Push IP Tables Script Disable"
    $ADB_BIN -s $DEVICE_ID push app/src/androidTest/res/disable.sh /sdcard/disable.sh
    sleep 5
    #Make sure screen is not locked.
    $ADB_BIN shell input keyevent 82
    sleep 5
    #Make sure we're home
    $ADB_BIN shell input keyevent 3
}

function cleanDevice(){
    echo "Stop com.kamcord.app"
    $ADB_BIN -s $DEVICE_ID shell su -c am force-stop com.kamcord.app
    sleep 5
    echo "Uninstall com.kamcord.app"
    $ADB_BIN -s $DEVICE_ID uninstall com.kamcord.app
    sleep 5
    echo "Stop com.kamcord.app.test"
    $ADB_BIN -s $DEVICE_ID shell su -c am force-stop com.kamcord.app.test
    sleep 5
    echo "Uninstall com.kamcord.app.test"
    $ADB_BIN -s $DEVICE_ID uninstall com.kamcord.app.test
    sleep 5
    echo "Stop com.kamcord.ripples"
    $ADB_BIN -s $DEVICE_ID shell su -c am force-stop com.kamcord.ripples
    sleep 5
    echo "Uninstall com.kamcord.ripples"
    $ADB_BIN -s $DEVICE_ID uninstall com.kamcord.ripples
    sleep 5
}
#generate test docs
$DIR/generateDoxygen.sh
rm -rf $TESTDOC_FOLDER
cp -R $DOXYGEN_FOLDER $TESTDOC_FOLDER

if [ -z "$ANDROID_SDK" ]; then
    ANDROID_SDK=$1
fi
echo "sdk.dir=$ANDROID_SDK" > local.properties
if [ -z "$BUILD_TAG" ]; then
    BUILD_TAG="ZX1G22S7X2"
fi

$ADB_BIN devices
#DEVICE_ID="047e1d53de4a0dac"
if [ -z "$DEVICE_ID" ]; then
    DEVICE_ID="ZX1G22S7X2"
    echo $DEVICE_ID
fi

#create report folder
rm -rf $REPORT_FOLDER
mkdir -p $REPORT_FOLDER

#test prep device
cleanDevice
testPrepDevice
#Run ProfileTest
gradle -PspoonClassName=com.kamcord.app.application.ProfileTest -PtargetDeviceId="$DEVICE_ID" spoon
FAILED=$?
cp -R $SPOON_FOLDER $REPORT_FOLDER/ProfileTest
gradle -PspoonClassName=com.kamcord.app.application.GameListTest -PtargetDeviceId="$DEVICE_ID" spoon
if [ $? -ne 0 ]; then
    FAILED=1
fi
cp -R $SPOON_FOLDER $REPORT_FOLDER/GameListTest
#Run LoginLogoutTest
cleanDevice
gradle -PspoonClassName=com.kamcord.app.application.LoginLogoutTest -PtargetDeviceId="$DEVICE_ID" spoon
if [ $? -ne 0 ]; then
    FAILED=1
fi
cp -R $SPOON_FOLDER $REPORT_FOLDER/LoginLogoutTest
#Run MemoryTest
cleanDevice
gradle -PspoonClassName=com.kamcord.app.application.MemoryTest -PtargetDeviceId="$DEVICE_ID" spoon
if [ $? -ne 0 ]; then
    FAILED=1
fi
cp -R $SPOON_FOLDER $REPORT_FOLDER/MemoryTest
#Run RecordingTestShort
cleanDevice
gradle -PspoonClassName=com.kamcord.app.application.RecordingTestShort -PtargetDeviceId="$DEVICE_ID" spoon
if [ $? -ne 0 ]; then
    FAILED=1
fi
cp -R $SPOON_FOLDER $REPORT_FOLDER/RecordingTestShort
#Run RecordingTestMedium
cleanDevice
gradle -PspoonClassName=com.kamcord.app.application.RecordingTestMedium -PtargetDeviceId="$DEVICE_ID" spoon
if [ $? -ne 0 ]; then
    FAILED=1
fi
cp -R $SPOON_FOLDER $REPORT_FOLDER/RecordingTestMedium
#Run RecordingTestLong
cleanDevice
gradle -PspoonClassName=com.kamcord.app.application.RecordingTestLong -PtargetDeviceId="$DEVICE_ID" spoon
if [ $? -ne 0 ]; then
    FAILED=1
fi
cp -R $SPOON_FOLDER $REPORT_FOLDER/RecordingTestLong
#exit with the error code.
exit $FAILED
