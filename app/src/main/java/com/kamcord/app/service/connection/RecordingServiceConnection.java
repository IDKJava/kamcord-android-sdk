package com.kamcord.app.service.connection;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.media.projection.MediaProjection;
import android.os.IBinder;

import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.service.RecordingService;

/**
 * Created by pplunkett on 5/20/15.
 */
public class RecordingServiceConnection implements ServiceConnection {
    private RecordingService recordingService;
    private MediaProjection mediaProjection = null;
    private RecordingSession recordingSession = null;
    private boolean isConnected = false;

    public void initializeForRecording(MediaProjection mediaProjection, RecordingSession recordingSession) {
        this.mediaProjection = mediaProjection;
        this.recordingSession = recordingSession;
    }

    public void uninitialize() {
        this.mediaProjection = null;
        this.recordingSession = null;
    }

    public boolean isInitializedForRecording() {
        return mediaProjection != null && recordingSession != null;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public RecordingSession getServiceRecordingSession()
    {
        return recordingService != null ? recordingService.getRecordingSession() : null;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        recordingService = ((RecordingService.LocalBinder) iBinder).getService();
        if (isInitializedForRecording()) {
            recordingService.startRecording(mediaProjection, recordingSession);
            uninitialize();
        }
        isConnected = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        uninitialize();
        isConnected = false;
    }
}
