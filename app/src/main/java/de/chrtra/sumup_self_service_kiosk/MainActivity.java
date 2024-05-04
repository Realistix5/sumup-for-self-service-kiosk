package de.chrtra.sumup_self_service_kiosk;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.UUID;

import com.sumup.merchant.reader.api.SumUpAPI;
import com.sumup.merchant.reader.api.SumUpLogin;
import com.sumup.merchant.reader.api.SumUpPayment;
import com.sumup.merchant.reader.models.TransactionInfo;

public class MainActivity extends Activity {

    private static final int REQUEST_CODE_LOGIN = 1;
    private static final int REQUEST_CODE_PAYMENT = 2;
    private static final int REQUEST_CODE_CARD_READER_PAGE = 4;

    private TextView mResultCode;
    private TextView mResultMessage;
    private TextView mTxCode;
    private TextView mReceiptSent;
    private TextView mTxInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();

        // Deep Link Handling
        handleDeepLink(getIntent());

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
                    mResultCode.setText("Result code: " + SumUpAPI.Response.ResultCode.ERROR_NOT_LOGGED_IN);
                    mResultMessage.setText("Message: Not logged in");
                } else {
                    mResultCode.setText("Result code: " + SumUpAPI.Response.ResultCode.SUCCESSFUL);
                    mResultMessage.setText(
                            String.format("Currency: %s, Merchant Code: %s", SumUpAPI.getCurrentMerchant().getCurrency().getIsoCode(),
                                    SumUpAPI.getCurrentMerchant().getMerchantCode()));
                }
            }
        });

        Button btnCharge = (Button) findViewById(R.id.button_charge);
        btnCharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SumUpPayment payment = SumUpPayment.builder()
                        // mandatory parameters
                        .total(new BigDecimal("1.12")) // minimum 1.00
                        .currency(SumUpPayment.Currency.EUR)
                        // optional: add details
                        .title("Taxi Ride")
                        .receiptEmail("customer@mail.com")
                        .receiptSMS("+3531234567890")
                        // optional: Add metadata
                        .addAdditionalInfo("AccountId", "taxi0334")
                        .addAdditionalInfo("From", "Paris")
                        .addAdditionalInfo("To", "Berlin")
                        // optional: foreign transaction ID, must be unique!
                        .foreignTransactionId(UUID.randomUUID().toString()) // can not exceed 128 chars
                        .build();

                SumUpAPI.checkout(MainActivity.this, payment, REQUEST_CODE_PAYMENT);
            }
        });

        Button cardReaderPage = (Button) findViewById(R.id.button_card_reader_page);
        cardReaderPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SumUpAPI.openCardReaderPage(MainActivity.this, REQUEST_CODE_CARD_READER_PAGE);
            }
        });

        Button prepareCardTerminal = (Button) findViewById(R.id.button_prepare_card_terminal);
        prepareCardTerminal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SumUpAPI.prepareForCheckout();
            }
        });

        Button btnLogout = (Button) findViewById(R.id.button_logout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SumUpAPI.logout();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                // Anzeigen von "Warte auf Zahlung"
                String amount = data.getQueryParameter("amount");


                SumUpPayment payment = SumUpPayment.builder()
                        // mandatory parameters
                        .total(new BigDecimal(amount)) // minimum 1.00
                        .currency(SumUpPayment.Currency.EUR)
                        // optional: to be used only if the card reader supports the feature, what can be checked with `SumUpApi.isTipOnCardReaderAvailable()`
                        //.tipOnCardReader()
                        // optional: include a tip amount in addition to the total, ignored if `tipOnCardReader()` is present
                        //.tip(new BigDecimal("0.10"))
                        // optional: add details
                        .title("Zahlung an den GSV Gundernhausen e.V.")
                        //.receiptEmail("customer@mail.com")
                        //.receiptSMS("+3531234567890")
                        // optional: Add metadata
                        //.addAdditionalInfo("AccountId", "taxi0334")
                        //.addAdditionalInfo("From", "Paris")
                        //.addAdditionalInfo("To", "Berlin")
                        // optional: foreign transaction ID, must be unique!
                        //.foreignTransactionId(UUID.randomUUID().toString())  // can not exceed 128 chars
                        // optional: skip the success screen
                        .skipSuccessScreen()
                        // optional: skip the failed screen
                        .skipFailedScreen()
                        .build();

                SumUpAPI.checkout(MainActivity.this, payment, 2);
            }
        }
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        resetViews();

        switch (requestCode) {
            case REQUEST_CODE_LOGIN:
            case REQUEST_CODE_CARD_READER_PAGE:
                setResultCodeAndMessage(data);
                break;

            case REQUEST_CODE_PAYMENT:
                if (data != null) {
                    Bundle extra = data.getExtras();

                    mResultCode.setText("Result code: " + extra.getInt(SumUpAPI.Response.RESULT_CODE));
                    mResultMessage.setText("Message: " + extra.getString(SumUpAPI.Response.MESSAGE));

                    String txCode = extra.getString(SumUpAPI.Response.TX_CODE);
                    mTxCode.setText(txCode == null ? "" : "Transaction Code: " + txCode);

                    boolean receiptSent = extra.getBoolean(SumUpAPI.Response.RECEIPT_SENT);
                    mReceiptSent.setText("Receipt sent: " + receiptSent);

                    TransactionInfo transactionInfo = extra.getParcelable(SumUpAPI.Response.TX_INFO);
                    mTxInfo.setText(transactionInfo == null ? "" : "Transaction Info : " + transactionInfo);

                    String url = "";
                    if (transactionInfo.mStatus.toString().equals("SUCCESSFUL")) {
                        // if transaction successful, redirect to webpage
                        url = "http://192.168.178.79:8000/?transaction_code="+transactionInfo.mTransactionCode.toString();
                    } else {
                        url = "http://192.168.178.79:8000/?transaction_code=failed";
                    }

                    // Erzeuge ein Intent mit der Aktion ACTION_VIEW
                    Intent intent = new Intent(Intent.ACTION_VIEW);

                    // Setze die Daten des Intents auf die URL
                    intent.setData(Uri.parse(url));

                    // Starte die Aktivität
                    startActivity(intent);
                }
                break;

            default:
                break;
        }
    }

    private void setResultCodeAndMessage(Intent data) {
        if (data != null) {
            Bundle extra = data.getExtras();
            mResultCode.setText("Result code: " + extra.getInt(SumUpAPI.Response.RESULT_CODE));
            mResultMessage.setText("Message: " + extra.getString(SumUpAPI.Response.MESSAGE));
        }
    }

    private void resetViews() {
        mResultCode.setText("");
        mResultMessage.setText("");
        mTxCode.setText("");
        mReceiptSent.setText("");
        mTxInfo.setText("");
    }

    private void findViews() {
        mResultCode = (TextView) findViewById(R.id.result);
        mResultMessage = (TextView) findViewById(R.id.result_msg);
        mTxCode = (TextView) findViewById(R.id.tx_code);
        mReceiptSent = (TextView) findViewById(R.id.receipt_sent);
        mTxInfo = (TextView) findViewById(R.id.tx_info);
    }

    public void openWebViewActivity(View view) {
        Intent intent = new Intent(this, WebViewActivity.class);
        startActivity(intent);
    }
}