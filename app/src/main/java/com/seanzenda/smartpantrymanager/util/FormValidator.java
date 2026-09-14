package com.seanzenda.smartpantrymanager.util;

import androidx.annotation.StringRes;

import com.seanzenda.smartpantrymanager.R;
import com.seanzenda.smartpantrymanager.model.PantryItem;

/**
 * Validation rules for the Add / Edit Ingredient form.
 *
 * <p>Each method returns {@code 0} when the value is valid, or the string resource id of the error
 * message to show under the field. Keeping the rules here, away from the Activity, means they can be
 * unit tested without launching a screen.</p>
 */
public final class FormValidator {

    public static final int NAME_MIN = 3;
    public static final int NAME_MAX = 40;
    public static final double QUANTITY_MAX = 100_000;

    /** Starts with a letter; then letters, spaces, apostrophes, ampersands, full stops or hyphens. */
    private static final String NAME_PATTERN = "^\\p{L}[\\p{L} '&.\\-]*$";

    private FormValidator() { }

    @StringRes
    public static int checkName(String raw) {
        String name = raw == null ? "" : raw.trim();
        if (name.isEmpty()) return R.string.err_name_required;
        if (name.length() < NAME_MIN) return R.string.err_name_short;
        if (name.length() > NAME_MAX) return R.string.err_name_long;
        if (!name.matches(NAME_PATTERN)) return R.string.err_name_chars;
        return 0;
    }

    @StringRes
    public static int checkQuantity(String raw) {
        String text = raw == null ? "" : raw.trim().replace(',', '.');   // accept "1,5" as well as "1.5"
        if (text.isEmpty()) return R.string.err_qty_required;

        double value;
        try {
            value = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return R.string.err_qty_number;
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) return R.string.err_qty_number;
        if (value <= 0) return R.string.err_qty_positive;
        if (value > QUANTITY_MAX) return R.string.err_qty_too_big;
        return 0;
    }

    /**
     * A newly chosen expiry date may not be in the past. An item that has already expired can still
     * be edited without being forced to change its date, so the original value is always accepted.
     */
    @StringRes
    public static int checkExpiry(long expiry, long originalExpiry) {
        if (expiry == PantryItem.NO_EXPIRY || expiry == originalExpiry) return 0;
        return DateUtils.daysUntil(expiry) < 0 ? R.string.err_expiry_past : 0;
    }

    /** Parses a quantity that has already passed {@link #checkQuantity(String)}. */
    public static double parseQuantity(String raw) {
        return Double.parseDouble(raw.trim().replace(',', '.'));
    }

    /** Tidies a name for storage: trimmed, single spaced, first letter upper case. */
    public static String cleanName(String raw) {
        String name = raw.trim().replaceAll("\\s+", " ");
        return name.isEmpty() ? name : Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
