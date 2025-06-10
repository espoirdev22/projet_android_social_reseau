package com.example.androidproject.api;

import com.example.androidproject.model.ApiResponse;
import com.example.androidproject.model.Event;
import com.example.androidproject.model.LoginRequest;
import com.example.androidproject.model.NotificationItem;
import com.example.androidproject.model.RegisterRequest;
import com.example.androidproject.model.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    //Authentification
    @POST("api/auth/login")
    Call<ApiResponse<User>> loginUser(@Body LoginRequest loginRequest);
    @POST("api/auth/register")
    Call<ApiResponse<User>> registerUser(@Body RegisterRequest registerRequest);

    // NOUVEAU: Google Login
    //@POST("api/auth/google")
    //Call<ApiResponse<User>> googleLogin(@Body GoogleLoginRequest googleLoginRequest);
    //Call<ApiResponse<User>> googleLogin(@Body GoogleLoginRequest googleLoginRequest);

    @GET("api/myEvents")
    Call<ApiResponse<List<Event>>> getUserEvents(@Header("Authorization") String token);

    @POST("api/events")
    Call<ApiResponse<Event>> createEvent(@Header("Authorization") String authHeader, @Body Event event);


    // Notez que l'url peut être "events" ou "api/events" selon votre configuration API


    @GET("api/events")
    Call<ApiResponse<List<Event>>> getAllEvents();

    //  Supprimer un événement
    @DELETE("api/events/{id}")
    Call<ApiResponse<Void>> deleteEvent(@Header("Authorization") String authorization, @Path("id") Long eventId);
    @GET("api/events/search")
    Call<ApiResponse<List<Event>>> searchEvents(@Query("query") String query);

    // Nouvel endpoint pour uploader une image
    @Multipart
    @POST("events")
    Call<ApiResponse<Event>> createEventWithImage(
            @Header("Authorization") String token,
            @Part("event") Event event,
            @Part MultipartBody.Part image
    );


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

