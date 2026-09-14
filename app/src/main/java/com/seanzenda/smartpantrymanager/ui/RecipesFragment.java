package com.seanzenda.smartpantrymanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.seanzenda.smartpantrymanager.MainActivity;
import com.seanzenda.smartpantrymanager.R;
import com.seanzenda.smartpantrymanager.RecipeDetailActivity;
import com.seanzenda.smartpantrymanager.adapter.RecipeAdapter;
import com.seanzenda.smartpantrymanager.data.DatabaseHelper;
import com.seanzenda.smartpantrymanager.logic.IngredientMatcher;
import com.seanzenda.smartpantrymanager.logic.RecipeMatcher;
import com.seanzenda.smartpantrymanager.model.PantryItem;
import com.seanzenda.smartpantrymanager.model.RecipeIngredient;
import com.seanzenda.smartpantrymanager.model.RecipeMatch;
import com.seanzenda.smartpantrymanager.util.DateUtils;
import com.seanzenda.smartpantrymanager.util.Prefs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Suggested Recipes tab. Runs the strict-matching engine against the current pantry every time the
 * tab comes into view, so adding or removing one ingredient immediately changes what is listed.
 */
public class RecipesFragment extends Fragment implements RecipeAdapter.Listener {

    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private RecyclerView list;
    private View emptyState;
    private TextView banner;

    public RecipesFragment() {
        super(R.layout.fragment_recipes);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = DatabaseHelper.getInstance(requireContext());

        list = view.findViewById(R.id.recipe_list);
        emptyState = view.findViewById(R.id.empty_state);
        banner = view.findViewById(R.id.match_banner);

        adapter = new RecipeAdapter(this);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);

        view.findViewById(R.id.btn_go_to_pantry).setOnClickListener(v -> onGoToPantry());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        List<PantryItem> pantry = db.getAllPantryItems();
        List<RecipeMatch> matches = RecipeMatcher.matchAll(db.getAllRecipes(), pantry);

        Set<String> expiring = expiringIngredients(pantry);
        List<RecipeMatch> suggested = new ArrayList<>();
        List<RecipeMatch> almostThere = new ArrayList<>();

        for (RecipeMatch match : matches) {
            if (match.canCookNow()) {                    // the strict rule - nothing missing
                match.setExpiringCount(countExpiring(match, expiring));
                suggested.add(match);
            } else if (match.isAlmostThere()) {          // exactly one missing - separate section
                almostThere.add(match);
            }
            // Two or more missing: not shown anywhere.
        }

        // Recipes that use up food about to expire go first; ties stay alphabetical.
        suggested.sort((a, b) -> {
            int byExpiring = Integer.compare(b.getExpiringCount(), a.getExpiringCount());
            return byExpiring != 0 ? byExpiring
                    : a.getRecipe().getName().compareToIgnoreCase(b.getRecipe().getName());
        });

        banner.setVisibility(suggested.isEmpty() ? View.GONE : View.VISIBLE);
        banner.setText(getResources().getQuantityString(R.plurals.recipes_can_make,
                suggested.size(), suggested.size()));

        // Nothing at all to show: the full-screen "no recipes match" state, never a blank screen.
        boolean nothing = suggested.isEmpty() && almostThere.isEmpty();
        emptyState.setVisibility(nothing ? View.VISIBLE : View.GONE);
        list.setVisibility(nothing ? View.GONE : View.VISIBLE);
        adapter.setData(suggested, almostThere);
    }

    /** Canonical names of pantry items expiring within the next few days (if alerts are on). */
    private Set<String> expiringIngredients(List<PantryItem> pantry) {
        Set<String> keys = new HashSet<>();
        if (!Prefs.isExpiryAlertsOn(requireContext())) return keys;
        for (PantryItem item : pantry) {
            if (DateUtils.isExpiringSoon(item) && DateUtils.daysUntil(item.getExpiryDate()) >= 0) {
                keys.add(IngredientMatcher.canonical(item.getName()));
            }
        }
        return keys;
    }

    private int countExpiring(RecipeMatch match, Set<String> expiring) {
        int count = 0;
        for (RecipeIngredient ingredient : match.getRecipe().getIngredients()) {
            if (expiring.contains(IngredientMatcher.canonical(ingredient.getName()))) count++;
        }
        return count;
    }

    /** Explicit Intent carrying the recipe id to the detail screen. */
    @Override
    public void onRecipeClick(RecipeMatch match) {
        Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, match.getRecipe().getId());
        startActivity(intent);
    }

    @Override
    public void onGoToPantry() {
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).selectTab(R.id.nav_pantry);
        }
    }
}
