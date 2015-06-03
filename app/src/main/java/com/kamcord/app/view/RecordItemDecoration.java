package com.kamcord.app.view;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

/**
 * Created by pplunkett on 6/3/15.
 */
public class RecordItemDecoration extends RecyclerView.ItemDecoration {

    private int spacing = 0;
    public RecordItemDecoration(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        super.getItemOffsets(outRect, view, parent, state);

        int halfSpacing = spacing / 2;

        int position = parent.getChildAdapterPosition(view);
        int totalSpan = getTotalSpan(parent);
        int row = getRow(position, totalSpan, parent);
        int col = getCol(position, totalSpan, parent);
        int childSpan = getChildSpan(position, parent);

        /* INVALID SPAN */
        if (totalSpan < 1) return;

        outRect.top = halfSpacing;
        outRect.bottom = halfSpacing;
        outRect.left = halfSpacing;
        outRect.right = halfSpacing;

        if (isTopEdge(row)) {
            outRect.top = spacing;
        }

        if (isLeftEdge(col)) {
            outRect.left = spacing;
        }

        if (isRightEdge(col, childSpan, totalSpan)) {
            outRect.right = spacing;
        }

//        if (isBottomEdge(spanIndex, childCount, spanCount)) {
//            outRect.bottom = spacing;
//        }
    }

    protected int getTotalSpan(RecyclerView parent) {
        int out = -1;
        RecyclerView.LayoutManager mgr = parent.getLayoutManager();
        if (mgr instanceof GridLayoutManager) {
            out = ((GridLayoutManager) mgr).getSpanCount();
        } else if (mgr instanceof StaggeredGridLayoutManager) {
            out = ((StaggeredGridLayoutManager) mgr).getSpanCount();
        }
        return out;
    }

    protected int getRow(int position, int totalSpan, RecyclerView parent) {
        int out = -1;
        RecyclerView.LayoutManager mgr = parent.getLayoutManager();
        if (mgr instanceof GridLayoutManager) {
            out = ((GridLayoutManager) mgr).getSpanSizeLookup().getSpanGroupIndex(position, totalSpan);
        } else if (mgr instanceof StaggeredGridLayoutManager) {
            out = position / totalSpan;
        }
        return out;
    }

    protected int getCol(int position, int totalSpan, RecyclerView parent) {
        int out = -1;
        RecyclerView.LayoutManager mgr = parent.getLayoutManager();
        if (mgr instanceof GridLayoutManager) {
            out = ((GridLayoutManager) mgr).getSpanSizeLookup().getSpanIndex(position, totalSpan);
        } else if (mgr instanceof StaggeredGridLayoutManager) {
            out = totalSpan % position;
        }
        return out;
    }

    protected int getChildSpan(int position, RecyclerView parent) {
        int out = -1;
        RecyclerView.LayoutManager mgr = parent.getLayoutManager();
        if (mgr instanceof GridLayoutManager) {
            out = ((GridLayoutManager) mgr).getSpanSizeLookup().getSpanSize(position);
        } else if (mgr instanceof StaggeredGridLayoutManager) {
            out = 1;
        }
        return out;
    }

    protected boolean isTopEdge(int row) {
        return row == 0;
    }

    protected boolean isLeftEdge(int col) {
        return col == 0;
    }

    protected boolean isRightEdge(int col, int childSpan, int totalSpan) {
        return col == totalSpan - childSpan;
    }

    protected boolean isBottomEdge(int row, int totalItems) {
        return row == totalItems - 1;
    }
}
