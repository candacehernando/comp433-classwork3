package com.example.classwork3;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase mydb = this.openOrCreateDatabase("mydb", Context.MODE_PRIVATE, null);

    private ActivityResultLauncher<Intent> cameraLauncher;

    private ImageView mainImageView;
    private Bitmap currentImage;
    private EditText setTags;

    private EditText findTags;

    private ImageView found1;
    private TextView found1Tags;
    private TextView found1DateTime;

    private ImageView found2;
    private TextView found2Tags;
    private TextView found2DateTime;

    private ImageView found3;
    private TextView found3Tags;
    private TextView found3DateTime;


    private int numSaved = 0;

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

        mainImageView = findViewById(R.id.mainImageView);
        setTags = findViewById(R.id.setTags);
        findTags = findViewById(R.id.findTags);

        found1 = findViewById(R.id.found1);
        found1Tags = findViewById(R.id.found1Tags);
        found1DateTime = findViewById(R.id.found1DateTime);

        found2 = findViewById(R.id.found2);
        found2Tags = findViewById(R.id.found2Tags);
        found2DateTime = findViewById(R.id.found2DateTime);

        found3 = findViewById(R.id.found3);
        found3Tags = findViewById(R.id.found3Tags);
        found3DateTime = findViewById(R.id.found3DateTime);

        // initializes button and puts in button id in activity_main
        Button takePictureBtn = findViewById(R.id.takePictureBtn);
        // when click button, use camera intent
        takePictureBtn.setOnClickListener(v -> useCamera());

        createDataBase();
    }

    public void createDataBase() {
        mydb.execSQL("DROP TABLE IF EXISTS STORAGE;");
        mydb.execSQL("CREATE TABLE STORAGE (NUMSAVED INT, PICTURE BITMAP, ALLTAGS STRING , SINGLETAG STRING , DATE STRING, TIME STRING);");
    }

    public void useCamera() {
        Intent cameraTime = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // had to replace resolveActivity
        PackageManager packageManager = getPackageManager();
        ResolveInfo resolveInfo = packageManager.resolveActivity(
                cameraTime,
                PackageManager.MATCH_DEFAULT_ONLY
        );
        // starts activity for result, but the complicated way bc deprecated
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        if (resolveInfo != null) {
                            cameraLauncher.launch(cameraTime);
                        }
                    }
                }
        );
    }

    // update ImageView after the user takes the picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle extras = data.getExtras();
        assert extras != null;
        currentImage = (Bitmap) extras.get("data");
        mainImageView.setImageBitmap(currentImage);
    }

    public void saveToTable(View view) {

        String newTags = setTags.getText().toString();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat newSimDate = new SimpleDateFormat("MM-dd-yyyy");
        String newDate = newSimDate.format(Calendar.getInstance().getTime());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat newSimTime = new SimpleDateFormat(" hh:mm:ss ");
        String newTime = newSimTime.format(Calendar.getInstance().getTime());

        // (PICTURE BITMAP, ALLTAGS STRING , SINGLETAG STRING , DATE STRING, TIME STRING)

        if(currentImage != null && !newTags.isEmpty()) {
            String regex = "[, ]";
            String[] myTags = newTags.split(regex);
            for (String s : myTags) {
                s = s.trim();
                if (!s.isEmpty()) {
                    mydb.execSQL("INSERT INTO STORAGE VALUES ('" + numSaved + "','" + currentImage + "','" + newTags + "','" + s + "','" + newDate + "','" + newTime + "');");
                    numSaved++;
                }
            }
        }

        // clear main image bitmap, imageview, and the tag edit text
        currentImage = null;
        mainImageView.setImageDrawable(null);
        setTags.getText().clear();
    }

    public void searchTable(View view) {
        String searchTags = findTags.getText().toString();
        String regex = "[, ]";
        String[] myTags = searchTags.split(regex);
        for (String s : myTags) {
            s = s.trim();
            if (!s.isEmpty()) {}
        }
    }
}