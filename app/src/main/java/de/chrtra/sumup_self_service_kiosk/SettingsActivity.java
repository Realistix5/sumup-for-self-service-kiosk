package de.chrtra.sumup_self_service_kiosk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends Activity {
    private TableLayout tableLayout;
    private Button saveButton;
    private SharedPreferences sharedPreferences;
    private boolean hasUnsavedChanges = false;

    private static class Setting {
        String name;
        String hint;
        String key;

        Setting(String name, String hint, String key) {
            this.name = name;
            this.hint = hint;
            this.key = key;
        }
    }

    private static class SettingGroup {
        String title;
        List<Setting> settings;

        SettingGroup(String title, List<Setting> settings) {
            this.title = title;
            this.settings = settings;
        }
    }

    private List<SettingGroup> settingGroups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tableLayout = findViewById(R.id.tableLayout);
        saveButton = findViewById(R.id.saveButton);
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);

        // Füge die Einstellungsgruppen zur Liste hinzu
        List<Setting> urlSettings = new ArrayList<>();
        urlSettings.add(new Setting(getString(R.string.payment_url_settings_scheme_label), getString(R.string.payment_url_settings_scheme_hint), "custom_url_schema"));
        urlSettings.add(new Setting(getString(R.string.payment_url_settings_host_label), getString(R.string.payment_url_settings_host_hint), "custom_url_host"));

        List<Setting> resultUrlSettings = new ArrayList<>();
        resultUrlSettings.add(new Setting(getString(R.string.website_settings_start_url_label), getString(R.string.website_settings_start_url_hint), "start_url"));
        resultUrlSettings.add(new Setting(getString(R.string.website_settings_success_url_label), getString(R.string.website_settings_success_url_hint), "success_url"));
        resultUrlSettings.add(new Setting(getString(R.string.website_settings_error_url_label), getString(R.string.website_settings_error_url_hint), "error_url"));

        settingGroups.add(new SettingGroup(getString(R.string.payment_url_settings_label), urlSettings));
        settingGroups.add(new SettingGroup(getString(R.string.website_settings_label), resultUrlSettings));

        loadSettings();

        saveButton.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        for (SettingGroup group : settingGroups) {
            // Hinzufügen der Gruppenüberschrift
            TextView header = new TextView(this);
            header.setText(group.title);
            header.setGravity(Gravity.START);
            header.setPadding(0, 20, 0, 10);
            header.setTextSize(18);
            tableLayout.addView(header);

            for (Setting setting : group.settings) {
                TableRow row = new TableRow(this);
                row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                TextView textView = new TextView(this);
                textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                textView.setText(setting.name);
                textView.setGravity(Gravity.END);
                textView.setPadding(0, 0, 16, 0);

                EditText editText = new EditText(this);
                editText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                editText.setHint(setting.hint);
                editText.setTag(setting.key); // Verwende den Key als Tag

                // Laden der gespeicherten Einstellung für dieses EditText-Feld
                String savedSetting = sharedPreferences.getString(setting.key, "");
                editText.setText(savedSetting);

                // Hinzufügen eines TextWatchers, um Änderungen zu überwachen
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        hasUnsavedChanges = true;
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });

                row.addView(textView);
                row.addView(editText);

                tableLayout.addView(row);
            }
        }
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Iteriere über alle TableRow-Kinder und speichere die Werte der EditText-Felder
        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            View view = tableLayout.getChildAt(i);
            if (view instanceof TableRow) {
                TableRow row = (TableRow) view;
                EditText editText = (EditText) row.getChildAt(1); // Das zweite Kind ist das EditText-Feld
                String key = (String) editText.getTag(); // Hole den Key aus dem Tag
                editor.putString(key, editText.getText().toString());
            }
        }

        editor.apply();
        hasUnsavedChanges = false;
        setResult(Activity.RESULT_OK); // Setze resultCode auf RESULT_OK (1)
        finish();
    }

    @Override
    public void onBackPressed() {
        if (hasUnsavedChanges) {
            new AlertDialog.Builder(this)
                    .setTitle("Änderungen verwerfen")
                    .setMessage("Es gibt ungespeicherte Änderungen. Möchten Sie diese verwerfen?")
                    .setPositiveButton("Ja", (dialog, which) -> {
                        setResult(Activity.RESULT_CANCELED); // Setze resultCode auf RESULT_CANCELED (0)
                        finish();
                    })
                    .setNegativeButton("Nein", null)
                    .show();
        } else {
            setResult(1); // Setze resultCode auf RESULT_CANCELED (0), wenn keine ungespeicherten Änderungen vorhanden sind
            super.onBackPressed();
        }
    }
}
