package com.kamcord.app.kamcord.activity.model;

public class GameModel {

    private String packageName;
    private String gameName;
    private int drawableID;

    public GameModel(String packageName, String gameName, int drawableID)
    {
        this.packageName = packageName;
        this.gameName = gameName;
        this.drawableID = drawableID;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setGameName(String packageName) {
        String temp = this.packageName.substring(packageName.lastIndexOf(".") + 1);
        this.gameName = temp.substring(0, 1).toUpperCase() + temp.substring(1);
    }

    public String getGameName() {
        return gameName;
    }

    public void setDrawableID(int drawableID) {
        this.drawableID = drawableID;
    }

    public int getDrawableID() {
        return drawableID;
    }
}
