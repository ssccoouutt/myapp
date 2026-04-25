package com.example.torch;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.widget.EditText;
import android.widget.Button;
import android.widget.LinearLayout;
import android.view.View;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.graphics.Color;
import android.webkit.WebSettings;

public class MainActivity extends Activity {
    private WebView webView;
    private EditText urlBar;
    private Button goButton;
    private Button backButton;
    private Button forwardButton;
    private Button refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create main layout
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.BLACK);
        
        // Create top bar layout
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setPadding(8, 8, 8, 8);
        topBar.setBackgroundColor(Color.parseColor("#1A1A2E"));
        
        // URL input bar
        urlBar = new EditText(this);
        urlBar.setHint("Enter URL or search...");
        urlBar.setHintTextColor(Color.parseColor("#888888"));
        urlBar.setTextColor(Color.WHITE);
        urlBar.setBackgroundColor(Color.parseColor("#0F3460"));
        urlBar.setPadding(16, 12, 16, 12);
        urlBar.setSingleLine(true);
        urlBar.setImeOptions(EditorInfo.IME_ACTION_GO);
        LinearLayout.LayoutParams urlParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        urlParams.setMargins(0, 0, 8, 0);
        urlBar.setLayoutParams(urlParams);
        
        // Go button
        goButton = new Button(this);
        goButton.setText("GO");
        goButton.setTextColor(Color.WHITE);
        goButton.setBackgroundColor(Color.parseColor("#E94560"));
        goButton.setPadding(24, 12, 24, 12);
        
        topBar.addView(urlBar);
        topBar.addView(goButton);
        
        // Navigation buttons bar
        LinearLayout navBar = new LinearLayout(this);
        navBar.setOrientation(LinearLayout.HORIZONTAL);
        navBar.setPadding(8, 4, 8, 4);
        navBar.setBackgroundColor(Color.parseColor("#16213E"));
        navBar.setGravity(android.view.Gravity.CENTER);
        
        // Back button
        backButton = new Button(this);
        backButton.setText("◀ BACK");
        backButton.setTextColor(Color.WHITE);
        backButton.setBackgroundColor(Color.parseColor("#0F3460"));
        backButton.setPadding(24, 12, 24, 12);
        backButton.setAllCaps(false);
        
        // Forward button
        forwardButton = new Button(this);
        forwardButton.setText("FORWARD ▶");
        forwardButton.setTextColor(Color.WHITE);
        forwardButton.setBackgroundColor(Color.parseColor("#0F3460"));
        forwardButton.setPadding(24, 12, 24, 12);
        forwardButton.setAllCaps(false);
        
        // Refresh button
        refreshButton = new Button(this);
        refreshButton.setText("⟳ REFRESH");
        refreshButton.setTextColor(Color.WHITE);
        refreshButton.setBackgroundColor(Color.parseColor("#0F3460"));
        refreshButton.setPadding(24, 12, 24, 12);
        refreshButton.setAllCaps(false);
        
        LinearLayout.LayoutParams navParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        navParams.setMargins(4, 0, 4, 0);
        backButton.setLayoutParams(navParams);
        forwardButton.setLayoutParams(navParams);
        refreshButton.setLayoutParams(navParams);
        
        navBar.addView(backButton);
        navBar.addView(forwardButton);
        navBar.addView(refreshButton);
        
        // WebView
        webView = new WebView(this);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setDomStorageEnabled(true);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                urlBar.setText(url);
                updateNavigationButtons();
            }
        });
        
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    setTitle("Loading... " + newProgress + "%");
                } else {
                    setTitle("Browser");
                }
            }
        });
        
        // Set button click listeners
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrl();
            }
        });
        
        urlBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    loadUrl();
                    return true;
                }
                return false;
            }
        });
        
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoBack()) {
                    webView.goBack();
                }
            }
        });
        
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoForward()) {
                    webView.goForward();
                }
            }
        });
        
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });
        
        // Add all views to main layout
        mainLayout.addView(topBar);
        mainLayout.addView(navBar);
        mainLayout.addView(webView, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT, 1));
        
        setContentView(mainLayout);
        
        // Load default page
        webView.loadUrl("https://www.google.com");
    }
    
    private void loadUrl() {
        String url = urlBar.getText().toString().trim();
        if (!url.isEmpty()) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                // Check if it's a search query (contains spaces or no dots)
                if (url.contains(" ") || (!url.contains(".") && !url.contains("://"))) {
                    url = "https://www.google.com/search?q=" + url.replace(" ", "+");
                } else {
                    url = "https://" + url;
                }
            }
            webView.loadUrl(url);
        }
    }
    
    private void updateNavigationButtons() {
        backButton.setEnabled(webView.canGoBack());
        forwardButton.setEnabled(webView.canGoForward());
        backButton.setAlpha(webView.canGoBack() ? 1.0f : 0.5f);
        forwardButton.setAlpha(webView.canGoForward() ? 1.0f : 0.5f);
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
