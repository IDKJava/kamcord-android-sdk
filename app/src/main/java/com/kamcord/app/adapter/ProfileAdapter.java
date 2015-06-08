package com.kamcord.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.kamcord.app.R;
import com.kamcord.app.activity.LoginActivity;
import com.kamcord.app.activity.ProfileVideoViewActivity;
import com.kamcord.app.adapter.viewholder.FooterViewHolder;
import com.kamcord.app.adapter.viewholder.ProfileHeaderViewHolder;
import com.kamcord.app.adapter.viewholder.ProfileVideoItemViewHolder;
import com.kamcord.app.model.ProfileItem;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.User;
import com.kamcord.app.server.model.Video;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.FileSystemManager;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by donliang1 on 5/28/15.
 */
public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<ProfileItem> mProfileList;

    public ProfileAdapter(Context context, List<ProfileItem> mProfileList) {
        this.mContext = context;
        this.mProfileList = mProfileList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView;
        ProfileItem.Type type = ProfileItem.Type.values()[viewType];
        switch (type) {
            case HEADER: {
                itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_profile_header, parent, false);
                return new ProfileHeaderViewHolder(itemLayoutView);
            }
            case VIDEO: {
                itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_profile_item, parent, false);
                return new ProfileVideoItemViewHolder(itemLayoutView);
            }
            case FOOTER: {
                itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_profile_footer, parent, false);
                return new FooterViewHolder(itemLayoutView);
            }
            default: {
                break;
            }

        }
        return new FooterViewHolder(null);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof ProfileHeaderViewHolder) {
            User user = getItem(position).getUser();
            if( user != null ) {
                bindProfileHeader((ProfileHeaderViewHolder) viewHolder, getItem(position).getUser());
            }

        } else if (viewHolder instanceof FooterViewHolder) {

        } else if (viewHolder instanceof ProfileVideoItemViewHolder) {
            Video video = getItem(position).getVideo();
            if( video != null ) {
                bindProfileVideoItemViewHolder((ProfileVideoItemViewHolder) viewHolder, getItem(position).getVideo());
            }
        }

    }

    private void bindProfileHeader(ProfileHeaderViewHolder viewHolder, User user) {
        if (user != null) {
            viewHolder.getProfileUserName().setText(user.username);
            if (user.username != null && user.username.length() > 0 ) {
                viewHolder.getProfileLetter().setText(user.username.substring(0, 1).toUpperCase());
            }
            viewHolder.getProfileUserTag().setText(user.tagline);
            viewHolder.getProfileUserVideos().setText(Integer.toString(user.video_count != null ? user.video_count : 0));
            viewHolder.getProfileUserFollowers().setText(Integer.toString(user.followers_count != null ? user.followers_count : 0));
            viewHolder.getProfileUserFollowing().setText(Integer.toString(user.following_count != null ? user.following_count : 0));

            int profileColor = mContext.getResources().getColor(R.color.defaultProfileColor);
            try {
                profileColor = Color.parseColor(user.profile_color);
            } catch( Exception e ) {
            }
            viewHolder.getProfileLetter().setTextColor(profileColor);
            viewHolder.getProfileHeaderLayout().setBackgroundColor(profileColor);
        }
        viewHolder.getActionMenuView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(mContext, v);
                popupMenu.getMenuInflater().inflate(R.menu.menu_record, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_cleancache: {
                                FileSystemManager.cleanCache();
                                break;
                            }
                            case R.id.action_signout: {
                                if (AccountManager.isLoggedIn()) {
                                    AppServerClient.getInstance().logout(logoutCallback);
                                }
                                break;
                            }
                        }
                        return false;
                    }
                });
            }
        });
    }

    private void bindProfileVideoItemViewHolder(ProfileVideoItemViewHolder viewHolder, final Video video) {

        viewHolder.getProfileItemTitle().setText(video.title);
        final TextView videoViewsTextView = viewHolder.getVideoViews();
        videoViewsTextView.setText(Integer.toString(video.views));
        final ImageView videoImageView = viewHolder.getProfileItemThumbnail();
        if (video.thumbnails != null && video.thumbnails.regular != null) {
            Picasso.with(mContext)
                    .load(video.thumbnails.regular)
                    .into(videoImageView);
        }
        videoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                video.views = video.views + 1;
                videoViewsTextView.setText(Integer.toString(video.views));
                Intent intent = new Intent(mContext, ProfileVideoViewActivity.class);
                intent.putExtra(ProfileVideoViewActivity.ARG_VIDEO_PATH, video.video_url);
                mContext.startActivity(intent);
                AppServerClient.getInstance().updateVideoViews(video.video_id, new UpdateVideoViewsCallback());
            }
        });

        viewHolder.getProfileItemAuthor().setText(String.format(Locale.ENGLISH, mContext.getResources().getString(R.string.byAuthor), video.username));
        viewHolder.getVideoComments().setText(Integer.toString(video.comments));

        final Button videoLikesButton = viewHolder.getVideoLikesButton();
        videoLikesButton.setText(Integer.toString(video.likes));
        videoLikesButton.setActivated(video.is_user_liking);
        videoLikesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (video.is_user_liking) {
                    video.is_user_liking = false;
                    video.likes = video.likes - 1;
                    videoLikesButton.setText(Integer.toString(video.likes));
                    videoLikesButton.setActivated(false);
                    AppServerClient.getInstance().unLikeVideo(video.video_id, new UnLikeVideosCallback());
                } else {
                    video.is_user_liking = true;
                    video.likes = video.likes + 1;
                    videoLikesButton.setText(Integer.toString(video.likes));
                    videoLikesButton.setActivated(true);
                    AppServerClient.getInstance().likeVideo(video.video_id, new LikeVideosCallback());
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        ProfileItem viewModel = mProfileList.get(position);
        return viewModel.getType().ordinal();
    }

    @Override
    public int getItemCount() {
        return mProfileList.size();
    }

    public ProfileItem getItem(int position) {
        return mProfileList.get(position);
    }

    private class LikeVideosCallback implements Callback<GenericResponse<?>> {
        @Override
        public void success(GenericResponse<?> responseWrapper, Response response) {
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e("Retrofit Failure", "  " + error.toString());
        }
    }

    private class UnLikeVideosCallback implements Callback<GenericResponse<?>> {
        @Override
        public void success(GenericResponse<?> responseWrapper, Response response) {
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e("Retrofit Unlike Failure", "  " + error.toString());
        }
    }

    private class UpdateVideoViewsCallback implements Callback<GenericResponse<?>> {
        @Override
        public void success(GenericResponse<?> responseWrapper, Response response) {
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e("Retrofit Unlike Failure", "  " + error.toString());
        }
    }

    private final Callback<GenericResponse<?>> logoutCallback = new Callback<GenericResponse<?>>() {
        @Override
        public void success(GenericResponse<?> responseWrapper, Response response) {
            AccountManager.clearStoredAccount();
            if (mContext != null) {
                Intent loginIntent = new Intent(mContext, LoginActivity.class);
                mContext.startActivity(loginIntent);
                ((Activity) mContext).finish();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            if (mContext != null) {
                Intent loginIntent = new Intent(mContext, LoginActivity.class);
                mContext.startActivity(loginIntent);
                ((Activity) mContext).finish();
            }
        }
    };
}
