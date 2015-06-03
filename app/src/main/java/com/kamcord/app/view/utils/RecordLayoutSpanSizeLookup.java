package com.kamcord.app.view.utils;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by pplunkett on 6/3/15.
 */
public class RecordLayoutSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
    private RecyclerView recyclerView;

    public RecordLayoutSpanSizeLookup(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Override
    public int getSpanSize(int position) {
        int spanSize = 1;

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            spanSize = ((GridLayoutManager) recyclerView.getLayoutManager()).getSpanCount();
        }

        return spanSize;
    }
}
