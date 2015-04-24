package com.kamcord.app.kamcord.activity.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.kamcord.app.kamcord.R;
import com.kamcord.app.kamcord.activity.application.KamcordApplication;
import com.kamcord.app.kamcord.activity.service.RecordingService;

import java.io.File;


public class RecordActivity extends Activity implements View.OnClickListener {

    private static final int RECORD_FLAG = 2;

    private MediaProjection mMediaProjection;

    private Button serviceStartButton;

    private File SDCard_Path;
    private File VideoFolder;
    private String VideoFolderPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        initKamcord();

    }

    public void initKamcord() {

        serviceStartButton = (Button) findViewById(R.id.servicestart_button);
        serviceStartButton.setOnClickListener(this);

        // SD Card Check and Folder Initialize
        SDCard_Path = Environment.getExternalStorageDirectory();
        VideoFolderPath = SDCard_Path.getParent() + "/" + SDCard_Path.getName() + "/" + "/Kamcord_Android/";
        VideoFolder = new File(VideoFolderPath);
        if (!VideoFolder.exists() || VideoFolder.isDirectory()) {
            VideoFolder.mkdir();
        }

        // App package name with permission(Should be a list)
        appInstalledOrNot("jp.co.ponos.battlecatsen");

    }

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
