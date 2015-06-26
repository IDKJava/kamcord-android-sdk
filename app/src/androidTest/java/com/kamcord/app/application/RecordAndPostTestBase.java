package com.kamcord.app.application;

import android.graphics.Point;
import android.os.RemoteException;
import android.support.test.uiautomator.UiObject2;

import com.kamcord.app.R;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static com.kamcord.app.testutils.UiUtilities.*;
import static com.kamcord.app.testutils.SystemUtilities.*;
import static org.junit.Assert.fail;



/**
 * Created by Mehmet on 5/27/15.
 */
public abstract class RecordAndPostTestBase extends TestBase {
    public enum UploadTestVariant {
        NoNetwork,
        Interrupted,
        Normal,
        Delete
    }

    protected void recordGameVideo(String gameName, int durationInMs){
        recordGameVideo(gameName, gameName, durationInMs, false, false, false, false);
    }
    protected void recordGameVideo(String gameName, String gameTitle, int durationInMs){
        recordGameVideo(gameName, gameTitle, durationInMs, false, false, false, false);
    }
    protected void recordGameVideo(String gameName,
                                   String gameTitle,
                                   int durationInMs,
                                   boolean pauseAfterGesture) {
        recordGameVideo(gameName, gameTitle, durationInMs, pauseAfterGesture, false, false, false);
    }

        protected void recordGameVideo(String gameName,
                                       String gameTitle,
                                       int durationInMs,
                                       boolean pauseAfterGesture,
                                       boolean useNotificationsToSwitchToKamcord,
                                       boolean shortVideo,
                                       boolean checkNotificationStatus) {
            mDevice.waitForIdle(UI_TIMEOUT_MS);
            sleep(UI_TIMEOUT_MS);
            waitForTileLoad(R.id.recordfragment_refreshlayout, APP_TIMEOUT_MS);
            //find ripples app logo and click
            //wait for load!!!!
            findGame(gameName);

            getStartRecordingButtonForGame(gameName).click();
            UiObject2 obj = findUiObj(ANDROID_SYSTEM_BUTTON1, UiObjSelType.Res, UI_TIMEOUT_MS, false);
            int clickTrials = 0;

            while (obj == null && clickTrials < MAX_CLICK_TRIALS) {
                sleep(UI_INTERACTION_DELAY_MS);
                getStartRecordingButtonForGame(gameName).click();
                obj = findUiObj(ANDROID_SYSTEM_BUTTON1, UiObjSelType.Res, UI_TIMEOUT_MS);
                clickTrials++;
            }
            obj.click();
            //test app loads?
            findUiObj(RIPPLE_TEST_MAIN_RES, UiObjSelType.Res, UI_TIMEOUT_MS, false);

            //pattern exec time hardcoded for now.
            int miniSleepInMs = 2000;
            int sleepStep = durationInMs / miniSleepInMs;

            for (int i = 0; i < sleepStep; i++) {
                //long time = System.currentTimeMillis();
                if ((i % 2) == 0) {
                    executeRectPattern();
                    sleep(400);
                } else {
                    if (pauseAfterGesture) {
                        //get running task list
                        try {
                            mDevice.pressRecentApps();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            assertFalse("Press recent apps failed!", true);
                        }

                        //check if it's recording
                        mDevice.openNotification();
                        mDevice.waitForIdle(UI_TIMEOUT_MS);
                        sleep(UI_TIMEOUT_MS);
                        //findUiObj(ANDROID_NOTIFICATION_HEADER, UiObjSelType.Res, APP_TIMEOUT_MS);

                        findUiObj(R.string.paused, UiObjIdType.Str, UiObjSelType.Txt, APP_TIMEOUT_MS);

                        mDevice.pressBack();
                        mDevice.waitForIdle(UI_TIMEOUT_MS);
                        sleep(UI_TIMEOUT_MS);
                        findUiObj(gameTitle, UiObjSelType.Txt, APP_TIMEOUT_MS).click();

                        findUiObj(RIPPLE_TEST_MAIN_RES, UiObjSelType.Res, APP_TIMEOUT_MS);

                    } else {
                        sleep(miniSleepInMs);
                    }
                }

            }

            //get running task list
            try {
                mDevice.pressRecentApps();
            } catch (RemoteException e) {
                e.printStackTrace();
                assertFalse("Press recent apps failed!", true);
            }
            //Flaky
            //check if it's recording
            if(checkNotificationStatus) {
                mDevice.openNotification();
                //openNotifications();

                mDevice.waitForIdle(UI_TIMEOUT_MS);
                sleep(UI_TIMEOUT_MS);
                findUiObj(ANDROID_NOTIFICATION_HEADER, UiObjSelType.Res, APP_TIMEOUT_MS);

                findUiObj(R.string.paused, UiObjIdType.Str, UiObjSelType.Txt, APP_TIMEOUT_MS);
                if (!useNotificationsToSwitchToKamcord) {
                    //closes notifications so we can pick from recent apps.
                    mDevice.pressBack();
                    mDevice.waitForIdle(UI_TIMEOUT_MS);
                    sleep(UI_TIMEOUT_MS);
                }
            }

            //click on notification to resume app.
            findUiObj(R.string.toolbarTitle, UiObjIdType.Str, UiObjSelType.Txt).click();
            sleep(UI_TIMEOUT_MS);
            //find stop recording button.
            UiObject2 stopButton = findUiObj(R.id.stopRecordingImageButton,
                    UiObjIdType.Res,
                    UiObjSelType.Res,
                    UI_TIMEOUT_MS,
                    false);
            int reTries = 0;
            while (reTries < MAX_CLICK_TRIALS && stopButton == null) {
                stopButton = findUiObj(R.id.stopRecordingImageButton,
                        UiObjIdType.Res,
                        UiObjSelType.Res,
                        UI_TIMEOUT_MS,
                        false);
                reTries++;
            }
            findUiObj(R.id.stopRecordingTakeoverContainer, UiObjIdType.Res, UiObjSelType.Res);
            findUiObj(R.id.stopRecordingImageButton, UiObjIdType.Res, UiObjSelType.Res).click();
            if(!shortVideo) {
                findUiObj(R.id.playImageView, UiObjIdType.Res, UiObjSelType.Res, durationInMs);
            }
        }

