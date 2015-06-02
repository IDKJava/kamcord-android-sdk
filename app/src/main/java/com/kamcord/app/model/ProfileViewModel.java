package com.kamcord.app.model;

import com.kamcord.app.server.model.User;
import com.kamcord.app.server.model.Video;

/**
 * Created by donliang1 on 6/1/15.
 */
public class ProfileViewModel {

    private int type;
    private Video video;
    private User user;

    public ProfileViewModel(int viewtype, Video video) {
        this.type = viewtype;
        this.video = video;
    }

    public int getType() {
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

    public ProfileViewModel() {
    }
}
