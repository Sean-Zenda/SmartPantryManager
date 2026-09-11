package com.seanzenda.smartpantrymanager.logic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Turns the messy name a user types into a canonical key that can be compared reliably.
 *
 * <p>The assignment warns that a naive exact-string match which breaks on "tomato" versus
 * "tomatoes" will be marked down, so a name goes through four steps before it is compared:</p>
 *
 * <ol>
 *     <li><b>Clean</b> - lower case, punctuation removed, repeated spaces collapsed.</li>
 *     <li><b>Strip descriptors</b> - words that describe preparation rather than the food itself
 *         ("fresh", "chopped", "large", "free range") are dropped.</li>
 *     <li><b>Singularise</b> - "tomatoes" becomes "tomato", "berries" becomes "berry".</li>
 *     <li><b>Alias</b> - a curated table maps real-world variants onto one canonical name, so
 *         "chicken fillet" and "chicken breasts" both become "chicken".</li>
 * </ol>
 *
 * <p>An alias table is used rather than a "does one name contain the other" rule on purpose. A
 * containment rule looks clever but would match "milk" against "coconut milk", which would suggest a
 * curry the user cannot actually cook - exactly the failure the strict rule exists to prevent.</p>
 */
public final class IngredientMatcher {

    /** Preparation words that say nothing about which food it is. */
    private static final Set<String> DESCRIPTORS = new HashSet<>(Arrays.asList(
            "fresh", "frozen", "dried", "raw", "cooked", "chopped", "diced", "sliced", "minced",
            "grated", "crushed", "whole", "large", "small", "medium", "extra", "organic", "free",
            "range", "ripe", "plain", "unsalted", "salted", "boneless", "skinless", "lean"));

    /** Real-world variants mapped onto the canonical name used by the seeded recipes. */
    private static final Map<String, String> ALIASES = new HashMap<>();

    static {
        alias("chicken", "chicken breast", "chicken breast fillet", "chicken fillet",
                "chicken thigh", "chicken piece");
        alias("beef mince", "mince", "beef", "ground beef", "minced beef");
        alias("cheddar", "cheddar cheese", "cheese");
        alias("parmesan", "parmesan cheese", "parmigiano");
        alias("feta", "feta cheese");
        alias("vegetable stock", "veg stock", "stock", "vegetable broth", "broth", "stock cube");
        alias("olive oil", "oil", "cooking oil", "sunflower oil", "vegetable oil");
        alias("soy sauce", "soya sauce");
        alias("coconut milk", "coconut cream");
        alias("cream", "fresh cream", "double cream", "heavy cream");
        alias("tomato", "tomatoe", "roma tomato", "cherry tomato", "tinned tomato", "canned tomato");
        alias("potato", "baby potato");
        alias("onion", "brown onion", "white onion", "red onion");
        alias("bean", "baked bean", "kidney bean", "black bean", "tinned bean", "canned bean");
        alias("bread", "bread slice", "loaf", "toast");
        alias("mayonnaise", "mayo");
        alias("curry powder", "curry spice", "masala");
        alias("egg", "free range egg");
        alias("sugar", "white sugar", "brown sugar", "castor sugar");
        alias("flour", "cake flour", "white flour", "all purpose flour");
        alias("milk", "full cream milk", "low fat milk", "fresh milk");
    }

    private IngredientMatcher() { }

    /**
     * Reduces an ingredient name to the key used for comparison.
     * Returns an empty string for null or blank input.
     */
    public static String canonical(String rawName) {
        if (rawName == null) return "";

        // 1. clean: lower case, letters and spaces only, single spaces
        String cleaned = rawName.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.isEmpty()) return "";

        // 2 + 3. drop descriptor words and singularise what is left
        StringBuilder builder = new StringBuilder();
        for (String word : cleaned.split(" ")) {
            if (DESCRIPTORS.contains(word)) continue;
            if (builder.length() > 0) builder.append(' ');
            builder.append(singular(word));
        }
        String normalised = builder.toString().trim();
        if (normalised.isEmpty()) normalised = cleaned;   // e.g. someone typed only "fresh"

        // 4. map known variants onto one canonical name
        String canonical = ALIASES.get(normalised);
        return canonical == null ? normalised : canonical;
    }

    /** True when two ingredient names refer to the same food. */
    public static boolean sameIngredient(String a, String b) {
        String canonicalA = canonical(a);
        return !canonicalA.isEmpty() && canonicalA.equals(canonical(b));
    }

    /**
     * Converts a plural word to its singular form.
     * Deliberately simple: it covers the plural endings that appear in food names without pulling
     * in a natural language library.
     */
    private static String singular(String word) {
        if (word.length() <= 3) return word;                       // "egg" stays "egg", not "eg"
        if (word.endsWith("ies")) return word.substring(0, word.length() - 3) + "y";  // berries
        if (word.endsWith("oes")) return word.substring(0, word.length() - 2);        // tomatoes
        if (word.endsWith("ches") || word.endsWith("shes") || word.endsWith("sses")) {
            return word.substring(0, word.length() - 2);                              // peaches
        }
        if (word.endsWith("ss")) return word;                                         // watercress
        if (word.endsWith("s")) return word.substring(0, word.length() - 1);          // carrots
        return word;
    }

    private static void alias(String canonical, String... variants) {
        ALIASES.put(canonical, canonical);
        for (String variant : variants) {
            ALIASES.put(variant, canonical);
        }
    }
}
