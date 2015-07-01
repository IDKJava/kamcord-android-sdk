package com.kamcord.app.activity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.kamcord.app.R;
import com.kamcord.app.adapter.MainViewPagerAdapter;
import com.kamcord.app.fragment.ShareFragment;
import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.thread.Uploader;
import com.kamcord.app.view.DisableableViewPager;
import com.kamcord.app.view.SlidingTabLayout;
import com.kamcord.app.view.utils.OnBackPressedListener;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class RecordActivity extends FragmentActivity implements
        Uploader.UploadStatusListener {

    @InjectView(R.id.main_pager) DisableableViewPager mViewPager;
    @InjectView(R.id.tabs) SlidingTabLayout mTabs;
    @InjectView(R.id.uploadProgressBar) ProgressBar uploadProgress;

    private MainViewPagerAdapter mainViewPagerAdapter;
    private CharSequence tabTitles[];
    private int numberOfTabs;

    private static final int HIDE_THRESHOLD = 20;
    private boolean controlsVisible = true;
    private int recyclerViewScrolledDistance = 0;

    private OnBackPressedListener onBackPressedListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mdrecord);
        FlurryAgent.onStartSession(this);
        ButterKnife.inject(this);
        initMainActivity();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onResume() {
        super.onResume();
        this.invalidateOptionsMenu();
    }

    public void initMainActivity() {

        tabTitles = new String[3];
        tabTitles[0] = getResources().getString(R.string.kamcordRecordTab);
        tabTitles[1] = getResources().getString(R.string.kamcordProfileTab);
        tabTitles[2] = getResources().getString(R.string.kamcordHomeTab);
        numberOfTabs = tabTitles.length;

        mTabs.setDistributeEvenly(true);
        mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        mTabs.setCustomTabView(R.layout.tab_icon_title, 0);
        mainViewPagerAdapter = new com.kamcord.app.adapter.MainViewPagerAdapter(getSupportFragmentManager(), tabTitles, numberOfTabs);
        mViewPager.setAdapter(mainViewPagerAdapter);
        mTabs.setViewPager(mViewPager);
    }

    public void setOnBackPressedListener(OnBackPressedListener listener) {
        onBackPressedListener = listener;
    }

    @Override
    public void onBackPressed() {
        boolean fragmentHandled = false;

        FragmentManager manager = getSupportFragmentManager();
        for( Fragment fragment : manager.getFragments() ) {
            if( fragment instanceof OnBackPressedListener ) {
                fragmentHandled |= ((OnBackPressedListener) fragment).onBackPressed();
            }
        }

        // We only forward the back click to super if none of our fragments handled it.
        if( !fragmentHandled ) {
            super.onBackPressed();
        }
    }

    ObjectAnimator progressBarAnimator = null;

    @Override
    public void onUploadStart(final RecordingSession recordingSession) {
        if (uploadProgress != null) {
        uploadProgress.post(new Runnable() {
            @Override
            public void run() {
                String toastText = recordingSession.getVideoTitle() != null
                        ? String.format(Locale.ENGLISH, getResources().getString(R.string.yourVideoIsUploading), recordingSession.getVideoTitle())
                        : getResources().getString(R.string.yourVideoIsUploadingNoTitle);
                Toast.makeText(RecordActivity.this, toastText, Toast.LENGTH_SHORT).show();
                uploadProgress.setVisibility(View.VISIBLE);
                uploadProgress.setAlpha(1f);
                uploadProgress.setProgress(0);
            }
        });
        }
    }

    @Override
    public void onUploadProgress(RecordingSession recordingSession, final float progress) {
        if (uploadProgress != null) {
        uploadProgress.post(new Runnable() {
            @Override
            public void run() {
                if (progressBarAnimator != null) {
                    progressBarAnimator.cancel();
                }
                int oldProgress = uploadProgress.getProgress();
                int newProgress = (int) (progress * uploadProgress.getMax());
                progressBarAnimator = ObjectAnimator.ofInt(uploadProgress, "progress", oldProgress, newProgress)
                        .setDuration(400);
                progressBarAnimator.start();
            }
        });
        }
    }

    @Override
    public void onUploadFinish(final RecordingSession recordingSession, final boolean success) {
        if (uploadProgress != null) {
        uploadProgress.post(new Runnable() {
            @Override
            public void run() {
                String toastText = recordingSession.getVideoTitle() != null
                        ? String.format(Locale.ENGLISH, getResources().getString(R.string.yourVideoFinishedUploading), recordingSession.getVideoTitle())
                        : getResources().getString(R.string.yourVideoFinishedUploadingNoTitle);
                Toast.makeText(RecordActivity.this, toastText, Toast.LENGTH_SHORT).show();
                uploadProgress.setIndeterminate(false);
                uploadProgress.animate().setStartDelay(500).alpha(0f).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        uploadProgress.setVisibility(View.GONE);
                    }
                }).start();
            }
        });
        }
    }

    public void setCurrentItem(int i)
    {
        mViewPager.setCurrentItem(i);
    }

    public void setPagingEnabled(boolean enabled) {
        mViewPager.setEnabled(enabled);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FlurryAgent.onEndSession(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        android.support.v4.app.Fragment fragment = getSupportFragmentManager().findFragmentByTag(ShareFragment.TAG);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
