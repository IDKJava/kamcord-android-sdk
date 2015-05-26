package com.kamcord.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kamcord.app.R;
import com.kamcord.app.adapter.viewholder.InstalledViewHolder;
import com.kamcord.app.adapter.viewholder.NotInstalledViewHolder;
import com.kamcord.app.server.model.Game;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

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
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View itemLayoutView = inflater.inflate(R.layout.view_game_item_not_installed, null);
        RecyclerView.ViewHolder viewHolder = new NotInstalledViewHolder(itemLayoutView, mItemClickListener);

        switch( viewType )
        {
            case VIEW_TYPE_FIRST_INSTALLED:
                itemLayoutView = inflater.inflate(R.layout.view_game_item_first_installed, null);
                viewHolder = new InstalledViewHolder(itemLayoutView, mItemClickListener);
                break;

            case VIEW_TYPE_INSTALLED:
                itemLayoutView = inflater.inflate(R.layout.view_game_item_installed, null);
                viewHolder = new InstalledViewHolder(itemLayoutView, mItemClickListener);
                break;

            case VIEW_TYPE_LAST_INSTALLED:
                itemLayoutView = inflater.inflate(R.layout.view_game_item_last_installed, null);
                viewHolder = new InstalledViewHolder(itemLayoutView, mItemClickListener);
                break;

            default:
                break;
        }


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

        } else if( viewHolder instanceof InstalledViewHolder) {
            InstalledViewHolder firstInstalledViewHolder = (InstalledViewHolder) viewHolder;
            Picasso.with(mContext)
                    .load(game.icons.regular)
                    .tag(game.play_store_id)
                    .into(firstInstalledViewHolder.gameThumbnailImageView);
            firstInstalledViewHolder.gameNameTextView.setText(game.name);
            firstInstalledViewHolder.gameFollowerCountTextView.setText(
                    String.format(Locale.ENGLISH,
                            mContext.getResources().getString(R.string.followersWithCount),
                            game.number_of_followers));

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
