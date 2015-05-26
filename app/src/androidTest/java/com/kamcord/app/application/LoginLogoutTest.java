package com.kamcord.app.application;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.EventCondition;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import android.util.Log;

import com.kamcord.app.R;


/**
 * Created by Mehmet on 5/25/15.
 */
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 21)
public class LoginLogoutTest {
    private static final int TIME_OUT_MS = 5000;

    static final String APP_NAME = "Kamcord";

    private UiDevice mDevice;

    private static final String KAMCORD_APP_PACKAGE = "com.kamcord.app";
    private static final String R_ID_LOGIN_BUTTON = "com.kamcord.app:id/loginButton";
    private static final String R_ID_SKIP_BUTTON = "com.kamcord.app:id/skipButton";
    private static final String R_ID_CREATE_PROFILE_BUTTON =
            "com.kamcord.app:id/createProfileButton";
    private static final String R_ID_ACTIVITY_LOGIN_LAYOUT =
            "com.kamcord.app:id/activity_login_layout";

    @Before
    public void startAppFromHomeScreen(){
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        mDevice.pressHome();

        final String launcherPackage = this.getLauncherPackageName();

        assertThat(launcherPackage, notNullValue());

        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), TIME_OUT_MS);

        Context context = InstrumentationRegistry.getContext();
        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(KAMCORD_APP_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        mDevice.wait(Until.hasObject(By.pkg(KAMCORD_APP_PACKAGE).depth(0)), TIME_OUT_MS);

        doLogout();
    }

    private boolean isLoggedIn(){
        UiObject2 loginActivity = mDevice.findObject(By.res(R_ID_ACTIVITY_LOGIN_LAYOUT));
        if (loginActivity != null){
            UiObject2 loginButton = mDevice.findObject(By.res(R_ID_LOGIN_BUTTON));
            if (loginButton != null)
                return false;
        } else {
            UiObject2 profileTab = mDevice.findObject(By.text("Profile"));
            if (profileTab != null) {
                profileTab.click();
                boolean result =
                        mDevice.wait(Until.hasObject(By.res("com.kamcord.app:id/signInPromptButton")), 1000);
                return !result;
            }
        }
        return false;

    }
    private boolean doLogin(){
        boolean success = false;
        // only works from login screen.
        // need to build a state machine to make this work for all states.
        if(mDevice.hasObject(By.res(R_ID_ACTIVITY_LOGIN_LAYOUT))){
            UiObject2 loginButton = mDevice.findObject(By.res(R_ID_LOGIN_BUTTON));
            loginButton.click();
            mDevice.wait(Until.hasObject(By.res("com.kamcord.app:id/forgotPasswordTextView")), TIME_OUT_MS);
            mDevice.findObject(By.res("com.kamcord.app:id/usernameEditText")).click();
            mDevice.findObject(By.res("com.kamcord.app:id/usernameEditText")).setText("bar1000");
            mDevice.findObject(By.res("com.kamcord.app:id/passwordEditText")).click();
            mDevice.findObject(By.res("com.kamcord.app:id/passwordEditText")).setText("hello123");
            mDevice.pressBack();
            loginButton = mDevice.findObject(By.res(R_ID_LOGIN_BUTTON));
            loginButton.click();
            success = mDevice.wait(Until.hasObject(By.res("com.kamcord.app:id/main_fab")), TIME_OUT_MS);
        }

        return success;
    }
    private boolean doLogout(){
        boolean success = false;
        if(mDevice.hasObject(By.res(R_ID_ACTIVITY_LOGIN_LAYOUT)) &&
                mDevice.hasObject(By.res(R_ID_LOGIN_BUTTON))) {
            return true;
        } else {
            success = mDevice.wait(Until.hasObject(By.desc("More options")), TIME_OUT_MS);
            if (!success){
                return success;
            }
        }
        success = false;
        UiObject2 options = mDevice.findObject(By.desc("More options"));
        if (options != null) {
            options.click();
            if(!mDevice.wait(Until.hasObject(By.text("Don't see your game?")), TIME_OUT_MS)) {
                return false;
            }
            UiObject2 signOut = mDevice.findObject(By.text("Sign Out"));
            if (signOut != null) {
                signOut.click();
                success = mDevice.wait(Until.hasObject(By.res(R_ID_ACTIVITY_LOGIN_LAYOUT)), TIME_OUT_MS);
            }
        }
        return success;
    }
    @Test
    public void testLogin(){
        assertFalse("Already logged in!", isLoggedIn());
        assertTrue("Login timed out or wrong password!", doLogin());
        assertTrue("Login failed!", isLoggedIn());
    }

    @Test
    public void testLogout() {
        assertFalse("Already logged in!", isLoggedIn());
        assertTrue("Login timed out or wrong password!", doLogin());
        assertTrue("Login failed!", isLoggedIn());
        assertTrue("Logout timed out!", doLogout());
        assertFalse("Still logged in!", isLoggedIn());

    }

    /**
     * Uses package manager to find the package name of the device launcher. Usually this package
     * is "com.android.launcher" but can be different at times. This is a generic solution which
     * works on all platforms.`
     */
    private String getLauncherPackageName() {
        // Create launcher Intent
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        // Use PackageManager to get the launcher package name
        PackageManager pm = InstrumentationRegistry.getContext().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.activityInfo.packageName;
    }

}
