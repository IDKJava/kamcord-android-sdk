package com.kamcord.app.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.kamcord.app.R;

/**
 * Created by donliang1 on 5/12/15.
 */
public class WatchFragment extends Fragment{

    private String kamcordURL = "https://www.kamcord.com/games/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_tab, container, false);
        WebView wv = (WebView) v.findViewById(R.id.webView);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebViewClient(new WebViewClient());
        wv.loadUrl(kamcordURL);
        return v;
    }
}
