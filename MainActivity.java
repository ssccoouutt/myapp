package com.example.torch;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.os.Vibrator;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.Handler;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class MainActivity extends Activity {
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String audioFilePath;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private View recordButton;
    private View playButton;
    private TextView statusText;
    private TextView tomText;
    private Vibrator vibrator;
    private ImageView tomImageView;
    private Handler handler = new Handler();
    private Random random = new Random();
    
    // Permission codes
    private static final int PERMISSION_REQUEST_CODE = 100;
    
    // Eye and mouth animation
    private boolean eyesOpen = true;
    private int mouthState = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        audioFilePath = getExternalFilesDir(null).getAbsolutePath() + "/tom_voice.wav";
        
        // Check and request permissions
        checkPermissions();
        
        // Main layout
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
        title.setText("😺 TALKING TOM 😺");
        title.setTextSize(28);
        title.setTextColor(Color.WHITE);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(android.view.Gravity.CENTER);
        title.setPadding(0, 0, 0, 8);
        
        // Subtitle
        TextView subtitle = new TextView(this);
        subtitle.setText("Tap Record, speak, then Play!");
        subtitle.setTextSize(14);
        subtitle.setTextColor(Color.parseColor("#B3FFFFFF"));
        subtitle.setGravity(android.view.Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 32);
        
        // Tom's image
        tomImageView = new ImageView(this);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(400, 400);
        imgParams.setMargins(0, 0, 0, 32);
        tomImageView.setLayoutParams(imgParams);
        updateTomFace();
        
        // Status text
        statusText = new TextView(this);
        statusText.setText("🔴 Ready to record");
        statusText.setTextSize(16);
        statusText.setTextColor(Color.parseColor("#9E9E9E"));
        statusText.setTypeface(null, android.graphics.Typeface.BOLD);
        statusText.setGravity(android.view.Gravity.CENTER);
        statusText.setPadding(0, 0, 0, 16);
        
        // Tom's speech bubble
        tomText = new TextView(this);
        tomText.setText("🎤 Say something!");
        tomText.setTextSize(14);
        tomText.setTextColor(Color.WHITE);
        tomText.setPadding(24, 16, 24, 16);
        tomText.setGravity(android.view.Gravity.CENTER);
        
        GradientDrawable bubbleBg = new GradientDrawable();
        bubbleBg.setCornerRadius(30);
        bubbleBg.setColor(Color.parseColor("#E94560"));
        tomText.setBackground(bubbleBg);
        
        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        bubbleParams.setMargins(0, 0, 0, 24);
        tomText.setLayoutParams(bubbleParams);
        
        // Button container
        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setGravity(android.view.Gravity.CENTER);
        buttonRow.setPadding(0, 16, 0, 16);
        
        // Record button
        recordButton = createCircularButton("🎙️", Color.parseColor("#E94560"));
        LinearLayout.LayoutParams recParams = new LinearLayout.LayoutParams(140, 140);
        recParams.setMargins(16, 0, 16, 0);
        recordButton.setLayoutParams(recParams);
        
        // Play button
        playButton = createCircularButton("▶️", Color.parseColor("#4ECDC4"));
        LinearLayout.LayoutParams playParams = new LinearLayout.LayoutParams(140, 140);
        playParams.setMargins(16, 0, 16, 0);
        playButton.setLayoutParams(playParams);
        
        buttonRow.addView(recordButton);
        buttonRow.addView(playButton);
        
        // Add all views
        root.addView(title);
        root.addView(subtitle);
        root.addView(tomImageView);
        root.addView(statusText);
        root.addView(tomText);
        root.addView(buttonRow);
        
        setContentView(root);
        
        // Setup listeners
        recordButton.setOnClickListener(v -> {
            if (vibrator != null) vibrator.vibrate(30);
            animateButton(recordButton);
            if (!isRecording) {
                startRecording();
            } else {
                stopRecording();
            }
        });
        
        playButton.setOnClickListener(v -> {
            if (vibrator != null) vibrator.vibrate(30);
            animateButton(playButton);
            if (!isPlaying && !isRecording) {
                playRecording();
            } else if (isPlaying) {
                stopPlaying();
            } else {
                Toast.makeText(this, "Stop recording first!", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Start animations
        startEyeBlinking();
        startMouthAnimation();
    }
    
    private void checkPermissions() {
        String[] permissions = {
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        };
        
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        
        if (!allGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }
    
    private View createCircularButton(String text, int color) {
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        
        View button = new View(this);
        GradientDrawable grad = new GradientDrawable();
        grad.setShape(GradientDrawable.OVAL);
        grad.setColor(color);
        grad.setStroke(3, Color.WHITE);
        button.setBackground(grad);
        button.setClickable(true);
        button.setElevation(12);
        
        TextView icon = new TextView(this);
        icon.setText(text);
        icon.setTextSize(48);
        icon.setTextColor(Color.WHITE);
        android.widget.FrameLayout.LayoutParams iconParams = new android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
            android.view.Gravity.CENTER);
        icon.setLayoutParams(iconParams);
        
        container.addView(button, new android.widget.FrameLayout.LayoutParams(140, 140));
        container.addView(icon, iconParams);
        
        return container;
    }
    
    private void updateTomFace() {
        Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        
        // Face background
        paint.setColor(Color.parseColor("#F5A623"));
        canvas.drawCircle(200, 200, 180, paint);
        
        // Ears
        paint.setColor(Color.parseColor("#E0951A"));
        canvas.drawCircle(80, 80, 50, paint);
        canvas.drawCircle(320, 80, 50, paint);
        
        // Inner ears
        paint.setColor(Color.parseColor("#FFC107"));
        canvas.drawCircle(80, 80, 30, paint);
        canvas.drawCircle(320, 80, 30, paint);
        
        // Eyes
        if (eyesOpen) {
            paint.setColor(Color.WHITE);
            canvas.drawCircle(150, 180, 30, paint);
            canvas.drawCircle(250, 180, 30, paint);
            
            paint.setColor(Color.BLACK);
            canvas.drawCircle(150, 180, 15, paint);
            canvas.drawCircle(250, 180, 15, paint);
            
            paint.setColor(Color.WHITE);
            canvas.drawCircle(140, 170, 5, paint);
            canvas.drawCircle(240, 170, 5, paint);
        } else {
            paint.setColor(Color.BLACK);
            canvas.drawRect(120, 170, 180, 190, paint);
            canvas.drawRect(220, 170, 280, 190, paint);
        }
        
        // Nose
        paint.setColor(Color.parseColor("#FF6B6B"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(200, 230, 15, paint);
        
        // Mouth
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(4);
        paint.setStyle(Paint.Style.STROKE);
        
        if (mouthState == 0) {
            Path path = new Path();
            path.moveTo(170, 270);
            path.quadTo(200, 290, 230, 270);
            canvas.drawPath(path, paint);
        } else if (mouthState == 1) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            canvas.drawOval(175, 265, 225, 295, paint);
            paint.setColor(Color.RED);
            canvas.drawOval(180, 270, 220, 290, paint);
        } else {
            canvas.drawCircle(200, 280, 20, paint);
        }
        
        tomImageView.setImageBitmap(bitmap);
    }
    
    private void startEyeBlinking() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                eyesOpen = !eyesOpen;
                updateTomFace();
                handler.postDelayed(this, 3000 + random.nextInt(2000));
            }
        });
    }
    
    private void startMouthAnimation() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isRecording || isPlaying) {
                    mouthState = (mouthState + 1) % 3;
                    updateTomFace();
                    handler.postDelayed(this, 150);
                } else {
                    mouthState = 0;
                    updateTomFace();
                    handler.postDelayed(this, 500);
                }
            }
        });
    }
    
    private void animateButton(View button) {
        Animation anim = new ScaleAnimation(1f, 0.85f, 1f, 0.85f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(100);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(1);
        button.startAnimation(anim);
    }
    
    private void startRecording() {
        if (!hasPermissions()) {
            Toast.makeText(this, "Please grant microphone permissions", Toast.LENGTH_SHORT).show();
            checkPermissions();
            return;
        }
        
        try {
            File audioFile = new File(audioFilePath);
            if (audioFile.exists()) {
                audioFile.delete();
            }
            
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setAudioBitRate(128000);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.prepare();
            mediaRecorder.start();
            
            isRecording = true;
            statusText.setText("🔴 RECORDING... Speak now!");
            statusText.setTextColor(Color.parseColor("#FF6B6B"));
            tomText.setText("🎤 Listening... Say something funny!");
            
            Toast.makeText(this, "Recording started! Tap Stop when done.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to record: " + e.getMessage(), Toast.LENGTH_LONG).show();
            stopRecording();
        }
    }
    
    private void stopRecording() {
        try {
            if (mediaRecorder != null) {
                try {
                    mediaRecorder.stop();
                } catch (Exception e) {
                    // Ignore stop errors
                }
                mediaRecorder.release();
                mediaRecorder = null;
            }
            
            isRecording = false;
            statusText.setText("✅ Recording saved! Tap PLAY to hear Tom!");
            statusText.setTextColor(Color.parseColor("#4ECDC4"));
            tomText.setText("✅ Got it! Now tap PLAY to hear me!");
            
            Toast.makeText(this, "Recording saved!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error stopping recording", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void playRecording() {
        File audioFile = new File(audioFilePath);
        if (!audioFile.exists()) {
            Toast.makeText(this, "No recording found! Please record something first.", Toast.LENGTH_LONG).show();
            return;
        }
        
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            
            isPlaying = true;
            statusText.setText("🔊 PLAYING... Tom is talking!");
            statusText.setTextColor(Color.parseColor("#4ECDC4"));
            tomText.setText("😺 " + getRandomTomPhrase());
            
            mediaPlayer.setOnCompletionListener(mp -> {
                stopPlaying();
            });
            
            Toast.makeText(this, "Tom is speaking! 🎵", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error playing recording", Toast.LENGTH_LONG).show();
        }
    }
    
    private void stopPlaying() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception e) {}
            mediaPlayer = null;
        }
        isPlaying = false;
        statusText.setText("⏹️ Ready to record again!");
        statusText.setTextColor(Color.parseColor("#9E9E9E"));
        tomText.setText("🎤 Say something!");
    }
    
    private String getRandomTomPhrase() {
        String[] phrases = {
            "That's hilarious! 😂", "You sound funny! 🤣", "Let's do that again! 🎤",
            "I love your voice! 💕", "You're awesome! ⭐", "Tell me more! 😺",
            "Haha! That's great! 🎉", "I'm Tom the cat! 🐱", "This is fun! 🎵",
            "Meow! That was purr-fect! 🐱", "Say something else! 🎙️"
        };
        return phrases[random.nextInt(phrases.length)];
    }
    
    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted! You can now record.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Microphone permission is required for this app!", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
            } catch (Exception e) {}
        }
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception e) {}
        }
        handler.removeCallbacksAndMessages(null);
    }
}
