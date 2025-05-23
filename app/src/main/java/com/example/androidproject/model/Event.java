package com.example.androidproject.model;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Event implements Serializable {
    private String id;
    private String titre;
    private String description;
    private String date;
    private String lieu;
    private String image;
    private double latitude;
    private double longitude;

    private String time; // Ajouté pour correspondre à getT()
    private String eventType; // Ajouté
    private String price; // Ajouté
    private String organizerName; // Ajouté
    @SerializedName("created_at")
    private String createdAt;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public String getT() {
        return time;
    }

    public void setT(String time) {
        this.time = time;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getFormattedPrice() {
        return price != null ? price : "Gratuit";
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getOrganizerName() {
        return organizerName != null ? organizerName : "Organisateur";
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public String getImageUrl() {
        return image;
    }



    public String getLocation() {
        return lieu;
    }

}