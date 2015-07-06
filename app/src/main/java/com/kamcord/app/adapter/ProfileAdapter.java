package com.kamcord.app.adapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kamcord.app.R;
import com.kamcord.app.activity.LoginActivity;
import com.kamcord.app.activity.ProfileVideoViewActivity;
import com.kamcord.app.adapter.viewholder.FooterViewHolder;
import com.kamcord.app.adapter.viewholder.ProfileHeaderViewHolder;
import com.kamcord.app.adapter.viewholder.ProfileUploadProgressViewHolder;
import com.kamcord.app.adapter.viewholder.ProfileVideoItemViewHolder;
import com.kamcord.app.analytics.KamcordAnalytics;
import com.kamcord.app.model.ProfileItem;
import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.User;
import com.kamcord.app.server.model.Video;
import com.kamcord.app.service.UploadService;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.FileSystemManager;
import com.kamcord.app.utils.StringUtils;
import com.kamcord.app.utils.VideoUtils;
import com.kamcord.app.utils.ViewUtils;
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
                bindProfileVideoItemViewHolder((ProfileVideoItemViewHolder) viewHolder, video);
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
        final Button videoViewsButton = viewHolder.getVideoViews();
        ViewUtils.setButtonPadding(videoViewsButton, (int)mContext.getResources().getDimension(R.dimen.buttonPadding));
        videoViewsButton.setText(StringUtils.abbreviatedCount(video.views));
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
                videoViewsButton.setText(StringUtils.abbreviatedCount(video.views));
                Intent intent = new Intent(mContext, ProfileVideoViewActivity.class);
                intent.putExtra(ProfileVideoViewActivity.ARG_VIDEO_PATH, video.video_url);
                mContext.startActivity(intent);
                AppServerClient.getInstance().updateVideoViews(video.video_id, new UpdateVideoViewsCallback());
            }
        });

        viewHolder.getProfileItemAuthor().setText(String.format(Locale.ENGLISH,
                mContext.getResources().getString(R.string.byAuthorGame),
                video.username, video.game_name));
        /*viewHolder.getVideoComments().setText(StringUtils.abbreviatedCount(video.comments));*/

        final Button videoLikesButton = viewHolder.getVideoLikesButton();
        ViewUtils.setButtonPadding(videoLikesButton, (int) mContext.getResources().getDimension(R.dimen.buttonPadding));
        if(video.likes < 0) {
            videoLikesButton.setText(StringUtils.abbreviatedCount(0));
        } else {
            videoLikesButton.setText(StringUtils.abbreviatedCount(video.likes));
        }
        videoLikesButton.setActivated(video.is_user_liking);
        if (video.is_user_liking) {
            videoLikesButton.setCompoundDrawablesWithIntrinsicBounds(
                    ViewUtils.getTintedDrawable(
                            mContext,
                            mContext.getResources().getDrawable(R.drawable.likes_white),
                            R.color.ColorPrimary),
                    null, null, null);
        } else {
            videoLikesButton.setCompoundDrawablesWithIntrinsicBounds(
                    ViewUtils.getTintedDrawable(
                            mContext,
                            mContext.getResources().getDrawable(R.drawable.likes_white),
                            R.color.kamcordGreen),
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
                                doExternalShare(video);
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
            if(video.likes > 0) {
                video.likes = video.likes - 1;
            }
            likeButton.setText(StringUtils.abbreviatedCount(video.likes));
            likeButton.setActivated(false);
            likeButton.setCompoundDrawablesWithIntrinsicBounds(
                    ViewUtils.getTintedDrawable(mContext, mContext.getResources().getDrawable(R.drawable.likes_white), R.color.kamcordGreen),
                    null, null, null);
            AppServerClient.getInstance().unLikeVideo(video.video_id, new UnLikeVideosCallback());
        } else {
            video.is_user_liking = true;
            video.likes = video.likes + 1;
            likeButton.setText(StringUtils.abbreviatedCount(video.likes));
            likeButton.setActivated(true);
            likeButton.setCompoundDrawablesWithIntrinsicBounds(
                    ViewUtils.getTintedDrawable(mContext, mContext.getResources().getDrawable(R.drawable.likes_white), R.color.ColorPrimary),
                    null, null, null);
            AppServerClient.getInstance().likeVideo(video.video_id, new LikeVideosCallback());
        }
        ViewUtils.buttonCircularReveal(likeButton);
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

    private static final int MAX_EXTERNAL_SHARE_TEXT_LENGTH = 140;

    private void doExternalShare(Video video) {
        if (mContext instanceof Activity && video.video_id != null) {
            Activity activity = (Activity) mContext;
            String watchPageLink = "www.kamcord.com/v/" + video.video_id;


            String externalShareText = null;
            if (video.title != null) {
                externalShareText = String.format(Locale.ENGLISH, activity.getString(R.string.externalShareText),
                        video.title, watchPageLink);
                int diff = externalShareText.length() - MAX_EXTERNAL_SHARE_TEXT_LENGTH;
                if (diff > 0) {
                    String truncatedTitle = StringUtils.ellipsize(video.title, video.title.length() - diff);
                    externalShareText = String.format(Locale.ENGLISH, activity.getString(R.string.externalShareText),
                            truncatedTitle, video.video_site_watch_page);
                }
            } else {
                externalShareText = String.format(Locale.ENGLISH, activity.getString(R.string.externalShareTextNoTitle),
                        watchPageLink);
            }
            externalShareText = StringUtils.ellipsize(externalShareText, MAX_EXTERNAL_SHARE_TEXT_LENGTH);

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, externalShareText);
            shareIntent.setType("text/plain");
            activity.startActivity(Intent.createChooser(shareIntent, activity.getString(R.string.shareTo)));
        }
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
            Log.e("Retrofit Unlike Failure", "  " + error.toString());
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
            for (ProfileItem item : mProfileList) {
                if (item.getType() == ProfileItem.Type.VIDEO
                        && item.getVideo().video_id.equals(video.video_id)) {
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
            AccountManager.clearStoredAccount();
            if (mContext != null) {
                Intent loginIntent = new Intent(mContext, LoginActivity.class);
                mContext.startActivity(loginIntent);
                ((Activity) mContext).finish();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            AccountManager.clearStoredAccount();
            if (mContext != null) {
                Intent loginIntent = new Intent(mContext, LoginActivity.class);
                mContext.startActivity(loginIntent);
                ((Activity) mContext).finish();
            }
        }
    };
}
