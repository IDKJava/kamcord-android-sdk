package com.kamcord.app.kamcord.activity.utils;

import android.os.Environment;

import java.io.File;
import java.util.UUID;

public class FileManagement {

    private File SDCard_Path;
    private File VideoFolder;
    private String VideoFolderPath;
    private String rootFolder;
    private String GamePath;
    private File GameFolder;
    private File GameUUIDFolder;
    private String uuidString;
    private UUID uuid;
    private String gameName;

    public FileManagement() {

    }

    public String getGameName() {
        return gameName;
    }

    public String getUUIDString() {
        return uuidString;
    }

    public void rootFolderInitialize() {
        // SD card check and folder initialize
        if (rootFolder == null) {
            rootFolder = "/Kamcord_Android/";
        }
        SDCard_Path = Environment.getExternalStorageDirectory();
        VideoFolderPath = SDCard_Path.getParent() + "/" + SDCard_Path.getName() + "/" + rootFolder;
        VideoFolder = new File(VideoFolderPath);
        if (!VideoFolder.exists() || VideoFolder.isDirectory()) {
            VideoFolder.mkdir();
        }
    }

    public void gameFolderInitialize(String gamePackageName) {
        gameName = gamePackageName.substring(gamePackageName.lastIndexOf(".") + 1);
        // Game Folder Generation
        GamePath = SDCard_Path.getParent() + "/" + SDCard_Path.getName() + "/" + rootFolder + "/" + gameName;
        GameFolder = new File(GamePath);
        if (!GameFolder.exists() || !GameFolder.isDirectory()) {
            GameFolder.mkdir();
        }
    }

    public void sessionFolderInitialize() {
        uuid = UUID.randomUUID();
        uuidString = uuid.toString();
        GamePath = SDCard_Path.getParent() + "/" + SDCard_Path.getName() + "/" + rootFolder + "/" + gameName + "/" + uuidString;
        GameUUIDFolder = new File(GamePath);
        if (!GameUUIDFolder.exists() || !GameUUIDFolder.isDirectory()) {
            GameUUIDFolder.mkdir();
        }
    }
}
