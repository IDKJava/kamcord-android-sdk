package com.kamcord.app.testutils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.view.Surface;

import com.kamcord.app.R;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by Mehmet on 6/1/15.
 */
public class UiUtilities {

    public static final int APP_TIMEOUT_MS = 5000;
    public static final int UI_TIMEOUT_MS = 3000;
    public static final int RECORDING_DURATION_MS = 6000;
    public static final int DEFAULT_UPLOAD_TIMEOUT = 10000;
    public static final int MS_PER_MIN = 60000;
    public static final int DEFAULT_VIDEO_PROCESSING_TIMEOUT = 10000;
    public static final String OVERFLOW_DESCRIPTION = "More options";
    public static final String KAMCORD_APP_PACKAGE = "com.kamcord.app";
    public static final String RIPPLE_TEST_APP_PACKAGE = "com.kamcord.ripples";
    public static final String RIPPLE_TEST_APP_NAME = "Ripple Test";
    public static final String RIPPLE_TEST_APP_TITLE = "Ripple Demo";
    public static final String RIPPLE_TEST_MAIN_RES = "com.kamcord.ripples:id/mainlayout";
    public static final String ANDROID_DISMISS_TASK = "com.android.systemui:id/dismiss_task";
    public static final String ANDROID_SYSTEM_BUTTON1 = "android:id/button1";
    public static final String ANDROID_NOTIFICATION_HEADER = "com.android.systemui:id/header";
    public static final int UI_INTERACTION_DELAY_MS = 1000;

    public static final UiDevice mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

    public enum UiObjIdType {
        Res,
        Str
    }

    public enum UiObjSelType {
        Res,
        Txt,
        Des
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

        BySelector objSelector;
        switch (selType) {
            case Res:
                objSelector = By.res(idString);
                break;
            case Txt:
                //TODO: Change to 'starts with' or 'contains'?? We may need to less restrictive with text!
                objSelector = By.text(idString);
                break;
            default:
                throw new UnsupportedOperationException("UI Selector type not supported!");
        }

        boolean notTimedOut = mDevice.wait(Until.hasObject(objSelector), timeOut);
        assertTrue("UI Object failed to load!", notTimedOut);
        return mDevice.findObject(objSelector);
    }
    public static UiObject2 findUiObjInObj(UiObject2 parentObj,
                                           int id,
                                           UiObjIdType idType,
                                           UiObjSelType selType,
                                           int timeOut) {
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

        BySelector objSelector;
        switch (selType) {
            case Res:
                objSelector = By.res(idString);
                break;
            case Txt:
                //TODO: Change to 'starts with' or 'contains'?? We may need to less restrictive with text!
                objSelector = By.text(idString);
                break;
            default:
                throw new UnsupportedOperationException("UI Selector type not supported!");
        }

        boolean notTimedOut = parentObj.wait(Until.hasObject(objSelector), timeOut);
        assertTrue("UI Object failed to load!", notTimedOut);
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

        BySelector objSelector;
        switch (selType) {
            case Res:
                objSelector = By.res(idString);
                break;
            case Txt:
                //TODO: Change to 'starts with' or 'contains'?? We may need to less restrictive with text!
                objSelector = By.text(idString);
                break;
            default:
                throw new UnsupportedOperationException("UI Selector type not supported!");
        }

        boolean notTimedOut = mDevice.wait(Until.gone(objSelector), timeOut);
        assertTrue("UI Object failed to load!", notTimedOut);
    }

    public static UiObject2 findUiObj(String text, UiObjSelType selType) {
        return findUiObj(text, selType, UI_TIMEOUT_MS);
    }
    public static UiObject2 findUiObj(String text, UiObjSelType selType, int timeOut)
    {
        return findUiObj(text, selType, timeOut, true);
    }
    public static UiObject2 findUiObj(String text, UiObjSelType selType, int timeOut, boolean failIfNotFound ) {
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
            default:
                throw new UnsupportedOperationException("UI Selector type not supported!");
        }

        boolean notTimedOut = mDevice.wait(Until.hasObject(objSelector), timeOut);
        if (failIfNotFound) {
            assertTrue("UI Object failed to load!", notTimedOut);
        }
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
        //TODO: works for vertical only.
        int bottomBar = 200;
        int topBar = 100;
        int height = mDevice.getDisplayHeight();
        int width = mDevice.getDisplayWidth();
        int orientation = mDevice.getDisplayRotation();
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


        return swipePoints;
    }

    public static void startApplication(String appPackageName) {
        final String launcherPackage = getLauncherPackageName();

        assertThat(launcherPackage, notNullValue());

        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), APP_TIMEOUT_MS);

        Context context = InstrumentationRegistry.getContext();

        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(appPackageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        boolean notTimedOut = mDevice.wait(Until.hasObject(By.pkg(KAMCORD_APP_PACKAGE).depth(0)), APP_TIMEOUT_MS);
        assertTrue("Application load timed out!", notTimedOut);
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

            assertTrue("Not scrollable!", scrollableObject.isScrollable());

            //larger number for max swipes.
            scrollableObject.flingToBeginning(100);
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
            assertTrue("Object not found!", false);
        }
        sleep(sleepAfterMs);
    }
    public static void executeTouchPattern(Point[] pattern, int steps) {
        mDevice.swipe(validateSwipe(pattern),steps);
    }
}

