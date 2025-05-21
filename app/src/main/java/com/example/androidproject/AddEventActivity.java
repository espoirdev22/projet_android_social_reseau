package com.example.androidproject;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.androidproject.api.ApiClient;
import com.example.androidproject.api.ApiService;
import com.example.androidproject.model.ApiResponse;
import com.example.androidproject.model.Event;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEventActivity extends AppCompatActivity {

    private static final String TAG = "AddEventActivity";

    // Utilisation des constantes centralisées
    private static final String PREFS_NAME = AppConstants.PREFS_NAME;
    private static final String TOKEN_KEY = AppConstants.TOKEN_KEY;
    private static final String USER_ID_KEY = AppConstants.USER_ID_KEY;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int IMAGE_PICKER_REQUEST_CODE = 102;

    // ======================== COMPOSANTS UI ========================
    private EditText titleEditText;
    private EditText descriptionEditText;
    private TextView dateTimeTextView;
    private Button dateTimeButton;
    private Button saveEventButton;

    // Composants pour la localisation
    private EditText locationSearchEditText;
    private LinearLayout selectedLocationLayout;
    private TextView selectedAddressTextView;
    private TextView selectedCoordinatesTextView;
    private View clearLocationButton;

    // Composants cachés pour compatibilité
    private EditText latitudeEditText;
    private EditText longitudeEditText;
    private EditText locationEditText;

    // Composants pour l'image
    private ImageView eventImageView;
    private View selectImageButton;

    // ======================== VARIABLES DE LOCALISATION ========================
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;
    private String selectedAddress = "";
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Geocoder geocoder;

    // ======================== VARIABLES DATE/HEURE ========================
    private Calendar selectedDateTime;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat apiDateFormat; // Format pour l'API

    // ======================== VARIABLES IMAGE ========================
    private Uri selectedImageUri;

    // ======================== API ========================
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        initializeComponents();
        setupEventListeners();
        initializeDateTime();
        initializeLocation();
        initializeApi();

        // Vérifier l'authentification dès le début
        checkAuthentication();
    }

    private void checkAuthentication() {
        String token = getAuthToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token d'authentification non trouvé");
            Toast.makeText(this, "Vous devez être connecté pour créer un événement", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        } else {
            Log.d(TAG, "Authentification OK: " + token.substring(0, Math.min(10, token.length())) + "...");
        }
    }

    private void initializeComponents() {
        // Champs principaux
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        dateTimeTextView = findViewById(R.id.dateTimeTextView);
        dateTimeButton = findViewById(R.id.dateTimeButton);
        saveEventButton = findViewById(R.id.saveEventButton);

        // Localisation
        locationSearchEditText = findViewById(R.id.locationSearchEditText);
        selectedLocationLayout = findViewById(R.id.selectedLocationLayout);
        selectedAddressTextView = findViewById(R.id.selectedAddressTextView);
        selectedCoordinatesTextView = findViewById(R.id.selectedCoordinatesTextView);
        clearLocationButton = findViewById(R.id.clearLocationButton);

        // Champs cachés
        latitudeEditText = findViewById(R.id.latitudeEditText);
        longitudeEditText = findViewById(R.id.longitudeEditText);
        locationEditText = findViewById(R.id.locationEditText);

        // Image components
        eventImageView = findViewById(R.id.eventImageView);
        selectImageButton = findViewById(R.id.selectImageButton);

        // Cacher les composants de localisation au début
        selectedLocationLayout.setVisibility(View.GONE);
    }

    private void setupEventListeners() {
        dateTimeButton.setOnClickListener(v -> showDateTimePicker());
        saveEventButton.setOnClickListener(v -> saveEvent());
        clearLocationButton.setOnClickListener(v -> clearLocation());

        // Listener pour la recherche d'adresse
        locationSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 3) {
                    searchLocation(s.toString());
                }
            }
        });

        // Listener pour la sélection d'image
        selectImageButton.setOnClickListener(v -> selectImage());
    }

    private void initializeDateTime() {
        selectedDateTime = Calendar.getInstance();
        // Format pour l'affichage à l'utilisateur
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        // Format pour l'API (format ISO)
        apiDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        updateDateTimeDisplay();
    }

    private void initializeLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());
    }

    private void initializeApi() {
        apiService = ApiClient.getClient().create(ApiService.class);
        Log.d(TAG, "API service initialized");
    }

    private void showDateTimePicker() {
        Calendar calendar = selectedDateTime;

        new android.app.DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    new android.app.TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                selectedDateTime = calendar;
                                updateDateTimeDisplay();
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true).show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateTimeDisplay() {
        dateTimeTextView.setText(dateFormat.format(selectedDateTime.getTime()));
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICKER_REQUEST_CODE);
    }

    private void searchLocation(String query) {
        if (geocoder != null) {
            try {
                List<Address> addresses = geocoder.getFromLocationName(query, 1);
                if (!addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    setSelectedLocation(address.getLatitude(), address.getLongitude(), formatAddress(address));
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur lors de la recherche d'adresse", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void clearLocation() {
        selectedLatitude = 0.0;
        selectedLongitude = 0.0;
        selectedAddress = "";
        selectedLocationLayout.setVisibility(View.GONE);
        locationSearchEditText.setText("");

        latitudeEditText.setText("");
        longitudeEditText.setText("");
        locationEditText.setText("");

        Toast.makeText(this, "Localisation supprimée", Toast.LENGTH_SHORT).show();
    }

    private void setSelectedLocation(double latitude, double longitude, String address) {
        selectedLatitude = latitude;
        selectedLongitude = longitude;
        selectedAddress = address;

        selectedAddressTextView.setText(address);
        selectedCoordinatesTextView.setText(
                String.format(Locale.getDefault(), "Lat: %.6f, Lng: %.6f", latitude, longitude)
        );
        selectedLocationLayout.setVisibility(View.VISIBLE);

        latitudeEditText.setText(String.valueOf(latitude));
        longitudeEditText.setText(String.valueOf(longitude));
        locationEditText.setText(address);

        locationSearchEditText.setText("");
    }

    private String formatAddress(Address address) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(address.getAddressLine(i));
        }

        if (sb.length() == 0) {
            if (address.getThoroughfare() != null) {
                sb.append(address.getThoroughfare());
            }
            if (address.getLocality() != null) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(address.getLocality());
            }
            if (address.getCountryName() != null) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(address.getCountryName());
            }
        }

        return sb.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && requestCode == IMAGE_PICKER_REQUEST_CODE) {
            selectedImageUri = data.getData();
            eventImageView.setImageURI(selectedImageUri);
            eventImageView.setVisibility(View.VISIBLE);
        }
    }

    private void saveEvent() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String dateTime = dateTimeTextView.getText().toString().trim();

        if (title.isEmpty()) {
            titleEditText.setError("Le titre est obligatoire");
            titleEditText.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            descriptionEditText.setError("La description est obligatoire");
            descriptionEditText.requestFocus();
            return;
        }

        if (dateTime.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner une date et une heure", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérifier si l'utilisateur est connecté
        String token = getAuthToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vous devez être connecté pour créer un événement", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        // Créer correctement l'objet Event
        Event event = new Event();
        event.setTitre(title);
        event.setDescription(description);

        // Utiliser le format de date ISO pour l'API
        event.setDate(apiDateFormat.format(selectedDateTime.getTime()));

        // Définir la localisation avec les noms de champs corrects
        if (selectedLatitude != 0.0 && selectedLongitude != 0.0) {
            event.setLatitude(selectedLatitude);
            event.setLongitude(selectedLongitude);
            // Utiliser setLieu() qui sera mappé à "lieu" dans le JSON
            event.setLieu(selectedAddress);
        } else {
            String locationText = locationSearchEditText.getText().toString().trim();
            if (!locationText.isEmpty()) {
                event.setLieu(locationText);
            }
        }

        // Définir l'image si elle est sélectionnée
        if (selectedImageUri != null) {
            event.setImage(selectedImageUri.toString());
        }

        // Le user_id est géré par le serveur avec Auth::id()
        // Donc pas besoin de le définir ici

        Log.d(TAG, "Creating event: " + event.toString());
        saveEventToApi(event);
    }

    private void saveEventToApi(Event event) {
        String token = getAuthToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Erreur d'authentification", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        saveEventButton.setEnabled(false);
        saveEventButton.setText("Enregistrement...");

        String authHeader = "Bearer " + token;

        Log.d(TAG, "Sending event to API with auth token: " + authHeader.substring(0, Math.min(15, authHeader.length())) + "...");
        Log.d(TAG, "Event data: " + event.toString());

        Call<ApiResponse<Event>> call = apiService.createEvent(authHeader, event);

        call.enqueue(new Callback<ApiResponse<Event>>() {
            @Override
            public void onResponse(Call<ApiResponse<Event>> call, Response<ApiResponse<Event>> response) {
                saveEventButton.setEnabled(true);
                saveEventButton.setText("Enregistrer l'événement");

                Log.d(TAG, "API Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Event creation successful");
                    ApiResponse<Event> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        Toast.makeText(AddEventActivity.this,
                                apiResponse.getMessage() != null ? apiResponse.getMessage() : "Événement créé avec succès",
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(AddEventActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e(TAG, "API error: " + apiResponse.getMessage());
                        Toast.makeText(AddEventActivity.this,
                                apiResponse.getMessage() != null ? apiResponse.getMessage() : "Erreur lors de la création",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error response: " + errorBody + ", code: " + response.code());
                        Toast.makeText(AddEventActivity.this, "Erreur: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Cannot read error body", e);
                    }

                    if (response.code() == 401) {
                        Toast.makeText(AddEventActivity.this, "Session expirée", Toast.LENGTH_SHORT).show();
                        redirectToLogin();
                    } else {
                        Toast.makeText(AddEventActivity.this, "Erreur serveur: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Event>> call, Throwable t) {
                saveEventButton.setEnabled(true);
                saveEventButton.setText("Enregistrer l'événement");

                Log.e(TAG, "Network error", t);
                Toast.makeText(AddEventActivity.this, "Erreur réseau: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getAuthToken() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String token = prefs.getString(TOKEN_KEY, null);
        Log.d(TAG, "Token récupéré: " + (token != null ? token.substring(0, Math.min(10, token.length())) + "..." : "null"));
        return token;
    }

    private Long getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long userId = prefs.getLong(USER_ID_KEY, -1);
        Log.d(TAG, "User ID récupéré: " + (userId != -1 ? userId : "non défini"));
        return userId;
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public static String getAuthToken(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        return prefs.getString(TOKEN_KEY, null);
    }

    public static Long getUserId(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        long userId = prefs.getLong(USER_ID_KEY, -1);
        return userId != -1 ? userId : null;
    }

    // Méthode pour obtenir la localisation actuelle
    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            try {
                                List<Address> addresses = geocoder.getFromLocation(
                                        location.getLatitude(), location.getLongitude(), 1);

                                if (!addresses.isEmpty()) {
                                    Address address = addresses.get(0);
                                    setSelectedLocation(location.getLatitude(), location.getLongitude(), formatAddress(address));
                                } else {
                                    setSelectedLocation(location.getLatitude(), location.getLongitude(), "Emplacement actuel");
                                }
                            } catch (IOException e) {
                                Log.e(TAG, "Geocoder error", e);
                                setSelectedLocation(location.getLatitude(), location.getLongitude(), "Emplacement actuel");
                            }
                        } else {
                            Toast.makeText(AddEventActivity.this, "Impossible d'obtenir la localisation actuelle", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Permission de localisation refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Demander confirmation si l'utilisateur a commencé à remplir le formulaire
        if (!titleEditText.getText().toString().isEmpty() ||
                !descriptionEditText.getText().toString().isEmpty() ||
                selectedImageUri != null) {

            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("Quitter sans enregistrer ?");
            builder.setMessage("Vous avez des modifications non enregistrées. Êtes-vous sûr de vouloir quitter ?");
            builder.setPositiveButton("Quitter", (dialog, which) -> {
                super.onBackPressed();
            });
            builder.setNegativeButton("Rester", (dialog, which) -> {
                dialog.dismiss();
            });
            builder.show();
        } else {
            super.onBackPressed();
        }
    }
}