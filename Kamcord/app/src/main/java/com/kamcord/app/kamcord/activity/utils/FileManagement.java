package com.kamcord.app.kamcord.activity.utils;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class FileManagement {

    private File SDCardPath;
    private File VideoFolder;
    private String VideoFolderPath;
    private String RootFolder;
    private String GamePath;
    private File GameFolder;
    private File GameUUIDFolder;
    private String UUIDString;
    private UUID Uuid;
    private String GameName;

    private File ClipsFile;

    public FileManagement() {

    }

    public String getGameName() {
        return GameName;
    }

    public String getUUIDString() {
        return UUIDString;
    }

    public void rootFolderInitialize() {
        // SD card check and folder initialize
        if (RootFolder == null) {
            RootFolder = "/Kamcord_Android/";
        }
        SDCardPath = Environment.getExternalStorageDirectory();
        VideoFolderPath = SDCardPath.getParent() + "/" + SDCardPath.getName() + "/" + RootFolder;
        VideoFolder = new File(VideoFolderPath);
        if (!VideoFolder.exists() || VideoFolder.isDirectory()) {
            VideoFolder.mkdir();
        }

        // create a cliplist for video-editting in the futrure
        try {
            ClipsFile = new File(VideoFolderPath + "/cliplist.txt");
            BufferedWriter output = new BufferedWriter(new FileWriter(ClipsFile));
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void gameFolderInitialize(String gamePackageName) {
        GameName = gamePackageName.substring(gamePackageName.lastIndexOf(".") + 1);
        // Game Folder Generation
        GamePath = SDCardPath.getParent() + "/" + SDCardPath.getName() + "/" + RootFolder + "/" + GameName;
        GameFolder = new File(GamePath);
        if (!GameFolder.exists() || !GameFolder.isDirectory()) {
            GameFolder.mkdir();
        }
    }

    public void sessionFolderInitialize() {
        Uuid = UUID.randomUUID();
        UUIDString = Uuid.toString();
        GamePath = SDCardPath.getParent() + "/" + SDCardPath.getName() + "/" + RootFolder + "/" + GameName + "/" + UUIDString;
        GameUUIDFolder = new File(GamePath);
        if (!GameUUIDFolder.exists() || !GameUUIDFolder.isDirectory()) {
            GameUUIDFolder.mkdir();
        }
    }
}
