package com.kamcord.app.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.RelativeLayout;

import com.kamcord.app.R;

/**
 * Created by pplunkett on 7/6/15.
 */
public class LiveMediaControls implements MediaControls {
    private RelativeLayout root;
    private MediaController.MediaPlayerControl player;
    private VisibilityHandler visibilityHandler = new VisibilityHandler(Looper.getMainLooper());

    public LiveMediaControls(Context context) {
        root = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.view_live_media_controls, null);
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
    public void show() {
        visibilityHandler.show();
    }

    @Override
    public void show(int timeoutMs) {
        visibilityHandler.show(timeoutMs);
    }

    @Override
    public void hide() {
        visibilityHandler.hide();
    }

    @Override
    public boolean isShowing() {
        return root.getVisibility() == View.VISIBLE;
    }

    private class VisibilityHandler extends Handler {
        public VisibilityHandler(Looper looper) {
            super(looper);
        }

        public void show() {
            show(0);
        }

        public void show(int timeoutMs) {
            this.removeCallbacks(showRunnable);
            this.post(showRunnable);

            this.removeCallbacks(hideRunnable);
            if( timeoutMs > 0 ) {
                this.postDelayed(hideRunnable, timeoutMs);
            }
        }

        public void hide() {
            this.removeCallbacks(hideRunnable);
            this.post(hideRunnable);
        }
    }

    private Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            root.setVisibility(View.INVISIBLE);
        }
    };

    private Runnable showRunnable = new Runnable() {
        @Override
        public void run() {
            root.setVisibility(View.VISIBLE);
        }
    };
}
