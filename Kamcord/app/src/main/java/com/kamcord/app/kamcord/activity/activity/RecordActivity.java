package com.kamcord.app.kamcord.activity.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.kamcord.app.kamcord.R;
import com.kamcord.app.kamcord.activity.ScreenRecorder;
import com.kamcord.app.kamcord.activity.service.RecordingService;

import java.io.IOException;
import java.nio.ByteBuffer;


public class RecordActivity extends Activity implements View.OnClickListener{

    private static final int PERMISSION_CODE = 1;

    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private Surface mSurface;
    private SurfaceView mSurfaceView;

    private Button recordButton;
    private Button serviceStartButton;
    private Button serviceStopButton;
    private ImageButton mImageButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        // Recording Initialization
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        recordButton = (Button) findViewById(R.id.record_button);
        serviceStartButton = (Button) findViewById(R.id.servicestart_button);
        serviceStopButton = (Button) findViewById(R.id.servicestop_button);
        mImageButton = (ImageButton) findViewById(R.id.imageButton);

        recordButton.setOnClickListener(this);
        serviceStartButton.setOnClickListener(this);
        serviceStopButton.setOnClickListener(this);
        mImageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Pressed", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.servicestart_button: {
                Intent startServiceIntent = new Intent(RecordActivity.this, RecordingService.class);
                startService(startServiceIntent);
                break;
            }
            case R.id.servicestop_button:
                Intent stopServiceIntent = new Intent(RecordActivity.this, RecordingService.class);
                stopService(stopServiceIntent);
                break;
            case R.id.record_button:
                // do something about recording
                Intent startServiceIntent = new Intent(RecordActivity.this, RecordingService.class);
                startService(startServiceIntent);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(mMediaProjection != null) {
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
