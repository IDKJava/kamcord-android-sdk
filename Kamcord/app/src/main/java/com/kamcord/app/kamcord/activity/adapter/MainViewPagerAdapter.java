package com.kamcord.app.kamcord.activity.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.kamcord.app.kamcord.activity.fragment.ProfileFragment;
import com.kamcord.app.kamcord.activity.fragment.RecordFragment;


public class MainViewPagerAdapter extends FragmentStatePagerAdapter {

    private int numberOfTabs;
    private CharSequence tabTitles[];

    public MainViewPagerAdapter(FragmentManager fm,CharSequence tabTitles[], int numberOfTabs) {
        super(fm);
        this.numberOfTabs = numberOfTabs;
        this.tabTitles = tabTitles;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                return new RecordFragment();
            case 1:
                return new ProfileFragment();
        }
        return new RecordFragment();
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return  tabTitles[position];
    }
}
