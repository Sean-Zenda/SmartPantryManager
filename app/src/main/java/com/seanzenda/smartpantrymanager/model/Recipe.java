package com.seanzenda.smartpantrymanager.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A recipe from the seeded collection: a name, the ingredients it requires,
 * and the numbered preparation steps.
 */
public class Recipe {

    /** Separator used to store the numbered method steps in a single TEXT column. */
    public static final String STEP_SEPARATOR = "\n";

    private long id;
    private String name;
    private int servings;
    private int minutes;
    private String emoji;
    private String method;
    private final List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe(long id, String name, int servings, int minutes, String emoji, String method) {
        this.id = id;
        this.name = name;
        this.servings = servings;
        this.minutes = minutes;
        this.emoji = emoji;
        this.method = method;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public int getServings() { return servings; }
    public int getMinutes() { return minutes; }
    public String getEmoji() { return emoji; }
    public String getMethod() { return method; }

    public List<RecipeIngredient> getIngredients() { return ingredients; }

    public void addIngredient(RecipeIngredient ingredient) {
        ingredient.setRecipeId(id);
        ingredients.add(ingredient);
    }

    /** The method text split back into the individual steps shown on the detail screen. */
    public List<String> getSteps() {
        List<String> steps = new ArrayList<>();
        if (method == null) return steps;
        for (String step : method.split(STEP_SEPARATOR)) {
            if (!step.trim().isEmpty()) steps.add(step.trim());
        }
        return steps;
    }

    /** Comma separated ingredient names, shown as the subtitle in the suggestions list. */
    public String getIngredientSummary() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ingredients.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(ingredients.get(i).getName());
        }
        return sb.toString();
    }
}
