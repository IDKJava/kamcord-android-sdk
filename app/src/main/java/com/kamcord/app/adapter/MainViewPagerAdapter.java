package com.kamcord.app.adapter;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.kamcord.app.R;
import com.kamcord.app.fragment.ProfileFragment;
import com.kamcord.app.fragment.RecordFragment;

import java.util.HashMap;


public class MainViewPagerAdapter extends FragmentStatePagerAdapter {

    private static final HashMap<Integer, Class<?>> POSITION_FRAGMENT_MAP = new HashMap<Integer, Class<?>>() {{
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            put(0, RecordFragment.class);
            put(1, ProfileFragment.class);
        } else {
            put(0, ProfileFragment.class);
        }
    }};

    private static final HashMap<Integer, Integer> POSITION_DRAWABLE_MAP = new HashMap<Integer, Integer>() {{
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            put(0, R.drawable.tabicon_record_selector);
            put(1, R.drawable.tabicon_profile_selector);
        } else {
            put(0, R.drawable.tabicon_profile_selector);
        }
    }};


    public static final int RECORD_FRAGMENT_POSITION = 0;
    public static final int PROFILE_FRAGMENT_POSITION = 1;
    public static final int NUMBERS_OF_TAB = 2;
    private int numberOfTabs;
    private CharSequence tabTitles[];

    public MainViewPagerAdapter(FragmentManager fm, CharSequence tabTitles[], int numberOfTabs) {
        super(fm);
        this.numberOfTabs = numberOfTabs;
        this.tabTitles = tabTitles;
    }

    @Override
    public Fragment getItem(int position) {

        Fragment fragment;
        Class fragmentClass = POSITION_FRAGMENT_MAP.get(position);
        if( fragmentClass != null && fragmentClass.equals(RecordFragment.class) ) {
            fragment = new RecordFragment();
        } else if( fragmentClass != null && fragmentClass.equals(ProfileFragment.class) ) {
            fragment = new ProfileFragment();
        } else {
            fragment = new RecordFragment();
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }

    public int getDrawableId(int position) {
        if(POSITION_DRAWABLE_MAP.get(position) != null) {
            return POSITION_DRAWABLE_MAP.get(position);
        }
        return POSITION_DRAWABLE_MAP.get(0);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
