package com.kamcord.app.application;

import org.junit.Test;

import static com.kamcord.app.testutils.UiUtilities.MS_PER_MIN;
import static com.kamcord.app.testutils.UiUtilities.RECORDING_DURATION_MS;
import static com.kamcord.app.testutils.UiUtilities.RIPPLE_TEST_APP_NAME;
import static com.kamcord.app.testutils.UiUtilities.RIPPLE_TEST_APP_TITLE;
import static com.kamcord.app.testutils.UiUtilities.UI_TIMEOUT_MS;
import static com.kamcord.app.testutils.UiUtilities.sleep;

/**
 * Created by Mehmet on 6/23/15.
 */
public class RecordingTestMedium extends RecordAndPostTestBase {
    //@Test
    public void recordRippleTestLoginFirstLong() {
        doLogin();
        //TODO: What do we do with the really long tests?
        int recordingDuration = 60 * MS_PER_MIN;
        recordGameVideo(RIPPLE_TEST_APP_NAME, RIPPLE_TEST_APP_TITLE, recordingDuration);
        handleShareFlowQueueCheck(recordingDuration,
                UploadTestVariant.Normal,
                false,
                false);
        //handleShareViewNotificationCheck(recordingDuration);
    }

    @Test
    public void recordRippleTestLoginFirst() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS);
        handleShareFlowQueueCheck(RECORDING_DURATION_MS,
                UploadTestVariant.Normal,
                false,
                false);
        //handleShareViewNotificationCheck(RECORDING_DURATION_MS);

    }
    @Test
    public void recordRippleTestLoginLast() {
        skipLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS);
        handleShareFlowQueueCheck(RECORDING_DURATION_MS,
                UploadTestVariant.Normal,
                false,
                true);
        //handleShareViewNotificationCheck(RECORDING_DURATION_MS, false);

    }
    //Enable after resolution of AA-41
    //@Test
    public void recordRippleTestNTimesLoggedIn() {
        doLogin();
        int N = 2;
        int recordingDuration = RECORDING_DURATION_MS;
        for (int i = 0; i < N - 1; i++) {
            recordGameVideo(RIPPLE_TEST_APP_NAME, RIPPLE_TEST_APP_TITLE, recordingDuration);
            handleShareFlowQueueCheck(recordingDuration,
                    UploadTestVariant.Normal,
                    false,
                    false);
            //handleShareViewNotificationCheck(recDuration, true, false);
            sleep(UI_TIMEOUT_MS);
        }
        recordGameVideo(RIPPLE_TEST_APP_NAME, RIPPLE_TEST_APP_TITLE, recordingDuration);
        //handleShareViewNotificationCheck(recDuration, true, true);
        handleShareFlowQueueCheck(recordingDuration,
                UploadTestVariant.Normal,
                false,
                false);
    }

    //Do not run fails by design!
    //@Test
    public void recordAndPostLoggedInWithPause() {
        doLogin();
        int recordingDuration = RECORDING_DURATION_MS * 2;
        recordGameVideo(RIPPLE_TEST_APP_NAME, RIPPLE_TEST_APP_TITLE, recordingDuration, true);
        handleShareFlowQueueCheck(recordingDuration,
                UploadTestVariant.Normal,
                false,
                false);
        //handleShareViewNotificationCheck(RECORDING_DURATION_MS);
    }
}
