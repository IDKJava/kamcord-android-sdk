package com.kamcord.app.application;

import android.graphics.Point;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;

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
    public void checkGamesToInstallList() throws UiObjectNotFoundException{
        //TODO: Check for content not just count.
        doLogin();
        mDevice.findObject(By.text(getStrByID(R.string.kamcordRecordTab))).click();

        findUiObj(R.string.kamcordRecordTab, UiObjIdType.Str, UiObjSelType.Txt, APP_TIMEOUT_MS)
                .click();

        ArrayList<String> gameTitles = new ArrayList<>();

        waitForGameTileLoad(R.id.recordfragment_refreshlayout, APP_TIMEOUT_MS);

        boolean unique = true;
        while(unique) {
            unique = false;
            mDevice.waitForIdle();
            for (UiObject2 gameTitle : mDevice.findObjects(By.res(getResByID(R.id.item_packagename)))) {
                String title = gameTitle.getText();
                if(!gameTitles.contains(title)){
                    gameTitles.add(title);
                    unique = true;
                }
            }

            mDevice.swipe(validateSwipe(new Point[]{new Point(380, 1760), new Point(380, 150)}), 40);
        }

        mDevice.swipe(validateSwipe(new Point[]{new Point(380, 400), new Point(380, 1150)}), 40);
        assertTrue("Has no games listed!", gameTitles.size() > 2);
    }

    @Test
    public void checkGamesInstalledList() throws UiObjectNotFoundException{
        //TODO: Check for content not just count.
        doLogin();
        mDevice.findObject(By.text(getStrByID(R.string.kamcordRecordTab))).click();

        findUiObj(R.string.kamcordRecordTab, UiObjIdType.Str, UiObjSelType.Txt, APP_TIMEOUT_MS)
                .click();

        ArrayList<String> gameTitles = new ArrayList<>();

        waitForGameTileLoad(R.id.recordfragment_refreshlayout, APP_TIMEOUT_MS);

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
        try {
            mDevice.findObject(By.text(getStrByID(R.string.kamcordRecordTab))).click();

            //main container for games tiles
            waitForGameTileLoad(R.id.recordfragment_refreshlayout, APP_TIMEOUT_MS);


            //scrollable child.
            UiScrollable gameTiles
                    = new UiScrollable(new UiSelector()
                    .resourceId(getResByID(R.id.record_recyclerview)));
            assertTrue("Not scrollable!", gameTiles.isScrollable());

            //larger number for max swipes.
            gameTiles.flingToBeginning(100);

            waitForGameTileLoad(R.id.recordfragment_refreshlayout, APP_TIMEOUT_MS);

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
