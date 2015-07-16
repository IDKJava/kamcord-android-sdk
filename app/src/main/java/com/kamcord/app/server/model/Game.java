package com.kamcord.app.server.model;

import java.util.Date;

public class Game {

    public static class Icons {
        public String regular;
    }

    public Icons icons;
    public String game_primary_id;
    public String name;
    public String app_store_url;
    public String app_store_id;
    public String play_store_url;
    public String play_store_id;
    public Date went_live_time;
    public int number_of_followers;
    public int number_of_players;
    public int number_of_videos;
    public Boolean is_user_following;

    // Client-specific fields
    public boolean isInstalled = false;
    public boolean isRecording = false;
    public int serverIndex = -1;

    @Override
    public boolean equals(Object other) {
        if( other == null || !(other instanceof Game) ) {
            return false;
        }

        Game otherGame = (Game) other;
        return game_primary_id != null && game_primary_id.equals(otherGame.game_primary_id);
    }

    @Override
    public int hashCode() {
        return game_primary_id != null ? game_primary_id.hashCode() : 0;
    }
}
