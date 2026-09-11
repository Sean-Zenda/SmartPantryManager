package com.seanzenda.smartpantrymanager.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.seanzenda.smartpantrymanager.model.PantryItem;
import com.seanzenda.smartpantrymanager.model.Recipe;
import com.seanzenda.smartpantrymanager.model.RecipeIngredient;
import com.seanzenda.smartpantrymanager.model.RecipeMatch;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for the strict-matching rule, which is the most important piece of logic in the app.
 * These run on the JVM because the matcher deliberately has no Android dependencies.
 */
public class RecipeMatcherTest {

    private Recipe omelette() {
        Recipe recipe = new Recipe(1, "Omelette", 1, 10, "E", "Cook it.");
        recipe.addIngredient(new RecipeIngredient("Eggs", 3, "pcs"));
        recipe.addIngredient(new RecipeIngredient("Spinach", 100, "g"));
        recipe.addIngredient(new RecipeIngredient("Feta", 60, "g"));
        return recipe;
    }

    private PantryItem item(String name, double quantity, String unit) {
        PantryItem pantryItem = new PantryItem();
        pantryItem.setName(name);
        pantryItem.setQuantity(quantity);
        pantryItem.setUnit(unit);
        return pantryItem;
    }

    @Test
    public void everyIngredientPresent_recipeIsSuggested() {
        List<PantryItem> pantry = Arrays.asList(
                item("Eggs", 6, "pcs"),
                item("Spinach", 200, "g"),
                item("Feta", 100, "g"));

        List<RecipeMatch> suggested =
                RecipeMatcher.suggested(Collections.singletonList(omelette()), pantry);

        assertEquals(1, suggested.size());
        assertTrue(suggested.get(0).canCookNow());
    }

    @Test
    public void oneIngredientMissing_recipeIsExcluded() {
        List<PantryItem> pantry = Arrays.asList(
                item("Eggs", 6, "pcs"),
                item("Spinach", 200, "g"));   // no feta

        List<Recipe> recipes = Collections.singletonList(omelette());

        assertTrue(RecipeMatcher.suggested(recipes, pantry).isEmpty());
        assertEquals(1, RecipeMatcher.almostThere(recipes, pantry).size());
    }

    @Test
    public void notEnoughOfAnIngredient_recipeIsExcluded() {
        List<PantryItem> pantry = Arrays.asList(
                item("Eggs", 2, "pcs"),        // recipe needs 3
                item("Spinach", 200, "g"),
                item("Feta", 100, "g"));

        assertTrue(RecipeMatcher.suggested(Collections.singletonList(omelette()), pantry).isEmpty());
    }

    @Test
    public void pluralAndSingularNamesMatch() {
        assertTrue(IngredientMatcher.sameIngredient("Tomato", "Tomatoes"));
        assertTrue(IngredientMatcher.sameIngredient("carrots", "Carrot"));
        assertTrue(IngredientMatcher.sameIngredient("Fresh chopped onions", "onion"));
    }

    @Test
    public void differentFoodsDoNotMatch() {
        assertFalse(IngredientMatcher.sameIngredient("Milk", "Coconut milk"));
        assertFalse(IngredientMatcher.sameIngredient("Onion", "Garlic"));
    }

    @Test
    public void unitsAreConvertedBeforeComparing() {
        Recipe rice = new Recipe(2, "Rice", 2, 20, "R", "Boil it.");
        rice.addIngredient(new RecipeIngredient("Rice", 300, "g"));

        List<PantryItem> pantry = Collections.singletonList(item("Rice", 1, "kg"));

        assertEquals(1, RecipeMatcher.suggested(Collections.singletonList(rice), pantry).size());
    }

    @Test
    public void duplicatePantryEntriesAreAddedTogether() {
        Recipe eggs = new Recipe(3, "Eggs", 1, 5, "E", "Fry them.");
        eggs.addIngredient(new RecipeIngredient("Eggs", 8, "pcs"));

        List<PantryItem> pantry = Arrays.asList(item("Eggs", 6, "pcs"), item("Eggs", 4, "pcs"));

        assertEquals(1, RecipeMatcher.suggested(Collections.singletonList(eggs), pantry).size());
    }

    @Test
    public void emptyPantrySuggestsNothing() {
        List<RecipeMatch> suggested =
                RecipeMatcher.suggested(Collections.singletonList(omelette()), new ArrayList<>());

        assertTrue(suggested.isEmpty());
    }
}
