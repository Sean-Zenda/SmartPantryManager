package com.seanzenda.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.seanzenda.smartpantrymanager.data.DatabaseHelper;
import com.seanzenda.smartpantrymanager.logic.UnitConverter;
import com.seanzenda.smartpantrymanager.model.PantryItem;
import com.seanzenda.smartpantrymanager.util.DateUtils;
import com.seanzenda.smartpantrymanager.util.FoodIcons;
import com.seanzenda.smartpantrymanager.util.FormValidator;
import com.seanzenda.smartpantrymanager.util.Insets;
import com.seanzenda.smartpantrymanager.util.Prefs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Add / Edit Ingredient screen - the Create, Update and Delete parts of CRUD.
 *
 * <p>One Activity handles both modes. It is opened with an explicit Intent:</p>
 * <ul>
 *     <li>with no extra - <b>add</b> mode, an empty form that inserts a new row</li>
 *     <li>with {@link #EXTRA_ITEM_ID} - <b>edit</b> mode, the row is loaded and can be updated or
 *         deleted</li>
 * </ul>
 *
 * <p>Only the id travels in the Intent, not the whole item. The database stays the single source of
 * truth, so the screen always shows the latest saved values.</p>
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    /** Intent extra: id of the pantry item to edit. Absent when adding a new item. */
    public static final String EXTRA_ITEM_ID = "com.seanzenda.smartpantrymanager.EXTRA_ITEM_ID";

    // Keys for state that the views do not save for us (EditText and Spinner restore themselves).
    private static final String STATE_EXPIRY = "state_expiry";
    private static final String STATE_CATEGORY = "state_category";

    private DatabaseHelper db;
    private PantryItem item;
    private boolean editMode;
    private long originalExpiry = PantryItem.NO_EXPIRY;

    private long expiryDate = PantryItem.NO_EXPIRY;
    private String category = "Fresh";

    // Errors only appear once the user has typed in a field, not on a brand new empty form.
    private boolean nameTouched;
    private boolean quantityTouched;

    private EditText nameInput;
    private EditText quantityInput;
    private TextView nameError;
    private TextView quantityError;
    private TextView expiryError;
    private TextView expiryText;
    private TextView iconPreview;
    private View clearDateButton;
    private Spinner unitSpinner;
    private MaterialButton saveButton;
    private TextView[] categoryChips;
    private List<String> units;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);
        Insets.applySystemBarsAndKeyboard(findViewById(R.id.add_edit_root));

        db = DatabaseHelper.getInstance(this);
        bindViews();

        // Which mode? Read the id passed in by the pantry list.
        long itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        editMode = itemId > 0;
        if (editMode) {
            item = db.getPantryItem(itemId);
            if (item == null) {                  // deleted in the meantime
                Toast.makeText(this, R.string.item_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            originalExpiry = item.getExpiryDate();
        } else {
            item = new PantryItem();
        }

        TextView title = findViewById(R.id.screen_title);
        title.setText(editMode ? R.string.edit_ingredient : R.string.add_ingredient);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        setUpUnitSpinner();
        setUpCategoryChips();
        setUpExpiryPicker();

        if (savedInstanceState == null) {
            if (editMode) populateFromItem();
        } else {
            // Restored after rotation: bring back what the views cannot restore by themselves.
            expiryDate = savedInstanceState.getLong(STATE_EXPIRY, PantryItem.NO_EXPIRY);
            category = savedInstanceState.getString(STATE_CATEGORY, "Fresh");
        }

        // Watchers are attached after pre-filling, so loading an item does not count as typing.
        setUpWatchers();

        MaterialButton deleteButton = findViewById(R.id.btn_delete);
        deleteButton.setVisibility(editMode ? View.VISIBLE : View.GONE);
        deleteButton.setOnClickListener(v -> confirmDelete());
        saveButton.setOnClickListener(v -> save());

        highlightCategory();
        showExpiry();
        refreshForm();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_EXPIRY, expiryDate);
        outState.putString(STATE_CATEGORY, category);
    }

    // =============================================================================================
    // Set-up
    // =============================================================================================

    private void bindViews() {
        nameInput = findViewById(R.id.name_input);
        quantityInput = findViewById(R.id.quantity_input);
        nameError = findViewById(R.id.name_error);
        quantityError = findViewById(R.id.quantity_error);
        expiryError = findViewById(R.id.expiry_error);
        expiryText = findViewById(R.id.expiry_text);
        iconPreview = findViewById(R.id.icon_preview);
        clearDateButton = findViewById(R.id.btn_clear_date);
        unitSpinner = findViewById(R.id.unit_spinner);
        saveButton = findViewById(R.id.btn_save);
    }

    /** The unit list follows the Metric / Imperial choice in Settings. */
    private void setUpUnitSpinner() {
        String[] base = Prefs.isMetric(this) ? UnitConverter.METRIC_UNITS
                : UnitConverter.IMPERIAL_UNITS;
        units = new ArrayList<>(Arrays.asList(base));

        // An item saved under the other system keeps its own unit available.
        if (editMode && !units.contains(item.getUnit())) {
            units.add(item.getUnit());
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, R.layout.item_spinner_unit, units);
        adapter.setDropDownViewResource(R.layout.item_spinner_unit);
        unitSpinner.setAdapter(adapter);
    }

    private void setUpCategoryChips() {
        categoryChips = new TextView[]{
                findViewById(R.id.cat_fresh),
                findViewById(R.id.cat_dry),
                findViewById(R.id.cat_dairy),
                findViewById(R.id.cat_other)
        };
        for (TextView chip : categoryChips) {
            chip.setOnClickListener(v -> {
                category = v.getTag().toString();
                highlightCategory();
                updatePreview();
            });
        }
    }

    private void setUpExpiryPicker() {
        expiryText.setOnClickListener(v -> openDatePicker());
        clearDateButton.setOnClickListener(v -> {
            expiryDate = PantryItem.NO_EXPIRY;
            showExpiry();
            refreshForm();
        });
    }

    private void setUpWatchers() {
        nameInput.addTextChangedListener(new SimpleWatcher(() -> {
            nameTouched = true;
            refreshForm();
        }));
        quantityInput.addTextChangedListener(new SimpleWatcher(() -> {
            quantityTouched = true;
            refreshForm();
        }));
    }

    /** Edit mode: copy the saved values into the form. */
    private void populateFromItem() {
        nameInput.setText(item.getName());
        quantityInput.setText(UnitConverter.format(item.getQuantity()));
        unitSpinner.setSelection(Math.max(0, units.indexOf(item.getUnit())));
        category = item.getCategory();
        expiryDate = item.getExpiryDate();
    }

    // =============================================================================================
    // Form state
    // =============================================================================================

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (expiryDate != PantryItem.NO_EXPIRY) calendar.setTimeInMillis(expiryDate);

        DatePickerDialog dialog = new DatePickerDialog(this, (picker, year, month, day) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, day);
            expiryDate = DateUtils.startOfDay(picked.getTimeInMillis());
            showExpiry();
            refreshForm();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // Days before today are greyed out, so a past date cannot be picked in the first place.
        dialog.getDatePicker().setMinDate(DateUtils.startOfDay(System.currentTimeMillis()));
        dialog.show();
    }

    private void showExpiry() {
        boolean hasDate = expiryDate != PantryItem.NO_EXPIRY;
        expiryText.setText(hasDate ? DateUtils.format(expiryDate)
                : getString(R.string.hint_select_date));
        expiryText.setTextColor(ContextCompat.getColor(this,
                hasDate ? R.color.text_primary : R.color.text_muted));
        clearDateButton.setVisibility(hasDate ? View.VISIBLE : View.GONE);
    }

    private void highlightCategory() {
        for (TextView chip : categoryChips) {
            boolean on = chip.getTag().toString().equals(category);
            chip.setBackgroundResource(on ? R.drawable.bg_chip_on : R.drawable.bg_chip_off);
            chip.setTextColor(ContextCompat.getColor(this, on ? R.color.white : R.color.text_primary));
        }
    }

    private void updatePreview() {
        iconPreview.setText(FoodIcons.forItem(nameInput.getText().toString(), category));
    }

    /**
     * Re-validates every field, shows the errors for fields the user has touched, and only enables
     * Save when the whole form is valid.
     */
    private void refreshForm() {
        int nameResult = FormValidator.checkName(nameInput.getText().toString());
        int quantityResult = FormValidator.checkQuantity(quantityInput.getText().toString());
        int expiryResult = FormValidator.checkExpiry(expiryDate, originalExpiry);

        showError(nameInput, nameError, nameTouched ? nameResult : 0);
        showError(quantityInput, quantityError, quantityTouched ? quantityResult : 0);
        showError(null, expiryError, expiryResult);

        saveButton.setEnabled(nameResult == 0 && quantityResult == 0 && expiryResult == 0);
        updatePreview();
    }

    /** Red outline plus message when {@code error} is set; normal field when it is 0. */
    private void showError(View field, TextView errorView, @StringRes int error) {
        boolean hasError = error != 0;
        errorView.setVisibility(hasError ? View.VISIBLE : View.GONE);
        if (hasError) errorView.setText(error);
        if (field != null) {
            field.setBackgroundResource(hasError ? R.drawable.bg_field_error : R.drawable.bg_field);
        }
    }

    // =============================================================================================
    // Create / Update / Delete
    // =============================================================================================

    private void save() {
        // Save is disabled while invalid, but check again - never trust UI state alone.
        nameTouched = true;
        quantityTouched = true;
        refreshForm();
        if (!saveButton.isEnabled()) return;

        item.setName(FormValidator.cleanName(nameInput.getText().toString()));
        item.setQuantity(FormValidator.parseQuantity(quantityInput.getText().toString()));
        item.setUnit((String) unitSpinner.getSelectedItem());
        item.setCategory(category);
        item.setExpiryDate(expiryDate);

        boolean ok;
        if (editMode) {
            ok = db.updatePantryItem(item) == 1;                 // UPDATE
        } else {
            ok = db.insertPantryItem(item) != -1;                // CREATE
        }

        if (!ok) {
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, getString(editMode ? R.string.toast_updated : R.string.toast_added,
                item.getName()), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();   // back to the pantry list, which reloads in onResume()
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.delete_confirm_title, item.getName()))
                .setMessage(R.string.delete_confirm_body)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    db.deletePantryItem(item.getId());           // DELETE
                    Toast.makeText(this, getString(R.string.toast_deleted, item.getName()),
                            Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .show();
    }

    /** TextWatcher that only cares about "the text changed". */
    private static class SimpleWatcher implements TextWatcher {
        private final Runnable onChange;

        SimpleWatcher(Runnable onChange) {
            this.onChange = onChange;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
        @Override public void afterTextChanged(Editable s) { onChange.run(); }
    }
}
