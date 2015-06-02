package com.kamcord.app.application;

import android.graphics.Point;
import android.os.RemoteException;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;

import com.kamcord.app.R;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        boolean notTimedOut =  mDevice
                .wait(Until.hasObject(By.res(getResByID(R.id.recordfragment_refreshlayout))),
                        UI_TIMEOUT_MS);
        assertTrue("Recording layout failed to load!", notTimedOut);

        //find ripples app logo and click
        findGameListed(gameName);

        notTimedOut = mDevice
                .wait(Until.hasObject(By.text(gameName)), UI_TIMEOUT_MS);
        assertTrue(String.format("%s not found!", gameName), notTimedOut);
        mDevice.findObject(By.text(gameName)).click();


        mDevice.findObject(By.res(getResByID(R.id.record_button))).click();

        //Ack the screen recording warning.
        notTimedOut = mDevice
                .wait(Until.hasObject(By.res(ANDROID_SYSTEM_BUTTON1)), UI_TIMEOUT_MS);
        assertTrue("Recording notification timed out!", notTimedOut);
        //String s = getResByID(android.R.id.button1);
        mDevice.findObject(By.res(ANDROID_SYSTEM_BUTTON1)).click();
        //wait for ripples to show up.

        notTimedOut = mDevice
                .wait(Until.hasObject(By.res(RIPPLE_TEST_MAIN_RES)), APP_TIMEOUT_MS);
        assertTrue("Ripple test launch timed out!", notTimedOut);


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
                    notTimedOut = mDevice
                            .wait(Until.hasObject(By.res(ANDROID_NOTIFICATION_HEADER)),
                                    APP_TIMEOUT_MS);
                    assertTrue("Android notification header not showing!", notTimedOut);
                    notTimedOut = mDevice
                            .wait(Until.hasObject(By.text(getStrByID(R.string.paused))),
                                    APP_TIMEOUT_MS);
                    assertTrue("Paused notification status not showing!", notTimedOut);
                    mDevice.pressBack();
                    notTimedOut = mDevice
                            .wait(Until.hasObject(By.text(gameTitle)), APP_TIMEOUT_MS);
                    assertTrue("Game name failed to show!", notTimedOut);
                    //click on notification to resume app.
                    mDevice.findObject(By.text(gameTitle)).click();
                    notTimedOut = mDevice
                            .wait(Until.hasObject(By.res(RIPPLE_TEST_MAIN_RES)), APP_TIMEOUT_MS);
                    assertTrue("Ripple test return timed out!", notTimedOut);
                } else {
                    sleep(miniSleepInMs);
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
        notTimedOut = mDevice
                .wait(Until.hasObject(By.res(ANDROID_NOTIFICATION_HEADER)),
                        APP_TIMEOUT_MS);
        assertTrue("Paused notification header not showing!", notTimedOut);
        notTimedOut = mDevice
                .wait(Until.hasObject(By.text(getStrByID(R.string.toolbarTitle))), UI_TIMEOUT_MS);
        assertTrue("Notification failed to show!", notTimedOut);
        notTimedOut = mDevice
                .wait(Until.hasObject(By.text(getStrByID(R.string.paused))), UI_TIMEOUT_MS);
        assertTrue("Paused notification status not showing!", notTimedOut);
        //click on notification to resume app.
        mDevice.findObject(By.text(getStrByID(R.string.toolbarTitle))).click();

        //find stop recording button.
        notTimedOut = mDevice
                .wait(Until.hasObject(By.res(getResByID(R.id.record_button))), APP_TIMEOUT_MS);
        assertTrue("Stop recording button timed out!", notTimedOut);
        mDevice.findObject(By.res(getResByID(R.id.record_button))).click();
    }

    protected void handleShareView(int durationInMs) {
        handleShareView(durationInMs, true, true);
    }

    protected void handleShareView(int durationInMs, boolean failIfNotLoggedIn) {
        handleShareView(durationInMs, failIfNotLoggedIn, true);
    }

    protected void handleShareView(int durationInMs, boolean failIfNotLoggedIn, boolean waitForUpload) {
        //wait for video processing to finish
        //TODO: Adjust the 4 divider to something reasonable as stitching perf. improves.
        int processingTimeout = Math.max((durationInMs / 4), DEFAULT_VIDEO_PROCESSING_TIMEOUT);
        int uploadTimeout = Math.max((durationInMs / 4), DEFAULT_UPLOAD_TIMEOUT);
        boolean notTimedOut =
                mDevice.wait(Until.hasObject(By.res(getResByID(R.id.playImageView))),
                        processingTimeout);
        assertTrue("Video processing timed out!", notTimedOut);
        mDevice.findObject(By.res(getResByID(R.id.titleEditText))).click();
        mDevice.findObject(By.res(getResByID(R.id.titleEditText)))
                .setText("my awesome ripple test video");
        //close soft keyboard
        mDevice.pressBack();
        /*
        Description removed.
        mDevice.findObject(By.res(getResByID(R.id.descriptionEditText))).click();
        mDevice.findObject(By.res(getResByID(R.id.descriptionEditText)))
                .setText("The quick brown fox jumps over the lazy dog.");
        //close soft keyboard
        mDevice.pressBack();
        */
        mDevice.findObject(By.res(getResByID(R.id.shareButton))).click();

        notTimedOut = mDevice
                .wait(Until.hasObject(By.text(getStrByID(R.string.kamcordRecordTab))),
                        DEFAULT_UPLOAD_TIMEOUT);
        if (failIfNotLoggedIn) {
            assertTrue("UI timed out!", notTimedOut);
        } else {
            handleWelcomeLoginView();
            notTimedOut = mDevice
                    .wait(Until.hasObject(By.res(getResByID(R.id.shareButton))),
                            DEFAULT_UPLOAD_TIMEOUT);
            assertTrue("Login before share failed!", notTimedOut);
            mDevice.findObject(By.res(getResByID(R.id.shareButton))).click();
        }

        //check if it's recording, we seem not to be fast enough to check this.
        mDevice.openNotification();
        //We're not fast enough to check both before the upload finishes. :(
        notTimedOut = mDevice
                .wait(Until.hasObject(By.text(getStrByID(R.string.app_name))), UI_TIMEOUT_MS);
        assertTrue("Uploading notification failed to show!", notTimedOut);
        notTimedOut = mDevice
                .wait(Until.hasObject(By.text(getStrByID(R.string.uploading))), UI_TIMEOUT_MS);
        assertTrue("Uploading notification failed to show!", notTimedOut);
        if(waitForUpload) {
            notTimedOut = mDevice
                    .wait(Until.gone(By.text(getStrByID(R.string.uploading))), uploadTimeout);
            assertTrue("Uploading notification failed to clear!", notTimedOut);
        }
        //close notifications
        mDevice.pressBack();
    }

    protected void executeTouchPatterns() {
        Point[] pattern = new Point[]{new Point(500, 300),
                new Point(500, 1600),
                new Point(1200, 1600),
                new Point(1200, 300),
                new Point(500, 300)};
        mDevice.swipe(pattern,25);
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
