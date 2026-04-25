package com.example.torch;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.os.Vibrator;

public class MainActivity extends Activity {
    private boolean torchOn = false;
    private CameraManager cameraManager;
    private String cameraId;
    private View torchButton;
    private TextView statusText;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
        // Create main layout
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(android.view.Gravity.CENTER);
        root.setPadding(32, 32, 32, 32);
        
        // Gradient background
        GradientDrawable gradient = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[] {Color.parseColor("#1A1A2E"), Color.parseColor("#16213E"), Color.parseColor("#0F3460")}
        );
        root.setBackground(gradient);
        
        // Title
        TextView title = new TextView(this);
        title.setText("🔦 TORCH PRO");
        title.setTextSize(28);
        title.setTextColor(Color.WHITE);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(android.view.Gravity.CENTER);
        title.setPadding(0, 0, 0, 8);
        
        // Subtitle
        TextView subtitle = new TextView(this);
        subtitle.setText("Premium Flashlight");
        subtitle.setTextSize(14);
        subtitle.setTextColor(Color.parseColor("#B3FFFFFF"));
        subtitle.setGravity(android.view.Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 64);
        
        // Torch button container
        android.widget.FrameLayout buttonContainer = new android.widget.FrameLayout(this);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(260, 260);
        containerParams.gravity = android.view.Gravity.CENTER;
        containerParams.setMargins(0, 0, 0, 32);
        buttonContainer.setLayoutParams(containerParams);
        
        // Torch button
        torchButton = new View(this);
        android.widget.FrameLayout.LayoutParams buttonParams = new android.widget.FrameLayout.LayoutParams(260, 260);
        buttonParams.gravity = android.view.Gravity.CENTER;
        torchButton.setLayoutParams(buttonParams);
        
        GradientDrawable buttonGrad = new GradientDrawable();
        buttonGrad.setShape(GradientDrawable.OVAL);
        buttonGrad.setColor(Color.parseColor("#E94560"));
        buttonGrad.setStroke(3, Color.parseColor("#FF03DAC5"));
        torchButton.setBackground(buttonGrad);
        torchButton.setClickable(true);
        torchButton.setElevation(16);
        
        // Torch icon (text)
        TextView torchIcon = new TextView(this);
        torchIcon.setText("🔦");
        torchIcon.setTextSize(64);
        torchIcon.setTextColor(Color.WHITE);
        android.widget.FrameLayout.LayoutParams iconParams = new android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT);
        iconParams.gravity = android.view.Gravity.CENTER;
        torchIcon.setLayoutParams(iconParams);
        
        buttonContainer.addView(torchButton);
        buttonContainer.addView(torchIcon);
        
        // Status text
        statusText = new TextView(this);
        statusText.setText("OFF");
        statusText.setTextSize(20);
        statusText.setTextColor(Color.parseColor("#9E9E9E"));
        statusText.setTypeface(null, android.graphics.Typeface.BOLD);
        statusText.setGravity(android.view.Gravity.CENTER);
        statusText.setPadding(0, 16, 0, 16);
        
        // Instruction
        TextView instruction = new TextView(this);
        instruction.setText("Tap to toggle flashlight");
        instruction.setTextSize(12);
        instruction.setTextColor(Color.parseColor("#88666666"));
        instruction.setGravity(android.view.Gravity.CENTER);
        
        // Add all views
        root.addView(title);
        root.addView(subtitle);
        root.addView(buttonContainer);
        root.addView(statusText);
        root.addView(instruction);
        
        setContentView(root);
        
        // Setup camera
        try {
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (Exception e) {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_LONG).show();
        }
        
        // Button click
        torchButton.setOnClickListener(v -> {
            if (vibrator != null) vibrator.vibrate(30);
            animateButton();
            toggleTorch();
            
            // Change icon
            if (torchOn) {
                torchIcon.setText("🔦");
            } else {
                torchIcon.setText("💡");
            }
        });
    }
    
    private void animateButton() {
        Animation anim = new ScaleAnimation(1f, 0.85f, 1f, 0.85f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(100);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(1);
        torchButton.startAnimation(anim);
    }
    
    private void toggleTorch() {
        try {
            torchOn = !torchOn;
            cameraManager.setTorchMode(cameraId, torchOn);
            
            GradientDrawable btnGrad = (GradientDrawable) torchButton.getBackground();
            
            if (torchOn) {
                btnGrad.setColor(Color.parseColor("#FFEB3B"));
                btnGrad.setStroke(3, Color.WHITE);
                statusText.setText("ON ⚡");
                statusText.setTextColor(Color.parseColor("#FFEB3B"));
                Toast.makeText(this, "✨ Torch ON ✨", Toast.LENGTH_SHORT).show();
            } else {
                btnGrad.setColor(Color.parseColor("#E94560"));
                btnGrad.setStroke(3, Color.parseColor("#FF03DAC5"));
                statusText.setText("OFF");
                statusText.setTextColor(Color.parseColor("#9E9E9E"));
                Toast.makeText(this, "🔦 Torch OFF", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (torchOn && cameraManager != null) {
            try {
                cameraManager.setTorchMode(cameraId, false);
            } catch (Exception e) {}
        }
    }
}