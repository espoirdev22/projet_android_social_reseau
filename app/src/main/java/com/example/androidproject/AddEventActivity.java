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
import java.text.ParseException;
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

    // ======================== COMPOSANTS UI ========================
    private EditText titleEditText;
    private EditText descriptionEditText;
    private TextView dateTimeTextView;
    private Button dateTimeButton;
    private Button saveEventButton;

    // Nouveaux composants pour la saisie de date
    private Button datePickerModeButton;
    private Button manualInputModeButton;
    private LinearLayout manualDateInputLayout;
    private LinearLayout datePickerLayout;
    private EditText manualDateEditText;

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

    // Composants pour l'URL d'image
    private EditText imageUrlEditText;
    private CardView imagePreviewCard;
    private ImageView imagePreviewView;

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
    private SimpleDateFormat manualInputFormat; // Format pour la saisie manuelle
    private boolean isManualInputMode = false;

    // ======================== VARIABLES IMAGE ========================
    private String imageUrl = "";

    // ======================== API ========================
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        getSupportActionBar().hide();


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

        // Nouveaux composants pour la saisie de date
        datePickerModeButton = findViewById(R.id.datePickerModeButton);
        manualInputModeButton = findViewById(R.id.manualInputModeButton);
        manualDateInputLayout = findViewById(R.id.manualDateInputLayout);
        datePickerLayout = findViewById(R.id.datePickerLayout);
        manualDateEditText = findViewById(R.id.manualDateEditText);

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

        // Composants pour l'URL d'image
        imageUrlEditText = findViewById(R.id.imageUrlEditText);
        imagePreviewCard = findViewById(R.id.imagePreviewCard);
        imagePreviewView = findViewById(R.id.imagePreviewView);

        // Cacher les composants de localisation au début
        selectedLocationLayout.setVisibility(View.GONE);

        AppCompatImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            // Redirect to HomeActivity
            Intent intent = new Intent(AddEventActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Optionally finish this activity
        });
    }

    private void setupEventListeners() {
        dateTimeButton.setOnClickListener(v -> showDateTimePicker());
        saveEventButton.setOnClickListener(v -> saveEvent());
        clearLocationButton.setOnClickListener(v -> clearLocation());

        // Listeners pour les modes de saisie de date
        datePickerModeButton.setOnClickListener(v -> switchToDatePickerMode());
        manualInputModeButton.setOnClickListener(v -> switchToManualInputMode());

        // Listener pour le clic sur le layout du sélecteur de date
        datePickerLayout.setOnClickListener(v -> {
            if (!isManualInputMode) {
                showDateTimePicker();
            }
        });

        // Listener pour la saisie manuelle de date
        manualDateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateAndParseManualDate(s.toString());
            }
        });

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

        // Listener pour l'URL d'image
        imageUrlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String url = s.toString().trim();
                if (isValidImageUrl(url)) {
                    imageUrl = url;
                    showImagePreview(url);
                } else {
                    imageUrl = "";
                    hideImagePreview();
                }
            }
        });
    }

    private boolean isValidImageUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        // PROBLÈME: Validation trop restrictive
        // SOLUTION: Ajouter validation des extensions d'image
        String urlLower = url.toLowerCase();
        return (url.startsWith("http://") || url.startsWith("https://")) &&
                (urlLower.contains(".jpg") || urlLower.contains(".jpeg") ||
                        urlLower.contains(".png") || urlLower.contains(".gif") ||
                        urlLower.contains(".bmp") || urlLower.contains(".webp"));
    }

    private void showImagePreview(String url) {
        // Ici vous pouvez utiliser une bibliothèque comme Picasso ou Glide pour charger l'image
        // Pour l'instant, on montre juste le placeholder
        imagePreviewCard.setVisibility(View.VISIBLE);

        // Exemple avec Picasso (si vous l'utilisez) :
        // Picasso.get().load(url).into(imagePreviewView);

        // Pour l'instant, affichage d'un placeholder
        Toast.makeText(this, "URL d'image détectée: " + url, Toast.LENGTH_SHORT).show();
    }

    private void hideImagePreview() {
        imagePreviewCard.setVisibility(View.GONE);
    }

    private void switchToDatePickerMode() {
        isManualInputMode = false;

        // Mettre à jour l'apparence des boutons
        datePickerModeButton.setBackgroundResource(R.drawable.secondary_button_background);
        datePickerModeButton.setTextColor(getResources().getColor(android.R.color.white));
        manualInputModeButton.setBackgroundResource(R.drawable.edit_text_background);
        manualInputModeButton.setTextColor(getResources().getColor(android.R.color.darker_gray));

        // Afficher/masquer les layouts appropriés
        datePickerLayout.setVisibility(View.VISIBLE);
        manualDateInputLayout.setVisibility(View.GONE);

        // Effacer le texte de saisie manuelle
        manualDateEditText.setText("");

        Log.d(TAG, "Mode sélecteur de date activé");
    }

    private void switchToManualInputMode() {
        isManualInputMode = true;

        // Mettre à jour l'apparence des boutons
        manualInputModeButton.setBackgroundResource(R.drawable.secondary_button_background);
        manualInputModeButton.setTextColor(getResources().getColor(android.R.color.white));
        datePickerModeButton.setBackgroundResource(R.drawable.edit_text_background);
        datePickerModeButton.setTextColor(getResources().getColor(android.R.color.darker_gray));

        // Afficher/masquer les layouts appropriés
        datePickerLayout.setVisibility(View.GONE);
        manualDateInputLayout.setVisibility(View.VISIBLE);

        Log.d(TAG, "Mode saisie manuelle activé");
    }

    private void validateAndParseManualDate(String input) {
        TextView validationText = findViewById(R.id.manualDateValidationText);

        if (input.isEmpty()) {
            validationText.setVisibility(View.GONE);
            selectedDateTime = null;
            return;
        }

        try {
            Calendar tempCalendar = Calendar.getInstance();
            tempCalendar.setTime(manualInputFormat.parse(input));

            // Vérifier que la date n'est pas dans le passé
            Calendar now = Calendar.getInstance();
            if (tempCalendar.before(now)) {
                validationText.setText("⚠️ La date ne peut pas être dans le passé");
                validationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                validationText.setVisibility(View.VISIBLE);
                selectedDateTime = null;
            } else {
                selectedDateTime = tempCalendar;
                validationText.setText("✓ Date valide");
                validationText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                validationText.setVisibility(View.VISIBLE);

                // Mettre à jour les champs cachés pour compatibilité
                updateDateTimeDisplay();
            }
        } catch (ParseException e) {
            validationText.setText("❌ Format incorrect (JJ/MM/AAAA HH:MM)");
            validationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            validationText.setVisibility(View.VISIBLE);
            selectedDateTime = null;
        }
    }

    private void initializeDateTime() {
        selectedDateTime = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.FRANCE);
        apiDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        manualInputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE);

        // Initialiser avec une date par défaut (dans 1 heure)
        selectedDateTime.add(Calendar.HOUR_OF_DAY, 1);
        updateDateTimeDisplay();
    }

    private void updateDateTimeDisplay() {
        if (selectedDateTime != null) {
            String formattedDate = dateFormat.format(selectedDateTime.getTime());

            if (!isManualInputMode) {
                // Mettre à jour l'affichage du sélecteur de date
                TextView datePreview = findViewById(R.id.fullDateTimePreview);
                if (datePreview != null) {
                    datePreview.setText("Date et heure : " + formattedDate);
                    datePreview.setVisibility(View.VISIBLE);
                }

                // Mettre à jour les textes des sélecteurs
                TextView selectedDateText = findViewById(R.id.selectedDateTextView);
                TextView selectedTimeText = findViewById(R.id.selectedTimeTextView);

                if (selectedDateText != null) {
                    SimpleDateFormat dateOnly = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
                    selectedDateText.setText(dateOnly.format(selectedDateTime.getTime()));
                    selectedDateText.setTextColor(getResources().getColor(android.R.color.black));
                }

                if (selectedTimeText != null) {
                    SimpleDateFormat timeOnly = new SimpleDateFormat("HH:mm", Locale.FRANCE);
                    selectedTimeText.setText(timeOnly.format(selectedDateTime.getTime()));
                    selectedTimeText.setTextColor(getResources().getColor(android.R.color.black));
                }
            }

            // Mettre à jour le champ caché pour compatibilité
            if (dateTimeTextView != null) {
                dateTimeTextView.setText(formattedDate);
            }
        }
    }

    private void showDateTimePicker() {
        if (isManualInputMode) return;

        // Afficher d'abord le sélecteur de date
        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Ensuite afficher le sélecteur d'heure
                    showTimePicker();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );

        // Empêcher la sélection de dates passées
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    selectedDateTime.set(Calendar.SECOND, 0);

                    updateDateTimeDisplay();
                    Toast.makeText(this, "Date et heure sélectionnées", Toast.LENGTH_SHORT).show();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true // Format 24h
        );

        timePickerDialog.show();
    }

    private void initializeLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        // Bouton de recherche
        Button searchButton = findViewById(R.id.searchLocationButton);
        if (searchButton != null) {
            searchButton.setOnClickListener(v -> {
                String query = locationSearchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    searchLocation(query);
                }
            });
        }
    }

    private void searchLocation(String query) {
        if (geocoder == null) return;

        try {
            List<Address> addresses = geocoder.getFromLocationName(query, 5);
            if (addresses != null && !addresses.isEmpty()) {
                Address bestMatch = addresses.get(0);

                selectedLatitude = bestMatch.getLatitude();
                selectedLongitude = bestMatch.getLongitude();
                selectedAddress = bestMatch.getAddressLine(0);

                // Mettre à jour l'affichage
                selectedAddressTextView.setText(selectedAddress);
                selectedCoordinatesTextView.setText(
                        String.format(Locale.getDefault(), "Lat: %.6f, Lng: %.6f",
                                selectedLatitude, selectedLongitude)
                );

                selectedLocationLayout.setVisibility(View.VISIBLE);

                // Mettre à jour les champs cachés
                latitudeEditText.setText(String.valueOf(selectedLatitude));
                longitudeEditText.setText(String.valueOf(selectedLongitude));
                locationEditText.setText(selectedAddress);

                Toast.makeText(this, "Localisation trouvée", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Aucune localisation trouvée", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de la recherche de localisation", e);
            Toast.makeText(this, "Erreur de recherche", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearLocation() {
        selectedLatitude = 0.0;
        selectedLongitude = 0.0;
        selectedAddress = "";

        selectedLocationLayout.setVisibility(View.GONE);
        locationSearchEditText.setText("");

        // Effacer les champs cachés
        latitudeEditText.setText("");
        longitudeEditText.setText("");
        locationEditText.setText("");

        Toast.makeText(this, "Localisation effacée", Toast.LENGTH_SHORT).show();
    }

    private void initializeApi() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    private void saveEvent() {
        // Validation des champs obligatoires
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

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

        if (selectedDateTime == null) {
            Toast.makeText(this, "Veuillez sélectionner une date et heure", Toast.LENGTH_SHORT).show();
            return;
        }

        // ==================== CORRECTION POUR L'URL D'IMAGE ====================
        Event event = new Event();

        // Champs avec @SerializedName
        event.setTitre(title);
        event.setDescription(description);
        event.setDate(apiDateFormat.format(selectedDateTime.getTime()));
        event.setLieu(selectedAddress.isEmpty() ? "Non spécifié" : selectedAddress);
        event.setLatitude(selectedLatitude);
        event.setLongitude(selectedLongitude);

        // CORRECTION: Récupérer directement depuis le champ EditText
        String imageUrlFromInput = imageUrlEditText.getText().toString().trim();

        // Log pour debug
        Log.d(TAG, "URL d'image saisie: " + imageUrlFromInput);
        Log.d(TAG, "Variable imageUrl: " + imageUrl);

        // Utiliser l'URL du champ de saisie si elle n'est pas vide
        if (!imageUrlFromInput.isEmpty()) {
            event.setImage(imageUrlFromInput);
            Log.d(TAG, "URL d'image assignée à l'événement: " + imageUrlFromInput);
        } else {
            event.setImage(null);
            Log.d(TAG, "Aucune URL d'image fournie");
        }

        // Champs additionnels
        event.setEventType("Événement");
        event.setPrice("Gratuit");
        event.setOrganizerName(null);
        event.setLocation(selectedAddress.isEmpty() ? "Non spécifié" : selectedAddress);

        // Désactiver le bouton pour éviter les doubles clics
        saveEventButton.setEnabled(false);
        saveEventButton.setText("Enregistrement...");

        // Appel API
        String token = getAuthToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Erreur d'authentification", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        Call<ApiResponse<Event>> call = apiService.createEvent("Bearer " + token, event);
        call.enqueue(new Callback<ApiResponse<Event>>() {
            @Override
            public void onResponse(Call<ApiResponse<Event>> call, Response<ApiResponse<Event>> response) {
                saveEventButton.setEnabled(true);
                saveEventButton.setText("Enregistrer l'événement");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Event> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(AddEventActivity.this, "Événement créé avec succès!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AddEventActivity.this,
                                "Erreur: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Erreur de réponse: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Corps de l'erreur: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Impossible de lire le corps de l'erreur", e);
                        }
                    }
                    Toast.makeText(AddEventActivity.this,
                            "Erreur lors de la création de l'événement (Code: " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Event>> call, Throwable t) {
                saveEventButton.setEnabled(true);
                saveEventButton.setText("Enregistrer l'événement");

                Log.e(TAG, "Erreur réseau", t);
                Toast.makeText(AddEventActivity.this,
                        "Erreur de connexion: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupImageUrlListener() {
        imageUrlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String url = s.toString().trim();

                // Log pour debug
                Log.d(TAG, "URL d'image modifiée: " + url);

                // Validation simple
                if (!url.isEmpty() && (url.startsWith("http://") || url.startsWith("https://"))) {
                    imageUrl = url; // Mettre à jour la variable globale
                    showImagePreview(url);
                    Log.d(TAG, "URL d'image valide assignée: " + url);
                } else {
                    imageUrl = ""; // Réinitialiser si invalide
                    hideImagePreview();
                    Log.d(TAG, "URL d'image invalide ou vide");
                }
            }
        });
    }

    private String getAuthToken() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(TOKEN_KEY, null);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Nettoyer les ressources si nécessaire
    }
}