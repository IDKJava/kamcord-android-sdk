package com.kamcord.app.kamcord.activity.fragment;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.kamcord.app.kamcord.R;
import com.kamcord.app.kamcord.activity.adapter.GameRecordListAdapter;
import com.kamcord.app.kamcord.activity.model.GameModel;
import com.kamcord.app.kamcord.activity.service.RecordingService;
import com.kamcord.app.kamcord.activity.utils.FileManagement;
import com.kamcord.app.kamcord.activity.utils.SpaceItemDecoration;
import com.kamcord.app.kamcord.activity.utils.Utils;

import java.util.ArrayList;

public class RecordFragment extends Fragment implements View.OnClickListener, GameRecordListAdapter.OnItemClickListener {

    private static final int MEDIA_PROJECTION_MANAGER_PERMISSION_CODE = 1;

    private RecyclerView mRecyclerView;
    private GameRecordListAdapter mRecyclerAdapter;
    private Button mServiceStartButton;
    private Button mRecordViewButton;

    private MediaProjectionManager mMediaProjectionManager;
    private Context mContext;
    private GameModel mSelectedGame = null;
    private RecordingService mRecordingService;
    private boolean mIsBoundToService = false;
    private FileManagement mFileManagement;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mRecordingService = ((RecordingService.LocalBinder) iBinder).getService();
            mIsBoundToService = true;
            if (mRecordingService.isRecording()) {
                mServiceStartButton.setText(R.string.stop_recording);
            } else {
                mServiceStartButton.setText(R.string.start_recording);
            }
            mServiceStartButton.setVisibility(View.VISIBLE);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mIsBoundToService = false;
        }
    };

    private ArrayList<GameModel> mSupportedGameList = new ArrayList<GameModel>() {
        {
            add(new GameModel("jp.ne.mkb.games.BOOSTBEAST", "jp.ne.mkb.games.BOOSTBEAST", R.drawable.boost_beast));
            add(new GameModel("com.rovio.BadPiggies", "com.rovio.BadPiggies", R.drawable.bad_piggies));
            add(new GameModel("com.yodo1.crossyroad", "com.yodo1.crossyroad", R.drawable.crossy_road));
            add(new GameModel("com.fingersoft.hillclimb", "com.fingersoft.hillclimb", R.drawable.hill_racing));
            add(new GameModel("com.madfingergames.deadtrigger2", "com.madfingergames.deadtrigger2", R.drawable.dead_trigger));
            add(new GameModel("com.kabam.marvelbattle", "com.kabam.marvelbattle", R.drawable.marvel));
        }
    };

    private String thumbnailString;
    private CustomReceiver mBroadcastReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.record_tab, container, false);

        mContext = getActivity().getApplicationContext();
        initKamcordRecordFragment(v);
        mContext.startService(new Intent(mContext, RecordingService.class));
        return v;
    }

    public void initKamcordRecordFragment(View v) {

        mServiceStartButton = (Button) v.findViewById(R.id.servicestart_button);
        mServiceStartButton.setOnClickListener(this);
        mServiceStartButton.setVisibility(View.INVISIBLE);

        mRecordViewButton = (Button) v.findViewById(R.id.recordview_button);
        mRecordViewButton.setOnClickListener(this);

        mFileManagement = new FileManagement();
        mFileManagement.rootFolderInitialize();

        // gridview init;
        mRecyclerView = (RecyclerView) v.findViewById(R.id.record_recyclerview);
        int paddingTop = Utils.getToolbarHeight(mContext) + Utils.getTabsHeight(mContext);
        mRecyclerView.setPadding(mRecyclerView.getPaddingLeft(), paddingTop, mRecyclerView.getPaddingRight(), mRecyclerView.getPaddingBottom());

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        int SpacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_margin);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(SpacingInPixels));
        mRecyclerAdapter = new GameRecordListAdapter(mContext, mSupportedGameList);
        mRecyclerAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        mBroadcastReceiver = new CustomReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.kamcord.RecordService");
        mContext.registerReceiver(mBroadcastReceiver, filter);

        if (mMediaProjectionManager != null && mSelectedGame != null) {
            try {
                Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(mSelectedGame.getPackageName());
                startActivity(launchIntent);

//                MediaProjection projection = mMediaProjectionManager.getMediaProjection(resultCode, data);
//                mRecordingService.startRecording(projection, mSelectedGame);
            } catch (ActivityNotFoundException e) {
                // TODO: show the user something about not finding the game.
            }
        } else {
            Log.w("Kamcord", "Unable to start recording because reasons.");
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        // Package for Launch Game
        mSelectedGame = mSupportedGameList.get(position);
        Toast.makeText(mContext,
                "You will record " + mSelectedGame.getPackageName(),
                Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.servicestart_button: {
                if (((Button) v).getText().equals(getResources().getString(R.string.start_recording))) {
                    if (mSelectedGame != null) {
                        obtainMediaProjection();
                        break;
                    } else {
                        Toast.makeText(mContext, R.string.select_a_game, Toast.LENGTH_SHORT).show();
                        break;
                    }
                } else if (((Button) v).getText().equals(getResources().getString(R.string.stop_recording))) {
                    ((Button) v).setText(R.string.start_recording);
                    mRecordingService.stopRecording();
                    showShareFragment();
                }
            }
            case R.id.recordview_button: {
                showShareFragment();
            }
        }
    }

    public void obtainMediaProjection() {
        mMediaProjectionManager = (MediaProjectionManager) mContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), MEDIA_PROJECTION_MANAGER_PERMISSION_CODE);
    }

    public void showShareFragment() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Fragment fragment = RecordShareFragment.newInstance();
        fragmentTransaction.add(R.id.activity_recordlayout, fragment, "tag")
                .addToBackStack("tag")
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        mContext.bindService(new Intent(mContext, RecordingService.class), mConnection, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mIsBoundToService) {
            mContext.unbindService(mConnection);
            mIsBoundToService = false;
        }
    }

    public String getVideoThumbnail() {
        return thumbnailString;
    }

    public class CustomReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            thumbnailString = intent.getStringExtra("ThumbNailPath");
        }
    }
}
