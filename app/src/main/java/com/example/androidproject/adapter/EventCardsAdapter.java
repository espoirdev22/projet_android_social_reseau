package com.example.androidproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidproject.R;
import com.example.androidproject.model.Event;

import java.util.ArrayList;
import java.util.List;

public class EventCardsAdapter extends RecyclerView.Adapter<EventCardsAdapter.EventCardViewHolder> {

    private List<Event> eventList = new ArrayList<>();
    private OnEventCardClickListener listener;

    public interface OnEventCardClickListener {
        void onEventCardClick(Event event);
    }

    public EventCardsAdapter(OnEventCardClickListener listener) {
        this.listener = listener;
    }

    public void updateEvents(List<Event> events) {
        this.eventList.clear();
        this.eventList.addAll(events);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_card, parent, false);
        return new EventCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventCardViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    class EventCardViewHolder extends RecyclerView.ViewHolder {
        private ImageView eventImage;
        private TextView eventTitle;
        private TextView eventDate;
        private TextView eventLocation;

        public EventCardViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventLocation = itemView.findViewById(R.id.eventLocation);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEventCardClick(eventList.get(position));
                }
            });
        }

        public void bind(Event event) {
            eventTitle.setText(event.getTitre());
            eventDate.setText(event.getDate());
            eventLocation.setText(event.getLieu());

            // Vous pouvez ajouter ici la logique pour charger des images
            // Par exemple avec Glide ou Picasso si vous avez des URLs d'images
            Glide.with(itemView.getContext())
                .load(event.getImage())
              .placeholder(R.drawable.ic_event_placeholder)
               .into(eventImage);

            // Pour l'instant, on utilise une image par défaut
           // eventImage.setImageResource(R.drawable.ic_event_placeholder);
        }
    }
}