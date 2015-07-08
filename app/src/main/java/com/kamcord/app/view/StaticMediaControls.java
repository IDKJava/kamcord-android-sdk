package com.kamcord.app.view;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.widget.MediaController;

import com.kamcord.app.server.model.Video;
import com.kamcord.app.view.utils.VisibilityHandler;

/**
 * Created by pplunkett on 7/6/15.
 */
public class StaticMediaControls implements MediaControls {
    private MediaController mediaController;
    private VisibilityHandler visibilityHandler;

    private Video video;

    public StaticMediaControls(Context context, Video video) {
        mediaController = new MediaController(context);
        visibilityHandler = new VisibilityHandler(mediaController);
        this.video = video;
    }

    @Override
    public void setAnchorView(View anchorView) {
        mediaController.setAnchorView(anchorView);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mediaController.dispatchKeyEvent(event);
    }

    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl control) {
        mediaController.setMediaPlayer(control);
    }

    @Override
    public void setEnabled(boolean enabled) {
        mediaController.setEnabled(enabled);
    }

    @Override
    public void show(int timeout, boolean fade) {
        mediaController.show(0);
        visibilityHandler.show(timeout, fade);
    }

    @Override
    public void hide(boolean fade) {
        visibilityHandler.hide(fade);
    }

    @Override
    public boolean isShowing() {
        return mediaController.getAlpha() > 0f && mediaController.isShowing();
    }
}
