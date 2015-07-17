package com.kamcord.app.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.kamcord.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by donliang1 on 15/7/14.
 */
public class LiveStreamHeaderViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.livestreamHeaderTextView)
    public TextView livestreamHeaderTextView;

    public LiveStreamHeaderViewHolder(View itemLayoutView) {
        super(itemLayoutView);
        ButterKnife.inject(this, itemLayoutView);
    }

}