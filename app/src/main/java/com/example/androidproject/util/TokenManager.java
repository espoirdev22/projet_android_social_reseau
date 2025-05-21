package com.example.androidproject.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class TokenManager {
    private static final String TAG = "TokenManager";
    private static final String PREF_NAME = "auth_preferences";
    private static final String TOKEN_KEY = "auth_token";

    private SharedPreferences sharedPreferences;

    public TokenManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(TOKEN_KEY, token);
            editor.apply();
            Log.d(TAG, "Token sauvegardé avec succès");
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la sauvegarde du token", e);
        }
    }

    public String getToken() {
        try {
            String token = sharedPreferences.getString(TOKEN_KEY, null);
            Log.d(TAG, "Token récupéré: " + (token != null ? "existe" : "n'existe pas"));
            return token;
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la récupération du token", e);
            return null;
        }
    }

    public void clearToken() {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(TOKEN_KEY);
            editor.apply();
            Log.d(TAG, "Token supprimé avec succès");
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la suppression du token", e);
        }
    }

    public boolean hasToken() {
        String token = getToken();
        return token != null && !token.isEmpty();
    }
}