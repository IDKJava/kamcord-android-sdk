package com.kamcord.app.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.kamcord.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pplunkett on 5/12/15.
 */
public class WelcomeFragment extends Fragment implements View.OnClickListener {

    @InjectView(R.id.subtitle_textview) TextView subtitleTextView;
    @InjectView(R.id.skip_btn) Button skipButton;
    @InjectView(R.id.create_profile_btn) Button createProfileButton;
    @InjectView(R.id.login_btn) Button loginButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_login, container, false);

        ButterKnife.inject(this, root);

        return root;
    }

    @Override
    public void onClick(View view) {

    }
}
