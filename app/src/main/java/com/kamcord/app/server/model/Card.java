package com.kamcord.app.server.model;

import java.util.List;

/**
 * Created by dennisqin on 6/30/15.
 */
public class Card {
    public String title;
    public String sub_title;
    public String card_type;
    public String action_type;
    public Video video;
    public User user;
    public Game game;
    public Stream stream;
    public Feed feed;
    public List<User> user_list;
}
