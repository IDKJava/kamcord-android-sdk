package com.kamcord.app.application;

import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;

import com.kamcord.app.R;

import org.junit.Test;

import android.graphics.Point;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mehmet on 5/27/15.
 */
public class GameListTest extends TestBase {

    @Test
    public void checkIfGamesListed() throws UiObjectNotFoundException{
        //TODO: Check for content not just count.
        doLogin();
        boolean notTimedOut = mDevice.wait(
                Until.hasObject(By.text(getStrByID(R.string.kamcordRecordTab))), APP_TIMEOUT_MS);
        assertTrue("Record tab failed to load!", notTimedOut);
        mDevice.findObject(By.text(getStrByID(R.string.kamcordRecordTab))).click();
        notTimedOut = mDevice.wait(
                Until.hasObject(By.res(getResByID(R.id.recordfragment_refreshlayout))),
                UI_TIMEOUT_MS);
        assertTrue("Games list failed to load!", notTimedOut);
        ArrayList<String> gameTitles = new ArrayList<>();
        UiObject2 gameTiles
                = mDevice.findObject(By.res(getResByID(R.id.recordfragment_refreshlayout)));
        notTimedOut = waitForGameTileLoad(gameTiles, APP_TIMEOUT_MS);
        assertTrue("Games list content failed to load!", notTimedOut);
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
    public void checkIfGamesUpdate(){
        //TODO: Check for content not just count.
        doLogin();
        boolean notTimedOut = mDevice.wait(
                Until.hasObject(By.text(getStrByID(R.string.kamcordRecordTab))), UI_TIMEOUT_MS);
        assertTrue("Record tab failed to load!", notTimedOut);
        mDevice.findObject(By.text(getStrByID(R.string.kamcordRecordTab))).click();
        notTimedOut = mDevice.wait(
                Until.hasObject(By.res(getResByID(R.id.recordfragment_refreshlayout))),
                UI_TIMEOUT_MS);
        assertTrue("Games list failed to load!", notTimedOut);
        mDevice.swipe(validateSwipe(new Point[]{new Point(380, 500), new Point(380, 1150)}), 40);
        boolean spinnerVisible =
                checkIfGameTilesUpdating(
                        mDevice.findObject(By.res(getResByID(R.id.recordfragment_refreshlayout))));
        assertTrue("Refresh indicator failed to show!", spinnerVisible);

        sleep(UI_TIMEOUT_MS);
        mDevice.waitForIdle();
        spinnerVisible =
                checkIfGameTilesUpdating(
                        mDevice.findObject(By.res(getResByID(R.id.recordfragment_refreshlayout))));
        assertFalse("Refresh indicator failed to disappear!", spinnerVisible);

    }

    protected boolean checkIfGameTilesUpdating(UiObject2 gameTiles){
        mDevice.waitForIdle();
        for (UiObject2 child : gameTiles.getChildren()) {
            if (child.getClassName().equals(android.widget.ImageView.class.getName())) {
                return true;
            }
        }
        return false;
    }

    protected boolean waitForGameTileLoad(UiObject2 gameTiles, int timeOut){

        boolean gone = false;
        int step = 100;
        int maxRetries = Math.max(1,timeOut / step);
        int retries = 0;
        while(!gone && retries < maxRetries){
            gone = true;
            mDevice.waitForIdle();
            for (UiObject2 child : gameTiles.getChildren()) {
                if (child.getClassName().equals(android.widget.ImageView.class.getName())) {
                    gone = !child.isFocused();
                }
            }
            retries++;
            sleep(step);
        }
        return gone;
    }

}
