package com.kamcord.app.kamcord.activity.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.kamcord.app.kamcord.R;
import com.kamcord.app.kamcord.activity.Model.GameModel;
import com.kamcord.app.kamcord.activity.application.KamcordApplication;
import com.kamcord.app.kamcord.activity.service.RecordingService;
import com.kamcord.app.kamcord.activity.utils.CustomRecyclerAdapter;
import com.kamcord.app.kamcord.activity.utils.SpaceItemDecoration;

import java.io.File;
import java.util.ArrayList;


public class RecordActivity extends Activity implements View.OnClickListener, CustomRecyclerAdapter.OnItemClickListener {

    private static final int RECORD_FLAG = 2;

    private MediaProjection mMediaProjection;

    private Button serviceStartButton;

    private File SDCard_Path;
    private File VideoFolder;
    private String VideoFolderPath;
    private String rootFolder;
    private String GamePath;
    private File GameFolder;

    private RecyclerView mRecyclerView;
    private CustomRecyclerAdapter mRecyclerAdapter;
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

        // sd card check and folder initialize
        if (rootFolder == null) {
            rootFolder = "/Kamcord_Android/";
        }
        SDCard_Path = Environment.getExternalStorageDirectory();
        VideoFolderPath = SDCard_Path.getParent() + "/" + SDCard_Path.getName() + "/" + rootFolder;
        VideoFolder = new File(VideoFolderPath);
        if (!VideoFolder.exists() || VideoFolder.isDirectory()) {
            VideoFolder.mkdir();
        }

        packageGameList = new ArrayList<GameModel>();
        for (int i = 0; i < packageNameArray.length; i++) {
            GameModel gameModel = new GameModel();
            gameModel.setPackageName(packageNameArray[i]);
            gameModel.setDrawableID(gameDrawablArray[i]);
            packageGameList.add(gameModel);
        }

        // gridview init;
        mRecyclerView = (RecyclerView) findViewById(R.id.record_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_margin);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(spacingInPixels));
        mRecyclerAdapter = new CustomRecyclerAdapter(this, packageGameList);
        mRecyclerAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mRecyclerAdapter);

    }

    // Not being used in this moment
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

    @Override
    public void onItemClick(View view, int position) {
        ((KamcordApplication) this.getApplication()).setSelectedPackageName(packageGameList.get(position).getPackageName());
        String toastString = ((KamcordApplication) this.getApplication()).getSelectedPackageName();
        String gameName = toastString.substring(toastString.lastIndexOf(".") + 1);
        Toast.makeText(getApplicationContext(),
                "You will record " + toastString.substring(toastString.lastIndexOf(".") + 1),
                Toast.LENGTH_SHORT)
                .show();
        ((KamcordApplication) this.getApplication()).setGameFolderString(gameName);
        GamePath = SDCard_Path.getParent() + "/" + SDCard_Path.getName() + "/" + rootFolder + "/" + gameName;
        GameFolder = new File(GamePath);
        if (!GameFolder.exists() || GameFolder.isDirectory()) {
            GameFolder.mkdir();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.servicestart_button: {
                if (!((KamcordApplication) this.getApplication()).getRecordFlag()) {
                    ((KamcordApplication) this.getApplication()).setRecordFlag(true);
                    serviceStartButton.setText("Pause");
                    Intent startServiceIntent = new Intent(RecordActivity.this, RecordingService.class);
                    startService(startServiceIntent);

                    break;
                } else {
                    ((KamcordApplication) this.getApplication()).setRecordFlag(false);
                    serviceStartButton.setText("Record");
                    Intent stopServiceIntent = new Intent(RecordActivity.this, RecordingService.class);
                    stopService(stopServiceIntent);
                    break;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mMediaProjection != null) {
                mMediaProjection.stop();
                mMediaProjection = null;
            }
        }
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
