package com.kamcord.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kamcord.app.model.FeedItem;
import com.kamcord.app.server.model.User;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by donliang1 on 15/6/23.
 */
public class ProfileListUtils {
    private static final String CACHE_PREFS = "CACHE_PREFS";
    private static final String PROFILE_INFO = "PROFILE_USER_INFO";
    private static final String PROFILE_LIST = "PROFILE_LIST";
    private static SharedPreferences preferences = null;

    public synchronized static void initializeWith(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE);
    }

    public static List<FeedItem> getCachedProfileList() {
        List<FeedItem> cachedFeedItemList = new ArrayList<>();
        try {
            String jsonProfileItemList = preferences.getString(PROFILE_LIST, "[]");
            Type type = new TypeToken<List<FeedItem>>() {}.getType();
            cachedFeedItemList = new Gson().fromJson(jsonProfileItemList, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cachedFeedItemList;
    }

    public static void saveProfileList(List<FeedItem> feedItemList) {
        try {
            preferences.edit()
                    .putString(PROFILE_LIST, new Gson().toJson(feedItemList))
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static User getCachedProfileInfo() {
        User cachedProfileInfo = new User();
        try {
            String jsonProfileInfo = preferences.getString(PROFILE_INFO, "[]");
            Type type = new TypeToken<User>() {}.getType();
            cachedProfileInfo = new Gson().fromJson(jsonProfileInfo, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cachedProfileInfo;
    }

    public static void saveProfileInfo(User userProfile) {
        try {
            preferences.edit()
                    .putString(PROFILE_INFO, new Gson().toJson(userProfile))
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
