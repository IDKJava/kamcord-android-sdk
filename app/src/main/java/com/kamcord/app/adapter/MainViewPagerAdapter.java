package com.kamcord.app.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.kamcord.app.R;
import com.kamcord.app.fragment.ProfileFragment;
import com.kamcord.app.fragment.RecordFragment;


public class MainViewPagerAdapter extends FragmentStatePagerAdapter {

    public static final int RECORD_FRAGMENT_POSITION = 0;
    public static final int PROFILE_FRAGMENT_POSITION = 1;
    public static final int NUMBERS_OF_TAB = 2;
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
        if (numberOfTabs >= NUMBERS_OF_TAB) {
            switch (position) {
                case RECORD_FRAGMENT_POSITION:
                    fragment = new RecordFragment();
                    break;

                case PROFILE_FRAGMENT_POSITION:
                    fragment = new ProfileFragment();
                    break;

                default:
                    fragment = new RecordFragment();
                    break;
            }
            return fragment;
        } else {
            switch (position) {
                case PROFILE_FRAGMENT_POSITION:
                    fragment = new ProfileFragment();
                    break;

                default:
                    fragment = new ProfileFragment();
                    break;
            }
            return fragment;
        }
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
