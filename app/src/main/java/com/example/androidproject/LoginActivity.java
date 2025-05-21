package com.example.androidproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.androidproject.api.ApiClient;
import com.example.androidproject.api.ApiService;
import com.example.androidproject.model.LoginRequest;
import com.example.androidproject.model.User;
import com.example.androidproject.model.ApiResponse;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    // Déclaration des vues
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private CardView loginButton;
    private CardView googleLoginButton;
    private TextView signUpText;
    private TextView forgotPasswordText;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "eventia_prefs";
    private static final String TOKEN_KEY = "auth_token";
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_EMAIL_KEY = "user_email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Masquer la barre d'action
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initializeViews();
        initializeSharedPreferences();
        checkExistingLogin();
        setupClickListeners();
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        googleLoginButton = findViewById(R.id.googleLoginButton);
        signUpText = findViewById(R.id.signUpText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
    }

    private void initializeSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void checkExistingLogin() {
        String token = sharedPreferences.getString(TOKEN_KEY, null);
        if (token != null && !token.isEmpty()) {
            // Utilisateur déjà connecté, rediriger vers l'accueil
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implémenter la récupération de mot de passe
                Toast.makeText(LoginActivity.this, "Fonctionnalité à venir", Toast.LENGTH_SHORT).show();
            }
        });

        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implémenter la connexion Google
                Toast.makeText(LoginActivity.this, "Connexion Google à venir", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validation des champs
        if (email.isEmpty()) {
            emailEditText.setError("Email requis");
            emailEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Mot de passe requis");
            passwordEditText.requestFocus();
            return;
        }

        // Désactiver le bouton pendant la requête
        loginButton.setEnabled(false);

        // Créer la requête de connexion
        LoginRequest loginRequest = new LoginRequest(email, password);

        // Appel API
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ApiResponse<User>> call = apiService.loginUser(loginRequest);

        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                loginButton.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        User user = apiResponse.getData();

                        // Sauvegarder les informations utilisateur
                        saveUserData(user, apiResponse.getToken());

                        Toast.makeText(LoginActivity.this,
                                apiResponse.getMessage() != null ? apiResponse.getMessage() : "Connexion réussie",
                                Toast.LENGTH_SHORT).show();

                        // Rediriger vers l'accueil
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                apiResponse.getMessage() != null ? apiResponse.getMessage() : "Erreur de connexion",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Erreur serveur: " + response.code(), Toast.LENGTH_LONG).show();
                }



            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                loginButton.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Erreur réseau: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveUserData(User user, String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Sauvegarder le token
        if (token != null && !token.isEmpty()) {
            editor.putString(TOKEN_KEY, token);
        }

        // Sauvegarder les infos utilisateur
        editor.putLong(USER_ID_KEY, user.getId());
        editor.putString(USER_EMAIL_KEY, user.getEmail());

        editor.apply();
    }

    public static String getAuthToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(TOKEN_KEY, null);
    }

    public static Long getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(USER_ID_KEY, -1);
    }

    public static void logout(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}