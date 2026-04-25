package com.example.torch;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.content.Context;
import android.hardware.camera2.CameraManager;

public class MainActivity extends Activity {
    private boolean torchOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Button btn = new Button(this);
        btn.setText("🔦 TORCH");
        btn.setTextSize(24);
        btn.setPadding(50, 30, 50, 30);
        
        btn.setOnClickListener(v -> {
            try {
                CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                String cameraId = camManager.getCameraIdList()[0];
                torchOn = !torchOn;
                camManager.setTorchMode(cameraId, torchOn);
                btn.setText(torchOn ? "💡 ON" : "🔦 OFF");
                Toast.makeText(MainActivity.this, torchOn ? "Torch ON" : "Torch OFF", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        
        setContentView(btn);
    }
}
