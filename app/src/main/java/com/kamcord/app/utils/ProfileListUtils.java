package com.kamcord.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kamcord.app.model.ProfileItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by donliang1 on 15/6/23.
 */
public class ProfileListUtils {
    private static final String CACHE_PREFS = "CACHE_PREFS";
    private static final String PROFILE_LIST = "PROFILE_LIST";
    private static SharedPreferences preferences = null;

    public synchronized static void initializeWith(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE);
    }

    public static List<ProfileItem> getCachedProfileList() {
        List<ProfileItem> cachedProfileItemList = new ArrayList<>();
        try {
            String jsonProfileItemList = preferences.getString(PROFILE_LIST, "[]");
            Type type = new TypeToken<List<ProfileItem>>() {}.getType();
            cachedProfileItemList = new Gson().fromJson(jsonProfileItemList, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cachedProfileItemList;
    }

    public static void saveProfileList(List<ProfileItem> profileItemList) {
        try {
            preferences.edit()
                    .putString(PROFILE_LIST, new Gson().toJson(profileItemList))
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
