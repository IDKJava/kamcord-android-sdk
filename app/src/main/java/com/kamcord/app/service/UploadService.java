package com.kamcord.app.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.kamcord.app.model.Video;

public class UploadService extends IntentService {
    private static final String TAG = RecordingService.class.getSimpleName();

    public static final String ARG_VIDEO_TO_SHARE = "video_to_share";

    public UploadService() {
        super("Kamcord Upload Service");
        this.setIntentRedelivery(true);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.v("FindMe", "UploadService received intent " + intent);
        Video videoToShare = intent.getParcelableExtra(ARG_VIDEO_TO_SHARE);
        Log.v("FindMe", "  with video " + videoToShare.toString());

    }


}
