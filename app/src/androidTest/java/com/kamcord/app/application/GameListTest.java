package com.kamcord.app.application;

import android.graphics.Point;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.test.suitebuilder.annotation.SmallTest;
import android.test.suitebuilder.annotation.Smoke;

import com.kamcord.app.R;

import org.junit.Test;

import java.util.ArrayList;

import static com.kamcord.app.testutils.UiUtilities.*;
import static org.junit.Assert.assertTrue;

/**
 * Created by Mehmet on 5/27/15.
 */

public class GameListTest extends RecordAndPostTestBase {

    @Test
    public void checkGamesInstalledList() throws UiObjectNotFoundException{
        //TODO: Check for content not just count.
        doLogin();

        findUiObj(R.string.kamcordRecordTab, UiObjIdType.Str, UiObjSelType.Des, APP_TIMEOUT_MS)
                .click();

        ArrayList<String> gameTitles = new ArrayList<>();
        mDevice.swipe(validateSwipe(new Point[]{new Point(380, 400), new Point(380, 1150)}), 40);
        waitForTileLoad(R.id.recordfragment_refreshlayout, APP_TIMEOUT_MS);

        boolean unique = true;
        while(unique) {
            unique = false;
            mDevice.waitForIdle();
            for (UiObject2 button : mDevice.findObjects(
                    By.res(getResByID(R.id.gameActionImageButton)))) {
                String buttonDesc = button.getContentDescription();
                UiObject2 gameTitle = findUiObjInObj(button.getParent(),
                        R.id.gameNameTextView,
                        UiObjIdType.Res,
                        UiObjSelType.Res,
                        UI_TIMEOUT_MS, false);
                if (gameTitle != null && buttonDesc != null) {
                    String title = gameTitle.getText();
                    if (!gameTitles.contains(title) &&
                            buttonDesc.equals(getStrByID(R.string.idle))) {
                        gameTitles.add(title);
                        unique = true;
                    }
                }
            }

            mDevice.swipe(validateSwipe(new Point[]{new Point(380, 1760), new Point(380, 150)}), 40);
        }

        mDevice.swipe(validateSwipe(new Point[]{new Point(380, 400), new Point(380, 1150)}), 40);
        assertTrue("Has no games listed!", gameTitles.size() > 2);
    }

    @Test
    public void checkGameList() throws UiObjectNotFoundException{
        //TODO: Check for content not just count.
        doLogin();

        findUiObj(R.string.kamcordRecordTab, UiObjIdType.Str, UiObjSelType.Des, APP_TIMEOUT_MS)
                .click();

        ArrayList<String> gameTitles = new ArrayList<>();

        waitForTileLoad(R.id.recordfragment_refreshlayout, APP_TIMEOUT_MS);

        boolean unique = true;
        while(unique) {
            unique = false;
            mDevice.waitForIdle();
            for (UiObject2 gameTitle : mDevice.findObjects(By.res(getResByID(R.id.gameNameTextView)))) {
                String title = gameTitle.getText();
                if(!gameTitles.contains(title)){
                    gameTitles.add(title);
                    unique = true;
                }
            }

            mDevice.swipe(validateSwipe(new Point[]{new Point(380, 1760), new Point(380, 150)}), 40);
        }

        mDevice.swipe(validateSwipe(new Point[]{new Point(380, 400), new Point(380, 1150)}), 40);
        assertTrue("Has no games listed!", gameTitles.size() >= 1);
    }


    @Test
    public void checkIfGamesUpdate() {
        //TODO: Check for content not just count.
        doLogin();

            findUiObj(R.string.kamcordRecordTab, UiObjIdType.Str, UiObjSelType.Des).click();

            //main container for games tiles
            waitForTileLoad(R.id.recordfragment_refreshlayout, APP_TIMEOUT_MS);


            //scrollable child.
        try {
            UiScrollable gameTiles
                    = new UiScrollable(new UiSelector()
                    .resourceId(getResByID(R.id.record_recyclerview)));
            assertTrue("Not scrollable!", gameTiles.isScrollable());

            //larger number for max swipes.
            gameTiles.flingToBeginning(100);

            waitForTileLoad(R.id.recordfragment_refreshlayout, APP_TIMEOUT_MS);

            //if we have reached the end and the toolbar should not be showing!
            // a short scroll up will make it appear.
            //mDevice.swipe(validateSwipe(new Point[]{new Point(380, 400), new Point(380, 425)}), 40);
            gameTiles.scrollBackward(1);
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
            assertTrue("Object not found!", false);
        }

    }

}
