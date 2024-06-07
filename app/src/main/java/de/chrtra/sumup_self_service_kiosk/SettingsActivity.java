package de.chrtra.sumup_self_service_kiosk;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    private List<Setting> settings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tableLayout = findViewById(R.id.tableLayout);
        saveButton = findViewById(R.id.saveButton);
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);

        // Füge die Einstellungen zur Liste hinzu
        settings.add(new Setting("Custom-URL Schema", "sumup", "custom_url_schema"));
        settings.add(new Setting("Custom-URL Host", "bezahlen", "custom_url_host"));
        settings.add(new Setting("Start-URL", "https://meine-seite.de", "start_url"));
        settings.add(new Setting("Erfolgsmeldungs-URL", "https://meine-seite.de/erfolg", "success_url"));
        settings.add(new Setting("Fehlermeldungs-URL", "https://meine-seite.de/fehler", "error_url"));

        loadSettings();

        saveButton.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        for (Setting setting : settings) {
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

            row.addView(textView);
            row.addView(editText);

            tableLayout.addView(row);
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
        finish();
    }
}
