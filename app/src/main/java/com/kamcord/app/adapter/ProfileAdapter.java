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
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    private Context mContext;
    private List<Video> mProfileList;
    private static OnItemClickListener mItemClickListener;

    public ProfileAdapter(Context context, List<Video> mProfileList) {
        this.mContext = context;
        this.mProfileList = mProfileList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        View itemLayoutView;
        itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_profile_item, null);
        viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        Video profileItem = getItem(position);
        viewHolder.profileItemTitle.setText(StringUtils.getFirstLetterUpperCase(profileItem.title));
        viewHolder.profileItemAuthor.setText(mContext.getResources().getString(R.string.videoFeedAuthorHelper) + profileItem.username);
        viewHolder.videoLikes.setText(Integer.toString(profileItem.likes));
        viewHolder.videoComments.setText("Comments: " + Integer.toString(profileItem.comments));
        viewHolder.videoViews.setText("Views: " + Integer.toString(profileItem.views));
        // Picasso
        Picasso.with(mContext)
                .load(profileItem.thumbnails.regular)
                .into(viewHolder.profileItemThumbnail);
    }

    @Override
    public int getItemCount() {
        return mProfileList.size();
    }

    public Video getItem(int position) {
        return mProfileList.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.profile_item_title) TextView profileItemTitle;
        @InjectView(R.id.profile_item_author) TextView profileItemAuthor;
        @InjectView(R.id.profile_item_thumbnail) ImageView profileItemThumbnail;
        @InjectView(R.id.video_likes) TextView videoLikes;
        @InjectView(R.id.video_comments) TextView videoComments;
        @InjectView(R.id.video_views) TextView videoViews;

        public ViewHolder(final View itemLayoutView) {
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

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

}
