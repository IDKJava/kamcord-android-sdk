package com.kamcord.app.application;

import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;


import com.kamcord.app.R;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static com.kamcord.app.testutils.UiUtilities.*;
import static com.kamcord.app.testutils.SystemUtilities.*;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.kamcord.app.testutils.testrules.FailureRule;
import com.kamcord.app.testutils.testrules.RetryRule;


/**
 * Created by Mehmet on 5/27/15.
 */
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 21)
public abstract class TestBase {

    @Rule
    public RetryRule mTestRetryRule = new RetryRule(5);
    @Rule
    public FailureRule mTestFailureRule = new FailureRule();

    @Before
    public void setUp(){
        //clearSharedPreferences();
        setOrientationNatural();
        toggleNetwork(true);
        startKamcordApp();
        doLogout();
    }

    @After
    public void cleanUp(){
        //do nothing here setUp makes sure test is ready to go.
        //stopService(com.kamcord.app.service.RecordingService.class);
        //stopService(com.kamcord.app.service.UploadService.class);
    }
    @AfterClass
    public static void cleanUpClass(){
        stopApp(RIPPLE_TEST_APP_PACKAGE);
    }

    protected void startKamcordApp(){
        mDevice.pressHome();

        startApplication(KAMCORD_APP_PACKAGE);

        mDevice.waitForIdle(APP_TIMEOUT_MS);
    }

    protected boolean isLoggedIn(){
        UiObject2 loginActivity = mDevice
                .findObject(By.res(getResByID(R.id.fragment_welcome_layout)));
        if (loginActivity != null){
            UiObject2 loginButton = mDevice.findObject(By.res(getResByID(R.id.loginButton)));
            if (loginButton != null)
                return false;
        } else {
            UiObject2 profileTab = mDevice.findObject(By.desc(getStrByID(R.string.kamcordProfileTab)));
            if (profileTab != null) {
                profileTab.click();
                boolean result =
                        mDevice.wait(Until.hasObject(By.res(getResByID(R.id.signInPromptButton))), UI_TIMEOUT_MS);
                return !result;
            }
        }
        return false;

    }

    protected void doLogin() {
        // only works from login screen.
        handleWelcomeLoginView();
        //Welcome screen of sorts
        findUiObj(R.id.activity_mdrecord_layout, UiObjIdType.Res, UiObjSelType.Res,APP_TIMEOUT_MS);
    }

    protected void handleWelcomeLoginView(){
        findUiObj(R.id.loginButton, UiObjIdType.Res, UiObjSelType.Res).click();

        //did the view load?
        findUiObj(R.id.fragment_login_layout, UiObjIdType.Res,
                UiObjSelType.Res, APP_TIMEOUT_MS);
        //is username here
        UiObject2 uName  = findUiObj(R.id.usernameEditText,
                UiObjIdType.Res,
                UiObjSelType.Res);
        uName.click();
        uName.setText(USERNAME1);
        //is password here?
        UiObject2 pWord = findUiObj(R.id.passwordEditText,
                UiObjIdType.Res,
                UiObjSelType.Res);
        pWord.click();
        pWord.setText(PASSWORD1);
        //hide the soft keyboard.
        mDevice.pressBack();
        findUiObj(R.id.loginButton, UiObjIdType.Res, UiObjSelType.Res).click();
    }

    protected void doLogout(){
        if(mDevice.hasObject(By.res(getResByID(R.id.fragment_welcome_layout))) &&
                mDevice.hasObject(By.res(getResByID(R.id.loginButton)))) {
            return;
        } else {
            findUiObj(R.string.kamcordProfileTab, UiObjIdType.Str, UiObjSelType.Des, UI_TIMEOUT_MS)
                    .click();
            scrollToBeginning(R.id.profile_recyclerview);
            waitForTileLoad(R.id.profile_recyclerview, APP_TIMEOUT_MS);
            findUiObj(R.id.profile_action_menu, UiObjIdType.Res, UiObjSelType.Res, APP_TIMEOUT_MS)
                    .click();
            findUiObj(R.string.action_signout, UiObjIdType.Str, UiObjSelType.Txt).click();
            findUiObj(R.id.fragment_welcome_layout, UiObjIdType.Res, UiObjSelType.Res);
        }
    }

    protected void skipLogin(){
        //skip login
        //should see an active record tab.
        findUiObj(R.id.skipButton, UiObjIdType.Res, UiObjSelType.Res).click();
        findUiObj(R.id.activity_mdrecord_layout, UiObjIdType.Res, UiObjSelType.Res);
    }

    public void findGame(String gameName) {
        findGame(gameName, true);
    }
    public void findGame(String gameName, boolean installed) {
        int idToFind;
        if(installed){
            idToFind = R.id.gameNameTextView;
        } else {
            idToFind = R.id.item_packagename;
        }
        try {
            //TODO: Refactor to move the try catch block to utilities.
            ArrayList<String> gameTitles = new ArrayList<>();
            findUiObj(R.string.kamcordRecordTab, UiObjIdType.Str, UiObjSelType.Des, APP_TIMEOUT_MS)
                    .click();

            waitForTileLoad(R.id.recordfragment_refreshlayout, APP_TIMEOUT_MS);

            //scrollable child.
            UiScrollable gameTiles
                    = new UiScrollable(new UiSelector()
                    .resourceId(getResByID(R.id.record_recyclerview)));

            assertTrue("Not scrollable!", gameTiles.isScrollable());

            //larger number for max swipes.
            gameTiles.flingToBeginning(100);
            mDevice.waitForIdle(UI_TIMEOUT_MS);
            sleep(UI_TIMEOUT_MS);
            waitForTileLoad(R.id.recordfragment_refreshlayout, APP_TIMEOUT_MS);

            //Longer timeout due to reload
            findUiObj(R.id.recordfragment_refreshlayout,
                    UiObjIdType.Res,
                    UiObjSelType.Res,
                    APP_TIMEOUT_MS);


            boolean unique = true;
            boolean found = false;
            while (unique && !found) {
                unique = false;
                mDevice.waitForIdle();
                for (UiObject2 gameTitle :
                        mDevice.findObjects(By.res(getResByID(idToFind)))) {
                    String title = gameTitle.getText();
                    if (!gameTitles.contains(title)) {
                        gameTitles.add(title);
                        unique = true;
                    }
                    if (title.equals(gameName)) {
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    gameTiles.flingForward();
                }
            }
            if (!unique && !found) {
                //if we have reached the end and the toolbar should not be showing!
                // a short scroll up will make it appear.
                //mDevice.swipe(validateSwipe(new Point[]{new Point(380, 400), new Point(380, 425)}), 40);
                gameTiles.scrollBackward(1);
            }
            assertTrue("Game not found!", found);
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
            assertTrue("Object not found!", false);
        }
    }

    protected void waitForTileLoad(int gameTileParentId, int timeOut){

        boolean gone = false;
        int step = 1000;
        int maxRetries = Math.max(1,timeOut / step);
        int retries = 0;
        while(!gone && retries < maxRetries){
            gone = true;
            mDevice.waitForIdle(UI_INTERACTION_DELAY_MS);
            sleep(UI_INTERACTION_DELAY_MS);
            UiObject2 gameTiles = findUiObj(gameTileParentId, UiObjIdType.Res, UiObjSelType.Res);
            for (UiObject2 child : gameTiles.getChildren()) {
                if (child.getClassName().equals(android.widget.ImageView.class.getName())) {
                    gone = false;
                }
            }
            retries++;
            sleep(step);
        }
        assertTrue("Games list content failed to load!", gone);
    }

}
