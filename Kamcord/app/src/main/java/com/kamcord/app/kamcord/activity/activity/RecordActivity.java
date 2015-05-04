package com.kamcord.app.kamcord.activity.activity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.kamcord.app.kamcord.R;
import com.kamcord.app.kamcord.activity.fragment.RecordShareFragment;
import com.kamcord.app.kamcord.activity.model.GameModel;
import com.kamcord.app.kamcord.activity.service.RecordingService;
import com.kamcord.app.kamcord.activity.utils.AudioRecordThread;
import com.kamcord.app.kamcord.activity.utils.FileManagement;
import com.kamcord.app.kamcord.activity.utils.GameRecordListAdapter;
import com.kamcord.app.kamcord.activity.utils.SpaceItemDecoration;
import com.kamcord.app.kamcord.activity.utils.StitchClipsThread;
import com.kamcord.app.kamcord.activity.utils.VideoFileFilter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class RecordActivity extends FragmentActivity implements View.OnClickListener, GameRecordListAdapter.OnItemClickListener {

    private Button serviceStartButton;
    private FileManagement mFileManagement;
    private String mGameName;
    private String gameFolderString;
    private String launchPackageName;
    private boolean buttonClicked = false;

    private Intent serviceIntent;

    private RecyclerView mRecyclerView;
    private GameRecordListAdapter mRecyclerAdapter;
    private ArrayList<GameModel> packageGameList;

    private static AudioRecordThread mAudioRecordThread;

    private String[] packageNameArray = new String[]{
            "com.rovio.BadPiggies",
            "com.yodo1.crossyroad",
            "com.madfingergames.deadtrigger2",
            "com.fingersoft.hillclimb",
            "com.kabam.marvelbattle",
    };
    private Integer[] gameDrawablArray = new Integer[]{
            R.drawable.bad_piggies,
            R.drawable.crossy_road,
            R.drawable.dead_trigger,
            R.drawable.hill_racing,
            R.drawable.marvel,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        initKamcord();

    }

    public void initKamcord() {

        serviceStartButton = (Button) findViewById(R.id.servicestart_button);
        serviceStartButton.setOnClickListener(this);

        mFileManagement = new FileManagement();
        mFileManagement.rootFolderInitialize();

        packageGameList = new ArrayList<GameModel>();
        for (int i = 0; i < packageNameArray.length; i++) {
            GameModel gameModel = new GameModel();
            gameModel.setPackageName(packageNameArray[i]);
            gameModel.setGameName(gameModel.getPackageName());
            gameModel.setDrawableID(gameDrawablArray[i]);
            packageGameList.add(gameModel);
        }

        // gridview init;
        mRecyclerView = (RecyclerView) findViewById(R.id.record_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        int SpacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_margin);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(SpacingInPixels));
        mRecyclerAdapter = new GameRecordListAdapter(this, packageGameList);
        mRecyclerAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        serviceIntent = new Intent(RecordActivity.this, RecordingService.class);

    }

    // Future use
    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        boolean appInstalled;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            appInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            appInstalled = false;
        }
        return appInstalled;
    }

    public void getInstalledGameList() {
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> applicationInfoList = packageManager.getInstalledApplications(0);

        ArrayList<ApplicationInfo> installedGameList = new ArrayList<ApplicationInfo>();
        for (ApplicationInfo app : applicationInfoList) {
            if ((app.flags & ApplicationInfo.FLAG_IS_GAME) == ApplicationInfo.FLAG_IS_GAME) {
                installedGameList.add(app);
            }
        }
    }

    @Override
    public void onItemClick(View view, int position) {

        // Package for Launch Game
        launchPackageName = packageGameList.get(position).getPackageName();
        mFileManagement.gameFolderInitialize(launchPackageName);
        mGameName = mFileManagement.getGameName();
        Toast.makeText(getApplicationContext(),
                "You will record " + launchPackageName.substring(launchPackageName.lastIndexOf(".") + 1),
                Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.servicestart_button: {
                if (!buttonClicked) {
                    if (mGameName != null) {
                        startRecordingService();
                        break;
                    } else {
                        Toast.makeText(getApplicationContext(), "Please select a game to record.", Toast.LENGTH_SHORT).show();
                        break;
                    }
                } else {
                    stopRecordingService();
                    break;
                }
            }
        }
    }

    // Showing the fragment after user stop a session of recording
    public void showShareFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = RecordShareFragment.newInstance();
        fragmentTransaction.add(R.id.activity_recordlayout, fragment, "tag")
                .addToBackStack("tag")
                .commit();
    }

    public void startRecordingService() {

        buttonClicked = true;
        serviceStartButton.setText("Stop");
        mFileManagement.sessionFolderInitialize();
        gameFolderString = mGameName + "/" + mFileManagement.getUUIDString() + "/";
        serviceIntent.putExtra("RecordFlag", true);
        serviceIntent.putExtra("GameFolder", gameFolderString);
        serviceIntent.putExtra("PackageName", launchPackageName);
        startService(serviceIntent);
    }

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
            } catch (IOException iox) {
                iox.printStackTrace();
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_record, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
