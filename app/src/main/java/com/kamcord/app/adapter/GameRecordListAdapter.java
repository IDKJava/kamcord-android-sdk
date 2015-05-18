package com.kamcord.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.kamcord.app.R;
import com.kamcord.app.server.model.Game;
import com.kamcord.app.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GameRecordListAdapter extends RecyclerView.Adapter<GameRecordListAdapter.ViewHolder> {

    private Context mContext;
    private List<Game> mGames;
    private static OnItemClickListener mItemClickListener;
    private static final int ITEM_VIEW_TYPE_HEADER = 0;
    private static final int ITEM_VIEW_TYPE_ITEM = 1;
    private final View header;
    private int height;

    public GameRecordListAdapter(Context context, List<Game> games, View header, int height) {
        this.mContext = context;
        this.mGames = games;
        this.header = header;
        this.height = height;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        View itemLayoutView;

        if (viewType == ITEM_VIEW_TYPE_HEADER) {

            itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_recyclerview_header, null);
            itemLayoutView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.getTabsHeight(mContext)));
            viewHolder = new ViewHolder(itemLayoutView);
            return viewHolder;
        }
        itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gridview_item, null);
        viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        if (viewHolder.getItemViewType() == 0) {
            return;
        }
        Game game = getItem(position - 1);
        if (viewHolder.getItemViewType() != 0) {
            viewHolder.itemPackageName.setText(game.name);
            Picasso.with(mContext)
                    .load(game.icons.regular)
                    .tag(game.play_store_id)
                    .into(viewHolder.itemImage);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return isHeader(position) ? ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_ITEM;
    }

    public boolean isHeader(int position) {
        return position == 0;
    }

    @Override
    public int getItemCount() {
        return mGames.size() + 1;
    }

    public Game getItem(int position) {
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

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

}
