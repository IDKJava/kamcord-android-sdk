package com.kamcord.app.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import com.kamcord.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pplunkett on 6/3/15.
 */
public class FirstInstalledViewHolder extends GameItemViewHolder {
    @InjectView(R.id.recordAndShareTextView)
    public TextView recordAndShareTextView;

    public FirstInstalledViewHolder(View itemLayoutView) {
        super(itemLayoutView);
        ButterKnife.inject(this, itemLayoutView);
    }

}
