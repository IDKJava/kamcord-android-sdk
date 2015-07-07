package com.kamcord.app.view;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.RelativeLayout;

import com.kamcord.app.R;
import com.kamcord.app.view.utils.VisibilityHandler;

/**
 * Created by pplunkett on 7/6/15.
 */
public class LiveMediaControls implements MediaControls {
    private RelativeLayout root;
    private MediaController.MediaPlayerControl player;
    private VisibilityHandler visibilityHandler;

    public LiveMediaControls(Context context) {
        root = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.view_live_media_controls, null);
        visibilityHandler = new VisibilityHandler(root);
    }

    @Override
    public void setAnchorView(View anchorView) {
        if( anchorView instanceof ViewGroup ) {
            ((ViewGroup) anchorView).addView(root);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return root.dispatchKeyEvent(event);
    }

    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
        this.player = player;
    }

    @Override
    public void setEnabled(boolean enabled) {
        root.setEnabled(enabled);
    }

    @Override
    public void show(int timeout, boolean fade) {
        if( fade ) {
            visibilityHandler.show(timeout);
        } else {
            root.setAlpha(1f);
        }
    }

    @Override
    public void hide(boolean fade) {
        if( fade ) {
            visibilityHandler.hide();
        } else {
            root.setAlpha(0f);
        }
    }

    @Override
    public boolean isShowing() {
        return root.getAlpha() > 0f;
    }
}
