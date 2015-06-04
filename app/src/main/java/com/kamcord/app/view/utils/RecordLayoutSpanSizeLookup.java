package com.kamcord.app.view.utils;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kamcord.app.model.RecordItem;

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
            int viewType = recyclerView.getAdapter().getItemViewType(position);
            RecordItem.Type type = RecordItem.Type.values()[viewType];
            switch (type) {
                case INSTALLED_HEADER:
                case NOT_INSTALLED_HEADER:
                    spanSize = ((GridLayoutManager) recyclerView.getLayoutManager()).getSpanCount();
                    break;

                default:
                    spanSize = 1;
                    break;
            }
        }

        return spanSize;
    }
}
