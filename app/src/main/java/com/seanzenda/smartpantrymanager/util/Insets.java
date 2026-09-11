package com.seanzenda.smartpantrymanager.util;

import android.view.View;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Keeps content clear of the status bar and gesture bar.
 *
 * <p>From Android 15 (API 35) apps are drawn edge-to-edge, behind the system bars, whether they ask
 * for it or not. Without this padding the screen titles would sit underneath the clock and battery
 * icons.</p>
 */
public final class Insets {

    private Insets() { }

    /** Pads the view by the system bar sizes. Pass false for bottom when a BottomNavigationView does it. */
    public static void applySystemBars(View view, boolean padBottom) {
        apply(view, padBottom, false);
    }

    /**
     * Like {@link #applySystemBars} but also lifts the content above the on-screen keyboard, so the
     * Save button on a form is never hidden while the user is typing.
     */
    public static void applySystemBarsAndKeyboard(View view) {
        apply(view, true, true);
    }

    private static void apply(View view, boolean padBottom, boolean includeKeyboard) {
        final int left = view.getPaddingLeft();
        final int top = view.getPaddingTop();
        final int right = view.getPaddingRight();
        final int bottom = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            androidx.core.graphics.Insets bars =
                    windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout());
            int bottomInset = padBottom ? bars.bottom : 0;
            if (includeKeyboard) {
                bottomInset = Math.max(bottomInset,
                        windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom);
            }
            v.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bottomInset);
            // Not consumed, so child views (e.g. the bottom navigation bar) still receive the insets.
            return windowInsets;
        });
    }
}
