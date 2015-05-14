package com.kamcord.app.utils;

import android.os.Environment;

import java.io.File;
import java.util.UUID;

public class FileManagement {

    private File sdCardPath;
    private File videoFolder;
    private String videoFolderPath;
    private String rootFolder;
    private String gamePath;
    private File gameFolder;
    private File gameUUIDFolder;
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
        sdCardPath = Environment.getExternalStorageDirectory();
        videoFolderPath = sdCardPath.getParent() + "/" + sdCardPath.getName() + "/" + rootFolder;
        videoFolder = new File(videoFolderPath);
        if (!videoFolder.exists() || videoFolder.isDirectory()) {
            videoFolder.mkdir();
        }
    }

    public void gameFolderInitialize(String gamePackageName) {
        gameName = gamePackageName.substring(gamePackageName.lastIndexOf(".") + 1);
        // Game Folder Generation
        gamePath = sdCardPath.getParent() + "/" + sdCardPath.getName() + "/" + rootFolder + "/" + gameName;
        gameFolder = new File(gamePath);
        if (!gameFolder.exists() || !gameFolder.isDirectory()) {
            gameFolder.mkdir();
        }
    }

    public void sessionFolderInitialize() {
        uuid = UUID.randomUUID();
        uuidString = uuid.toString();
        gamePath = sdCardPath.getParent() + "/" + sdCardPath.getName() + "/" + rootFolder + "/" + gameName + "/" + uuidString;
        gameUUIDFolder = new File(gamePath);
        if (!gameUUIDFolder.exists() || !gameUUIDFolder.isDirectory()) {
            gameUUIDFolder.mkdir();
        }
    }

    public String getGamePath() {
        return gamePath;
    }
}
