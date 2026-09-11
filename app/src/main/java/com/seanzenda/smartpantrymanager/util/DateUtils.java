package com.seanzenda.smartpantrymanager.util;

import android.content.Context;

import androidx.annotation.ColorRes;

import com.seanzenda.smartpantrymanager.R;
import com.seanzenda.smartpantrymanager.model.PantryItem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** Expiry date formatting and the "is this about to go off?" rules. */
public final class DateUtils {

    /** An item this many days (or fewer) from its expiry date counts as "expiring soon". */
    public static final int SOON_DAYS = 3;

    private DateUtils() { }

    /** "12 Apr 2026" */
    public static String format(long millis) {
        return new SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(new Date(millis));
    }

    /** Midnight at the start of the day containing {@code millis}. */
    public static long startOfDay(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /** Whole days from today until the date: 0 = today, 1 = tomorrow, negative = already expired. */
    public static int daysUntil(long millis) {
        long diff = startOfDay(millis) - startOfDay(System.currentTimeMillis());
        // Math.round absorbs the one-hour difference on daylight-saving change days.
        return (int) Math.round(diff / (double) TimeUnit.DAYS.toMillis(1));
    }

    public static boolean isExpiringSoon(PantryItem item) {
        return item.hasExpiry() && daysUntil(item.getExpiryDate()) <= SOON_DAYS;
    }

    /** The human friendly expiry line shown under an item, e.g. "Expires tomorrow". */
    public static String label(Context context, PantryItem item) {
        if (!item.hasExpiry()) return context.getString(R.string.expiry_none);

        int days = daysUntil(item.getExpiryDate());
        if (days < 0) {
            return context.getResources().getQuantityString(R.plurals.expired_days_ago, -days, -days);
        }
        if (days == 0) return context.getString(R.string.expiry_today);
        if (days == 1) return context.getString(R.string.expiry_tomorrow);
        if (days <= SOON_DAYS) {
            return context.getResources().getQuantityString(R.plurals.expires_in_days, days, days);
        }
        return context.getString(R.string.expiry_date, format(item.getExpiryDate()));
    }

    /**
     * Colour of the status dot: red when expired, amber when expiring soon, green otherwise.
     * With expiring-soon alerts switched off in Settings every dated item shows green.
     */
    @ColorRes
    public static int statusColor(PantryItem item, boolean alertsOn) {
        if (!item.hasExpiry()) return R.color.text_muted;
        if (!alertsOn) return R.color.status_ok;

        int days = daysUntil(item.getExpiryDate());
        if (days < 0) return R.color.status_danger;
        if (days <= SOON_DAYS) return R.color.status_warn;
        return R.color.status_ok;
    }
}
