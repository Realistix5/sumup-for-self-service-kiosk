package de.chrtra.sumup_self_service_kiosk;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import java.math.BigDecimal;
import com.sumup.merchant.reader.api.SumUpAPI;
import com.sumup.merchant.reader.api.SumUpPayment;
import com.sumup.merchant.reader.models.TransactionInfo;


public class PaymentActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uri = getIntent().getData();
        if (uri != null && uri.getQueryParameter("amount") != null) {
            String amount = uri.getQueryParameter("amount");

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 2 && data != null) {
            Intent intent = new Intent(this, WebViewActivity.class);

            switch (resultCode) {
                case 1:
                    // Success
                    Bundle extra = data.getExtras();
                    TransactionInfo transactionInfo = extra.getParcelable(SumUpAPI.Response.TX_INFO);
                    String transactionCode = transactionInfo.mTransactionCode.toString();
                    intent.putExtra("url", "http://192.168.178.79:8000/process_payment/" + transactionCode);
                    break;

                case 2:
                    // Failed
                    intent.putExtra("url", "http://192.168.178.79:8000/payment_failed/");
                    break;

                case 8:
                    // Not logged in
                    intent.putExtra("url", "http://192.168.178.79:8000/payment_problem/");
                    break;

                default:
                    intent.putExtra("url", "http://192.168.178.79:8000/payment_problem/"+resultCode);
                    break;

            }

            startActivity(intent);
        }

    }
}

