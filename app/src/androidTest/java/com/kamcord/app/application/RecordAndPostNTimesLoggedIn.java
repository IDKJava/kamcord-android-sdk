package com.kamcord.app.application;

import org.junit.Test;

/**
 * Created by Mehmet on 5/28/15.
 */
public class RecordAndPostNTimesLoggedIn extends RecordAndPostTestBase {


    @Test
    public void recordRippleTestNTimesLoggedIn() {
        doLogin();
        int N = 2;
        for(int i=0; i < N; i++) {
            recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS);
            handleShareView(RECORDING_DURATION_MS);
            recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS);
            handleShareView(RECORDING_DURATION_MS);
        }
    }


}
