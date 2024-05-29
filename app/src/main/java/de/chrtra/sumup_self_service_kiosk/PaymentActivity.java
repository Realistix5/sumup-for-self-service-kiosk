package de.chrtra.sumup_self_service_kiosk;

import android.app.Activity;
import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        queryParams = new HashMap<>();

        Uri uri = getIntent().getData();
        if (uri != null) {
            for (String parameter : uri.getQueryParameterNames()) {
                queryParams.put(parameter, uri.getQueryParameter(parameter));
            }

            if (queryParams.containsKey("amount")) {
                String amount = queryParams.get("amount");

                SumUpPayment payment = SumUpPayment.builder()
                        .total(new BigDecimal(amount)) // Der minimale Betrag ist 1.00
                        .currency(SumUpPayment.Currency.EUR)
                        .title("Zahlung an den GSV Gundernhausen e.V.")
                        .skipSuccessScreen()
                        .skipFailedScreen()
                        .build();

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
