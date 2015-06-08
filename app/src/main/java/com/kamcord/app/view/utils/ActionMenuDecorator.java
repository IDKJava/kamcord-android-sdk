package com.kamcord.app.view.utils;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;

/**
 * Created by donliang1 on 6/8/15.
 */
public class ActionMenuDecorator {

    public static void setMenuItemColor(MenuItem menuItem, int color) {
        Drawable itemIcon = menuItem.getIcon();
        itemIcon.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        menuItem.setIcon(itemIcon);
    }
}
