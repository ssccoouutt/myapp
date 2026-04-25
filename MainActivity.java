package com.example.torch;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.widget.EditText;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.view.View;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.graphics.Color;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

public class MainActivity extends Activity {
    private WebView webView;
    private EditText urlBar;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Main layout
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#1A1A2E"));
        
        // Top bar with URL
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setPadding(16, 16, 16, 16);
        topBar.setBackgroundColor(Color.parseColor("#16213E"));
        
        // URL input
        urlBar = new EditText(this);
        urlBar.setHint("Search or enter URL");
        urlBar.setHintTextColor(Color.parseColor("#888888"));
        urlBar.setTextColor(Color.WHITE);
        urlBar.setBackgroundColor(Color.parseColor("#0F3460"));
        urlBar.setPadding(20, 14, 20, 14);
        urlBar.setSingleLine(true);
        urlBar.setImeOptions(EditorInfo.IME_ACTION_GO);
        LinearLayout.LayoutParams urlParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        urlParams.setMargins(0, 0, 12, 0);
        urlBar.setLayoutParams(urlParams);
        
        // Go button
        Button goButton = new Button(this);
        goButton.setText("GO");
        goButton.setTextColor(Color.WHITE);
        goButton.setBackgroundColor(Color.parseColor("#E94560"));
        goButton.setPadding(32, 14, 32, 14);
        
        topBar.addView(urlBar);
        topBar.addView(goButton);
        
        // Progress bar
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E94560")));
        progressBar.setMax(100);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 6));
        
        // Navigation buttons
        LinearLayout navBar = new LinearLayout(this);
        navBar.setOrientation(LinearLayout.HORIZONTAL);
        navBar.setPadding(16, 8, 16, 8);
        navBar.setBackgroundColor(Color.parseColor("#0F3460"));
        navBar.setGravity(android.view.Gravity.CENTER);
        
        Button backBtn = createNavButton("◀ BACK", navBar);
        Button forwardBtn = createNavButton("FORWARD ▶", navBar);
        Button refreshBtn = createNavButton("⟳ REFRESH", navBar);
        
        navBar.addView(backBtn);
        navBar.addView(forwardBtn);
        navBar.addView(refreshBtn);
        
        // WebView
        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                urlBar.setText(url);
                updateNavButtons(backBtn, forwardBtn);
                progressBar.setVisibility(View.GONE);
            }
        });
        
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);
                if (newProgress == 100) progressBar.setVisibility(View.GONE);
            }
        });
        
        // Button actions
        goButton.setOnClickListener(v -> {
            String url = urlBar.getText().toString().trim();
            if (!url.isEmpty()) {
                if (!url.startsWith("http")) url = "https://" + url;
                webView.loadUrl(url);
                hideKeyboard();
            }
        });
        
        urlBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                goButton.performClick();
                return true;
            }
            return false;
        });
        
        backBtn.setOnClickListener(v -> { if (webView.canGoBack()) webView.goBack(); });
        forwardBtn.setOnClickListener(v -> { if (webView.canGoForward()) webView.goForward(); });
        refreshBtn.setOnClickListener(v -> webView.reload());
        
        // Add all to layout
        mainLayout.addView(topBar);
        mainLayout.addView(progressBar);
        mainLayout.addView(navBar);
        mainLayout.addView(webView, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT, 1));
        
        setContentView(mainLayout);
        
        // Load default page
        webView.loadUrl("https://www.google.com");
    }
    
    private Button createNavButton(String text, LinearLayout parent) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundColor(Color.parseColor("#16213E"));
        btn.setPadding(24, 12, 24, 12);
        btn.setAllCaps(false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 
            LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        params.setMargins(4, 0, 4, 0);
        btn.setLayoutParams(params);
        return btn;
    }
    
    private void updateNavButtons(Button back, Button forward) {
        back.setEnabled(webView.canGoBack());
        forward.setEnabled(webView.canGoForward());
        back.setAlpha(webView.canGoBack() ? 1.0f : 0.4f);
        forward.setAlpha(webView.canGoForward() ? 1.0f : 0.4f);
    }
    
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
