package com.kamcord.app.activity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.kamcord.app.R;
import com.kamcord.app.adapter.MainViewPagerAdapter;
import com.kamcord.app.fragment.RecordFragment;
import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.thread.Uploader;
import com.kamcord.app.view.SlidingTabLayout;
import com.kamcord.app.view.utils.OnBackPressedListener;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class RecordActivity extends AppCompatActivity implements
        RecordFragment.RecyclerViewScrollListener,
        Uploader.UploadStatusListener {
    private static final String TAG = RecordActivity.class.getSimpleName();

    @InjectView(R.id.main_pager) ViewPager mViewPager;
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
    protected void onStart() {
        super.onStart();
        Uploader.setUploadStatusListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.invalidateOptionsMenu();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Uploader.setUploadStatusListener(null);
    }

    public void initMainActivity() {

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
        super.onBackPressed();
        if( onBackPressedListener != null ) {
            onBackPressedListener.onBackPressed();
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
                && recyclerView.getChildAt(0).getTop() > 0)) {
            recyclerViewScrolledDistance = 0;
        } else if (recyclerViewScrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
            recyclerViewScrolledDistance = 0;
        }

        if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
            recyclerViewScrolledDistance += dy;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        FlurryAgent.onEndSession(this);
    }
}
