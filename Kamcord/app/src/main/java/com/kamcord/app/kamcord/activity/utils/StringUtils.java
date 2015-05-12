package com.kamcord.app.kamcord.activity.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.view.View;

import com.kamcord.app.kamcord.R;

/**
 * Created by donliang1 on 5/12/15.
 */
public class StringUtils {

    public static URLSpan makeURLSpan(final Activity activity, String url) {
        return new URLSpan(url) {
            @Override
            public void onClick(View widget) {
                Uri uri = Uri.parse("https://www.kamcord.com/tos/");
                activity.startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        };
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
}
