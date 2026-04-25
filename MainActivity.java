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
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaExtractor;
import android.media.MediaMuxer;
import java.io.IOException;
import java.io.File;
import android.os.Handler;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
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
    
    // Eye animation variables
    private boolean eyesOpen = true;
    private int eyeX = 150, eyeY = 200;
    private int mouthState = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        audioFilePath = getExternalFilesDir(null).getAbsolutePath() + "/tom_voice.wav";
        
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
        
        // Tom's image (custom drawn)
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
        tomText.setBackgroundColor(Color.parseColor("#E94560"));
        tomText.setPadding(24, 16, 24, 16);
        tomText.setGravity(android.view.Gravity.CENTER);
        
        // Round the speech bubble corners
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
        recordButton = new View(this);
        LinearLayout.LayoutParams recParams = new LinearLayout.LayoutParams(140, 140);
        recParams.setMargins(16, 0, 16, 0);
        recordButton.setLayoutParams(recParams);
        
        GradientDrawable recGrad = new GradientDrawable();
        recGrad.setShape(GradientDrawable.OVAL);
        recGrad.setColor(Color.parseColor("#E94560"));
        recGrad.setStroke(3, Color.WHITE);
        recordButton.setBackground(recGrad);
        recordButton.setClickable(true);
        recordButton.setElevation(12);
        
        TextView recIcon = new TextView(this);
        recIcon.setText("🎙️");
        recIcon.setTextSize(48);
        recIcon.setTextColor(Color.WHITE);
        android.widget.FrameLayout recContainer = new android.widget.FrameLayout(this);
        recContainer.addView(recordButton);
        recContainer.addView(recIcon, new android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
            android.view.Gravity.CENTER));
        
        // Play button
        playButton = new View(this);
        LinearLayout.LayoutParams playParams = new LinearLayout.LayoutParams(140, 140);
        playParams.setMargins(16, 0, 16, 0);
        playButton.setLayoutParams(playParams);
        
        GradientDrawable playGrad = new GradientDrawable();
        playGrad.setShape(GradientDrawable.OVAL);
        playGrad.setColor(Color.parseColor("#4ECDC4"));
        playGrad.setStroke(3, Color.WHITE);
        playButton.setBackground(playGrad);
        playButton.setClickable(true);
        playButton.setElevation(12);
        
        TextView playIcon = new TextView(this);
        playIcon.setText("▶️");
        playIcon.setTextSize(48);
        playIcon.setTextColor(Color.WHITE);
        android.widget.FrameLayout playContainer = new android.widget.FrameLayout(this);
        playContainer.addView(playButton);
        playContainer.addView(playIcon, new android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
            android.view.Gravity.CENTER));
        
        buttonRow.addView(recContainer);
        buttonRow.addView(playContainer);
        
        // Add all views
        root.addView(title);
        root.addView(subtitle);
        root.addView(tomImageView);
        root.addView(statusText);
        root.addView(tomText);
        root.addView(buttonRow);
        
        setContentView(root);
        
        // Setup record button
        recordButton.setOnClickListener(v -> {
            if (vibrator != null) vibrator.vibrate(30);
            animateButton(recordButton);
            if (!isRecording) {
                startRecording();
            } else {
                stopRecording();
            }
        });
        
        // Setup play button
        playButton.setOnClickListener(v -> {
            if (vibrator != null) vibrator.vibrate(30);
            animateButton(playButton);
            if (!isPlaying) {
                playRecording();
            } else {
                stopPlaying();
            }
        });
        
        // Animate Tom's eyes blinking
        startEyeBlinking();
        
        // Animate mouth when recording/playing
        startMouthAnimation();
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
            
            // Eye shine
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
        canvas.drawCircle(200, 230, 15, paint);
        
        // Mouth based on state
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(4);
        paint.setStyle(Paint.Style.STROKE);
        
        if (mouthState == 0) {
            // Happy smile
            Path path = new Path();
            path.moveTo(170, 270);
            path.quadTo(200, 290, 230, 270);
            canvas.drawPath(path, paint);
        } else if (mouthState == 1) {
            // Open mouth (talking)
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            canvas.drawRect(175, 265, 225, 295, paint);
            paint.setColor(Color.RED);
            canvas.drawRect(180, 270, 220, 290, paint);
        } else {
            // O mouth
            paint.setStyle(Paint.Style.STROKE);
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
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.prepare();
            mediaRecorder.start();
            
            isRecording = true;
            statusText.setText("🔴 RECORDING... Speak now!");
            statusText.setTextColor(Color.parseColor("#FF6B6B"));
            tomText.setText("🎤 Listening... Say something funny!");
            
            GradientDrawable btnGrad = (GradientDrawable) recordButton.getBackground();
            btnGrad.setColor(Color.parseColor("#FF6B6B"));
            
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to record: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void stopRecording() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            }
            
            isRecording = false;
            statusText.setText("✅ Recording saved! Tap PLAY to hear Tom!");
            statusText.setTextColor(Color.parseColor("#4ECDC4"));
            tomText.setText("✅ Got it! Now tap PLAY to hear me!");
            
            GradientDrawable btnGrad = (GradientDrawable) recordButton.getBackground();
            btnGrad.setColor(Color.parseColor("#E94560"));
            
            Toast.makeText(this, "Recording saved!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error stopping recording", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void playRecording() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            
            isPlaying = true;
            statusText.setText("🔊 PLAYING... Tom is talking!");
            statusText.setTextColor(Color.parseColor("#4ECDC4"));
            tomText.setText("😺 " + getRandomTomPhrase());
            
            GradientDrawable btnGrad = (GradientDrawable) playButton.getBackground();
            btnGrad.setColor(Color.parseColor("#FF6B6B"));
            
            mediaPlayer.setOnCompletionListener(mp -> {
                stopPlaying();
            });
            
            Toast.makeText(this, "🎵 Tom is speaking! 🎵", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "No recording found! Please record something first.", Toast.LENGTH_LONG).show();
        }
    }
    
    private String getRandomTomPhrase() {
        String[] phrases = {
            "That's hilarious! 😂", "You sound funny! 🤣", "Let's do that again! 🎤",
            "I love your voice! 💕", "You're awesome! ⭐", "Tell me more! 😺",
            "Haha! That's great! 🎉", "I'm Tom the cat! 🐱", "This is fun! 🎵"
        };
        return phrases[random.nextInt(phrases.length)];
    }
    
    private void stopPlaying() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPlaying = false;
        statusText.setText("⏹️ Ready to record again!");
        statusText.setTextColor(Color.parseColor("#9E9E9E"));
        tomText.setText("🎤 Say something!");
        
        GradientDrawable btnGrad = (GradientDrawable) playButton.getBackground();
        btnGrad.setColor(Color.parseColor("#4ECDC4"));
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaRecorder != null) {
            mediaRecorder.release();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        handler.removeCallbacksAndMessages(null);
    }
}
