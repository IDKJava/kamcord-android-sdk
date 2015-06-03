package com.kamcord.app.utils;

import android.support.v7.widget.RecyclerView;

/**
 * Created by donliang1 on 5/29/15.
 */
public interface RecyclerViewScrollListener {
    void onRecyclerViewScrollStateChanged(RecyclerView recyclerView, int state);
    void onRecyclerViewScrolled(RecyclerView recyclerView, int dx, int dy);
}
