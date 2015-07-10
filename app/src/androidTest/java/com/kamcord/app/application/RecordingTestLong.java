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
    //TODO: Enable when mid-stream interruption of upload is possible.
    /**
     * Test record and upload flow. Interrupt upload mid-stream.
     * <p>
     *     <b>Test Sequence:</b><br>
     *     1) {@link TestBase#doLogin Log in.}<br>
     *     2) {@link RecordAndPostTestBase#recordGameVideo Record} a video.<br>
     *     3) Upload a video with an interruption in the middle (not supported, yet)<br>
     *     4) Expect the video appear on profile feed.<br>
     * </p>
     */
    public void recordRippleTestLoginFirstRetryUploadInterrupted() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS * 3);
        handleShareFlowQueueCheck(RECORDING_DURATION_MS,
                UploadTestVariant.Interrupted,
                false,
                false);
    }

    @Test
    /**
     * Test record and upload flow. Start with no network then retry when there's.
     * <p>
     *     <b>Test Sequence:</b><br>
     *     1) {@link TestBase#doLogin Log in.}<br>
     *     2) {@link RecordAndPostTestBase#recordGameVideo Record} a video.<br>
     *     3) {@link RecordAndPostTestBase#handleShareFlowQueueCheck Upload}
     *     a video after an unsuccessful upload attempt.<br>
     *     4) Expect the video appear on profile feed.<br>
     * </p>
     */
    public void recordRippleTestLoginFirstRetryUploadNoNetwork() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS * 3);
        handleShareFlowQueueCheck(RECORDING_DURATION_MS,
                UploadTestVariant.NoNetwork,
                false,
                false);
    }

    @Test
    /**
     * Test record and upload flow.
     * <p>
     *     <b>Test Sequence:</b><br>
     *     1) {@link TestBase#doLogin Log in.}<br>
     *     2) {@link RecordAndPostTestBase#recordGameVideo Record} a video.<br>
     *     3) {@link RecordAndPostTestBase#handleShareFlowQueueCheck Upload} a video.<br>
     *     4) Expect the video appear on profile feed.<br>
     * </p>
     */
    public void recordRippleTestLoginUploadCompleteFlowChecks() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS * 3);
        handleShareFlowQueueCheck(RECORDING_DURATION_MS,
                UploadTestVariant.Normal,
                false,
                false);
    }

    @Test
    /**
     * Test record and upload flow. Start with no network then delete failed video.
     * <p>
     *     <b>Test Sequence:</b><br>
     *     1) {@link TestBase#doLogin Log in.}<br>
     *     2) {@link RecordAndPostTestBase#recordGameVideo Record} a video.<br>
     *     3) {@link RecordAndPostTestBase#handleShareFlowQueueCheck Delete}
     *     video after an unsuccessful upload attempt.<br>
     *     4) Expect the video to disappear into the void.<br>
     * </p>
     */
    public void recordRippleTestLoginFirstRetryUploadDelete() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS);
        handleShareFlowQueueCheck(RECORDING_DURATION_MS,
                UploadTestVariant.Delete,
                false,
                false);
    }
}
