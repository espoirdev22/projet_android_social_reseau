package com.example.androidproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidproject.model.Event;

public class EventDetailActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageButton btnShare;
    private ImageButton btnBookmark;
    private ImageView eventBanner;
    private TextView eventTitle;
    private TextView eventDate;
    private TextView eventTime;
    private TextView eventType;
    private TextView eventLocation;
    private TextView eventRating;
    private TextView organizerName;
    private TextView eventPrice;
    private Button btnJoinRegister;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        initViews();
        setupClickListeners();
        loadEventData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        btnBookmark = findViewById(R.id.btnBookmark);
        eventBanner = findViewById(R.id.eventBanner);
        eventTitle = findViewById(R.id.eventTitle);
        eventDate = findViewById(R.id.eventDate);
        eventTime = findViewById(R.id.eventTime);
        eventType = findViewById(R.id.eventType);
        eventLocation = findViewById(R.id.eventLocation);
        eventRating = findViewById(R.id.eventRating);
        organizerName = findViewById(R.id.organizerName);
        eventPrice = findViewById(R.id.eventPrice);
        btnJoinRegister = findViewById(R.id.btnJoinRegister);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Découvrez cet événement : " + currentEvent.getTitre());
            startActivity(Intent.createChooser(shareIntent, "Partager"));
        });

        btnBookmark.setOnClickListener(v -> {
            // Toggle bookmark state
            Toast.makeText(this, "Événement ajouté aux favoris", Toast.LENGTH_SHORT).show();
        });

        btnJoinRegister.setOnClickListener(v -> {
            Toast.makeText(this, "Inscription réussie !", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadEventData() {
        // Récupérer l'événement depuis l'intent
        currentEvent = (Event) getIntent().getSerializableExtra("event");

        if (currentEvent != null) {
            populateEventData();
        } else {
            // Données d'exemple si aucun événement n'est passé
            loadSampleData();
        }

        // Animation d'entrée
        animateContent();
    }

    private void animateContent() {
        eventTitle.setAlpha(0f);
        eventDate.setAlpha(0f);
        eventTime.setAlpha(0f);

        eventTitle.animate().alpha(1f).setDuration(500).setStartDelay(200).start();
        eventDate.animate().alpha(1f).setDuration(500).setStartDelay(400).start();
        eventTime.animate().alpha(1f).setDuration(500).setStartDelay(600).start();
    }

    private void populateEventData() {
        eventTitle.setText(currentEvent.getTitre());
        eventDate.setText(formatDate(currentEvent.getDate()));
        eventTime.setText(formatTime(currentEvent.getT()));
        eventLocation.setText(currentEvent.getLocation());

        // Définir le type d'événement
        eventType.setText(currentEvent.getEventType());

        // Prix
        eventPrice.setText(currentEvent.getFormattedPrice());

        // Note et organisateur
        eventRating.setText("4.2 ⭐⭐⭐⭐⭐");
        organizerName.setText(currentEvent.getOrganizerName() != null ?
                currentEvent.getOrganizerName() : "Organisateur");

        // Charger l'image si disponible
        if (currentEvent.getImageUrl() != null && !currentEvent.getImageUrl().isEmpty()) {
            // Ici vous pourriez utiliser Glide ou Picasso pour charger l'image
            // Glide.with(this).load(currentEvent.getImageUrl()).into(eventBanner);
        }
    }

    private void loadSampleData() {
        eventTitle.setText("Hands-On Data Cleaning in R with Regular Expressions");
        eventDate.setText("vendredi, mai 30, 2025");
        eventTime.setText("16:00 - 18:00 GMT+00:00");
        eventType.setText("Événement en ligne");
        eventLocation.setText("Lien visible par les participants");
        eventRating.setText("4.2 ⭐⭐⭐⭐⭐");
        organizerName.setText("Unilorin R Users Group");
        eventPrice.setText("Gratuit");
    }

    private String formatDate(String date) {
        if (date == null) return "";
        // Formatage de la date selon vos besoins
        return date;
    }

    private String formatTime(String time) {
        if (time == null) return "";
        // Formatage de l'heure selon vos besoins
        return time;
    }
}