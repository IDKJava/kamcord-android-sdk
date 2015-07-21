package com.kamcord.app.adapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kamcord.app.R;
import com.kamcord.app.activity.LoginActivity;
import com.kamcord.app.activity.RecordActivity;
import com.kamcord.app.activity.VideoViewActivity;
import com.kamcord.app.adapter.viewholder.FooterViewHolder;
import com.kamcord.app.adapter.viewholder.LiveStreamHeaderViewHolder;
import com.kamcord.app.adapter.viewholder.StreamItemViewHolder;
import com.kamcord.app.adapter.viewholder.TrendingVideoHeaderViewHolder;
import com.kamcord.app.adapter.viewholder.TrendingVideoItemViewHolder;
import com.kamcord.app.analytics.KamcordAnalytics;
import com.kamcord.app.model.FeedItem;
import com.kamcord.app.server.callbacks.FollowCallback;
import com.kamcord.app.server.callbacks.UnfollowCallback;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.Stream;
import com.kamcord.app.server.model.User;
import com.kamcord.app.server.model.Video;
import com.kamcord.app.server.model.analytics.Event;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.StringUtils;
import com.kamcord.app.utils.VideoUtils;
import com.kamcord.app.utils.ViewUtils;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * Created by donliang1 on 15/7/10.
 */
