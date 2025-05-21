package com.example.androidproject.model;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

public class Event {
    private String id;
    private String titre;
    private String description;
    private String date;
    private String lieu;
    private String image;
    private double latitude;
    private double longitude;

    public Event() {}

    public Event(String id, String titre, String description, String date,
                 String lieu, String image, double latitude, double longitude) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.date = date;
        this.lieu = lieu;
        this.image = image;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    // Getters and setters

    public void setId(String id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // Méthode utilitaire pour vérifier si l'événement a une position valide
    public boolean hasValidLocation() {
        return latitude != 0 && longitude != 0;
    }

    // Méthode pour obtenir l'objet LatLng pour Google Maps
    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }

    public void setLocation(String location) {
    }

    private String userId;
    private Uri imageUri;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setImageUri(String uri) {
        this.imageUri = Uri.parse(uri);
    }

    public Uri getImageUri() {
        return imageUri;
    }

}