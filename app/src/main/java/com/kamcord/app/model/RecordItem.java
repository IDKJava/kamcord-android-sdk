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
        INSTALLED_HEADER,
        GAME,
        NOT_INSTALLED_HEADER,
        REQUEST_GAME,
    }

    public int getListOrdinal() {
        int ordinal = 0;

        if( type == Type.INSTALLED_HEADER ) {
            ordinal = 0;

        } else if( type == Type.GAME && game != null && game.isInstalled ) {
            ordinal = 1;

        } else if(type == Type.REQUEST_GAME ) {
            ordinal = 2;

        } else if( type == Type.NOT_INSTALLED_HEADER ) {
            ordinal = 3;

        } else if( type == Type.GAME && game != null ) {
            ordinal = 4;

        }

        return ordinal;
    }
}
