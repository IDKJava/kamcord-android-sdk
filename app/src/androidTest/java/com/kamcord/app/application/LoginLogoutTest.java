package com.kamcord.app.application;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Created by Mehmet on 5/25/15.
 */
public class LoginLogoutTest extends TestBase {

    @Test
    /**
     * Tests log in to the app.
     * <p>
     *     <b>Test Sequence:</b><br>
     *     1) {@link TestBase#doLogin  Log in}<br>
     *     2) Expect to be taken to the recording tab.<br>
     * </p>
     */
    public void testLogin() {
        assertFalse("Already logged in!", isLoggedIn());
        doLogin();
        assertTrue("Login failed!", isLoggedIn());
    }

    @Test
    /**
     * Tests log in to the app, check if keyboard hides automatically.
     * <p>
     *     <b>Test Sequence:</b><br>
     *     1) {@link TestBase#doLogin  Log in}<br>
     *     2) Expect to be taken to the recording tab.<br>
     * </p>
     */
    public void testLoginAutoHideKeyboard() {
        assertFalse("Already logged in!", isLoggedIn());
        doLogin(true);
        assertTrue("Login failed!", isLoggedIn());
    }

    @Test
    /**
     * Tests log out from the app.
     * <p>
     *     <b>Test Sequence:</b><br>
     *     1) {@link TestBase#doLogin Log in}<br>
     *     2) {@link TestBase#doLogout Log out}<br>
     *     3) Expect to be taken to the welcome page.<br>
     * <p>
     */
    public void testLogout() {
        assertFalse("Already logged in!", isLoggedIn());
        doLogin();
        assertTrue("Login failed!", isLoggedIn());
        doLogout();
        assertFalse("Still logged in!", isLoggedIn());
    }
}
