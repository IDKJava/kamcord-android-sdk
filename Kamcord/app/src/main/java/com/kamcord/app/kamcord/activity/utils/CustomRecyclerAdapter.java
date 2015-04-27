package com.kamcord.app.kamcord.activity.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kamcord.app.kamcord.R;
import com.kamcord.app.kamcord.activity.Model.GameModel;

import java.util.List;

/**
 * Created by donliang1 on 4/24/15.
 */
public class CustomRecyclerAdapter extends RecyclerView.Adapter<CustomRecyclerAdapter.ViewHolder> {

    private static Context mContext;
    private List<GameModel> mGames;
    private static OnItemClickListener mItemClickListener;

    public CustomRecyclerAdapter(Context context, List<GameModel> games) {
        this.mContext = context;
        this.mGames = games;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        View itemLayoutView;

        itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_row, null);
        viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final GameModel gameModel = getItem(position);
        viewHolder.itemPackageName.setText(gameModel.getPackageName());
        viewHolder.itemImage.setBackgroundResource(gameModel.getDrawableID());
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return mGames.size();
    }

    public GameModel getItem(int position) {
        return mGames.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView itemPackageName;
        private ImageView itemImage;

        public ViewHolder(final View itemLayoutView) {
            super(itemLayoutView);
            itemPackageName = (TextView) itemLayoutView.findViewById(R.id.item_packagename);
            itemImage = (ImageView) itemLayoutView.findViewById(R.id.item_image);
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

    public static interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

}
