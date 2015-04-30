package com.kamcord.app.kamcord.activity.utils;

import android.os.Environment;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by donliang1 on 4/29/15.
 */
public class StitchClipsThread extends Thread {

    private MovieCreator mMovieCreator;
    private String ResultPath;
    private ArrayList<String> inputVideoClips;

    public StitchClipsThread(ArrayList<String> inputVideos) {
        this.inputVideoClips = inputVideos;
    }

    @Override
    public void run() {
        mMovieCreator = new MovieCreator();
        ResultPath = Environment.getExternalStorageDirectory().getParent() + "/" + Environment.getExternalStorageDirectory().getName() + "/Kamcord_Video";
        File ResultFolder = new File(ResultPath);
        if (!ResultFolder.exists() || !ResultFolder.isDirectory()) {
            ResultFolder.mkdir();
        }
        startStitching(inputVideoClips);
    }

    public void startStitching(ArrayList<String> inputVideos) {
        try {

            int clipsLength = inputVideos.size();
            Movie[] clips = new Movie[clipsLength];
            List<Track> videoTracks = new LinkedList<>();

            for (int i = 0; i < clipsLength; i++) {
                clips[i] = mMovieCreator.build(inputVideos.get(i));
            }
            for (Movie movie : clips) {
                for (Track track : movie.getTracks()) {
                    videoTracks.add(track);
                }
            }

            Movie result = new Movie();
            if (videoTracks.size() > 0) {
                result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
            }
            Container outputISOFile = new DefaultMp4Builder().build(result);
            WritableByteChannel fileOutputStream = new RandomAccessFile(String.format(ResultPath + "/Kamcord_complete.mp4"), "rw").getChannel();
            outputISOFile.writeContainer(fileOutputStream);
            fileOutputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
