package com.example.androidproject.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ApiResponse<T> {
    // Champs pour différents formats de statut
    @SerializedName(value = "status", alternate = {"success", "isSuccess"})
    private String status;

    // Champs pour différents formats de données
    @SerializedName(value = "data", alternate = {"user", "users", "event", "events", "result", "results", "items", "content"})
    private T data;

    // Champ pour l'autorisation (spécifique au login)
    @SerializedName("authorization")
    private Authorization authorization;

    // Champs pour différents formats de message
    @SerializedName(value = "message", alternate = {"msg", "error", "error_message", "statusMessage"})
    private String message;

    // Champ token direct (pour certaines APIs)
    @SerializedName("token")
    private String directToken;

    // Classe interne pour l'autorisation
    public static class Authorization {
        @SerializedName("token")
        private String token;

        @SerializedName("type")
        private String type;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "Authorization{" +
                    "token='" + token + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    // Constructeurs
    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, T data) {
        this.status = success ? "success" : "error";
        this.message = message;
        this.data = data;
    }

    public ApiResponse(boolean success, String message, T data, String token) {
        this.status = success ? "success" : "error";
        this.message = message;
        this.data = data;
        this.directToken = token;
    }

    // Méthode pour déterminer le succès (générique)
    public boolean isSuccess() {
        if (status != null) {
            // Gère les formats: "success", "true", "1"
            return status.equalsIgnoreCase("success") ||
                    status.equalsIgnoreCase("true") ||
                    status.equals("1");
        }
        return false;
    }

    // Méthode générique pour récupérer les données
    public T getData() {
        return data;
    }

    // Méthode intelligente pour récupérer le token
    public String getToken() {
        // Essaie d'abord le token via authorization
        if (authorization != null && authorization.getToken() != null) {
            return authorization.getToken();
        }
        // Puis le token direct
        if (directToken != null) {
            return directToken;
        }
        return null;
    }

    // Getters et setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Authorization getAuthorization() {
        return authorization;
    }

    public void setAuthorization(Authorization authorization) {
        this.authorization = authorization;
    }

    public String getDirectToken() {
        return directToken;
    }

    public void setDirectToken(String directToken) {
        this.directToken = directToken;
    }

    // Méthodes utilitaires
    public boolean hasData() {
        return data != null;
    }

    public boolean hasToken() {
        return getToken() != null && !getToken().isEmpty();
    }

    public boolean hasMessage() {
        return message != null && !message.isEmpty();
    }

    // Méthode pour obtenir la taille des données si c'est une liste
    public int getDataSize() {
        if (data instanceof List) {
            return ((List<?>) data).size();
        }
        return data != null ? 1 : 0;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "status='" + status + '\'' +
                ", data=" + (data instanceof List ? "List[size=" + ((List<?>) data).size() + "]" : data) +
                ", authorization=" + authorization +
                ", message='" + message + '\'' +
                ", directToken='" + (directToken != null ? "***" : null) + '\'' +
                '}';
    }
}