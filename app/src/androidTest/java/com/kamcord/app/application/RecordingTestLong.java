package com.kamcord.app.application;

import org.junit.Test;

import static com.kamcord.app.testutils.UiUtilities.MS_PER_MIN;
import static com.kamcord.app.testutils.UiUtilities.RECORDING_DURATION_MS;
import static com.kamcord.app.testutils.UiUtilities.RIPPLE_TEST_APP_NAME;
import static com.kamcord.app.testutils.UiUtilities.RIPPLE_TEST_APP_TITLE;
import static com.kamcord.app.testutils.UiUtilities.UI_TIMEOUT_MS;
import static com.kamcord.app.testutils.UiUtilities.sleep;

/**
 * Created by Mehmet on 6/22/15.
 */
public class RecordingTestLong extends RecordAndPostTestBase {



    //@Test
    //Disable network connection makes the app freeze: Bug or not?
    public void recordRippleTestLoginFirstRetryUploadInterrupted() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS * 3);
        handleShareFlowQueueCheck(RECORDING_DURATION_MS,
                UploadTestVariant.Interrupted,
                false,
                false);
    }

    @Test
    public void recordRippleTestLoginFirstRetryUploadNoNetwork() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS * 3);
        handleShareFlowQueueCheck(RECORDING_DURATION_MS,
                UploadTestVariant.NoNetwork,
                false,
                false);
    }

    @Test
    public void recordRippleTestLoginUploadCompleteFlowChecks() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS * 3);
        handleShareFlowQueueCheck(RECORDING_DURATION_MS,
                UploadTestVariant.Normal,
                false,
                false);
    }

    @Test
    public void recordRippleTestLoginFirstRetryUploadDelete() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS);
        handleShareFlowQueueCheck(RECORDING_DURATION_MS,
                UploadTestVariant.Delete,
                false,
                false);
    }
}
