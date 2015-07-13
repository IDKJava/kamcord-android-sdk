package com.kamcord.app.model;

import com.kamcord.app.server.model.Stream;
import com.kamcord.app.server.model.User;
import com.kamcord.app.server.model.Video;

/**
 * Created by donliang1 on 6/1/15.
 */
public class FeedItem<T> {

    private Type type;
    private T data;

    public FeedItem(Type viewtype, T data) {
        this.type = viewtype;
        this.data = data;
    }

    public Type getType() {
        return type;
    }

    public User getUser() {
        return type == Type.HEADER ? (User) data : null;
    }

    public void setUser(User user) {
        this.data = type == Type.HEADER ? (T) user : null;
    }

    public Video getVideo() {
        return type == Type.VIDEO ? (Video) data : null;
    }

    public Stream getStream() {
        return type == Type.STREAM ? (Stream) data : null;
    }

    public RecordingSession getSession() {
        return type == Type.UPLOAD_PROGRESS ? (RecordingSession) data : null;
    }

    public enum Type {
        HEADER,
        FOOTER,
        VIDEO,
        STREAM,
        UPLOAD_PROGRESS,
        NORMAL_HEADER,
    }
}
