package com.kamcord.app.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.TypedValue;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.Button;

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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            activityReference.getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
            if(activityReference.getActionBar() != null) {
                activityReference.getActionBar().hide();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void buttonCircularReveal(Button button) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewAnimationUtils.createCircularReveal(button,
                    button.getWidth() / 2, button.getHeight() / 2, 0,
                    button.getHeight() * 2).start();
        }
    }

    public static Drawable getTintedDrawable(Context context, Drawable drawable, int color) {
        Resources resources = context.getResources();
        Drawable tintedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(tintedDrawable, resources.getColor(color));
        DrawableCompat.setTintMode(tintedDrawable.mutate(), PorterDuff.Mode.MULTIPLY);
        return tintedDrawable;
    }
}
