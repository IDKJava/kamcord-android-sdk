package com.kamcord.app.kamcord.activity.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kamcord.app.kamcord.R;
import com.kamcord.app.kamcord.activity.activity.RecordActivity;

public class RecordShareFragment extends DialogFragment implements View.OnClickListener {

    private ImageView thumbNailImageView;

    public static RecordShareFragment newInstance() {
        RecordShareFragment recordShareFragment = new RecordShareFragment();
        return recordShareFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        RecordActivity mRecordActivity = (RecordActivity) getActivity();
        String thumbNailPath = mRecordActivity.getVideoThumbnail();
        View v = inflater.inflate(R.layout.fragment_recordshare, container, false);
        thumbNailImageView = (ImageView) v.findViewById(R.id.videothumbnail_imageview);
        Bitmap bitmap = BitmapFactory.decodeFile(thumbNailPath);
        thumbNailImageView.setImageBitmap(bitmap);
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
