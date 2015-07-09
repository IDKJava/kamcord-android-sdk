package com.kamcord.app.adapter;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.kamcord.app.R;
import com.kamcord.app.fragment.HomeFragment;
import com.kamcord.app.fragment.ProfileFragment;
import com.kamcord.app.fragment.RecordFragment;


public class MainViewPagerAdapter extends FragmentStatePagerAdapter {

    public static final int HOME_FRAGMENT_POSITION = 0;
    public static final int RECORD_FRAGMENT_POSITION = 1;
    public static final int PROFILE_FRAGMENT_POSITION = 2;
    private int numberOfTabs;
    private CharSequence tabTitles[];

    private int[] imageResId = {
            R.drawable.tabicon_home_selector,
            R.drawable.tabicon_record_selector,
            R.drawable.tabicon_profile_selector};

    public MainViewPagerAdapter(FragmentManager fm, CharSequence tabTitles[], int numberOfTabs) {
        super(fm);
        this.numberOfTabs = numberOfTabs;
        this.tabTitles = tabTitles;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && position >= RECORD_FRAGMENT_POSITION)
            position++;
        switch (position) {
            case HOME_FRAGMENT_POSITION:
                fragment = new HomeFragment();
                break;

            case RECORD_FRAGMENT_POSITION:
                fragment = new RecordFragment();
                break;

            case PROFILE_FRAGMENT_POSITION:
                fragment = new ProfileFragment();
                break;

            default:
                fragment = new HomeFragment();
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }

    public int getDrawableId(int position) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && position >= RECORD_FRAGMENT_POSITION)
            position++;
        return imageResId[position];
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
