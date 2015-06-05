package com.kamcord.app.adapter.viewholder;

/**
 * Created by donliang1 on 6/4/15.
 */

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kamcord.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by donliang1 on 6/1/15.
 */
public class ProfileHeaderViewHolder extends RecyclerView.ViewHolder {

    @InjectView(R.id.profile_action_menu)
    ImageView actionMenuView;
    @InjectView(R.id.profileLetter)
    TextView profileLetter;
    @InjectView(R.id.profile_user_name)
    TextView profileUserName;
    @InjectView(R.id.profile_user_tag)
    TextView profileUserTag;
    @InjectView(R.id.profile_user_videos)
    TextView profileUserVideos;
    @InjectView(R.id.profile_user_followers)
    TextView profileUserFollowers;
    @InjectView(R.id.profile_user_following)
    TextView profileUserFollowing;
    @InjectView(R.id.profile_headerLayout)
    LinearLayout profileHeaderLayout;

    public ProfileHeaderViewHolder(final View itemLayoutView) {
        super(itemLayoutView);
        ButterKnife.inject(this, itemLayoutView);
    }

    public ImageView getActionMenuView() {
        return this.actionMenuView;
    }

    public TextView getProfileLetter() {
        return this.profileLetter;
    }

    public TextView getProfileUserName() {
        return this.profileUserName;
    }

    public TextView getProfileUserTag() { return this.profileUserTag;}

    public TextView getProfileUserVideos() {
        return this.profileUserVideos;
    }

    public TextView getProfileUserFollowers() {
        return this.profileUserFollowers;
    }

    public TextView getProfileUserFollowing() {
        return this.profileUserFollowing;
    }

    public LinearLayout getProfileHeaderLayout() {
        return this.profileHeaderLayout;
    }
}

