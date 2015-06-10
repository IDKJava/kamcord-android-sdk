package com.kamcord.app.application;

import android.util.EventLogTags;

import com.kamcord.app.R;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;



import static com.kamcord.app.testutils.SystemUtilities.*;
import static com.kamcord.app.testutils.UiUtilities.*;


/**
 * Created by Mehmet on 6/3/15.
 */
public class TestFailureRule extends TestWatcher {

    public TestFailureRule(){
        super();
    }

    @Override
    protected void failed(Throwable e, Description description) {
        stopService(com.kamcord.app.service.RecordingService.class);
        stopService(com.kamcord.app.service.UploadService.class);
        //closeApp(RIPPLE_TEST_APP_PACKAGE);
    }
}
