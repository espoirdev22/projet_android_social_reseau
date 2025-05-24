package com.example.androidproject.model;

import com.google.android.gms.maps.model.LatLng;
import java.io.Serializable;

public class Event implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String titre;
    private String description;
    private String date;
    private String time; // ou getT() selon votre implémentation
    private String lieu;
    private String location; // pour la localisation textuelle
    private double latitude;
    private double longitude;
    private String eventType;
    private String price;
    private String organizerName;
    private String image;
    private String createdAt;

    // Constructeurs
    public Event() {}

    public Event(String id, String titre, String description, String date,
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
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    // Alias pour getT() si vous l'utilisez dans votre code existant
    public String getT() { return time; }
    public void setT(String time) { this.time = time; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public String getLocation() { return location != null ? location : lieu; }
    public void setLocation(String location) { this.location = location; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getEventType() { return eventType != null ? eventType : "Événement"; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Méthodes utilitaires
    public boolean hasValidLocation() {
        return latitude != 0.0 && longitude != 0.0;
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
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
                "id='" + id + '\'' +
                ", titre='" + titre + '\'' +
                ", date='" + date + '\'' +
                ", lieu='" + lieu + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}