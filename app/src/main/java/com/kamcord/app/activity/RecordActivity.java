package com.kamcord.app.activity;

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
import android.widget.ImageButton;
import android.widget.Toast;

import com.kamcord.app.R;
import com.kamcord.app.fragment.RecordFragment;
import com.kamcord.app.fragment.RecordShareFragment;
import com.kamcord.app.server.model.Game;
import com.kamcord.app.service.RecordingService;
import com.kamcord.app.utils.SlidingTabLayout;

public class RecordActivity extends ActionBarActivity implements View.OnClickListener, RecordFragment.selectdGameListener {
    private static final String TAG = RecordActivity.class.getSimpleName();
    private static final int MEDIA_PROJECTION_MANAGER_PERMISSION_CODE = 1;

    Toolbar mToolBar;
    ImageButton mFloatingActionButton;
    private ViewPager mViewPager;
    private com.kamcord.app.adapter.MainViewPagerAdapter mainViewPagerAdapter;
    private SlidingTabLayout mTabs;
    private CharSequence tabTitles[] = {"Record", "Profile"};
    private int numberOfTabs = 2;


    private Game mSelectedGame = null;
    private RecordingServiceConnection mConnection = new RecordingServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mdrecord);

        initMainActivity();

        if (RecordingService.isRunning()) {
            bindService(new Intent(this, RecordingService.class), mConnection, 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConnection.isConnected()) {
            unbindService(mConnection);
        }
    }

    public void initMainActivity() {

        mToolBar = (Toolbar) findViewById(R.id.md_toolbar);
        mToolBar.setTitle(R.string.toolbarTitle);
        setSupportActionBar(mToolBar);

        mTabs = (SlidingTabLayout) findViewById(R.id.tabs);
        mTabs.setDistributeEvenly(true);
        mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
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
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
                            .add(R.id.main_activity_layout, new RecordShareFragment())
                            .addToBackStack("LoginFragment").commit();
                    mFloatingActionButton.setImageResource(R.drawable.ic_videocam_white_36dp);
                    stopService(new Intent(this, RecordingService.class));
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
    public void onResume() {
        super.onResume();
        handleServiceRunning();
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
                    mConnection.initializeForRecording(projection, mSelectedGame);

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

    private static class RecordingServiceConnection implements ServiceConnection {
        private MediaProjection mediaProjection = null;
        private Game gameModel = null;
        private boolean isConnected = false;

        public void initializeForRecording(MediaProjection mediaProjection, Game gameModel) {
            this.mediaProjection = mediaProjection;
            this.gameModel = gameModel;
        }

        public void uninitialize() {
            this.mediaProjection = null;
            this.gameModel = null;
        }

        public boolean isInitializedForRecording() {
            return mediaProjection != null && gameModel != null;
        }

        public boolean isConnected()
        {
            return isConnected;
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            RecordingService recordingService = ((RecordingService.LocalBinder) iBinder).getService();
            if (isInitializedForRecording()) {
                recordingService.startRecording(mediaProjection, gameModel);
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
