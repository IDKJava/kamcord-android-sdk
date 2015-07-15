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
 * Created by donliang1 on 15/7/14.
 */
public class TrendVideoItemViewHolder extends RecyclerView.ViewHolder {

    @InjectView(R.id.trend_item_title)
    TextView trendItemTitle;
    @InjectView(R.id.trend_item_author)
    TextView trendItemAuthor;
    @InjectView(R.id.trend_item_thumbnail)
    ImageView trendItemThumbnail;
    @InjectView(R.id.trend_video_likes_button)
    Button videoLikes;
    // No comments for now, but we'll be back!
    /*@InjectView(R.id.video_comments)
    TextView videoComments;*/
    @InjectView(R.id.trend_video_views)
    TextView videoViews;

    public TrendVideoItemViewHolder(final View itemLayoutView) {
        super(itemLayoutView);
        ButterKnife.inject(this, itemLayoutView);
    }

    public TextView getTrendItemTitle() {
        return this.trendItemTitle;
    }

    public TextView getTrendItemAuthor() {
        return this.trendItemAuthor;
    }

    public ImageView getTrendItemThumbnail() {
        return this.trendItemThumbnail;
    }

    public Button getTrendVideoLikesButton() {
        return this.videoLikes;
    }

    /*public TextView getVideoComments() {
        return this.videoComments;
    }*/

    public TextView getTrendVideoViews() {
        return this.videoViews;
    }
}