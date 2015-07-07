package com.kamcord.app.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kamcord.app.R;
import com.kamcord.app.server.model.User;
import com.kamcord.app.view.utils.VisibilityHandler;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * Created by pplunkett on 7/6/15.
 */
public class LiveMediaControls implements MediaControls {
    private RelativeLayout root;
    private VisibilityHandler visibilityHandler;

    private User owner;

    @InjectView(R.id.owner_container)
    ViewGroup ownerContainer;
    @InjectView(R.id.share_button)
    ImageButton shareButton;
    @InjectView(R.id.profile_letter_textview)
    TextView profileLetterTextView;
    @InjectView(R.id.avatar_imageview)
    ImageView avatarImageView;
    @InjectView(R.id.username_textview)
    TextView usernameTextView;
    @InjectView(R.id.follow_button)
    Button followButton;

    public LiveMediaControls(Context context, User owner) {
        root = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.view_live_media_controls, null);
        visibilityHandler = new VisibilityHandler(root);
        this.owner = owner;

    }

    @Override
    public void setAnchorView(View anchorView) {
        if( anchorView instanceof ViewGroup ) {
            ((ViewGroup) anchorView).addView(root);

            ButterKnife.inject(this, root);

            if( owner != null ) {
                avatarImageView.setVisibility(View.GONE); // TODO: unhide this when we start receiving avatars from server

                usernameTextView.setText(owner.username);

                if (owner.username != null && owner.username.length() > 0) {
                    profileLetterTextView.setText(owner.username.substring(0, 1).toUpperCase());
                    CalligraphyUtils.applyFontToTextView(root.getContext(), profileLetterTextView, "fonts/proximanova_semibold.otf");
                }

                int profileColor = root.getResources().getColor(R.color.defaultProfileColor);
                try {
                    profileColor = Color.parseColor(owner.profile_color);
                } catch (Exception e) {
                }
                profileLetterTextView.getBackground().setColorFilter(profileColor, PorterDuff.Mode.MULTIPLY);
            } else {
                ownerContainer.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return root.dispatchKeyEvent(event);
    }

    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
    }

    @Override
    public void setEnabled(boolean enabled) {
        root.setEnabled(enabled);
    }

    @Override
    public void show(int timeout, boolean fade) {
        visibilityHandler.show(owner == null || owner.is_user_following ? timeout : 0, fade);
    }

    @Override
    public void hide(boolean fade) {
        visibilityHandler.hide(fade );
    }

    @Override
    public boolean isShowing() {
        return root.getAlpha() > 0f;
    }
}
