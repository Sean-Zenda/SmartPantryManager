package com.seanzenda.smartpantrymanager.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.seanzenda.smartpantrymanager.R;
import com.seanzenda.smartpantrymanager.adapter.PantryAdapter;
import com.seanzenda.smartpantrymanager.data.DatabaseHelper;
import com.seanzenda.smartpantrymanager.model.PantryItem;
import com.seanzenda.smartpantrymanager.util.Prefs;

import java.util.List;

/**
 * Pantry tab: every ingredient the user has at home, read from SQLite into a RecyclerView.
 *
 * <p>The list is reloaded in {@link #onResume()} rather than {@link #onViewCreated}. onResume runs
 * every time this screen comes back into view - including when the user returns from adding or
 * editing an item - so the list always reflects what is in the database.</p>
 */
public class PantryFragment extends Fragment {

    private DatabaseHelper db;
    private PantryAdapter adapter;

    private RecyclerView list;
    private View emptyState;
    private TextView emptyTitle;
    private TextView emptyBody;
    private TextView[] chips;

    /** Current filters. A null category means the "All" chip. */
    private String searchText = "";
    @Nullable private String category = null;

    public PantryFragment() {
        super(R.layout.fragment_pantry);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = DatabaseHelper.getInstance(requireContext());

        list = view.findViewById(R.id.pantry_list);
        emptyState = view.findViewById(R.id.empty_state);
        emptyTitle = view.findViewById(R.id.empty_title);
        emptyBody = view.findViewById(R.id.empty_body);

        adapter = new PantryAdapter(this::onItemClicked);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);

        setUpSearch(view.findViewById(R.id.search_input));
        setUpChips(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPantry();
    }

    /** Reads the pantry through the current search and category filters. */
    private void loadPantry() {
        List<PantryItem> items = db.getPantryItems(searchText, category);
        adapter.setItems(items, Prefs.isExpiryAlertsOn(requireContext()));

        boolean empty = items.isEmpty();
        list.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);

        if (empty) {
            // Distinguish "you have nothing" from "nothing matches your filter".
            boolean filtering = !searchText.isEmpty() || category != null;
            emptyTitle.setText(filtering ? R.string.pantry_no_results_title
                    : R.string.pantry_empty_title);
            emptyBody.setText(filtering ? R.string.pantry_no_results_body
                    : R.string.pantry_empty_body);
        }
    }

    private void setUpSearch(EditText search) {
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                searchText = s.toString().trim();
                loadPantry();
            }
        });
    }

    private void setUpChips(View root) {
        chips = new TextView[]{
                root.findViewById(R.id.chip_all),
                root.findViewById(R.id.chip_fresh),
                root.findViewById(R.id.chip_dry),
                root.findViewById(R.id.chip_dairy),
                root.findViewById(R.id.chip_other)
        };
        for (TextView chip : chips) {
            chip.setOnClickListener(v -> {
                // Each chip's android:tag holds its category name; the "All" chip has no tag.
                Object tag = v.getTag();
                category = tag == null ? null : tag.toString();
                highlightChip((TextView) v);
                loadPantry();
            });
        }
        highlightChip(chips[0]);
    }

    private void highlightChip(TextView selected) {
        for (TextView chip : chips) {
            boolean on = chip == selected;
            chip.setBackgroundResource(on ? R.drawable.bg_chip_on : R.drawable.bg_chip_off);
            chip.setTextColor(ContextCompat.getColor(requireContext(),
                    on ? R.color.white : R.color.text_primary));
        }
    }

    private void onItemClicked(PantryItem item) {
        // Opening the edit screen is wired up together with the Add / Edit activity.
    }
}
