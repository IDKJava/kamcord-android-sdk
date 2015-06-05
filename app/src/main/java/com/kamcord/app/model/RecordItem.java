package com.kamcord.app.model;

import com.kamcord.app.server.model.Game;

/**
 * Created by pplunkett on 6/4/15.
 */
public class RecordItem {

    private Game game = null;
    private Type type = Type.GAME;

    public RecordItem(Type type, Game game) {
        this.type = type;
        this.game = game;
    }

    public Type getType() {
        return type;
    }

    public Game getGame() {
        return game;
    }

    public enum Type {
        GAME,
        INSTALLED_HEADER,
        NOT_INSTALLED_HEADER,
        REQUEST_GAME,
    }
}
