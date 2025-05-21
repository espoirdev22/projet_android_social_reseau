package com.example.androidproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        holder.eventTitle.setText(event.getTitre());

        // Formatage de la date
        try {
            Date eventDate = inputDateFormat.parse(event.getDate());
            if (eventDate != null) {
                holder.eventDay.setText(dateFormat.format(eventDate));
                holder.eventMonth.setText(monthFormat.format(eventDate).toUpperCase());

                // Formatage de l'heure et du lieu
                String timeAndLocation = timeFormat.format(eventDate) + " • " + event.getLieu();
                holder.eventTimeLocation.setText(timeAndLocation);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            // Gérer le cas où la date ne peut pas être parsée
            holder.eventDay.setText("--");
            holder.eventMonth.setText("---");
            holder.eventTimeLocation.setText("--:-- • " + event.getLieu());
        }

        // Couleur de fond aléatoire ou basée sur la catégorie
        int[] colors = {0xFF3FBAD5, 0xFF3EAB8D, 0xFF272727};
        int colorIndex = position % colors.length;
        holder.dateBackground.setBackgroundColor(colors[colorIndex]);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventDay, eventMonth, eventTitle, eventTimeLocation, eventOrganizer;
        View dateBackground;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventDay = itemView.findViewById(R.id.event_day);
            eventMonth = itemView.findViewById(R.id.event_month);
            eventTitle = itemView.findViewById(R.id.event_title);
            eventTimeLocation = itemView.findViewById(R.id.event_time_location);
            eventOrganizer = itemView.findViewById(R.id.event_organizer);
            dateBackground = itemView.findViewById(R.id.date_background);
        }
    }
}