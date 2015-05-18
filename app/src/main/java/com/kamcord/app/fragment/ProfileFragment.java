package com.kamcord.app.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.kamcord.app.R;
import com.kamcord.app.activity.LoginActivity;
import com.kamcord.app.server.model.Account;
import com.kamcord.app.utils.AccountManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by donliang1 on 5/6/15.
 */
public class ProfileFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String KAMCORD_DOMAIN = "www.kamcord.com";
    private static final String KAMCORD_PROFILE_BASE_URL = "https://" + KAMCORD_DOMAIN + "/profile/";
    private static final Pattern domainPattern = Pattern.compile(".*?([^.]+\\.[^.]+)$");

    @InjectView(R.id.webView) WebView webView;
    @InjectView(R.id.signInPromptContainer) ViewGroup signInPromptContainer;
    @InjectView(R.id.signInPromptButton) Button signInPromptButton;
    @InjectView(R.id.webViewRefreshLayout) SwipeRefreshLayout webViewRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.profile_tab, container, false);

        ButterKnife.inject(this, root);

        webViewRefreshLayout.setEnabled(false);

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
            webView.setVisibility(View.VISIBLE);
            Account account = AccountManager.getStoredAccount();
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new SameDomainWebViewClient(KAMCORD_DOMAIN));

            webViewRefreshLayout.setEnabled(false);
            webViewRefreshLayout.setOnRefreshListener(this);
            webViewRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    webViewRefreshLayout.setRefreshing(true);
                }
            });

            webView.loadUrl(KAMCORD_PROFILE_BASE_URL + account.username);
        }
        else
        {
            signInPromptContainer.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.signInPromptButton)
    public void showSignInPrompt()
    {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        webViewRefreshLayout.setEnabled(false);
        if (AccountManager.isLoggedIn()) {
            Account account = AccountManager.getStoredAccount();
            webViewRefreshLayout.setRefreshing(true);
            webView.loadUrl(KAMCORD_PROFILE_BASE_URL + account.username);
        }
        else {
            webViewRefreshLayout.setRefreshing(false);
        }
    }

    public class SameDomainWebViewClient extends WebViewClient
    {
        private String domain = null;

        SameDomainWebViewClient(String domain)
        {
            this.domain = domain;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url)
        {
            Uri uri = Uri.parse(url);
            return !hasThisDomain(uri);
        }

        @Override
        public void onPageFinished(WebView webView, String url)
        {
            webViewRefreshLayout.setEnabled(true);
            webViewRefreshLayout.setRefreshing(false);
        }

        private boolean hasThisDomain(Uri uri)
        {
            String uriDomain = "";
            Matcher m = domainPattern.matcher(uri.getHost());
            if( m.matches() )
            {
                uriDomain = m.group(1);
            }

            return uriDomain.equals(domain);
        }
    }
}
