package de.chrtra.sumup_self_service_kiosk;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sumup.merchant.reader.api.SumUpAPI;
import com.sumup.merchant.reader.api.SumUpLogin;

import java.util.Objects;
import java.util.Set;

public class MainActivity extends Activity {

    private static final int REQUEST_CODE_LOGIN = 1;
    private static final int REQUEST_CODE_WEBVIEW = 2;
    private static final int REQUEST_CODE_CARD_READER_PAGE = 4;
    private static final int REQUEST_CODE_SETTINGS = 11;
    private static final int DEVICE_PIN_REQUEST_CODE = 101; // Request-Code für die Geräte-PIN-Abfrage

    private SharedPreferences sharedPreferences;

    private TextView mMessage;

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminComponent = new ComponentName(this, MyDeviceAdminReceiver.class);
        dpm.setLockTaskPackages(adminComponent, new String[]{getPackageName()});

        findViews();

        mMessage.setText(R.string.welcome_message);

        Button login = findViewById(R.id.button_login);
        login.setOnClickListener(v -> {
            // Please go to https://me.sumup.com/developers to get your Affiliate Key by entering the application ID of your app. (e.g. com.sumup.sdksampleapp)
            SumUpLogin sumupLogin = SumUpLogin.builder(sharedPreferences.getString("affiliate_key", "")).build();
            SumUpAPI.openLoginActivity(MainActivity.this, sumupLogin, REQUEST_CODE_LOGIN);
        });

        Button logMerchant = findViewById(R.id.button_log_merchant);
        logMerchant.setOnClickListener(view -> {
            if (!SumUpAPI.isLoggedIn()) {
                mMessage.setText(R.string.not_logged_in_message);
            } else {
                mMessage.setText(
                        String.format("Currency: %s, Merchant Code: %s", Objects.requireNonNull(SumUpAPI.getCurrentMerchant()).getCurrency().getIsoCode(),
                                SumUpAPI.getCurrentMerchant().getMerchantCode()));
            }
        });

        Button cardReaderPage = findViewById(R.id.button_card_reader_page);
        cardReaderPage.setOnClickListener(v -> SumUpAPI.openCardReaderPage(MainActivity.this, REQUEST_CODE_CARD_READER_PAGE));

        Button btnLogout = findViewById(R.id.button_logout);
        btnLogout.setOnClickListener(v -> SumUpAPI.logout());

        Button openWebView = findViewById(R.id.btn_open_webview);
        openWebView.setOnClickListener(v -> {
            if (areSettingsValid()) {
                if (SumUpAPI.isLoggedIn()) {
                    requestDevicePIN();
                } else {
                    openLoginActivity();
                }
            } else {
                Toast.makeText(MainActivity.this, R.string.settings_not_set_message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean areSettingsValid() {
        // Hier die Logik hinzufügen, um die erforderlichen Einstellungen zu überprüfen
        // Zum Beispiel:
        Set<String> mandatory_settings = Set.of(
                "custom_url_schema",
                "custom_url_host",
                "start_url",
                "success_url",
                "error_url",
                "currency"
        );
        for (String setting_id : mandatory_settings) {
            String setting = sharedPreferences.getString(setting_id, "");
            if (setting.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void openLoginActivity() {
        SumUpLogin sumupLogin = SumUpLogin.builder(sharedPreferences.getString("affiliate_key", "")).build();
        SumUpAPI.openLoginActivity(MainActivity.this, sumupLogin, REQUEST_CODE_LOGIN);
    }

    private void requestDevicePIN() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager != null && keyguardManager.isKeyguardSecure()) {
            Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(getString(R.string.device_pin_required_title), getString(R.string.device_pin_required_start_description));
            if (intent != null) {
                startActivityForResult(intent, DEVICE_PIN_REQUEST_CODE);
            }
        } else {
            Toast.makeText(this, R.string.no_device_lock_available_message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_LOGIN:
            case REQUEST_CODE_CARD_READER_PAGE:
                if (data != null) {
                    Bundle extra = data.getExtras();
                    assert extra != null;
                    String message = extra.getString(SumUpAPI.Response.MESSAGE);
                    mMessage.setText(message);
                }
                break;
            case REQUEST_CODE_SETTINGS:
                if (resultCode == Activity.RESULT_OK) {
                    mMessage.setText(R.string.settings_saved_message);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    mMessage.setText(R.string.settings_discarded_message);
                } else if (resultCode == SettingsActivity.RESULT_NO_CHANGES) {
                    mMessage.setText("");
                }
                break;
            case DEVICE_PIN_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    openWebViewActivity();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.authentification_failed_message, Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void openWebViewActivity() {
        Intent intent = new Intent(this, WebViewActivity.class);
        startActivityForResult(intent, REQUEST_CODE_WEBVIEW);
    }

    private void findViews() {
        mMessage = findViewById(R.id.message);
    }

    public void openSettingsActivity(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SETTINGS);
    }
}
