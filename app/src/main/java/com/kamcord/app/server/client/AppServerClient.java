package com.kamcord.app.server.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.PaginatedGameList;

import java.lang.reflect.Type;
import java.util.Date;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by pplunkett on 5/11/15.
 */
public class AppServerClient {
    public interface AppServer
    {
        @GET("/app/v3/kcp/games")
        void getGamesList(@Query("isAndroidOnly") boolean isAndroidOnly, @Query("isIOSOnly") boolean isIOSOnly, Callback<GenericResponse<PaginatedGameList>> cb);
    }

    private static AppServer instance;

    public static synchronized AppServer getInstance()
    {
        if( instance == null )
        {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                        @Override
                        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                            return new Date(json.getAsJsonPrimitive().getAsLong());
                        }
                    })
                    .create();

            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint("https://app.kamcord.com/")
                    .setConverter(new GsonConverter(gson))
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .build();
            instance = restAdapter.create(AppServer.class);
        }
        return instance;
    }
}
