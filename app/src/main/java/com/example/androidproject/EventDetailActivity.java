package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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
    private TextView eventDescription;
    private ImageView btnAddToCalendar;
    private ImageView btnShowLocation;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        initViews();
        setupClickListeners();
        loadEventData();

        getSupportActionBar().hide();
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
        eventDescription = findViewById(R.id.eventDescription);
        btnAddToCalendar = findViewById(R.id.btnAddToCalendar);
        btnShowLocation = findViewById(R.id.btnShowLocation);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnShare.setOnClickListener(v -> {
            if (currentEvent != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareText = "Découvrez cet événement : " + currentEvent.getTitre();
                if (currentEvent.getDate() != null) {
                    shareText += "\nDate : " + currentEvent.getDate();
                }
                if (currentEvent.getLocation() != null) {
                    shareText += "\nLieu : " + currentEvent.getLocation();
                }
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(shareIntent, "Partager"));
            }
        });

        btnBookmark.setOnClickListener(v -> {
            // Toggle bookmark state
            Toast.makeText(this, "Événement ajouté aux favoris", Toast.LENGTH_SHORT).show();
        });

        btnAddToCalendar.setOnClickListener(v -> {
            Toast.makeText(this, "Ajout au calendrier", Toast.LENGTH_SHORT).show();
            // Implémenter l'ajout au calendrier
        });

        btnShowLocation.setOnClickListener(v -> {
            if (currentEvent != null && currentEvent.getLocation() != null) {
                Toast.makeText(this, "Affichage de la localisation: " +
                        currentEvent.getLocation(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Lien de l'événement sera disponible pour les participants",
                        Toast.LENGTH_SHORT).show();
            }
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

        // Description
        if (currentEvent.getDescription() != null) {
            eventDescription.setText(currentEvent.getDescription());
        }

        // Charger l'image
        if (currentEvent.getImage() != null && !currentEvent.getImage().isEmpty()) {
            // Ici vous pourriez utiliser Glide ou Picasso pour charger l'image
             Glide.with(this).load(currentEvent.getImage()).into(eventBanner);
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
        eventDescription.setText("Cette session pratique vous permettra d'apprendre à nettoyer des données efficacement en utilisant R et les expressions régulières. Parfait pour les débutants et intermédiaires qui souhaitent améliorer leurs compétences en préparation de données.");
    }

    private String formatDate(String date) {
        if (date == null || date.isEmpty()) {
            return "Date à définir";
        }
        // Vous pouvez ajouter ici une logique de formatage plus sophistiquée
        return date;
    }

    private String formatTime(String time) {
        if (time == null || time.isEmpty()) {
            return "Heure à définir";
        }
        // Vous pouvez ajouter ici une logique de formatage plus sophistiquée
        return time;
    }
}