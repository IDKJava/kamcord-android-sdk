package com.kamcord.app.adapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.kamcord.app.R;
import com.kamcord.app.activity.LoginActivity;
import com.kamcord.app.activity.VideoViewActivity;
import com.kamcord.app.adapter.viewholder.FooterViewHolder;
import com.kamcord.app.adapter.viewholder.ProfileHeaderViewHolder;
import com.kamcord.app.adapter.viewholder.ProfileUploadProgressViewHolder;
import com.kamcord.app.adapter.viewholder.ProfileVideoItemViewHolder;
import com.kamcord.app.adapter.viewholder.StreamItemViewHolder;
import com.kamcord.app.analytics.KamcordAnalytics;
import com.kamcord.app.model.FeedItem;
import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.StatusCode;
import com.kamcord.app.server.model.Stream;
import com.kamcord.app.server.model.User;
import com.kamcord.app.server.model.Video;
import com.kamcord.app.server.model.analytics.Event;
import com.kamcord.app.service.UploadService;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.FileSystemManager;
import com.kamcord.app.utils.StringUtils;
import com.kamcord.app.utils.VideoUtils;
import com.kamcord.app.utils.ViewUtils;
import com.kamcord.app.view.utils.ProfileLayoutSpanSizeLookup;
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
    private RecyclerView mRecyclerView;
    private List<FeedItem> mProfileList;
    private User owner;

    public ProfileAdapter(Context context, RecyclerView recyclerView, List<FeedItem> mProfileList) {
        this.mContext = context;
        this.mRecyclerView = recyclerView;
        this.mProfileList = mProfileList;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView;
        FeedItem.Type type = FeedItem.Type.values()[viewType];
        switch (type) {
            case HEADER: {
                itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_profile_header, parent, false);
                return new ProfileHeaderViewHolder(itemLayoutView);
            }
            case VIDEO: {
                itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_profile_item, parent, false);
                return new ProfileVideoItemViewHolder(itemLayoutView);
            }
            case STREAM: {
                itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_stream_item, parent, false);
                return new StreamItemViewHolder(itemLayoutView);
            }
            case FOOTER: {
                itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_profile_footer, parent, false);
                return new FooterViewHolder(itemLayoutView);
            }
            case UPLOAD_PROGRESS:
                itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_upload_progress_item, parent, false);
                return new ProfileUploadProgressViewHolder(itemLayoutView);
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
            if (user != null) {
                bindProfileHeader((ProfileHeaderViewHolder) viewHolder, user);
            }

        } else if (viewHolder instanceof FooterViewHolder) {

        } else if (viewHolder instanceof ProfileVideoItemViewHolder) {
            Video video = getItem(position).getVideo();
            if (video != null) {
                bindProfileVideoItemViewHolder((ProfileVideoItemViewHolder) viewHolder, video, position);
            }

        } else if (viewHolder instanceof StreamItemViewHolder) {
            Stream stream = getItem(position).getStream();
            if( stream != null ) {
                bindStreamVideoItemViewHolder((StreamItemViewHolder) viewHolder, stream);
            }

        } else if (viewHolder instanceof ProfileUploadProgressViewHolder) {
            RecordingSession session = getItem(position).getSession();
            if (session != null && Build.VERSION.SDK_INT >= 21) {
                bindProfileUploadProgressViewHolder((ProfileUploadProgressViewHolder) viewHolder, session, position);
            }
        }

    }

    private void bindProfileHeader(ProfileHeaderViewHolder viewHolder, User user) {
        if (user != null) {
            viewHolder.getProfileUserName().setText(user.username);
            if (user.username != null && user.username.length() > 0) {
                viewHolder.getProfileLetter().setText(user.username.substring(0, 1).toUpperCase());
            }
            viewHolder.getProfileUserTag().setText(user.tagline);

            int count = user.video_count != null ? user.video_count : 0;
            viewHolder.getVideosText().setText(mContext.getResources().getQuantityString(R.plurals.headerVideos, count));
            viewHolder.getVideosCount().setText(StringUtils.abbreviatedCount(count));

            count = user.followers_count != null ? user.followers_count : 0;
            viewHolder.getFollowersText().setText(mContext.getResources().getQuantityString(R.plurals.headerFollowers, count));
            viewHolder.getFollowersCount().setText(StringUtils.abbreviatedCount(count));

            count = user.following_count != null ? user.following_count : 0;
            viewHolder.getFollowingText().setText(mContext.getResources().getQuantityString(R.plurals.headerFollowings, count));
            viewHolder.getFollowingCount().setText(StringUtils.abbreviatedCount(count));

            int profileColor = mContext.getResources().getColor(R.color.defaultProfileColor);
            try {
                profileColor = Color.parseColor(user.profile_color);
            } catch (Exception e) {
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
                            case R.id.action_signout: {
                                if (AccountManager.isLoggedIn()) {
                                    KamcordAnalytics.startSession(logoutCallback, Event.Name.PROFILE_LOGIN);
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

    private void bindProfileVideoItemViewHolder(ProfileVideoItemViewHolder viewHolder, final Video video, final int position) {

        viewHolder.getProfileItemTitle().setText(video.title);
        final TextView videoViewsButton = viewHolder.getVideoViews();
        videoViewsButton.setText(StringUtils.abbreviatedCount(video.views));
        final ImageView videoImageView = viewHolder.getProfileItemThumbnail();
        if (video.thumbnails != null && video.thumbnails.regular != null) {
            Picasso.with(mContext)
                    .load(video.thumbnails.regular)
                    .into(videoImageView);
        }
        viewHolder.getContainer().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                video.views = video.views + 1;
                videoViewsButton.setText(StringUtils.abbreviatedCount(video.views));
                Intent intent = new Intent(mContext, VideoViewActivity.class);
                intent.putExtra(VideoViewActivity.ARG_VIDEO, new Gson().toJson(video));

                // Add analytics extras
                intent.putExtra(KamcordAnalytics.VIEW_SOURCE_KEY, Event.ViewSource.VIDEO_LIST_VIEW);
                intent.putExtra(KamcordAnalytics.VIDEO_LIST_TYPE_KEY, Event.ListType.PROFILE);
                if( mRecyclerView.getLayoutManager() instanceof GridLayoutManager ) {
                    GridLayoutManager gridLayoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
                    int spanCount = gridLayoutManager.getSpanCount();
                    if( gridLayoutManager.getSpanSizeLookup() instanceof ProfileLayoutSpanSizeLookup ) {
                        ProfileLayoutSpanSizeLookup lookup = (ProfileLayoutSpanSizeLookup) gridLayoutManager.getSpanSizeLookup();

                        intent.putExtra(KamcordAnalytics.VIDEO_LIST_ROW_KEY,
                                lookup.getSpanGroupIndex(position, spanCount)+1);
                        intent.putExtra(KamcordAnalytics.VIDEO_LIST_COL_KEY,
                                lookup.getSpanIndex(position, spanCount)+1);
                    }
                }
                if( owner != null ) {
                    intent.putExtra(KamcordAnalytics.PROFILE_USER_ID_KEY, owner.id);
                }

                mContext.startActivity(intent);
                AppServerClient.getInstance().updateVideoViews(video.video_id, new UpdateVideoViewsCallback());
            }
        });

        viewHolder.getProfileItemAuthor().setText(String.format(Locale.ENGLISH,
                mContext.getResources().getString(R.string.byAuthorGame),
                video.username, video.game_name));
        /*viewHolder.getVideoComments().setText(StringUtils.abbreviatedCount(video.comments));*/

        final Button videoLikesButton = viewHolder.getVideoLikesButton();
        videoLikesButton.setText(StringUtils.abbreviatedCount(video.likes));
        videoLikesButton.setActivated(video.is_user_liking);
        if (video.is_user_liking) {
            videoLikesButton.setTextColor(mContext.getResources().getColor(R.color.kamcordGreen));
            videoLikesButton.setCompoundDrawablesWithIntrinsicBounds(
                    ViewUtils.getTintedDrawable(
                            mContext,
                            mContext.getResources().getDrawable(R.drawable.likes_white),
                            R.color.kamcordGreen),
                    null, null, null);
        } else {
            videoLikesButton.setTextColor(mContext.getResources().getColor(R.color.kamcordGray));
            videoLikesButton.setCompoundDrawablesWithIntrinsicBounds(
                    ViewUtils.getTintedDrawable(
                            mContext,
                            mContext.getResources().getDrawable(R.drawable.likes_white),
                            R.color.kamcordGray),
                    null, null, null);
        }
        videoLikesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLikeButton(videoLikesButton, video);
            }
        });

        viewHolder.getMoreVideoActions().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
                popupMenu.inflate(R.menu.menu_more_video_actions);
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.action_external_share:
                                Bundle extras = new Bundle();
                                extras.putSerializable(KamcordAnalytics.VIEW_SOURCE_KEY, Event.ViewSource.VIDEO_LIST_VIEW);
                                extras.putSerializable(KamcordAnalytics.VIDEO_LIST_TYPE_KEY, Event.ListType.PROFILE);
                                if( mRecyclerView.getLayoutManager() instanceof GridLayoutManager ) {
                                    GridLayoutManager gridLayoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
                                    int spanCount = gridLayoutManager.getSpanCount();
                                    if( gridLayoutManager.getSpanSizeLookup() instanceof ProfileLayoutSpanSizeLookup ) {
                                        ProfileLayoutSpanSizeLookup lookup = (ProfileLayoutSpanSizeLookup) gridLayoutManager.getSpanSizeLookup();

                                        extras.putInt(KamcordAnalytics.VIDEO_LIST_ROW_KEY,
                                                lookup.getSpanGroupIndex(position, spanCount)+1);
                                        extras.putInt(KamcordAnalytics.VIDEO_LIST_COL_KEY,
                                                lookup.getSpanIndex(position, spanCount)+1);
                                    }
                                }
                                extras.putString(KamcordAnalytics.VIDEO_ID_KEY, video.video_id);
                                KamcordAnalytics.fireEvent(Event.Name.EXTERNAL_RESHARE, extras);

                                VideoUtils.doExternalShare(mContext, video);
                                break;

                            case R.id.action_delete:
                                showDeleteVideoDialog(video);
                                break;
                        }
                        return true;
                    }
                });
            }
        });
    }

    @TargetApi(21)
    private void bindStreamVideoItemViewHolder(StreamItemViewHolder viewHolder, final Stream stream) {

        viewHolder.getStreamItemTitle().setText(stream.name);
        final TextView streamViewsTextView = viewHolder.getStreamViews();
        streamViewsTextView.setText(StringUtils.abbreviatedCount(stream.current_viewers_count));
        final ImageView streamImageView = viewHolder.getStreamItemThumbnail();
        if (stream.thumbnails != null && stream.thumbnails.medium != null) {
            Picasso.with(mContext)
                    .load(stream.thumbnails.medium.unsecure_url)
                    .into(streamImageView);
        }
        streamImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stream.current_viewers_count = stream.current_viewers_count + 1;
                streamViewsTextView.setText(StringUtils.abbreviatedCount(stream.current_viewers_count));
                Intent intent = new Intent(mContext, VideoViewActivity.class);
                intent.putExtra(VideoViewActivity.ARG_STREAM,
                        new Gson().toJson(stream));

                mContext.startActivity(intent);
                //AppServerClient.getInstance().updateVideoViews(video.video_id, new UpdateVideoViewsCallback()); //DQTODO update server views?
            }
        });

        String username = "";

        if (stream.user != null && stream.user.username != null)
            username = stream.user.username;

        viewHolder.getStreamItemAuthor().setText(String.format(Locale.ENGLISH,
                mContext.getResources().getString(R.string.author), username));
    }

    @TargetApi(21)
    private void bindProfileUploadProgressViewHolder(final ProfileUploadProgressViewHolder viewHolder, final RecordingSession session, final int position) {
        Picasso.with(mContext)
                .load(VideoUtils.getVideoThumbnailFile(session))
                .into(viewHolder.thumbnailImageView);

        viewHolder.retryUploadImageButton.setVisibility(View.GONE);
        viewHolder.uploadFailedImageButton.setVisibility(View.GONE);
        viewHolder.uploadProgressBar.setVisibility(View.GONE);
        viewHolder.divider.setVisibility(View.GONE);
        String uploadStatus = null;
        if (session.getUploadProgress() < 0f) {
            uploadStatus = mContext.getString(R.string.queuedForUpload);
        } else if (session.getUploadProgress() <= 1f) {
            int percentProgress = (int) (100f * session.getUploadProgress());
            int progressBarProgress = (int) (viewHolder.uploadProgressBar.getMax() * session.getUploadProgress());
            uploadStatus = String.format(Locale.ENGLISH, mContext.getString(R.string.currentlyUploadingPercent), percentProgress);
            viewHolder.uploadProgressBar.setVisibility(View.VISIBLE);
            viewHolder.uploadProgressBar.setProgressDrawable(mContext.getDrawable(R.drawable.upload_progressbar_blue));
            viewHolder.uploadProgressBar.setProgress(progressBarProgress);

        } else if (session.getUploadProgress() == RecordingSession.UPLOAD_PROCESSING_PROGRESS) {
            uploadStatus = mContext.getString(R.string.processingPullToRefresh);
            viewHolder.divider.setVisibility(View.VISIBLE);

        } else if (session.getUploadProgress() == RecordingSession.UPLOAD_FAILED_PROGRESS) {
            uploadStatus = mContext.getString(R.string.uploadFailed);
            viewHolder.uploadProgressBar.setVisibility(View.VISIBLE);
            viewHolder.uploadProgressBar.setProgressDrawable(mContext.getDrawable(R.drawable.upload_progressbar_red));
            viewHolder.uploadProgressBar.setProgress(viewHolder.uploadProgressBar.getMax());

            viewHolder.uploadFailedImageButton.setVisibility(View.VISIBLE);
            viewHolder.uploadFailedImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(mContext, v);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_upload_failed, popupMenu.getMenu());
                    popupMenu.show();
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.action_delete: {
                                    FileSystemManager.cleanRecordingSessionCacheDirectory(session);
                                    mProfileList.remove(position);
                                    notifyItemRemoved(position);
                                    break;
                                }
                            }
                            return false;
                        }
                    });
                }
            });
            viewHolder.retryUploadImageButton.setVisibility(View.VISIBLE);
            viewHolder.retryUploadImageButton.setEnabled(true);
            viewHolder.retryUploadImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setEnabled(false);
                    mProfileList.remove(position);
                    notifyItemRemoved(position);

                    Intent uploadIntent = new Intent(mContext, UploadService.class);
                    session.setShareAppSessionId(KamcordAnalytics.getCurrentAppSessionId());
                    session.setIsUploadRetry(true);
                    uploadIntent.putExtra(UploadService.ARG_SESSION_TO_SHARE, new Gson().toJson(session));
                    mContext.startService(uploadIntent);
                }
            });
        }

        viewHolder.uploadStatusTextView.setText(uploadStatus);
        viewHolder.videoTitleTextView.setText(session.getVideoTitle());
    }

    private void toggleLikeButton(Button likeButton, Video video) {
        if (video.is_user_liking) {
            video.is_user_liking = false;
            if (video.likes > 0) {
                video.likes = video.likes - 1;
            }
            likeButton.setText(StringUtils.abbreviatedCount(video.likes));
            likeButton.setTextColor(mContext.getResources().getColor(R.color.kamcordGray));
            likeButton.setActivated(false);
            likeButton.setCompoundDrawablesWithIntrinsicBounds(
                    ViewUtils.getTintedDrawable(mContext, mContext.getResources().getDrawable(R.drawable.likes_white), R.color.kamcordGray),
                    null, null, null);
            AppServerClient.getInstance().unLikeVideo(video.video_id, new UnLikeVideosCallback());
        } else {
            video.is_user_liking = true;
            video.likes = video.likes + 1;
            likeButton.setText(StringUtils.abbreviatedCount(video.likes));
            likeButton.setTextColor(mContext.getResources().getColor(R.color.kamcordGreen));
            likeButton.setActivated(true);
            likeButton.setCompoundDrawablesWithIntrinsicBounds(
                    ViewUtils.getTintedDrawable(mContext, mContext.getResources().getDrawable(R.drawable.likes_white), R.color.kamcordGreen),
                    null, null, null);
            AppServerClient.getInstance().likeVideo(video.video_id, new LikeVideosCallback());
        }
        ViewUtils.buttonCircularReveal(likeButton);
    }

    @Override
    public int getItemViewType(int position) {
        FeedItem viewModel = mProfileList.get(position);
        return viewModel.getType().ordinal();
    }

    @Override
    public int getItemCount() {
        return mProfileList.size();
    }

    public FeedItem getItem(int position) {
        return mProfileList.get(position);
    }

    private void showDeleteVideoDialog(final Video video) {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.areYouSure)
                .setMessage(R.string.ifYouDeleteThis)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.deleteVideo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AppServerClient.getInstance().deleteVideo(
                                video.video_id,
                                new DeleteVideoCallback(video));
                    }
                }).show();
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
            Log.e("Retrofit Update Video Views Failure", "  " + error.toString());
        }
    }

    private class DeleteVideoCallback implements Callback<GenericResponse<?>> {
        private Video video;

        public DeleteVideoCallback(Video video) {
            this.video = video;
        }

        @Override
        public void success(GenericResponse<?> genericResponse, Response response) {
            int index = 0;
            for( FeedItem item : mProfileList ) {
                if( item.getType() == FeedItem.Type.VIDEO
                    && item.getVideo().video_id.equals(video.video_id) ) {
                    mProfileList.remove(index);
                    notifyItemRemoved(index);
                    break;
                }
                index++;
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Toast.makeText(mContext, mContext.getString(R.string.failedToDelete), Toast.LENGTH_SHORT).show();
        }
    }

    private final Callback<GenericResponse<?>> logoutCallback = new Callback<GenericResponse<?>>() {
        @Override
        public void success(GenericResponse<?> responseWrapper, Response response) {
            boolean isSuccess = responseWrapper != null && responseWrapper.status != null && responseWrapper.status.equals(StatusCode.OK);
            String failureReason = responseWrapper != null && responseWrapper.status != null && !responseWrapper.status.equals(StatusCode.OK)
                    ? responseWrapper.status.status_reason : null;
            Bundle extras = analyticsExtras(isSuccess, failureReason);
            KamcordAnalytics.endSession(this, Event.Name.PROFILE_LOGIN, extras);

            AccountManager.clearStoredAccount();
            if (mContext != null) {
                Intent loginIntent = new Intent(mContext, LoginActivity.class);
                loginIntent.putExtra(KamcordAnalytics.VIEW_SOURCE_KEY, Event.ViewSource.PROFILE_DETAIL_VIEW);
                loginIntent.putExtra(KamcordAnalytics.INDUCING_ACTION_KEY, Event.InducingAction.PROFILE_LOGOUT);
                mContext.startActivity(loginIntent);
                ((Activity) mContext).finish();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Bundle extras = analyticsExtras(false, null);
            KamcordAnalytics.endSession(this, Event.Name.PROFILE_LOGIN, extras);

            AccountManager.clearStoredAccount();
            if (mContext != null) {
                Intent loginIntent = new Intent(mContext, LoginActivity.class);
                loginIntent.putExtra(KamcordAnalytics.VIEW_SOURCE_KEY, Event.ViewSource.PROFILE_DETAIL_VIEW);
                loginIntent.putExtra(KamcordAnalytics.INDUCING_ACTION_KEY, Event.InducingAction.PROFILE_LOGOUT);
                mContext.startActivity(loginIntent);
                ((Activity) mContext).finish();
            }
        }

        private Bundle analyticsExtras(boolean isSuccess, String failureReason) {
            Bundle extras = new Bundle();

            extras.putInt(KamcordAnalytics.IS_SUCCESS_KEY, isSuccess ? 1 : 0);
            extras.putString(KamcordAnalytics.FAILURE_REASON_KEY, failureReason);
            extras.putSerializable(KamcordAnalytics.VIEW_SOURCE_KEY, Event.ViewSource.PROFILE_DETAIL_VIEW);
            extras.putInt(KamcordAnalytics.IS_LOGIN_KEY, 0);

            return extras;
        }
    };
}
