package com.kamcord.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.gson.Gson;
import com.kamcord.app.server.model.Account;

import java.io.IOException;
import java.util.HashSet;

/**
 * Created by pplunkett on 5/13/15.
 */
public class AccountManager {
    private static final String ACCOUNT_PREFS = "ACCOUNT_PREFS";
    private static final String ACCOUNT = "ACCOUNT";
    private static final Object accountLock = new Object();
    private static SharedPreferences preferences = null;
    private static boolean currentLoginState = false;
    private static HashSet<AccountListener> accountListeners = new HashSet<>();

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
                    listener.onLoggedInChanged(state);
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

    public static class YouTube {
        private static final String YOUTUBE_NAME_KEY = "youtube_name";
        private static final String YOUTUBE_TYPE_KEY = "youtube_type";
        private static final String YOUTUBE_AUTH_CODE_KEY = "youtube_auth_code";

        public static android.accounts.Account getStoredAccount() {
            android.accounts.Account youTubeAccount = null;
            if( preferences.contains(YOUTUBE_NAME_KEY) && preferences.contains(YOUTUBE_TYPE_KEY) ) {
                youTubeAccount = new android.accounts.Account(
                        preferences.getString(YOUTUBE_NAME_KEY, null),
                        preferences.getString(YOUTUBE_TYPE_KEY, null)
                );
            }
            return youTubeAccount;
        }

        public static void setStoredAccount(android.accounts.Account youTubeAccount) {
            preferences.edit()
                    .putString(YOUTUBE_NAME_KEY, youTubeAccount.name)
                    .putString(YOUTUBE_TYPE_KEY, youTubeAccount.type)
                    .commit();
        }

        public static void clearStoredAccount() {
            preferences.edit()
                    .remove(YOUTUBE_NAME_KEY)
                    .remove(YOUTUBE_TYPE_KEY)
                    .commit();
        }

        public static String getStoredAuthorizationCode() {
            String auth_code = null;

            if (YouTube.getStoredAccount() != null && preferences.contains(YOUTUBE_AUTH_CODE_KEY)) {
                auth_code = preferences.getString(YOUTUBE_AUTH_CODE_KEY, null);
            }

            return auth_code;
        }

        public static void fetchAuthorizationCode(final Activity activity, final int requestCode) {
            final android.accounts.Account youTubeAccount = YouTube.getStoredAccount();
            if (youTubeAccount != null) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            String auth_code = GoogleAuthUtil.getToken(activity.getApplicationContext(), youTubeAccount,
                                    "oauth2:server:client_id:"
                                            + "1003397135098-vhs5iocngq6re8mrd30id78rffuq31dt.apps.googleusercontent.com:"
                                            + "api_scope:https://gdata.youtube.com https://www.googleapis.com/auth/userinfo.profile");
                            preferences.edit()
                                    .putString(YOUTUBE_AUTH_CODE_KEY, auth_code)
                                    .commit();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (UserRecoverableAuthException e) {
                            if( requestCode >= 0 ) {
                                activity.startActivityForResult(e.getIntent(), requestCode);
                            }
                        } catch (GoogleAuthException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute();

            }
        }
    }
}