    protected void handleShareFlowQueueCheck(int durationInMs) {
        handleShareFlowQueueCheck(durationInMs, UploadTestVariant.Normal, false, false);
    }

    protected void handleShareFlowQueueCheck(int durationInMs,
                                             UploadTestVariant uploadTestType,
                                             boolean checkNotifications,
                                             boolean notLoggedIn) {
        switch (uploadTestType) {

            case NoNetwork:
            case Delete:
                toggleNetwork(false);
                break;
            case Normal:
            case Interrupted:
                //let normal run here and interrupt if need be
                break;
        }
        sleep(UI_INTERACTION_DELAY_MS);

        String videoTitle = UUID.randomUUID().toString();
        String currentlyUploading = getStrByID(R.string.currentlyUploadingPercent).split("\\(")[0];
        //wait for video processing to finish
        //TODO: Adjust the "1" divider to something reasonable as stitching perf. improves.
        int processingTimeout = Math.max((durationInMs / 1), DEFAULT_VIDEO_PROCESSING_TIMEOUT);
        int uploadTimeout = Math.max((durationInMs / 1), DEFAULT_UPLOAD_TIMEOUT);

        findUiObj(R.id.playImageView, UiObjIdType.Res, UiObjSelType.Res, processingTimeout);

        UiObject2 title = findUiObj(R.id.titleEditText, UiObjIdType.Res, UiObjSelType.Res);
        assertTrue("Default video title not correct!",
                String.format("My latest %s video", RIPPLE_TEST_APP_NAME).equals(title.getText()));
        title.click();
        title.setText(videoTitle);

        //close soft keyboard
        mDevice.pressBack();
        mDevice.waitForIdle(UI_TIMEOUT_MS);
        sleep(UI_TIMEOUT_MS);
        UiObject2 procVidObj;
        int maxReTries;
        int reTries;
        String videoAuthor;
        //network start state

        findUiObj(R.id.share_button, UiObjIdType.Res, UiObjSelType.Res).click();
        if(notLoggedIn) {
            handleWelcomeLoginView();
            findUiObj(R.id.share_button, UiObjIdType.Res, UiObjSelType.Res).click();
        }
        findUiObj(R.string.kamcordProfileTab,
                UiObjIdType.Str,
                UiObjSelType.Des,
                UI_TIMEOUT_MS).click();
        //Decisions on how to resume.
        switch (uploadTestType) {
            case NoNetwork:
            case Delete:
                //sleep(50);
                findUiObj(R.string.uploadFailed,
                        UiObjIdType.Str,
                        UiObjSelType.Txt,
                        DEFAULT_UPLOAD_TIMEOUT);
                //turning on the Internets
                toggleNetwork(true);
                sleep(UI_TIMEOUT_MS);
                break;
            case Normal:
                //do nothing
                break;
            case Interrupted:
                //let it start
                findUiObj(currentlyUploading, UiObjSelType.TxtContains, DEFAULT_UPLOAD_TIMEOUT);
                //interrupt
                toggleNetwork(false);
                findUiObj(R.string.uploadFailed,
                        UiObjIdType.Str,
                        UiObjSelType.Txt,
                        DEFAULT_UPLOAD_TIMEOUT);
                //turning on the Internets
                toggleNetwork(true);
                sleep(UI_TIMEOUT_MS);
                break;
        }
        //the finale
        switch (uploadTestType) {
            case Interrupted:
            case NoNetwork:
                findUiObj(R.id.retryUploadImageButton, UiObjIdType.Res, UiObjSelType.Res).click();
                //let it run.
            case Normal:
                //Successful Completion Ending.
                findUiObj(currentlyUploading, UiObjSelType.TxtContains, DEFAULT_UPLOAD_TIMEOUT);
                if(checkNotifications) {
                    mDevice.openNotification();
                    mDevice.waitForIdle(UI_TIMEOUT_MS);
                    //We're not fast enough to check both before the upload finishes. :(
                    //findUiObj(R.string.app_name, UiObjIdType.Str, UiObjSelType.Txt);
                    findUiObj(R.string.uploading, UiObjIdType.Str, UiObjSelType.Txt, APP_TIMEOUT_MS);
                    mDevice.pressBack();
                    mDevice.waitForIdle(UI_TIMEOUT_MS);
                    sleep(UI_TIMEOUT_MS);
                }
                //go to profile
                findUiObj(R.string.processingPullToRefresh,
                        UiObjIdType.Str,
                        UiObjSelType.Txt,
                        uploadTimeout);
                //pull to refresh and see what's going on.
                procVidObj = findUiObj(R.string.processingPullToRefresh,
                        UiObjIdType.Str, UiObjSelType.Txt, UI_TIMEOUT_MS, false);
                //UI timeout * 2 * 30 = 120s. We give it ~1min be processed.
                maxReTries = 30;
                reTries = 0;
                while (reTries < maxReTries && procVidObj != null) {
                    reTries++;
                    procVidObj = findUiObj(R.string.processingPullToRefresh,
                            UiObjIdType.Str, UiObjSelType.Txt, UI_TIMEOUT_MS, false);
                    scrollToBeginning(R.id.profile_recyclerview, UI_INTERACTION_DELAY_MS);
                    sleep(UI_TIMEOUT_MS);
                }
                assertTrue("Processing timed out!", reTries <= maxReTries);
                //look for the video id in the feed
                maxReTries = 15;
                reTries = 0;
                procVidObj = null;
                while (reTries < maxReTries &&
                        (procVidObj == null ||
                                procVidObj.getResourceName()
                                        .equals(getResByID(R.id.videoTitleTextView)))) {
                    reTries++;
                    procVidObj = findUiObj(videoTitle, UiObjSelType.Txt, UI_TIMEOUT_MS, false);
                    scrollToBeginning(R.id.profile_recyclerview, UI_INTERACTION_DELAY_MS);
                    mDevice.waitForIdle(UI_TIMEOUT_MS);
                    sleep(UI_TIMEOUT_MS);
                }
                assertTrue("Feed timed out!", reTries <= maxReTries);
                videoAuthor = getVideoAuthor(videoTitle);
                assertTrue("Video not found!", videoAuthor.contains(USERNAME1));

                //close notifications
                break;
            case Delete:
                findUiObj(R.id.uploadFailedImageButton,
                        UiObjIdType.Res,
                        UiObjSelType.Res,
                        UI_TIMEOUT_MS).click();
                findUiObj(R.string.delete,
                        UiObjIdType.Str,
                        UiObjSelType.Txt,
                        UI_TIMEOUT_MS).click();
                loseUiObj(R.string.uploadFailed,
                        UiObjIdType.Str,
                        UiObjSelType.Txt,
                        UI_TIMEOUT_MS);
                break;
        }
    }


