package com.example.androidproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidproject.HomeActivity;
import com.example.androidproject.LoginActivity;
import com.example.androidproject.RegisterActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnInscription;
    private TextView tvLoginLink;
    private SharedPreferences sharedPreferences;

    // Utiliser les mêmes constantes que HomeActivity
    private static final String PREFS_NAME = "eventia_prefs";
    private static final String TOKEN_KEY = "auth_token";
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_EMAIL_KEY = "user_email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialiser SharedPreferences avec le même nom que HomeActivity
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Vérifier si l'utilisateur est déjà connecté AVANT de charger la layout
        if (isUserLoggedIn()) {
            // L'utilisateur est connecté, rediriger directement vers HomeActivity
            redirectToMainApp();
            return; // Important : arrêter l'exécution ici
        }

        // Si l'utilisateur n'est pas connecté, continuer avec l'affichage normal
        setContentView(R.layout.activity_main);

        // Initialiser les vues
        initViews();

        // Configurer les listeners
        setupClickListeners();

        getSupportActionBar().hide();

    }

    private void initViews() {
        btnInscription = findViewById(R.id.btn_inscription);
        tvLoginLink = findViewById(R.id.tv_login_link);
    }

    private void setupClickListeners() {
        // Bouton d'inscription
        btnInscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectToRegister();
            }
        });

        // Lien de connexion
        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectToLogin();
            }
        });
    }

    private boolean isUserLoggedIn() {
        // Vérifier si le token existe et est valide
        String token = sharedPreferences.getString(TOKEN_KEY, null);

        // Si le token existe et n'est pas vide, l'utilisateur est considéré comme connecté
        return token != null && !token.trim().isEmpty();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Fermer cette activité
    }

    private void redirectToRegister() {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish(); // Fermer cette activité
    }

    private void redirectToMainApp() {
        // Rediriger vers l'activité principale de l'app (après connexion)
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Vérifier à nouveau quand l'activité reprend
        if (isUserLoggedIn()) {
            redirectToMainApp();
        }
    }
}