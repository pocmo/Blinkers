package com.androidzeitgeist.blinkers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {
    private View progressView;
    private WebView webView;
    private Handler handler;

    public class CallbackInterface {
        @JavascriptInterface
        public void setArticleContent(final String title, final String content) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    setTitle(title);

                    webView.loadData(content, "text/html", "UTF-8");
                    webView.setVisibility(View.VISIBLE);
                    progressView.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();

        setContentView(R.layout.activity_main);
        progressView = findViewById(R.id.processing);

        setupWebView();

        final Intent intent = getIntent();
        final Uri uri = intent.getData();
        if (uri != null) {
            processUri(uri);
        }

        final String text = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (text != null) {
            processUri(Uri.parse(text));
        }
    }

    private void setupWebView() {
        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new CallbackInterface(), "Blinkers");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.w("SKDBG", "Finished: " + url);

                if (url.startsWith("http")) {
                    injectReadabilityCode();
                }
            }
        });
    }

    private void processUri(Uri uri) {
        webView.loadUrl(uri.toString());
    }

    private void injectReadabilityCode() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                String code = "javascript:var script = document.createElement('script');script.type = 'text/javascript';script.src = 'http://people.mozilla.org/~skaspari/Readability.js';document.head.appendChild(script);";
                webView.loadUrl(code);

            }
        });

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String processor = "javascript:var uri = {spec: location.href, host: location.host, prePath: location.protocol + \"//\" + location.host, scheme: location.protocol.substr(0, location.protocol.indexOf(\":\")), pathBase: location.protocol + \"//\" + location.host + location.pathname.substr(0, location.pathname.lastIndexOf(\"/\") + 1)};var article = new Readability(uri, document).parse();Blinkers.setArticleContent(article.title, article.content);";
                webView.loadUrl(processor);
            }
        }, 5000);

    }
}
