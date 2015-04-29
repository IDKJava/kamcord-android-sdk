package com.kamcord.app.kamcord.activity.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.kamcord.app.kamcord.R;
import com.kamcord.app.kamcord.activity.model.GameModel;
import com.kamcord.app.kamcord.activity.service.RecordingService;
import com.kamcord.app.kamcord.activity.utils.FileManagement;
import com.kamcord.app.kamcord.activity.utils.GameRecordListAdapter;
import com.kamcord.app.kamcord.activity.utils.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.List;


public class RecordActivity extends Activity implements View.OnClickListener, GameRecordListAdapter.OnItemClickListener {

    private Button serviceStartButton;
    private FileManagement fileManagement;
    private String mGameName;
    private String gameFolderString;
    private String launchPackageName;
    private boolean buttonClicked = false;

    private Intent serviceIntent;

    private RecyclerView mRecyclerView;
    private GameRecordListAdapter mRecyclerAdapter;
    private ArrayList<GameModel> packageGameList;

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

        fileManagement = new FileManagement();
        fileManagement.rootFolderInitialize();

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
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_margin);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(spacingInPixels));
        mRecyclerAdapter = new GameRecordListAdapter(this, packageGameList);
        mRecyclerAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        serviceIntent = new Intent(RecordActivity.this, RecordingService.class);

//        getInstalledGameList();
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
        launchPackageName = packageGameList.get(position).getPackageName();
        fileManagement.gameFolderInitialize(launchPackageName);
        mGameName = fileManagement.getGameName();
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

    public void startRecordingService() {
        buttonClicked = true;
        serviceStartButton.setText("Stop");
        fileManagement.sessionFolderInitialize();

        gameFolderString = mGameName + "/" + fileManagement.getUUIDString();
        serviceIntent.putExtra("RecordFlag", true);
        serviceIntent.putExtra("GameFolder", gameFolderString);
        serviceIntent.putExtra("PackageName", launchPackageName);
        startService(serviceIntent);
    }

    public void stopRecordingService() {
        buttonClicked = false;
        serviceStartButton.setText("Record");
        serviceIntent.putExtra("RecordFlag", false);
        stopService(serviceIntent);
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
