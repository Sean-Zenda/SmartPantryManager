package com.seanzenda.smartpantrymanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.seanzenda.smartpantrymanager.data.DatabaseHelper;
import com.seanzenda.smartpantrymanager.logic.RecipeMatcher;
import com.seanzenda.smartpantrymanager.logic.UnitConverter;
import com.seanzenda.smartpantrymanager.model.Recipe;
import com.seanzenda.smartpantrymanager.model.RecipeIngredient;
import com.seanzenda.smartpantrymanager.model.RecipeMatch;
import com.seanzenda.smartpantrymanager.util.Insets;

import java.util.Collections;
import java.util.List;

/**
 * Recipe Detail screen: the full ingredient list and numbered method for one recipe.
 *
 * <p>Opened with an explicit Intent carrying {@link #EXTRA_RECIPE_ID}. Each ingredient is ticked
 * green when the pantry covers it, or crossed when it is missing, using the same strict matcher as
 * the suggestions list.</p>
 */
public class RecipeDetailActivity extends AppCompatActivity {

    /** Intent extra: id of the recipe to show. */
    public static final String EXTRA_RECIPE_ID = "com.seanzenda.smartpantrymanager.EXTRA_RECIPE_ID";

    private DatabaseHelper db;
    private Recipe recipe;

    private LinearLayout ingredientsContainer;
    private TextView statusBanner;
    private MaterialButton cookedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        Insets.applySystemBars(findViewById(R.id.detail_root), true);

        db = DatabaseHelper.getInstance(this);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        recipe = db.getRecipe(recipeId);
        if (recipe == null) {
            Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ingredientsContainer = findViewById(R.id.ingredients_container);
        statusBanner = findViewById(R.id.status_banner);
        cookedButton = findViewById(R.id.btn_cooked);
        cookedButton.setOnClickListener(v -> confirmCooked());

        bindHeader();
        bindMethod();
    }

    /**
     * The pantry may have changed while this screen was in the background, so the ticks and the
     * button are recalculated every time the screen becomes visible again.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (recipe != null) bindIngredients();
    }

    private void bindHeader() {
        ((TextView) findViewById(R.id.hero_emoji)).setText(recipe.getEmoji());
        ((TextView) findViewById(R.id.recipe_name)).setText(recipe.getName());
        ((TextView) findViewById(R.id.recipe_servings)).setText(getResources().getQuantityString(
                R.plurals.servings, recipe.getServings(), recipe.getServings()));
        ((TextView) findViewById(R.id.recipe_minutes)).setText(
                getString(R.string.recipe_minutes, recipe.getMinutes()));
    }

    private void bindIngredients() {
        RecipeMatch match = RecipeMatcher
                .matchAll(Collections.singletonList(recipe), db.getAllPantryItems())
                .get(0);
        List<RecipeIngredient> missing = match.getMissing();

        LayoutInflater inflater = LayoutInflater.from(this);
        ingredientsContainer.removeAllViews();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            View row = inflater.inflate(R.layout.item_detail_ingredient, ingredientsContainer, false);
            boolean have = !missing.contains(ingredient);

            ImageView icon = row.findViewById(R.id.ingredient_status);
            icon.setImageResource(have ? R.drawable.ic_check_circle : R.drawable.ic_cancel_circle);
            icon.setColorFilter(ContextCompat.getColor(this,
                    have ? R.color.status_ok : R.color.status_danger));
            icon.setContentDescription(getString(have ? R.string.in_pantry : R.string.not_in_pantry));

            ((TextView) row.findViewById(R.id.ingredient_name)).setText(ingredient.getName());
            ((TextView) row.findViewById(R.id.ingredient_qty)).setText(getString(
                    R.string.quantity_unit, UnitConverter.format(ingredient.getQuantity()),
                    ingredient.getUnit()));
            ingredientsContainer.addView(row);
        }

        if (match.canCookNow()) {
            statusBanner.setText(R.string.detail_ready);
            statusBanner.setBackgroundResource(R.drawable.bg_banner_pale);
            statusBanner.setTextColor(ContextCompat.getColor(this, R.color.green_primary));
            cookedButton.setEnabled(true);
            cookedButton.setText(R.string.btn_cooked);
        } else {
            statusBanner.setText(getResources().getQuantityString(R.plurals.detail_missing,
                    missing.size(), missing.size()));
            statusBanner.setBackgroundResource(R.drawable.bg_banner_warn);
            statusBanner.setTextColor(ContextCompat.getColor(this, R.color.warn_text));
            cookedButton.setEnabled(false);
            cookedButton.setText(getResources().getQuantityString(R.plurals.btn_missing,
                    missing.size(), missing.size()));
        }
    }

    private void bindMethod() {
        LinearLayout container = findViewById(R.id.method_container);
        LayoutInflater inflater = LayoutInflater.from(this);
        List<String> steps = recipe.getSteps();
        for (int i = 0; i < steps.size(); i++) {
            View row = inflater.inflate(R.layout.item_method_step, container, false);
            ((TextView) row.findViewById(R.id.step_number)).setText(String.valueOf(i + 1));
            ((TextView) row.findViewById(R.id.step_text)).setText(steps.get(i));
            container.addView(row);
        }
    }

    private void confirmCooked() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.cook_confirm_title, recipe.getName()))
                .setMessage(R.string.cook_confirm_body)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.cook_confirm_yes, (dialog, which) -> {
                    db.cookRecipe(recipe);
                    Toast.makeText(this, R.string.cook_done, Toast.LENGTH_LONG).show();
                    finish();
                })
                .show();
    }
}
