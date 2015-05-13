package com.kamcord.app.utils;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;

/**
 * Created by pplunkett on 5/13/15.
 */
public class DeviceManager {
    private static final String TAG = DeviceManager.class.getSimpleName();
    private static final String GUID = ".guid";
    private static String deviceToken;

    public static void initialize()
    {
        File[] possibleLocations = new File[]{
                new File(FileSystemManager.getCacheDirectory(), GUID),
                new File(FileSystemManager.getSDKCacheDirectory(), GUID),
                new File(FileSystemManager.getOldSDKCacheDirectory(), GUID),
        };

        boolean readDeviceToken = false;
        for( File possibleLocation : possibleLocations )
        {
            try
            {
                readUUIDFromFile(possibleLocation);
                readDeviceToken = true;
                break;
            }
            catch( Exception e )
            {
                Log.i(TAG, "Couldn't find device token, trying again.");
            }
        }

        if( !readDeviceToken )
        {
            deviceToken = UUID.randomUUID().toString();
            writeUUIDToFile();
        }
    }

    public static String getDeviceToken()
    {
        return deviceToken;
    }

    private static void readUUIDFromFile(File file) throws FileNotFoundException
    {
        // First determine if we've already generated a GUID for this device.
        StringBuilder stringBuilder = new StringBuilder((int) file.length());
        Scanner scanner = new Scanner(file);

        try
        {
            stringBuilder.append(scanner.nextLine());
            deviceToken = stringBuilder.toString();
        }
        finally
        {
            scanner.close();
        }

        // Check if what we read is actually a UUID.
        UUID.fromString(deviceToken);
        // If the above line didn't throw anything, it's a valid UUID.
    }

    private static void writeUUIDToFile()
    {
        // Write the UUID to file.
        try
        {
            FileWriter fileWriter = new FileWriter(new File(FileSystemManager.getCacheDirectory(), ".guid"));
            fileWriter.write(deviceToken);
            fileWriter.close();
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }
}
