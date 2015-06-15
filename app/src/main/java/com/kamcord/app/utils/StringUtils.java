package com.kamcord.app.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;

import com.kamcord.app.R;
import com.kamcord.app.model.RecordingSession;

import java.util.Locale;

/**
 * Created by donliang1 on 5/12/15.
 */
public class StringUtils {
    private static final String TAG = StringUtils.class.getSimpleName();

    public static URLSpan newURLSpan(final String url) {
        URLSpan urlSpan = new URLSpan(url)
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
                catch( Exception e )
                {
                    Log.w(TAG, "Could not launch activity for URL " + url, e);
                }
            }
        };
        return urlSpan;
    }

    public static SpannableStringBuilder linkify(String sourceText, String[] linkTexts, String[] linkURLs)
    {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(sourceText);

        if( linkTexts.length == linkURLs.length )
        {
            for( int i=0; i<linkTexts.length; i++ )
            {
                int index = sourceText.indexOf(linkTexts[i]);
                if( index >= 0 )
                {
                    spannableStringBuilder.setSpan(newURLSpan(linkURLs[i]),
                            index, index + linkTexts[i].length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        return spannableStringBuilder;
    }

    public static void highLightText(Context context, URLSpan urlSpan, String originStr, String highLightStr, SpannableStringBuilder textViewStyle) {
        int indexOfMatchStr = originStr.indexOf(highLightStr);
        textViewStyle.setSpan(urlSpan,
                indexOfMatchStr,
                indexOfMatchStr + highLightStr.length(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        textViewStyle.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.TermsHighLighted)),
                indexOfMatchStr,
                indexOfMatchStr + highLightStr.length(),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        textViewStyle.setSpan(new StyleSpan(Typeface.BOLD),
                indexOfMatchStr,
                indexOfMatchStr + highLightStr.length(),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
    }

    public static boolean compare(String first, String second)
    {
        return (first == null && second == null)
                || (first != null && second != null && first.equals(second));
    }

    public static String abbreviatedCount(long count) {
        if( count < 1000 ) {
            return Long.toString(count);
        } else if( count < 1000000 ) {
            return String.format(Locale.ENGLISH, "%.1fK", ((double) count) / 1e3);
        } else if( count < 1000000000 ) {
            return String.format(Locale.ENGLISH, "%.1fM", ((double) count) / 1e6);
        } else {
            return String.format(Locale.ENGLISH, "%.1fB", ((double) count) / 1e9);
        }
    }

    public static String defaultVideoTitle(Context context, RecordingSession session) {
        return String.format(Locale.ENGLISH,
                context.getString(R.string.myLatestVideo),
                session.getGameServerName());
    }
}
