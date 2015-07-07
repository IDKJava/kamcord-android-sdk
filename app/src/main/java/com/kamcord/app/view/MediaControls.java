package com.kamcord.app.view;

import android.view.KeyEvent;
import android.view.View;
import android.widget.MediaController;

/**
 * Created by pplunkett on 7/6/15.
 */
public interface MediaControls {
    void setAnchorView(View anchorView);
    boolean dispatchKeyEvent(KeyEvent event);
    void setMediaPlayer(MediaController.MediaPlayerControl control);
    void setEnabled(boolean enabled);
    void show(int timeout, boolean fade);
    void hide(boolean fade);
    boolean isShowing();
}
