package com.example.androidproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.example.androidproject.HomeActivity;
// Importez votre classe NotificationsActivity
// import com.example.androidproject.NotificationsActivity;

public class BottomNavHelper {

    private final Context context;
    private final Activity currentActivity;
    // Couleur pour l'onglet actif - #FF6347 (tomato)
    private final int ACTIVE_TAB_COLOR = Color.parseColor("#FF6347");

    public BottomNavHelper(Context context, Activity currentActivity) {
        this.context = context;
        this.currentActivity = currentActivity;
    }

    public void setupBottomNavigation(
            LinearLayout homeLayout,
            LinearLayout exploreLayout,
            LinearLayout notificationsLayout,
            int currentTab) {

        // Initialisation correcte des listeners
        if (homeLayout != null) {
            homeLayout.setOnClickListener(v -> navigateTo(HomeActivity.class));
        }
        if (exploreLayout != null) {
            exploreLayout.setOnClickListener(v -> navigateTo(EventMapActivity.class));
        }
        if (notificationsLayout != null) {
            // Ajout du listener pour les notifications
            notificationsLayout.setOnClickListener(v -> {
                // Si vous avez une activité NotificationsActivity, utilisez celle-ci:
                 navigateTo(NotificationsActivity.class);

                // Si vous n'avez pas encore créé cette activité,
                // vous pouvez temporairement afficher un message
                android.widget.Toast.makeText(
                        context,
                        "Notifications bientôt disponibles",
                        android.widget.Toast.LENGTH_SHORT
                ).show();
            });
        }

        highlightCurrentTab(currentTab);
    }

    private void navigateTo(Class<?> targetActivity) {
        if (!currentActivity.getClass().equals(targetActivity)) {
            Intent intent = new Intent(context, targetActivity);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            currentActivity.finish();
        }
    }

    private void highlightCurrentTab(int currentTab) {
        resetTabColors();

        // Trouver le layout parent de l'onglet actif
        View tabView = null;
        if (currentTab == R.id.nav_home) {
            tabView = currentActivity.findViewById(R.id.nav_home);
        } else if (currentTab == R.id.nav_explore) {
            tabView = currentActivity.findViewById(R.id.nav_explore);
        } else if (currentTab == R.id.nav_notifications) {
            tabView = currentActivity.findViewById(R.id.nav_notifications);
        }

        if (tabView != null) {
            // Trouver l'icône et le texte dans ce layout
            ImageView icon = findImageViewInTab(tabView);
            TextView text = findTextViewInTab(tabView);

            if (icon != null) {
                icon.setColorFilter(ACTIVE_TAB_COLOR);
            }
            if (text != null) {
                text.setTextColor(ACTIVE_TAB_COLOR);
            }
        }
    }

    private void resetTabColors() {
        // Réinitialiser les couleurs de tous les textes
        int[] textIds = {R.id.nav_text_home, R.id.nav_text_explore, R.id.nav_text_notifications};
        for (int id : textIds) {
            TextView textView = currentActivity.findViewById(id);
            if (textView != null) {
                // Utiliser une couleur grise du système
                textView.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            }
        }

        // Réinitialiser les couleurs de toutes les icônes
        int[] iconIds = {R.id.nav_icon_home, R.id.nav_icon_explore, R.id.nav_icon_notifications};
        for (int id : iconIds) {
            ImageView imageView = currentActivity.findViewById(id);
            if (imageView != null) {
                imageView.clearColorFilter();
            }
        }
    }

    private String getTabName(int tabId) {
        if (tabId == R.id.nav_home) return "home";
        if (tabId == R.id.nav_explore) return "explorer";
        if (tabId == R.id.nav_notifications) return "notifications";
        return "";
    }

    private TextView findTextViewInTab(View tab) {
        if (tab instanceof LinearLayout) {
            LinearLayout layout = (LinearLayout) tab;
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                if (child instanceof TextView) {
                    return (TextView) child;
                }
            }
        }
        return null;
    }

    private ImageView findImageViewInTab(View tab) {
        if (tab instanceof LinearLayout) {
            LinearLayout layout = (LinearLayout) tab;
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                if (child instanceof ImageView) {
                    return (ImageView) child;
                }
            }
        }
        return null;
    }
}