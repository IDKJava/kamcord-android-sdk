package com.kamcord.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.TypedValue;
import android.view.Window;

import com.kamcord.app.R;

public class ViewUtils {

    public static int getToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        return toolbarHeight;
    }

    public static int getTabsHeight(Context context) {
        return (int) context.getResources().getDimension(R.dimen.tabsHeight);
    }

    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static void setUpActionBar(Activity activityReference) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            activityReference.getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
            activityReference.getActionBar().hide();
        }
    }
}
