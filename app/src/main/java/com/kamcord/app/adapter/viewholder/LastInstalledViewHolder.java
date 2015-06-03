package com.kamcord.app.adapter.viewholder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.kamcord.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pplunkett on 6/3/15.
 */
public class LastInstalledViewHolder extends GameItemViewHolder {
    @InjectView(R.id.requestGameImageButton)
    public ImageButton requestGameImageButton;
    @InjectView(R.id.alsoRecordTheseTextView)
    public TextView alsoRecordTheseTextView;

    public LastInstalledViewHolder(View itemLayoutView) {
        super(itemLayoutView);
        ButterKnife.inject(this, itemLayoutView);
    }

}
