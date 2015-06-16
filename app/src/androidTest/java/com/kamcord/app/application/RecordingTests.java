package com.kamcord.app.application;

import com.kamcord.app.R;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static com.kamcord.app.testutils.UiUtilities.*;
import static com.kamcord.app.testutils.SystemUtilities.*;

/**
 * Created by Mehmet on 5/29/15.
 */
public class RecordingTests extends RecordAndPostTestBase {

    //@Test
    public void recordRippleTestLoginFirstLong() {
        doLogin();
        //TODO: What do we do with the really long tests?
        int recordingDuration = 60 * MS_PER_MIN;
        recordGameVideo(RIPPLE_TEST_APP_NAME, RIPPLE_TEST_APP_TITLE, recordingDuration);
        handleShareViewNotificationCheck(recordingDuration);
    }

    @Test
    public void recordRippleTestNTimesLoggedIn() {
        doLogin();
        int N = 5;
        int recDuration = RECORDING_DURATION_MS * 3;
        for (int i = 0; i < N - 1; i++) {
            recordGameVideo(RIPPLE_TEST_APP_NAME, RIPPLE_TEST_APP_TITLE, recDuration);
            handleShareViewNotificationCheck(recDuration, true, false);
        }
        recordGameVideo(RIPPLE_TEST_APP_NAME, RIPPLE_TEST_APP_TITLE, recDuration);
        handleShareViewNotificationCheck(recDuration, true, true);
    }

    @Test
    public void recordRippleTestLoginLast() {
        skipLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS);
        handleShareViewNotificationCheck(RECORDING_DURATION_MS, false);

    }

    //TODO: Enable when test conditions are clear!
    //@Test
    public void recordAndPostLoggedInWithPause() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RIPPLE_TEST_APP_TITLE, 2 * RECORDING_DURATION_MS, true);
        handleShareViewNotificationCheck(RECORDING_DURATION_MS);
    }

    @Test
    public void recordRippleTestLoginFirstShort() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, 1000);
        findUiObj(R.id.activity_mdrecord_layout, UiObjIdType.Res, UiObjSelType.Res);
    }

    @Test
    public void recordRippleTestSkipLoginShort() {
        skipLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, 1000);
        findUiObj(ANDROID_SYSTEM_BUTTON1, UiObjSelType.Res).click();
    }

    @Test
    public void recordRippleTestLoginFirst() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS);
        handleShareViewNotificationCheck(RECORDING_DURATION_MS);

    }

    //TODO: Enable after resolution of AA-23
    @Test
    public void recordRippleTestLoginFirstSwitchWithNotificationClick() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME,
                RIPPLE_TEST_APP_NAME,
                RECORDING_DURATION_MS,
                false,
                true);
        handleShareViewNotificationCheck(RECORDING_DURATION_MS);

    }

    @Test
    public void recordRippleTestLoginFirstPrematurePlay() {
        doLogin();
        int recordingDuration = 10000;
        recordGameVideo(RIPPLE_TEST_APP_NAME, recordingDuration);
        //TODO: Click on processing tag and see what will happen.
        findUiObj(R.id.thumbnailImageView, UiObjIdType.Res, UiObjSelType.Res).click();
        findUiObj(R.id.fragment_share_layout, UiObjIdType.Res, UiObjSelType.Res);


    }

    @Test
    public void recordRippleTestLoginFirstRetryUploadInterrupted() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS * 3);
        handleShareViewQueueCheck(RECORDING_DURATION_MS, true, UploadTestVariant.Interrupted);


    }
    @Test
    public void recordRippleTestLoginFirstRetryUploadNoNetwork() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS * 3);
        handleShareViewQueueCheck(RECORDING_DURATION_MS, true, UploadTestVariant.NoNetwork);
    }

    @Test
    public void recordRippleTestLoginUploadCompleteFlowChecks() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS * 3);
        handleShareViewQueueCheck(RECORDING_DURATION_MS, true, UploadTestVariant.Normal);
    }
    @Test
    public void recordRippleTestLoginFirstRetryUploadDelete() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS);
        handleShareViewQueueCheck(RECORDING_DURATION_MS, true, UploadTestVariant.Delete);
    }

}
