package com.kamcord.app.server.model;

import java.util.List;

/**
 * Created by pplunkett on 5/14/15.
 */
public class ReserveVideoEntity {
    public String game_id;
    public List<String> keywords;
    public String description;
    public List<Metadata> metadata;
    public String default_title;
    public String user_title;
}
