package com.kamcord.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.kamcord.app.server.model.Game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            Set<String> cachedGameSet = preferences.getStringSet(GAME_LIST, new HashSet<String>());
            for (String gameString : cachedGameSet) {
                cachedGameList.add(new Gson().fromJson(gameString, Game.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cachedGameList;
    }

    public static void saveGameList(List<Game> gameList) {
        try {
            Set<String> gameStringsSet = new HashSet<>();
            for (Game game : gameList) {
                gameStringsSet.add(new Gson().toJson(game));
            }
            preferences.edit()
                    .putStringSet(GAME_LIST, gameStringsSet)
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
