package com.kamcord.app.view.utils;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

/**
 * Created by pplunkett on 7/6/15.
 */
public class VisibilityHandler extends Handler {
    private static final int FADE_DURATION_MS = 150;
    private View root;

    public VisibilityHandler(View root) {
        super(Looper.getMainLooper());
        this.root = root;
    }

    public void show(int timeoutMs, boolean fade) {
        removeAllCallbacks();

        this.post(fade ? fadeInRunnable : showRunnable);
        if( timeoutMs > 0 ) {
            this.postDelayed(fade ? fadeOutRunnable : hideRunnable, timeoutMs);
        }
    }

    public void hide(boolean fade) {
        removeAllCallbacks();
        this.post(fade ? fadeOutRunnable : hideRunnable);
    }

    private void removeAllCallbacks() {
        removeCallbacks(hideRunnable);
        removeCallbacks(showRunnable);
        removeCallbacks(fadeOutRunnable);
        removeCallbacks(fadeInRunnable);
    }

    private Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            root.animate().cancel();
            root.setAlpha(0f);
        }
    };

    private Runnable showRunnable = new Runnable() {
        @Override
        public void run() {
            root.animate().cancel();
            root.setAlpha(1f);
        }
    };

    private Runnable fadeInRunnable = new Runnable() {
        @Override
        public void run() {
            root.animate().alpha(1f).setDuration(FADE_DURATION_MS);
        }
    };

    private Runnable fadeOutRunnable = new Runnable() {
        @Override
        public void run() {
            root.animate().alpha(0f).setDuration(FADE_DURATION_MS);
        }
    };
}
