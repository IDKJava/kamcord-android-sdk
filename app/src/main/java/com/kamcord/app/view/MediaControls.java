package com.kamcord.app.view;

import android.view.KeyEvent;
import android.view.View;
import android.widget.MediaController;

import com.kamcord.app.player.Player;

/**
 * Created by pplunkett on 7/6/15.
 */
public interface MediaControls extends Player.Listener {
    void setAnchorView(View anchorView);
    void setControlButtonClickListener(LiveMediaControls.ControlButtonClickListener listener);
    boolean dispatchKeyEvent(KeyEvent event);
    void setMediaPlayer(MediaController.MediaPlayerControl control);
    void setEnabled(boolean enabled);
    void show(int timeout, boolean fade);
    void hide(boolean fade);
    boolean isShowing();

    interface ControlButtonClickListener {
        void onPlayButtonClicked();
        void onPauseButtonClicked();
        void onReplayButtonClicked();
    }
}
