package com.kamcord.app.view;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.widget.MediaController;

/**
 * Created by pplunkett on 7/6/15.
 */
public class StaticMediaControls implements MediaControls {
    private MediaController mediaController;

    public StaticMediaControls(Context context) {
        mediaController = new MediaController(context);
    }

    @Override
    public void setAnchorView(View view) {
        mediaController.setAnchorView(view);
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
    public void show() {
        mediaController.show();
    }

    @Override
    public void show(int timeout) {
        mediaController.show(timeout);
    }

    @Override
    public void hide() {
        mediaController.hide();
    }

    @Override
    public boolean isShowing() {
        return mediaController.isShowing();
    }
}
