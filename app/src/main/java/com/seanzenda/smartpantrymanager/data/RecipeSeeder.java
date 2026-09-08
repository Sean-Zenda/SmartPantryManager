package com.seanzenda.smartpantrymanager.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/**
 * Fills the {@code recipes} and {@code recipe_ingredients} tables with the starter collection the
 * first time the database is created.
 *
 * <p>The assignment asks for 15-20 recipes stored in the database rather than hard-coded in the
 * screens, so the whole collection is inserted once inside a single transaction and read back out
 * of SQLite from then on. Quantities deliberately overlap between recipes (onion, garlic, pasta,
 * eggs and rice appear repeatedly) so that adding or removing one pantry item visibly changes what
 * the strict matcher suggests.</p>
 */
final class RecipeSeeder {

    private RecipeSeeder() { }

    /** Inserts all 20 recipes. Called from {@link DatabaseHelper#onCreate(SQLiteDatabase)}. */
    static void seed(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            insert(db, "Tomato Chicken Pasta", 4, 30, "🍝",
                    "Cook the pasta according to the packet instructions.\n"
                            + "Heat the olive oil in a pan and cook the chicken until golden.\n"
                            + "Add the onion and garlic and cook for 2 minutes.\n"
                            + "Add the chopped tomatoes and simmer for 10 minutes.\n"
                            + "Combine with the pasta and serve.",
                    ing("Tomatoes", 3, "pcs"),
                    ing("Chicken breast", 500, "g"),
                    ing("Pasta", 250, "g"),
                    ing("Garlic", 2, "cloves"),
                    ing("Onion", 1, "pcs"),
                    ing("Olive oil", 2, "tbsp"));

            insert(db, "Spinach and Feta Omelette", 1, 10, "🍳",
                    "Whisk the eggs with a pinch of salt.\n"
                            + "Melt the butter in a non-stick pan over medium heat.\n"
                            + "Pour in the eggs and cook until they start to set.\n"
                            + "Scatter over the spinach and crumbled feta.\n"
                            + "Fold the omelette in half and slide it onto a plate.",
                    ing("Eggs", 3, "pcs"),
                    ing("Spinach", 100, "g"),
                    ing("Feta", 60, "g"),
                    ing("Butter", 1, "tbsp"));

            insert(db, "Chicken Fried Rice", 3, 25, "🍚",
                    "Cook the rice and let it cool.\n"
                            + "Fry the diced chicken until cooked through, then set it aside.\n"
                            + "Fry the onion and carrots until softened.\n"
                            + "Push everything aside, scramble the eggs in the pan.\n"
                            + "Stir in the rice, chicken and soy sauce and toss for 3 minutes.",
                    ing("Rice", 300, "g"),
                    ing("Chicken breast", 250, "g"),
                    ing("Carrots", 2, "pcs"),
                    ing("Onion", 1, "pcs"),
                    ing("Eggs", 2, "pcs"),
                    ing("Soy sauce", 2, "tbsp"));

            insert(db, "Tomato Soup", 4, 35, "🍅",
                    "Fry the chopped onion and garlic in the olive oil until soft.\n"
                            + "Add the roughly chopped tomatoes and cook for 5 minutes.\n"
                            + "Pour in the stock and simmer for 20 minutes.\n"
                            + "Blend until smooth and season to taste.",
                    ing("Tomatoes", 6, "pcs"),
                    ing("Onion", 1, "pcs"),
                    ing("Garlic", 2, "cloves"),
                    ing("Vegetable stock", 500, "ml"),
                    ing("Olive oil", 1, "tbsp"));

            insert(db, "Vegetable Stir Fry", 2, 15, "🥦",
                    "Heat the olive oil in a wok over a high heat.\n"
                            + "Add the sliced onion and carrots and stir fry for 3 minutes.\n"
                            + "Add the broccoli florets and stir fry for another 4 minutes.\n"
                            + "Pour over the soy sauce and toss to coat.",
                    ing("Broccoli", 200, "g"),
                    ing("Carrots", 2, "pcs"),
                    ing("Onion", 1, "pcs"),
                    ing("Soy sauce", 2, "tbsp"),
                    ing("Olive oil", 1, "tbsp"));

            insert(db, "Creamy Garlic Pasta", 2, 20, "🍜",
                    "Boil the pasta until al dente and drain.\n"
                            + "Melt the butter and gently fry the crushed garlic for 1 minute.\n"
                            + "Pour in the cream and simmer until it thickens slightly.\n"
                            + "Stir through the parmesan, then fold in the pasta.",
                    ing("Pasta", 250, "g"),
                    ing("Cream", 200, "ml"),
                    ing("Garlic", 3, "cloves"),
                    ing("Butter", 1, "tbsp"),
                    ing("Parmesan", 50, "g"));

            insert(db, "Scrambled Eggs on Toast", 1, 10, "🍞",
                    "Whisk the eggs with the milk.\n"
                            + "Melt the butter in a pan over a low heat.\n"
                            + "Add the eggs and stir slowly until just set.\n"
                            + "Toast the bread and spoon the eggs on top.",
                    ing("Eggs", 3, "pcs"),
                    ing("Milk", 30, "ml"),
                    ing("Butter", 1, "tbsp"),
                    ing("Bread", 2, "pcs"));

            insert(db, "Banana Pancakes", 4, 20, "🥞",
                    "Mash the bananas in a large bowl.\n"
                            + "Whisk in the eggs, milk and sugar.\n"
                            + "Fold in the flour until you have a smooth batter.\n"
                            + "Cook spoonfuls in a hot pan for 2 minutes a side.",
                    ing("Flour", 200, "g"),
                    ing("Milk", 250, "ml"),
                    ing("Eggs", 2, "pcs"),
                    ing("Bananas", 2, "pcs"),
                    ing("Sugar", 2, "tbsp"));

            insert(db, "Cheese Toastie", 1, 8, "🧀",
                    "Butter the outside of both slices of bread.\n"
                            + "Fill with the grated cheddar.\n"
                            + "Fry in a dry pan for 3 minutes a side until golden and melted.",
                    ing("Bread", 2, "pcs"),
                    ing("Cheddar", 80, "g"),
                    ing("Butter", 1, "tbsp"));

            insert(db, "Chicken Curry", 4, 40, "🍛",
                    "Fry the onion and garlic until soft and golden.\n"
                            + "Stir in the curry powder and cook for 1 minute.\n"
                            + "Add the diced chicken and brown all over.\n"
                            + "Pour in the coconut milk and simmer for 20 minutes.\n"
                            + "Serve over the cooked rice.",
                    ing("Chicken breast", 500, "g"),
                    ing("Onion", 1, "pcs"),
                    ing("Garlic", 2, "cloves"),
                    ing("Curry powder", 2, "tbsp"),
                    ing("Coconut milk", 400, "ml"),
                    ing("Rice", 300, "g"));

            insert(db, "Greek Salad", 2, 10, "🥗",
                    "Chop the tomatoes and cucumber into chunks.\n"
                            + "Add the olives and crumbled feta.\n"
                            + "Dress with the olive oil, season and toss gently.",
                    ing("Tomatoes", 3, "pcs"),
                    ing("Cucumber", 1, "pcs"),
                    ing("Feta", 100, "g"),
                    ing("Olives", 50, "g"),
                    ing("Olive oil", 2, "tbsp"));

            insert(db, "Mashed Potatoes", 4, 25, "🥔",
                    "Peel and quarter the potatoes, then boil until tender.\n"
                            + "Drain well and return them to the hot pot.\n"
                            + "Mash with the butter and warm milk until smooth.",
                    ing("Potatoes", 800, "g"),
                    ing("Butter", 50, "g"),
                    ing("Milk", 100, "ml"));

            insert(db, "Beef Bolognese", 4, 45, "🍲",
                    "Brown the beef mince in a large pan and drain the fat.\n"
                            + "Add the chopped onion and garlic and cook until soft.\n"
                            + "Stir in the chopped tomatoes and simmer for 25 minutes.\n"
                            + "Cook the pasta and serve the sauce on top.",
                    ing("Beef mince", 500, "g"),
                    ing("Tomatoes", 4, "pcs"),
                    ing("Onion", 1, "pcs"),
                    ing("Garlic", 2, "cloves"),
                    ing("Pasta", 250, "g"));

            insert(db, "Shakshuka", 2, 25, "🥘",
                    "Fry the sliced onion and garlic in the olive oil until soft.\n"
                            + "Add the chopped tomatoes and simmer for 10 minutes.\n"
                            + "Make four wells in the sauce and crack an egg into each.\n"
                            + "Cover and cook until the whites are set.",
                    ing("Eggs", 4, "pcs"),
                    ing("Tomatoes", 5, "pcs"),
                    ing("Onion", 1, "pcs"),
                    ing("Garlic", 2, "cloves"),
                    ing("Olive oil", 2, "tbsp"));

            insert(db, "Tuna Pasta Salad", 2, 15, "🐟",
                    "Boil the pasta, drain and rinse under cold water.\n"
                            + "Flake in the drained tuna and add the finely diced onion.\n"
                            + "Stir through the mayonnaise and season well.",
                    ing("Pasta", 200, "g"),
                    ing("Tuna", 185, "g"),
                    ing("Mayonnaise", 3, "tbsp"),
                    ing("Onion", 1, "pcs"));

            insert(db, "Chicken Soup", 4, 40, "🍜",
                    "Simmer the chicken in the stock for 20 minutes.\n"
                            + "Lift out the chicken, shred it and return it to the pot.\n"
                            + "Add the sliced carrots and onion and simmer for 15 minutes.",
                    ing("Chicken breast", 300, "g"),
                    ing("Carrots", 2, "pcs"),
                    ing("Onion", 1, "pcs"),
                    ing("Vegetable stock", 750, "ml"));

            insert(db, "Rice and Beans", 3, 30, "🍚",
                    "Fry the onion and garlic in the olive oil until soft.\n"
                            + "Add the drained beans and warm through.\n"
                            + "Stir the mixture into the cooked rice and season.",
                    ing("Rice", 300, "g"),
                    ing("Beans", 400, "g"),
                    ing("Onion", 1, "pcs"),
                    ing("Garlic", 2, "cloves"),
                    ing("Olive oil", 1, "tbsp"));

            insert(db, "French Toast", 2, 15, "🍯",
                    "Beat the eggs with the milk and sugar in a shallow dish.\n"
                            + "Soak each slice of bread for 20 seconds a side.\n"
                            + "Fry in the melted butter until golden on both sides.",
                    ing("Bread", 4, "pcs"),
                    ing("Eggs", 3, "pcs"),
                    ing("Milk", 100, "ml"),
                    ing("Sugar", 1, "tbsp"),
                    ing("Butter", 1, "tbsp"));

            insert(db, "Garlic Potato Wedges", 3, 40, "🍟",
                    "Heat the oven to 200 degrees Celsius.\n"
                            + "Cut the potatoes into wedges and toss with the oil and crushed garlic.\n"
                            + "Spread on a tray and bake for 35 minutes, turning once.",
                    ing("Potatoes", 600, "g"),
                    ing("Olive oil", 3, "tbsp"),
                    ing("Garlic", 2, "cloves"));

            insert(db, "Cheesy Broccoli Bake", 4, 35, "🧀",
                    "Boil the broccoli for 4 minutes, then drain well.\n"
                            + "Warm the cream with the butter and half the cheddar.\n"
                            + "Pour over the broccoli in a dish and top with the rest of the cheese.\n"
                            + "Bake for 20 minutes until bubbling.",
                    ing("Broccoli", 400, "g"),
                    ing("Cheddar", 150, "g"),
                    ing("Cream", 200, "ml"),
                    ing("Butter", 1, "tbsp"));

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** Inserts one recipe row plus a row per required ingredient. */
    private static void insert(SQLiteDatabase db, String name, int servings, int minutes,
                               String emoji, String method, String[]... ingredients) {
        ContentValues recipe = new ContentValues();
        recipe.put(DatabaseHelper.R_NAME, name);
        recipe.put(DatabaseHelper.R_SERVINGS, servings);
        recipe.put(DatabaseHelper.R_MINUTES, minutes);
        recipe.put(DatabaseHelper.R_EMOJI, emoji);
        recipe.put(DatabaseHelper.R_METHOD, method);

        long recipeId = db.insert(DatabaseHelper.T_RECIPES, null, recipe);

        for (String[] ingredient : ingredients) {
            ContentValues row = new ContentValues();
            row.put(DatabaseHelper.RI_RECIPE_ID, recipeId);
            row.put(DatabaseHelper.RI_NAME, ingredient[0]);
            row.put(DatabaseHelper.RI_QTY, Double.parseDouble(ingredient[1]));
            row.put(DatabaseHelper.RI_UNIT, ingredient[2]);
            db.insert(DatabaseHelper.T_RECIPE_ING, null, row);
        }
    }

    /** Small helper so each recipe above reads like its real ingredient list. */
    private static String[] ing(String name, double quantity, String unit) {
        return new String[]{name, String.valueOf(quantity), unit};
    }
}
