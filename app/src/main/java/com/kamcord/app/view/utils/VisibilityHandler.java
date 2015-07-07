package com.kamcord.app.view.utils;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

/**
 * Created by pplunkett on 7/6/15.
 */
public class VisibilityHandler extends Handler {
    private View root;

    public VisibilityHandler(View root) {
        super(Looper.getMainLooper());
        this.root = root;
    }

    public void show(int timeoutMs, boolean fade) {
        this.removeCallbacks(showRunnable);
        this.removeCallbacks(hideRunnable);

        this.post(showRunnable);
        if( timeoutMs > 0 ) {
            this.postDelayed(hideRunnable, timeoutMs);
        }
    }

    public void hide(boolean fade) {
        this.removeCallbacks(showRunnable);
        this.removeCallbacks(hideRunnable);
        this.post(hideRunnable);
    }

    private Runnable hideRunnable = new Runnable() {
        int duration = 200;
        @Override
        public void run() {
            root.animate().alpha(0f).setDuration(duration);
        }
    };

    private Runnable showRunnable = new Runnable() {
        int duration = 200;
        @Override
        public void run() {
            root.animate().alpha(1f).setDuration(duration);
        }
    };
}
