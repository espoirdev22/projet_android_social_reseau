package com.example.androidproject.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.EventDetailActivity;
import com.example.androidproject.R;
import com.example.androidproject.model.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault());
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat fallbackDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public EventAdapter(List<Event> eventList) {
        this.eventList = eventList;
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

        // Titre de l'événement
        holder.eventTitle.setText(event.getTitre());

        // Formatage et affichage de la date
        Date eventDate = parseEventDate(event.getDate());
        if (eventDate != null) {
            SimpleDateFormat displayDateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.FRENCH);
            holder.eventDate.setText(displayDateFormat.format(eventDate));
            holder.eventTime.setText(timeFormat.format(eventDate));
        } else {
            holder.eventDate.setText("Date à définir");
            holder.eventTime.setText("Heure à définir");
        }

        // Lieu de l'événement
        holder.eventLocation.setText(event.getLieu() != null ? event.getLieu() : "Lieu à définir");

        // Type d'événement (vous pouvez adapter selon vos besoins)
        if (event.hasValidLocation()) {
            holder.eventType.setText("Présentiel");
            holder.eventIcon.setImageResource(R.drawable.ic_location_on);
        } else {
            holder.eventType.setText("En ligne");
            holder.eventIcon.setImageResource(R.drawable.ic_video_camera);
        }

        // Prix (à adapter selon votre modèle Event)
        holder.eventPrice.setText("Gratuit"); // Ou event.getFormattedPrice() si vous avez cette méthode

        // Gestion des images si présentes
        if (holder.eventImage != null && event.getImage() != null && !event.getImage().isEmpty()) {
            // Ici vous pouvez utiliser Glide, Picasso ou une autre bibliothèque pour charger l'image
            // Glide.with(holder.itemView.getContext()).load(event.getImage()).into(holder.eventImage);
        }

        // Listener pour ouvrir la page de détail
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), EventDetailActivity.class);
            intent.putExtra("event", event);
            holder.itemView.getContext().startActivity(intent);
        });
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

        // Essayer d'abord le format avec l'heure
        try {
            return inputDateFormat.parse(dateString);
        } catch (ParseException e1) {
            // Essayer le format date seulement
            try {
                return fallbackDateFormat.parse(dateString);
            } catch (ParseException e2) {
                e2.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Met à jour la liste des événements
     */
    public void updateEvents(List<Event> newEvents) {
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
        }
    }

    /**
     * Supprime un événement de la liste
     */
    public void removeEvent(int position) {
        if (eventList != null && position >= 0 && position < eventList.size()) {
            eventList.remove(position);
            notifyItemRemoved(position);
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