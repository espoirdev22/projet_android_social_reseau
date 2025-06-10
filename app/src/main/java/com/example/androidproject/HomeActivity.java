package com.example.androidproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapter.EventAdapter;
import com.example.androidproject.api.ApiClient;
import com.example.androidproject.api.ApiService;
import com.example.androidproject.model.ApiResponse;
import com.example.androidproject.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private TextView titleView;
    private TextView profileInitial;
    private TextView eventCountText; // Compteur total d'événements
    private TextView monthEventCountText; // Compteur d'événements ce mois
    private View nav_home;
    private View nav_explore;
    private View nav_notifications;
    private View fabAddEvent;
    private RecyclerView upcomingEventsRecyclerView;
    private EventAdapter eventAdapter;
    private List<Event> userEvents = new ArrayList<>();
    private ApiService apiService;
    private BottomNavHelper bottomNavHelper;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "eventia_prefs";
    private static final String TOKEN_KEY = "auth_token";
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_EMAIL_KEY = "user_email";

    // Formats de date pour parser les événements
    private SimpleDateFormat inputDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat inputDateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat inputDateFormat3 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        apiService = ApiClient.getClient().create(ApiService.class);

        initializeSharedPreferences();
        initViews();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setupRecyclerView();
        setupNavigation();
        setupAddEventButton();
        displayUserInitial();
        setupBottomNavigation();
        checkAuthenticationAndLoadEvents();
    }

    private void initializeSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void initViews() {
        nav_home = findViewById(R.id.nav_home);
        nav_explore = findViewById(R.id.nav_explore);
        nav_notifications = findViewById(R.id.nav_notifications);
        fabAddEvent = findViewById(R.id.fabAddEvent);
        upcomingEventsRecyclerView = findViewById(R.id.upcomingEventsRecyclerView);
        profileInitial = findViewById(R.id.profileInitial);

        // Initialisation des compteurs
        initializeCounters();

        // Ajout d'un listener de clic sur le profil
        profileInitial.setOnClickListener(v -> showLogoutDialog());
    }

    private void initializeCounters() {
        // Vous devez ajouter des IDs aux TextViews des compteurs dans votre XML
        // Pour le moment, je vais chercher par la hiérarchie des vues
        try {
            // Trouver les TextViews des compteurs dans les CardViews de statistiques
            LinearLayout statsLayout = findViewById(R.id.statsLayout); // Vous devrez ajouter cet ID au LinearLayout des stats
            if (statsLayout != null) {
                // Premier CardView - Compteur total d'événements
                View firstCard = statsLayout.getChildAt(0);
                if (firstCard != null) {
                    eventCountText = firstCard.findViewById(R.id.eventCountText); // Vous devrez ajouter cet ID
                }

                // Deuxième CardView - Compteur événements ce mois
                View secondCard = statsLayout.getChildAt(1);
                if (secondCard != null) {
                    monthEventCountText = secondCard.findViewById(R.id.monthEventCountText); // Vous devrez ajouter cet ID
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing counters: " + e.getMessage());
            // Fallback - chercher directement par ID si vous les ajoutez
            eventCountText = findViewById(R.id.eventCountText);
            monthEventCountText = findViewById(R.id.monthEventCountText);
        }
    }

    private void displayUserInitial() {
        String email = getUserEmail();
        if (email != null && !email.isEmpty()) {
            String initial = String.valueOf(email.charAt(0)).toUpperCase();
            profileInitial.setText(initial);
        } else {
            profileInitial.setText("?");
        }
    }

    private void setupBottomNavigation() {
        View bottomNavContainer = findViewById(R.id.bottomNavContainer);
        if (bottomNavContainer != null) {
            LinearLayout homeNav = bottomNavContainer.findViewById(R.id.nav_home);
            LinearLayout exploreNav = bottomNavContainer.findViewById(R.id.nav_explore);
            LinearLayout notificationsNav = bottomNavContainer.findViewById(R.id.nav_notifications);

            if (homeNav != null && exploreNav != null && notificationsNav != null) {
                bottomNavHelper = new BottomNavHelper(this, this);
                bottomNavHelper.setupBottomNavigation(homeNav, exploreNav, notificationsNav, R.id.nav_home);
            } else {
                Log.e(TAG, "Bottom navigation views not found");
            }
        }
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(userEvents);
        eventAdapter.setAuthToken(getAuthToken());
        upcomingEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        upcomingEventsRecyclerView.setAdapter(eventAdapter);
    }

    private void setupNavigation() {
        nav_home.setOnClickListener(v -> {
            // Déjà sur Home
        });

        nav_explore.setOnClickListener(v -> {
            startActivity(new Intent(this, EventMapActivity.class));
        });

        nav_notifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });
    }

    private void setupAddEventButton() {
        fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddEventActivity.class);
            startActivity(intent);
        });
    }

    private void checkAuthenticationAndLoadEvents() {
        String token = getAuthToken();

        if (token == null || token.isEmpty()) {
            redirectToLogin();
            return;
        }

        loadUserEvents();
    }

    private void loadUserEvents() {
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
                        eventAdapter.setAuthToken(getAuthToken());
                        eventAdapter.notifyDataSetChanged();

                        // Mettre à jour les compteurs
                        updateEventCounters();

                        if (userEvents.isEmpty()) {
                            Toast.makeText(HomeActivity.this, "Aucun événement trouvé", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String message = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Erreur lors du chargement";
                        Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
                        updateEventCounters(); // Mettre à jour avec 0 événements
                    }
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(HomeActivity.this, "Session expirée", Toast.LENGTH_SHORT).show();
                        redirectToLogin();
                    } else {
                        Toast.makeText(HomeActivity.this, "Erreur lors du chargement des événements", Toast.LENGTH_SHORT).show();
                    }
                    updateEventCounters(); // Mettre à jour avec 0 événements
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Event>>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                updateEventCounters(); // Mettre à jour avec 0 événements
            }
        });
    }

    /**
     * Met à jour les compteurs d'événements
     */
    private void updateEventCounters() {
        int totalEvents = userEvents.size();
        int eventsThisMonth = countEventsThisMonth();

        Log.d(TAG, "Updating counters - Total: " + totalEvents + ", This month: " + eventsThisMonth);

        // Mettre à jour le compteur total
        if (eventCountText != null) {
            eventCountText.setText(String.valueOf(totalEvents));
        } else {
            Log.w(TAG, "eventCountText is null, cannot update total count");
        }

        // Mettre à jour le compteur du mois
        if (monthEventCountText != null) {
            monthEventCountText.setText(String.valueOf(eventsThisMonth));
        } else {
            Log.w(TAG, "monthEventCountText is null, cannot update month count");
        }
    }

    /**
     * Compte le nombre d'événements de ce mois
     */
    private int countEventsThisMonth() {
        Calendar currentMonth = Calendar.getInstance();
        int currentYear = currentMonth.get(Calendar.YEAR);
        int currentMonthValue = currentMonth.get(Calendar.MONTH);

        int count = 0;
        for (Event event : userEvents) {
            Date eventDate = parseEventDate(event.getDate());
            if (eventDate != null) {
                Calendar eventCalendar = Calendar.getInstance();
                eventCalendar.setTime(eventDate);

                if (eventCalendar.get(Calendar.YEAR) == currentYear &&
                        eventCalendar.get(Calendar.MONTH) == currentMonthValue) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Parse la date de l'événement en essayant différents formats
     */
    private Date parseEventDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        // Format 1: yyyy-MM-dd HH:mm:ss
        try {
            return inputDateFormat1.parse(dateString);
        } catch (ParseException e1) {
            // Continue avec le format suivant
        }

        // Format 2: yyyy-MM-dd'T'HH:mm:ss
        try {
            return inputDateFormat2.parse(dateString);
        } catch (ParseException e2) {
            // Continue avec le format suivant
        }

        // Format 3: yyyy-MM-dd
        try {
            return inputDateFormat3.parse(dateString);
        } catch (ParseException e3) {
            Log.w(TAG, "Failed to parse date: " + dateString);
            return null;
        }
    }

    private void redirectToLogin() {
        logout();
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String token = getAuthToken();
        if (token != null && !token.isEmpty()) {
            loadUserEvents();
            displayUserInitial();
        }
    }

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

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Profil");
        builder.setMessage("Voulez-vous vous déconnecter ?");
        builder.setPositiveButton("Déconnexion", (dialog, which) -> {
            logout();
            Toast.makeText(HomeActivity.this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        });
        builder.setNegativeButton("Annuler", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    /**
     * Méthode publique pour permettre à l'EventAdapter de notifier
     * la suppression d'un événement et mettre à jour les compteurs
     */
    public void onEventDeleted() {
        updateEventCounters();
    }
}