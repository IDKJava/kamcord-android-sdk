package com.kamcord.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * Created by pplunkett on 5/20/15.
 */
public class ObservableWebView extends WebView {
    public interface ObservableWebViewScrollListener
    {
        void onObservableWebViewScrolled(ObservableWebView webView, int dx, int dy);
    }

    private ObservableWebViewScrollListener mScrollListener;

    public ObservableWebView(final Context context)
    {
        super(context);
    }

    public ObservableWebView(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ObservableWebView(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public void setObservableWebViewScrollListener(ObservableWebViewScrollListener listener)
    {
        this.mScrollListener = listener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt)
    {
        super.onScrollChanged(l, t, oldl, oldt);
        if( mScrollListener != null )
        {
            mScrollListener.onObservableWebViewScrolled(this, l-oldl, t-oldt);
        }
    }
}
