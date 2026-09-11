package com.seanzenda.smartpantrymanager.logic;

import com.seanzenda.smartpantrymanager.model.PantryItem;
import com.seanzenda.smartpantrymanager.model.Recipe;
import com.seanzenda.smartpantrymanager.model.RecipeIngredient;
import com.seanzenda.smartpantrymanager.model.RecipeMatch;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The core business logic of the app: deciding which recipes the user can cook right now.
 *
 * <p><b>The strict rule.</b> A recipe qualifies as suggested only when <i>every single</i>
 * ingredient it requires is in the pantry, in at least the required quantity. One missing
 * ingredient, or one ingredient present in too small a quantity, disqualifies the whole recipe.
 * There is no partial credit and no "close enough" - see {@link RecipeMatch#canCookNow()}.</p>
 *
 * <p><b>How a comparison is made.</b> The pantry is indexed once per refresh into a map of
 * canonical ingredient name to the total amount held, split by dimension:</p>
 *
 * <pre>
 *     "tomato"  -&gt; { COUNT: 5 pieces }
 *     "rice"    -&gt; { MASS: 1000 grams }
 * </pre>
 *
 * <p>Indexing first means the whole screen costs one pass over the pantry plus one pass over the
 * recipes, instead of re-scanning the pantry for every ingredient of every recipe.</p>
 *
 * <p>Totals are summed, so two separate entries of "6 eggs" and "4 eggs" satisfy a recipe that needs
 * 8 eggs. Quantities are only compared when both sides share a dimension; if a recipe asks for
 * "2 pcs chicken" while the pantry holds "500 g chicken" there is no honest way to convert pieces
 * into grams, so the ingredient counts as present and only its quantity check is skipped. That keeps
 * the app from wrongly hiding a recipe the user really can cook.</p>
 */
public final class RecipeMatcher {

    /** Floating point slack, so 249.99999 g is not treated as less than 250 g. */
    private static final double TOLERANCE = 0.0001;

    private RecipeMatcher() { }

    /**
     * Runs the rule over every recipe.
     *
     * @return one {@link RecipeMatch} per recipe, each listing what is missing (empty when the
     *         recipe can be cooked right now)
     */
    public static List<RecipeMatch> matchAll(List<Recipe> recipes, List<PantryItem> pantry) {
        Map<String, Map<UnitConverter.Dimension, Double>> index = indexPantry(pantry);

        List<RecipeMatch> results = new ArrayList<>();
        for (Recipe recipe : recipes) {
            RecipeMatch match = new RecipeMatch(recipe);
            for (RecipeIngredient required : recipe.getIngredients()) {
                if (!isSatisfied(required, index)) {
                    match.addMissing(required);
                }
            }
            results.add(match);
        }
        return results;
    }

    /** Only the recipes that pass the strict rule, ready for the Suggested Recipes list. */
    public static List<RecipeMatch> suggested(List<Recipe> recipes, List<PantryItem> pantry) {
        List<RecipeMatch> suggested = new ArrayList<>();
        for (RecipeMatch match : matchAll(recipes, pantry)) {
            if (match.canCookNow()) suggested.add(match);
        }
        return suggested;
    }

    /**
     * Recipes missing exactly one ingredient. These are deliberately kept out of
     * {@link #suggested(List, List)} and shown in their own clearly labelled section.
     */
    public static List<RecipeMatch> almostThere(List<Recipe> recipes, List<PantryItem> pantry) {
        List<RecipeMatch> almost = new ArrayList<>();
        for (RecipeMatch match : matchAll(recipes, pantry)) {
            if (match.isAlmostThere()) almost.add(match);
        }
        return almost;
    }

    // =============================================================================================
    // Internals
    // =============================================================================================

    /**
     * Builds a lookup of canonical ingredient name to the total amount held, per dimension.
     * Duplicate pantry entries of the same food are added together.
     */
    private static Map<String, Map<UnitConverter.Dimension, Double>> indexPantry(
            List<PantryItem> pantry) {

        Map<String, Map<UnitConverter.Dimension, Double>> index = new HashMap<>();

        for (PantryItem item : pantry) {
            String key = IngredientMatcher.canonical(item.getName());
            if (key.isEmpty()) continue;

            UnitConverter.Dimension dimension = UnitConverter.dimensionOf(item.getUnit());
            double amount = UnitConverter.toBase(item.getQuantity(), item.getUnit());

            Map<UnitConverter.Dimension, Double> totals = index.get(key);
            if (totals == null) {
                totals = new EnumMap<>(UnitConverter.Dimension.class);
                index.put(key, totals);
            }
            Double existing = totals.get(dimension);
            totals.put(dimension, existing == null ? amount : existing + amount);
        }
        return index;
    }

    /**
     * Decides whether one required ingredient is covered by the pantry.
     *
     * <p>Two conditions must both hold:</p>
     * <ol>
     *     <li>the food is in the pantry at all (after name normalisation), and</li>
     *     <li>where the units are comparable, the pantry holds at least the required amount.</li>
     * </ol>
     */
    private static boolean isSatisfied(RecipeIngredient required,
                                       Map<String, Map<UnitConverter.Dimension, Double>> index) {

        Map<UnitConverter.Dimension, Double> totals =
                index.get(IngredientMatcher.canonical(required.getName()));

        // Condition 1: not in the pantry at all -> the recipe is disqualified.
        if (totals == null || totals.isEmpty()) return false;

        UnitConverter.Dimension neededDimension = UnitConverter.dimensionOf(required.getUnit());
        Double held = totals.get(neededDimension);

        // The food is present but measured a different way (grams held, pieces required).
        // Pieces cannot honestly be converted into grams, so accept it on presence alone.
        if (held == null) return true;

        // Condition 2: enough of it.
        double needed = UnitConverter.toBase(required.getQuantity(), required.getUnit());
        return held + TOLERANCE >= needed;
    }
}
