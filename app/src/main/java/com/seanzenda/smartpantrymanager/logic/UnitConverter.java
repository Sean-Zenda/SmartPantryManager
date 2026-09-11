package com.seanzenda.smartpantrymanager.logic;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Converts the units a user might type into a common base unit so that quantities can be compared.
 *
 * <p>A pantry holding "1 kg rice" must satisfy a recipe that asks for "300 g rice", so every unit is
 * reduced to one of three base units before any comparison happens:</p>
 *
 * <ul>
 *     <li>{@link Dimension#MASS} - base unit grams</li>
 *     <li>{@link Dimension#VOLUME} - base unit millilitres</li>
 *     <li>{@link Dimension#COUNT} - base unit pieces (a clove and a slice are both counted things)</li>
 * </ul>
 *
 * <p>Two quantities are only ever compared numerically when they share a dimension. Grams cannot be
 * converted into pieces without knowing the density of the food, so that case is handled by
 * {@link RecipeMatcher} rather than being guessed at here.</p>
 */
public final class UnitConverter {

    /** The kind of measurement a unit expresses. */
    public enum Dimension { MASS, VOLUME, COUNT, UNKNOWN }

    /** Units the Add / Edit form offers, in the order they appear in the dropdown. */
    public static final String[] SUPPORTED_UNITS = {
            "pcs", "g", "kg", "ml", "l", "tbsp", "tsp", "cup", "cloves", "slices"
    };

    private static final Map<String, Dimension> DIMENSIONS = new HashMap<>();
    private static final Map<String, Double> TO_BASE = new HashMap<>();

    static {
        // ---- mass, base unit = gram ----
        mass("g", 1);
        mass("gram", 1);
        mass("grams", 1);
        mass("kg", 1000);
        mass("kilogram", 1000);
        mass("kilograms", 1000);
        mass("mg", 0.001);
        mass("oz", 28.3495);
        mass("lb", 453.592);

        // ---- volume, base unit = millilitre ----
        volume("ml", 1);
        volume("millilitre", 1);
        volume("millilitres", 1);
        volume("l", 1000);
        volume("litre", 1000);
        volume("litres", 1000);
        volume("tbsp", 15);
        volume("tablespoon", 15);
        volume("tablespoons", 15);
        volume("tsp", 5);
        volume("teaspoon", 5);
        volume("teaspoons", 5);
        volume("cup", 250);
        volume("cups", 250);

        // ---- count, base unit = piece ----
        count("pcs", 1);
        count("pc", 1);
        count("piece", 1);
        count("pieces", 1);
        count("unit", 1);
        count("units", 1);
        count("clove", 1);
        count("cloves", 1);
        count("slice", 1);
        count("slices", 1);
        count("can", 1);
        count("cans", 1);
        count("tin", 1);
        count("tins", 1);
    }

    private UnitConverter() { }

    /** The dimension of a unit, or {@link Dimension#UNKNOWN} for anything unrecognised. */
    public static Dimension dimensionOf(String unit) {
        Dimension dimension = DIMENSIONS.get(clean(unit));
        return dimension == null ? Dimension.UNKNOWN : dimension;
    }

    /**
     * Converts a quantity into its base unit (grams, millilitres or pieces).
     * An unrecognised unit is returned unchanged, so nothing is silently scaled by a wrong factor.
     */
    public static double toBase(double quantity, String unit) {
        Double factor = TO_BASE.get(clean(unit));
        return factor == null ? quantity : quantity * factor;
    }

    /** Formats a quantity for display, dropping the ".0" from whole numbers ("3" not "3.0"). */
    public static String format(double quantity) {
        if (quantity == Math.floor(quantity) && !Double.isInfinite(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(Math.round(quantity * 100.0) / 100.0);
    }

    private static String clean(String unit) {
        return unit == null ? "" : unit.trim().toLowerCase(Locale.ROOT);
    }

    private static void mass(String unit, double toGrams) {
        DIMENSIONS.put(unit, Dimension.MASS);
        TO_BASE.put(unit, toGrams);
    }

    private static void volume(String unit, double toMillilitres) {
        DIMENSIONS.put(unit, Dimension.VOLUME);
        TO_BASE.put(unit, toMillilitres);
    }

    private static void count(String unit, double toPieces) {
        DIMENSIONS.put(unit, Dimension.COUNT);
        TO_BASE.put(unit, toPieces);
    }
}
