package com.kamcord.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import com.kamcord.app.adapter.viewholder.HeaderViewHolder;
import com.kamcord.app.adapter.viewholder.ItemViewHolder;
import com.kamcord.app.model.ProfileItemType;
import com.kamcord.app.model.ProfileViewModel;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.Account;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.User;
import com.kamcord.app.server.model.Video;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.FileSystemManager;
import com.kamcord.app.utils.StringUtils;
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
    private List<ProfileViewModel> mProfileList;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_VIDEO_ITEM = 1;
    private static final int TYPE_FOOTER = 2;

    public ProfileAdapter(Context context, List<ProfileViewModel> mProfileList) {
        this.mContext = context;
        this.mProfileList = mProfileList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView;
        switch (viewType) {
            case TYPE_HEADER: {
                itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_profile_header, parent, false);
                return new HeaderViewHolder(itemLayoutView);
            }
            case TYPE_VIDEO_ITEM: {
                itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_profile_item, parent, false);
                return new ItemViewHolder(itemLayoutView);
            }
            case TYPE_FOOTER: {
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
        if (viewHolder instanceof HeaderViewHolder) {
            ProfileViewModel headerItem = getItem(position);
            User user = headerItem.getUser();
            if (headerItem != null) {
                if (user != null && user.username != null) {
                    if (user.username != null) {
                        ((HeaderViewHolder) viewHolder).getProfileUserName().setText(StringUtils.getFirstLetterUpperCase(user.username));
                        ((HeaderViewHolder) viewHolder).getProfileLetter().setText(StringUtils.getFirstLetterUpperCase(user.username).substring(0, 1));
                        ((HeaderViewHolder) viewHolder).getProfileLetter().setTextColor(Color.parseColor(user.profile_color));
                    }
                    if (user.tagline != null) {
                        ((HeaderViewHolder) viewHolder).getProfileUserTag().setText(StringUtils.getFirstLetterUpperCase(user.tagline));
                    }
                    if (user.video_count != null) {
                        ((HeaderViewHolder) viewHolder).getProfileUserVideos().setText(StringUtils.getFirstLetterUpperCase(Integer.toString(user.video_count)));
                    }
                    if (user.followers_count != null) {
                        ((HeaderViewHolder) viewHolder).getProfileUserFollowers().setText(StringUtils.getFirstLetterUpperCase(Integer.toString(user.followers_count)));

                    }
                    if (user.following_count != null) {
                        ((HeaderViewHolder) viewHolder).getProfileUserFollowing().setText(StringUtils.getFirstLetterUpperCase(Integer.toString(user.following_count)));
                    }
                    ((HeaderViewHolder) viewHolder).getProfileHeaderLayout().setBackgroundColor(Color.parseColor(user.profile_color));
                }
                ((HeaderViewHolder) viewHolder).getActionMenuView().setOnClickListener(new View.OnClickListener() {
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
                                    case R.id.action_request_game: {
                                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mContext.getResources().getString(R.string.communityEmail),});
                                        intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getResources().getString(R.string.canIRecord));
                                        String body = mContext.getResources().getString(R.string.iWantToRecord) + " \n"
                                                + "\n";
                                        if (AccountManager.isLoggedIn()) {
                                            Account account = AccountManager.getStoredAccount();
                                            body += String.format(Locale.ENGLISH, mContext.getResources().getString(R.string.sincerely), account.username);
                                        }
                                        intent.putExtra(Intent.EXTRA_TEXT, body);
                                        intent.setType("*/*");
                                        intent.setData(Uri.parse("mailto:"));
                                        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                                            mContext.startActivity(intent);
                                        } else {
                                            // TODO: show the user there's no app to handle emails.
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

        } else if (viewHolder instanceof FooterViewHolder) {

        } else if (viewHolder instanceof ItemViewHolder) {
            final ProfileViewModel profileItem = getItem(position);
            final Video videoItem = profileItem.getVideo();
            if (videoItem.title != null) {
                ((ItemViewHolder) viewHolder).getProfileItemTitle().setText(StringUtils.getFirstLetterUpperCase(videoItem.title));
            }
            final TextView videoViewsTextView = ((ItemViewHolder) viewHolder).getVideoViews();
            videoViewsTextView.setText("Views: " + Integer.toString(videoItem.views));
            final ImageView videoImageView = ((ItemViewHolder) viewHolder).getProfileItemThumbnail();
            if (videoItem.thumbnails != null && videoItem.thumbnails.regular != null) {
                Picasso.with(mContext)
                        .load(videoItem.thumbnails.regular)
                        .into(videoImageView);
            }
            videoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppServerClient.getInstance().updateVideoViews(videoItem.video_id, new UpdateVideoViewsCallback());
                    videoViewsTextView.setText("Views: " + Integer.toString(videoItem.views + 1));
                    Intent intent = new Intent(mContext, ProfileVideoViewActivity.class);
                    intent.putExtra(ProfileVideoViewActivity.ARG_VIDEO_PATH, profileItem.getVideo().video_url);
                    mContext.startActivity(intent);
                }
            });

            ((ItemViewHolder) viewHolder).getProfileItemAuthor().setText(mContext.getResources().getString(R.string.byAuthor) + videoItem.username);
            ((ItemViewHolder) viewHolder).getVideoComments().setText("Comments: " + Integer.toString(videoItem.comments));


            final Button videoLikesButton = ((ItemViewHolder) viewHolder).getVideoLikesButton();
            videoLikesButton.setText(Integer.toString(videoItem.likes));
            videoLikesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (videoItem.is_user_liking) {
                        videoItem.is_user_liking = false;
                        videoItem.likes = videoItem.likes - 1;
                        videoLikesButton.setPressed(false);
                        videoLikesButton.setText(Integer.toString(videoItem.likes));
                        AppServerClient.getInstance().unLikeVideo(videoItem.video_id, new UnLikeVideosCallback());
                    } else {
                        videoItem.is_user_liking = true;
                        videoItem.likes = videoItem.likes + 1;
                        videoLikesButton.setPressed(true);
                        videoLikesButton.setText(Integer.toString(videoItem.likes));
                        AppServerClient.getInstance().likeVideo(videoItem.video_id, new LikeVideosCallback());
                    }

                }
            });
        }

    }

    @Override
    public int getItemViewType(int position) {
        ProfileViewModel viewModel = mProfileList.get(position);
        if (viewModel.getType() == ProfileItemType.HEADER) {
            return TYPE_HEADER;
        } else if (viewModel.getType() == ProfileItemType.FOOTER) {
            return TYPE_FOOTER;
        } else {
            return TYPE_VIDEO_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return mProfileList.size();
    }

    public ProfileViewModel getItem(int position) {
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
            Intent loginIntent = new Intent(mContext, LoginActivity.class);
            mContext.startActivity(loginIntent);
            ((Activity) mContext).finish();
        }

        @Override
        public void failure(RetrofitError error) {
            AccountManager.clearStoredAccount();
            Intent loginIntent = new Intent(mContext, LoginActivity.class);
            mContext.startActivity(loginIntent);
            ((Activity) mContext).finish();
        }
    };
}
