package com.kamcord.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kamcord.app.R;
import com.kamcord.app.server.model.Video;
import com.kamcord.app.utils.StringUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by donliang1 on 5/28/15.
 */
public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Video> mProfileList;
    private static OnItemClickListener mItemClickListener;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_VIDEO_ITEM = 1;
    private static final int TYPE_FOOTER = 2;

    public ProfileAdapter(Context context, List<Video> mProfileList) {
        this.mContext = context;
        this.mProfileList = mProfileList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView;
        switch (viewType) {
            case 0: {
                itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_profile_header, parent, false);
                return new HeaderViewHolder(itemLayoutView);
            }
            case 1: {
                itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_profile_item, parent, false);
                return new ItemViewHolder(itemLayoutView);
            }
            case 2: {
                itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_profile_footer, parent, false);
                return new FooterViewHolder(itemLayoutView);
            }
            default: {
                return new ItemViewHolder(null);
            }

        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof HeaderViewHolder) {
            Video headerItem = getItem(position);
            if (headerItem != null) {
                ((HeaderViewHolder) viewHolder).profileUserName.setText("User Name: " + StringUtils.getFirstLetterUpperCase(headerItem.user.username));
                ((HeaderViewHolder) viewHolder).profileUserVideos.setText(StringUtils.getFirstLetterUpperCase(Integer.toString(headerItem.user.video_count)));
                ((HeaderViewHolder) viewHolder).profileUserFollowers.setText(StringUtils.getFirstLetterUpperCase(Integer.toString(headerItem.user.followers_count)));
                ((HeaderViewHolder) viewHolder).profileUserFollowing.setText(StringUtils.getFirstLetterUpperCase(Integer.toString(headerItem.user.following_count)));
            }
        } else if (viewHolder instanceof FooterViewHolder) {

        } else if (viewHolder instanceof ItemViewHolder) {
            Video profileItem = getItem(position);
            if (profileItem.title != null) {
                ((ItemViewHolder) viewHolder).profileItemTitle.setText(StringUtils.getFirstLetterUpperCase(profileItem.title));
            }
            if (profileItem.thumbnails.regular != null) {
                Picasso.with(mContext)
                        .load(profileItem.thumbnails.regular)
                        .into(((ItemViewHolder) viewHolder).profileItemThumbnail);
            }
            ((ItemViewHolder) viewHolder).profileItemAuthor.setText(mContext.getResources().getString(R.string.byAuthor) + profileItem.username);
            ((ItemViewHolder) viewHolder).videoLikes.setText(Integer.toString(profileItem.likes));
            ((ItemViewHolder) viewHolder).videoComments.setText("Comments: " + Integer.toString(profileItem.comments));
            ((ItemViewHolder) viewHolder).videoViews.setText("Views: " + Integer.toString(profileItem.views));

        }

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else if (position == mProfileList.size() - 1) {
            return TYPE_FOOTER;
        } else {
            return TYPE_VIDEO_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return mProfileList.size();
    }

    public Video getItem(int position) {
        return mProfileList.get(position);
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.profile_user_name)
        TextView profileUserName;
        @InjectView(R.id.profile_user_videos)
        TextView profileUserVideos;
        @InjectView(R.id.profile_user_followers)
        TextView profileUserFollowers;
        @InjectView(R.id.profile_user_following)
        TextView profileUserFollowing;

        public HeaderViewHolder(final View itemLayoutView) {
            super(itemLayoutView);
            ButterKnife.inject(this, itemLayoutView);
            ButterKnife.inject(this, itemLayoutView);
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.profile_item_title)
        TextView profileItemTitle;
        @InjectView(R.id.profile_item_author)
        TextView profileItemAuthor;
        @InjectView(R.id.profile_item_thumbnail)
        ImageView profileItemThumbnail;
        @InjectView(R.id.video_likes)
        TextView videoLikes;
        @InjectView(R.id.video_comments)
        TextView videoComments;
        @InjectView(R.id.video_views)
        TextView videoViews;

        public ItemViewHolder(final View itemLayoutView) {
            super(itemLayoutView);
            ButterKnife.inject(this, itemLayoutView);
            itemLayoutView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(itemLayoutView, getAdapterPosition());
                    }
                }
            });
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(final View itemLayoutView) {
            super(itemLayoutView);
            ButterKnife.inject(this, itemLayoutView);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

}
