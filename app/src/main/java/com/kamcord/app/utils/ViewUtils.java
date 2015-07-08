package com.kamcord.app.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;

import com.kamcord.app.R;

import java.util.concurrent.atomic.AtomicInteger;

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

    @TargetApi(21)
    public static void buttonCircularReveal(Button button) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
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

    private static final AtomicInteger nextGeneratedId = new AtomicInteger(1);

    public static int generateViewId() {
        if (Build.VERSION.SDK_INT < 17) {
            for (; ; ) {
                final int result = nextGeneratedId.get();
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
                if (nextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {

            return View.generateViewId();

        }
    }

    public static void setViewPadding(View view, int pixel) {
        if (Build.VERSION.SDK_INT < 17) {
            view.setPadding(pixel, 0, pixel, 0);
        }
    }
}
