package de.chrtra.sumup_self_service_kiosk;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import com.sumup.merchant.reader.api.SumUpAPI;
import com.sumup.merchant.reader.api.SumUpPayment;
import com.sumup.merchant.reader.models.TransactionInfo;

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
            Intent intent = new Intent(this, WebViewActivity.class);
            Bundle extra = data.getExtras();
            Uri.Builder uriBuilder = new Uri.Builder();

            switch (resultCode) {
                case 1:
                    // Success
                    TransactionInfo transactionInfo = extra.getParcelable(SumUpAPI.Response.TX_INFO);
                    String transactionCode = transactionInfo.mTransactionCode;
                    uriBuilder = Uri.parse("https://192.168.178.79:8000/process_payment/")
                            .buildUpon()
                            .appendQueryParameter("paid", transactionCode);
                    break;

                case 2:
                    // Failed
                    uriBuilder = Uri.parse("https://192.168.178.79:8000/payment_failed/").buildUpon();
                    break;

                default:
                    uriBuilder = Uri.parse("https://192.168.178.79:8000/payment_problem/")
                            .buildUpon()
                            .appendQueryParameter("code", String.valueOf(resultCode));
                    break;
            }

            // Füge die gespeicherten Query-Parameter hinzu
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (!entry.getKey().equals("amount")) { // "amount" Parameter nicht hinzufügen
                    uriBuilder.appendQueryParameter(entry.getKey(), entry.getValue());
                }
            }

            Uri finalUri = uriBuilder.build();
            intent.putExtra("url", finalUri.toString());

            startActivity(intent);
        }
    }
}
