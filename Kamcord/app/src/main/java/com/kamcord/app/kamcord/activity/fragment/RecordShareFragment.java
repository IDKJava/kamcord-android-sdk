package com.kamcord.app.kamcord.activity.fragment;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kamcord.app.kamcord.R;

public class RecordShareFragment extends Fragment implements View.OnClickListener {

    private ImageView thumbNailImageView;

    public static RecordShareFragment newInstance() {
        RecordShareFragment recordShareFragment = new RecordShareFragment();
        return recordShareFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        RecordActivity mRecordActivity = (RecordActivity) getActivity();
//        String thumbNailPath = mRecordActivity.getVideoThumbnail();

        View v = inflater.inflate(R.layout.fragment_recordshare, container, false);
        v.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
//        thumbNailImageView = (ImageView) v.findViewById(R.id.videothumbnail_imageview);
//        thumbNailImageView.setImageBitmap(getVideoThumbnail(thumbNailPath));
        return v;
    }

    public Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(2000000);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {

    }
}
