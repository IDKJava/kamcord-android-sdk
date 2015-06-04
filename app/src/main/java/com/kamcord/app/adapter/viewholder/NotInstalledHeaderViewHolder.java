package com.kamcord.app.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.kamcord.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pplunkett on 6/4/15.
 */
public class NotInstalledHeaderViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.alsoRecordTheseTextView)
    public TextView alsoRecordTheseTextView;

    public NotInstalledHeaderViewHolder(View itemLayoutView) {
        super(itemLayoutView);
        ButterKnife.inject(this, itemLayoutView);
    }

}
