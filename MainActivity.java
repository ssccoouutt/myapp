package com.example.torch;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.AnimationDrawable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import java.util.Random;

public class MainActivity extends Activity {
    private boolean torchOn = false;
    private CameraManager cameraManager;
    private String cameraId;
    private View torchButton;
    private TextView statusText;
    private Vibrator vibrator;
    private TextView torchIcon;
    private LinearLayout rootLayout;
    private TextView intensityText;
    private int brightness = 100;
    private Handler handler = new Handler();
    private Random random = new Random();
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    
    // Dynamic colors
    private int[] colors = {
        Color.parseColor("#FF6B6B"), Color.parseColor("#4ECDC4"), 
        Color.parseColor("#45B7D1"), Color.parseColor("#96CEB4"),
        Color.parseColor("#FFEAA7"), Color.parseColor("#DDA0DD"),
        Color.parseColor("#98D8C8"), Color.parseColor("#F7DC6F")
    };
    private int currentColorIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
        // Create main layout with animated gradient background
        rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setGravity(android.view.Gravity.CENTER);
        rootLayout.setPadding(32, 32, 32, 32);
        rootLayout.setBackgroundColor(Color.parseColor("#1A1A2E"));
        
        // Animated gradient background
        AnimationDrawable animatedGradient = new AnimationDrawable();
        animatedGradient.addFrame(createGradientDrawable(new int[]{Color.parseColor("#1A1A2E"), Color.parseColor("#16213E")}), 3000);
        animatedGradient.addFrame(createGradientDrawable(new int[]{Color.parseColor("#16213E"), Color.parseColor("#0F3460")}), 3000);
        animatedGradient.addFrame(createGradientDrawable(new int[]{Color.parseColor("#0F3460"), Color.parseColor("#1A1A2E")}), 3000);
        animatedGradient.setOneShot(false);
        rootLayout.setBackground(animatedGradient);
        animatedGradient.start();
        
        // App Title with animation
        TextView title = new TextView(this);
        title.setText("✨ TORCH PRO MAX ✨");
        title.setTextSize(32);
        title.setTextColor(Color.WHITE);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(android.view.Gravity.CENTER);
        title.setPadding(0, 0, 0, 8);
        
        // Subtitle with glow effect
        TextView subtitle = new TextView(this);
        subtitle.setText("Dynamic Flashlight Experience");
        subtitle.setTextSize(14);
        subtitle.setTextColor(Color.parseColor("#CCFFFFFF"));
        subtitle.setGravity(android.view.Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 48);
        
        // Main torch button container with shadow
        android.widget.FrameLayout buttonContainer = new android.widget.FrameLayout(this);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(300, 300);
        containerParams.gravity = android.view.Gravity.CENTER;
        containerParams.setMargins(0, 0, 0, 32);
        buttonContainer.setLayoutParams(containerParams);
        
        // Multi-layer button for 3D effect
        // Layer 1: Outer glow
        View outerGlow = new View(this);
        android.widget.FrameLayout.LayoutParams glowParams = new android.widget.FrameLayout.LayoutParams(320, 320);
        glowParams.gravity = android.view.Gravity.CENTER;
        outerGlow.setLayoutParams(glowParams);
        GradientDrawable glowGrad = new GradientDrawable();
        glowGrad.setShape(GradientDrawable.OVAL);
        glowGrad.setColor(Color.parseColor("#20E94560"));
        outerGlow.setBackground(glowGrad);
        
        // Layer 2: Main button
        torchButton = new View(this);
        android.widget.FrameLayout.LayoutParams buttonParams = new android.widget.FrameLayout.LayoutParams(280, 280);
        buttonParams.gravity = android.view.Gravity.CENTER;
        torchButton.setLayoutParams(buttonParams);
        
        GradientDrawable buttonGrad = new GradientDrawable();
        buttonGrad.setShape(GradientDrawable.OVAL);
        buttonGrad.setColor(Color.parseColor("#E94560"));
        buttonGrad.setStroke(4, Color.parseColor("#FF03DAC5"));
        torchButton.setBackground(buttonGrad);
        torchButton.setClickable(true);
        torchButton.setElevation(20);
        torchButton.setTranslationZ(20);
        
