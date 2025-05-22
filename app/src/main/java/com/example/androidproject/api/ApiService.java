package com.example.androidproject.api;

import com.example.androidproject.model.ApiResponse;
import com.example.androidproject.model.Event;
import com.example.androidproject.model.LoginRequest;
import com.example.androidproject.model.NotificationItem;
import com.example.androidproject.model.RegisterRequest;
import com.example.androidproject.model.User;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    //Authentification
    @POST("api/auth/login")
    Call<ApiResponse<User>> loginUser(@Body LoginRequest loginRequest);
    @POST("api/auth/register")
    Call<ApiResponse<User>> registerUser(@Body RegisterRequest registerRequest);
    @GET("api/myEvents")
    Call<ApiResponse<List<Event>>> getUserEvents(@Header("Authorization") String token);

    @POST("api/events")
    Call<ApiResponse<Event>> createEvent(@Header("Authorization") String authHeader, @Body Event event);


    // Notez que l'url peut être "events" ou "api/events" selon votre configuration API


    @GET("api/events")
    Call<ApiResponse<List<Event>>> getAllEvents();

    // Endpoint alternatif au cas où le chemin serait différent


    @GET("api/events/search")
    Call<ApiResponse<List<Event>>> searchEvents(@Query("query") String query);


    // Notifications

    @PUT("api/notifications/mark-all-read")
    Call<ApiResponse<Void>> markAllNotificationsAsRead(
            @Header("Authorization") String authHeader
    );

    // Notifications - Correction des endpoints
    @GET("api/notifications")
    Call<ApiResponse<List<NotificationItem>>> getNotifications(@Header("Authorization") String authorization);

    // Alternative: Si votre backend utilise les événements comme notifications
    @GET("api/events")
    Call<ApiResponse<List<Event>>> getEvents(@Header("Authorization") String authorization);

    @PUT("api/notifications/{id}/read")
    Call<ApiResponse<Void>> markNotificationAsRead(@Header("Authorization") String authorization, @Path("id") Long id);
}

