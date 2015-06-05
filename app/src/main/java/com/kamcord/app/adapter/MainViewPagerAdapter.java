package com.kamcord.app.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.kamcord.app.R;
import com.kamcord.app.fragment.ProfileFragment;
import com.kamcord.app.fragment.RecordFragment;


public class MainViewPagerAdapter extends FragmentStatePagerAdapter {

    private int numberOfTabs;
    private CharSequence tabTitles[];

    private int[] imageResId = {
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
        switch (position) {
            case 0:
                fragment = new RecordFragment();
                break;

            case 1:
                fragment = new ProfileFragment();
                break;

            default:
                fragment = new RecordFragment();
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }

    public int getDrawableId(int position) {
        return imageResId[position];
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
