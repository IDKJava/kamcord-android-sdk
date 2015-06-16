package com.kamcord.app.server.model.analytics;

/**
 * Created by pplunkett on 6/15/15.
 */
public class WrappedResponse<T> {
    public T response;
    public StatusCode status_code;
    public String status_reason;
    public boolean retriable;

    public enum StatusCode {
        OK,
    }
}
