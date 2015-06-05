package com.kamcord.app.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;

import com.kamcord.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pplunkett on 6/3/15.
 */
public class RequestGameViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.requestGameImageButton)
    public ImageButton requestGameImageButton;

    public RequestGameViewHolder(View itemLayoutView) {
        super(itemLayoutView);
        ButterKnife.inject(this, itemLayoutView);
    }
}
