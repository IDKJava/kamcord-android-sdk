package com.kamcord.app.application;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Mehmet on 5/28/15.
 */
// Will work in Junit 4.11
// @FixMethodOrder
public class CacheTest extends RecordAndPostTestBase {
    protected static final String KAMCORD_CACHE_FOLDER = "Kamcord_Android";
    protected static final String NOMEDIA_TAG = ".nomedia";
    protected static final String SDCARD_ROOT = "/storage/sdcard0/";

    public void clearCacheTest(){
        //enable line below when we update the test and detach from checkCacheNoMediaTest
        //skipLogin();
        clearCache();
        //TODO: Ask for some feedback as to weather the clean cache op has completed.
        sleep(APP_TIMEOUT_MS);
        String files = executeShellCommand(String.format("ls -al %s", SDCARD_ROOT));
        assertFalse("Kamcord_Android folder is present!", files.contains(KAMCORD_CACHE_FOLDER));
    }
    @Test
    public void checkCacheNoMediaTest(){
        //Run post test video
        skipLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS);
        String files = executeShellCommand(String.format("ls -al %s/%s", SDCARD_ROOT, KAMCORD_CACHE_FOLDER));
        assertTrue(".nodmedia tag is not present!", files.contains(NOMEDIA_TAG));
        //clear cache.
        mDevice.pressBack();
        clearCacheTest();
    }
}
