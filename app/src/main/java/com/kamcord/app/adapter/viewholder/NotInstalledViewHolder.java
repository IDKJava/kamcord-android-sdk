package com.kamcord.app.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kamcord.app.R;
import com.kamcord.app.adapter.GameRecordListAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pplunkett on 5/26/15.
 */
public class NotInstalledViewHolder extends RecyclerView.ViewHolder {

    @InjectView(R.id.item_packagename)
    public TextView itemPackageName;
    @InjectView(R.id.item_image)
    public ImageView itemImage;
    @InjectView(R.id.installGameTextView)
    public TextView installGameTextView;

    public NotInstalledViewHolder(final View itemLayoutView, final GameRecordListAdapter.OnItemClickListener listener) {
        super(itemLayoutView);
        ButterKnife.inject(this, itemLayoutView);
        itemLayoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(itemLayoutView, getAdapterPosition());
                }
            }
        });
    }
}
