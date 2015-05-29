package com.kamcord.app.application;

import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Until;

import com.kamcord.app.R;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by Mehmet on 5/27/15.
 */
public class RecordAndPostLoggedInShort extends RecordAndPostTestBase{

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
}
