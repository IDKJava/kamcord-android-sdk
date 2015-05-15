package com.kamcord.app.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by pplunkett on 5/15/15.
 */
public class Video implements Parcelable {
    private String videoPath;
    private String audioPath;
    private String title;
    private String description;

    public Video() {}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(videoPath);
        parcel.writeString(audioPath);
        parcel.writeString(title);
        parcel.writeString(description);
    }

    public static final Parcelable.Creator<Video> CREATOR
            = new Parcelable.Creator<Video>() {
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    private Video(Parcel in)
    {
        videoPath = in.readString();
        audioPath = in.readString();
        title = in.readString();
        description = in.readString();
    }

    public static class Builder
    {
        private Video video = new Video();

        public Builder setVideoPath(String path)
        {
            video.videoPath = path;
            return this;
        }

        public Builder setAudioPath(String path)
        {
            video.audioPath = path;
            return this;
        }

        public Builder setTitle(String title)
        {
            video.title = title;
            return this;
        }

        public Builder setDescription(String description)
        {
            video.description = description;
            return this;
        }

        public Video build()
        {
            return video;
        }
    }
}
