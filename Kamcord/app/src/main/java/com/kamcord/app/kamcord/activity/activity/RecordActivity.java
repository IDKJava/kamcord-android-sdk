package com.kamcord.app.kamcord.activity.activity;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.kamcord.app.kamcord.R;
import com.kamcord.app.kamcord.activity.adapter.MainViewPagerAdapter;
import com.kamcord.app.kamcord.activity.utils.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;


public class RecordActivity extends ActionBarActivity {

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private MainViewPagerAdapter mainViewPagerAdapter;
    private SlidingTabLayout mTabs;
    private CharSequence tabTitles[] = {"Record", "Profile"};
    private int numberOfTabs = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        initKamcord();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void initKamcord() {

        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("Kamcord");
        mToolbar.setElevation(0);

        mViewPager = (ViewPager) findViewById(R.id.main_pager);
        mainViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager(), tabTitles, numberOfTabs);
        mViewPager.setAdapter(mainViewPagerAdapter);
        mTabs = (SlidingTabLayout) findViewById(R.id.tabs);
        mTabs.setDistributeEvenly(true);
        mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        mTabs.setViewPager(mViewPager);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_record, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
