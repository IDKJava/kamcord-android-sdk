package com.kamcord.app.kamcord.activity.activity;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.kamcord.app.kamcord.R;
import com.kamcord.app.kamcord.activity.adapter.MainViewPagerAdapter;
import com.kamcord.app.kamcord.activity.fragment.RecordFragment;
import com.kamcord.app.kamcord.activity.fragment.RecordShareFragment;
import com.kamcord.app.kamcord.activity.server.model.Game;
import com.kamcord.app.kamcord.activity.service.RecordingService;
import com.kamcord.app.kamcord.activity.utils.SlidingTabLayout;

public class RecordActivity extends ActionBarActivity implements View.OnClickListener, RecordFragment.selectdGameListener {

    private static final int MEDIA_PROJECTION_MANAGER_PERMISSION_CODE = 1;

    Toolbar mToolBar;
    ImageButton mFloatingActionButton;
    private ViewPager mViewPager;
    private MainViewPagerAdapter mainViewPagerAdapter;
    private SlidingTabLayout mTabs;
    private CharSequence tabTitles[] = {"Record", "Profile"};
    private int numberOfTabs = 2;

    private int recordButtonResId = -1;

    private RecordingService mRecordingService;
    private MediaProjectionManager mMediaProjectionManager;
    private boolean mIsBoundToService = false;
    private Game mSelectedGame = null;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mRecordingService = ((RecordingService.LocalBinder) iBinder).getService();
            mIsBoundToService = true;
            if (mRecordingService.isRecording()) {
                mFloatingActionButton.setImageResource(R.drawable.ic_videocam_off_white_36dp);
            } else {
                mFloatingActionButton.setImageResource(R.drawable.ic_videocam_white_36dp);
            }
            mFloatingActionButton.setVisibility(View.VISIBLE);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mIsBoundToService = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mdrecord);

        initMainActivity();
    }

    public void initMainActivity() {

        startService(new Intent(this, RecordingService.class));

        mToolBar = (Toolbar) findViewById(R.id.md_toolbar);
        mToolBar.setTitle(R.string.toolbar_title);
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
        mainViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager(), tabTitles, numberOfTabs);
        mViewPager.setAdapter(mainViewPagerAdapter);
        mTabs.setViewPager(mViewPager);

        mFloatingActionButton = (ImageButton) findViewById(R.id.main_fab);
        mFloatingActionButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_fab: {
                if (recordButtonResId == -1) {
                    if (mSelectedGame != null) {
                        mFloatingActionButton.setImageResource(R.drawable.ic_videocam_off_white_36dp);
                        obtainMediaProjection();
                        recordButtonResId = 0;
                        break;
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.select_a_game, Toast.LENGTH_SHORT).show();
                        break;
                    }
                } else {
                    ((ImageButton) v).setImageResource(R.drawable.ic_videocam_white_36dp);
                    mRecordingService.stopRecording();
                    recordButtonResId = -1;
                    showUploadFragment();
                }
            }
        }
    }

    public void showUploadFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = RecordShareFragment.newInstance();
        fragmentTransaction.replace(R.id.main_activity_layout, fragment, "tag")
                .addToBackStack("tag")
                .commit();
    }
    @Override
    public void selectedGame(Game gameModel) {
        mSelectedGame = gameModel;
        Log.d("Selected game:", " " + mSelectedGame.name);
    }

    public void obtainMediaProjection() {
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), MEDIA_PROJECTION_MANAGER_PERMISSION_CODE);
    }

    @Override
    public void onResume() {
        super.onResume();
        bindService(new Intent(this, RecordingService.class), mConnection, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mIsBoundToService) {
            unbindService(mConnection);
            mIsBoundToService = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == MEDIA_PROJECTION_MANAGER_PERMISSION_CODE) {
            if (mMediaProjectionManager != null && mSelectedGame != null) {
                try {
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(mSelectedGame.play_store_id);
                    Log.d("start Activity", "yoyo");
                    startActivity(launchIntent);
                    MediaProjection projection = mMediaProjectionManager.getMediaProjection(resultCode, data);
                    mRecordingService.startRecording(projection, mSelectedGame);
                } catch (ActivityNotFoundException e) {
                    // TODO: show the user something about not finding the game.
                }
            } else {
                Log.w("Kamcord", "Unable to start recording because reasons.");
            }
        }
    }
}
