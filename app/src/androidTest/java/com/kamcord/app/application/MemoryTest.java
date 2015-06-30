package com.kamcord.app.application;

import com.kamcord.app.R;

import org.junit.Test;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import static com.kamcord.app.testutils.UiUtilities.*;
import static com.kamcord.app.testutils.SystemUtilities.*;

/**
 * Created by Mehmet on 5/28/15.
 */
// Will work in Junit 4.11
// @FixMethodOrder

/**
 * Checks disk cache usage and memory leaks.
 */
public class MemoryTest extends RecordAndPostTestBase {

    @Test
    /**
     * Checks if the cache is cleared when a new video recording starts.
     * Checks if the temp files have .nomedia tags.
     * <p>
     *     <b>Test Sequence:</b><br>
     *     1) {@link TestBase#doLogin Log in}<br>
     *     2) Record a short video. @see RecordAndPostTestBase#recordGameVideo<br>
     *     3) Check cache size.<br>
     *     4) Discard the video after processing is done by clicking back.<br>
     *     5) Record a video 3 times the short length.<br>
     *     6) Expect the cache size to be less than the expected sum of the two recordings.<br>
     *     7) Expect ".nomedia" files to be present in the temp folder. (check using shell cmd)<br>
     * </p>
     */
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
        //hit the delete button
        findUiObj(ANDROID_SYSTEM_BUTTON1, UiObjSelType.Res, UI_TIMEOUT_MS).click();
        recordGameVideo(RIPPLE_TEST_APP_NAME, recordindDuration3X);
        findUiObj(R.id.playImageView, UiObjIdType.Res, UiObjSelType.Res, recordindDuration3X);
        //cacheSize  is 3x video size  by the same logic.
        sleep(UI_INTERACTION_DELAY_MS);
        int cacheSize = getCacheSize();
        //by the same logic
        assertTrue("Cache didn't increase!", cacheSizeBefore < cacheSize);
        assertTrue("Nomedia tag is missing!", isNoMediaTagPresent());
        mDevice.pressBack();
        findUiObj(ANDROID_SYSTEM_BUTTON1, UiObjSelType.Res, UI_TIMEOUT_MS).click();
        //We need stitching to be over.
        assertTrue("Cache didn't reduce!", cacheSize < cacheSizeBefore * sizeMultiplier);

    }

    //@Test
    //Char only for now
    /**
     * Record 5 videos and discard videos after processing.
     * Monitor dalvik and native heap usage. Expect min-max diff to be less than 10% of min.
     * <p>
     *     <b>Test Sequence:</b><br>
     *     1) {@link TestBase#doLogin Log in}<br>
     *     2) {@link RecordAndPostTestBase#recordGameVideo Record} a 4 sec video.<br>
     *     3) Check heap sizes.<br>
     *     4) Discard the video after processing is done by clicking back.<br>
     *     5) Repeat 2-4 5 times.<br>
     *     6) Expect max-min difference to be less than min heap size.<br>
     * </p>
     */
    public void checkHeapUsage() {
        int recordingTrials = 5;
        int recordingBase  = 4000;
        doLogin();
        //get baseline
        int maxDalvik = Integer.MIN_VALUE;
        int maxNative = Integer.MIN_VALUE;
        int minDalvik = Integer.MAX_VALUE;
        int minNative = Integer.MAX_VALUE;

        for (int i = 0; i < recordingTrials; i++) {
            recordGameVideo(RIPPLE_TEST_APP_NAME, recordingBase + 100 * i);
            findUiObj(R.id.playImageView, UiObjIdType.Res, UiObjSelType.Res, 1000);
            mDevice.pressBack();
            findUiObj(ANDROID_SYSTEM_BUTTON1, UiObjSelType.Res, 1000).click();
            mDevice.waitForIdle(UI_TIMEOUT_MS);
            sleep(APP_TIMEOUT_MS);
            ArrayList<Integer> sizes = getHeapSize(KAMCORD_APP_PACKAGE);
            maxDalvik = Math.max(sizes.get(0), maxDalvik);
            maxNative = Math.max(sizes.get(1), maxNative);
            minDalvik = Math.min(sizes.get(0), minDalvik);
            minNative = Math.min(sizes.get(1), minNative);
        }

        assertTrue("Dalvik heap leaked!",
                ((maxDalvik - minDalvik) / (double)minDalvik) < 0.1);
        assertTrue("Native heap leaked!",
                ((maxNative - minNative) / (double)minNative) < 0.1);
    }

    //@Test
    //Char only for now
    public void checkHeapUsageShortRecording() {
        int recordingTrials = 20;
        int recordingBase  = 100;
        doLogin();
        //get baseline
        ArrayList<Integer> dataPoints = new ArrayList<>();
        ArrayList<Integer> baseline = getHeapSize(KAMCORD_APP_PACKAGE);

        for (int i = 0; i < recordingTrials; i++) {
            recordGameVideo(RIPPLE_TEST_APP_NAME,
                    RIPPLE_TEST_APP_TITLE,
                    recordingBase + 100 * i,
                    false,
                    false,
                    true,
                    false);
            findUiObj(ANDROID_SYSTEM_BUTTON3, UiObjSelType.Res, 1000).click();
            mDevice.waitForIdle(UI_TIMEOUT_MS);
            sleep(APP_TIMEOUT_MS);
            dataPoints.addAll(getHeapSize(KAMCORD_APP_PACKAGE));
        }

        System.gc();
        sleep(APP_TIMEOUT_MS);
        ArrayList<Integer> endOfTest = getHeapSize(KAMCORD_APP_PACKAGE);
        //get baseline with short video
        //cacheSizeBefore is 1x video size. System cleans up before record.

        assertTrue("Dalvik heap leaked!",
                (Math.abs(endOfTest.get(0) - baseline.get(0)) / baseline.get(0)) < 0.1);
        assertTrue("Dalvik heap leaked!",
                (Math.abs(endOfTest.get(1) - baseline.get(1)) / baseline.get(1)) < 0.1);
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
