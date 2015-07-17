package com.kamcord.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kamcord.app.server.model.Game;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by pplunkett on 5/22/15.
 */
public class GameListUtils {
    private static final String CACHE_PREFS = "CACHE_PREFS";
    private static final String INSTALLED_GAME_LIST = "INSTALLED_GAME_LIST";
    private static SharedPreferences preferences = null;
    private static PackageManager pm;

    public synchronized static void initializeWith(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE);
        pm = context.getPackageManager();
    }

    public static List<Game> getCachedInstalledGameList() {
        List<Game> cachedInstalledGameList = new ArrayList<>();
        try {
            String jsonGameList = preferences.getString(INSTALLED_GAME_LIST, "[]");
            Type type = new TypeToken<List<Game>>() {}.getType();
            cachedInstalledGameList = new Gson().fromJson(jsonGameList, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cachedInstalledGameList;
    }

    public static void saveInstalledGameList(List<Game> gameList) {
        try {
            preferences.edit()
                    .putString(INSTALLED_GAME_LIST, new Gson().toJson(gameList))
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Set<String> getInstalledPackages() {
        Set<String> installedPackages = new HashSet<>();

        List<PackageInfo> packageInfos = pm.getInstalledPackages(0);
        for( PackageInfo packageInfo : packageInfos ) {
            installedPackages.add(packageInfo.packageName);
        }

        return installedPackages;
    }
}
