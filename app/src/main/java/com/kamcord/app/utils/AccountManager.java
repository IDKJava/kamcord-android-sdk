package com.kamcord.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.kamcord.app.server.model.Account;

/**
 * Created by pplunkett on 5/13/15.
 */
public class AccountManager {
    private static final String ACCOUNT_PREFS = "ACCOUNT_PREFS";
    private static final String ACCOUNT = "ACCOUNT";
    private static SharedPreferences preferences = null;

    public synchronized static void initializeWith(Context context)
    {
        preferences = context.getApplicationContext()
                .getSharedPreferences(ACCOUNT_PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isLoggedIn()
    {
        Account account = getStoredAccount();
        return account != null && account.token != null;
    }
    public static Account getStoredAccount()
    {
        Account account = null;

        if( preferences != null )
        {
            try {
                account = new Gson().fromJson(preferences.getString(ACCOUNT, null), Account.class);
            }
            catch( Exception e )
            {
                Log.e("Kamcord", "Unable to get stored account!", e);
            }
        }

        return account;
    }

    public static void setStoredAccount(Account account)
    {
        if( preferences != null )
        {
            preferences.edit().putString(ACCOUNT, new Gson().toJson(account)).apply();
        }
    }

    public static void clearStoredAccount()
    {
        if( preferences != null )
        {
            preferences.edit().remove(ACCOUNT).apply();
        }
    }
}
