package com.kamcord.app.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.kamcord.app.R;
import com.kamcord.app.activity.LoginActivity;
import com.kamcord.app.activity.RecordActivity;
import com.kamcord.app.server.model.Account;
import com.kamcord.app.utils.AccountManager;

import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by donliang1 on 5/6/15.
 */
public class ProfileFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String KAMCORD_DOMAIN = "kamcord.com";
    private static final String KAMCORD_PROFILE_BASE_URL = "https://www." + KAMCORD_DOMAIN + "/profile/";
    private static final Pattern domainPattern = Pattern.compile(".*?([^.]+\\.[^.]+)$");

    @InjectView(R.id.signInPromptContainer) ViewGroup signInPromptContainer;
    @InjectView(R.id.signInPromptButton) Button signInPromptButton;
    @InjectView(R.id.swipeRefreshLayout) SwipeRefreshLayout viewRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        ButterKnife.inject(this, root);

        viewRefreshLayout.setEnabled(false);
        viewRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(R.dimen.refreshEnd));
        viewRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.refreshColor));

        return root;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if(AccountManager.isLoggedIn()) {
            signInPromptContainer.setVisibility(View.GONE);

            viewRefreshLayout.setEnabled(false);
            viewRefreshLayout.setOnRefreshListener(this);
            viewRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    viewRefreshLayout.setRefreshing(true);
                }
            });
        }
        else
        {
//            signInPromptContainer.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.signInPromptButton)
    public void showSignInPrompt()
    {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onRefresh() {
        viewRefreshLayout.setEnabled(false);
        if (AccountManager.isLoggedIn()) {
            Account account = AccountManager.getStoredAccount();
            Activity activity = getActivity();
            if( activity instanceof RecordActivity )
            {
                ((RecordActivity) activity).showToolbar();
            }
            viewRefreshLayout.setRefreshing(true);
        }
        else {
            viewRefreshLayout.setRefreshing(false);
        }
    }

    private static float pxToDp(float px, Context context) {
        if( context != null ) {
            return px / context.getResources().getDisplayMetrics().density;
        }
        return px;
    }
}
