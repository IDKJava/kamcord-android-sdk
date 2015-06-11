package com.kamcord.app.application;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static com.kamcord.app.testutils.UiUtilities.*;
import static com.kamcord.app.testutils.SystemUtilities.*;

/**
 * Created by Mehmet on 5/28/15.
 */
// Will work in Junit 4.11
// @FixMethodOrder
public class CacheTest extends RecordAndPostTestBase {

    protected static int CACHE_CLEAR_TRIALS = 2;

    public void clearCacheTest(){
        //enable line below when we update the test and detach from checkCacheNoMediaTest
        //skipLogin();
        boolean notDeleted = true;
        for (int trials = 0; trials < CACHE_CLEAR_TRIALS && notDeleted; trials++) {
            clearCache();
            //TODO: Ask for some feedback as to weather the clean cache op has completed.
            sleep(APP_TIMEOUT_MS);
            String files = executeShellCommand(String.format("ls -al %s", SDCARD_ROOT));
            notDeleted = files.contains(KAMCORD_CACHE_FOLDER);
        }

        assertFalse("Kamcord_Android folder is present!", notDeleted);
    }
    @Test
    public void checkCacheNoMediaTest(){
        //Run post test video
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS);
        String files = executeShellCommand(String.format("ls -al %s/%s", SDCARD_ROOT, KAMCORD_CACHE_FOLDER));
        assertTrue(".nodmedia tag is not present!", files.contains(NOMEDIA_TAG));
        //clear cache.
        mDevice.pressBack();
        clearCacheTest();
    }
}
