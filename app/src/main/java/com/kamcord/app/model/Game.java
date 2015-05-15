package com.kamcord.app.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by pplunkett on 5/15/15.
 */
public class Game implements Parcelable {
    private String id;
    private String name;
    private String packageName;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(packageName);
    }

    public static final Parcelable.Creator<Game> CREATOR
            = new Parcelable.Creator<Game>() {
        public Game createFromParcel(Parcel in) {
            return new Game(in);
        }

        public Game[] newArray(int size) {
            return new Game[size];
        }
    };

    private Game(Parcel in) {
        id = in.readString();
        name = in.readString();
        packageName = in.readString();
    }
}
