package com.kamcord.app.server.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.kamcord.app.BuildConfig;
import com.kamcord.app.server.model.Account;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.PaginatedGameList;
import com.kamcord.app.server.model.ReserveVideoEntity;
import com.kamcord.app.server.model.ReserveVideoResponse;
import com.kamcord.app.server.model.UserErrorCode;
import com.kamcord.app.server.model.VideoUploadedEntity;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.DeviceManager;
import com.squareup.okhttp.Call;

import java.lang.reflect.Type;
import java.util.Date;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by pplunkett on 5/11/15.
 */
public class AppServerClient {
    private static final String BASE_URL = "https://app.kamcord.com";

    public interface AppServer
    {
        @GET("/app/v3/kcp/games")
        void getGamesList(
                @Query("isAndroidOnly") boolean isAndroidOnly,
                @Query("isIOSOnly") boolean isIOSOnly,
                Callback<GenericResponse<PaginatedGameList>> cb);

        @FormUrlEncoded
        @POST("/app/v3/account/create")
        void createProfile(
                @Field("username") String username,
                @Field("email") String email,
                @Field("password") String password,
                Callback<GenericResponse<Account>> cb);

        @FormUrlEncoded
        @POST("/app/v3/account/login")
        void login(
                @Field("username") String username,
                @Field("password") String password,
                Callback<GenericResponse<Account>> cb);

        @GET("/app/v3/account/username/validate")
        void validateUsername(
                @Query("username") String username,
                Callback<GenericResponse<UserErrorCode>> cb);

        @GET("/app/v3/account/email/validate")
        void validateEmail(
                @Query("email") String email,
                Callback<GenericResponse<UserErrorCode>> cb);

        @FormUrlEncoded
        @POST("/app/v3/account/password/reset")
        void resetPassword(
                @Field("username") String email,
                Callback<GenericResponse<?>> cb);

        @POST("/app/v3/account/logout")
        void logout(Callback<GenericResponse<?>> cb);

        @POST("/app/v3/kcp/video/reserve")
        GenericResponse<ReserveVideoResponse> reserveVideo(@Body ReserveVideoEntity body);

        @POST("/app/v3/kcp/video/uploaded")
        GenericResponse<?> videoUploaded(@Body VideoUploadedEntity body);

        @GET("/app/v3/feeds/myfeed")
        void getMyFeedPage(@Query("page") String page, Callback<GenericResponse<?>>);
    }

    private static AppServer instance;
    private static RequestInterceptor addHeadersInterceptor = new RequestInterceptor() {
        @Override
        public void intercept(RequestFacade request) {
            request.addHeader("user-agent", "android_app_" + BuildConfig.VERSION_NAME);

            Account account = AccountManager.getStoredAccount();
            if( account != null )
            {
                request.addHeader("user-token", account.token);
            }

            String deviceToken = DeviceManager.getDeviceToken();
            if( deviceToken != null && !deviceToken.isEmpty() ) {
                request.addHeader("device-token", DeviceManager.getDeviceToken());
            }
        }
    };

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
                    .setEndpoint(BASE_URL)
                    .setConverter(new GsonConverter(gson))
                    .setRequestInterceptor(addHeadersInterceptor)
                    .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                    .build();
            instance = restAdapter.create(AppServer.class);
        }
        return instance;
    }
}
