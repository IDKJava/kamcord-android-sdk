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
    /**
     * Test record and upload flow with a long video.
     * Char only
     * <p>
     *     <b>Test Sequence:</b><br>
     *     1) {@link TestBase#doLogin Log in.}<br>
     *     2) {@link RecordAndPostTestBase#recordGameVideo Record}
     *     a very long video.<br>
     *     3) {@link RecordAndPostTestBase#handleShareFlowQueueCheck Upload} video.<br>
     *     4) Expect the video to appear on the profile.<br>
     * </p>
     */
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

    //@Test
    /**
     * Basic record and share.
     * Disabled as it is redundant.
     * <p>
     *     <b>Test Sequence:</b><br>
     *     1) {@link TestBase#doLogin Log in.}<br>
     *     2) {@link RecordAndPostTestBase#recordGameVideo Record}
     *     a video.<br>
     *     3) {@link RecordAndPostTestBase#handleShareFlowQueueCheck Upload} video.<br>
     *     4) Expect the video to appear on the profile.<br>
     * </p>
     */
    public void recordRippleTestLoginFirst() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS);
        handleShareFlowQueueCheck(RECORDING_DURATION_MS,
                UploadTestVariant.Normal,
                false,
                false);

    }
    @Test
    /**
     * Basic record and share logging in before share.
     * <p>
     *     <b>Test Sequence:</b><br>
     *     1) {@link TestBase#skipLogin Skip log in.}<br>
     *     2) {@link RecordAndPostTestBase#recordGameVideo Record}
     *     a video.<br>
     *     3) {@link RecordAndPostTestBase#handleShareFlowQueueCheck Upload}
     *     video while logging in<br>
     *     4) Expect the video to appear on the profile.<br>
     * </p>
     */
    public void recordRippleTestLoginLast() {
        skipLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS);
        handleShareFlowQueueCheck(RECORDING_DURATION_MS,
                UploadTestVariant.Normal,
                false,
                true);
    }
    //@Test
    /**
     * Basic record and share logging in before share repeated twice in a row.
     * Seems redundant and hence disabled. May review later.
     * <p>
     *     <b>Test Sequence:</b><br>
     *     1) {@link TestBase#doLogin Log in.}<br>
     *     2) {@link RecordAndPostTestBase#recordGameVideo Record} a video.<br>
     *     3) {@link RecordAndPostTestBase#handleShareFlowQueueCheck Upload} video<br>
     *     4) Repeat 2-3 two times.<br>
     *     4) Expect the videos to appear on the profile.<br>
     * </p>
     */
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
            sleep(UI_TIMEOUT_MS);
        }
        recordGameVideo(RIPPLE_TEST_APP_NAME, RIPPLE_TEST_APP_TITLE, recordingDuration);
        handleShareFlowQueueCheck(recordingDuration,
                UploadTestVariant.Normal,
                false,
                false);
    }

    //Do not run fails by design!
    //@Test
    /**
     * Basic record and share, where recording is paused every 2 secs.
     * Pausing too often isn't supported, may revise later, if need be.
     * <p>
     *     <b>Test Sequence:</b><br>
     *     1) {@link TestBase#doLogin Log in.}<br>
     *     2) {@link RecordAndPostTestBase#recordGameVideo Record}
     *     a video.<br>
     *     3){@link RecordAndPostTestBase#handleShareFlowQueueCheck Upload} video<br>
     *     4) Expect the videos to appear on the profile.<br>
     *
     */
    public void recordAndPostLoggedInWithPause() {
        doLogin();
        int recordingDuration = RECORDING_DURATION_MS * 2;
        recordGameVideo(RIPPLE_TEST_APP_NAME, RIPPLE_TEST_APP_TITLE, recordingDuration, true);
        handleShareFlowQueueCheck(recordingDuration,
                UploadTestVariant.Normal,
                false,
                false);
    }
}
