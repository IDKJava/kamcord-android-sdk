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
public class RecordingTestShort extends RecordAndPostTestBase {

    @Test
    public void recordRippleTestLoginFirstShort() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RIPPLE_TEST_APP_TITLE, 1000, false, false, true, false);
        //hit the delete button
        findUiObj(ANDROID_SYSTEM_BUTTON3, UiObjSelType.Res, UI_TIMEOUT_MS).click();
        findUiObj(R.id.activity_mdrecord_layout, UiObjIdType.Res, UiObjSelType.Res);
    }


    @Test
    public void recordRippleTestSkipLoginShort() {
        skipLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RIPPLE_TEST_APP_TITLE, 1000, false, false, true, false);
        //hit the delete button
        findUiObj(ANDROID_SYSTEM_BUTTON3, UiObjSelType.Res, UI_TIMEOUT_MS).click();
        findUiObj(R.id.activity_mdrecord_layout, UiObjIdType.Res, UiObjSelType.Res);
    }

    //TODO: Enable after resolution of AA-23
    //Works on occasion, but not stable.
    //@Test
    public void recordRippleTestLoginFirstSwitchWithNotificationClick() {
        doLogin();
        int recordingDuration = RECORDING_DURATION_MS * 3;
        recordGameVideo(RIPPLE_TEST_APP_NAME,
                RIPPLE_TEST_APP_NAME,
                recordingDuration,
                false,
                true,
                false,
                false);
        handleShareFlowQueueCheck(recordingDuration, UploadTestVariant.Normal, true, false);

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
    public void recordRippleTestLoginFirstCheckNotifications() {
        doLogin();
        int recordingDuration = RECORDING_DURATION_MS * 3;
        recordGameVideo(RIPPLE_TEST_APP_NAME,
                RIPPLE_TEST_APP_TITLE,
                recordingDuration,
                false,
                false,
                false,
                false);
        handleShareFlowQueueCheck(recordingDuration,
                UploadTestVariant.Normal,
                true,
                false);
        //handleShareViewNotificationCheck(RECORDING_DURATION_MS);

    }

}
