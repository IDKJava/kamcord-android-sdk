package com.kamcord.app.adapter.viewholder;

import android.support.v7.widget.CardView;
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
public class StreamItemViewHolder extends RecyclerView.ViewHolder {

    @InjectView(R.id.fragment_stream_item_layout)
    CardView container;
    @InjectView(R.id.stream_item_title)
    TextView streamItemTitle;
    @InjectView(R.id.stream_item_author)
    TextView streamItemAuthor;
    @InjectView(R.id.stream_item_thumbnail)
    ImageView streamItemThumbnail;
    @InjectView(R.id.stream_views)
    TextView streamViews;
    @InjectView(R.id.stream_length_views)
    TextView streamLengthViews;
    @InjectView(R.id.stream_follow_button)
    Button streamFollowButton;

    public StreamItemViewHolder(final View itemLayoutView) {
        super(itemLayoutView);
        ButterKnife.inject(this, itemLayoutView);
    }

    public CardView getContainer() {
        return this.container;
    }
    public TextView getStreamItemTitle() {
        return this.streamItemTitle;
    }

    public TextView getStreamItemAuthor() {
        return this.streamItemAuthor;
    }

    public ImageView getStreamItemThumbnail() {
        return this.streamItemThumbnail;
    }

    public TextView getStreamViews() {
        return this.streamViews;
    }

    public TextView getStreamLengthViews() {
        return this.streamLengthViews;
    }

    public Button getStreamFollowButton() {
        return this.streamFollowButton;
    }
}
