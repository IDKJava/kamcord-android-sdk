package com.kamcord.app.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kamcord.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pplunkett on 6/8/15.
 */
public class ProfileUploadProgressViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.thumbnailImageView)
    public ImageView thumbnailImageView;
    @InjectView(R.id.uploadStatusTextView)
    public TextView uploadStatusTextView;
    @InjectView(R.id.uploadProgressBar)
    public ProgressBar uploadProgressBar;
    @InjectView(R.id.videoTitleTextView)
    public TextView videoTitleTextView;

    @InjectView(R.id.uploadFailedImageButton)
    public ImageButton uploadFailedImageButton;
    @InjectView(R.id.retryUploadImageButton)
    public ImageButton retryUploadImageButton;

    public ProfileUploadProgressViewHolder(final View itemLayoutView) {
        super(itemLayoutView);
        ButterKnife.inject(this, itemLayoutView);
    }
}
