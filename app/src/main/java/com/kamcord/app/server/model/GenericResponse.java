package com.kamcord.app.server.model;


/**
 * Created by pplunkett on 5/11/15.
 */
public class GenericResponse<T>
{
    public StatusCode status;
    public T response;
}
