package com.kamcord.app.application;

import android.graphics.Point;
import android.os.RemoteException;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;

import com.kamcord.app.R;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static com.kamcord.app.testutils.UiUtilities.*;

/**
 * Created by Mehmet on 5/27/15.
 */
public abstract class RecordAndPostTestBase extends TestBase {

    protected void recordGameVideo(String gameName, int durationInMs){
        recordGameVideo(gameName, gameName, durationInMs, false);
    }
    protected void recordGameVideo(String gameName, String gameTitle, int durationInMs){
        recordGameVideo(gameName, gameTitle, durationInMs, false);
    }
    protected void recordGameVideo(String gameName, String gameTitle, int durationInMs, boolean pauseAfterGesture) {
        mDevice.waitForIdle();
        waitForGameTileLoad(R.id.recordfragment_refreshlayout, APP_TIMEOUT_MS);
        //find ripples app logo and click
        //wait for load!!!!
        findGameListed(gameName);

        findUiObj(gameName, UiObjSelType.Txt).click();

        findUiObj(R.id.record_button, UiObjIdType.Res, UiObjSelType.Res).click();

        //Ack the screen recording warning.

        //Ack the message
        findUiObj(ANDROID_SYSTEM_BUTTON1, UiObjSelType.Res).click();

        //test app loads?
        findUiObj(RIPPLE_TEST_MAIN_RES, UiObjSelType.Res, APP_TIMEOUT_MS);

        //pattern exec time hardcoded for now.
        int miniSleepInMs = 2000;
        int sleepStep = durationInMs /  miniSleepInMs;

        for (int i = 0; i < sleepStep; i++) {
            //long time = System.currentTimeMillis();
            if ((i % 2) == 0){
                executeTouchPatterns();
                sleep(400);
            } else {
                if (pauseAfterGesture){
                    //get running task list
                    try {
                        mDevice.pressRecentApps();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        assertFalse("Press recent apps failed!", true);
                    }
                    //check if it's recording
                    mDevice.openNotification();

                    //findUiObj(ANDROID_NOTIFICATION_HEADER, UiObjSelType.Res, APP_TIMEOUT_MS);

                    findUiObj(R.string.paused, UiObjIdType.Str,  UiObjSelType.Txt, APP_TIMEOUT_MS);

                    mDevice.pressBack();

                    findUiObj(gameTitle, UiObjSelType.Txt, APP_TIMEOUT_MS).click();

                    findUiObj(RIPPLE_TEST_MAIN_RES, UiObjSelType.Res, APP_TIMEOUT_MS);

                } else {
                    long time = System.currentTimeMillis();
                    sleep(miniSleepInMs);
                    time = System.currentTimeMillis() - time;
                    time = Math.abs(time);
                }
            }

            //time = System.currentTimeMillis() - time;
            //time = Math.abs(time);
            //time = time;
        }

        //get running task list
        try {
            mDevice.pressRecentApps();
        } catch (RemoteException e) {
            e.printStackTrace();
            assertFalse("Press recent apps failed!", true);
        }
        //check if it's recording
        mDevice.openNotification();
        //findUiObj(ANDROID_NOTIFICATION_HEADER, UiObjSelType.Res, APP_TIMEOUT_MS);

        findUiObj(R.string.paused, UiObjIdType.Str, UiObjSelType.Txt,APP_TIMEOUT_MS);

        //click on notification to resume app.
        findUiObj(R.string.toolbarTitle, UiObjIdType.Str, UiObjSelType.Txt).click();

        //find stop recording button.
        findUiObj(R.id.record_button, UiObjIdType.Res, UiObjSelType.Res).click();

    }

    protected void handleShareView(int durationInMs) {
        handleShareView(durationInMs, true, true);
    }

    protected void handleShareView(int durationInMs, boolean failIfNotLoggedIn) {
        handleShareView(durationInMs, failIfNotLoggedIn, true);
    }

    protected void handleShareView(int durationInMs, boolean failIfNotLoggedIn, boolean waitForUpload) {
        //wait for video processing to finish
        //TODO: Adjust the "1" divider to something reasonable as stitching perf. improves.
        int processingTimeout = Math.max((durationInMs / 1), DEFAULT_VIDEO_PROCESSING_TIMEOUT);
        int uploadTimeout = Math.max((durationInMs / 1), DEFAULT_UPLOAD_TIMEOUT);
        mDevice.waitForIdle();
        findUiObj(R.id.playImageView, UiObjIdType.Res, UiObjSelType.Res, processingTimeout);

        UiObject2 title = findUiObj(R.id.titleEditText, UiObjIdType.Res, UiObjSelType.Res);
        title.click();
        title.setText("my awesome ripple test video");

        //close soft keyboard
        mDevice.pressBack();

        findUiObj(R.id.shareButton, UiObjIdType.Res, UiObjSelType.Res).click();



        if (failIfNotLoggedIn) {
            findUiObj(R.string.kamcordRecordTab, UiObjIdType.Str, UiObjSelType.Txt);
        } else {
            handleWelcomeLoginView();
            findUiObj(R.id.shareButton, UiObjIdType.Res, UiObjSelType.Res).click();
        }

        //check if it's recording, we seem not to be fast enough to check this.
        mDevice.openNotification();
        //We're not fast enough to check both before the upload finishes. :(
        findUiObj(R.string.app_name, UiObjIdType.Str, UiObjSelType.Txt);
        findUiObj(R.string.uploading, UiObjIdType.Str, UiObjSelType.Txt);

        if(waitForUpload) {
            loseUiObj(R.string.uploading, UiObjIdType.Str, UiObjSelType.Txt, uploadTimeout);
        }
        //close notifications
        mDevice.pressBack();
    }

    protected void executeTouchPatterns() {
        Point[] pattern = new Point[]{new Point(500, 300),
                new Point(500, 1600),
                new Point(1000, 1600),
                new Point(1000, 300),
                new Point(500, 300)};
        mDevice.swipe(validateSwipe(pattern),25);
    }

    protected boolean checkIfGameTilesUpdating(UiObject2 gameTiles){
        mDevice.waitForIdle();
        for (UiObject2 child : gameTiles.getChildren()) {
            if (child.getClassName().equals(android.widget.ImageView.class.getName())) {
                return true;
            }
        }
        return false;
    }


}
