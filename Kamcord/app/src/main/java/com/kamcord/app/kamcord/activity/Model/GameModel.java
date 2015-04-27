package com.kamcord.app.kamcord.activity.Model;

/**
 * Created by donliang1 on 4/24/15.
 */
public class GameModel {

    private String packageName;
    private int drawableID;

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setDrawableID(int drawableID) {
        this.drawableID = drawableID;
    }

    public int getDrawableID() {
        return drawableID;
    }
}
