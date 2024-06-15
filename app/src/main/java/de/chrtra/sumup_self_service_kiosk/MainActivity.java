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

public class MainActivity extends Activity {

    private static final int REQUEST_CODE_LOGIN = 1;
    private static final int REQUEST_CODE_WEBVIEW = 2;
    private static final int REQUEST_CODE_CARD_READER_PAGE = 4;
    private static final int REQUEST_CODE_SETTINGS = 11;

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

        mMessage.setText("Wilkommen in der Self-Service Kiosk App!");

        Button login = (Button) findViewById(R.id.button_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Please go to https://me.sumup.com/developers to get your Affiliate Key by entering the application ID of your app. (e.g. com.sumup.sdksampleapp)
                SumUpLogin sumupLogin = SumUpLogin.builder(sharedPreferences.getString("affiliate_key", "")).build();
                SumUpAPI.openLoginActivity(MainActivity.this, sumupLogin, REQUEST_CODE_LOGIN);
            }
        });

        Button logMerchant = (Button) findViewById(R.id.button_log_merchant);
        logMerchant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!SumUpAPI.isLoggedIn()) {
                    mMessage.setText("Not logged in");
                } else {
                    mMessage.setText(
                            String.format("Currency: %s, Merchant Code: %s", SumUpAPI.getCurrentMerchant().getCurrency().getIsoCode(),
                                    SumUpAPI.getCurrentMerchant().getMerchantCode()));
                }
            }
        });

        Button cardReaderPage = (Button) findViewById(R.id.button_card_reader_page);
        cardReaderPage.setOnClickListener(v -> SumUpAPI.openCardReaderPage(MainActivity.this, REQUEST_CODE_CARD_READER_PAGE));

        Button btnLogout = (Button) findViewById(R.id.button_logout);
        btnLogout.setOnClickListener(v -> SumUpAPI.logout());

        Button openWebView = (Button) findViewById(R.id.btn_open_webview);
        openWebView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (areSettingsValid()) {
                    if (SumUpAPI.isLoggedIn()) {
                        requestDevicePIN();
                    } else {
                        openLoginActivity();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Bitte setzen Sie alle erforderlichen Einstellungen.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean areSettingsValid() {
        // Hier die Logik hinzufügen, um die erforderlichen Einstellungen zu überprüfen
        // Zum Beispiel:
        String setting1 = sharedPreferences.getString("start_url", "");
        String setting2 = sharedPreferences.getString("success_url", "");
        return !setting1.isEmpty() && !setting2.isEmpty();
    }

    private void openLoginActivity() {
        SumUpLogin sumupLogin = SumUpLogin.builder(sharedPreferences.getString("affiliate_key", "")).build();
        SumUpAPI.openLoginActivity(MainActivity.this, sumupLogin, REQUEST_CODE_LOGIN);
    }

    private void requestDevicePIN() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager != null && keyguardManager.isKeyguardSecure()) {
            Intent intent = keyguardManager.createConfirmDeviceCredentialIntent("Geräte-PIN erforderlich", "Bitte geben Sie Ihre Geräte-PIN ein, um den Kiosk-Modus zu beenden.");
            if (intent != null) {
                openWebViewActivity();
            }
        } else {
            Toast.makeText(this, "Gerätesperre ist nicht eingerichtet. Der Kiosk-Modus kann nicht beendet werden.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_LOGIN:
            case REQUEST_CODE_CARD_READER_PAGE:
                if (data != null) {
                    Bundle extra = data.getExtras();
                    String message = extra.getString(SumUpAPI.Response.MESSAGE);
                    mMessage.setText(message);
                }
                break;
            case REQUEST_CODE_SETTINGS:
                if (resultCode == Activity.RESULT_OK) {
                    mMessage.setText("Einstellungen gespeichert.");
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    mMessage.setText("Änderungen an den Einstellungen wurden verworfen.");
                } else if (resultCode == SettingsActivity.RESULT_NO_CHANGES) {
                    mMessage.setText("");
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
        mMessage = (TextView) findViewById(R.id.message);
    }

    public void openSettingsActivity(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SETTINGS);
    }
}
