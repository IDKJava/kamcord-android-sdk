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
import com.kamcord.app.utils.AccountManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by pplunkett on 5/12/15.
 */
public class WelcomeFragment extends Fragment {

    @InjectView(R.id.subtitleTextView) TextView subtitleTextView;
    @InjectView(R.id.skipButton) Button skipButton;
    @InjectView(R.id.createProfileButton) Button createProfileButton;
    @InjectView(R.id.loginButton) Button loginButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_welcome, container, false);

        ButterKnife.inject(this, root);
        initializeSubtitleText();

        if( AccountManager.isLoggedIn() )
        {
            createProfileButton.setVisibility(View.GONE);
            loginButton.setVisibility(View.GONE);
            root.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getActivity(), RecordActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }, 3000);
            // TODO: cancel this delayed message if this activity is already finished.
        }
        else
        {
            AccountManager.clearStoredAccount();
            createProfileButton.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.VISIBLE);
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private void initializeSubtitleText() {
        String subtitle = getResources().getString(R.string.socialNetworkForGamers);
        String subtitleHighlighted = getResources().getString(R.string.gamers);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(subtitle);
        int indexOfMatchStr = subtitle.indexOf(subtitleHighlighted);
        spannableStringBuilder.setSpan(new TextAppearanceSpan(getActivity(), R.style.TextAppearance_KamcordHighlighted),
                indexOfMatchStr,
                indexOfMatchStr + subtitleHighlighted.length(),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        subtitleTextView.setText(spannableStringBuilder);
    }

    private int getContainerViewId() {
        if (getActivity() instanceof LoginActivity) {
            return ((LoginActivity) getActivity()).getContainerViewId();
        }
        return 0;
    }

    @OnClick(R.id.skipButton)
    public void skip() {
        Intent intent = new Intent(getActivity(), RecordActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        getActivity().finish();
    }

    @OnClick(R.id.createProfileButton)
    public void pushCreateProfileFragment() {
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .replace(getContainerViewId(), new CreateProfileFragment())
                .addToBackStack("CreateProfileFragment").commit();
    }

    @OnClick(R.id.loginButton)
    public void pushLoginFragment() {
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .replace(getContainerViewId(), new LoginFragment())
                .addToBackStack("LoginFragment").commit();

    }
}