public class StreamListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<FeedItem> mFeedItems;

    public StreamListAdapter(Context context, List<FeedItem> feedItems) {
        this.mContext = context;
        this.mFeedItems = feedItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View itemLayoutView;
        FeedItem.Type type = FeedItem.Type.values()[viewType];
        switch (type) {
            case LIVESTREAM_HEADER: {
                itemLayoutView = inflater.inflate(R.layout.view_livestream_header, null);
                return new LiveStreamHeaderViewHolder(itemLayoutView);
            }
            case STREAM: {
                itemLayoutView = inflater.inflate(R.layout.fragment_stream_item, parent, false);
                return new StreamItemViewHolder(itemLayoutView);
            }
            case TRENDVIDEO_HEADER: {
                itemLayoutView = inflater.inflate(R.layout.view_trendvideo_header, null);
                return new TrendingVideoHeaderViewHolder(itemLayoutView);
            }
            case VIDEO: {
                itemLayoutView = inflater.inflate(R.layout.fragment_trending_video_item, parent, false);
                return new TrendingVideoItemViewHolder(itemLayoutView);
            }
            case FETCH_MORE:
                ProgressBar progressSpinner = new ProgressBar(parent.getContext());
                progressSpinner.setIndeterminate(true);
                return new RecyclerView.ViewHolder(progressSpinner) {};

            default:
                break;
        }
        return new FooterViewHolder(null);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

        if (viewHolder instanceof LiveStreamHeaderViewHolder) {
            bindLiveStreamHeaderViewHolder((LiveStreamHeaderViewHolder) viewHolder);

        } else if (viewHolder instanceof TrendingVideoHeaderViewHolder) {
            bindTrendVideoViewHolder((TrendingVideoHeaderViewHolder) viewHolder);

        } else if (viewHolder instanceof StreamItemViewHolder) {
            Stream stream = getItem(position).getStream();
            if (stream != null) {
                bindStreamVideoItemViewHolder((StreamItemViewHolder) viewHolder, stream);
            }
        } else if (viewHolder instanceof TrendingVideoItemViewHolder) {
            Video video = getItem(position).getVideo();
            if (video != null) {
                bindVideoItemViewHolder((TrendingVideoItemViewHolder) viewHolder, video);
            }
        }
    }

    private void bindLiveStreamHeaderViewHolder(LiveStreamHeaderViewHolder viewHolder) {
        CalligraphyUtils.applyFontToTextView(mContext, viewHolder.livestreamHeaderTextView, "fonts/proximanova_semibold.otf");
    }

    @TargetApi(21)
    private void bindStreamVideoItemViewHolder(StreamItemViewHolder viewHolder, final Stream stream) {

        viewHolder.getStreamItemTitle().setText(stream.name);
        final TextView streamViewsTextView = viewHolder.getStreamViews();
        streamViewsTextView.setText(StringUtils.abbreviatedCount(stream.current_viewers_count));
        final TextView streamLengthTextView = viewHolder.getStreamLengthViews();
        streamLengthTextView.setText(VideoUtils.getStreamDurationString(stream.started_at));
        final ImageView streamImageView = viewHolder.getStreamItemThumbnail();
        if (stream.thumbnails != null && stream.thumbnails.medium != null) {
            Picasso.with(mContext)
                    .load(stream.thumbnails.medium.unsecure_url)
                    .into(streamImageView);
        }
        viewHolder.getContainer().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stream.current_viewers_count = stream.current_viewers_count + 1;
                streamViewsTextView.setText(StringUtils.abbreviatedCount(stream.current_viewers_count));
                Intent intent = new Intent(mContext, VideoViewActivity.class);
                intent.putExtra(VideoViewActivity.ARG_STREAM, new Gson().toJson(stream));
                if (mContext instanceof Activity) {
                    ((Activity) mContext).startActivityForResult(intent, RecordActivity.HOME_FRAGMENT_RESULT_CODE);
                } else {
                    mContext.startActivity(intent);
                }
            }
        });

        final Button streamFollowButton = viewHolder.getStreamFollowButton();
        boolean followingState = false;
        if (AccountManager.isLoggedIn() ) {
            if (stream.user != null && AccountManager.getStoredAccount().id.equals(stream.user.id)) {
                streamFollowButton.setVisibility(View.GONE);
            } else if (stream.user != null && stream.user.is_user_following != null ) {
                followingState = stream.user.is_user_following;
            }
        }
        streamFollowButton.setActivated(followingState);

        streamFollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFollowButton(streamFollowButton, stream.user);
                if (stream.user != null && stream.user.is_user_following != null)
                    updateItem(stream.user_id, stream.user.is_user_following);
            }
        });

        String username = "";

        if (stream.user != null && stream.user.username != null)
            username = stream.user.username;

        viewHolder.getStreamItemAuthor().setText(String.format(Locale.ENGLISH,
                mContext.getResources().getString(R.string.author), username));
    }

    private void bindTrendVideoViewHolder(TrendingVideoHeaderViewHolder viewHolder) {
        CalligraphyUtils.applyFontToTextView(mContext, viewHolder.trendvideoHeaderTextView, "fonts/proximanova_semibold.otf");
    }

    private void bindVideoItemViewHolder(TrendingVideoItemViewHolder viewHolder, final Video video) {

        viewHolder.getTrendItemTitle().setText(video.title);
        final TextView videoViewsButton = viewHolder.getTrendVideoViews();
        videoViewsButton.setText(StringUtils.abbreviatedCount(video.views));
        final ImageView videoImageView = viewHolder.getTrendItemThumbnail();
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
                if (mContext instanceof Activity) {
                    ((Activity) mContext).startActivityForResult(intent, RecordActivity.HOME_FRAGMENT_RESULT_CODE);
                } else {
                    mContext.startActivity(intent);
                }
                AppServerClient.getInstance().updateVideoViews(video.video_id, new UpdateVideoViewsCallback());
            }
        });

        viewHolder.getTrendItemAuthor().setText(String.format(Locale.ENGLISH,
                mContext.getResources().getString(R.string.byAuthorGame),
                video.username, video.game_name));
        /*viewHolder.getVideoComments().setText(StringUtils.abbreviatedCount(video.comments));*/

        final Button trendFollowButton = viewHolder.getTrendFollowButton();
        boolean followingState = false;
        if (AccountManager.isLoggedIn() ) {
            if (video.user != null && AccountManager.getStoredAccount().id.equals(video.user.id)) {
                trendFollowButton.setVisibility(View.GONE);
            } else if (video.user != null && video.user.is_user_following != null ) {
                followingState = video.user.is_user_following;
            }
        }
        trendFollowButton.setActivated(followingState);

        trendFollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFollowButton(trendFollowButton, video.user);
                if (video.user != null && video.user.is_user_following != null)
                    updateItem(video.user_id, video.user.is_user_following);
            }
        });

        final Button videoLikesButton = viewHolder.getTrendVideoLikesButton();
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
    }

    private void toggleFollowButton(Button followButton, User user) {
        if (AccountManager.isLoggedIn()) {
            Callback<?> callback = null;
            if (user.is_user_following == null)
                user.is_user_following = false;
            if (user.is_user_following) {
                user.is_user_following = false;
                followButton.setActivated(false);
                callback = new UnfollowCallback(user.id, null, Event.ViewSource.STREAM_DETAIL_VIEW);
                AppServerClient.getInstance().unfollow(user.id, (UnfollowCallback) callback);
            } else {
                user.is_user_following = true;
                followButton.setActivated(true);
                callback = new FollowCallback(user.id, null, Event.ViewSource.STREAM_DETAIL_VIEW);
                AppServerClient.getInstance().follow(user.id, (FollowCallback) callback);
            }
            KamcordAnalytics.startSession(callback, Event.Name.FOLLOW_USER);
            ViewUtils.buttonCircularReveal(followButton);
        } else {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.youMustBeLoggedIn), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(mContext, LoginActivity.class);
            mContext.startActivity(intent);
        }
    }

    private void toggleLikeButton(Button likeButton, Video video) {
        if (AccountManager.isLoggedIn()) {
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
        } else {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.youMustBeLoggedIn), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(mContext, LoginActivity.class);
            mContext.startActivity(intent);
        }
    }

    public void updateItem(String user_id, boolean is_user_following) {
        if (user_id != null) {

            boolean changed = false;

            for (FeedItem item : mFeedItems) {
                Stream feedStream = item.getStream();
                Video feedVideo = item.getVideo();
                if (feedStream != null && feedStream.user != null && user_id.equals(feedStream.user_id) &&
                        (feedStream.user.is_user_following == null || feedStream.user.is_user_following != is_user_following)) {
                    feedStream.user.is_user_following = is_user_following;
                    changed = true;
                }
                if (feedVideo != null && feedVideo.user != null && user_id.equals(feedVideo.user_id) &&
                        (feedVideo.user.is_user_following == null || feedVideo.user.is_user_following != is_user_following)) {
                    feedVideo.user.is_user_following = is_user_following;
                    changed = true;
                }
            }
            if (changed)
                notifyDataSetChanged();
        }
    }

    private class UpdateVideoViewsCallback implements Callback<GenericResponse<?>> {
        @Override
        public void success(GenericResponse<?> responseWrapper, Response response) {
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e("Retrofit UpVid Fail", "  " + error.toString());
        }
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

    public FeedItem getItem(int position) {
        return mFeedItems.get(position);
    }

    @Override
    public int getItemCount() {
        return mFeedItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        FeedItem item = mFeedItems.get(position);
        return item.getType().ordinal();
    }
}

