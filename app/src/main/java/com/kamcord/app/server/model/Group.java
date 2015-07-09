package com.kamcord.app.server.model;

import java.util.List;

/**
 * Created by dennisqin on 6/30/15.
 */
public class Group {
    public String next_page;
    public String group_type;
    public String title;
    public String feed_id;
    public List<Card> card_models;

    public static final String HERO = "HERO";
    public static final String DETAIL = "DETAIL";
    public static final String WHO_TO_FOLLOW = "WHO_TO_FOLLOW";
    public static final String SOCIAL_MEDIA = "SOCIAL_MEDIA";
    public static final String GAME_HIGHLIGHT = "GAME_HIGHLIGHT";
    public static final String STREAM_LIST = "STREAM_LIST";
}
