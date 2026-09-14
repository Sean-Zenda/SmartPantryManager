package com.seanzenda.smartpantrymanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.seanzenda.smartpantrymanager.R;
import com.seanzenda.smartpantrymanager.model.Recipe;
import com.seanzenda.smartpantrymanager.model.RecipeMatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the Suggested Recipes screen.
 *
 * <p>The list mixes three kinds of row, each with its own layout, chosen by
 * {@link #getItemViewType(int)}:</p>
 * <ul>
 *     <li>{@link #TYPE_RECIPE} - a recipe card</li>
 *     <li>{@link #TYPE_HEADER} - the "Almost There" section title that separates the strict
 *         suggestions from recipes missing one ingredient</li>
 *     <li>{@link #TYPE_NOTICE} - the "no recipes match yet" card, used when nothing passes the
 *         strict rule but some recipes are one ingredient away</li>
 * </ul>
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Listener {
        void onRecipeClick(RecipeMatch match);

        void onGoToPantry();
    }

    private static final int TYPE_RECIPE = 0;
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_NOTICE = 2;

    /** One row of the list: its type, plus the match it shows (recipe rows only). */
    private static final class Row {
        final int type;
        final RecipeMatch match;

        Row(int type, RecipeMatch match) {
            this.type = type;
            this.match = match;
        }
    }

    private final List<Row> rows = new ArrayList<>();
    private final Listener listener;

    public RecipeAdapter(Listener listener) {
        this.listener = listener;
    }

    /**
     * Rebuilds the rows. The strict suggestions always come first; "Almost There" recipes only ever
     * appear below their own header, so the two can never be confused.
     */
    public void setData(List<RecipeMatch> suggested, List<RecipeMatch> almostThere) {
        rows.clear();
        if (suggested.isEmpty()) {
            rows.add(new Row(TYPE_NOTICE, null));
        }
        for (RecipeMatch match : suggested) {
            rows.add(new Row(TYPE_RECIPE, match));
        }
        if (!almostThere.isEmpty()) {
            rows.add(new Row(TYPE_HEADER, null));
            for (RecipeMatch match : almostThere) {
                rows.add(new Row(TYPE_RECIPE, match));
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).type;
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new SimpleHolder(inflater.inflate(R.layout.item_section_header, parent, false));
        }
        if (viewType == TYPE_NOTICE) {
            View view = inflater.inflate(R.layout.item_recipe_notice, parent, false);
            view.findViewById(R.id.btn_go_to_pantry).setOnClickListener(v -> listener.onGoToPantry());
            return new SimpleHolder(view);
        }
        return new RecipeHolder(inflater.inflate(R.layout.item_recipe, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (row.type == TYPE_RECIPE) {
            ((RecipeHolder) holder).bind(row.match, listener);
        }
        // Header and notice rows have fixed text, so there is nothing to bind.
    }

    /** Header and notice rows. */
    static class SimpleHolder extends RecyclerView.ViewHolder {
        SimpleHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    /** A recipe card. */
    static class RecipeHolder extends RecyclerView.ViewHolder {
        final TextView emoji;
        final TextView name;
        final TextView ingredients;
        final TextView meta;

        RecipeHolder(@NonNull View itemView) {
            super(itemView);
            emoji = itemView.findViewById(R.id.recipe_emoji);
            name = itemView.findViewById(R.id.recipe_name);
            ingredients = itemView.findViewById(R.id.recipe_ingredients);
            meta = itemView.findViewById(R.id.recipe_meta);
        }

        void bind(RecipeMatch match, Listener listener) {
            Context context = itemView.getContext();
            Recipe recipe = match.getRecipe();

            emoji.setText(recipe.getEmoji());
            name.setText(recipe.getName());
            ingredients.setText(recipe.getIngredientSummary());

            if (!match.canCookNow()) {
                // Almost There: say exactly what is missing, in amber.
                meta.setText(context.getString(R.string.missing_one, match.getFirstMissingName()));
                meta.setTextColor(ContextCompat.getColor(context, R.color.warn_text));
            } else if (match.getExpiringCount() > 0) {
                meta.setText(context.getResources().getQuantityString(R.plurals.uses_expiring,
                        match.getExpiringCount(), match.getExpiringCount()));
                meta.setTextColor(ContextCompat.getColor(context, R.color.warn_text));
            } else {
                String minutes = context.getString(R.string.recipe_minutes, recipe.getMinutes());
                String servings = context.getResources().getQuantityString(R.plurals.servings,
                        recipe.getServings(), recipe.getServings());
                meta.setText(context.getString(R.string.recipe_meta, minutes, servings));
                meta.setTextColor(ContextCompat.getColor(context, R.color.green_mid));
            }

            itemView.setOnClickListener(v -> listener.onRecipeClick(match));
        }
    }
}
