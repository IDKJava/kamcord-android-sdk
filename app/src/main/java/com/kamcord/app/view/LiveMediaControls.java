package com.kamcord.app.view;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.RelativeLayout;

import com.kamcord.app.R;
import com.kamcord.app.server.model.User;
import com.kamcord.app.view.utils.VisibilityHandler;

/**
 * Created by pplunkett on 7/6/15.
 */
public class LiveMediaControls implements MediaControls {
    private RelativeLayout root;
    private VisibilityHandler visibilityHandler;

    private User owner;

    public LiveMediaControls(Context context, User owner) {
        root = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.view_live_media_controls, null);
        visibilityHandler = new VisibilityHandler(root);
        this.owner = owner;
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
    }

    @Override
    public void setEnabled(boolean enabled) {
        root.setEnabled(enabled);
    }

    @Override
    public void show(int timeout, boolean fade) {
        visibilityHandler.show(owner == null || owner.is_user_following ? timeout : 0, fade);
    }

    @Override
    public void hide(boolean fade) {
        visibilityHandler.hide(fade );
    }

    @Override
    public boolean isShowing() {
        return root.getAlpha() > 0f;
    }
}
