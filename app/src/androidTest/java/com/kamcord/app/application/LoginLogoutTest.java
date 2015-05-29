package com.kamcord.app.application;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Created by Mehmet on 5/25/15.
 */
public class LoginLogoutTest extends TestBase {

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
