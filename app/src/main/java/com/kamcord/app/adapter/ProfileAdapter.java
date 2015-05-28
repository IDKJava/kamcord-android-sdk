package com.kamcord.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kamcord.app.R;
import com.kamcord.app.server.model.ProfileItem;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by donliang1 on 5/28/15.
 */
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    private Context mContext;
    private List<ProfileItem> mProfileList;
    private static OnItemClickListener mItemClickListener;

    public ProfileAdapter(Context context, List<ProfileItem> mProfileList) {
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
        ProfileItem profileItem = getItem(position);
        viewHolder.profileItemTitle.setText("Kamcord Title");
        viewHolder.profileItemAuthor.setText("Don@Kamcord");
        // Picasso
    }

    @Override
    public int getItemCount() {
        return mProfileList.size();
    }

    public ProfileItem getItem(int position) {
        return mProfileList.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.profile_item_title) TextView profileItemTitle;
        @InjectView(R.id.profile_item_author) TextView profileItemAuthor;
        @InjectView(R.id.profile_item_thumbnail) ImageView profileItemThumbnail;

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
