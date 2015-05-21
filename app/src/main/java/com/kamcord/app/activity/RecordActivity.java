package com.kamcord.app.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kamcord.app.R;
import com.kamcord.app.adapter.MainViewPagerAdapter;
import com.kamcord.app.fragment.RecordFragment;
import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.server.model.Game;
import com.kamcord.app.service.RecordingService;
import com.kamcord.app.service.connection.RecordingServiceConnection;
import com.kamcord.app.thread.Uploader;
import com.kamcord.app.utils.SlidingTabLayout;
import com.kamcord.app.view.ObservableWebView;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class RecordActivity extends ActionBarActivity implements
        View.OnClickListener,
        RecordFragment.SelectedGameListener,
        RecordFragment.RecyclerViewScrollListener,
        ObservableWebView.ObservableWebViewScrollListener,
        Uploader.UploadStatusListener {
    private static final String TAG = RecordActivity.class.getSimpleName();
    private static final int MEDIA_PROJECTION_MANAGER_PERMISSION_CODE = 1;

    @InjectView(R.id.main_fab) ImageButton mFloatingActionButton;
    @InjectView(R.id.main_pager) ViewPager mViewPager;
    @InjectView(R.id.tabs) SlidingTabLayout mTabs;
    @InjectView(R.id.toolbarContainer) ViewGroup toolbarContainer;
    @InjectView(R.id.toolbar) Toolbar mToolbar;
    @InjectView(R.id.uploadProgressBar) ProgressBar uploadProgress;

    private MainViewPagerAdapter mainViewPagerAdapter;
    private CharSequence tabTitles[];
    private int numberOfTabs;

    private Game mSelectedGame = null;
    private RecordingServiceConnection mRecordingServiceConnection = new RecordingServiceConnection();

    private static final int HIDE_THRESHOLD = 20;
    private boolean controlsVisible = true;
    private int recyclerViewScrolledDistance = 0;
    private int webViewScrolledDistance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mdrecord);
        ButterKnife.inject(this);

        initMainActivity();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (RecordingService.isRunning()) {
            bindService(new Intent(this, RecordingService.class), mRecordingServiceConnection, 0);
        }
        Uploader.setUploadStatusListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        handleServiceRunning();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (mRecordingServiceConnection.isConnected()) {
            unbindService(mRecordingServiceConnection);
        }
        Uploader.setUploadStatusListener(null);
    }

    public void initMainActivity() {

        setSupportActionBar(mToolbar);
        mToolbar.setTitle(getString(R.string.app_name));
        mToolbar.setLogo(R.drawable.toolbar_icon);

        tabTitles = new String[2];
        tabTitles[0] = getResources().getString(R.string.kamcordRecordTab);
        tabTitles[1] = getResources().getString(R.string.kamcordProfileTab);
        numberOfTabs = tabTitles.length;

        mTabs.setDistributeEvenly(true);
        mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        mTabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position != 0) {
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mFloatingActionButton.getLayoutParams();
                    mFloatingActionButton.animate().translationY(mFloatingActionButton.getHeight() + layoutParams.bottomMargin);
                } else {
                    mFloatingActionButton.animate().translationY(0);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if( state == ViewPager.SCROLL_STATE_DRAGGING )
                {
                    showToolbar();
                }
            }
        });
        mainViewPagerAdapter = new com.kamcord.app.adapter.MainViewPagerAdapter(getSupportFragmentManager(), tabTitles, numberOfTabs);
        mViewPager.setAdapter(mainViewPagerAdapter);
        mTabs.setViewPager(mViewPager);

        mFloatingActionButton.setOnClickListener(this);

    }

    private void hideToolbar() {
        toolbarContainer.animate()
                .translationY(-mToolbar.getHeight())
                .setInterpolator(new AccelerateInterpolator(2));
        controlsVisible = false;
    }

    private void showToolbar() {
        toolbarContainer.animate()
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator(2));
        controlsVisible = true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_fab: {
                new Thread(new Runnable() {
                    RecordingSession session = new RecordingSession();
                    @Override
                    public void run() {
                        try {
                            synchronized (this) {
                                onUploadStart(session);
                                wait(500);
                                onUploadProgress(session, 0.2f);
                                wait(500);
                                onUploadProgress(session, 0.4f);
                                wait(500);
                                onUploadProgress(session, 0.6f);
                                wait(500);
                                onUploadProgress(session, 0.8f);
                                wait(500);
                                onUploadProgress(session, 1f);
                                wait(500);
                                onUploadFinish(session, true);
                            }
                        }
                        catch( Exception e )
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
                /*
                if (!RecordingService.isRunning()) {
                    if (mSelectedGame != null) {
                        mFloatingActionButton.setImageResource(R.drawable.ic_videocam_off_white_36dp);
                        obtainMediaProjection();

                    } else {
                        Toast.makeText(getApplicationContext(), R.string.selectAGame, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ((ImageButton) v).setImageResource(R.drawable.ic_videocam_white_36dp);
                    mFloatingActionButton.setImageResource(R.drawable.ic_videocam_white_36dp);
                    stopService(new Intent(this, RecordingService.class));

                    RecordingSession recordingSession = mRecordingServiceConnection.getServiceRecordingSession();
                    if( recordingSession != null ) {
                        ShareFragment recordShareFragment = new ShareFragment();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(ShareFragment.ARG_RECORDING_SESSION, mRecordingServiceConnection.getServiceRecordingSession());
                        recordShareFragment.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.main_activity_layout, recordShareFragment)
                                .addToBackStack("ShareFragment").commit();
                    }
                    else
                    {
                        // TODO: show the user something about being unable to get the recording session.
                    }
                }
                */
            }
        }
    }

    @Override
    public void selectedGame(Game gameModel) {
        mSelectedGame = gameModel;
    }

    public void obtainMediaProjection() {
        startActivityForResult(((MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE))
                .createScreenCaptureIntent(), MEDIA_PROJECTION_MANAGER_PERMISSION_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == MEDIA_PROJECTION_MANAGER_PERMISSION_CODE) {
            if (mSelectedGame != null) {
                try {
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(mSelectedGame.play_store_id);
                    startActivity(launchIntent);

                    MediaProjection projection = ((MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE))
                            .getMediaProjection(resultCode, data);
                    RecordingSession recordingSession = new RecordingSession(mSelectedGame);
                    mRecordingServiceConnection.initializeForRecording(projection, recordingSession);

                    startService(new Intent(this, RecordingService.class));
                    bindService(new Intent(this, RecordingService.class), mRecordingServiceConnection, 0);
                } catch (ActivityNotFoundException e) {
                    // TODO: show the user something about not finding the game.
                    Log.w(TAG, "Could not find activity with package " + mSelectedGame.play_store_id);
                }
            } else {
                // TODO: show the user something about selecting a game.
                Log.w("Kamcord", "Unable to start recording because user has not selected a game to record!");
            }
        }
    }

    private void handleServiceRunning() {
        if (RecordingService.isRunning()) {
            mFloatingActionButton.setImageResource(R.drawable.ic_videocam_off_white_36dp);
        } else {
            mFloatingActionButton.setImageResource(R.drawable.ic_videocam_white_36dp);
        }
    }

    @Override
    public void onRecyclerViewScrollStateChanged(RecyclerView recyclerView, int state) {
    }

    @Override
    public void onRecyclerViewScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (recyclerViewScrolledDistance > HIDE_THRESHOLD && controlsVisible
                && !(recyclerView.getChildCount() > 0
                        && recyclerView.getChildAdapterPosition(recyclerView.getChildAt(0)) == 0
                        && recyclerView.getChildAt(0).getTop() > 0) )
        {
            hideToolbar();
            recyclerViewScrolledDistance = 0;
        } else if (recyclerViewScrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
            showToolbar();
            recyclerViewScrolledDistance = 0;
        }

        if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
            recyclerViewScrolledDistance += dy;
        }
    }

    @Override
    public void onObservableWebViewScrolled(ObservableWebView webView, int dx, int dy) {
        if (webViewScrolledDistance > HIDE_THRESHOLD && controlsVisible
                && webView.getScrollY() >= getResources().getDimensionPixelSize(R.dimen.tabsHeight) ) {
            hideToolbar();
            controlsVisible = false;
            webViewScrolledDistance = 0;
        } else if (webViewScrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
            showToolbar();
            controlsVisible = true;
            webViewScrolledDistance = 0;
        }

        if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
            webViewScrolledDistance += dy;
        }
    }

    ObjectAnimator progressBarAnimator = null;
    @Override
    public void onUploadStart(final RecordingSession recordingSession) {
        Log.v("FindMe", "onUploadStart(" + recordingSession + ")");
        uploadProgress.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RecordActivity.this, String.format(Locale.ENGLISH, getResources().getString(R.string.yourVideoIsUploading), recordingSession.getVideoTitle()), Toast.LENGTH_SHORT).show();
                uploadProgress.setVisibility(View.VISIBLE);
                uploadProgress.setAlpha(1f);
                uploadProgress.setProgress(0);
