package com.kamcord.app.model;

import com.kamcord.app.server.model.User;
import com.kamcord.app.server.model.Video;

/**
 * Created by donliang1 on 6/1/15.
 */
public class ProfileItem<T> {

    private Type type;
    private T data;

    public ProfileItem(Type viewtype, T data) {
        this.type = viewtype;
        this.data = data;
    }

    public Type getType() {
        return type;
    }

    public User getUser() {
        return data instanceof User ? (User) data : null;
    }

    public void setUser(User user) {
        this.data = data instanceof User ? (T) user : null;
    }

    public Video getVideo() {
        return data instanceof Video ? (Video) data : null;
    }

    public RecordingSession getSession() {
        return data instanceof RecordingSession ? (RecordingSession) data : null;
    }

    public enum Type {
        HEADER,
        FOOTER,
        VIDEO,
        UPLOAD_PROGRESS,
    }
}
