package com.kamcord.app.application;

import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

import com.kamcord.app.R;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static com.kamcord.app.testutils.UiUtilities.*;

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
            sleep(UI_TIMEOUT_MS);
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

    //Do not run fails by design!
    //@Test
    public void recordAndPostLoggedInWithPause() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RIPPLE_TEST_APP_TITLE, 2 * RECORDING_DURATION_MS, true);
        handleShareViewNotificationCheck(RECORDING_DURATION_MS);
    }

    @Test
    public void recordRippleTestLoginFirstShort() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RIPPLE_TEST_APP_TITLE, 1000, false, false, true);
        //hit the delete button
        findUiObj(ANDROID_SYSTEM_BUTTON3, UiObjSelType.Res, UI_TIMEOUT_MS).click();
        findUiObj(R.id.activity_mdrecord_layout, UiObjIdType.Res, UiObjSelType.Res);
    }

    @Test
    public void recordRippleTestLoginFirst() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS);
        handleShareViewNotificationCheck(RECORDING_DURATION_MS);

    }

    @Test
    public void recordRippleTestSkipLoginShort() {
        skipLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RIPPLE_TEST_APP_TITLE, 1000, false, false, true);
        //hit the delete button
        findUiObj(ANDROID_SYSTEM_BUTTON3, UiObjSelType.Res, UI_TIMEOUT_MS).click();
        findUiObj(R.id.activity_mdrecord_layout, UiObjIdType.Res, UiObjSelType.Res);
    }

    //TODO: Enable after resolution of AA-23
    //Works on occasion, but not stable.
    //@Test
    public void recordRippleTestLoginFirstSwitchWithNotificationClick() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME,
                RIPPLE_TEST_APP_NAME,
                RECORDING_DURATION_MS,
                false,
                true,
                false);
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
        handleShareFlowQueueCheck(RECORDING_DURATION_MS, UploadTestVariant.Interrupted);


    }

    @Test
    public void recordRippleTestLoginFirstRetryUploadNoNetwork() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS * 3);
        handleShareFlowQueueCheck(RECORDING_DURATION_MS, UploadTestVariant.NoNetwork);
    }

    @Test
    public void recordRippleTestLoginUploadCompleteFlowChecks() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS * 3);
        handleShareFlowQueueCheck(RECORDING_DURATION_MS, UploadTestVariant.Normal);
    }

    @Test
    public void recordRippleTestLoginFirstRetryUploadDelete() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS);
        handleShareFlowQueueCheck(RECORDING_DURATION_MS, UploadTestVariant.Delete);
    }

}
