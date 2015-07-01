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
    /**
     * Basic record with video length <3 secs.
     * <p>
     *     <b>Test Sequence:</b><br>
     *     1) {@link TestBase#doLogin Log in.}<br>
     *     2) {@link RecordAndPostTestBase#recordGameVideo Record} a video.<br>
     *     3) Agree to discard short video.<br>
     *     4) Expect to be taken to the recording tab.<br>
     *
     */
    public void recordRippleTestLoginFirstShort() {
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RIPPLE_TEST_APP_TITLE, 1000, false, false, true, false);
        //hit the delete button
        findUiObj(ANDROID_SYSTEM_BUTTON3, UiObjSelType.Res, UI_TIMEOUT_MS).click();
        findUiObj(R.id.activity_mdrecord_layout, UiObjIdType.Res, UiObjSelType.Res);
    }


    @Test
    /**
     * Basic record with video length <3 secs, user not logged in.
     * <p>
     *     <b>Test Sequence:</b><br>
     *     1) {@link TestBase#skipLogin Skip log in.}<br>
     *     2) {@link RecordAndPostTestBase#recordGameVideo Record} a video.<br>
     *     3) Agree to discard short video.<br>
     *     4) Expect to be taken to the recording tab.<br>
     *
     */
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
    /**
     * Basic record and share with app switching through notification click.
     * <p>
     *     <b>Test Sequence:</b><br>
     *     1) {@link TestBase#doLogin Log in.}<br>
     *     2) {@link RecordAndPostTestBase#recordGameVideo Record} a video.<br>
     *     3) {@link RecordAndPostTestBase#handleShareFlowQueueCheck Upload} video.<br>
     *     4) Expect video to appear on profile tab.<br>
     *
     */
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
    /**
     * Basic record and share with app switching through notification click.
     * <p>
     *     <b>Test Sequence:</b><br>
     *     1) {@link TestBase#doLogin Log in.}<br>
     *     2) {@link RecordAndPostTestBase#recordGameVideo Record} a video.<br>
     *     3) Click play on share view, before stitching ends.<br>
     *     4) Expect nothing to happen.<br>
     *
     */
    public void recordRippleTestLoginFirstPrematurePlay() {
        doLogin();
        int recordingDuration = 10000;
        recordGameVideo(RIPPLE_TEST_APP_NAME, recordingDuration);
        //TODO: Click on processing tag and see what will happen.
        findUiObj(R.id.thumbnailImageView, UiObjIdType.Res, UiObjSelType.Res).click();
        findUiObj(R.id.fragment_share_layout, UiObjIdType.Res, UiObjSelType.Res);


    }

    @Test
    /**
     * Basic record and share with status checks for upload/recording, etc.
     * through notifications
     * <p>
     *     <b>Test Sequence:</b><br>
     *     1) {@link TestBase#doLogin Log in.}<br>
     *     2) {@link RecordAndPostTestBase#recordGameVideo Record} a video.<br>
     *     3) {@link RecordAndPostTestBase#handleShareFlowQueueCheck Upload} video.<br>
     *     4) Expect video to appear on profile tab.
     *
     */
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
