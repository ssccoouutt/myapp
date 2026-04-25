package com.example.torch;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.PermissionRequest;
import android.widget.EditText;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.view.View;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.graphics.Color;
import android.os.Build;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

public class MainActivity extends Activity {
    private WebView webView;
    private EditText urlBar;
    private Button goButton;
    private Button backButton;
    private Button forwardButton;
    private Button refreshButton;
    private ProgressBar progressBar;
    private LinearLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Main container
        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#1A1A2E"));
        
        // Top bar with URL
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setPadding(16, 16, 16, 16);
        topBar.setBackgroundColor(Color.parseColor("#16213E"));
        topBar.setElevation(8);
        
        // URL input
        urlBar = new EditText(this);
        urlBar.setHint("Search or enter website");
        urlBar.setHintTextColor(Color.parseColor("#888888"));
        urlBar.setTextColor(Color.WHITE);
        urlBar.setBackgroundColor(Color.parseColor("#0F3460"));
        urlBar.setPadding(20, 14, 20, 14);
        urlBar.setSingleLine(true);
        urlBar.setImeOptions(EditorInfo.IME_ACTION_GO);
        urlBar.setTextSize(14);
        LinearLayout.LayoutParams urlParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        urlParams.setMargins(0, 0, 12, 0);
        urlBar.setLayoutParams(urlParams);
        
        // Go button
        goButton = new Button(this);
        goButton.setText("GO");
        goButton.setTextColor(Color.WHITE);
        goButton.setBackgroundColor(Color.parseColor("#E94560"));
        goButton.setPadding(32, 14, 32, 14);
        goButton.setAllCaps(false);
        goButton.setTextSize(14);
        
        topBar.addView(urlBar);
        topBar.addView(goButton);
        
        // Progress bar
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E94560")));
        progressBar.setMax(100);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 6));
        
        // Navigation bar
        LinearLayout navBar = new LinearLayout(this);
        navBar.setOrientation(LinearLayout.HORIZONTAL);
        navBar.setPadding(16, 8, 16, 8);
        navBar.setBackgroundColor(Color.parseColor("#0F3460"));
        navBar.setGravity(android.view.Gravity.CENTER);
        
        // Back button
        backButton = createNavButton("◀");
        // Forward button
        forwardButton = createNavButton("▶");
        // Refresh button
        refreshButton = createNavButton("⟳");
        // Home button
        Button homeButton = createNavButton("🏠");
        
        LinearLayout.LayoutParams navParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        navParams.setMargins(4, 0, 4, 0);
        backButton.setLayoutParams(navParams);
        forwardButton.setLayoutParams(navParams);
        refreshButton.setLayoutParams(navParams);
        homeButton.setLayoutParams(navParams);
        
        navBar.addView(backButton);
        navBar.addView(forwardButton);
        navBar.addView(refreshButton);
        navBar.addView(homeButton);
        
        // WebView
        webView = new WebView(this);
        configureWebView();
        
        // Add all to main layout
        mainLayout.addView(topBar);
        mainLayout.addView(progressBar);
        mainLayout.addView(navBar);
        mainLayout.addView(webView, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT, 1));
        
        setContentView(mainLayout);
        
        // Set listeners
        setupListeners();
        
        // Load default page
        webView.loadUrl("https://www.google.com");
    }
    
    private Button createNavButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundColor(Color.parseColor("#16213E"));
        btn.setPadding(24, 12, 24, 12);
        btn.setAllCaps(false);
        btn.setTextSize(16);
        return btn;
    }
    
    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        
        // Enable remote debugging
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        
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
                progressBar.setVisibility(View.GONE);
            }
            
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(MainActivity.this, "Error: " + description, Toast.LENGTH_SHORT).show();
            }
        });
        
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }
            
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });
    }
    
    private void setupListeners() {
        goButton.setOnClickListener(v -> {
            loadUrl();
            hideKeyboard();
        });
        
        urlBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                loadUrl();
                hideKeyboard();
                return true;
            }
            return false;
        });
        
        backButton.setOnClickListener(v -> {
            if (webView.canGoBack()) {
                webView.goBack();
            }
        });
        
        forwardButton.setOnClickListener(v -> {
            if (webView.canGoForward()) {
                webView.goForward();
            }
        });
        
        refreshButton.setOnClickListener(v -> webView.reload());
        
        findViewById(goButton.getId()).setOnLongClickListener(v -> {
            urlBar.setText("");
            return true;
        });
    }
    
    private void loadUrl() {
        String url = urlBar.getText().toString().trim();
        if (url.isEmpty()) return;
        
        // Check if it's a search query
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            if (url.contains(" ") || (!url.contains(".") && !url.contains("://"))) {
                url = "https://www.google.com/search?q=" + url.replace(" ", "+");
            } else {
                url = "https://" + url;
            }
        }
        
        webView.loadUrl(url);
    }
    
    private void updateNavigationButtons() {
        backButton.setEnabled(webView.canGoBack());
        forwardButton.setEnabled(webView.canGoForward());
        backButton.setAlpha(webView.canGoBack() ? 1.0f : 0.4f);
        forwardButton.setAlpha(webView.canGoForward() ? 1.0f : 0.4f);
    }
    
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }
}
