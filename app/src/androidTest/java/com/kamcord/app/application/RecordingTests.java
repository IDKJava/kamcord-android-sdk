package com.kamcord.app.application;

import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Until;

import com.kamcord.app.R;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static com.kamcord.app.testutils.UiUtilities.*;

/**
 * Created by Mehmet on 5/29/15.
 */
public class RecordingTests extends RecordAndPostTestBase{

    //@Test
    public void recordRippleTestLoginFirstLong(){
        doLogin();
        //TODO: What do we do with the really long tests?
        int recordingDuration = 60 * MS_PER_MIN;
        recordGameVideo(RIPPLE_TEST_APP_NAME, RIPPLE_TEST_APP_TITLE, recordingDuration);
        handleShareView(recordingDuration);
    }

    @Test
    public void recordRippleTestNTimesLoggedIn() {
        doLogin();
        int N = 5;
        int recDuration = RECORDING_DURATION_MS * 3;
        for(int i=0; i < N-1; i++) {
            recordGameVideo(RIPPLE_TEST_APP_NAME, RIPPLE_TEST_APP_TITLE, recDuration);
            handleShareView(recDuration, true, false);
        }
        recordGameVideo(RIPPLE_TEST_APP_NAME, RIPPLE_TEST_APP_TITLE, recDuration);
        handleShareView(recDuration, true, true);
    }

    @Test
    public void recordRippleTestLoginLast() {
        skipLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS);
        handleShareView(RECORDING_DURATION_MS, false);

    }
    //TODO: Enable when test conditions are clear!
    //@Test
    public void recordAndPostLoggedInWithPause(){
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RIPPLE_TEST_APP_TITLE, 2 * RECORDING_DURATION_MS, true);
        handleShareView(RECORDING_DURATION_MS);
    }

    @Test
    public void recordRippleTestLoginFirstShort(){
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, 1000);
        boolean notTimedOut =
                mDevice.wait(Until.hasObject(By.res(getResByID(R.id.activity_mdrecord_layout))),
                        UI_TIMEOUT_MS);
        assertTrue("Record view did not show up! The short video should not trigger share",
                notTimedOut);
    }

    @Test
    public void recordRippleTestSkipLoginShort(){
        skipLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, 1000);
        boolean notTimedOut =
                mDevice.wait(Until.hasObject(By.res(getResByID(R.id.activity_mdrecord_layout))),
                        UI_TIMEOUT_MS);
        assertTrue("Record view did not show up! The short video should not trigger share",
                notTimedOut);
    }

    @Test
    public void recordRippleTestLoginFirst(){
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS);
        handleShareView(RECORDING_DURATION_MS);

    }

}
