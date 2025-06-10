package com.example.androidproject.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidproject.EventDetailActivity;
import com.example.androidproject.R;
import com.example.androidproject.model.ApiResponse;
import com.example.androidproject.model.Event;
import com.example.androidproject.api.ApiClient;
import com.example.androidproject.api.ApiService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private static final String TAG = "EventAdapter";
    private List<Event> eventList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault());
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat fallbackDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private ApiService apiService;
    private String authToken;

    private SimpleDateFormat inputDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat inputDateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat inputDateFormat3 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


    public EventAdapter(List<Event> eventList) {
        this.eventList = eventList;
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void setAuthToken(String token) {
        this.authToken = token;
        Log.d(TAG, "Auth token set: " + (token != null ? "Present" : "Null"));
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        // Debug - afficher les détails de l'événement
        Log.d(TAG, "Binding event: ID=" + event.getId() + ", Title=" + event.getTitre());

        // Titre de l'événement
        holder.eventTitle.setText(event.getTitre());

        // Formatage et affichage de la date
        Date eventDate = parseEventDate(event.getDate());
        if (eventDate != null) {
            SimpleDateFormat displayDateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.ENGLISH);
            holder.eventDate.setText(displayDateFormat.format(eventDate));
            holder.eventTime.setText(timeFormat.format(eventDate));
        } else {
            holder.eventDate.setText("Date à définir");
            holder.eventTime.setText("Heure à définir");
        }

        // Lieu de l'événement
        holder.eventLocation.setText(event.getLieu() != null ? event.getLieu() : "Lieu à définir");

        // Remplacer le prix par un bouton de suppression
        holder.eventPrice.setText("Supprimer");
        holder.eventPrice.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));

        // Listener pour la suppression
        holder.eventPrice.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                Log.d(TAG, "Delete button clicked for position: " + currentPosition + ", Event ID: " + event.getId());
                showDeleteConfirmationDialog(holder.itemView.getContext(), event, currentPosition);
            }
        });

        // Gestion des images si présentes
        if (holder.eventImage != null && event.getImage() != null && !event.getImage().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(event.getImage()).into(holder.eventImage);
        }

        // Listener pour ouvrir la page de détail
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), EventDetailActivity.class);
            intent.putExtra("event", event);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    private void showDeleteConfirmationDialog(android.content.Context context, Event event, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Supprimer l'événement");
        builder.setMessage("Êtes-vous sûr de vouloir supprimer l'événement \"" + event.getTitre() + "\" ?");

        builder.setPositiveButton("Supprimer", (dialog, which) -> {
            deleteEvent(context, event, position);
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }

    private void deleteEvent(android.content.Context context, Event event, int position) {
        Log.d(TAG, "=== DEBUT SUPPRESSION ===");
        Log.d(TAG, "Event ID: " + event.getId());
        Log.d(TAG, "Position: " + position);
        Log.d(TAG, "Auth Token: " + (authToken != null ? "Present (length: " + authToken.length() + ")" : "Null"));

        if (authToken == null || authToken.isEmpty()) {
            Log.e(TAG, "Auth token is null or empty");
            Toast.makeText(context, "Erreur d'authentification", Toast.LENGTH_SHORT).show();
            return;
        }

        if (event.getId() == null) {
            Log.e(TAG, "Event ID is null");
            Toast.makeText(context, "Erreur: ID de l'événement manquant", Toast.LENGTH_SHORT).show();
            return;
        }

        if (position < 0 || position >= eventList.size()) {
            Log.e(TAG, "Invalid position: " + position + ", list size: " + eventList.size());
            Toast.makeText(context, "Erreur: position invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        String authHeader = "Bearer " + authToken;
        Log.d(TAG, "Making API call to delete event with ID: " + event.getId());
        Log.d(TAG, "Auth header: Bearer " + authToken.substring(0, Math.min(10, authToken.length())) + "...");

        // Appel API pour supprimer l'événement
        Call<ApiResponse<Void>> call = apiService.deleteEvent(authHeader, event.getId());

        // Log de l'URL et des paramètres
        Log.d(TAG, "API Call URL: " + call.request().url());
        Log.d(TAG, "HTTP Method: " + call.request().method());

        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                Log.d(TAG, "=== REPONSE API ===");
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response successful: " + response.isSuccessful());

                if (response.errorBody() != null) {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e(TAG, "Error body: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }

                if (response.isSuccessful()) {
                    Log.d(TAG, "Event deleted successfully from server");
                    // Vérifier si la position est toujours valide
                    if (position >= 0 && position < eventList.size()) {
                        // Supprimer l'événement de la liste locale
                        eventList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, eventList.size());
                        Toast.makeText(context, "Événement supprimé avec succès", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Event removed from local list");
                    } else {
                        Log.w(TAG, "Position no longer valid after API call");
                    }
                } else {
                    // Gérer différents codes d'erreur
                    String errorMessage;
                    switch (response.code()) {
                        case 401:
                            errorMessage = "Session expirée, veuillez vous reconnecter";
                            Log.e(TAG, "401 Unauthorized - Token may be expired");
                            break;
                        case 403:
                            errorMessage = "Vous n'avez pas l'autorisation de supprimer cet événement";
                            Log.e(TAG, "403 Forbidden - Not authorized to delete this event");
                            break;
                        case 404:
                            errorMessage = "Événement non trouvé sur le serveur";
                            Log.e(TAG, "404 Not Found - Event or endpoint not found");
                            Log.e(TAG, "Requested URL: " + call.request().url());
                            break;
                        case 500:
                            errorMessage = "Erreur serveur interne";
                            Log.e(TAG, "500 Internal Server Error");
                            break;
                        default:
                            errorMessage = "Erreur lors de la suppression (Code: " + response.code() + ")";
                            Log.e(TAG, "HTTP Error " + response.code());
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                }
                Log.d(TAG, "=== FIN REPONSE API ===");
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e(TAG, "=== ECHEC API ===");
                Log.e(TAG, "API call failed", t);
                Log.e(TAG, "Error message: " + t.getMessage());
                Log.e(TAG, "Error class: " + t.getClass().getSimpleName());

                String errorMessage = "Erreur de connexion: " + t.getMessage();
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
        Log.d(TAG, "=== FIN SUPPRESSION ===");
    }

    @Override
    public int getItemCount() {
        return eventList != null ? eventList.size() : 0;
    }

    /**
     * Parse la date de l'événement en essayant différents formats
     */
    private Date parseEventDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        Log.d(TAG, "Parsing date: " + dateString);

        // Format 1: yyyy-MM-dd HH:mm:ss (votre format actuel)
        try {
            Date parsed = inputDateFormat1.parse(dateString);
            Log.d(TAG, "Successfully parsed with format 1: " + parsed);
            return parsed;
        } catch (ParseException e1) {
            Log.d(TAG, "Format 1 failed, trying format 2");
        }

        // Format 2: yyyy-MM-dd'T'HH:mm:ss (format ISO)
        try {
            Date parsed = inputDateFormat2.parse(dateString);
            Log.d(TAG, "Successfully parsed with format 2: " + parsed);
            return parsed;
        } catch (ParseException e2) {
            Log.d(TAG, "Format 2 failed, trying format 3");
        }

        // Format 3: yyyy-MM-dd (date seulement)
        try {
            Date parsed = inputDateFormat3.parse(dateString);
            Log.d(TAG, "Successfully parsed with format 3: " + parsed);
            return parsed;
        } catch (ParseException e3) {
            Log.w(TAG, "Failed to parse date with all formats: " + dateString);
            return null;
        }
    }
    /**
     * Met à jour la liste des événements
     */
    public void updateEvents(List<Event> newEvents) {
        Log.d(TAG, "Updating events list. New size: " + (newEvents != null ? newEvents.size() : 0));
        if (this.eventList != null) {
            this.eventList.clear();
            if (newEvents != null) {
                this.eventList.addAll(newEvents);
            }
        } else {
            this.eventList = newEvents;
        }
        notifyDataSetChanged();
    }

    /**
     * Ajoute un nouvel événement à la liste
     */
    public void addEvent(Event event) {
        if (eventList != null && event != null) {
            eventList.add(event);
            notifyItemInserted(eventList.size() - 1);
            Log.d(TAG, "Event added to list: " + event.getTitre());
        }
    }

    /**
     * Supprime un événement de la liste (méthode publique pour usage externe)
     */
    public void removeEvent(int position) {
        if (eventList != null && position >= 0 && position < eventList.size()) {
            Event removedEvent = eventList.get(position);
            eventList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, eventList.size());
            Log.d(TAG, "Event removed from list: " + removedEvent.getTitre());
        }
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle;
        TextView eventDate;
        TextView eventTime;
        TextView eventLocation;
        TextView eventType;
        TextView eventPrice;
        ImageView eventIcon;
        ImageView eventImage;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventTime = itemView.findViewById(R.id.eventTime);
            eventLocation = itemView.findViewById(R.id.eventLocation);
            eventType = itemView.findViewById(R.id.eventType);
            eventPrice = itemView.findViewById(R.id.eventPrice);
            eventIcon = itemView.findViewById(R.id.eventIcon);
            eventImage = itemView.findViewById(R.id.eventImage);
        }
    }
}