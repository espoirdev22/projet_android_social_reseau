package com.example.androidproject;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.adapter.EventCardsAdapter;
import com.example.androidproject.api.ApiClient;
import com.example.androidproject.api.ApiService;
import com.example.androidproject.model.ApiResponse;
import com.example.androidproject.model.Event;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventMapActivity extends AppCompatActivity implements OnMapReadyCallback, EventCardsAdapter.OnEventCardClickListener {

    private static final String TAG = "EventMapActivity";
    private final int FINE_PERMISSION_CODE = 1;
    private BottomNavHelper bottomNavHelper;

    // UI Components
    private SearchView mapSearchView;
    private ProgressBar loadingIndicator;
    private TextView emptyView;
    private RecyclerView eventsRecyclerView;
    private EventCardsAdapter eventCardsAdapter;
    private View nav_home;
    private View nav_explore;
    private View nav_notifications;

    // Map and Location
    private GoogleMap myMap;
    private Map<Marker, Event> markerEventMap = new HashMap<>();
    private Map<Event, Marker> eventMarkerMap = new HashMap<>();
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;

    // Data
    private List<Event> eventList = new ArrayList<>();
    private List<Event> filteredEventList = new ArrayList<>();

    // API Service
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_map);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize services
        apiService = ApiClient.getClient().create(ApiService.class);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize views
        initViews();
        setupRecyclerView();

        // Set up map
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Map fragment not found");
            Toast.makeText(this, "Erreur de chargement de la carte", Toast.LENGTH_SHORT).show();
        }

        // Load data
        getLastLocation();
        loadEvents();
        setupBottomNavigation();
    }

    private void setupNavigation() {
        nav_home.setOnClickListener(v -> {
            startActivity(new Intent(this, EventMapActivity.class));
        });

        nav_explore.setOnClickListener(v -> {

        });

        nav_notifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });
    }

    private void setupBottomNavigation() {
        // Récupération correcte des vues de navigation
        View bottomNavContainer = findViewById(R.id.bottomNavContainer);
        if (bottomNavContainer != null) {
            LinearLayout homeNav = bottomNavContainer.findViewById(R.id.nav_home);
            LinearLayout exploreNav = bottomNavContainer.findViewById(R.id.nav_explore);
            LinearLayout notificationsNav = bottomNavContainer.findViewById(R.id.nav_notifications);

            if (homeNav != null && exploreNav != null && notificationsNav != null) {
                bottomNavHelper = new BottomNavHelper(this, this);
                bottomNavHelper.setupBottomNavigation(homeNav, exploreNav, notificationsNav, R.id.nav_explore);
            } else {
                Log.e(TAG, "Bottom navigation views not found");
            }
        }
    }

    private void initViews() {
        // Search view
        nav_home = findViewById(R.id.nav_home);
        nav_explore = findViewById(R.id.nav_explore);
        nav_notifications = findViewById(R.id.nav_notifications);

        mapSearchView = findViewById(R.id.mapSearch);
        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterEvents(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    showAllEvents();
                } else {
                    filterEvents(newText);
                }
                return true;
            }
        });

        // Loading indicator and empty view
        loadingIndicator = findViewById(R.id.loadingIndicator);
        emptyView = findViewById(R.id.emptyView);

        // RecyclerView
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
    }

    private void setupRecyclerView() {
        eventCardsAdapter = new EventCardsAdapter(this);
        eventsRecyclerView.setAdapter(eventCardsAdapter);

        // Configuration du LinearLayoutManager horizontal
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        eventsRecyclerView.setLayoutManager(layoutManager);

        // Optionnel : ajouter un décorateur pour l'espacement
        // eventsRecyclerView.addItemDecoration(new HorizontalSpaceItemDecoration(16));
    }

    @Override
    public void onEventCardClick(Event event) {
        if (myMap != null && event.hasValidLocation()) {
            // Animer la caméra vers l'événement sélectionné
            LatLng eventLocation = event.getLatLng();
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 16));

            // Trouver et afficher la fenêtre d'information du marqueur correspondant
            Marker marker = eventMarkerMap.get(event);
            if (marker != null) {
                marker.showInfoWindow();
            }

            Log.d(TAG, "Navigation vers l'événement: " + event.getTitre());
        }
    }

    private void loadEvents() {
        showLoading(true);
        Log.d(TAG, "Début du chargement des événements");

        apiService.getAllEvents().enqueue(new Callback<ApiResponse<List<Event>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Event>>> call,
                                   Response<ApiResponse<List<Event>>> response) {
                Log.d(TAG, "Réponse API reçue, code: " + response.code());
                showLoading(false);

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d(TAG, "Corps de réponse: " + response.body());
                        if (response.body().getData() != null) {
                            eventList.clear();
                            eventList.addAll(response.body().getData());
                            Log.d(TAG, "Événements chargés: " + eventList.size());

                            showAllEvents();
                        } else {
                            Log.e(TAG, "Les données sont null dans la réponse");
                            loadMockEvents();
                        }
                    } else {
                        Log.e(TAG, "Le corps de la réponse est null");
                        loadMockEvents();
                    }
                } else {
                    String errorMsg = "Erreur serveur: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                            Log.e(TAG, errorMsg);
                        } catch (Exception e) {
                            Log.e(TAG, "Erreur lors de la lecture du corps d'erreur", e);
                        }
                    }
                    Toast.makeText(EventMapActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    loadMockEvents();
                }

                updateEmptyState();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Event>>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Échec de la requête API", t);
                Toast.makeText(EventMapActivity.this,
                        "Erreur de connexion: " + t.getMessage(), Toast.LENGTH_LONG).show();

                loadMockEvents();
                updateEmptyState();
            }
        });
    }

    private void loadMockEvents() {
        eventList.clear();

        Event event1 = new Event();
        event1.setId(Long.valueOf("1"));
        event1.setTitre("Concert au parc");
        event1.setDescription("Concert en plein air");
        event1.setDate("15 mai 2025");
        event1.setLieu("Parc central");
        event1.setLatitude(48.8566);
        event1.setLongitude(2.3522);

        Event event2 = new Event();
        event2.setId(Long.valueOf("2"));
        event2.setTitre("Exposition d'art");
        event2.setDescription("Exposition temporaire");
        event2.setDate("20 mai 2025");
        event2.setLieu("Musée d'art moderne");
        event2.setLatitude(48.8606);
        event2.setLongitude(2.3376);

        Event event3 = new Event();
        event3.setId(Long.valueOf("3"));
        event3.setTitre("Festival de cinéma");
        event3.setDescription("Projections en plein air");
        event3.setDate("25 mai 2025");
        event3.setLieu("Place de la République");
        event3.setLatitude(48.8674);
        event3.setLongitude(2.3640);

        eventList.add(event1);
        eventList.add(event2);
        eventList.add(event3);

        showAllEvents();
    }

    private void showAllEvents() {
        filteredEventList.clear();
        filteredEventList.addAll(eventList);

        if (myMap != null) {
            addEventsToMap(filteredEventList);
        }

        eventCardsAdapter.updateEvents(filteredEventList);
    }

    private void showLoading(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void updateEmptyState() {
        emptyView.setVisibility(filteredEventList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void filterEvents(String query) {
        filteredEventList.clear();
        for (Event event : eventList) {
            if (event.getTitre().toLowerCase().contains(query.toLowerCase()) ||
                    event.getLieu().toLowerCase().contains(query.toLowerCase())) {
                filteredEventList.add(event);
            }
        }

        if (myMap != null) {
            addEventsToMap(filteredEventList);
        }

        eventCardsAdapter.updateEvents(filteredEventList);
        updateEmptyState();
    }

    private void addEventsToMap(List<Event> events) {
        if (myMap == null) return;

        myMap.clear();
        markerEventMap.clear();
        eventMarkerMap.clear();

        for (Event event : events) {
            if (event.hasValidLocation()) {
                LatLng eventLocation = event.getLatLng();
                Marker marker = myMap.addMarker(new MarkerOptions()
                        .position(eventLocation)
                        .title(event.getTitre())
                        .snippet(event.getDate())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                if (marker != null) {
                    markerEventMap.put(marker, event);
                    eventMarkerMap.put(event, marker);
                }
            }
        }

        // Configuration du clic sur les marqueurs
        myMap.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();
            return false;
        });

        // Configuration du clic sur la fenêtre d'information
        myMap.setOnInfoWindowClickListener(marker -> {
            Event event = markerEventMap.get(marker);
            if (event != null) {
                Intent intent = new Intent(EventMapActivity.this, EventDetailActivity.class);
                intent.putExtra("event", event);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                Log.d(TAG, "Navigation vers les détails de l'événement: " + event.getTitre());
            } else {
                Log.e(TAG, "Événement non trouvé pour ce marqueur");
                Toast.makeText(EventMapActivity.this,
                        "Erreur: Impossible de charger les détails de l'événement",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLocation = location;
                        if (myMap != null) {
                            showCurrentLocationOnMap();
                        }
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;

        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                myMap.setMyLocationEnabled(true);
                myMap.getUiSettings().setMyLocationButtonEnabled(true);
                myMap.getUiSettings().setZoomControlsEnabled(true);
                myMap.getUiSettings().setCompassEnabled(true);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Error enabling location", e);
        }

        if (currentLocation != null) {
            showCurrentLocationOnMap();
        } else {
            // Position par défaut (Dakar) si la localisation n'est pas disponible
            LatLng defaultLocation = new LatLng(14.6928, -17.4467);
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
        }

        if (!filteredEventList.isEmpty()) {
            addEventsToMap(filteredEventList);
        }
    }

    private void showCurrentLocationOnMap() {
        if (myMap == null || currentLocation == null) return;

        LatLng myLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        myMap.addMarker(new MarkerOptions()
                .position(myLatLng)
                .title("Vous êtes ici")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Permission de localisation refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }
}