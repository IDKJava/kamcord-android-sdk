package com.kamcord.app.application;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;

import com.kamcord.app.R;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by Mehmet on 5/27/15.
 */
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 21)
public abstract class TestBase {

    protected static final int APP_TIMEOUT_MS = 5000;
    protected static final int UI_TIMEOUT_MS = 2000;
    protected static final int RECORDING_DURATION_MS = 5000;
    protected static final int UPLOAD_TIMEOUT = 10000;
    protected static final int MS_PER_MIN = 60000;
    protected static final int VIDEO_PROCESSING_TIMEOUT = 10000;
    protected static final String OVERFLOW_DESCRIPTION = "More options";
    protected static final String KAMCORD_APP_PACKAGE = "com.kamcord.app";
    protected static final String RIPPLE_TEST_APP_NAME = "Ripple Test";
    protected static final String RIPPLE_TEST_MAIN_RES = "com.kamcord.ripples:id/mainlayout";
    protected static final String ANDROID_DISMISS_TASK = "com.android.systemui:id/dismiss_task";
    protected static final String ANDROID_SYSTEM_BUTTON1 = "android:id/button1";

    protected UiDevice mDevice;

    @Before
    public void setUp(){
        startKamcordApp();
        doLogout();
    }

    @After
    public void cleanUp(){
        doLogout();
    }
    protected void startKamcordApp(){
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        //closeAllApps();

        mDevice.pressHome();

        final String launcherPackage = this.getLauncherPackageName();

        assertThat(launcherPackage, notNullValue());

        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), APP_TIMEOUT_MS);

        Context context = InstrumentationRegistry.getContext();

        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(KAMCORD_APP_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        boolean notTimedOut = mDevice.wait(Until.hasObject(By.pkg(KAMCORD_APP_PACKAGE).depth(0)), APP_TIMEOUT_MS);
        assertTrue("Application load timed out!", notTimedOut);

        mDevice.waitForIdle(APP_TIMEOUT_MS);
    }
    protected String getResByID(int resourceId) {
        Context currentContext = InstrumentationRegistry.getTargetContext();
        String prefix = currentContext.getPackageName();
        String id = currentContext.getResources().getResourceEntryName(resourceId);

        return String.format("%s:id/%s",prefix,id);
    }
    protected String getStrByID(int resourceId) {
        Context currentContext = InstrumentationRegistry.getTargetContext();
        String s = currentContext.getString(resourceId);
        return s;
    }
    protected boolean isLoggedIn(){
        UiObject2 loginActivity = mDevice
                .findObject(By.res(getResByID(R.id.fragment_welcome_layout)));
        if (loginActivity != null){
            UiObject2 loginButton = mDevice.findObject(By.res(getResByID(R.id.loginButton)));
            if (loginButton != null)
                return false;
        } else {
            UiObject2 profileTab = mDevice.findObject(By.text(getStrByID(R.string.kamcordProfileTab)));
            if (profileTab != null) {
                profileTab.click();
                boolean result =
                        mDevice.wait(Until.hasObject(By.res(getResByID(R.id.signInPromptButton))), UI_TIMEOUT_MS);
                return !result;
            }
        }
        return false;

    }
    protected boolean doLogin(){
        // only works from login screen.
        // need to build a state machine to make this work for all states.
        if(mDevice.hasObject(By.res(getResByID(R.id.fragment_welcome_layout)))){
            //we're on login screen, handle it
            handleWelcomeLoginView();

            boolean notTimedOut =
                    mDevice.wait(Until.hasObject(By.res(getResByID(R.id.activity_mdrecord_layout))),
                            UI_TIMEOUT_MS);
            return notTimedOut;
        }
        return false;
    }
    protected void handleWelcomeLoginView(){
        boolean notTimedOut =
                mDevice.wait(Until.hasObject(By.res(getResByID(R.id.loginButton))),
                        APP_TIMEOUT_MS);
        assertTrue("Login button not found on welcome screen!", notTimedOut);
        mDevice.findObject(By.res(getResByID(R.id.loginButton))).click();

        notTimedOut =
                mDevice.wait(Until.hasObject(By.res(getResByID(R.id.fragment_login_layout))),
                        APP_TIMEOUT_MS);
        assertTrue("Login screen timed out while loading!", notTimedOut);
        UiObject2 uname  = mDevice.findObject(By.res(getResByID(R.id.usernameEditText)));
        uname.click();
        uname.setText("bar1000");
        UiObject2 pword = mDevice.findObject(By.res(getResByID(R.id.passwordEditText)));
        pword.click();
        pword.setText("hello123");
        //hide the soft keyboard.
        mDevice.pressBack();
        mDevice.findObject(By.res(getResByID(R.id.loginButton))).click();
    }
    protected boolean doLogout(){
        boolean success = false;
        if(mDevice.hasObject(By.res(getResByID(R.id.fragment_welcome_layout))) &&
                mDevice.hasObject(By.res(getResByID(R.id.loginButton)))) {
            return true;
        } else {
            success = mDevice.wait(Until.hasObject(By.desc(OVERFLOW_DESCRIPTION)), APP_TIMEOUT_MS);
            if (!success){
                return success;
            }
        }
        success = false;
        UiObject2 options = mDevice.findObject(By.desc(OVERFLOW_DESCRIPTION));
        if (options != null) {
            options.click();
            if(!mDevice.wait(Until.hasObject(
                    By.text(getStrByID(R.string.action_request_game))), APP_TIMEOUT_MS)) {
                return false;
            }
            UiObject2 signOut = mDevice.findObject(By.text(getStrByID(R.string.action_signout)));
            if (signOut != null) {
                signOut.click();
                success = mDevice.wait(Until.hasObject(
                        By.res(getResByID(R.id.activity_mdrecord_layout))), APP_TIMEOUT_MS);
                assertTrue("Logout redirect failed!", success);
                //click on profile
                mDevice.findObject(By.text(getStrByID(R.string.kamcordProfileTab))).click();
                //find sign in
                success = mDevice.wait(Until.hasObject(
                        By.res(getResByID(R.id.signInPromptButton))), APP_TIMEOUT_MS);
                assertTrue("Sign in button not found on profile! Already logged in?", success);
                //click sign in.
                mDevice.findObject(By.res(getResByID(R.id.signInPromptButton))).click();
                //find welcome screen.
                success = mDevice.wait(Until.hasObject(
                        By.res(getResByID(R.id.fragment_welcome_layout))), APP_TIMEOUT_MS);
                assertTrue("Welcome screen did not load!", success);

            }
        }
        return success;
    }
    /**
     * Uses package manager to find the package name of the device launcher. Usually this package
     * is "com.android.launcher" but can be different at times. This is a generic solution which
     * works on all platforms.`
     */
    protected String getLauncherPackageName() {
        // Create launcher Intent
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        // Use PackageManager to get the launcher package name
        PackageManager pm = InstrumentationRegistry.getContext().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.activityInfo.packageName;
    }

     /*
    protected void closeAllApps(){

        try {
            mDevice.pressHome();

            mDevice.pressRecentApps();
            boolean notTimedOut = mDevice
                    .wait(Until.hasObject(By.res(ANDROID_DISMISS_TASK)),
                            2000);

            UiObject2 recentItem = mDevice.findObject(By.res(ANDROID_DISMISS_TASK));
            while (recentItem != null) {
                recentItem.click();
                recentItem = mDevice.findObject(By.res(ANDROID_DISMISS_TASK));
            }
            mDevice.pressHome();

        } catch (RemoteException e){
            e.printStackTrace();
        }

    }
    */
}
