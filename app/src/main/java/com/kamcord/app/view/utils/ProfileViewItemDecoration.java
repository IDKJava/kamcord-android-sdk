package com.kamcord.app.view.utils;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.kamcord.app.model.ProfileItem;

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
        int viewType = parent.getAdapter().getItemViewType(adapterPosition);
        ProfileItem.Type type = ProfileItem.Type.values()[viewType];
        if( type == ProfileItem.Type.HEADER ) {
            outRect.top = 0;
            outRect.left = 0;
            outRect.right = 0;
        }
    }
}
