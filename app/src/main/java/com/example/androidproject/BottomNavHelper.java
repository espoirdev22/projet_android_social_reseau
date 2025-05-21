package com.example.androidproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.androidproject.EventMapActivity;
import com.example.androidproject.HomeActivity;

public class BottomNavHelper {

    private final Context context;
    private final Activity currentActivity;

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

        View tab = currentActivity.findViewById(currentTab);
        if (tab != null) {
            TextView textView = findTextViewInTab(tab);
            ImageView imageView = findImageViewInTab(tab);

            if (textView != null) {
                textView.setTextColor(context.getResources().getColor(R.color.active_color));
            }

            if (imageView != null) {
                try {
                    int resId = context.getResources().getIdentifier(
                            "ic_" + getTabName(currentTab) + "_active",
                            "drawable",
                            context.getPackageName());
                    if (resId != 0) {
                        imageView.setImageResource(resId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void resetTabColors() {
        int[] tabIds = {R.id.nav_home, R.id.nav_explore, R.id.nav_notifications};

        for (int id : tabIds) {
            View tab = currentActivity.findViewById(id);
            if (tab != null) {
                TextView textView = findTextViewInTab(tab);
                ImageView imageView = findImageViewInTab(tab);

                if (textView != null) {
                    textView.setTextColor(context.getResources().getColor(R.color.inactive_color));
                }

                if (imageView != null) {
                    try {
                        int resId = context.getResources().getIdentifier(
                                "ic_" + getTabName(id),
                                "drawable",
                                context.getPackageName());
                        if (resId != 0) {
                            imageView.setImageResource(resId);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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