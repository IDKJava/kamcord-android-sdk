package com.kamcord.app.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.kamcord.app.R;
import com.kamcord.app.activity.LoginActivity;
import com.kamcord.app.activity.RecordActivity;
import com.kamcord.app.server.model.Account;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.view.ObservableWebView;

import java.util.regex.Matcher;
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

    @InjectView(R.id.webView) ObservableWebView webView;
    @InjectView(R.id.signInPromptContainer) ViewGroup signInPromptContainer;
    @InjectView(R.id.signInPromptButton) Button signInPromptButton;
    @InjectView(R.id.webViewRefreshLayout) SwipeRefreshLayout webViewRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.profile_tab, container, false);

        ButterKnife.inject(this, root);

        webViewRefreshLayout.setEnabled(false);
        webViewRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(R.dimen.refreshEnd));
        webViewRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.refreshColor));

        Activity activity = getActivity();
        if( activity != null && activity instanceof ObservableWebView.ObservableWebViewScrollListener)
        {
            webView.setObservableWebViewScrollListener((ObservableWebView.ObservableWebViewScrollListener) activity);
        }

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

            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new SameDomainWebViewClient(KAMCORD_DOMAIN));
            webView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                    WebView wv = (WebView) view;
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN
                            && keyCode == KeyEvent.KEYCODE_BACK
                            && wv.canGoBack()) {
                        Activity activity = getActivity();
                        if( activity instanceof RecordActivity )
                        {
                            ((RecordActivity) activity).showToolbar();
                        }
                        wv.goBack();
                        return true;
                    }
                    return false;
                }
            });

            webViewRefreshLayout.setEnabled(false);
            webViewRefreshLayout.setOnRefreshListener(this);
            webViewRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    webViewRefreshLayout.setRefreshing(true);
                }
            });

            webView.loadUrl(KAMCORD_PROFILE_BASE_URL + AccountManager.getStoredAccount().username);
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
        getActivity().finish();
    }

    @Override
    public void onRefresh() {
        webViewRefreshLayout.setEnabled(false);
        if (AccountManager.isLoggedIn()) {
            Account account = AccountManager.getStoredAccount();
            Activity activity = getActivity();
            if( activity instanceof RecordActivity )
            {
                ((RecordActivity) activity).showToolbar();
            }
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
            boolean override = !hasThisDomain(uri);
            if( !override )
            {
                Activity activity = getActivity();
                if( activity instanceof RecordActivity )
                {
                    ((RecordActivity) activity).showToolbar();
                }
            }
            return override;
        }

        @Override
        public void onPageFinished(WebView webView, String url)
        {
            if( isResumed() ) {
                ((RecordActivity) getActivity()).showToolbar();
                webViewRefreshLayout.setEnabled(true);
                webViewRefreshLayout.setRefreshing(false);
                int px = getResources().getDimensionPixelSize(R.dimen.tabsHeight);
                int dp = Math.round(pxToDp(px, getActivity()));

                String js = String.format("document.body.style.marginTop= \"%dpx\"", dp);
                webView.evaluateJavascript(js, null);
            }
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

    private static float pxToDp(float px, Context context) {
        if( context != null ) {
            return px / context.getResources().getDisplayMetrics().density;
        }
        return px;
    }
}
