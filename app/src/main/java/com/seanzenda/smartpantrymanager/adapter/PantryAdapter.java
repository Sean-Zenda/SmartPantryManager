package com.seanzenda.smartpantrymanager.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.seanzenda.smartpantrymanager.R;
import com.seanzenda.smartpantrymanager.logic.UnitConverter;
import com.seanzenda.smartpantrymanager.model.PantryItem;
import com.seanzenda.smartpantrymanager.util.DateUtils;
import com.seanzenda.smartpantrymanager.util.FoodIcons;

import java.util.ArrayList;
import java.util.List;

/**
 * Binds {@link PantryItem} rows from the database to the {@code item_pantry} layout.
 *
 * <p>RecyclerView only creates enough row views to fill the screen (plus a few spare). As the user
 * scrolls, rows that leave the screen are handed back to {@link #onBindViewHolder} and filled with
 * the next item's data, instead of a new view being inflated for every single item.</p>
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    /** Callback so the adapter never needs to know which screen it is used on. */
    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final OnItemClickListener listener;
    private boolean expiryAlertsOn = true;

    public PantryAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    /** Replaces the list contents with a fresh read from the database. */
    public void setItems(List<PantryItem> newItems, boolean alertsOn) {
        items.clear();
        items.addAll(newItems);
        expiryAlertsOn = alertsOn;
        // The pantry is small, so redrawing every row is cheaper to reason about than a diff.
        notifyDataSetChanged();
    }

    public PantryItem getItem(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PantryItem item = items.get(position);

        holder.icon.setText(FoodIcons.forItem(item.getName(), item.getCategory()));
        holder.name.setText(item.getName());
        holder.quantity.setText(holder.itemView.getContext().getString(R.string.quantity_unit,
                UnitConverter.format(item.getQuantity()), item.getUnit()));
        holder.expiry.setText(DateUtils.label(holder.itemView.getContext(), item));

        int color = ContextCompat.getColor(holder.itemView.getContext(),
                DateUtils.statusColor(item, expiryAlertsOn));
        holder.dot.setBackgroundTintList(ColorStateList.valueOf(color));

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Holds the row's views so findViewById runs once per row, not once per bind. */
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView icon;
        final TextView name;
        final TextView quantity;
        final TextView expiry;
        final View dot;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.item_icon);
            name = itemView.findViewById(R.id.item_name);
            quantity = itemView.findViewById(R.id.item_quantity);
            expiry = itemView.findViewById(R.id.item_expiry);
            dot = itemView.findViewById(R.id.item_status_dot);
        }
    }
}
