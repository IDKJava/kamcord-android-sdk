package com.kamcord.app.kamcord.activity.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.kamcord.app.kamcord.R;
import com.kamcord.app.kamcord.activity.adapter.MainViewPagerAdapter;
import com.kamcord.app.kamcord.activity.utils.SlidingTabLayout;

public class MDRecordActivity extends ActionBarActivity {

    Toolbar mToolBar;
    ImageButton mFAB;
    private ViewPager mViewPager;
    private MainViewPagerAdapter mainViewPagerAdapter;
    private SlidingTabLayout mTabs;
    private CharSequence tabTitles[] = {"Record", "Profile"};
    private int numberOfTabs = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mdrecord);

        initMainActivity();
    }

    public void initMainActivity() {

        mToolBar = (Toolbar) findViewById(R.id.md_toolbar);
        mToolBar.setTitle(getResources().getString(R.string.kamcord));
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

        mFAB = (ImageButton) findViewById(R.id.main_fab);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Start Recording", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mdrecord, menu);
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
