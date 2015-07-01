package com.kamcord.app.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kamcord.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by donliang1 on 6/1/15.
 */
public class ProfileStreamItemViewHolder extends RecyclerView.ViewHolder {

    @InjectView(R.id.stream_item_title)
    TextView streamItemTitle;
    @InjectView(R.id.stream_item_author)
    TextView streamItemAuthor;
    @InjectView(R.id.stream_item_thumbnail)
    ImageView streamItemThumbnail;
    @InjectView(R.id.stream_likes_button)
    Button videoLikes;
    @InjectView(R.id.video_views)
    TextView videoViews;

    public ProfileStreamItemViewHolder(final View itemLayoutView) {
        super(itemLayoutView);
        ButterKnife.inject(this, itemLayoutView);
    }

    public TextView getProfileItemTitle() {
        return this.streamItemTitle;
    }

    public TextView getProfileItemAuthor() {
        return this.streamItemAuthor;
    }

    public ImageView getProfileItemThumbnail() {
        return this.streamItemThumbnail;
    }

    public Button getStreamLikesButton() {
        return this.videoLikes;
    }

    /*public TextView getVideoComments() {
        return this.videoComments;
    }*/

    public TextView getStreamViews() {
        return this.videoViews;
    }
}
