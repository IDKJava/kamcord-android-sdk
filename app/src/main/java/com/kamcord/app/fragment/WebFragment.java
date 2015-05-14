package com.kamcord.app.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.kamcord.app.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by donliang1 on 5/6/15.
 */
public class WebFragment extends Fragment {


    public static final String URL = "url";
    public static final String RESTRICT_DOMAIN = "restrict_domain";
    private static final Pattern domainPattern = Pattern.compile(".*?([^.]+\\.[^.]+)$");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_web, container, false);

        String url = getArguments().getString(URL);
        if( url != null && !url.isEmpty() && root instanceof WebView )
        {
            WebView webView = (WebView) root;

            Matcher matcher = domainPattern.matcher(Uri.parse(url).getHost());
            if( matcher.matches() &&  getArguments().getBoolean(RESTRICT_DOMAIN) )
            {
                String domain = matcher.group(1);
                webView.setWebViewClient(new SameDomainWebViewClient(domain));
            }

            webView.loadUrl(url);
        }

        return root;
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
