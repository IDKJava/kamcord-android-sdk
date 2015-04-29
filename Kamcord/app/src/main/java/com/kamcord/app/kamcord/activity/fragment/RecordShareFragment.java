package com.kamcord.app.kamcord.activity.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kamcord.app.kamcord.R;

public class RecordShareFragment extends DialogFragment implements View.OnClickListener {

    public static RecordShareFragment newInstance() {
        RecordShareFragment recordShareFragment = new RecordShareFragment();
        return recordShareFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recordshare, container, false);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {

    }
}
