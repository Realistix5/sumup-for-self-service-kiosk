package de.chrtra.sumup_self_service_kiosk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

import com.sumup.merchant.reader.api.SumUpAPI;
import com.sumup.merchant.reader.api.SumUpLogin;

public class MainActivity extends Activity {

    private static final int REQUEST_CODE_LOGIN = 1;
    private static final int REQUEST_CODE_WEBVIEW = 2;
    private static final int REQUEST_CODE_CARD_READER_PAGE = 4;

    private TextView mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminComponent = new ComponentName(this, MyDeviceAdminReceiver.class);
        dpm.setLockTaskPackages(adminComponent, new String[]{getPackageName()});

        findViews();

        mMessage.setText("");

        Button login = (Button) findViewById(R.id.button_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Please go to https://me.sumup.com/developers to get your Affiliate Key by entering the application ID of your app. (e.g. com.sumup.sdksampleapp)
                SumUpLogin sumupLogin = SumUpLogin.builder("sup_afk_CEmmyW58Brq8BOivwmxvnWO52jje9WpO").build();
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
            case REQUEST_CODE_WEBVIEW:
                mMessage.setText("Kiosk-Modus beendet.");
                break;
            default:
                break;
        }
    }

    private void findViews() {
        mMessage = (TextView) findViewById(R.id.message);
    }

    public void openWebViewActivity(View view) {
        Intent intent = new Intent(this, WebViewActivity.class);
        startActivityForResult(intent, REQUEST_CODE_WEBVIEW);
    }

    public void openSettingsActivity(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
