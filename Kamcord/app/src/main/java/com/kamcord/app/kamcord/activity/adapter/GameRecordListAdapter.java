package com.kamcord.app.kamcord.activity.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kamcord.app.kamcord.R;
import com.kamcord.app.kamcord.activity.server.model.Game;
import com.squareup.picasso.Picasso;

import java.util.List;

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

        itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_row, null);
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
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return mGames.size();
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
