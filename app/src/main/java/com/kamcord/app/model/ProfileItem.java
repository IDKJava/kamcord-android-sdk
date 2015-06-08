package com.kamcord.app.model;

import com.kamcord.app.server.model.User;
import com.kamcord.app.server.model.Video;

/**
 * Created by donliang1 on 6/1/15.
 */
public class ProfileItem {

    private Type type;
    private Video video;
    private User user;

    public ProfileItem(Type viewtype, Video video) {
        this.type = viewtype;
        this.video = video;
    }

    public Type getType() {
        return type;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Video getVideo() {
        return video;
    }

    public enum Type {
        HEADER,
        FOOTER,
        VIDEO,
    }
}
