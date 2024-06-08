package de.chrtra.sumup_self_service_kiosk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import com.sumup.merchant.reader.api.SumUpAPI;
import com.sumup.merchant.reader.api.SumUpPayment;
import com.sumup.merchant.reader.models.TransactionInfo;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PaymentActivity extends Activity {

    private Map<String, String> queryParams;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);

        queryParams = new HashMap<>();

        Uri uri = getIntent().getData();
        if (uri != null) {
            for (String parameter : uri.getQueryParameterNames()) {
                queryParams.put(parameter, uri.getQueryParameter(parameter));
            }

            if (queryParams.containsKey("total")) {
                // Initialize payment builder
                SumUpPayment.Builder paymentBuilder = SumUpPayment.builder();

                // Add total
                paymentBuilder.total(new BigDecimal(queryParams.get("total")));

                // Add currency
                if (queryParams.containsKey("currency")) {
                    paymentBuilder.currency(SumUpPayment.Currency.valueOf(queryParams.get("currency")));
                } else {
                    try {
                        paymentBuilder.currency(SumUpPayment.Currency.valueOf(sharedPreferences.getString("currency", "EUR")));
                    } catch (IllegalArgumentException e) {
                        paymentBuilder.currency(SumUpPayment.Currency.EUR);
                    }
                }

                // Add title
                if (queryParams.containsKey("title")) {
                    paymentBuilder.title(queryParams.get("title"));
                } else {
                    paymentBuilder.title(sharedPreferences.getString("payment_title", ""));
                }

                // Add tip
                if (queryParams.containsKey("tip")) {
                    paymentBuilder.tip(new BigDecimal(queryParams.get("tip")));
                }

                // Add receiptEmail
                if (queryParams.containsKey("receipt_email")) {
                    paymentBuilder.receiptEmail(queryParams.get("receipt_email"));
                }

                // Add receiptSMS
                if (queryParams.containsKey("receipt_sms")) {
                    paymentBuilder.receiptEmail(queryParams.get("receipt_sms"));
                }

                // Add foreignTransactionId
                if (queryParams.containsKey("foreign_transaction_id")) {
                    paymentBuilder.foreignTransactionId(Objects.requireNonNull(queryParams.get("foreign_transaction_id")));
                }

                // Add tip
                if (queryParams.containsKey("tip")) {
                    paymentBuilder.tip(new BigDecimal(queryParams.get("tip")));
                }

                // Add booleans
                // Tip on card reader
                if (queryParams.containsKey("tip_on_card_reader")) {
                    if (queryParams.get("tip_on_card_reader").equals("1")){
                        paymentBuilder.tipOnCardReader();
                    }
                } else {
                    if (sharedPreferences.getBoolean("tip_on_card_reader", false)) {
                        paymentBuilder.tipOnCardReader();
                    }
                }
                // Skip failed screen
                if (queryParams.containsKey("skip_failed_screen")) {
                    if (queryParams.get("skip_failed_screen").equals("1")){
                        paymentBuilder.skipFailedScreen();
                    }
                } else {
                    if (sharedPreferences.getBoolean("skip_failed_screen", false)) {
                        paymentBuilder.skipFailedScreen();
                    }
                }
                // Skip success screen
                if (queryParams.containsKey("skip_success_screen")) {
                    if (queryParams.get("skip_success_screen").equals("1")){
                        paymentBuilder.skipSuccessScreen();
                    }
                } else {
                    if (sharedPreferences.getBoolean("skip_success_screen", false)) {
                        paymentBuilder.skipSuccessScreen();
                    }
                }

                // Build payment
                SumUpPayment payment = paymentBuilder.build();

                // Start checkout
                SumUpAPI.checkout(this, payment, 2); // '2' ist der Request-Code für die Activity-Result-Rückgabe
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2 && data != null) {
            Intent resultIntent = new Intent();
            resultIntent.putStringArrayListExtra("params", new ArrayList<>(queryParams.keySet()));

            switch (resultCode) {
                case 1:
                    // Success
                    TransactionInfo transactionInfo = data.getParcelableExtra(SumUpAPI.Response.TX_INFO);
                    if (transactionInfo != null) {
                        resultIntent.putExtra("paid", transactionInfo.getTransactionCode());
                    }
                    break;

                case 2:
                    // Failed
                    // Keine zusätzlichen Daten erforderlich
                    break;

                default:
                    resultIntent.putExtra("code", resultCode);
                    break;
            }

            // Füge die gespeicherten Query-Parameter hinzu
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                resultIntent.putExtra(entry.getKey(), entry.getValue());
            }

            setResult(resultCode, resultIntent);
            finish();
        }
    }
}
