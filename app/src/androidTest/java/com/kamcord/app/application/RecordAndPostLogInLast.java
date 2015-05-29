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
    public void recordRippleTestLoginLast() {
        skipLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS);
        handleShareView(RECORDING_DURATION_MS, false);

    }
}
