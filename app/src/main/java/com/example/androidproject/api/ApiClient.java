package com.example.androidproject.api;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final String TAG = "ApiClient";
    // Utilisez l'URL sans le segment 'api/' si vos logs montrent des appels à /api/events
    private static final String BASE_URL = "http://192.168.1.3:8000/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Ajouter un intercepteur de logging pour le débogage
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(message ->
                    Log.d(TAG, message));
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            Log.d(TAG, "API Client initialized with baseURL: " + BASE_URL);
        }
        return retrofit;
    }
}