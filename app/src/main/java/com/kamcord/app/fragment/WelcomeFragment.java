package com.kamcord.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.kamcord.app.R;
import com.kamcord.app.activity.LoginActivity;
import com.kamcord.app.activity.RecordActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by pplunkett on 5/12/15.
 */
public class WelcomeFragment extends Fragment {

    @InjectView(R.id.subtitle_textview) TextView subtitleTextView;
    @InjectView(R.id.skip_btn) Button skipButton;
    @InjectView(R.id.create_profile_btn) Button createProfileButton;
    @InjectView(R.id.login_btn) Button loginButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_login, container, false);

        ButterKnife.inject(this, root);
        initializeSubtitleText();

        return root;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private void initializeSubtitleText()
    {
        String subtitle = getResources().getString(R.string.kamcordSubtitle);
        String subtitleHighlighted = getResources().getString(R.string.kamcordSubtitleHighlighted);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(subtitle);
        int indexOfMatchStr = subtitle.indexOf(subtitleHighlighted);
        spannableStringBuilder.setSpan(new TextAppearanceSpan(getActivity(), R.style.TextAppearance_KamcordHighlighted),
                indexOfMatchStr,
                indexOfMatchStr + subtitleHighlighted.length(),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        subtitleTextView.setText(spannableStringBuilder);
    }

    private int getContainerViewId()
    {
        if( getActivity() instanceof LoginActivity )
        {
            return ((LoginActivity) getActivity()).getContainerViewId();
        }
        return 0;
    }

    @OnClick(R.id.skip_btn)
    public void skip()
    {
        Intent intent = new Intent(getActivity(), RecordActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.create_profile_btn)
    public void createProfile()
    {
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
                .replace(getContainerViewId(), new CreateProfileFragment())
                .addToBackStack("CreateProfileFragment").commit();

    }

    @OnClick(R.id.login_btn)
    public void login()
    {
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
                .replace(getContainerViewId(), new LoginFragment())
                .addToBackStack("LoginFragment").commit();

    }
}
