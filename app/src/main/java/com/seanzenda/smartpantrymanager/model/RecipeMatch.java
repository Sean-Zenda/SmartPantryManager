package com.seanzenda.smartpantrymanager.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The verdict of the strict-matching engine for one recipe: the recipe itself and exactly which of
 * its ingredients the pantry cannot cover.
 */
public class RecipeMatch {

    private final Recipe recipe;
    private final List<RecipeIngredient> missing = new ArrayList<>();

    public RecipeMatch(Recipe recipe) {
        this.recipe = recipe;
    }

    public Recipe getRecipe() { return recipe; }

    public List<RecipeIngredient> getMissing() { return missing; }

    public void addMissing(RecipeIngredient ingredient) { missing.add(ingredient); }

    /**
     * The strict rule: a recipe can be cooked right now only when nothing at all is missing.
     * This is the single condition that decides whether a recipe appears under Suggested Recipes.
     */
    public boolean canCookNow() { return missing.isEmpty(); }

    /** Missing exactly one ingredient - shown in the clearly separated "Almost There" section. */
    public boolean isAlmostThere() { return missing.size() == 1; }

    /** The name of the one missing ingredient, for the "Almost There" subtitle. */
    public String getFirstMissingName() {
        return missing.isEmpty() ? "" : missing.get(0).getName();
    }
}
