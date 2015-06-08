package com.kamcord.app.view.utils;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kamcord.app.model.ProfileItem;

/**
 * Created by pplunkett on 6/5/15.
 */
public class ProfileLayoutSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
    private RecyclerView recyclerView;

    public ProfileLayoutSpanSizeLookup(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Override
    public int getSpanSize(int position) {
        int spanSize = 1;

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            int viewType = recyclerView.getAdapter().getItemViewType(position);
            ProfileItem.Type type = ProfileItem.Type.values()[viewType];
            switch(type) {
                case HEADER:
                case FOOTER:
                    spanSize = ((GridLayoutManager) layoutManager).getSpanCount();
                    break;
                default:
                    spanSize = 1;
                    break;
            }
        }

        return spanSize;
    }
}
