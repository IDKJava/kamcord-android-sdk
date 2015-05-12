package com.kamcord.app.server.model;

/**
 * Created by pplunkett on 5/11/15.
 */
public class StatusCode {
    public static final StatusCode OK = new StatusCode(0, "All ok.");
    public static final StatusCode USER_NOT_AUTHORIZED = new StatusCode(1, "User not authorized for action.");
    public static final StatusCode COULD_NOT_SET_TOKEN = new StatusCode(2, "Could not set device token.");
    public static final StatusCode INSUFFICIENT_PARAMETERS = new StatusCode(3, "Insufficient parameters.");
    public static final StatusCode UNABLE_TO_DELETE_TOKEN = new StatusCode(4, "Unable to delete token.");
    public static final StatusCode PAGE_OUT_OF_BOUNDS = new StatusCode(7, "Page out of bounds.");
    public static final StatusCode GENERIC_ERROR = new StatusCode(8, "Error completing action.");
    public static final StatusCode ENTITY_NOT_FOUND = new StatusCode(9, "Entity not found.");
    public static final StatusCode USER_BLOCKED = new StatusCode(10, "Unable to complete action. User is blocked");

    public int status_code = -1;
    public String status_reason = "";

    public StatusCode(int statusCode, String statusMessage) {
        this.status_code = statusCode;
        this.status_reason = statusMessage;
    }

    @Override
    public boolean equals(Object otherStatus)
    {
        if( otherStatus == null || !(otherStatus instanceof StatusCode) )
        {
            return false;
        }

        if( this.status_code != ((StatusCode) otherStatus).status_code )
        {
            return false;
        }

        if( this.status_reason == null )
        {
            if( ((StatusCode) otherStatus).status_reason != null )
            {
                return false;
            }
        }
        else if( !this.status_reason.equals(((StatusCode) otherStatus).status_reason) )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return status_reason != null ? status_reason.hashCode() : 0;
    }
}
