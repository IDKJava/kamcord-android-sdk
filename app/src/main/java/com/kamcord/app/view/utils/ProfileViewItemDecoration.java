package com.kamcord.app.view.utils;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.kamcord.app.model.FeedItem;

/**
 * Created by pplunkett on 6/5/15.
 */
public class ProfileViewItemDecoration extends GridViewItemDecoration {

    public ProfileViewItemDecoration(int spacing) {
        super(spacing);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int adapterPosition = parent.getChildAdapterPosition(view);
        if( adapterPosition == RecyclerView.NO_POSITION ) {
            return;
        }

        int viewType = parent.getAdapter().getItemViewType(adapterPosition);
        FeedItem.Type type = FeedItem.Type.values()[viewType];
        if( type == FeedItem.Type.HEADER || type == FeedItem.Type.UPLOAD_PROGRESS ) {
            outRect.top = 0;
            outRect.left = 0;
            outRect.right = 0;
        }
    }
}
