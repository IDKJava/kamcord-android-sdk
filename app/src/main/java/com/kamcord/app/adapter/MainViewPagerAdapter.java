package com.kamcord.app.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.kamcord.app.R;
import com.kamcord.app.fragment.ProfileFragment;
import com.kamcord.app.fragment.RecordFragment;
import com.kamcord.app.fragment.WatchFragment;


public class MainViewPagerAdapter extends FragmentStatePagerAdapter {

    private int numberOfTabs;
    private CharSequence tabTitles[];

    private int[] imageResId = {
            R.drawable.ic_videocam_grey600_18dp,
            R.drawable.ic_person_grey600_18dp,
            R.drawable.ic_video_collection_grey600_18dp
    };

    public MainViewPagerAdapter(FragmentManager fm, CharSequence tabTitles[], int numberOfTabs) {
        super(fm);
        this.numberOfTabs = numberOfTabs;
        this.tabTitles = tabTitles;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new RecordFragment();
            case 1:
                return new ProfileFragment();
            case 2:
                return new WatchFragment();
        }
        return new RecordFragment();
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        return tabTitles[position];
//        Drawable image = mContext.getResources().getDrawable(imageResId[position]);
//        image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
//        SpannableString sb = new SpannableString(" ");
//        ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
//        sb.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        return sb;
    }
}