        // Layer 3: Inner ring
        View innerRing = new View(this);
        android.widget.FrameLayout.LayoutParams ringParams = new android.widget.FrameLayout.LayoutParams(240, 240);
        ringParams.gravity = android.view.Gravity.CENTER;
        innerRing.setLayoutParams(ringParams);
        GradientDrawable ringGrad = new GradientDrawable();
        ringGrad.setShape(GradientDrawable.OVAL);
        ringGrad.setStroke(2, Color.parseColor("#FFFFFF"));
        ringGrad.setColor(Color.TRANSPARENT);
        innerRing.setBackground(ringGrad);
        
        // Torch icon
        torchIcon = new TextView(this);
        torchIcon.setText("🔦");
        torchIcon.setTextSize(80);
        torchIcon.setTextColor(Color.WHITE);
        android.widget.FrameLayout.LayoutParams iconParams = new android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT);
        iconParams.gravity = android.view.Gravity.CENTER;
        torchIcon.setLayoutParams(iconParams);
        
        buttonContainer.addView(outerGlow);
        buttonContainer.addView(torchButton);
        buttonContainer.addView(innerRing);
        buttonContainer.addView(torchIcon);
        
        // Status text with dynamic effects
        statusText = new TextView(this);
        statusText.setText("🔴 TORCH OFF");
        statusText.setTextSize(22);
        statusText.setTextColor(Color.parseColor("#9E9E9E"));
        statusText.setTypeface(null, android.graphics.Typeface.BOLD);
        statusText.setGravity(android.view.Gravity.CENTER);
        statusText.setPadding(0, 16, 0, 8);
        
        // Brightness indicator
        intensityText = new TextView(this);
        intensityText.setText("⚡ INTENSITY: 0%");
        intensityText.setTextSize(12);
        intensityText.setTextColor(Color.parseColor("#88666666"));
        intensityText.setGravity(android.view.Gravity.CENTER);
        intensityText.setPadding(0, 0, 0, 16);
        
        // Instruction
        TextView instruction = new TextView(this);
        instruction.setText("Tap anywhere on the circle to toggle\nLong press for magic effect");
        instruction.setTextSize(12);
        instruction.setTextColor(Color.parseColor("#88666666"));
        instruction.setGravity(android.view.Gravity.CENTER);
        
        // Add all views
        rootLayout.addView(title);
        rootLayout.addView(subtitle);
        rootLayout.addView(buttonContainer);
        rootLayout.addView(statusText);
        rootLayout.addView(intensityText);
        rootLayout.addView(instruction);
        
        setContentView(rootLayout);
        
        // Check camera permission
        checkCameraPermission();
        
        // Button click listeners
        torchButton.setOnClickListener(v -> {
            if (hasCameraPermission()) {
                if (vibrator != null) vibrator.vibrate(30);
                animateButton();
                toggleTorch();
            } else {
                checkCameraPermission();
            }
        });
        
        // Long press for magic effect
        torchButton.setOnLongClickListener(v -> {
            if (torchOn) {
                magicEffect();
            } else {
                Toast.makeText(this, "Turn on torch first for magic effect!", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }
    
    private GradientDrawable createGradientDrawable(int[] colors) {
        GradientDrawable gradient = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR, colors);
        gradient.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        return gradient;
    }
    
    private void magicEffect() {
        // Color changing effect
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                runOnUiThread(() -> {
                    int randomColor = colors[random.nextInt(colors.length)];
                    GradientDrawable btnGrad = (GradientDrawable) torchButton.getBackground();
                    btnGrad.setColor(randomColor);
                    
                    if (vibrator != null) vibrator.vibrate(50);
                    Toast.makeText(this, "✨ MAGIC MODE ✨", Toast.LENGTH_SHORT).show();
                });
                try { Thread.sleep(200); } catch (Exception e) {}
            }
            runOnUiThread(() -> {
                GradientDrawable btnGrad = (GradientDrawable) torchButton.getBackground();
                btnGrad.setColor(Color.parseColor("#FFEB3B"));
                Toast.makeText(MainActivity.this, "🌈 Color Magic Complete!", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
    
    private void animateButton() {
        // Scale animation
        Animation scaleAnim = new ScaleAnimation(1f, 0.85f, 1f, 0.85f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnim.setDuration(100);
        scaleAnim.setRepeatMode(Animation.REVERSE);
        scaleAnim.setRepeatCount(1);
        
        // Rotation animation
        Animation rotateAnim = new RotateAnimation(0, 360,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnim.setDuration(300);
        rotateAnim.setRepeatCount(1);
        
        torchButton.startAnimation(scaleAnim);
        torchIcon.startAnimation(rotateAnim);
    }
    
    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
    
    private void checkCameraPermission() {
        if (!hasCameraPermission()) {
            ActivityCompat.requestPermissions(this, 
                new String[]{android.Manifest.permission.CAMERA}, 
                CAMERA_PERMISSION_REQUEST);
        } else {
            initCamera();
        }
    }
    
    private void initCamera() {
        try {
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (Exception e) {
            Toast.makeText(this, "Camera error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initCamera();
                Toast.makeText(this, "✅ Camera permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Camera permission required for torch", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void toggleTorch() {
        if (cameraManager == null || cameraId == null) {
            Toast.makeText(this, "Camera not ready. Please restart app.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            torchOn = !torchOn;
            cameraManager.setTorchMode(cameraId, torchOn);
            
            GradientDrawable btnGrad = (GradientDrawable) torchButton.getBackground();
            AnimationDrawable bgAnim = (AnimationDrawable) rootLayout.getBackground();
            
            if (torchOn) {
                // Torch ON - Dynamic effects
                btnGrad.setColor(Color.parseColor("#FFEB3B"));
                btnGrad.setStroke(4, Color.WHITE);
                statusText.setText("💡 TORCH ON 💡");
                statusText.setTextColor(Color.parseColor("#FFEB3B"));
                torchIcon.setText("💡");
                torchIcon.setTextSize(90);
                intensityText.setText("⚡ INTENSITY: 100% ⚡");
                intensityText.setTextColor(Color.parseColor("#FFEB3B"));
                
                // Animate background faster
                bgAnim.start();
                
                // Pulse effect on button
                animatePulse();
                
                // Change outer glow
                View outerGlow = ((android.widget.FrameLayout) torchButton.getParent()).getChildAt(0);
                GradientDrawable glowGrad = (GradientDrawable) outerGlow.getBackground();
                glowGrad.setColor(Color.parseColor("#40FFEB3B"));
                
                Toast.makeText(this, "✨ TORCH ACTIVATED ✨", Toast.LENGTH_SHORT).show();
            } else {
                // Torch OFF
                btnGrad.setColor(Color.parseColor("#E94560"));
                btnGrad.setStroke(4, Color.parseColor("#FF03DAC5"));
                statusText.setText("🔴 TORCH OFF");
                statusText.setTextColor(Color.parseColor("#9E9E9E"));
                torchIcon.setText("🔦");
                torchIcon.setTextSize(80);
                intensityText.setText("⚡ INTENSITY: 0%");
                intensityText.setTextColor(Color.parseColor("#88666666"));
                
                // Reset outer glow
                View outerGlow = ((android.widget.FrameLayout) torchButton.getParent()).getChildAt(0);
                GradientDrawable glowGrad = (GradientDrawable) outerGlow.getBackground();
                glowGrad.setColor(Color.parseColor("#20E94560"));
                
                Toast.makeText(this, "🔦 TORCH DEACTIVATED", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            torchOn = false;
        }
    }
    
    private void animatePulse() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (torchOn) {
                    Animation pulse = new ScaleAnimation(1f, 1.05f, 1f, 1.05f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                    pulse.setDuration(800);
                    pulse.setRepeatMode(Animation.REVERSE);
                    pulse.setRepeatCount(Animation.INFINITE);
                    torchButton.startAnimation(pulse);
                }
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (torchOn && cameraManager != null && cameraId != null) {
            try {
                cameraManager.setTorchMode(cameraId, false);
            } catch (Exception e) {}
        }
        handler.removeCallbacksAndMessages(null);
    }
}