package com.kamcord.app.application;

import org.junit.Test;

/**
 * Created by Mehmet on 5/27/15.
 */
public class RecordAndPotsLoggedInLong extends RecordAndPostTestBase{

    @Test
    public void recordRippleTestLoginFirstLong(){
        doLogin();
        //TODO: What do we do with the really long tests?
        int recordingDuration = 10 * MS_PER_MIN;
        recordGameVideo(RIPPLE_TEST_APP_NAME, recordingDuration);
        handleShareView(recordingDuration);
    }
}
