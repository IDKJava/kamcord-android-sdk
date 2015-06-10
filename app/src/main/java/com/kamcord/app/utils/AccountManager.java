package com.kamcord.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.kamcord.app.server.model.Account;

import java.util.ArrayList;

/**
 * Created by pplunkett on 5/13/15.
 */
public class AccountManager {
    private static final String ACCOUNT_PREFS = "ACCOUNT_PREFS";
    private static final String ACCOUNT = "ACCOUNT";
    private static final Object accountLock = new Object();
    private static SharedPreferences preferences = null;
    private static boolean currentLoginState = false;
    private static ArrayList<AccountListener> accountListeners = new ArrayList<AccountListener>();

    public synchronized static void initializeWith(Context context)
    {
        preferences = context.getApplicationContext()
                .getSharedPreferences(ACCOUNT_PREFS, Context.MODE_PRIVATE);
        currentLoginState = isLoggedIn();
    }

    public static void addListener(AccountListener listener)
    {
        synchronized(accountLock)
        {
            if( accountListeners != null ) {
                accountListeners.add(listener);
            }
        }
    }

    public static void removeListener(AccountListener listener)
    {
        synchronized(accountLock)
        {
            if( accountListeners != null ) {
                accountListeners.remove(listener);
            }
        }
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

    private static void notifyLoginStateChanged(boolean state)
    {
        synchronized (accountLock)
        {
            if( accountListeners != null )
            {
                for( AccountListener listener : accountListeners )
                    listener.isLoggedInChanged(state);
            }
        }
    }

    public static void setStoredAccount(Account account)
    {
        if( preferences != null )
        {
            preferences.edit().putString(ACCOUNT, new Gson().toJson(account)).apply();
            if( isLoggedIn() != currentLoginState )
            {
                currentLoginState = isLoggedIn();
                notifyLoginStateChanged(currentLoginState);
            }
        }
    }

    public static void clearStoredAccount()
    {
        if( preferences != null )
        {
            preferences.edit().remove(ACCOUNT).apply();
            if( isLoggedIn() != currentLoginState )
            {
                currentLoginState = isLoggedIn();
                notifyLoginStateChanged(currentLoginState);
            }
        }
    }
}
