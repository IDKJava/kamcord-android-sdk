package com.kamcord.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kamcord.app.server.model.Game;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pplunkett on 5/22/15.
 */
public class GameListUtils {
    private static final String CACHE_PREFS = "CACHE_PREFS";
    private static final String GAME_LIST = "GAME_LIST";
    private static SharedPreferences preferences = null;

    public synchronized static void initializeWith(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE);
    }

    public static List<Game> getCachedGameList() {
        List<Game> cachedGameList = new ArrayList<>();
        try {
            String jsonGameList = preferences.getString(GAME_LIST, "[]");
            Type type = new TypeToken<List<Game>>() {}.getType();
            cachedGameList = new Gson().fromJson(jsonGameList, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cachedGameList;
    }

    public static void saveGameList(List<Game> gameList) {
        try {
            preferences.edit()
                    .putString(GAME_LIST, new Gson().toJson(gameList))
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
