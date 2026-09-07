package com.seanzenda.smartpantrymanager.model;

/**
 * A single ingredient the user currently has at home.
 * One row of the {@code pantry_items} table.
 */
public class PantryItem {

    /** Value used for {@link #expiryDate} when the user did not supply a date (it is optional). */
    public static final long NO_EXPIRY = 0L;

    private long id;
    private String name;
    private double quantity;
    private String unit;
    private String category;
    private long expiryDate;   // epoch millis, or NO_EXPIRY
    private long createdAt;    // epoch millis, used to order the pantry list

    public PantryItem() {
        this.id = -1;
        this.unit = "pcs";
        this.category = "Other";
        this.expiryDate = NO_EXPIRY;
    }

    public PantryItem(long id, String name, double quantity, String unit,
                      String category, long expiryDate, long createdAt) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.category = category;
        this.expiryDate = expiryDate;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getExpiryDate() { return expiryDate; }
    public void setExpiryDate(long expiryDate) { this.expiryDate = expiryDate; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean hasExpiry() { return expiryDate != NO_EXPIRY; }

    /** True when this item is a brand new (not yet saved) record. */
    public boolean isNew() { return id <= 0; }
}
