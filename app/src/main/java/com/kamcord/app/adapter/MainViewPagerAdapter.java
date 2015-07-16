package com.kamcord.app.adapter;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.kamcord.app.R;
import com.kamcord.app.fragment.HomeFragment;
import com.kamcord.app.fragment.ProfileFragment;
import com.kamcord.app.fragment.RecordFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainViewPagerAdapter extends FragmentStatePagerAdapter {

    public static final int HOME_FRAGMENT_POSITION = 0;
    public static final int RECORD_FRAGMENT_POSITION =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 1 : -1;
    public static final int PROFILE_FRAGMENT_POSITION =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 2 : 1;
    private int numberOfTabs;
    private CharSequence tabTitles[];
    public HashMap<Integer, Fragment> viewPagerHashMap = new HashMap<>();
    private List<Integer> imageResId = new ArrayList<>();


    public MainViewPagerAdapter(FragmentManager fm, CharSequence tabTitles[], int numberOfTabs) {
        super(fm);
        this.numberOfTabs = numberOfTabs;
        this.tabTitles = tabTitles;
        initSlideTabIcon();
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;

        if (position == HOME_FRAGMENT_POSITION) {
            fragment = new HomeFragment();
            viewPagerHashMap.put(position, fragment);
        } else if (position == RECORD_FRAGMENT_POSITION) {
            fragment = new RecordFragment();
            viewPagerHashMap.put(position, fragment);
        } else if (position == PROFILE_FRAGMENT_POSITION) {
            fragment = new ProfileFragment();
            viewPagerHashMap.put(position, fragment);
        } else {
            fragment = new HomeFragment();
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }

    public int getDrawableId(int position) {
        return imageResId.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    public HashMap<Integer, Fragment> getViewPagerHashMap() {
        return this.viewPagerHashMap;
    }

    private void initSlideTabIcon() {
        imageResId.add(R.drawable.tabicon_home_selector);
        imageResId.add(R.drawable.tabicon_profile_selector);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageResId.add(1, R.drawable.tabicon_record_selector);
        }
    }
}
