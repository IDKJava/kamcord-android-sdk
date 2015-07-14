package com.kamcord.app.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kamcord.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by donliang1 on 6/1/15.
 */
public class StreamItemViewHolder extends RecyclerView.ViewHolder {

    @InjectView(R.id.stream_item_title)
    TextView streamItemTitle;
    @InjectView(R.id.stream_item_author)
    TextView streamItemAuthor;
    @InjectView(R.id.stream_item_thumbnail)
    ImageView streamItemThumbnail;
    @InjectView(R.id.stream_views)
    TextView videoViews;
    @InjectView(R.id.stream_length_views)
    TextView streamLengthViews;

    public StreamItemViewHolder(final View itemLayoutView) {
        super(itemLayoutView);
        ButterKnife.inject(this, itemLayoutView);
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
        return this.videoViews;
    }

    public TextView getStreamLengthViews() {
        return this.streamLengthViews;
    }
}
