package com.kamcord.app.testutils.testrules;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import static com.kamcord.app.testutils.SystemUtilities.stopService;

/**
 * Created by Mehmet on 6/11/15.
 */
public class FailureRule extends TestWatcher {

    public FailureRule(){
        super();
    }

    @Override
    protected void failed(Throwable e, Description description) {
        stopService(com.kamcord.app.service.RecordingService.class);
        stopService(com.kamcord.app.service.UploadService.class);
        //stopActivity(com.kamcord.app.activity.LoginActivity.class);
        //stopActivity(com.kamcord.app.activity.ProfileVideoViewActivity.class);
        //stopActivity(com.kamcord.app.activity.RecordActivity.class);
        //stopActivity(com.kamcord.app.activity.VideoPreviewActivity.class);
        //closeApp(RIPPLE_TEST_APP_PACKAGE);
    }
}