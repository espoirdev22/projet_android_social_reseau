package com.example.androidproject.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Event implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("id")
    private Long id;

    private String time;

    @SerializedName("titre")
    private String titre;

    @SerializedName("description")
    private String description;

    @SerializedName("date")
    private String date;

    @SerializedName("lieu")
    private String lieu;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("image")
    private String image; // Ceci contiendra le Base64 ou l'URL de l'image

    @SerializedName("user_id")
    private Long userId;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    private String eventType;
    private String price;
    private String organizerName;
    private String location; // pour la localisation textuelle

    // Constructeurs
    public Event() {}

    public Event(Long id, String titre, String description, String date,
                 String lieu, double latitude, double longitude) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.date = date;
        this.lieu = lieu;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    // Alias pour getT() si vous l'utilisez dans votre code existant
    public String getT() {
        return time;
    }

    public void setT(String time) {
        this.time = time;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getLocation() {
        return location != null ? location : lieu;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getLatitude() {
        return latitude != null ? latitude : 0.0;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude != null ? longitude : 0.0;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getEventType() {
        return eventType != null ? eventType : "Événement";
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Méthodes utilitaires
    public boolean hasValidLocation() {
        return latitude != null && longitude != null && latitude != 0.0 && longitude != 0.0;
    }

    public LatLng getLatLng() {
        return new LatLng(getLatitude(), getLongitude());
    }

    public String getFormattedPrice() {
        if (price == null || price.isEmpty()) {
            return "Gratuit";
        }
        return price;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", date='" + date + '\'' +
                ", lieu='" + lieu + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", userId=" + userId +
                '}';
    }
}