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
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.Account;
import com.kamcord.app.server.model.GenericResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;

import javax.net.ssl.HttpsURLConnection;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

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
            AppServerClient.getInstance().unregisterPushNotif(unRegisterNotifCallback);
        }
    }

    private static Callback<GenericResponse<?>> unRegisterNotifCallback = new Callback<GenericResponse<?>>() {
        @Override
        public void success(GenericResponse<?> responseWrapper, Response response) {
            Log.e("Retrofit UnRegister", "  " + "success");
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e("Retrofit Failure", "  " + error.toString());
        }
    };

    public static class YouTube {
        private static final String YOUTUBE_NAME_KEY = "youtube_name";
        private static final String YOUTUBE_TYPE_KEY = "youtube_type";
        private static final String YOUTUBE_AUTH_CODE_KEY = "youtube_auth_code";
        private static final String YOUTUBE_ACCESS_TOKEN_KEY = "youtube_access_token";
        private static final String YOUTUBE_REFRESH_TOKEN_KEY = "youtube_refresh_token";

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

        public static String getStoredAccessToken() {
            return preferences.getString(YOUTUBE_ACCESS_TOKEN_KEY, null);
        }

        public static String getStoredRefreshToken() {
            return preferences.getString(YOUTUBE_REFRESH_TOKEN_KEY, null);
        }

        private static final String CLIENT_ID = "1003397135098-vhs5iocngq6re8mrd30id78rffuq31dt.apps.googleusercontent.com";
        private static final String CLIENT_SECRET = "6SonhMps5tW_e3VYrZekh76S"; // TODO: We really shouldn't be storing this on the client...
        public static void fetchAuthorizationCode(final Activity activity, final int requestCode) {
            final android.accounts.Account youTubeAccount = YouTube.getStoredAccount();
            if (youTubeAccount != null) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        String auth_code = null;
                        boolean errored = false;

                        try {
                            auth_code = GoogleAuthUtil.getToken(activity.getApplicationContext(), youTubeAccount,
                                    "oauth2:server:client_id:"
                                            + CLIENT_ID + ":"
                                            + "api_scope:https://gdata.youtube.com https://www.googleapis.com/auth/userinfo.profile");
                            preferences.edit()
                                    .putString(YOUTUBE_AUTH_CODE_KEY, auth_code)
                                    .commit();

                            // TODO: Ideally, we'd just stop here and send the authorization code to server only,
                            // but for now, we're just going to get the tokens ourselves.
                            URL url = new URL("https://accounts.google.com/o/oauth2/token");
                            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                            conn.setReadTimeout(10000);
                            conn.setConnectTimeout(15000);
                            conn.setRequestMethod("POST");
                            conn.setDoInput(true);
                            conn.setDoOutput(true);

                            String[] names = new String[]{
                                    "grant_type",
                                    "code",
                                    "client_id",
                                    "client_secret"};
                            String[] values = new String[]{
                                    "authorization_code",
                                    auth_code,
                                    CLIENT_ID,
                                    CLIENT_SECRET};

                            StringBuilder queryBuilder = new StringBuilder();
                            for( int i=0; i<names.length; i++ ) {
                                if( i > 0 ) {
                                    queryBuilder.append("&");
                                }
                                queryBuilder.append(URLEncoder.encode(names[i], "UTF-8"))
                                        .append("=")
                                        .append(URLEncoder.encode(values[i], "UTF-8"));
                            }

                            OutputStream os = conn.getOutputStream();
                            BufferedWriter writer = new BufferedWriter(
                                    new OutputStreamWriter(os, "UTF-8"));
                            writer.write(queryBuilder.toString());
                            writer.flush();
                            writer.close();
                            os.close();

                            conn.connect();
                            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line).append("\n");
                            }
                            br.close();
                            String responseString = sb.toString();

                            int responseCode = conn.getResponseCode();
                            if( responseCode == 200 || responseCode == 201 ) {

                                JSONObject jsonResponse = new JSONObject(responseString);
                                if( jsonResponse.has("access_token") && jsonResponse.has("refresh_token") ) {
                                    String accessToken = jsonResponse.getString("access_token");
                                    String refreshToken = jsonResponse.getString("refresh_token");
                                    preferences.edit()
                                            .putString(YOUTUBE_ACCESS_TOKEN_KEY, accessToken)
                                            .putString(YOUTUBE_REFRESH_TOKEN_KEY, refreshToken)
                                            .commit();
                                } else {
                                    errored = true;
                                }

                            } else {
                                Log.e("Kamcord", "Non-200 when attempting to fetch tokens!");
                                Log.e("Kamcord", "Message: " + responseString);
                                errored = true;
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            errored = true;
                        } catch (UserRecoverableAuthException e) {
                            if( requestCode >= 0 ) {
                                activity.startActivityForResult(e.getIntent(), requestCode);
                            }
                        } catch (GoogleAuthException e) {
                            e.printStackTrace();
                            errored = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            errored = true;
                        } catch( Exception e ) {
                            e.printStackTrace();
                        }

                        // If anything goes wrong, we clear the authorization so the user can try again next time.
                        if( errored && auth_code != null ) {
                            try {
                                GoogleAuthUtil.clearToken(activity.getApplicationContext(), auth_code);
                            } catch( Exception e ) {
                            }
                        }
                        return null;
                    }
                }.execute();

            }
        }
    }
}
