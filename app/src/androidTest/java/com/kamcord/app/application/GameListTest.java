package com.kamcord.app.application;

import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;

import com.kamcord.app.R;

import org.junit.Test;

import android.graphics.Point;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

/**
 * Created by Mehmet on 5/27/15.
 */
public class GameListTest extends TestBase {

    @Test
    public void CheckIfGamesListed(){
        //TODO: Check for content not just count.
        doLogin();
        mDevice.findObject(By.text(getStrByID(R.string.kamcordRecordTab))).click();
        boolean notTimedOut = mDevice.wait(
                Until.hasObject(By.res(getResByID(R.id.record_recyclerview))), UI_TIMEOUT_MS);
        assertTrue("Games list failed to load!", notTimedOut);
        boolean unique = true;
        ArrayList<String> gameTitles = new ArrayList<>();
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
            mDevice.swipe(new Point[]{new Point(380, 1760), new Point(380, 150)}, 40);
        }
        mDevice.swipe(new Point[]{new Point(380, 400), new Point(380, 1150)}, 40);
        assertTrue("Has no games listed!", gameTitles.size() > 2);
    }



}
