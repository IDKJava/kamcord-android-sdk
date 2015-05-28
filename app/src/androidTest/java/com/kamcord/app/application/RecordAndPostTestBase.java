package com.kamcord.app.application;

import android.os.RemoteException;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Until;

import com.kamcord.app.R;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Mehmet on 5/27/15.
 */
public abstract class RecordAndPostTestBase extends TestBase {

    public void recordRippleTestLoginLast(){
        recordAndPostGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS);
    }

    protected void recordAndPostGameVideo(String gameName, int durationInMs){
        recordAndPostGameVideo(gameName, durationInMs, true);
    }
    protected void recordAndPostGameVideo(String gameName, int durationInMs, boolean failIfNotLoggedIn) {
        //find ripples app logo and click
        mDevice.findObject(By.text(gameName)).click();

        mDevice.findObject(By.res(getResByID(R.id.record_button))).click();

        //Ack the screen recording warning.
        boolean notTimedOut = mDevice
                .wait(Until.hasObject(By.res(ANDROID_SYSTEM_BUTTON1)), UI_TIMEOUT_MS);
        assertTrue("Recording notification timed out!", notTimedOut);
        //String s = getResByID(android.R.id.button1);
        mDevice.findObject(By.res(ANDROID_SYSTEM_BUTTON1)).click();
        //wait for ripples to show up.

        notTimedOut = mDevice
                .wait(Until.hasObject(By.res(RIPPLE_TEST_MAIN_RES)), APP_TIMEOUT_MS);
        assertTrue("Ripple test launch timed out!", notTimedOut);

        try {
            Thread.sleep(durationInMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertFalse("Test interrupted", true);
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
                .wait(Until.hasObject(By.text(getStrByID(R.string.toolbarTitle))), UI_TIMEOUT_MS);
        assertTrue("Notification failed to show!", notTimedOut);
        notTimedOut = mDevice
                .wait(Until.hasObject(By.text(getStrByID(R.string.paused))), UI_TIMEOUT_MS);
        assertTrue("Paused notification status not recording!", notTimedOut);
        //click on notification to resume app.
        mDevice.findObject(By.text(getStrByID(R.string.toolbarTitle))).click();
        //close notifications
        //click on notification to go back to the app

        //stop recording.
        //notTimedOut = mDevice
        //        .wait(Until.hasObject(By.text(getStrByID(R.string.toolbarTitle))), APP_TIMEOUT_MS);
        //assertTrue("Kamcord app not found in recent apps!", notTimedOut);
        //Bring up Kamcord
        //mDevice.findObject(By.text(getStrByID(R.string.toolbarTitle))).click();

        //find stop recording button.
        notTimedOut = mDevice
                .wait(Until.hasObject(By.res(getResByID(R.id.record_button))), APP_TIMEOUT_MS);
        assertTrue("Stop recording button timed out!", notTimedOut);
        mDevice.findObject(By.res(getResByID(R.id.record_button))).click();
        //wait for video processing to finish
        //TODO: Adjust the 4 divider to something reasonable as stitching perf. improves.
        int processingTimeout = Math.max((durationInMs / 4), VIDEO_PROCESSING_TIMEOUT);
        notTimedOut = mDevice
                .wait(Until.hasObject(By.res(getResByID(R.id.playImageView))), processingTimeout);
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
                        UPLOAD_TIMEOUT);
        if(failIfNotLoggedIn){
            assertTrue("UI timed out!", notTimedOut);
        } else {
            handleWelcomeLoginView();
            notTimedOut = mDevice
                    .wait(Until.hasObject(By.res(getResByID(R.id.shareButton))),
                            UPLOAD_TIMEOUT);
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
        assertTrue("Uploading notification failed to show recording!", notTimedOut);
        //close notifications
        mDevice.pressBack();
    }
}
