package com.kamcord.app.activity;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.kamcord.app.R;
import com.kamcord.app.adapter.MainViewPagerAdapter;
import com.kamcord.app.fragment.RecordFragment;
import com.kamcord.app.fragment.ShareFragment;
import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.server.model.Game;
import com.kamcord.app.service.RecordingService;
import com.kamcord.app.utils.SlidingTabLayout;

public class RecordActivity extends ActionBarActivity implements View.OnClickListener, RecordFragment.SelectedGameListener {
    private static final String TAG = RecordActivity.class.getSimpleName();
    private static final int MEDIA_PROJECTION_MANAGER_PERMISSION_CODE = 1;

    ImageButton mFloatingActionButton;
    private ViewPager mViewPager;
    private MainViewPagerAdapter mainViewPagerAdapter;
    private SlidingTabLayout mTabs;
    private CharSequence tabTitles[];
    private int numberOfTabs;
    public String videoPath;
    private ProgressDialog mProgressDialog;
    public Toolbar mToolbar;

    private Game mSelectedGame = null;
    private RecordingServiceConnection mConnection = new RecordingServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mdrecord);

        initMainActivity();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (RecordingService.isRunning()) {
            bindService(new Intent(this, RecordingService.class), mConnection, 0);
        }
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
        if (mConnection.isConnected()) {
            unbindService(mConnection);
        }
    }

    public void initMainActivity() {

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(getString(R.string.app_name));
        mToolbar.setLogo(R.drawable.toolbar_icon);

        tabTitles = new String[2];
        tabTitles[0] = getResources().getString(R.string.kamcordRecordTab);
        tabTitles[1] = getResources().getString(R.string.kamcordProfileTab);
        numberOfTabs = tabTitles.length;

        mTabs = (SlidingTabLayout) findViewById(R.id.tabs);
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
            }
        });
        mViewPager = (ViewPager) findViewById(R.id.main_pager);
        mainViewPagerAdapter = new com.kamcord.app.adapter.MainViewPagerAdapter(getSupportFragmentManager(), tabTitles, numberOfTabs);
        mViewPager.setAdapter(mainViewPagerAdapter);
        mTabs.setViewPager(mViewPager);

        mFloatingActionButton = (ImageButton) findViewById(R.id.main_fab);
        mFloatingActionButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_fab: {
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

                    RecordingSession recordingSession = mConnection.getServiceRecordingSession();
                    if( recordingSession != null ) {
                        ShareFragment recordShareFragment = new ShareFragment();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(ShareFragment.ARG_RECORDING_SESSION, mConnection.getServiceRecordingSession());
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
                    mConnection.initializeForRecording(projection, recordingSession);

                    startService(new Intent(this, RecordingService.class));
                    bindService(new Intent(this, RecordingService.class), mConnection, 0);
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

    private class RecordingServiceConnection implements ServiceConnection {
        private RecordingService recordingService;
        private MediaProjection mediaProjection = null;
        private RecordingSession recordingSession = null;
        private boolean isConnected = false;

        public void initializeForRecording(MediaProjection mediaProjection, RecordingSession recordingSession) {
            this.mediaProjection = mediaProjection;
            this.recordingSession = recordingSession;
        }

        public void uninitialize() {
            this.mediaProjection = null;
            this.recordingSession = null;
        }

        public boolean isInitializedForRecording() {
            return mediaProjection != null && recordingSession != null;
        }

        public boolean isConnected() {
            return isConnected;
        }

        public RecordingSession getServiceRecordingSession()
        {
            return recordingService != null ? recordingService.getRecordingSession() : null;
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            recordingService = ((RecordingService.LocalBinder) iBinder).getService();
            if (isInitializedForRecording()) {
                recordingService.startRecording(mediaProjection, recordingSession);
                uninitialize();
            }
            isConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            uninitialize();
            isConnected = false;
        }
    }
}
