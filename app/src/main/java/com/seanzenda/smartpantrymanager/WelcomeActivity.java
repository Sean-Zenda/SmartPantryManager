package com.seanzenda.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.seanzenda.smartpantrymanager.util.Insets;
import com.seanzenda.smartpantrymanager.util.Prefs;

/**
 * The launcher screen. Shows the welcome message and the Get Started button the first time the app
 * is opened, and skips straight to the pantry on every launch after that.
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Returning user: go straight to the pantry without ever inflating this screen.
        if (Prefs.isOnboarded(this)) {
            openPantry();
            return;
        }

        setContentView(R.layout.activity_welcome);
        Insets.applySystemBars(findViewById(R.id.welcome_root), true);

        findViewById(R.id.btn_get_started).setOnClickListener(v -> {
            Prefs.setOnboarded(this);
            openPantry();
        });
    }

    /** Explicit Intent to the host activity. finish() removes this screen from the back stack. */
    private void openPantry() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
