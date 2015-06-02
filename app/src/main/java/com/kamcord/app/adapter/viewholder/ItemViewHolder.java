package com.kamcord.app.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kamcord.app.R;
import com.kamcord.app.adapter.ProfileAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by donliang1 on 6/1/15.
 */
public class ItemViewHolder extends RecyclerView.ViewHolder {

    @InjectView(R.id.profile_item_title)
    TextView profileItemTitle;
    @InjectView(R.id.profile_item_author)
    TextView profileItemAuthor;
    @InjectView(R.id.profile_item_thumbnail)
    ImageView profileItemThumbnail;
    @InjectView(R.id.video_likes)
    TextView videoLikes;
    @InjectView(R.id.video_comments)
    TextView videoComments;
    @InjectView(R.id.video_views)
    TextView videoViews;

    public ItemViewHolder(final View itemLayoutView, final ProfileAdapter.OnItemClickListener mItemClickListener) {
        super(itemLayoutView);
        ButterKnife.inject(this, itemLayoutView);
        itemLayoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(itemLayoutView, getAdapterPosition());
                }
            }
        });
    }

    public TextView getProfileItemTitle() {
        return this.profileItemTitle;
    }

    public TextView getProfileItemAuthor() {
        return this.profileItemAuthor;
    }

    public ImageView getProfileItemThumbnail() {
        return this.profileItemThumbnail;
    }

    public TextView getVideoLikes() {
        return this.videoLikes;
    }

    public TextView getVideoComments() {
        return this.videoComments;
    }

    public TextView getVideoViews() {
        return this.videoViews;
    }
}
