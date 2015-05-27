package com.kamcord.app.utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by donliang1 on 5/27/15.
 */
public class KeyboardUtils {

    public static void hideSoftKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
