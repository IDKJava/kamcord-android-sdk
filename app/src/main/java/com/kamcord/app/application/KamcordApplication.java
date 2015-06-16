package com.kamcord.app.application;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.kamcord.app.BuildConfig;
import com.kamcord.app.R;
import com.kamcord.app.analytics.KamcordAnalytics;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.ActiveRecordingSessionManager;
import com.kamcord.app.utils.ApplicationStateUtils;
import com.kamcord.app.utils.Connectivity;
import com.kamcord.app.utils.DeviceManager;
import com.kamcord.app.utils.GameListUtils;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class KamcordApplication extends Application {

    private final static String FLURRY_API_KEY = "PDK8Q3PP86J4M3DBXQJH";
    private final static String TWITTER_CONSUMER_KEY = "AMmZst034vuzxLIKhug1tw";
    private final static String TWITTER_CONSUMER_SECRET = "JpQanvqL0EjpIVI4GrWhwxQ5ErRBFXfCaYeRgXUR20";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("FindMe", "KamcordApplication.onCreate()");

        if (!BuildConfig.DEBUG) {
            Fabric.with(
                    this,
                    new Crashlytics(),
                    new Twitter(new TwitterAuthConfig(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET)));
        } else {
            Fabric.with(this, new Twitter(new TwitterAuthConfig(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET)));
        }

        AccountManager.initializeWith(this);
        DeviceManager.initialize();
        GameListUtils.initializeWith(this);
        ApplicationStateUtils.initializeWith(this);
        ActiveRecordingSessionManager.initializeWith(this);
        Connectivity.initializeWith(this);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/proximanova_regular.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        FlurryAgent.setLogEnabled(true);
        FlurryAgent.setLogLevel(Log.VERBOSE);
        FlurryAgent.init(this, BuildConfig.DEBUG ? "nonsense" : FLURRY_API_KEY);

        KamcordAnalytics.initializeWith(this);
    }
}
