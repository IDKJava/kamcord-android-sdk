package com.kamcord.app.application;

        import org.junit.Test;

/**
 * Created by Mehmet on 5/27/15.
 */
public class RecordAndPostLoggedIn extends RecordAndPostTestBase {
    @Test
    public void recordRippleTestLoginFirst(){
        doLogin();
        recordGameVideo(RIPPLE_TEST_APP_NAME, RECORDING_DURATION_MS);
        handleShareView(RECORDING_DURATION_MS);

    }
}
