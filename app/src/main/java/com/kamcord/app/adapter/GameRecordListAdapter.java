package com.kamcord.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kamcord.app.R;
import com.kamcord.app.adapter.viewholder.NotInstalledViewHolder;
import com.kamcord.app.server.model.Game;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GameRecordListAdapter extends RecyclerView.Adapter<ViewHolder> {

    public static final int VIEW_TYPE_INSTALLED = 0;
    public static final int VIEW_TYPE_FIRST_INSTALLED = 1;
    public static final int VIEW_TYPE_LAST_INSTALLED = 2;
    public static final int VIEW_TYPE_NOT_INSTALLED = 3;

    private Context mContext;
    private List<Game> mGames;
    private OnItemClickListener mItemClickListener;

    public GameRecordListAdapter(Context context, List<Game> games, OnItemClickListener listener) {
        this.mContext = context;
        this.mGames = games;
        this.mItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        View itemLayoutView;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

//        switch( viewType )
//        {
//            case VIEW_TYPE_FIRST_INSTALLED:
//                itemLayoutView = inflater.inflate(R.layout.view_game_item_not_installed, null);
//                viewHolder = new NotInstalledViewHolder(itemLayoutView, mItemClickListener);
//                break;
//
//            case VIEW_TYPE_INSTALLED:
//
//        }

        itemLayoutView = inflater.inflate(R.layout.view_game_item_not_installed, null);
        viewHolder = new NotInstalledViewHolder(itemLayoutView, mItemClickListener);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        Game game = mGames.get(position);

        if( viewHolder instanceof NotInstalledViewHolder )
        {
            NotInstalledViewHolder notInstalledViewHolder = (NotInstalledViewHolder) viewHolder;
            notInstalledViewHolder.itemPackageName.setText(game.name);
            Picasso.with(mContext)
                    .load(game.icons.regular)
                    .tag(game.play_store_id)
                    .into(notInstalledViewHolder.itemImage);
            if (game.isInstalled) {
                notInstalledViewHolder.installGameTextView.setVisibility(View.GONE);
            } else {
                notInstalledViewHolder.installGameTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mGames.size();
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = VIEW_TYPE_NOT_INSTALLED;

        Game game = mGames.get(position);
        if (game.isInstalled) {
            if (position == 0) {
                viewType = VIEW_TYPE_FIRST_INSTALLED;

            } else if (position + 1 < mGames.size() && !mGames.get(position + 1).isInstalled) {
                viewType = VIEW_TYPE_LAST_INSTALLED;

            } else {
                viewType = VIEW_TYPE_INSTALLED;
            }
        }

        return viewType;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
