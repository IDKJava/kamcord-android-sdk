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
public class LoginLogoutTest extends BaseTest{

    @Test
    public void testLogin() {
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
}
