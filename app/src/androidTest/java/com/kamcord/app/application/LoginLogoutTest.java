package com.kamcord.app.application;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import com.kamcord.app.R;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


/**
 * Created by Mehmet on 5/25/15.
 */
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 21)
public class LoginLogoutTest {
    private static final int TIME_OUT_MS = 5000;

    private static final String OVERFLOW_DESCRIPTION = "More options";
    private static final String KAMCORD_APP_PACKAGE = "com.kamcord.app";
    private static final String RIPPLE_TEST_APP_NAME = "Ripple Test";
    private static final String RIPPLE_TEST_MAIN_RES = "com.kamcord.ripples:id/mainlayout";
    private static final String ANDROID_DISMISS_TASK = "com.android.systemui:id/dismiss_task";

    private UiDevice mDevice;



    @Before
    public void startAppFromHomeScreen(){

        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        //closeAllApps();

        mDevice.pressHome();

        final String launcherPackage = this.getLauncherPackageName();

        assertThat(launcherPackage, notNullValue());

        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), TIME_OUT_MS);

        Context context = InstrumentationRegistry.getContext();

        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(KAMCORD_APP_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        boolean notTimedOut = mDevice.wait(Until.hasObject(By.pkg(KAMCORD_APP_PACKAGE).depth(0)), TIME_OUT_MS);
        assertTrue("Application load timed out!", notTimedOut);

        mDevice.waitForIdle(TIME_OUT_MS);
        doLogout();
    }
    private String getResByID(int resourceId) {
        Context currentContext = InstrumentationRegistry.getTargetContext();
        String prefix = currentContext.getPackageName();
        String id = currentContext.getResources().getResourceEntryName(resourceId);

        return String.format("%s:id/%s",prefix,id);
    }
    private String getStrByID(int resourceId) {
        Context currentContext = InstrumentationRegistry.getTargetContext();
        String s = currentContext.getString(resourceId);
        return s;
    }
    private boolean isLoggedIn(){
        UiObject2 loginActivity = mDevice
                .findObject(By.res(getResByID(R.id.fragment_welcome)));
        if (loginActivity != null){
            UiObject2 loginButton = mDevice.findObject(By.res(getResByID(R.id.loginButton)));
            if (loginButton != null)
                return false;
        } else {
            UiObject2 profileTab = mDevice.findObject(By.text(getStrByID(R.string.kamcordProfileTab)));
            if (profileTab != null) {
                profileTab.click();
                boolean result =
                        mDevice.wait(Until.hasObject(By.res(getResByID(R.id.signInPromptButton))), 1000);
                return !result;
            }
        }
        return false;

    }
    private boolean doLogin(){
        boolean success = false;
        // only works from login screen.
        // need to build a state machine to make this work for all states.
        if(mDevice.hasObject(By.res(getResByID(R.id.fragment_welcome)))){
            UiObject2 loginButton = mDevice.findObject(By.res(getResByID(R.id.loginButton)));
            loginButton.click();

            mDevice.wait(Until.hasObject(By.res(getResByID(R.id.fragment_login))), TIME_OUT_MS);
            UiObject2 uname  = mDevice.findObject(By.res(getResByID(R.id.usernameEditText)));
            uname.click();
            uname.setText("bar1000");
            UiObject2 pword = mDevice.findObject(By.res(getResByID(R.id.passwordEditText)));
            pword.click();
            pword.setText("hello123");
            //hide the soft keyboard.
            mDevice.pressBack();
            loginButton = mDevice.findObject(By.res(getResByID(R.id.loginButton)));
            loginButton.click();
            //String s = getResByID(R.id.activity_mdrecord);
            success = mDevice.wait(Until.hasObject(By.res(getResByID(R.id.activity_mdrecord))), 10000);
        }

        return success;
    }
    private boolean doLogout(){
        boolean success = false;
        if(mDevice.hasObject(By.res(getResByID(R.id.fragment_welcome))) &&
                mDevice.hasObject(By.res(getResByID(R.id.loginButton)))) {
            return true;
        } else {
            success = mDevice.wait(Until.hasObject(By.desc(OVERFLOW_DESCRIPTION)), TIME_OUT_MS);
            if (!success){
                return success;
            }
        }
        success = false;
        UiObject2 options = mDevice.findObject(By.desc(OVERFLOW_DESCRIPTION));
        if (options != null) {
            options.click();
            if(!mDevice.wait(Until.hasObject(
                    By.text(getStrByID(R.string.action_request_game))), TIME_OUT_MS)) {
                return false;
            }
            UiObject2 signOut = mDevice.findObject(By.text(getStrByID(R.string.action_signout)));
            if (signOut != null) {
                signOut.click();
                success = mDevice.wait(Until.hasObject(
                        By.res(getResByID(R.id.fragment_welcome))), TIME_OUT_MS);
            }
        }
        return success;
    }
    //@Test
    public void testLogin() {
        assertFalse("Already logged in!", isLoggedIn());
        assertTrue("Login timed out or wrong password!", doLogin());
        assertTrue("Login failed!", isLoggedIn());
    }
    //@Test
    public void testLogout() {
        assertFalse("Already logged in!", isLoggedIn());
        assertTrue("Login timed out or wrong password!", doLogin());
        assertTrue("Login failed!", isLoggedIn());
        assertTrue("Logout timed out!", doLogout());
        assertFalse("Still logged in!", isLoggedIn());
    }
    @Test
    public void recordRippleTest(){
        doLogin();

        //find ripples app logo and click
        mDevice.findObject(By.text(RIPPLE_TEST_APP_NAME)).click();

        mDevice.findObject(By.res(getResByID(R.id.record_button))).click();

        //Ack the screen recording warning.
        boolean notTimedOut = mDevice
                .wait(Until.hasObject(By.res(getResByID(android.R.id.button1))), TIME_OUT_MS);
        //assertTrue("Recording notification timed out!", notTimedOut);
        String s = getResByID(android.R.id.button1);
        mDevice.findObject(By.res(getResByID(android.R.id.button1))).click();
        //wait for ripples to show up.

        notTimedOut = mDevice
                .wait(Until.hasObject(By.res(RIPPLE_TEST_MAIN_RES)), TIME_OUT_MS);
        assertTrue("Ripple test launch timed out!", notTimedOut);
        //Record for some time?
        try {
            Thread.sleep(TIME_OUT_MS);
        } catch (InterruptedException e){
            e.printStackTrace();
            assertFalse("Test interrupted", true);
        }

        //get running task list
        try {
            mDevice.pressRecentApps();
        } catch (RemoteException e) {
            e.printStackTrace();
            assertFalse("Press recent apps failed!", true);
        }
        notTimedOut = mDevice
                .wait(Until.hasObject(By.text(getStrByID(R.string.toolbarTitle))), TIME_OUT_MS);
        assertTrue("Kamcord app not found in recent apps!", notTimedOut);
        //Bring up Kamcord
        mDevice.findObject(By.text(getStrByID(R.string.toolbarTitle))).click();

        //find stop recording button.
        notTimedOut = mDevice
                .wait(Until.hasObject(By.res(getResByID(R.id.record_button))), TIME_OUT_MS);
        assertTrue("Stop recording button timed out!", notTimedOut);
        mDevice.findObject(By.res(getResByID(R.id.record_button))).click();
        //wait for video processing to finish
        notTimedOut = mDevice
                .wait(Until.hasObject(By.res(getResByID(R.id.playImageView))),
                        2 * TIME_OUT_MS);
        assertTrue("Video processing timed out!", notTimedOut);
        mDevice.findObject(By.res(getResByID(R.id.titleEditText))).click();
        mDevice.findObject(By.res(getResByID(R.id.titleEditText)))
                .setText("my awesome ripple test video");
        //close soft keyboard
        mDevice.pressBack();

        mDevice.findObject(By.res(getResByID(R.id.descriptionEditText))).click();
        mDevice.findObject(By.res(getResByID(R.id.descriptionEditText)))
                .setText("The quick brown fox jumps over the lazy dog.");
        //close soft keyboard
        mDevice.pressBack();

        mDevice.findObject(By.res(getResByID(R.id.shareButton))).click();

        notTimedOut = mDevice
                .wait(Until.hasObject(By.text(getStrByID(R.string.kamcordRecordTab))),
                        2 * TIME_OUT_MS);
        assertTrue("Video upload timed out!", notTimedOut);

    }
    /*
    public void closeAllApps(){

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
