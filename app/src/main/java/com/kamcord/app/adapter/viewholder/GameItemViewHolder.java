package com.kamcord.app.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.kamcord.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pplunkett on 5/26/15.
 */
public class GameItemViewHolder extends RecyclerView.ViewHolder {

    @InjectView(R.id.gameThumbnailImageView)
    public ImageView gameThumbnailImageView;
    @InjectView(R.id.gameNameTextView)
    public TextView gameNameTextView;
    @InjectView(R.id.gameVideoCountTextView)
    public TextView gameVideoCountTextView;
    @InjectView(R.id.gameActionImageButton)
    public ImageButton gameActionImageButton;

    public GameItemViewHolder(View itemLayoutView) {
        super(itemLayoutView);
        ButterKnife.inject(this, itemLayoutView);
    }
}