//                uploadProgress.setIndeterminate(true);
            }
        });
    }

    @Override
    public void onUploadProgress(RecordingSession recordingSession, final float progress) {
        Log.v("FindMe", "onUploadProgress(" + recordingSession + ", " + progress + ")");
        uploadProgress.post(new Runnable() {
            @Override
            public void run() {
                if( progressBarAnimator != null )
                {
                    progressBarAnimator.cancel();
                }
                int oldProgress = uploadProgress.getProgress();
                int newProgress = (int) (progress * uploadProgress.getMax());
                progressBarAnimator = ObjectAnimator.ofInt(uploadProgress, "progress", oldProgress, newProgress)
                        .setDuration(400);
                progressBarAnimator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation)
                            {
                                uploadProgress.setIndeterminate(false);
                            }
                            @Override
                            public void onAnimationEnd(Animator animation) {
//                                uploadProgress.setIndeterminate(true);
                            }
                        });
                progressBarAnimator.start();
            }
        });
    }

    @Override
    public void onUploadFinish(final RecordingSession recordingSession, final boolean success) {
        Log.v("FindMe", "onUploadFinish(" + recordingSession + ", " + success + ")");
        uploadProgress.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RecordActivity.this, String.format(Locale.ENGLISH, getResources().getString(R.string.yourVideoFinishedUploading), recordingSession.getVideoTitle()), Toast.LENGTH_SHORT).show();
                uploadProgress.setIndeterminate(false);
                uploadProgress.animate().setStartDelay(1000).alpha(0f).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        uploadProgress.setVisibility(View.GONE);
                    }
                }).start();
            }
        });
    }
}
