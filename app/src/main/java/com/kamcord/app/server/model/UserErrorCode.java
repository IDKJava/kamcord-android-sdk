package com.kamcord.app.server.model;

/**
 * Created by pplunkett on 5/25/15.
 */
public class UserErrorCode {
    public static UserErrorCode OK = new UserErrorCode(0);
    public static UserErrorCode USERNAME_TAKEN = new UserErrorCode(1);
    public static UserErrorCode USERNAME_SHORT = new UserErrorCode(2);
    public static UserErrorCode USERNAME_LONG = new UserErrorCode(3);
    public static UserErrorCode INVALID_CHARACTERS = new UserErrorCode(4);
    public static UserErrorCode EMAIL_TAKEN = new UserErrorCode(5);
    public static UserErrorCode EMAIL_INVALID = new UserErrorCode(6);
    public static UserErrorCode EMAIL_LONG = new UserErrorCode(7);
    public static UserErrorCode USERNAME_MISSING = new UserErrorCode(14);
    public static UserErrorCode EMAIL_MISSING = new UserErrorCode(15);
    public static UserErrorCode GENERIC_ERROR = new UserErrorCode(18);

    public int error_code = 0;
    public UserErrorCode(int error_code)
    {
        this.error_code = error_code;
    }

    @Override
    public boolean equals(Object otherStatus)
    {
        if( otherStatus == null || !(otherStatus instanceof UserErrorCode) )
        {
            return false;
        }

        if( this.error_code != ((UserErrorCode) otherStatus).error_code )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return this.error_code;
    }
}
