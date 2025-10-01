package com.example.classwork3;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

private ActivityResultLauncher<Intent> cameraLauncher;

private ImageView mainImageView;
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cameraTLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        ;
                    }
                }
    }

    public void useCamera() {
        Intent cameraTime = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // had to replace resolveActivity
        PackageManager packageManager = getPackageManager();
        ResolveInfo resolveInfo = packageManager.resolveActivity(
                cameraTime,
                PackageManager.MATCH_DEFAULT_ONLY
        );
        // Ensure there is a camera app to handle the intent
        if (resolveInfo != null) {
            startActivityForResult(, REQUEST_IMAGE_CAPTURE);
        }
    }
}