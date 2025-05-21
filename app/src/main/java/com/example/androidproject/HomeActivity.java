package com.example.androidproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapter.EventAdapter;
import com.example.androidproject.api.ApiClient;
import com.example.androidproject.api.ApiService;
import com.example.androidproject.model.ApiResponse;
import com.example.androidproject.model.Event;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private SearchView homeSearch;
    private View nav_home;
    private View nav_explore;
    private View nav_notifications;
    private View fabAddEvent;
    private RecyclerView upcomingEventsRecyclerView;
    private EventAdapter eventAdapter;
    private List<Event> userEvents = new ArrayList<>();
    private ApiService apiService;

    // Utilisation de la même approche que LoginActivity pour la gestion du token
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "eventia_prefs";
    private static final String TOKEN_KEY = "auth_token";
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_EMAIL_KEY = "user_email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        apiService = ApiClient.getClient().create(ApiService.class);

        // Initialiser SharedPreferences comme dans LoginActivity
        initializeSharedPreferences();

        initViews();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setupRecyclerView();
        setupNavigation();
        setupSearchView();
        setupAddEventButton();

        // Vérifier l'authentification avant de charger les événements
        checkAuthenticationAndLoadEvents();
    }

    private void initializeSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void initViews() {
        homeSearch = findViewById(R.id.homeSearch);
        nav_home = findViewById(R.id.nav_home);
        nav_explore = findViewById(R.id.nav_explore);
        nav_notifications = findViewById(R.id.nav_notifications);
        fabAddEvent = findViewById(R.id.fabAddEvent);
        upcomingEventsRecyclerView = findViewById(R.id.upcomingEventsRecyclerView);
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(userEvents);
        upcomingEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        upcomingEventsRecyclerView.setAdapter(eventAdapter);
    }

    private void setupNavigation() {
        nav_home.setOnClickListener(v -> {
            // Déjà sur Home, pas besoin de rediriger
        });

        nav_explore.setOnClickListener(v -> {
            startActivity(new Intent(this, EventMapActivity.class));
        });

        nav_notifications.setOnClickListener(v -> {
            // Gérer la navigation vers les notifications
        });
    }

    private void setupSearchView() {
        homeSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchEvents(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadUserEvents();
                }
                return false;
            }
        });
    }

    private void setupAddEventButton() {
        fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddEventActivity.class);
            startActivity(intent);
        });
    }

    private void checkAuthenticationAndLoadEvents() {
        // Utiliser la même méthode que LoginActivity pour récupérer le token
        String token = getAuthToken();

        if (token == null || token.isEmpty()) {
            // Rediriger immédiatement vers la page de connexion
            redirectToLogin();
            return;
        }

        loadUserEvents();
    }

    private void loadUserEvents() {
        // Utiliser la même méthode que LoginActivity pour récupérer le token
        String token = getAuthToken();

        if (token == null || token.isEmpty()) {
            redirectToLogin();
            return;
        }

        String authHeader = "Bearer " + token;

        apiService.getUserEvents(authHeader).enqueue(new Callback<ApiResponse<List<Event>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Event>>> call, Response<ApiResponse<List<Event>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Event>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        userEvents.clear();
                        userEvents.addAll(apiResponse.getData());
                        eventAdapter.notifyDataSetChanged();

                        if (userEvents.isEmpty()) {
                            Toast.makeText(HomeActivity.this, "Aucun événement trouvé", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String message = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Erreur lors du chargement";
                        Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Erreur de réponse - vérifier si c'est un problème d'authentification
                    if (response.code() == 401) {
                        Toast.makeText(HomeActivity.this, "Session expirée", Toast.LENGTH_SHORT).show();
                        redirectToLogin();
                    } else {
                        Toast.makeText(HomeActivity.this, "Erreur lors du chargement des événements", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Event>>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchEvents(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadUserEvents();
            return;
        }

        apiService.searchEvents(query).enqueue(new Callback<ApiResponse<List<Event>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Event>>> call, Response<ApiResponse<List<Event>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Event>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        userEvents.clear();
                        userEvents.addAll(apiResponse.getData());
                        eventAdapter.notifyDataSetChanged();

                        if (userEvents.isEmpty()) {
                            Toast.makeText(HomeActivity.this, "Aucun résultat trouvé", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(HomeActivity.this, "Erreur lors de la recherche", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Erreur lors de la recherche", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Event>>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Erreur de connexion lors de la recherche", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToLogin() {
        // Utiliser la même méthode que LoginActivity pour la déconnexion
        logout();

        // Créer l'intent pour la page de connexion
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);

        // Ces flags nettoient la pile d'activités pour éviter de revenir à HomeActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish(); // Fermer HomeActivity
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les événements uniquement si l'utilisateur est connecté
        String token = getAuthToken();
        if (token != null && !token.isEmpty()) {
            loadUserEvents();
        }
    }

    // Méthodes utilitaires identiques à celles de LoginActivity
    private String getAuthToken() {
        return sharedPreferences.getString(TOKEN_KEY, null);
    }

    private Long getUserId() {
        return sharedPreferences.getLong(USER_ID_KEY, -1);
    }

    private String getUserEmail() {
        return sharedPreferences.getString(USER_EMAIL_KEY, null);
    }

    private void logout() {
        sharedPreferences.edit().clear().apply();
    }

    // Méthodes statiques pour l'accès depuis d'autres activités (identiques à LoginActivity)
    public static String getAuthToken(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        return prefs.getString(TOKEN_KEY, null);
    }

    public static Long getUserId(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        return prefs.getLong(USER_ID_KEY, -1);
    }

    public static void logout(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}