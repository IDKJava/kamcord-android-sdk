package com.kamcord.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.kamcord.app.model.RecordingSession;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by pplunkett on 6/10/15.
 */
public class ActiveRecordingSessionManager {

    private static final String ACTIVE_SESSION_PREFS = "active_sessions_prefs";
    private static final String ACTIVE_SESSIONS_KEY = "active_sessions";
    private static SharedPreferences preferences = null;

    private static Set<RecordingSession> activeSessions = new HashSet<>();

    public synchronized static void initializeWith(Context context)
    {
        preferences = context.getApplicationContext()
                .getSharedPreferences(ACTIVE_SESSION_PREFS, Context.MODE_PRIVATE);
        loadActiveSessions();
    }

    public synchronized static boolean addActiveSession(RecordingSession session) {
        boolean added = activeSessions.add(session);
        if( added ) {
            saveActiveSessions();
        }
        return added;
    }

    public synchronized static boolean updateActiveSession(RecordingSession session) {
        boolean updated = false;
        if( activeSessions.contains(session) ) {
            activeSessions.remove(session);
            updated = activeSessions.add(session);
        }
        if( updated ) {
            saveActiveSessions();
        }
        return updated;
    }

    public synchronized static boolean removeActiveSession(RecordingSession session) {
        return activeSessions.remove(session);
    }

    public synchronized static Set<RecordingSession> getActiveSessions() {
        return activeSessions;
    }

    public synchronized static boolean isSessionActive(RecordingSession session) {
        return activeSessions.contains(session);
    }

    public synchronized static RecordingSession.State getActiveSessionState(RecordingSession session) {
        if( isSessionActive(session) ) {
            for( RecordingSession activeSession : activeSessions ) {
                if( activeSession.equals(session) ) {
                    return activeSession.getState();
                }
            }
        }
        return null;
    }

    private synchronized static void loadActiveSessions() {
        Set<String> serializedSessions = preferences.getStringSet(ACTIVE_SESSIONS_KEY, new HashSet<String>());

        activeSessions.clear();
        for( String serializedSession : serializedSessions ) {
            RecordingSession deserializedSession = new Gson().fromJson(serializedSession, RecordingSession.class);
            File mergedVideoFile = new File(FileSystemManager.getRecordingSessionCacheDirectory(deserializedSession),
                    FileSystemManager.MERGED_VIDEO_FILENAME);
            if( mergedVideoFile.exists() ) {
                activeSessions.add(deserializedSession);
            }
        }
    }

    private synchronized static void saveActiveSessions() {
        Set<String> serializedSessions = new HashSet<>();
        for( RecordingSession session : activeSessions) {
            String serializedSession = new Gson().toJson(session);
            serializedSessions.add(serializedSession);
        }
        preferences.edit()
                .putStringSet(ACTIVE_SESSIONS_KEY, serializedSessions)
                .apply();
    }
}
