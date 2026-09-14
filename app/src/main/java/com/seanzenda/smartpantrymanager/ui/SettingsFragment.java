package com.seanzenda.smartpantrymanager.ui;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.seanzenda.smartpantrymanager.MainActivity;
import com.seanzenda.smartpantrymanager.R;
import com.seanzenda.smartpantrymanager.util.Prefs;

/**
 * Settings tab. Both settings are simple flags, saved with SharedPreferences through {@link Prefs}
 * so they survive the app being closed, just like the pantry data in SQLite.
 *
 * <ul>
 *     <li><b>Expiring Soon Alerts</b> - colours items by expiry, sorts recipes that use
 *         them first, and shows the count badge on the Expiring tab</li>
 *     <li><b>Units Preference</b> - which units the Add / Edit form offers</li>
 * </ul>
 */
public class SettingsFragment extends Fragment {

    private TextView unitsSubtitle;
    private String versionName = "1.0";

    public SettingsFragment() {
        super(R.layout.fragment_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        versionName = readVersionName();
        ((TextView) view.findViewById(R.id.app_version))
                .setText(getString(R.string.version_label, versionName));

        setUpAlertsToggle(view);

        unitsSubtitle = view.findViewById(R.id.units_subtitle);
        showUnits();
        view.findViewById(R.id.row_units).setOnClickListener(v -> chooseUnits());

        view.findViewById(R.id.row_about).setOnClickListener(v ->
                showInfo(R.string.about_title, getString(R.string.about_body, versionName)));
        view.findViewById(R.id.row_help).setOnClickListener(v ->
                showInfo(R.string.help_title, getString(R.string.help_body)));
    }

    private void setUpAlertsToggle(View view) {
        MaterialSwitch alertsSwitch = view.findViewById(R.id.switch_alerts);

        // Set the saved value before attaching the listener, so restoring it is not treated as a change.
        alertsSwitch.setChecked(Prefs.isExpiryAlertsOn(requireContext()));
        alertsSwitch.setOnCheckedChangeListener((button, isOn) -> {
            Prefs.setExpiryAlertsOn(requireContext(), isOn);
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).refreshExpiringBadge();
            }
        });

        // Tapping anywhere on the row flips the switch, not just the small switch itself.
        view.findViewById(R.id.row_alerts).setOnClickListener(v -> alertsSwitch.toggle());
    }

    private void showUnits() {
        unitsSubtitle.setText(Prefs.isMetric(requireContext())
                ? R.string.units_metric : R.string.units_imperial);
    }

    private void chooseUnits() {
        String[] options = {getString(R.string.units_metric), getString(R.string.units_imperial)};
        int checked = Prefs.isMetric(requireContext()) ? 0 : 1;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.units_pref)
                .setSingleChoiceItems(options, checked, (dialog, which) -> {
                    Prefs.setMetric(requireContext(), which == 0);
                    showUnits();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showInfo(@StringRes int title, String body) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(body)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    /** versionName from build.gradle, so the screen never shows a stale hard-coded number. */
    @SuppressWarnings("deprecation")   // the flags-object overload only exists from API 33
    private String readVersionName() {
        try {
            String name = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            return name == null ? "1.0" : name;
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0";
        }
    }
}
