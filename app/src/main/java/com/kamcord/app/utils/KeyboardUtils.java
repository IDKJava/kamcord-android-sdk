package com.kamcord.app.utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by donliang1 on 5/27/15.
 */
public class KeyboardUtils {

    public static void hideSoftKeyboard(EditText editText, Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm.isActive() == true) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }
}
