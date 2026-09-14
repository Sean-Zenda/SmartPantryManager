package com.seanzenda.smartpantrymanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.seanzenda.smartpantrymanager.AddEditIngredientActivity;
import com.seanzenda.smartpantrymanager.MainActivity;
import com.seanzenda.smartpantrymanager.R;
import com.seanzenda.smartpantrymanager.adapter.PantryAdapter;
import com.seanzenda.smartpantrymanager.data.DatabaseHelper;
import com.seanzenda.smartpantrymanager.model.PantryItem;
import com.seanzenda.smartpantrymanager.util.DateUtils;
import com.seanzenda.smartpantrymanager.util.Prefs;

import java.util.List;

/**
 * Expiring Soon tab: pantry items that expire within the next few days (or already have), soonest
 * first, so the user knows what to use before it goes to waste.
 *
 * <p>It reuses {@link PantryAdapter} - the same adapter as the Pantry tab, fed a different query.</p>
 */
public class ExpiringFragment extends Fragment {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private RecyclerView list;
    private View emptyState;
    private View alertsOffState;

    public ExpiringFragment() {
        super(R.layout.fragment_expiring);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = DatabaseHelper.getInstance(requireContext());

        list = view.findViewById(R.id.expiring_list);
        emptyState = view.findViewById(R.id.empty_state);
        alertsOffState = view.findViewById(R.id.alerts_off_state);

        adapter = new PantryAdapter(this::openItem);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);

        view.findViewById(R.id.btn_turn_on).setOnClickListener(v -> {
            Prefs.setExpiryAlertsOn(requireContext(), true);
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).refreshExpiringBadge();
            }
            load();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        boolean alertsOn = Prefs.isExpiryAlertsOn(requireContext());
        alertsOffState.setVisibility(alertsOn ? View.GONE : View.VISIBLE);
        if (!alertsOn) {
            list.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
            return;
        }

        List<PantryItem> items = db.getExpiringItems(DateUtils.soonCutoff());
        adapter.setItems(items, true);
        list.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openItem(PantryItem item) {
        Intent intent = new Intent(requireContext(), AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }
}
