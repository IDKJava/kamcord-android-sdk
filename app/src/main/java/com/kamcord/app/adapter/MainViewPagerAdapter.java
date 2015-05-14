package com.kamcord.app.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.kamcord.app.fragment.RecordFragment;
import com.kamcord.app.fragment.WebFragment;


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
        Fragment fragment;

        switch(position) {
            case 0:
                fragment = new RecordFragment();
                break;

            case 1:
                fragment = new WebFragment();

                Bundle args = new Bundle();
                String url = "https://www.kamcord.com/";
                // TODO: append profile/{username} to the url if the user is logged in.
                args.putString(WebFragment.URL, url);
                args.putBoolean(WebFragment.RESTRICT_DOMAIN, true);
                fragment.setArguments(args);
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

    @Override
    public CharSequence getPageTitle(int position) {
        return  tabTitles[position];
    }
}
