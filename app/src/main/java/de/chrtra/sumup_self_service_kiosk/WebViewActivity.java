package de.chrtra.sumup_self_service_kiosk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.sumup.merchant.reader.api.SumUpAPI;
import com.sumup.merchant.reader.models.TransactionInfo;

import java.util.ArrayList;

public class WebViewActivity extends Activity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int PAYMENT_REQUEST_CODE = 2; // Request-Code für die PaymentActivity
    private WebView webView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        webView = findViewById(R.id.webview);

        startLockTask();

        enableImmersiveMode();

        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false); // Ermöglicht automatische Medienwiedergabe
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setWebViewClient(new WebViewClient() {
            // Start intent for custom url scheme
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String starts_with = sharedPreferences.getString("custom_url_schema", "")+"://"+sharedPreferences.getString("custom_url_host", "");
                if (uri.toString().startsWith(starts_with)) {
                    Intent intent = new Intent(view.getContext(), PaymentActivity.class);
                    intent.setData(uri);
                    startActivityForResult(intent, PAYMENT_REQUEST_CODE);
                    return true;
                } else {
                    return false;
                }
            }

            // Get Dialogue on received SSL error
            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this);
                String message = "SSL Certificate error.";
                switch (error.getPrimaryError()) {
                    case SslError.SSL_UNTRUSTED:
                        message = "The certificate authority is not trusted.";
                        break;
                    case SslError.SSL_EXPIRED:
                        message = "The certificate has expired.";
                        break;
                    case SslError.SSL_IDMISMATCH:
                        message = "The certificate Hostname mismatch.";
                        break;
                    case SslError.SSL_NOTYETVALID:
                        message = "The certificate is not yet valid.";
                        break;
                }
                message += " Do you want to continue anyway?";

                builder.setTitle("SSL Certificate Error");
                builder.setMessage(message);
                builder.setPositiveButton("continue", (dialog, which) -> handler.proceed());
                builder.setNegativeButton("cancel", (dialog, which) -> handler.cancel());
                final AlertDialog dialog = builder.create();
                dialog.show();
            }

            // Don't request favicon.ico
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (!request.isForMainFrame() && request.getUrl().getPath().contains("/favicon.ico")) {
                    try {
                        return new WebResourceResponse("image/png", null, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        });

        // Automatically grant permission requests
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });

        loadWebView();
    }

    // Load page from intent or default url
    private void loadWebView() {
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        } else {
            webView.loadUrl(sharedPreferences.getString("start_url", "")); // Standard URL laden, falls keine URL übermittelt wurde
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYMENT_REQUEST_CODE && data != null) {
            Uri.Builder uriBuilder;

            switch (resultCode) {
                case 1:
                    // Success
                    String transactionInfo = data.getStringExtra("paid");

                    uriBuilder = Uri.parse(sharedPreferences.getString("success_url", ""))
                            .buildUpon()
                            .appendQueryParameter("paid", transactionInfo);
                    break;

                default:
                    uriBuilder = Uri.parse(sharedPreferences.getString("error_url", ""))
                            .buildUpon()
                            .appendQueryParameter("code", String.valueOf(resultCode));
                    break;
            }

            // Füge die gespeicherten Query-Parameter hinzu
            ArrayList<String> params = data.getStringArrayListExtra("params");
            if (params != null) {
                for (String parameter : params) {
                    String value = data.getStringExtra(parameter);
                    if (!"amount".equals(parameter)) { // "amount" Parameter nicht hinzufügen
                        uriBuilder.appendQueryParameter(parameter, value);
                    }
                }
            }

            Uri finalUri = uriBuilder.build();
            webView.loadUrl(finalUri.toString());
        }
    }

    @Override
    public void onBackPressed() {
        exitKioskMode();
    }

    @Override
    public void onResume() {
        super.onResume();

        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
    }

    public void exitKioskMode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Beenden des Kiosk-Modus");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = input.getText().toString();
                if (password.equals("1234")) { // Überprüfen Sie das Passwort
                    stopLockTask(); // Kiosk-Modus beenden
                    onDestroy();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Falsches Passwort", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void enableImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            final int flags = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enableImmersiveMode();
        }
    }
}
