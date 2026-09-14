# Smart Pantry Manager

An Android application written in **Java** that helps you **reduce food waste**. You record the
ingredients you already have at home, and the app suggests only the recipes you can cook **right
now**, with no shopping trip required.

> Module: Mobile App Development 700 — Practical Assignment

---

## The core idea: strict matching

A recipe appears under **Suggested Recipes** only when **every ingredient it requires** is in your
pantry, **in at least the required quantity**. If a recipe needs 5 ingredients and your pantry has 4
of them, that recipe is not suggested.

Recipes that are missing **exactly one** ingredient are listed in a separate, clearly labelled
**Almost There** section below the suggestions, together with the name of the missing ingredient.
They never appear in the suggestions list itself.

Matching copes with the ordinary mess of real input:

| Real-world difference | Example | Handled by |
|---|---|---|
| Singular / plural | `tomato` = `tomatoes` | `IngredientMatcher` singularises every word |
| Preparation words | `fresh chopped onions` = `onion` | descriptor words are stripped |
| Name variants | `chicken fillet` = `chicken breast` | curated alias table |
| Different units | `1 kg rice` covers `300 g rice` | `UnitConverter` reduces to g / ml / pieces |
| Split entries | `6 eggs` + `4 eggs` covers `8 eggs` | pantry totals are summed per ingredient |

An alias table is used instead of a "does one name contain the other" rule on purpose. A
containment rule would match `milk` against `coconut milk` and suggest a curry you cannot cook.

## Features

- **Pantry management** with full Create / Read / Update / Delete: name, quantity, unit, category
  and an optional expiry date
- **Pantry list** in a RecyclerView with live search, category filters (All / Fresh / Dry / Dairy /
  Other) and colour-coded expiry status
- **20 seeded recipes**, each with its required ingredients and a numbered method, loaded into
  SQLite on first launch
- **Suggested Recipes** driven by the strict-matching engine, with the separate *Almost There*
  section and a friendly message when nothing matches
- **Recipe Detail** showing a green tick or red cross next to every ingredient, plus an
  **I Cooked This** action that deducts the used ingredients from the pantry, using the items that
  expire soonest first
- **Expiring Soon** tab listing food to use within the next 3 days, with a count badge on the tab
- **Settings**: expiring-soon alerts toggle and Metric / Imperial units preference
- **Input validation** on the Add / Edit form: inline error messages, and Save stays disabled until
  the form is valid
- **Unit tests** for the matching rules and the form validation

## Database choice: SQLite (`SQLiteOpenHelper`)

SQLite was chosen for three reasons:

1. **Offline first.** A pantry app is used standing in the kitchen. Reading a list of food you
   already own should not depend on a network connection, and SQLite needs none.
2. **The data is relational.** A recipe *has many* required ingredients, which is a classic
   one-to-many relationship. The `recipes` and `recipe_ingredients` tables model it directly, and a
   single `LEFT JOIN` loads the entire recipe collection in one query.
3. **No accounts, cost or setup.** Firebase would need a Google account, a `google-services.json`
   file and internet access. PostgreSQL would need a hosted server and a REST backend. For
   single-user data stored on one device, both add failure modes without adding value.

Data is written to a file on the device, so everything **persists after the app is closed and
reopened**. The two settings are simple on/off flags, so they are stored in `SharedPreferences`
instead.

### Data model

```
recipes                         recipe_ingredients                pantry_items
─────────────────────           ──────────────────────            ─────────────────────
_id        INTEGER PK  ──1───┐  _id        INTEGER PK             _id         INTEGER PK
name       TEXT              └──N recipe_id INTEGER FK             name        TEXT
servings   INTEGER              name       TEXT                   quantity    REAL
minutes    INTEGER              quantity   REAL                   unit        TEXT
emoji      TEXT                 unit       TEXT                   category    TEXT
method     TEXT                                                   expiry_date INTEGER (0 = none)
                                ON DELETE CASCADE                 created_at  INTEGER
```

`pantry_items` is intentionally **not** linked to recipes by a foreign key. Pantry names are free
text typed by the user, so they are connected to recipe ingredients at runtime by the matching
engine rather than by an exact database key.

## Screens and navigation

| Screen | Class | How you get there |
|---|---|---|
| Welcome | `WelcomeActivity` | Launcher (shown on first run only) → `MainActivity` via `Intent` |
| Pantry List | `PantryFragment` | Bottom navigation |
| Add / Edit Ingredient | `AddEditIngredientActivity` | `Intent`; edit mode adds `putExtra(EXTRA_ITEM_ID, id)` |
| Suggested Recipes | `RecipesFragment` | Bottom navigation |
| Recipe Detail | `RecipeDetailActivity` | `Intent` with `putExtra(EXTRA_RECIPE_ID, id)` |
| Expiring Soon | `ExpiringFragment` | Bottom navigation |
| Settings | `SettingsFragment` | Bottom navigation |

## Project structure

```
app/src/main/java/com/seanzenda/smartpantrymanager/
├── WelcomeActivity.java            first-run screen
├── MainActivity.java               hosts the four tabs + bottom navigation
├── AddEditIngredientActivity.java  create / update / delete a pantry item
├── RecipeDetailActivity.java       ingredients, method, "I Cooked This"
├── ui/          PantryFragment, RecipesFragment, ExpiringFragment, SettingsFragment
├── adapter/     PantryAdapter, RecipeAdapter          (custom RecyclerView adapters)
├── data/        DatabaseHelper, RecipeSeeder          (SQLite)
├── logic/       RecipeMatcher, IngredientMatcher, UnitConverter   (strict matching)
├── model/       PantryItem, Recipe, RecipeIngredient, RecipeMatch
└── util/        FormValidator, DateUtils, FoodIcons, Prefs, Insets

app/src/test/java/…                 RecipeMatcherTest, FormValidatorTest
```

## Setup and run

1. Clone the repository:
   ```
   git clone https://github.com/Sean-Zenda/SmartPantryManager.git
   ```
2. Open the project folder in **Android Studio**.
3. Let Gradle sync. The project needs **JDK 21** and **Android SDK Platform 37**
   (`compileSdk 37`, `minSdk 24`).
4. Choose an emulator or a physical device running Android 7.0 (API 24) or newer.
5. Press **Run ▶**. The recipe collection is seeded automatically on first launch.

No API keys, accounts or backend services are required.

### Running the unit tests

```
./gradlew test
```

On Windows use `gradlew.bat test`, or right-click `app/src/test` in Android Studio and choose
**Run Tests**.

### Quick check of strict matching

1. Add **Tomatoes 3 pcs**, **Chicken breast 500 g**, **Pasta 250 g**, **Onion 1 pcs** and
   **Olive oil 250 ml**.
2. Open **Recipes**. *Tomato Chicken Pasta* is listed under **Almost There — Missing: Garlic**.
3. Add **Garlic 5 cloves** and return to **Recipes**. It now appears as a suggestion.
4. Delete the garlic. The recipe moves back to *Almost There*.

## Out of scope (by assignment instruction)

No Google Maps, no mapping SDK, no GPS or location services, and no payments.

## Tech

Java · Android SDK · SQLite (`SQLiteOpenHelper`) · RecyclerView with custom adapters ·
Material Components · Fragments and Activities connected with explicit Intents · JUnit 4
