package com.kamcord.app.kamcord.activity.activity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
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
import com.kamcord.app.kamcord.activity.utils.StitchClipsThread;

import java.util.ArrayList;
import java.util.List;


public class RecordActivity extends FragmentActivity implements View.OnClickListener, GameRecordListAdapter.OnItemClickListener {

    private Button ServiceStartButton;
    private FileManagement mFileManagement;
    private String mGameName;
    private String GameFolderString;
    private String LaunchPackageName;
    private boolean ButtonClicked = false;

    private Intent ServiceIntent;

    private RecyclerView mRecyclerView;
    private GameRecordListAdapter mRecyclerAdapter;
    private ArrayList<GameModel> PackageGameList;

    private String[] PackageNameArray = new String[]{
            "com.rovio.BadPiggies",
            "com.yodo1.crossyroad",
            "com.madfingergames.deadtrigger2",
            "com.fingersoft.hillclimb",
            "com.kabam.marvelbattle",
    };
    private Integer[] GameDrawablArray = new Integer[]{
            R.drawable.bad_piggies,
            R.drawable.crossy_road,
            R.drawable.dead_trigger,
            R.drawable.hill_racing,
            R.drawable.marvel,
    };

    private Button MergeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        initKamcord();

    }

    public void initKamcord() {

        ServiceStartButton = (Button) findViewById(R.id.servicestart_button);
        ServiceStartButton.setOnClickListener(this);

        mFileManagement = new FileManagement();
        mFileManagement.rootFolderInitialize();

        PackageGameList = new ArrayList<GameModel>();
        for (int i = 0; i < PackageNameArray.length; i++) {
            GameModel gameModel = new GameModel();
            gameModel.setPackageName(PackageNameArray[i]);
            gameModel.setGameName(gameModel.getPackageName());
            gameModel.setDrawableID(GameDrawablArray[i]);
            PackageGameList.add(gameModel);
        }

        // gridview init;
        mRecyclerView = (RecyclerView) findViewById(R.id.record_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        int SpacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_margin);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(SpacingInPixels));
        mRecyclerAdapter = new GameRecordListAdapter(this, PackageGameList);
        mRecyclerAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        ServiceIntent = new Intent(RecordActivity.this, RecordingService.class);

        MergeButton = (Button) findViewById(R.id.merge_button);
        MergeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "hey", Toast.LENGTH_SHORT).show();

                ArrayList<String> arrayList = new ArrayList<String>();
                arrayList.add(Environment.getExternalStorageDirectory().getParent() + "/" + Environment.getExternalStorageDirectory().getName() + "/Kamcord_Android/clip1.mp4");
                arrayList.add(Environment.getExternalStorageDirectory().getParent() + "/" + Environment.getExternalStorageDirectory().getName() + "/Kamcord_Android/clip2.mp4");
                arrayList.add(Environment.getExternalStorageDirectory().getParent() + "/" + Environment.getExternalStorageDirectory().getName() + "/Kamcord_Android/clip3.mp4");
                StitchClipsThread stitchClipsThread = new StitchClipsThread(arrayList);
                stitchClipsThread.start();
            }
        });
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
        for(ApplicationInfo app : applicationInfoList) {
            if((app.flags & ApplicationInfo.FLAG_IS_GAME) == ApplicationInfo.FLAG_IS_GAME){
                installedGameList.add(app);
                Log.d("Game Installed: ", (String)packageManager.getApplicationLabel(app));
            }
        }
    }

    @Override
    public void onItemClick(View view, int position) {

        // Package for Launch Game
        LaunchPackageName = PackageGameList.get(position).getPackageName();
        mFileManagement.gameFolderInitialize(LaunchPackageName);
        mGameName = mFileManagement.getGameName();
        Toast.makeText(getApplicationContext(),
                "You will record " + LaunchPackageName.substring(LaunchPackageName.lastIndexOf(".") + 1),
                Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.servicestart_button: {
                if (!ButtonClicked) {
                    if (mGameName != null) {
                        startRecordingService();
                        break;
                    } else {
                        Toast.makeText(getApplicationContext(), "Please select a game to record.", Toast.LENGTH_SHORT).show();
                        break;
                    }
                } else {
                    stopRecordingService();
//                    showShareFragment();
                    break;
                }
            }
        }
    }

    public void showShareFragment() {
//        ServiceStartButton.setVisibility(View.GONE);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = RecordShareFragment.newInstance();
        fragmentTransaction.add(R.id.activity_recordlayout, fragment, "tag")
                .addToBackStack("tag")
                .commit();
    }

    public void startRecordingService() {
        ButtonClicked = true;
        ServiceStartButton.setText("Stop");
        mFileManagement.sessionFolderInitialize();

        GameFolderString = mGameName + "/" + mFileManagement.getUUIDString();
        ServiceIntent.putExtra("RecordFlag", true);
        ServiceIntent.putExtra("GameFolder", GameFolderString);
        ServiceIntent.putExtra("PackageName", LaunchPackageName);
        startService(ServiceIntent);
    }

    public void stopRecordingService() {
        ButtonClicked = false;
        ServiceStartButton.setText("Record");
        ServiceIntent.putExtra("RecordFlag", false);
        stopService(ServiceIntent);
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
