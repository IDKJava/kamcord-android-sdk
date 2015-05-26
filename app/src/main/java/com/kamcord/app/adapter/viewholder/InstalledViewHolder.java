package com.kamcord.app.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.kamcord.app.R;
import com.kamcord.app.adapter.GameRecordListAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pplunkett on 5/26/15.
 */
public class InstalledViewHolder extends RecyclerView.ViewHolder {

    @InjectView(R.id.gameThumbnailImageView)
    public ImageView gameThumbnailImageView;
    @InjectView(R.id.gameNameTextView)
    public TextView gameNameTextView;
    @InjectView(R.id.gameFollowerCountTextView)
    public TextView gameFollowerCountTextView;
    @InjectView(R.id.recordImageButton)
    public ImageButton recordImageButton;

    public InstalledViewHolder(final View itemLayoutView, final GameRecordListAdapter.OnItemClickListener listener) {
        super(itemLayoutView);
        ButterKnife.inject(this, itemLayoutView);
        itemLayoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(itemLayoutView, getAdapterPosition());
                }
            }
        });
    }
}