    protected void executeRectPattern() {
        Point[] pattern = new Point[]{new Point(500, 300),
                new Point(500, 1600),
                new Point(1000, 1600),
                new Point(1000, 300),
                new Point(500, 300)};
        //25 steps to ensure ~1600ms execution
        executeTouchPattern(pattern, 25);
    }

    protected UiObject2 getStartRecordingButtonForGame(String gameName){

        UiObject2 gameLabel = findUiObj(gameName, UiObjSelType.Txt);

        UiObject2 completeGameItem = gameLabel.getParent().getParent();

        UiObject2 button = findUiObjInObj(completeGameItem, R.id.gameActionImageButton,
                UiObjIdType.Res, UiObjSelType.Res, UI_TIMEOUT_MS);
        return button;
    }

    //TODO: Refactor to a class for this kind of uploadvideo object
    protected UiObject2 getUploadRetryButton(String videoTitle){
        UiObject2 videoTitleObj = findUiObj(videoTitle, UiObjSelType.Txt, UI_TIMEOUT_MS);
        UiObject2 listItemObj = videoTitleObj.getParent().getParent().getParent();
        UiObject2 button = findUiObjInObj(listItemObj,
                R.id.retryUploadImageButton,
                UiObjIdType.Res,
                UiObjSelType.Res,
                UI_TIMEOUT_MS);
        return button;
    }

