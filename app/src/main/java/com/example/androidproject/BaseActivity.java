package com.example.androidproject;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        // Initialisation de la barre de navigation
        setupBottomNavigation();
    }

    protected abstract int getLayoutId();
    protected abstract int getActiveTabId();

    private void setupBottomNavigation() {
        BottomNavHelper navHelper = new BottomNavHelper(this, this);
        navHelper.setupBottomNavigation(
                findViewById(R.id.nav_home),
                findViewById(R.id.nav_explore),
                findViewById(R.id.nav_notifications),
                getActiveTabId()
        );
    }
}