package com.kamcord.app.application;

import org.junit.Test;

/**
 * Created by Mehmet on 5/27/15.
 */
public class RecordAndPotsLoggedInLong extends RecordAndPostTestBase{

    @Test
    public void recordRippleTestLoginFirstLong(){
        doLogin();
        recordGame(RIPPLE_TEST_APP_NAME, 10 * MS_PER_MIN);
    }
}
