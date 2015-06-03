package com.kamcord.app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.kamcord.app.R;
import com.kamcord.app.utils.ViewUtils;
import com.kamcord.app.view.utils.RecordLayoutSpanSizeLookup;

/**
 * Created by donliang1 on 5/22/15.
 */
public class DynamicRecyclerView extends RecyclerView {

    private GridLayoutManager gridLayoutManager;
    private int columnWidth = 0;
    private int columnNumber = 3;

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
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.DynamicRecyclerView);
            columnWidth = array.getDimensionPixelSize(R.styleable.DynamicRecyclerView_minGridItemWidth, ViewUtils.dpToPx(getContext(), 150));
            array.recycle();
        }
        gridLayoutManager = new GridLayoutManager(getContext(), columnNumber);
        gridLayoutManager.setSpanSizeLookup(new RecordLayoutSpanSizeLookup(this));
        setLayoutManager(gridLayoutManager);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        int spanCount = Math.max(columnNumber, getMeasuredWidth() / columnWidth);
        gridLayoutManager.setSpanCount(spanCount);
    }

}
