package de.chrtra.sumup_self_service_kiosk;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SettingsActivity extends Activity {
    private TableLayout tableLayout;
    private Button saveButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tableLayout = findViewById(R.id.tableLayout);
        saveButton = findViewById(R.id.saveButton);
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);

        loadSettings();

        saveButton.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        String[] settingNames = {"Custom-URL Schema", "Custom-URL Host", "Start-URL", "Rückmeldungs-URL"};
        String[] settingHints = {"sumup", "bezahlen", "https://meine-seite.de", "https://meine-seite.de/verarbeite_rückmeldung"};

        for (int i = 0; i < settingNames.length; i++) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            TextView textView = new TextView(this);
            textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            textView.setText(settingNames[i]);
            textView.setGravity(Gravity.END);
            textView.setPadding(0, 0, 16, 0);

            EditText editText = new EditText(this);
            editText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            editText.setHint(settingHints[i]);
            editText.setId(i + 1); // Setze eine eindeutige ID für jedes EditText-Feld

            // Laden der gespeicherten Einstellung für dieses EditText-Feld
            String savedSetting = sharedPreferences.getString("setting" + (i + 1), "");
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
                editor.putString("setting" + editText.getId(), editText.getText().toString());
            }
        }

        editor.apply();
        finish();
    }
}
