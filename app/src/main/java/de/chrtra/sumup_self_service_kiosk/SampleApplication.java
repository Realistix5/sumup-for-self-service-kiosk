package de.chrtra.sumup_self_service_kiosk;

import android.app.Application;
import com.sumup.merchant.reader.api.SumUpState;

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SumUpState.init(this);
    }
}
