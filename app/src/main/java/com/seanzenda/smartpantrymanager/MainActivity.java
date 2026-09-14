package com.seanzenda.smartpantrymanager;

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.seanzenda.smartpantrymanager.data.DatabaseHelper;
import com.seanzenda.smartpantrymanager.ui.ExpiringFragment;
import com.seanzenda.smartpantrymanager.ui.PantryFragment;
import com.seanzenda.smartpantrymanager.ui.RecipesFragment;
import com.seanzenda.smartpantrymanager.ui.SettingsFragment;
import com.seanzenda.smartpantrymanager.util.DateUtils;
import com.seanzenda.smartpantrymanager.util.Insets;
import com.seanzenda.smartpantrymanager.util.Prefs;

/**
 * The host activity. It owns the bottom navigation bar and swaps one Fragment in and out of
 * {@code R.id.fragment_container} for each tab: Pantry, Recipes, Expiring and Settings.
 *
 * <p>The Add / Edit Ingredient and Recipe Detail screens are separate Activities, opened with
 * explicit Intents that carry the id of the record to show.</p>
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The bottom navigation bar pads itself for the gesture bar, so only pad the top here.
        Insets.applySystemBars(findViewById(R.id.main), false);

        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            showTab(item.getItemId());
            return true;
        });
        // Tapping the tab that is already open should not rebuild it.
        bottomNav.setOnItemReselectedListener(item -> { });

        // Only choose the first tab on a fresh start. After a rotation the FragmentManager has
        // already restored the fragment that was showing, and the nav bar restores its selection.
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_pantry);
        }
    }

    /**
     * onResume runs on first start and every time the user comes back from Add / Edit or Recipe
     * Detail, which are exactly the moments the number of expiring items can have changed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        refreshExpiringBadge();
    }

    /** Lets a fragment switch tabs, e.g. the "Go to Pantry" button on the empty recipes screen. */
    public void selectTab(@IdRes int itemId) {
        bottomNav.setSelectedItemId(itemId);
    }

    /** Shows how many items are expiring soon on the Expiring tab, or hides the badge at zero. */
    public void refreshExpiringBadge() {
        int count = Prefs.isExpiryAlertsOn(this)
                ? DatabaseHelper.getInstance(this).getExpiringCount(DateUtils.soonCutoff())
                : 0;

        if (count > 0) {
            BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_expiring);
            badge.setNumber(count);
            badge.setBackgroundColor(ContextCompat.getColor(this, R.color.status_warn));
            badge.setBadgeTextColor(ContextCompat.getColor(this, R.color.white));
            badge.setVisible(true);
        } else {
            bottomNav.removeBadge(R.id.nav_expiring);
        }
    }

    private void showTab(@IdRes int itemId) {
        Fragment fragment;
        if (itemId == R.id.nav_recipes) {
            fragment = new RecipesFragment();
        } else if (itemId == R.id.nav_expiring) {
            fragment = new ExpiringFragment();
        } else if (itemId == R.id.nav_settings) {
            fragment = new SettingsFragment();
        } else {
            fragment = new PantryFragment();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
