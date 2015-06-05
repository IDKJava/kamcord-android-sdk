package com.kamcord.app.application;

import android.graphics.Point;
import android.os.RemoteException;
import android.support.test.uiautomator.UiObject2;

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
    protected void recordGameVideo(String gameName,
                                   String gameTitle,
                                   int durationInMs,
                                   boolean pauseAfterGesture) {
        recordGameVideo(gameName, gameTitle, durationInMs, pauseAfterGesture, false);
    }

        protected void recordGameVideo(String gameName,
                                   String gameTitle,
                                   int durationInMs,
                                   boolean pauseAfterGesture,
                                   boolean useRecentAppsToSwitchToKamcord) {
        mDevice.waitForIdle();
        waitForTileLoad(R.id.recordfragment_refreshlayout, APP_TIMEOUT_MS);
        //find ripples app logo and click
        //wait for load!!!!
        findGame(gameName);

        getRecordButtonForGame(gameName, true).longClick();

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
                executeRectPattern();
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
                    sleep(miniSleepInMs);
                }
            }

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

        findUiObj(R.string.paused, UiObjIdType.Str, UiObjSelType.Txt, APP_TIMEOUT_MS);
        if(useRecentAppsToSwitchToKamcord){
            //closes notifications so we can pick from recent apps.
            mDevice.pressBack();
        }
        //click on notification to resume app.
        findUiObj(R.string.toolbarTitle, UiObjIdType.Str, UiObjSelType.Txt).click();
        //find stop recording button.
        getRecordButtonForGame(gameName, false).click();

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
        title.setText("my awesome test video");

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
        //check if profile page works.

        findUiObj(R.string.kamcordProfileTab, UiObjIdType.Str, UiObjSelType.Txt).click();
        //is it there?
        findUiObj(R.id.userProfileInfo, UiObjIdType.Res, UiObjSelType.Res);
    }

    protected void executeRectPattern() {
        Point[] pattern = new Point[]{new Point(500, 300),
                new Point(500, 1600),
                new Point(1000, 1600),
                new Point(1000, 300),
                new Point(500, 300)};
        //25 steps to ensure ~1600ms execution
        executeTouchPattern(pattern, 25);
    }

    protected UiObject2 getRecordButtonForGame(String gameName, boolean start){

        UiObject2 gameLabel = findUiObj(R.id.gameNameTextView, UiObjIdType.Res, UiObjSelType.Res);

        UiObject2 completeGameItem = gameLabel.getParent().getParent();

        UiObject2 button = findUiObjInObj(completeGameItem, R.id.recordImageButton,
                UiObjIdType.Res, UiObjSelType.Res, UI_TIMEOUT_MS);
        String buttonDescExpected;
        if(start) {
            buttonDescExpected = getStrByID(R.string.idle);
        } else {
            buttonDescExpected = getStrByID(R.string.recording);
        }

        assertTrue("Wrong button showing!",
                button.getContentDescription().equals(buttonDescExpected));
        return button;
    }


}
