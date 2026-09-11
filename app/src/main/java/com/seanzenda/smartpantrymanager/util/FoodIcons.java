package com.seanzenda.smartpantrymanager.util;

import com.seanzenda.smartpantrymanager.logic.IngredientMatcher;

import java.util.HashMap;
import java.util.Map;

/**
 * Picks an emoji to use as the round "photo" of a pantry item.
 *
 * <p>Emoji are used instead of downloaded images so the app needs no network access, no image
 * library and no bundled photo assets, and every ingredient still gets a recognisable picture.</p>
 */
public final class FoodIcons {

    private static final Map<String, String> ICONS = new HashMap<>();

    static {
        ICONS.put("tomato", "🍅");
        ICONS.put("chicken", "🍗");
        ICONS.put("pasta", "🍝");
        ICONS.put("spaghetti", "🍝");
        ICONS.put("garlic", "🧄");
        ICONS.put("onion", "🧅");
        ICONS.put("olive oil", "🫒");
        ICONS.put("olive", "🫒");
        ICONS.put("egg", "🥚");
        ICONS.put("spinach", "🥬");
        ICONS.put("lettuce", "🥬");
        ICONS.put("cabbage", "🥬");
        ICONS.put("feta", "🧀");
        ICONS.put("cheddar", "🧀");
        ICONS.put("parmesan", "🧀");
        ICONS.put("rice", "🍚");
        ICONS.put("carrot", "🥕");
        ICONS.put("soy sauce", "🍶");
        ICONS.put("vegetable stock", "🍲");
        ICONS.put("broccoli", "🥦");
        ICONS.put("cream", "🥛");
        ICONS.put("milk", "🥛");
        ICONS.put("butter", "🧈");
        ICONS.put("bread", "🍞");
        ICONS.put("flour", "🌾");
        ICONS.put("banana", "🍌");
        ICONS.put("sugar", "🍬");
        ICONS.put("curry powder", "🌶️");
        ICONS.put("chilli", "🌶️");
        ICONS.put("pepper", "🫑");
        ICONS.put("coconut milk", "🥥");
        ICONS.put("cucumber", "🥒");
        ICONS.put("potato", "🥔");
        ICONS.put("beef mince", "🥩");
        ICONS.put("steak", "🥩");
        ICONS.put("tuna", "🐟");
        ICONS.put("fish", "🐟");
        ICONS.put("mayonnaise", "🥫");
        ICONS.put("bean", "🫘");
        ICONS.put("lemon", "🍋");
        ICONS.put("apple", "🍎");
        ICONS.put("mushroom", "🍄");
        ICONS.put("corn", "🌽");
        ICONS.put("avocado", "🥑");
    }

    private FoodIcons() { }

    /** The emoji for an ingredient, falling back to one that suits its category. */
    public static String forItem(String name, String category) {
        String canonical = IngredientMatcher.canonical(name);

        String icon = ICONS.get(canonical);
        if (icon != null) return icon;

        // "chicken wing" is not in the table, but its word "chicken" is.
        for (String word : canonical.split(" ")) {
            icon = ICONS.get(word);
            if (icon != null) return icon;
        }
        return forCategory(category);
    }

    private static String forCategory(String category) {
        if ("Fresh".equals(category)) return "🥗";
        if ("Dry".equals(category)) return "🌾";
        if ("Dairy".equals(category)) return "🥛";
        return "🥫";
    }
}
