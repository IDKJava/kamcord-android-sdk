package com.kamcord.app.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.kamcord.app.utils.StringUtils;

/**
 * Created by pplunkett on 5/15/15.
 */
public class Video implements Parcelable {
    private String uuid;
    private String title;
    private String description;

    public Video() {
    }

    public String getUUID() {
        return uuid;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(uuid);
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
        uuid = in.readString();
        title = in.readString();
        description = in.readString();
    }

    public static class Builder {
        private Video video = new Video();

        public Builder setVideoPath(String path) {
            video.uuid = nonEmptyString(path);
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
        if( !StringUtils.compare(uuid, otherVideo.uuid)
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
                .append(uuid).append(",")
                .append(title).append(",")
                .append(description)
                .append("]").toString();
    }
}
