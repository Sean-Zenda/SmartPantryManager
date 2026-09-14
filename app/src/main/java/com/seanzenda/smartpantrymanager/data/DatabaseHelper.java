package com.seanzenda.smartpantrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.seanzenda.smartpantrymanager.logic.IngredientMatcher;
import com.seanzenda.smartpantrymanager.logic.UnitConverter;
import com.seanzenda.smartpantrymanager.model.PantryItem;
import com.seanzenda.smartpantrymanager.model.Recipe;
import com.seanzenda.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The single on-device SQLite database for the app.
 *
 * <p>Three tables model the data:</p>
 * <ul>
 *     <li>{@code pantry_items} - what the user currently has at home (full CRUD)</li>
 *     <li>{@code recipes} - the seeded recipe collection</li>
 *     <li>{@code recipe_ingredients} - the ingredients each recipe requires (one-to-many)</li>
 * </ul>
 *
 * <p>A single shared instance is used ({@link #getInstance(Context)}) so that every screen reads and
 * writes through the same connection. That avoids "database is locked" errors when a fragment and
 * an activity touch the database at the same time.</p>
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "smart_pantry.db";
    public static final int DB_VERSION = 1;

    // ---- pantry_items -------------------------------------------------------------------------
    public static final String T_PANTRY = "pantry_items";
    public static final String P_ID = "_id";
    public static final String P_NAME = "name";
    public static final String P_QTY = "quantity";
    public static final String P_UNIT = "unit";
    public static final String P_CATEGORY = "category";
    public static final String P_EXPIRY = "expiry_date";
    public static final String P_CREATED = "created_at";

    // ---- recipes ------------------------------------------------------------------------------
    public static final String T_RECIPES = "recipes";
    public static final String R_ID = "_id";
    public static final String R_NAME = "name";
    public static final String R_SERVINGS = "servings";
    public static final String R_MINUTES = "minutes";
    public static final String R_EMOJI = "emoji";
    public static final String R_METHOD = "method";

    // ---- recipe_ingredients -------------------------------------------------------------------
    public static final String T_RECIPE_ING = "recipe_ingredients";
    public static final String RI_ID = "_id";
    public static final String RI_RECIPE_ID = "recipe_id";
    public static final String RI_NAME = "name";
    public static final String RI_QTY = "quantity";
    public static final String RI_UNIT = "unit";

    private static DatabaseHelper instance;

    /** Returns the one shared helper used by the whole app. */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            // Application context, so a destroyed Activity is never leaked by the helper.
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_PANTRY + " ("
                + P_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + P_NAME + " TEXT NOT NULL, "
                + P_QTY + " REAL NOT NULL, "
                + P_UNIT + " TEXT NOT NULL, "
                + P_CATEGORY + " TEXT NOT NULL, "
                + P_EXPIRY + " INTEGER NOT NULL DEFAULT 0, "
                + P_CREATED + " INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE " + T_RECIPES + " ("
                + R_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + R_NAME + " TEXT NOT NULL, "
                + R_SERVINGS + " INTEGER NOT NULL, "
                + R_MINUTES + " INTEGER NOT NULL, "
                + R_EMOJI + " TEXT NOT NULL, "
                + R_METHOD + " TEXT NOT NULL)");

        // ON DELETE CASCADE ties the ingredient rows to the life of their parent recipe.
        db.execSQL("CREATE TABLE " + T_RECIPE_ING + " ("
                + RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + RI_RECIPE_ID + " INTEGER NOT NULL, "
                + RI_NAME + " TEXT NOT NULL, "
                + RI_QTY + " REAL NOT NULL, "
                + RI_UNIT + " TEXT NOT NULL, "
                + "FOREIGN KEY (" + RI_RECIPE_ID + ") REFERENCES "
                + T_RECIPES + "(" + R_ID + ") ON DELETE CASCADE)");

        // The suggestions screen looks ingredients up by recipe on every refresh.
        db.execSQL("CREATE INDEX idx_recipe_ing ON " + T_RECIPE_ING + "(" + RI_RECIPE_ID + ")");

        // Pre-load the recipe collection so the app is useful on its very first launch.
        RecipeSeeder.seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The recipe collection is seed data and the pantry is small, so a clean rebuild is safe.
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPE_ING);
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + T_PANTRY);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // =============================================================================================
    // PANTRY - CREATE
    // =============================================================================================

    /** Inserts a new pantry item and returns its generated id (-1 on failure). */
    public long insertPantryItem(@NonNull PantryItem item) {
        ContentValues values = toValues(item);
        values.put(P_CREATED, System.currentTimeMillis());
        return getWritableDatabase().insert(T_PANTRY, null, values);
    }

    // =============================================================================================
    // PANTRY - READ
    // =============================================================================================

    /**
     * Reads the pantry, optionally narrowed by the search box and the category chips.
     *
     * @param search   text typed in the search field, or null/empty for everything
     * @param category one of Fresh/Dry/Dairy/Other, or null for "All"
     */
    public List<PantryItem> getPantryItems(@Nullable String search, @Nullable String category) {
        List<String> where = new ArrayList<>();
        List<String> args = new ArrayList<>();

        if (!TextUtils.isEmpty(search)) {
            where.add(P_NAME + " LIKE ?");
            args.add("%" + search.trim() + "%");
        }
        if (!TextUtils.isEmpty(category)) {
            where.add(P_CATEGORY + " = ?");
            args.add(category);
        }

        String selection = where.isEmpty() ? null : TextUtils.join(" AND ", where);
        String[] selectionArgs = args.isEmpty() ? null : args.toArray(new String[0]);

        List<PantryItem> items = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(T_PANTRY, null, selection, selectionArgs,
                null, null, P_CREATED + " DESC")) {
            while (c.moveToNext()) {
                items.add(readPantryItem(c));
            }
        }
        return items;
    }

    /** Convenience overload: the whole pantry, unfiltered. Used by the matching engine. */
    public List<PantryItem> getAllPantryItems() {
        return getPantryItems(null, null);
    }

    /** Reads a single item by id, or null when it no longer exists. */
    @Nullable
    public PantryItem getPantryItem(long id) {
        try (Cursor c = getReadableDatabase().query(T_PANTRY, null, P_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? readPantryItem(c) : null;
        }
    }

    /** Number of items currently in the pantry (used for the empty-state check). */
    public int getPantryCount() {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + T_PANTRY, null)) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    /**
     * Items that have an expiry date on or before {@code cutoff} (this includes anything already
     * expired), soonest first. Items with no expiry date are stored as 0 and excluded.
     */
    public List<PantryItem> getExpiringItems(long cutoff) {
        List<PantryItem> items = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(T_PANTRY, null,
                P_EXPIRY + " != 0 AND " + P_EXPIRY + " <= ?",
                new String[]{String.valueOf(cutoff)}, null, null, P_EXPIRY + " ASC")) {
            while (c.moveToNext()) {
                items.add(readPantryItem(c));
            }
        }
        return items;
    }

    /** Count for the badge on the Expiring tab - COUNT(*) so no rows are loaded just to be counted. */
    public int getExpiringCount(long cutoff) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + T_PANTRY + " WHERE " + P_EXPIRY + " != 0 AND "
                        + P_EXPIRY + " <= ?", new String[]{String.valueOf(cutoff)})) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    // =============================================================================================
    // PANTRY - UPDATE / DELETE
    // =============================================================================================

    /** Saves changes to an existing item. Returns the number of rows changed (1 on success). */
    public int updatePantryItem(@NonNull PantryItem item) {
        return getWritableDatabase().update(T_PANTRY, toValues(item),
                P_ID + " = ?", new String[]{String.valueOf(item.getId())});
    }

    /** Removes an item from the pantry. Returns the number of rows deleted (1 on success). */
    public int deletePantryItem(long id) {
        return getWritableDatabase().delete(T_PANTRY,
                P_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // =============================================================================================
    // RECIPES - READ
    // =============================================================================================

    /**
     * Loads every recipe together with its required ingredients in a single JOIN query.
     *
     * <p>The JOIN returns one row per (recipe, ingredient) pair, so a recipe with five ingredients
     * appears on five rows. A LinkedHashMap keyed by recipe id folds those rows back into one
     * {@link Recipe} each, while keeping the alphabetical order from ORDER BY. One query replaces the
     * 21 separate queries (1 for recipes + 1 per recipe) a naive loop would run.</p>
     */
    public List<Recipe> getAllRecipes() {
        String sql = "SELECT r." + R_ID + " AS rid, r." + R_NAME + " AS rname, r." + R_SERVINGS
                + ", r." + R_MINUTES + ", r." + R_EMOJI + ", r." + R_METHOD
                + ", i." + RI_ID + " AS iid, i." + RI_NAME + " AS iname, i." + RI_QTY
                + ", i." + RI_UNIT
                + " FROM " + T_RECIPES + " r"
                + " LEFT JOIN " + T_RECIPE_ING + " i ON i." + RI_RECIPE_ID + " = r." + R_ID
                + " ORDER BY r." + R_NAME + " COLLATE NOCASE ASC, i." + RI_ID + " ASC";

        Map<Long, Recipe> byId = new LinkedHashMap<>();
        try (Cursor c = getReadableDatabase().rawQuery(sql, null)) {
            while (c.moveToNext()) {
                long recipeId = c.getLong(c.getColumnIndexOrThrow("rid"));
                Recipe recipe = byId.get(recipeId);
                if (recipe == null) {
                    recipe = new Recipe(recipeId,
                            c.getString(c.getColumnIndexOrThrow("rname")),
                            c.getInt(c.getColumnIndexOrThrow(R_SERVINGS)),
                            c.getInt(c.getColumnIndexOrThrow(R_MINUTES)),
                            c.getString(c.getColumnIndexOrThrow(R_EMOJI)),
                            c.getString(c.getColumnIndexOrThrow(R_METHOD)));
                    byId.put(recipeId, recipe);
                }
                // LEFT JOIN: a recipe with no ingredient rows gives NULL ingredient columns.
                int ingredientIdCol = c.getColumnIndexOrThrow("iid");
                if (!c.isNull(ingredientIdCol)) {
                    recipe.addIngredient(new RecipeIngredient(
                            c.getLong(ingredientIdCol),
                            recipeId,
                            c.getString(c.getColumnIndexOrThrow("iname")),
                            c.getDouble(c.getColumnIndexOrThrow(RI_QTY)),
                            c.getString(c.getColumnIndexOrThrow(RI_UNIT))));
                }
            }
        }
        return new ArrayList<>(byId.values());
    }

    /**
     * "I cooked this": takes the recipe's ingredients out of the pantry inside one transaction.
     *
     * <p>Items with the soonest expiry date are used up first, so older food is not left to go off.
     * An item that is completely used up is deleted; otherwise its quantity is reduced, expressed in
     * the unit the user saved it in. Where the pantry measures an ingredient differently from the
     * recipe (grams versus pieces) the amount used cannot be known, so that item is left unchanged.</p>
     */
    public void cookRecipe(@NonNull Recipe recipe) {
        List<PantryItem> pantry = getAllPantryItems();
        pantry.sort((a, b) -> Long.compare(
                a.hasExpiry() ? a.getExpiryDate() : Long.MAX_VALUE,
                b.hasExpiry() ? b.getExpiryDate() : Long.MAX_VALUE));

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (RecipeIngredient needed : recipe.getIngredients()) {
                String key = IngredientMatcher.canonical(needed.getName());
                UnitConverter.Dimension dimension = UnitConverter.dimensionOf(needed.getUnit());
                double remaining = UnitConverter.toBase(needed.getQuantity(), needed.getUnit());

                for (PantryItem item : pantry) {
                    if (remaining <= 0) break;
                    if (item.getQuantity() <= 0) continue;                       // already used up
                    if (!key.equals(IngredientMatcher.canonical(item.getName()))) continue;
                    if (UnitConverter.dimensionOf(item.getUnit()) != dimension) continue;

                    double perUnit = UnitConverter.toBase(1, item.getUnit());
                    double held = item.getQuantity() * perUnit;
                    double used = Math.min(held, remaining);
                    remaining -= used;

                    double left = (held - used) / perUnit;
                    if (left < 0.001) {
                        item.setQuantity(0);
                        deletePantryItem(item.getId());
                    } else {
                        item.setQuantity(Math.round(left * 100.0) / 100.0);
                        updatePantryItem(item);
                    }
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** Loads one recipe (with ingredients) for the detail screen, or null if the id is unknown. */
    @Nullable
    public Recipe getRecipe(long id) {
        Recipe recipe = null;
        try (Cursor c = getReadableDatabase().query(T_RECIPES, null, R_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) recipe = readRecipe(c);
        }
        if (recipe != null) loadIngredientsInto(recipe);
        return recipe;
    }

    /** Number of recipes in the collection - used to decide whether seeding is still needed. */
    public int getRecipeCount() {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + T_RECIPES, null)) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    private void loadIngredientsInto(Recipe recipe) {
        try (Cursor c = getReadableDatabase().query(T_RECIPE_ING, null, RI_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipe.getId())}, null, null, RI_ID + " ASC")) {
            while (c.moveToNext()) {
                recipe.addIngredient(new RecipeIngredient(
                        c.getLong(c.getColumnIndexOrThrow(RI_ID)),
                        c.getLong(c.getColumnIndexOrThrow(RI_RECIPE_ID)),
                        c.getString(c.getColumnIndexOrThrow(RI_NAME)),
                        c.getDouble(c.getColumnIndexOrThrow(RI_QTY)),
                        c.getString(c.getColumnIndexOrThrow(RI_UNIT))));
            }
        }
    }

    // =============================================================================================
    // Row to object mapping
    // =============================================================================================

    private ContentValues toValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put(P_NAME, item.getName());
        values.put(P_QTY, item.getQuantity());
        values.put(P_UNIT, item.getUnit());
        values.put(P_CATEGORY, item.getCategory());
        values.put(P_EXPIRY, item.getExpiryDate());
        return values;
    }

    private PantryItem readPantryItem(Cursor c) {
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow(P_ID)),
                c.getString(c.getColumnIndexOrThrow(P_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(P_QTY)),
                c.getString(c.getColumnIndexOrThrow(P_UNIT)),
                c.getString(c.getColumnIndexOrThrow(P_CATEGORY)),
                c.getLong(c.getColumnIndexOrThrow(P_EXPIRY)),
                c.getLong(c.getColumnIndexOrThrow(P_CREATED)));
    }

    private Recipe readRecipe(Cursor c) {
        return new Recipe(
                c.getLong(c.getColumnIndexOrThrow(R_ID)),
                c.getString(c.getColumnIndexOrThrow(R_NAME)),
                c.getInt(c.getColumnIndexOrThrow(R_SERVINGS)),
                c.getInt(c.getColumnIndexOrThrow(R_MINUTES)),
                c.getString(c.getColumnIndexOrThrow(R_EMOJI)),
                c.getString(c.getColumnIndexOrThrow(R_METHOD)));
    }
}
