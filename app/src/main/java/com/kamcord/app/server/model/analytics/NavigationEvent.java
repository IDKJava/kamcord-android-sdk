package com.kamcord.app.server.model.analytics;

/**
 * Created by pplunkett on 6/16/15.
 */
public class NavigationEvent extends Event {
    public long event_duration;
    public SourceView source_view;

    public enum SourceView {

    }
}
