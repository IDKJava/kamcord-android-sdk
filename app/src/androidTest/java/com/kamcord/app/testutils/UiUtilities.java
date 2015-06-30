package com.kamcord.app.testutils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;

import com.kamcord.app.R;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

/**
 * Created by Mehmet on 6/1/15.
 */
public class UiUtilities {

    //APP timeout is huge, but the game list won't
    // load on time if the delay is not large,
    public static final int APP_TIMEOUT_MS = 30000;
    public static final int UI_TIMEOUT_MS = 5000;
    public static final int RECORDING_DURATION_MS = 6000;
    public static final int DEFAULT_UPLOAD_TIMEOUT = 30000;
    public static final int MS_PER_MIN = 60000;
    public static final int DEFAULT_VIDEO_PROCESSING_TIMEOUT = 20000;
    public static final String OVERFLOW_DESCRIPTION = "More options";
    public static final String KAMCORD_APP_PACKAGE = "com.kamcord.app";
    public static final String RIPPLE_TEST_APP_PACKAGE = "com.kamcord.ripples";
    public static final String RIPPLE_TEST_APP_NAME = "Ripple Test";
    public static final String RIPPLE_TEST_APP_TITLE = "Ripple Demo";
    public static final String RIPPLE_TEST_MAIN_RES = "com.kamcord.ripples:id/mainlayout";
    public static final String ANDROID_DISMISS_TASK = "com.android.systemui:id/dismiss_task";
    public static final String ANDROID_SYSTEM_BUTTON1 = "android:id/button1";
    public static final String ANDROID_SYSTEM_BUTTON3 = "android:id/button3";
    public static final String ANDROID_LOCK_ICON = "com.android.systemui:id/lock_icon";
    public static final String ANDROID_SETTINGS_L_BUTTON = "com.android.settings:id/left_button";
    public static final String ANDROID_NOTIFICATION_HEADER = "com.android.systemui:id/header";
    public static final String ANDROID_APP_ICON  = "com.android.systemui:id/application_icon";
    public static final String ANDROID_SETTINGS_APP_RESID = "com.android.settings:id/name";
    public static final String ANDROID_RUNNING_TASK_LIST = "android:id/list";
    public static final int UI_INTERACTION_DELAY_MS = 1000;
    public static final int MAX_CLICK_TRIALS = 3;

