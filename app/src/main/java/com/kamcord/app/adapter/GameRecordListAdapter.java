package com.kamcord.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kamcord.app.R;
import com.kamcord.app.server.model.Game;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class GameRecordListAdapter extends RecyclerView.Adapter<GameRecordListAdapter.ViewHolder> {

    private Context mContext;
    private List<Game> mGames;
    private static OnItemClickListener mItemClickListener;

    public GameRecordListAdapter(Context context, List<Game> games) {
        this.mContext = context;
        this.mGames = games;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        View itemLayoutView;

        itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gridview_item, null);
        viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Game game = getItem(position);
        viewHolder.itemPackageName.setText(game.name);
        Picasso.with(mContext)
                .load(game.icons.regular)
                .tag(game.play_store_id)
                .into(viewHolder.itemImage);
        if (game.isInstalled) {
            viewHolder.installGameTextView.setVisibility(View.GONE);
        } else {
            viewHolder.installGameTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mGames.size();
    }

    public Game getItem(int position) {
        return mGames.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @Optional @InjectView(R.id.item_packagename) TextView itemPackageName;
        @Optional @InjectView(R.id.item_image) ImageView itemImage;
        @Optional @InjectView(R.id.installGameTextView) TextView installGameTextView;

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
