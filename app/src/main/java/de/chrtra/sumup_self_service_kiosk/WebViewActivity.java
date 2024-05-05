package de.chrtra.sumup_self_service_kiosk;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.app.Activity;

public class WebViewActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        webView = findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("http://192.168.178.79:8000/");
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (uri.toString().startsWith("pay://sumup")) {
                    // Hier wird deine spezifische Logik ausgeführt, um zur gewünschten Activity zu navigieren
                    Intent intent = new Intent(view.getContext(), PaymentActivity.class);
                    intent.setData(uri);
                    view.getContext().startActivity(intent);
                    return true; // Gib true zurück, um zu signalisieren, dass der Link behandelt wurde
                } else {
                    // Wenn es sich nicht um dein benutzerdefiniertes Schema handelt, lasse die WebView es behandeln
                    return false;
                }
            }
        });

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        } else {
            webView.loadUrl("http://192.168.178.79:8000/");  // Standard URL laden, falls keine URL übermittelt wurde
        }
    }
}

