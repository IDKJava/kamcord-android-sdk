package com.kamcord.app.application;

import android.graphics.Point;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.test.suitebuilder.annotation.SmallTest;

import com.kamcord.app.R;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static com.kamcord.app.testutils.UiUtilities.*;
import static com.kamcord.app.testutils.SystemUtilities.*;
/**
 * Created by Mehmet on 6/3/15.
 */
public class ProfileTest extends TestBase {


    @Test
    public void testProfileLoadLoggedIn()
    {
        doLogin();
        findUiObj(R.string.kamcordProfileTab, UiObjIdType.Str, UiObjSelType.Des).click();
        //TODO: Add test member method call
        verifyProfileUserInfo();
    }

    @Test
    public void testProfileLoadLoggedOut()
    {
        skipLogin();
        findUiObj(R.string.kamcordProfileTab, UiObjIdType.Str, UiObjSelType.Des).click();
        findUiObj(R.id.signInPromptButton, UiObjIdType.Res, UiObjSelType.Res).click();
        handleWelcomeLoginView();
        findUiObj(R.string.kamcordProfileTab, UiObjIdType.Str, UiObjSelType.Des).click();
        //TODO: Add test member method call
        verifyProfileUserInfo();
    }
    @Test
    public void testProfileVideoLike(){
        //TODO: find a way to cover more than just video #1
        doLogin();
        findUiObj(R.string.kamcordProfileTab, UiObjIdType.Str, UiObjSelType.Des).click();
        waitForTileLoad(R.id.profile_recyclerview, APP_TIMEOUT_MS);

        UiObject2 likeObj =
                findUiObj(R.id.profile_video_likes_button, UiObjIdType.Res, UiObjSelType.Res);
        int numLikes;
        try {
            numLikes = Integer.parseInt(likeObj.getText());
        } catch (Exception e) {
            e.printStackTrace();
            numLikes = 0;
        }
        likeObj.click();
        scrollToBeginning(R.id.profile_recyclerview);
        waitForTileLoad(R.id.profile_recyclerview, APP_TIMEOUT_MS);
        likeObj =
                findUiObj(R.id.profile_video_likes_button, UiObjIdType.Res, UiObjSelType.Res);
        int numLikesAfter;
        try {
            numLikesAfter = Integer.parseInt(likeObj.getText());
        } catch (Exception e) {
            e.printStackTrace();
            numLikesAfter = Integer.MAX_VALUE;
        }
        assertTrue("Like didn't work!", Math.abs(numLikes - numLikesAfter) == 1);
    }
    @Test
    public void testProfileVideoView(){
        //TODO: find a way to cover more than just video #1
        doLogin();
        findUiObj(R.string.kamcordProfileTab, UiObjIdType.Str, UiObjSelType.Des).click();
        scrollToBeginning(R.id.profile_recyclerview);
        waitForTileLoad(R.id.profile_recyclerview, APP_TIMEOUT_MS);

        UiObject2 viewObj =
                findUiObj(R.id.video_views, UiObjIdType.Res, UiObjSelType.Res);
        int numViews;
        try {
            numViews = Integer.parseInt(viewObj.getText());
        } catch (Exception e){
            e.printStackTrace();
            numViews = 0;
        }
        //find parent and then the sibling thumbnail! Click that
        UiObject2 videoContainer = viewObj.getParent().getParent();
        findUiObjInObj(videoContainer, R.id.profile_item_thumbnail,
                UiObjIdType.Res, UiObjSelType.Res, UI_TIMEOUT_MS).click();
        findUiObj(R.id.profile_videoview, UiObjIdType.Res, UiObjSelType.Res, APP_TIMEOUT_MS);

        mDevice.pressBack();
        scrollToBeginning(R.id.profile_recyclerview);
        waitForTileLoad(R.id.profile_recyclerview, APP_TIMEOUT_MS);
        viewObj =
                findUiObj(R.id.video_views, UiObjIdType.Res, UiObjSelType.Res);
        int numViewsAfter;
        try {
            numViewsAfter = Integer.parseInt(viewObj.getText());
        } catch (Exception e){
            e.printStackTrace();
            numViewsAfter = Integer.MAX_VALUE;
        }
        assertTrue("View counter update did not work!", Math.abs(numViews - numViewsAfter) == 1);


    }
    //TODO: enable after AA-36 is fixed.
    //@Test
    public void verifyProfileOffline(){
        //Addresses AA-36
        doLogin();
        findUiObj(R.string.kamcordProfileTab, UiObjIdType.Str, UiObjSelType.Des, UI_TIMEOUT_MS)
                .click();
        verifyProfileUserInfo();
        toggleNetwork(false);
        mDevice.pressHome();

        startKamcordApp();
        findUiObj(R.string.kamcordProfileTab, UiObjIdType.Str, UiObjSelType.Des, UI_TIMEOUT_MS)
                .click();
        verifyProfileUserInfo();


    }
    //@Test
    public void refreshProfileTest() {
        //Addresses AA-72
        doLogin();
        int numRefreshes = 10000;
        findUiObj(R.string.kamcordProfileTab, UiObjIdType.Str, UiObjSelType.Des, UI_TIMEOUT_MS)
                .click();
        Point[] pattern = new Point[]{
                new Point(500, 500),
                new Point(500, 800)};
        sleep(1000);
        for (int i = 0; i < numRefreshes; i++) {

            executeTouchPattern(pattern, 2);
            sleep(1);

        }
        sleep(3000);
    }
    protected void verifyProfileUserInfo() {
        //TODO: Add more features to test.

        //verify video tile load
        //scrollable child.
        scrollToBeginning(R.id.profile_recyclerview);

        waitForTileLoad(R.id.profile_recyclerview, APP_TIMEOUT_MS);
        //check username
        String userNameString = findUiObj(R.id.profile_user_name, UiObjIdType.Res, UiObjSelType.Res)
                .getText()
                .toLowerCase();
        assertTrue("Wrong user name!", userNameString.equals(USERNAME1));

        //check profile letter
        String profileLetter = findUiObj(R.id.profileLetter, UiObjIdType.Res, UiObjSelType.Res)
                .getText()
                .toLowerCase();
        assertTrue("Wrong profile letter!", userNameString.charAt(0) == profileLetter.charAt(0));

    }
}
