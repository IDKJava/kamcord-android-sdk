package com.kamcord.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kamcord.app.R;
import com.kamcord.app.adapter.viewholder.FooterViewHolder;
import com.kamcord.app.adapter.viewholder.HeaderViewHolder;
import com.kamcord.app.adapter.viewholder.ItemViewHolder;
import com.kamcord.app.model.ProfileItemType;
import com.kamcord.app.model.ProfileViewModel;
import com.kamcord.app.server.model.User;
import com.kamcord.app.server.model.Video;
import com.kamcord.app.utils.StringUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by donliang1 on 5/28/15.
 */
public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<ProfileViewModel> mProfileList;
    private static OnItemClickListener mItemClickListener;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_VIDEO_ITEM = 1;
    private static final int TYPE_FOOTER = 2;

    public ProfileAdapter(Context context, List<ProfileViewModel> mProfileList, OnItemClickListener itemClickListener) {
        this.mContext = context;
        this.mProfileList = mProfileList;
        this.mItemClickListener = itemClickListener;
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
                return new ItemViewHolder(itemLayoutView, mItemClickListener);
            }
            case 2: {
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
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof HeaderViewHolder) {
            ProfileViewModel headerItem = getItem(position);
            User user = headerItem.getUser();
            if (headerItem != null) {
                ((HeaderViewHolder) viewHolder).getProfileUserName().setText("User Name: " + StringUtils.getFirstLetterUpperCase(user.username));
                ((HeaderViewHolder) viewHolder).getProfileUserVideos().setText(StringUtils.getFirstLetterUpperCase(Integer.toString(user.video_count)));
                ((HeaderViewHolder) viewHolder).getProfileUserFollowers().setText(StringUtils.getFirstLetterUpperCase(Integer.toString(user.followers_count)));
                ((HeaderViewHolder) viewHolder).getProfileUserFollowing().setText(StringUtils.getFirstLetterUpperCase(Integer.toString(user.following_count)));
            }
        } else if (viewHolder instanceof FooterViewHolder) {

        } else if (viewHolder instanceof ItemViewHolder) {
            ProfileViewModel profileItem = getItem(position);
            Video videoItem = profileItem.getVideo();
            if (videoItem.title != null) {
                ((ItemViewHolder) viewHolder).getProfileItemTitle().setText(StringUtils.getFirstLetterUpperCase(videoItem.title));
            }
            if (videoItem.thumbnails != null && videoItem.thumbnails.regular != null) {
                Picasso.with(mContext)
                        .load(videoItem.thumbnails.regular)
                        .into(((ItemViewHolder) viewHolder).getProfileItemThumbnail());
            }
            ((ItemViewHolder) viewHolder).getProfileItemAuthor().setText(mContext.getResources().getString(R.string.byAuthor) + videoItem.username);
            ((ItemViewHolder) viewHolder).getVideoLikes().setText(Integer.toString(videoItem.likes));
            ((ItemViewHolder) viewHolder).getVideoComments().setText("Comments: " + Integer.toString(videoItem.comments));
            ((ItemViewHolder) viewHolder).getVideoViews().setText("Views: " + Integer.toString(videoItem.views));

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


    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

}
