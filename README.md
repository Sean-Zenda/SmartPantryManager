# Smart Pantry Manager

An Android application (Java) that helps you **reduce food waste** by tracking the ingredients you
actually have at home, and suggesting only the recipes you can cook **right now** — no shopping trip
required.

> Module: Mobile App Development 700 — Practical Assignment

## The core idea: strict matching

A recipe is only ever shown under **Suggested Recipes** if **every single ingredient it requires** is
currently in your pantry, **in at least the required quantity**. A recipe that needs 5 ingredients
while your pantry only has 4 of them will never appear in that list.

Recipes that are missing exactly one ingredient are shown in a **clearly separated** "Almost There"
section further down the screen, so the strict list is never diluted.

## Features

- Pantry management — full **Create / Read / Update / Delete** on ingredients (name, quantity, unit,
  category, optional expiry date)
- Pantry list with live search and category filters (All / Fresh / Dry / Dairy / Other)
- 20 seeded recipes with required ingredients and step-by-step method
- Suggested Recipes screen driven by the strict-matching engine
- Recipe detail screen with full ingredient list and numbered method
- Settings screen — expiring-soon alerts toggle and metric/imperial units preference
- Friendly empty state when no recipe matches the pantry
- Input validation on the Add / Edit Ingredient form

## Database choice: SQLite (`SQLiteOpenHelper`)

SQLite was chosen for three reasons:

1. **Offline first.** A pantry app is used standing in the kitchen. There is no reason to require a
   network connection to read a list of ingredients you own, and SQLite works with no connectivity.
2. **The matching logic is relational.** A recipe *has many* required ingredients, which is a classic
   one-to-many relationship. Three tables (`pantry_items`, `recipes`, `recipe_ingredients`) model this
   naturally, and a `JOIN` retrieves a full recipe in a single query.
3. **No account, no cost, no setup.** Firebase would need a Google account, a `google-services.json`
   and internet; PostgreSQL would need a hosted server plus a REST backend. Neither adds value for
   single-user, device-local data — they only add failure modes.

Data is written to the device database file, so everything **persists after the app is closed and
reopened**.

## Screens

| Screen | Class | Navigation |
|---|---|---|
| Welcome | `WelcomeActivity` | Launcher → `MainActivity` via `Intent` |
| Pantry List | `PantryFragment` | Bottom navigation |
| Add / Edit Ingredient | `AddEditIngredientActivity` | `Intent` + `putExtra(EXTRA_ITEM_ID, …)` |
| Suggested Recipes | `RecipesFragment` | Bottom navigation |
| Recipe Detail | `RecipeDetailActivity` | `Intent` + `putExtra(EXTRA_RECIPE_ID, …)` |
| Settings | `SettingsFragment` | Bottom navigation |

## Setup and run

1. Clone the repository:
   ```
   git clone <your-repo-url>
   ```
2. Open the project folder in **Android Studio** (Giraffe or newer).
3. Let Gradle sync. The project requires **JDK 21** and **Android SDK Platform 37**
   (`compileSdk 37`, `minSdk 24`).
4. Select an emulator or a physical device (Android 7.0 / API 24 or higher).
5. Press **Run ▶**. The recipe collection is seeded automatically on first launch.

No API keys, `local.properties` entries or backend services are required.

## Out of scope (by assignment instruction)

No Google Maps, no mapping SDK, no GPS or location services, no payments.

## Tech

Java · Android SDK · SQLite (`SQLiteOpenHelper`) · RecyclerView + custom Adapters ·
Material Components · Fragments + Activities navigated with explicit Intents
