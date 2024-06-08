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
import android.widget.CheckBox;
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
    public static final int RESULT_NO_CHANGES = 33;

    private static class Setting {
        String name;
        String hint;
        String key;
        boolean isBoolean;

        Setting(String name, String hint, String key, boolean isBoolean) {
            this.name = name;
            this.hint = hint;
            this.key = key;
            this.isBoolean = isBoolean;
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
        urlSettings.add(new Setting(getString(R.string.payment_url_settings_scheme_label), getString(R.string.payment_url_settings_scheme_hint), "custom_url_schema", false));
        urlSettings.add(new Setting(getString(R.string.payment_url_settings_host_label), getString(R.string.payment_url_settings_host_hint), "custom_url_host", false));
        settingGroups.add(new SettingGroup(getString(R.string.payment_url_settings_label), urlSettings));

        List<Setting> resultUrlSettings = new ArrayList<>();
        resultUrlSettings.add(new Setting(getString(R.string.website_settings_start_url_label), getString(R.string.website_settings_start_url_hint), "start_url", false));
        resultUrlSettings.add(new Setting(getString(R.string.website_settings_success_url_label), getString(R.string.website_settings_success_url_hint), "success_url", false));
        resultUrlSettings.add(new Setting(getString(R.string.website_settings_error_url_label), getString(R.string.website_settings_error_url_hint), "error_url", false));
        settingGroups.add(new SettingGroup(getString(R.string.website_settings_label), resultUrlSettings));

        List<Setting> webViewSettings = new ArrayList<>();
        webViewSettings.add(new Setting(getString(R.string.webview_settings_ignore_ssl_errors_label), "", "ignore_ssl_errors", true));
        settingGroups.add(new SettingGroup(getString(R.string.webview_settings_label), webViewSettings));

        List<Setting> paymentSettings = new ArrayList<>();
        paymentSettings.add(new Setting(getString(R.string.payment_settings_currency_code_label), getString(R.string.payment_settings_currency_code_hint), "currency", false));
        paymentSettings.add(new Setting(getString(R.string.payment_settings_title_label), getString(R.string.payment_settings_title_hint), "payment_title", false));
        paymentSettings.add(new Setting(getString(R.string.payment_settings_additional_info_label), getString(R.string.payment_settings_additional_info_hint), "additional_info", false));
        paymentSettings.add(new Setting(getString(R.string.payment_settings_tip_on_card_reader_label), "", "tip_on_card_reader", true));
        paymentSettings.add(new Setting(getString(R.string.payment_settings_skip_success_screen_label), "", "skip_success_screen", true));
        paymentSettings.add(new Setting(getString(R.string.payment_settings_skip_error_screen_label), "", "skip_failed_screen", true));
        paymentSettings.add(new Setting(getString(R.string.payment_settings_affiliate_key_label), getString(R.string.payment_settings_affiliate_key_hint), "affiliate_key", false));
        settingGroups.add(new SettingGroup(getString(R.string.payment_settings_label), paymentSettings));


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

                if (setting.isBoolean) {
                    CheckBox checkBox = new CheckBox(this);
                    checkBox.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                    checkBox.setTag(setting.key);

                    // Laden der gespeicherten Einstellung für dieses CheckBox-Feld
                    boolean savedSetting = sharedPreferences.getBoolean(setting.key, false);
                    checkBox.setChecked(savedSetting);

                    // Hinzufügen eines OnCheckedChangeListener, um Änderungen zu überwachen
                    checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> hasUnsavedChanges = true);

                    row.addView(textView);
                    row.addView(checkBox);
                } else {
                    EditText editText = new EditText(this);
                    editText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                    editText.setHint(setting.hint);
                    editText.setTag(setting.key);

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
                }

                tableLayout.addView(row);
            }
        }
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Iteriere über alle TableRow-Kinder und speichere die Werte der EditText- und CheckBox-Felder
        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            View view = tableLayout.getChildAt(i);
            if (view instanceof TableRow) {
                TableRow row = (TableRow) view;
                View valueView = row.getChildAt(1);
                if (valueView instanceof EditText) {
                    EditText editText = (EditText) valueView;
                    String key = (String) editText.getTag();
                    editor.putString(key, editText.getText().toString());
                } else if (valueView instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) valueView;
                    String key = (String) checkBox.getTag();
                    editor.putBoolean(key, checkBox.isChecked());
                }
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
            setResult(RESULT_NO_CHANGES); // Setze resultCode auf RESULT_NO_CHANGES (33), wenn keine ungespeicherten Änderungen vorhanden sind
            super.onBackPressed();
        }
    }
}
