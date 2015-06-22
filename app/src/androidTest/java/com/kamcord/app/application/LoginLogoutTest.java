package com.kamcord.app.application;

import com.kamcord.app.testutils.interfaces.BasicTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Created by Mehmet on 5/25/15.
 */
public class LoginLogoutTest extends TestBase {

    @Test
    @Category(BasicTest.class)
    public void testLogin() {
        assertFalse("Already logged in!", isLoggedIn());
        doLogin();
        assertTrue("Login failed!", isLoggedIn());
    }

    @Test
    @Category(BasicTest.class)
    public void testLogout() {
        assertFalse("Already logged in!", isLoggedIn());
        doLogin();
        assertTrue("Login failed!", isLoggedIn());
        doLogout();
        assertFalse("Still logged in!", isLoggedIn());
    }
}
