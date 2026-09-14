package com.seanzenda.smartpantrymanager.util;

import static org.junit.Assert.assertEquals;

import com.seanzenda.smartpantrymanager.R;
import com.seanzenda.smartpantrymanager.model.PantryItem;

import org.junit.Test;

/** Unit tests for the Add / Edit Ingredient form rules. */
public class FormValidatorTest {

    @Test
    public void validName_hasNoError() {
        assertEquals(0, FormValidator.checkName("Tomatoes"));
        assertEquals(0, FormValidator.checkName("  Chicken breast  "));
    }

    @Test
    public void badNames_areRejected() {
        assertEquals(R.string.err_name_required, FormValidator.checkName("   "));
        assertEquals(R.string.err_name_short, FormValidator.checkName("To"));
        assertEquals(R.string.err_name_chars, FormValidator.checkName("123"));
        assertEquals(R.string.err_name_long,
                FormValidator.checkName("A very very very long ingredient name indeed"));
    }

    @Test
    public void quantities_areChecked() {
        assertEquals(0, FormValidator.checkQuantity("3"));
        assertEquals(0, FormValidator.checkQuantity("1,5"));
        assertEquals(R.string.err_qty_required, FormValidator.checkQuantity(""));
        assertEquals(R.string.err_qty_positive, FormValidator.checkQuantity("0"));
        assertEquals(R.string.err_qty_number, FormValidator.checkQuantity("."));
        assertEquals(R.string.err_qty_too_big, FormValidator.checkQuantity("9999999"));
    }

    @Test
    public void pastExpiry_isRejectedUnlessUnchanged() {
        long yesterday = System.currentTimeMillis() - 24L * 60 * 60 * 1000;
        assertEquals(R.string.err_expiry_past,
                FormValidator.checkExpiry(yesterday, PantryItem.NO_EXPIRY));
        assertEquals(0, FormValidator.checkExpiry(yesterday, yesterday));
        assertEquals(0, FormValidator.checkExpiry(PantryItem.NO_EXPIRY, PantryItem.NO_EXPIRY));
    }

    @Test
    public void namesAreTidiedForStorage() {
        assertEquals("Olive oil", FormValidator.cleanName("  olive    oil "));
    }
}