    public static final String USERNAME1 = "bar1000";
    public static final String PASSWORD1 = "hello123";
    public static final UiDevice mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());



    public enum UiObjIdType {
        Res,
        Str
    }

    public enum UiObjSelType {
        Res,
        Txt,
        Des,
        TxtContains,
        DesContains
    }

    public static void sleep(int timeInMS) {
        try {
            Thread.sleep(timeInMS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue("Sleep interrupted!", false);
        }
    }

    public static UiObject2 findUiObj(int id, UiObjIdType idType, UiObjSelType selType) {
        return findUiObj(id, idType, selType, UI_TIMEOUT_MS);
    }
    public static UiObject2 findUiObj(int id,
                                      UiObjIdType idType,
                                      UiObjSelType selType,
                                      int timeOut) {
        return findUiObj(id, idType, selType, timeOut, true);
    }

    public static UiObject2 findUiObj(int id,
                                      UiObjIdType idType,
                                      UiObjSelType selType,
                                      int timeOut,
                                      boolean failIfNotFound) {
        String idString;
        switch (idType) {
            case Res:
                idString = getResByID(id);
                break;
            case Str:
                idString = getStrByID(id);
                break;
            default:
                throw new UnsupportedOperationException("Object Id type not supported!");
        }

        BySelector objSelector = getSelector(selType, idString);

        boolean notTimedOut = mDevice.wait(Until.hasObject(objSelector), timeOut);
        assertTrue("UI Object failed to load!", notTimedOut || !failIfNotFound);
        return mDevice.findObject(objSelector);
    }
    public static UiObject2 findUiObjInObj(UiObject2 parentObj,
                                           int id,
                                           UiObjIdType idType,
                                           UiObjSelType selType,
                                           int timeOut) {
        return findUiObjInObj(parentObj, id, idType, selType, timeOut, true);
    }
    public static UiObject2 findUiObjInObj(UiObject2 parentObj,
                                           int id,
                                           UiObjIdType idType,
                                           UiObjSelType selType,
                                           int timeOut,
                                           boolean failIfNotFound) {
        //TODO: Refactor this into the findUiObj
        String idString;
        switch (idType) {
            case Res:
                idString = getResByID(id);
                break;
            case Str:
                idString = getStrByID(id);
                break;
            default:
                throw new UnsupportedOperationException("Object Id type not supported!");
        }

        BySelector objSelector = getSelector(selType, idString);

        boolean notTimedOut = parentObj.wait(Until.hasObject(objSelector), timeOut);
        assertTrue("UI Object failed to load!", notTimedOut || !failIfNotFound);
        return parentObj.findObject(objSelector);
    }
    public static UiObject2 findUiObjInObj(UiObject2 parentObj,
                                           String idString,
                                           UiObjSelType selType,
                                           int timeOut)
    {
        return findUiObjInObj(parentObj, idString, selType, timeOut, true);
    }
    public static UiObject2 findUiObjInObj(UiObject2 parentObj,
                                           String idString,
                                           UiObjSelType selType,
                                           int timeOut,
                                           boolean failIfNotFound) {

        BySelector objSelector = getSelector(selType, idString);

        boolean notTimedOut = parentObj.wait(Until.hasObject(objSelector), timeOut);
        assertTrue("UI Object failed to load!", notTimedOut || !failIfNotFound);
        return parentObj.findObject(objSelector);
    }
    public static void loseUiObj(int id,
                                 UiObjIdType idType,
                                 UiObjSelType selType,
                                 int timeOut) {
        String idString;
        switch (idType) {
            case Res:
                idString = getResByID(id);
                break;
            case Str:
                idString = getStrByID(id);
                break;
            default:
                throw new UnsupportedOperationException("Object Id type not supported!");
        }

        BySelector objSelector = getSelector(selType, idString);

        boolean notTimedOut = mDevice.wait(Until.gone(objSelector), timeOut);
        assertTrue("UI Object failed to disappear!", notTimedOut);
    }

    public static UiObject2 findUiObj(String text, UiObjSelType selType) {
        return findUiObj(text, selType, UI_TIMEOUT_MS);
    }
    public static UiObject2 findUiObj(String text, UiObjSelType selType, int timeOut)
    {
        return findUiObj(text, selType, timeOut, true);
    }
    public static UiObject2 findUiObj(String text,
                                      UiObjSelType selType,
                                      int timeOut,
                                      boolean failIfNotFound) {
        BySelector objSelector = getSelector(selType, text);

        boolean notTimedOut = mDevice.wait(Until.hasObject(objSelector), timeOut);

        assertTrue("UI Object failed to load!", notTimedOut || !failIfNotFound);

        return mDevice.findObject(objSelector);
    }

    public static String getResByID(int resourceId) {
        Context currentContext = InstrumentationRegistry.getTargetContext();
        String prefix = currentContext.getPackageName();
        String id = currentContext.getResources().getResourceEntryName(resourceId);

        return String.format("%s:id/%s", prefix, id);
    }

    public static String getStrByID(int resourceId) {
        Context currentContext = InstrumentationRegistry.getTargetContext();
        String s = currentContext.getString(resourceId);
        return s;
    }

    /**
     * Uses package manager to find the package name of the device launcher. Usually this package
     * is "com.android.launcher" but can be different at times. This is a generic solution which
     * works on all platforms.`
     */
    public static String getLauncherPackageName() {
        // Create launcher Intent
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        // Use PackageManager to get the launcher package name
        PackageManager pm = InstrumentationRegistry.getContext().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.activityInfo.packageName;
    }

    public static Point[] validateSwipe(Point[] swipePoints) {
        //TODO: works fine except when the device is a tablet
        /*
        int bottomBar = 200;
        int topBar = 100;
        int height = mDevice.getDisplayHeight();
        int width = mDevice.getDisplayWidth();
        int orientation = mDevice.getDisplayRotation();
        String name = mDevice.getProductName();
        switch (orientation) {
            case Surface.ROTATION_90:
                //TODO: handle swipe by processing swapping coordinates and proportionallly
                for (Point p : swipePoints) {
                    int y = (int) (((float) p.x / width) * height);
                    int x = (int) (((float) p.y / height) * width);
                    x = Math.min(Math.max(topBar, x), width - bottomBar);
                    p.set(x, y);
                }
                break;
            case Surface.ROTATION_270:
                //TODO: handle swipe by processing swapping coordinates and proportionallly
                for (Point p : swipePoints) {
                    int y = (int) (((float) p.x / width) * height);
                    int x = (int) (((float) p.y / height) * width);
                    x = Math.min(Math.max(topBar, x), width - bottomBar);
                    //upside down!!!
                    x = width - x;
                    p.set(x, y);
                }
                break;

            case Surface.ROTATION_180:
            case Surface.ROTATION_0:
            default:
                break;
        }

        */
        return swipePoints;
    }



    public static void scrollToBeginning(int id){
        //safe delay? 25ms for now, may need more.
        scrollToBeginning(id, UI_INTERACTION_DELAY_MS);
    }
    public static void scrollToBeginning(int id, int sleepAfterMs){
        try {
            UiScrollable scrollableObject
                    = new UiScrollable(new UiSelector()
                    .resourceId(getResByID(id)));

            //assertTrue("Not scrollable!", scrollableObject.isScrollable());

            //adding more steps to avoid getting stuck with scrolling without refresh
            scrollableObject.scrollForward();
            scrollableObject.scrollToBeginning(100, 20);
            scrollableObject.scrollBackward();
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
            assertTrue("Object not found!", false);
        }
        sleep(sleepAfterMs);
    }
    public static void executeTouchPattern(Point[] pattern, int steps) {
        mDevice.swipe(validateSwipe(pattern), steps);
    }

    private static BySelector getSelector(UiObjSelType selType, String text){
        BySelector objSelector;
        switch (selType) {
            case Res:
                objSelector = By.res(text);
                break;
            case Txt:
                //TODO: Change to 'starts with' or 'contains'?? We may need to less restrictive with text!
                objSelector = By.text(text);
                break;
            case Des:
                objSelector = By.desc(text);
                break;
            case TxtContains:
                objSelector = By.textContains(text);
                break;
            case DesContains:
                objSelector = By.descContains(text);
                break;
            default:
                throw new UnsupportedOperationException("UI Selector type not supported!");
        }
        return objSelector;
    }

    public static void closeAppUI(String appPackageName){
        try {
            Context context = InstrumentationRegistry.getContext();
            Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            settingsIntent.setData(Uri.parse("package:" + appPackageName));
            context.startActivity(settingsIntent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            assertTrue("App could not be killed!", false);
        }
        UiObject2 obj =
                findUiObj(ANDROID_SETTINGS_L_BUTTON, UiObjSelType.Res, UI_TIMEOUT_MS);
        if(obj != null && obj.isClickable()) {
            obj.click();
         } else {
            return;
        }

        findUiObj(ANDROID_SYSTEM_BUTTON1, UiObjSelType.Res, UI_TIMEOUT_MS).click();

    }

    public static void stopServiceUI(String appPackageName){
        //it takes a max of two home presses to get home.
        mDevice.pressHome();
        mDevice.pressHome();

        try {
            Context context = InstrumentationRegistry.getContext();
            Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
            settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(settingsIntent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            assertTrue("App could not be killed!", false);
        }
        findUiObj("Running", UiObjSelType.Txt, UI_TIMEOUT_MS).click();

        //findUiObj("com.android.settings:id/title", UiObjSelType.Res, UI_TIMEOUT_MS);
        mDevice.waitForIdle();
        UiObject2 obj =  findAppItem(getStrByID(R.string.app_name));
        if(obj != null){
            obj.click();
            obj = findUiObj(ANDROID_SETTINGS_L_BUTTON, UiObjSelType.Res, UI_TIMEOUT_MS, false);
            if(obj != null && obj.isClickable()){
                obj.click();
            }
        } else {
            //nothing to be done service not listed.
            return;
        }
    }

    public static UiObject2 findAppItem(String appName){
        UiObject2 appObj = null;
        try {
            //TODO: Refactor to move the try catch block to utilities.
            ArrayList<String> appTitles = new ArrayList<>();

            //scrollable child.
            UiScrollable appItems
                    = new UiScrollable(new UiSelector()
                    .resourceId(ANDROID_RUNNING_TASK_LIST));

            assertTrue("Not scrollable!", appItems.isScrollable());

            //larger number for max swipes.
            appItems.flingToBeginning(100);
            sleep(UI_INTERACTION_DELAY_MS);


            boolean unique = true;
            boolean found = false;
            while (unique && !found) {
                unique = false;
                mDevice.waitForIdle();
                for (UiObject2 appTitle :
                        mDevice.findObjects(By.res(ANDROID_SETTINGS_APP_RESID))) {
                    String title = appTitle.getText();
                    if (!appTitles.contains(title)) {
                        appTitles.add(title);
                        unique = true;
                    }
                    if (title.equals(appName)) {
                        found = true;
                        appObj = appTitle;
                        break;
                    }
                }
                if(!found) {
                    appItems.flingForward();
                }
            }
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
            assertTrue("Object not found!", false);
        }
        return appObj;
    }
    public static void setOrientationNatural(){
        try{
            mDevice.setOrientationNatural();
        } catch (RemoteException e){
            e.printStackTrace();
            assertTrue("Failed setting device orientation!", false);
        }
    }



}

