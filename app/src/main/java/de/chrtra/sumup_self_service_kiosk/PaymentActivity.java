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

            if (queryParams.containsKey("amount")) {
                String amount = queryParams.get("amount");
                String title = sharedPreferences.getString("payment_title", "");

                SumUpPayment.Builder paymentBuilder = SumUpPayment.builder()
                        .total(new BigDecimal(amount)) // Der minimale Betrag ist 1.00
                        .currency(SumUpPayment.Currency.EUR)
                        .title(title);

                SumUpPayment.Currency currency;
                try {
                    currency = SumUpPayment.Currency.valueOf(sharedPreferences.getString("currency", "EUR"));
                } catch (IllegalArgumentException e) {
                    currency = SumUpPayment.Currency.valueOf( "EUR");
                }
                paymentBuilder.currency(currency);

                if (sharedPreferences.getBoolean("tip_on_card_reader", false)) {
                    paymentBuilder.tipOnCardReader();
                }
                if (sharedPreferences.getBoolean("skip_failed_screen", false)) {
                    paymentBuilder.skipFailedScreen();
                }
                if (sharedPreferences.getBoolean("skip_success_screen", false)) {
                    paymentBuilder.skipSuccessScreen();
                }

                // Handle default additional info
                Map<String, String> additionalInfo = convertStringToMap(sharedPreferences.getString("additional_info", ""));
                for (Map.Entry<String, String> entry : additionalInfo.entrySet()) {
                    paymentBuilder.addAdditionalInfo(entry.getKey(), entry.getValue());
                }

                SumUpPayment payment = paymentBuilder.build();

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
    public static Map<String, String> convertStringToMap(String input) {
        Map<String, String> keyValuePairs = new HashMap<>();

        // Split the input string by newline characters
        String[] lines = input.split("\n");

        for (String line : lines) {
            // Split each line by the first occurrence of ": "
            String[] parts = line.split(": ", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();
                keyValuePairs.put(key, value);
            }
        }

        return keyValuePairs;
    }
}
