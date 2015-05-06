package com.kamcord.app.kamcord.activity.activity;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.kamcord.app.kamcord.R;
import com.kamcord.app.kamcord.activity.fragment.RecordShareFragment;
import com.kamcord.app.kamcord.activity.model.GameModel;
import com.kamcord.app.kamcord.activity.service.RecordingService;
import com.kamcord.app.kamcord.activity.utils.FileManagement;
import com.kamcord.app.kamcord.activity.utils.GameRecordListAdapter;
import com.kamcord.app.kamcord.activity.utils.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.List;


public class RecordActivity extends FragmentActivity implements View.OnClickListener, GameRecordListAdapter.OnItemClickListener
{
    private static final int MEDIA_PROJECTION_MANAGER_PERMISSION_CODE = 1;

    private Button mServiceStartButton;
    private FileManagement mFileManagement;
    private boolean mButtonClicked = false;

    private ArrayList<GameModel> mSupportedGameList = new ArrayList<GameModel>()
    {
        {
            add(new GameModel("com.rovio.BadPiggies", "com.rovio.BadPiggies", R.drawable.bad_piggies));
            add(new GameModel("com.yodo1.crossyroad", "com.yodo1.crossyroad", R.drawable.crossy_road));
            add(new GameModel("com.madfingergames.deadtrigger2", "com.madfingergames.deadtrigger2", R.drawable.dead_trigger));
            add(new GameModel("com.fingersoft.hillclimb", "com.fingersoft.hillclimb", R.drawable.hill_racing));
            add(new GameModel("com.kabam.marvelbattle", "com.kabam.marvelbattle", R.drawable.marvel));
        }
    };
    private GameModel mSelectedGame = null;

    private RecordingService mRecordingService;
    private ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            mRecordingService = ((RecordingService.LocalBinder) iBinder).getService();
            mIsBoundToService = true;
            if( mRecordingService.isRecording() )
            {
                mServiceStartButton.setText(R.string.stop_recording);
            } else
            {
                mServiceStartButton.setText(R.string.start_recording);
            }
            mServiceStartButton.setVisibility(View.VISIBLE);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            mIsBoundToService = false;
        }
    };
    private boolean mIsBoundToService = false;

    private MediaProjectionManager mMediaProjectionManager;

    private RecyclerView mRecyclerView;
    private GameRecordListAdapter mRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        initKamcord();

        startService(new Intent(this, RecordingService.class));
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        bindService(new Intent(this, RecordingService.class), mConnection, 0);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if( mIsBoundToService )
        {
            unbindService(mConnection);
            mIsBoundToService = false;
        }
    }

    public void initKamcord()
    {

        mServiceStartButton = (Button) findViewById(R.id.servicestart_button);
        mServiceStartButton.setOnClickListener(this);
        mServiceStartButton.setVisibility(View.INVISIBLE);

        mFileManagement = new FileManagement();
        mFileManagement.rootFolderInitialize();

        // gridview init;
        mRecyclerView = (RecyclerView) findViewById(R.id.record_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        int SpacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_margin);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(SpacingInPixels));
        mRecyclerAdapter = new GameRecordListAdapter(this, mSupportedGameList);
        mRecyclerAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mRecyclerAdapter);

//        getInstalledGameList();
    }

    // Future use
    private boolean appInstalledOrNot(String uri)
    {
        PackageManager pm = getPackageManager();
        boolean appInstalled;
        try
        {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            appInstalled = true;
        } catch( PackageManager.NameNotFoundException e )
        {
            appInstalled = false;
        }
        return appInstalled;
    }

    public void getInstalledGameList()
    {
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> applicationInfoList = packageManager.getInstalledApplications(0);

        ArrayList<ApplicationInfo> installedGameList = new ArrayList<ApplicationInfo>();
        for( ApplicationInfo app : applicationInfoList )
        {
            if( (app.flags & ApplicationInfo.FLAG_IS_GAME) == ApplicationInfo.FLAG_IS_GAME )
            {
                installedGameList.add(app);
            }
        }
    }

    @Override
    public void onItemClick(View view, int position)
    {
        // Package for Launch Game
        mSelectedGame = mSupportedGameList.get(position);
        Toast.makeText(getApplicationContext(),
                "You will record " + mSelectedGame.getPackageName(),
                Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onClick(View v)
    {
        switch( v.getId() )
        {
            case R.id.servicestart_button:
            {
                if( ((Button) v).getText().equals(getResources().getString(R.string.start_recording)) )
                {
                    if( mSelectedGame != null )
                    {
                        obtainMediaProjection();
                        break;
                    } else
                    {
                        Toast.makeText(getApplicationContext(), R.string.select_a_game, Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                else if( ((Button) v).getText().equals(getResources().getString(R.string.stop_recording)) )
                {
                    mRecordingService.stopRecording();
                }
            }
        }
    }

    public void showShareFragment()
    {
        mServiceStartButton.setVisibility(View.GONE);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = RecordShareFragment.newInstance();
        fragmentTransaction.add(R.id.activity_recordlayout, fragment, "tag")
                .addToBackStack("tag")
                .commit();
    }

    public void obtainMediaProjection()
    {
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), MEDIA_PROJECTION_MANAGER_PERMISSION_CODE);
    }

    /*
    public void stopRecordingService() {

        writeClipFile();
        buttonClicked = false;
        serviceStartButton.setText("Record");
        serviceIntent.putExtra("RecordFlag", false);
        stopService(serviceIntent);
        StitchClipsThread stitchClipsThread = new StitchClipsThread("/sdcard/Kamcord_Android/" + gameFolderString, getApplicationContext());
        stitchClipsThread.start();
    }

    public void writeClipFile() {
        File sessionFile = new File("/sdcard/Kamcord_Android/" + gameFolderString);
        if (sessionFile.exists() && sessionFile.isDirectory()) {
            try {
                FileWriter fileWriter = new FileWriter(sessionFile + "/cliplist.txt", true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                for (final File file : sessionFile.listFiles(new VideoFileFilter())) {
                    bufferedWriter.write("file '" + file.getAbsolutePath() + "'\n");
                }
                bufferedWriter.close();

                FileWriter audioFileWriter = new FileWriter(sessionFile + "/audiolist.txt", true);
                BufferedWriter audioBufferedWriter = new BufferedWriter(audioFileWriter);
                for (final File file : sessionFile.listFiles(new AudioFileFilter())) {
                    audioBufferedWriter.write("file '" + file.getAbsolutePath() + "'\n");
                }
                audioBufferedWriter.close();
            } catch (IOException iox) {
                iox.printStackTrace();
            }
        }
    }
    */


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_record, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if( id == R.id.action_settings )
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if( resultCode == RESULT_OK && requestCode == MEDIA_PROJECTION_MANAGER_PERMISSION_CODE )
        {
            if( mMediaProjectionManager != null && mSelectedGame != null )
            {
                try
                {
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(mSelectedGame.getPackageName());
                    startActivity(launchIntent);

                    MediaProjection projection = mMediaProjectionManager.getMediaProjection(resultCode, data);
                    mRecordingService.startRecording(projection, mSelectedGame);
                }
                catch( ActivityNotFoundException e )
                {
                    // TODO: show the user something about not finding the game.
                }
            }
            else
            {
                Log.w("Kamcord", "Unable to start recording because reasons.");
            }
        }
    }
}
