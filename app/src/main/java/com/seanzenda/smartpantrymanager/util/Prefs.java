package com.seanzenda.smartpantrymanager.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Small wrapper around {@link SharedPreferences} for the handful of app settings.
 *
 * <p>Settings are simple key/value flags, so SharedPreferences is the right tool for them. The
 * pantry and recipes are structured, relational data and live in SQLite instead.</p>
 */
public final class Prefs {

    private static final String FILE = "smart_pantry_prefs";

    private static final String KEY_ONBOARDED = "onboarded";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    private static final String KEY_METRIC = "units_metric";

    private Prefs() { }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    /** True once the user has tapped Get Started, so the welcome screen is only shown once. */
    public static boolean isOnboarded(Context context) {
        return prefs(context).getBoolean(KEY_ONBOARDED, false);
    }

    public static void setOnboarded(Context context) {
        prefs(context).edit().putBoolean(KEY_ONBOARDED, true).apply();
    }

    /** Whether items close to their expiry date are highlighted. On by default. */
    public static boolean isExpiryAlertsOn(Context context) {
        return prefs(context).getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public static void setExpiryAlertsOn(Context context, boolean on) {
        prefs(context).edit().putBoolean(KEY_EXPIRY_ALERTS, on).apply();
    }

    /** Metric (g, ml, kg) or imperial (oz, lb, cups) units as the default on the Add form. */
    public static boolean isMetric(Context context) {
        return prefs(context).getBoolean(KEY_METRIC, true);
    }

    public static void setMetric(Context context, boolean metric) {
        prefs(context).edit().putBoolean(KEY_METRIC, metric).apply();
    }
}