    protected UiObject2 getUploadOptionsButton(String videoTitle){
        UiObject2 videoTitleObj = findUiObj(videoTitle, UiObjSelType.Txt, UI_TIMEOUT_MS);
        UiObject2 listItemObj = videoTitleObj.getParent().getParent().getParent();
        UiObject2 button = findUiObjInObj(listItemObj,
                R.id.uploadFailedImageButton,
                UiObjIdType.Res,
                UiObjSelType.Res,
                UI_TIMEOUT_MS);
        return button;
    }
    protected String getUploadStatus(String videoTitle){
        UiObject2 videoTitleObj = findUiObj(videoTitle, UiObjSelType.Txt, UI_TIMEOUT_MS);
        UiObject2 listItemObj = videoTitleObj.getParent();
        UiObject2 button = findUiObjInObj(listItemObj,
                R.id.uploadStatusTextView,
                UiObjIdType.Res,
                UiObjSelType.Res,
                UI_TIMEOUT_MS);
        return button.getText();

    }
    protected String getVideoAuthor(String videoTitle){
        UiObject2 videoTitleObj = findUiObj(videoTitle.substring(0, 8),
                                            UiObjSelType.TxtContains,
                                            UI_TIMEOUT_MS);
        UiObject2 listItemObj = videoTitleObj.getParent();
        UiObject2 obj = findUiObjInObj(listItemObj,
                R.id.profile_item_author,
                UiObjIdType.Res,
                UiObjSelType.Res,
                UI_TIMEOUT_MS);
        return obj.getText();
    }

}
