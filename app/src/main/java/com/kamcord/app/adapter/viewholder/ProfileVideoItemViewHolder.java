package com.kamcord.app.adapter.viewholder;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.kamcord.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by donliang1 on 6/1/15.
 */
public class ProfileVideoItemViewHolder extends RecyclerView.ViewHolder {

    @InjectView(R.id.fragment_profile_item_layout)
    CardView container;
    @InjectView(R.id.profile_item_title)
    TextView profileItemTitle;
    @InjectView(R.id.profile_item_author)
    TextView profileItemAuthor;
    @InjectView(R.id.profile_item_thumbnail)
    ImageView profileItemThumbnail;
    @InjectView(R.id.profile_video_likes_button)
    Button videoLikesButton;
    // No comments for now, but we'll be back!
    /*@InjectView(R.id.video_comments)
    TextView videoComments;*/
    @InjectView(R.id.video_views)
    TextView videoViews;
    @InjectView(R.id.moreVideoActionsImageButton)
    ImageButton moreVideoActions;

    public ProfileVideoItemViewHolder(final View itemLayoutView) {
        super(itemLayoutView);
        ButterKnife.inject(this, itemLayoutView);
    }

    public CardView getContainer() {
        return this.container;
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

    public Button getVideoLikesButton() {
        return this.videoLikesButton;
    }

    /*public TextView getVideoComments() {
        return this.videoComments;
    }*/

    public TextView getVideoViews() {
        return this.videoViews;
    }

    public ImageButton getMoreVideoActions() {
        return this.moreVideoActions;
    }

}
