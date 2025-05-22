package com.example.androidproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.pusher.client.connection.ConnectionEventListener;
import com.example.androidproject.adapter.NotificationAdapter;
import com.example.androidproject.api.ApiClient;
import com.example.androidproject.api.ApiService;
import com.example.androidproject.model.ApiResponse;
import com.example.androidproject.model.NotificationItem;
import com.example.androidproject.model.Event;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListener {

    private static final String TAG = "NotificationsActivity";
    private static final String PREFS_NAME = AppConstants.PREFS_NAME;
    private static final String TOKEN_KEY = AppConstants.TOKEN_KEY;
    private static final String USER_ID_KEY = AppConstants.USER_ID_KEY;

    // Pusher configuration
    private static final String PUSHER_APP_KEY = "eb2bdc7cc9b23e6cec1b";
    private static final String PUSHER_CLUSTER = "mt1";
    private static final String EVENTS_CHANNEL = "events-channel";

    // UI Components
    private RecyclerView notificationsRecyclerView;
    private NotificationAdapter notificationAdapter;
    private List<NotificationItem> notificationsList;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private TextView todayHeaderTextView;

    // API
    private ApiService apiService;

    // Pusher
    private Pusher pusher;
    private Channel channel;

    // Bottom Navigation
    private BottomNavHelper bottomNavHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Log.d(TAG, "onCreate: Starting NotificationsActivity");

        initializeComponents();
        setupRecyclerView();
        initializeApi();
        setupBottomNavigation();

        // IMPORTANT : D'abord masquer le progress bar
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        // Charger immédiatement les données mockées
        loadMockNotifications();

        // Ensuite essayer l'API (qui peut échouer)
        loadNotifications();

        setupPusher();
    }

    private void initializeComponents() {
        Log.d(TAG, "initializeComponents: Initializing UI components");

        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyTextView = findViewById(R.id.emptyTextView);
        todayHeaderTextView = findViewById(R.id.todayHeaderTextView);

        notificationsList = new ArrayList<>();

        // Vérifier que les vues sont trouvées
        if (notificationsRecyclerView == null) {
            Log.e(TAG, "notificationsRecyclerView is null!");
        }
        if (progressBar == null) {
            Log.e(TAG, "progressBar is null!");
        }
        if (emptyTextView == null) {
            Log.e(TAG, "emptyTextView is null!");
        }
        if (todayHeaderTextView == null) {
            Log.e(TAG, "todayHeaderTextView is null!");
        }
    }

    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: Setting up RecyclerView");

        if (notificationsRecyclerView != null) {
            notificationAdapter = new NotificationAdapter(this, notificationsList);
            notificationAdapter.setOnNotificationClickListener(this);
            notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            notificationsRecyclerView.setAdapter(notificationAdapter);
            Log.d(TAG, "RecyclerView setup complete");
        } else {
            Log.e(TAG, "Cannot setup RecyclerView - view is null");
        }
    }

    private void setupBottomNavigation() {
        View bottomNavContainer = findViewById(R.id.bottomNavContainer);
        if (bottomNavContainer != null) {
            LinearLayout homeNav = bottomNavContainer.findViewById(R.id.nav_home);
            LinearLayout exploreNav = bottomNavContainer.findViewById(R.id.nav_explore);
            LinearLayout notificationsNav = bottomNavContainer.findViewById(R.id.nav_notifications);

            if (homeNav != null && exploreNav != null && notificationsNav != null) {
                bottomNavHelper = new BottomNavHelper(this, this);
                bottomNavHelper.setupBottomNavigation(homeNav, exploreNav, notificationsNav, R.id.nav_notifications);
            } else {
                Log.e(TAG, "Bottom navigation views not found");
            }
        }
    }

    private void initializeApi() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    private void loadMockNotifications() {
        Log.d(TAG, "loadMockNotifications: Loading mock data immediately");

        List<NotificationItem> mockNotifications = new ArrayList<>();

        // Créer quelques notifications fictives pour test
        NotificationItem notification1 = new NotificationItem();
        notification1.setTitle("Bienvenue !");
        notification1.setMessage("Bienvenue dans l'application d'événements. Explorez les fonctionnalités disponibles.");
        notification1.setTimestamp("Il y a 5 minutes");
        notification1.setType("info");
        notification1.setRead(false);

        NotificationItem notification2 = new NotificationItem();
        notification2.setTitle("Nouvel événement disponible");
        notification2.setMessage("Un nouvel événement intéressant a été ajouté près de votre localisation.");
        notification2.setTimestamp("Il y a 1 heure");
        notification2.setType("event");
        notification2.setRead(false);

        NotificationItem notification3 = new NotificationItem();
        notification3.setTitle("Rappel important");
        notification3.setMessage("N'oubliez pas de consulter les événements de cette semaine qui pourraient vous intéresser.");
        notification3.setTimestamp("Il y a 2 heures");
        notification3.setType("reminder");
        notification3.setRead(true);

        mockNotifications.add(notification1);
        mockNotifications.add(notification2);
        mockNotifications.add(notification3);

        Log.d(TAG, "Created " + mockNotifications.size() + " mock notifications");

        // IMPORTANT : Forcer l'affichage même sans attendre l'API
        updateNotificationsList(mockNotifications);

        // Double sécurité : forcer l'état visible après un délai
        new android.os.Handler().postDelayed(() -> {
            if (notificationsList != null && !notificationsList.isEmpty()) {
                showNotificationsState();
            }
        }, 100);
    }


    private void loadNotifications() {
        String token = getAuthToken();
        if (token == null || token.isEmpty()) {
            Log.w(TAG, "No auth token found, keeping mock data");
            return; // Garder les données mockées
        }

        Log.d(TAG, "loadNotifications: Loading from API");
        String authHeader = "Bearer " + token;

        // Tentative 1: Essayer de récupérer les vraies notifications
        Call<ApiResponse<List<NotificationItem>>> notificationsCall = apiService.getNotifications(authHeader);
        notificationsCall.enqueue(new Callback<ApiResponse<List<NotificationItem>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<NotificationItem>>> call,
                                   Response<ApiResponse<List<NotificationItem>>> response) {
                Log.d(TAG, "Notifications API response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<NotificationItem>> apiResponse = response.body();
                    if ("success".equals(apiResponse.getStatus()) && apiResponse.getData() != null) {
                        Log.d(TAG, "Successfully loaded " + apiResponse.getData().size() + " notifications from API");
                        updateNotificationsList(apiResponse.getData());
                        return;
                    } else {
                        Log.w(TAG, "Notifications API returned status != success or null data");
                    }
                } else {
                    Log.w(TAG, "Notifications endpoint failed with code: " + response.code());
                }

                // Fallback: essayer de charger les événements
                loadEventsAsNotifications(authHeader);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<NotificationItem>>> call, Throwable t) {
                Log.w(TAG, "Notifications endpoint failed, trying events as fallback", t);
                loadEventsAsNotifications(authHeader);
            }
        });
    }

    private void loadEventsAsNotifications(String authHeader) {
        Log.d(TAG, "loadEventsAsNotifications: Loading events as notifications fallback");

        Call<ApiResponse<List<Event>>> eventsCall = apiService.getEvents(authHeader);
        eventsCall.enqueue(new Callback<ApiResponse<List<Event>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Event>>> call,
                                   Response<ApiResponse<List<Event>>> response) {
                Log.d(TAG, "Events API response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Event>> apiResponse = response.body();
                    if ("success".equals(apiResponse.getStatus()) && apiResponse.getData() != null) {
                        Log.d(TAG, "Successfully loaded " + apiResponse.getData().size() + " events");
                        List<NotificationItem> notifications = convertEventsToNotifications(apiResponse.getData());
                        if (!notifications.isEmpty()) {
                            updateNotificationsList(notifications);
                        } else {
                            Log.d(TAG, "No events converted to notifications, keeping mock data");
                        }
                    } else {
                        Log.w(TAG, "Events API returned status != success or null data, keeping mock data");
                    }
                } else {
                    Log.e(TAG, "Events API failed with code: " + response.code() + ", keeping mock data");
                    if (response.code() == 401) {
                        Toast.makeText(NotificationsActivity.this, "Session expirée", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Event>>> call, Throwable t) {
                Log.e(TAG, "Events API network error, keeping mock data", t);
                if (t instanceof java.net.UnknownHostException ||
                        t instanceof java.net.ConnectException ||
                        t instanceof java.net.SocketTimeoutException) {
                    Toast.makeText(NotificationsActivity.this, "Pas de connexion internet", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private List<NotificationItem> convertEventsToNotifications(List<Event> events) {
        List<NotificationItem> notifications = new ArrayList<>();

        for (Event event : events) {
            NotificationItem notification = new NotificationItem();
            notification.setTitle("Événement : " + (event.getTitre() != null ? event.getTitre() : "Sans titre"));

            StringBuilder message = new StringBuilder();
            if (event.getDate() != null) {
                message.append("Date : ").append(formatEventDate(event.getDate()));
            }
            if (event.getLieu() != null && !event.getLieu().trim().isEmpty()) {
                if (message.length() > 0) message.append("\n");
                message.append("Lieu : ").append(event.getLieu());
            }

            notification.setMessage(message.length() > 0 ? message.toString() : "Détails de l'événement à venir");
            notification.setTimestamp(event.getCreatedAt() != null ? getRelativeTime(event.getCreatedAt()) : "Récemment");
            notification.setType("event");
            notification.setRead(false);



            notifications.add(notification);
        }

        Log.d(TAG, "Converted " + events.size() + " events to " + notifications.size() + " notifications");
        return notifications;
    }

    // Modifiez aussi la méthode updateNotificationsList pour forcer la mise à jour :

    private void updateNotificationsList(List<NotificationItem> notifications) {
        Log.d(TAG, "updateNotificationsList: Updating with " + notifications.size() + " notifications");

        runOnUiThread(() -> {
            if (notificationsList != null && notificationAdapter != null) {
                notificationsList.clear();
                notificationsList.addAll(notifications);

                // Notifier l'adapter AVANT de changer les vues
                notificationAdapter.notifyDataSetChanged();

                Log.d(TAG, "Adapter updated with " + notificationsList.size() + " items");

                // Gérer l'affichage selon le nombre de notifications
                if (notifications.isEmpty()) {
                    showEmptyState();
                } else {
                    // IMPORTANT : Appeler showNotificationsState() après avoir mis à jour l'adapter
                    showNotificationsState();

                    // Double vérification pour forcer l'affichage
                    View notificationsContainer = findViewById(R.id.notificationsContainer);
                    if (notificationsContainer != null && notificationsContainer.getVisibility() != View.VISIBLE) {
                        Log.w(TAG, "Container still not visible, forcing visibility");
                        notificationsContainer.setVisibility(View.VISIBLE);
                    }
                }

                debugViewStates(); // Pour debugging
            } else {
                Log.e(TAG, "Cannot update notifications list - adapter or list is null");
            }
        });
    }

    // 5. AJOUTER une méthode de debugging améliorée
    private void debugViewStates() {
        Log.d(TAG, "=== DEBUG VIEW STATES ===");

        View notificationsContainer = findViewById(R.id.notificationsContainer);
        if (notificationsContainer != null) {
            Log.d(TAG, "Container visibility: " + getVisibilityString(notificationsContainer.getVisibility()));
        }

        if (notificationsRecyclerView != null) {
            Log.d(TAG, "RecyclerView visibility: " + getVisibilityString(notificationsRecyclerView.getVisibility()));
            Log.d(TAG, "RecyclerView adapter item count: " +
                    (notificationsRecyclerView.getAdapter() != null ?
                            notificationsRecyclerView.getAdapter().getItemCount() : "null adapter"));
        }

        if (todayHeaderTextView != null) {
            Log.d(TAG, "Today header visibility: " + getVisibilityString(todayHeaderTextView.getVisibility()));
        }

        if (emptyTextView != null) {
            Log.d(TAG, "Empty text visibility: " + getVisibilityString(emptyTextView.getVisibility()));
        }

        if (progressBar != null) {
            Log.d(TAG, "Progress bar visibility: " + getVisibilityString(progressBar.getVisibility()));
        }

        Log.d(TAG, "Notifications list size: " + (notificationsList != null ? notificationsList.size() : "null"));
        Log.d(TAG, "========================");
    }

    private String getVisibilityString(int visibility) {
        switch (visibility) {
            case View.VISIBLE: return "VISIBLE";
            case View.GONE: return "GONE";
            case View.INVISIBLE: return "INVISIBLE";
            default: return "UNKNOWN";
        }
    }
    private void showNotificationsState() {
        Log.d(TAG, "showNotificationsState: Showing notifications");

        runOnUiThread(() -> {
            // CRITIQUE : Rendre le container visible AVANT tout le reste
            View notificationsContainer = findViewById(R.id.notificationsContainer);
            if (notificationsContainer != null) {
                notificationsContainer.setVisibility(View.VISIBLE);
                Log.d(TAG, "notificationsContainer set to VISIBLE");
            }

            // Ensuite masquer les autres états
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Progress bar set to GONE");
            }
            if (emptyTextView != null) {
                emptyTextView.setVisibility(View.GONE);
                Log.d(TAG, "Empty text set to GONE");
            }

            // Enfin rendre visible le contenu des notifications
            if (notificationsRecyclerView != null) {
                notificationsRecyclerView.setVisibility(View.VISIBLE);
                Log.d(TAG, "RecyclerView set to VISIBLE");
            }
            if (todayHeaderTextView != null) {
                todayHeaderTextView.setVisibility(View.VISIBLE);
                Log.d(TAG, "Today header set to VISIBLE");
            }
        });
    }



    private void showEmptyState() {
        Log.d(TAG, "showEmptyState: Showing empty state");

        View notificationsContainer = findViewById(R.id.notificationsContainer);
        if (notificationsContainer != null) {
            notificationsContainer.setVisibility(View.GONE);
        }
        if (notificationsRecyclerView != null) {
            notificationsRecyclerView.setVisibility(View.GONE);
        }
        if (todayHeaderTextView != null) {
            todayHeaderTextView.setVisibility(View.GONE);
        }
        if (emptyTextView != null) {
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText("Aucune notification");
        }
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    // Méthodes pour Pusher
    private void setupPusher() {
        try {
            PusherOptions options = new PusherOptions();
            options.setCluster(PUSHER_CLUSTER);

            pusher = new Pusher(PUSHER_APP_KEY, options);

            pusher.connect(new ConnectionEventListener() {
                @Override
                public void onConnectionStateChange(ConnectionStateChange change) {
                    Log.i(TAG, "Pusher state changed from " + change.getPreviousState() +
                            " to " + change.getCurrentState());

                    runOnUiThread(() -> {
                        if (change.getCurrentState() == ConnectionState.CONNECTED) {
                            Log.d(TAG, "Pusher connected successfully");
                        }
                    });
                }

                @Override
                public void onError(String message, String code, Exception e) {
                    Log.e(TAG, "Pusher connection error: " + message + ", code: " + code, e);
                }
            }, ConnectionState.ALL);

            channel = pusher.subscribe(EVENTS_CHANNEL);

            channel.bind("event.created", new SubscriptionEventListener() {
                @Override
                public void onEvent(PusherEvent event) {
                    Log.i(TAG, "Received new event notification: " + event.getData());

                    runOnUiThread(() -> {
                        try {
                            NotificationItem notification = createEventNotification(event.getData());
                            addNewNotification(notification);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing pusher event", e);
                        }
                    });
                }
            });

            channel.bind("event.updated", new SubscriptionEventListener() {
                @Override
                public void onEvent(PusherEvent event) {
                    Log.i(TAG, "Received event update notification: " + event.getData());

                    runOnUiThread(() -> {
                        try {
                            NotificationItem notification = createEventUpdateNotification(event.getData());
                            addNewNotification(notification);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing pusher event update", e);
                        }
                    });
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error setting up Pusher", e);
        }
    }

    private NotificationItem createEventNotification(String eventData) {
        NotificationItem notification = new NotificationItem();

        try {
            Gson gson = new Gson();
            JsonObject eventObject = gson.fromJson(eventData, JsonObject.class);

            String eventTitle = eventObject.has("titre") ?
                    eventObject.get("titre").getAsString() : "Nouvel événement";
            String eventDate = eventObject.has("date") ?
                    eventObject.get("date").getAsString() : "";
            String eventLocation = eventObject.has("lieu") ?
                    eventObject.get("lieu").getAsString() : "";

            notification.setTitle("Nouvel événement : " + eventTitle);

            StringBuilder message = new StringBuilder();
            if (!eventDate.isEmpty()) {
                message.append("Date : ").append(formatEventDate(eventDate));
            }
            if (!eventLocation.isEmpty()) {
                if (message.length() > 0) message.append("\n");
                message.append("Lieu : ").append(eventLocation);
            }

            notification.setMessage(message.toString());
            notification.setTimestamp(getCurrentTimestamp());
            notification.setType("event");
            notification.setRead(false);

            if (eventObject.has("id")) {
                notification.setEventId(eventObject.get("id").getAsLong());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing event data", e);
            notification.setTitle("Nouvel événement créé");
            notification.setMessage("Un nouvel événement a été ajouté");
            notification.setTimestamp(getCurrentTimestamp());
            notification.setType("event");
            notification.setRead(false);
        }

        return notification;
    }

    private NotificationItem createEventUpdateNotification(String eventData) {
        NotificationItem notification = new NotificationItem();

        try {
            Gson gson = new Gson();
            JsonObject eventObject = gson.fromJson(eventData, JsonObject.class);

            String eventTitle = eventObject.has("titre") ?
                    eventObject.get("titre").getAsString() : "Événement";

            notification.setTitle("Événement modifié : " + eventTitle);
            notification.setMessage("Un événement a été mis à jour");
            notification.setTimestamp(getCurrentTimestamp());
            notification.setType("event");
            notification.setRead(false);

            if (eventObject.has("id")) {
                notification.setEventId(eventObject.get("id").getAsLong());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing event update data", e);
            notification.setTitle("Événement modifié");
            notification.setMessage("Un événement a été mis à jour");
            notification.setTimestamp(getCurrentTimestamp());
            notification.setType("event");
            notification.setRead(false);
        }

        return notification;
    }

    private void addNewNotification(NotificationItem notification) {
        if (notification != null && notificationsList != null && notificationAdapter != null) {
            Log.d(TAG, "Adding new notification: " + notification.getTitle());

            notificationsList.add(0, notification);
            notificationAdapter.notifyItemInserted(0);

            if (emptyTextView != null && emptyTextView.getVisibility() == View.VISIBLE) {
                showNotificationsState();
            }

            if (notificationsRecyclerView != null) {
                notificationsRecyclerView.scrollToPosition(0);
            }

            Toast.makeText(this, "Nouvelle notification : " + notification.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    // Méthodes utilitaires
    private String formatEventDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date: " + dateString, e);
            return dateString;
        }
    }

    private String getCurrentTimestamp() {
        return "Il y a quelques secondes";
    }

    private String getRelativeTime(String createdAt) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
            Date eventDate = inputFormat.parse(createdAt);
            Date now = new Date();

            long diffInMillis = now.getTime() - eventDate.getTime();
            long diffInHours = diffInMillis / (1000 * 60 * 60);
            long diffInDays = diffInHours / 24;

            if (diffInDays > 0) {
                return "Il y a " + diffInDays + " jour" + (diffInDays > 1 ? "s" : "");
            } else if (diffInHours > 0) {
                return "Il y a " + diffInHours + " heure" + (diffInHours > 1 ? "s" : "");
            } else {
                return "Il y a quelques minutes";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date", e);
            return "Récemment";
        }
    }

    private String getAuthToken() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(TOKEN_KEY, null);
    }

    // Implémentation de OnNotificationClickListener
    @Override
    public void onNotificationClick(NotificationItem notification) {
        Log.d(TAG, "Notification clicked: " + notification.getTitle());

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationAdapter.notifyDataSetChanged();
        }

        switch (notification.getType()) {
            case "event":
                if (notification.getEventId() != null) {
                    navigateToEventDetails(notification.getEventId());
                } else {
                    navigateToEventsList();
                }
                break;

            case "info":
            case "reminder":
                showNotificationDetails(notification);
                break;

            default:
                navigateToEventMap();
                break;
        }
    }

    private void navigateToEventsList() {
        try {
            Intent intent = new Intent(this, EventMapActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Cannot navigate to events list", e);
            Toast.makeText(this, "Navigation vers les événements non disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToEventMap() {
        try {
            Intent intent = new Intent(this, EventMapActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Cannot navigate to event map", e);
            Toast.makeText(this, "Navigation vers la carte non disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNotificationDetails(NotificationItem notification) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(notification.getTitle())
                .setMessage(notification.getMessage() + "\n\nReçu: " + notification.getTimestamp())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("Voir les événements", (dialog, which) -> {
                    dialog.dismiss();
                    navigateToEventMap();
                });

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void navigateToEventDetails(Long eventId) {
        // Navigation vers les détails de l'événement si l'activité existe
        Log.d(TAG, "Navigate to event details for ID: " + eventId);
        // Intent intent = new Intent(this, EventDetailsActivity.class);
        // intent.putExtra("event_id", eventId);
        // startActivity(intent);

        // Pour l'instant, aller à la liste des événements
        navigateToEventsList();
    }

    // Méthodes de cycle de vie
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Activity resumed");

        // Recharger les notifications si nécessaire
        if (notificationsList.isEmpty()) {
            loadMockNotifications();
            loadNotifications();
        }

        if (pusher != null && pusher.getConnection().getState() != ConnectionState.CONNECTED) {
            setupPusher();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectPusher();
    }

    private void disconnectPusher() {
        try {
            if (channel != null) {
                pusher.unsubscribe(EVENTS_CHANNEL);
            }
            if (pusher != null) {
                pusher.disconnect();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error disconnecting Pusher", e);
        }
    }

    public void addNotificationFromBroadcast(NotificationItem notification) {
        runOnUiThread(() -> addNewNotification(notification));
    }
}