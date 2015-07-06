package com.kamcord.app.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by donliang1 on 5/27/15.
 */
public class KeyboardUtils {

    public static void hideSoftKeyboard(View view, Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm.isActive() == true && view.getWindowToken() != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void showSoftKeyboard(View view, Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, 0);
//        if(imm.isActive() == true && view.getWindowToken() != null) {
//            imm.showSoftInputFromInputMethod(view.getWindowToken(), 0);
//        }
    }

    public static void setSoftKeyboardVisibility(View view, Context context, boolean visible) {
        if( visible ) {
            showSoftKeyboard(view, context);
        } else {
            hideSoftKeyboard(view, context);
        }
    }
}
