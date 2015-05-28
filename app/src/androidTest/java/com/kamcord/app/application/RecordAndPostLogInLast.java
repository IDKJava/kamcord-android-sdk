package com.kamcord.app.application;


import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Until;

import org.junit.Test;
import com.kamcord.app.R;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
/**
 * Created by Mehmet on 5/27/15.
 */
public class RecordAndPostLogInLast extends RecordAndPostTestBase {
    @Test
    public void recordRippleTestLoginFirst() {
        //skip login
        //should see an active record tab.
        boolean notTimedOut = mDevice.wait(Until.hasObject(By.res(getResByID(R.id.skipButton))),
                UI_TIMEOUT_MS);
        assertTrue("No skip button found! Are we logged in?", notTimedOut);
        mDevice.findObject(By.res(getResByID(R.id.skipButton))).click();
        notTimedOut = mDevice.wait(Until.hasObject(By.res(getResByID(R.id.activity_mdrecord_layout))),
                UI_TIMEOUT_MS);
        assertTrue("Record view did not load!", notTimedOut);
        recordAndPostGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS, false);

    }
}
