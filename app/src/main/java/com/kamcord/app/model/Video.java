package com.kamcord.app.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.kamcord.app.utils.StringUtils;

/**
 * Created by pplunkett on 5/15/15.
 */
public class Video implements Parcelable {
    private String videoPath;
    private String audioPath;
    private String title;
    private String description;

    public Video() {
    }

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

    private Video(Parcel in) {
        videoPath = in.readString();
        audioPath = in.readString();
        title = in.readString();
        description = in.readString();
    }

    public static class Builder {
        private Video video = new Video();

        public Builder setVideoPath(String path) {
            video.videoPath = nonEmptyString(path);
            return this;
        }

        public Builder setAudioPath(String path) {
            video.audioPath = nonEmptyString(path);
            return this;
        }

        public Builder setTitle(String title) {
            video.title = nonEmptyString(title);
            return this;
        }

        public Builder setDescription(String description) {
            video.description = nonEmptyString(description);
            return this;
        }

        private String nonEmptyString(String in) {
            return in == null || in.isEmpty() ? null : in;
        }

        public Video build() {
            return video;
        }
    }

    @Override
    public boolean equals(Object other)
    {
        if( other == null || !(other instanceof Video) )
            return false;

        Video otherVideo = (Video) other;
        if( !StringUtils.compare(videoPath, otherVideo.videoPath)
                || !StringUtils.compare(audioPath, otherVideo.audioPath)
                || !StringUtils.compare(title, otherVideo.title)
                || !StringUtils.compare(description, otherVideo.description) )
        {
            return false;
        }

        return true;
    }

    @Override
    public String toString()
    {
        return new StringBuilder()
                .append("[")
                .append(videoPath).append(",")
                .append(audioPath).append(",")
                .append(title).append(",")
                .append(description)
                .append("]").toString();
    }
}
