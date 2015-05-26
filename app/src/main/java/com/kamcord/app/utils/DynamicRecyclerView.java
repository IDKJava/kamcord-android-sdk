package com.kamcord.app.utils;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.kamcord.app.adapter.GameRecordListAdapter;

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
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int spanSize = 1;
                int viewType = getAdapter().getItemViewType(position);

                switch( viewType )
                {
                    case GameRecordListAdapter.VIEW_TYPE_FIRST_INSTALLED:
                    case GameRecordListAdapter.VIEW_TYPE_INSTALLED:
                    case GameRecordListAdapter.VIEW_TYPE_LAST_INSTALLED:
                        spanSize = gridLayoutManager.getSpanCount();
                        break;

                    default:
                        spanSize = 1;
                        break;
                }
                return spanSize;
            }
        });
        setLayoutManager(gridLayoutManager);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
            int spanCount = Math.max(3, getMeasuredWidth() / columnWidth);
            gridLayoutManager.setSpanCount(spanCount);
    }

}
