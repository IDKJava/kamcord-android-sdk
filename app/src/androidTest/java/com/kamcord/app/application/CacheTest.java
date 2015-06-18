package com.kamcord.app.application;



import com.kamcord.app.R;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static com.kamcord.app.testutils.UiUtilities.*;
import static com.kamcord.app.testutils.SystemUtilities.*;

/**
 * Created by Mehmet on 5/28/15.
 */
// Will work in Junit 4.11
// @FixMethodOrder
public class CacheTest extends RecordAndPostTestBase {



    @Test
    public void checkCacheNoMediaTest(){
        int recordindDuration1X = RECORDING_DURATION_MS;
        int recordindDuration3X = RECORDING_DURATION_MS * 3;
        //magic number due to the removed 3 seconds from the beginning
        //1st rec ~3 sec 2nd rec ~18sec
        double sizeMultiplier =  6;
        doLogin();
        //create short baseline
        recordGameVideo(RIPPLE_TEST_APP_NAME, recordindDuration1X);
        findUiObj(R.id.playImageView, UiObjIdType.Res, UiObjSelType.Res, recordindDuration1X);
        //get baseline with short video
        //cacheSizeBefore is 1x video size. System cleans up before record.

        sleep(UI_INTERACTION_DELAY_MS);
        int cacheSizeBefore = getCacheSize();

        mDevice.pressBack();
        recordGameVideo(RIPPLE_TEST_APP_NAME, recordindDuration3X);
        findUiObj(R.id.playImageView, UiObjIdType.Res, UiObjSelType.Res, recordindDuration3X);
        //cacheSize  is 3x video size  by the same logic.
        sleep(UI_INTERACTION_DELAY_MS);
        int cacheSize = getCacheSize();
        //by the same logic
        assertTrue("Cache didn't increase!", cacheSizeBefore < cacheSize);
        assertTrue("Nomedia tag is missing!", isNoMediaTagPresent());
        mDevice.pressBack();
        //We need stitching to be over.
        assertTrue("Cache didn't reduce!", cacheSize < cacheSizeBefore * sizeMultiplier);

    }

    protected int getCacheSize(){
        String cacheFullPath = String.format("%s%s", SDCARD_ROOT, KAMCORD_CACHE_FOLDER);
        return getFolderSize(cacheFullPath);
    }

    protected boolean isNoMediaTagPresent(){
        String cacheFullPath = String.format("%s%s", SDCARD_ROOT, KAMCORD_CACHE_FOLDER);
        String list = executeShellCommand(String.format("ls -al %s/.nomedia", cacheFullPath));
        return list.contains(".nomedia");
    }


}
