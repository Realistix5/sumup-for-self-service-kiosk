package de.chrtra.sumup_self_service_kiosk;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import java.math.BigDecimal;
import com.sumup.merchant.reader.api.SumUpAPI;
import com.sumup.merchant.reader.api.SumUpPayment;



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

    // Add handling of result

}
