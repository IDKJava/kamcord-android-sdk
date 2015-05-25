package com.kamcord.app.utils;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by donliang1 on 5/22/15.
 */
public class DynamicRecyclerView extends RecyclerView {

    private GridLayoutManager gridLayoutManager;
    private int columnWidth = 300;

    public DynamicRecyclerView(Context context) {
        super(context);
        init(context, null);
    }

    public DynamicRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DynamicRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        gridLayoutManager = new GridLayoutManager(getContext(), 3);
        setLayoutManager(gridLayoutManager);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
            int spanCount = Math.max(3, getMeasuredWidth() / columnWidth);
            gridLayoutManager.setSpanCount(spanCount);
    }

}
