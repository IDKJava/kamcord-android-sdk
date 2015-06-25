REPORT_FOLDER=/Library/WebServer/Documents/reports/$BUILD_TAG
ADB_BIN=$ANDROID_SDK"/platform-tools/adb"
$ADB_BIN devices
#DEVICE_ID="047e1d53de4a0dac"
if [ "$DEVICE_ID" -eq "" ]; then
    DEVICE_ID="ZX1G22S7X2"
fi
$ADB_BIN uninstall com.kamcord.app
$ADB_BIN uninstall com.kamcord.app.test
mkdir -p $REPORT_FOLDER
gradle spoon -PspoonClassName=com.kamcord.app.application.ProfileTest,targetDeviceId="$DEVICE_ID"
FAILED=$?
cp -R app/build/spoon/debug $REPORT_FOLDER/ProfileTest
gradle spoon -PspoonClassName=com.kamcord.app.application.GameListTest,targetDeviceId="$DEVICE_ID"
if [ $? -ne 0 ]; then
    FAILED=1
fi
cp -R app/build/spoon/debug $REPORT_FOLDER/GameListTest
gradle spoon -PspoonClassName=com.kamcord.app.application.LoginLogoutTest,targetDeviceId="$DEVICE_ID"
if [ $? -ne 0 ]; then
    FAILED=1
fi
cp -R app/build/spoon/debug $REPORT_FOLDER/LoginLogoutTest
gradle spoon -PspoonClassName=com.kamcord.app.application.MemoryTest,targetDeviceId="$DEVICE_ID"
if [ $? -ne 0 ]; then
    FAILED=1
fi
cp -R app/build/spoon/debug $REPORT_FOLDER/MemoryTest
gradle spoon -PspoonClassName=com.kamcord.app.application.RecordingTestShort,targetDeviceId="$DEVICE_ID"
if [ $? -ne 0 ]; then
    FAILED=1
fi
cp -R app/build/spoon/debug $REPORT_FOLDER/RecordingTestShort
gradle spoon -PspoonClassName=com.kamcord.app.application.RecordingTestMedium,targetDeviceId="$DEVICE_ID"
if [ $? -ne 0 ]; then
    FAILED=1
fi
cp -R app/build/spoon/debug $REPORT_FOLDER/RecordingTestMedium
gradle spoon -PspoonClassName=com.kamcord.app.application.RecordingTestLong,targetDeviceId="$DEVICE_ID"
if [ $? -ne 0 ]; then
    FAILED=1
fi
cp -R app/build/spoon/debug $REPORT_FOLDER/RecordingTestLong
#exit with the error code.
exit $FAILED