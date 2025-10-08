package com.example.classwork3;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase mydb;

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
        try {
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

            Log.e("DEBUG", "found1 = " + found1);
            Log.e("DEBUG", "found2 = " + found2);
            Log.e("DEBUG", "found3 = " + found3);

            mydb = this.openOrCreateDatabase("mydb", Context.MODE_PRIVATE, null);
            Cursor maxCursor = mydb.rawQuery("SELECT MAX(numSaved) FROM storage", null);
            if (maxCursor.moveToFirst()) {
                numSaved = maxCursor.getInt(0) + 1;
            }
            maxCursor.close();
            createDataBase();

            // starts activity for result, but the complicated way bc deprecated
            cameraLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Bundle extras = result.getData().getExtras();
                            if (extras != null) {
                                currentImage = (Bitmap) extras.get("data");
                                mainImageView.setImageBitmap(currentImage);
                            }
                        }
                    }
            );

            // initializes button and puts in button id in activity_main
            Button takePictureBtn = findViewById(R.id.takePictureBtn);
            // when click button, use camera intent
            takePictureBtn.setOnClickListener(v -> useCamera());

            mainImageView.setImageDrawable(null);
            setTags.getText().clear();
            findTags.getText().clear();
        } catch (Exception e) {
            Toast.makeText(this, "Crash in onCreate: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void createDataBase() {
        mydb.execSQL("CREATE TABLE IF NOT EXISTS storage (numSaved INTEGER, picture BLOB, allTags TEXT , dateTime TEXT);");
        Cursor c = mydb.rawQuery("SELECT * FROM storage ORDER BY numSaved DESC;", null);
        assignSelected(c);
        c.close();
    }

    public void useCamera() {
        PackageManager pm = getPackageManager();

        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(cameraIntent);
        } else {
            // No camera available → show a message to the user
            Toast.makeText(this, "No camera available on this device", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public void saveToTable(View view) {

        if (currentImage == null) {
            Toast.makeText(this, "No image to save", Toast.LENGTH_SHORT).show();
            return;
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        currentImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] ba = stream.toByteArray();

        String newTags = setTags.getText().toString();
        String[] tagsArray = newTags.split(",");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tagsArray.length; i++) {
            if (!tagsArray[i].trim().isEmpty()) {
                if (sb.length() > 0) sb.append(",");
                sb.append(tagsArray[i].trim());
            }
        }

        // Get current date and time
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(now);
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        String currentTime = timeFormat.format(now);
        String dateAndTime = currentDate + " - " + currentTime;

        // Create a ContentValues object to map column names to values
        ContentValues values = new ContentValues();
        values.put("numSaved", numSaved);
        values.put("picture", ba);
        values.put("allTags", sb.toString());
        values.put("dateTime", dateAndTime);

        mydb.insert("storage", null, values);
        numSaved++;

        // clear main image bitmap, imageview, and the tag edit text
        currentImage = null;
        mainImageView.setImageDrawable(null);
        setTags.getText().clear();
    }

    public void searchTable(View view) {
        Cursor c;
        String searchTag = findTags.getText().toString();
        if (searchTag.isEmpty()) {
            c = mydb.rawQuery("SELECT * FROM storage ORDER BY numSaved DESC;", null);
        } else {
            c = mydb.rawQuery("SELECT * FROM storage WHERE ',' || allTags || ',' LIKE '%,' || ? || ',%' ORDER BY numSaved DESC", new String[]{searchTag});
        }
        assignSelected(c);
        c.close();
    }

    // mydb.execSQL("CREATE TABLE storage (numSaved INTEGER, picture BLOB, allTags TEXT , singleTag TEXT , dateTime TEXT);");
    public void assignSelected(Cursor c) {
        int count = 0;

        if (c != null && c.moveToFirst()) {
            // cache column indices once
            int idxPicture = c.getColumnIndexOrThrow("picture");
            int idxAllTags = c.getColumnIndexOrThrow("allTags");
            int idxDateTime = c.getColumnIndexOrThrow("dateTime");

            // optional: store views in arrays for concise code
            ImageView[] foundImgs = new ImageView[]{found1, found2, found3};
            TextView[] foundTags = new TextView[]{found1Tags, found2Tags, found3Tags};
            TextView[] foundDate = new TextView[]{found1DateTime, found2DateTime, found3DateTime};

            for (int i = 0; i < foundImgs.length; i++) {
                foundImgs[i].setImageDrawable(null);
                foundTags[i].setText("unavailable");
                foundDate[i].setText("");
            }

            do {
                if (count >= 3) break;                    // stop *before* moving the cursor again

                byte[] pictureBlob = c.getBlob(idxPicture);
                Bitmap b = null;
                if (pictureBlob != null && pictureBlob.length > 0) {
                    b = BitmapFactory.decodeByteArray(pictureBlob, 0, pictureBlob.length);
                }

                if (b != null) {
                    foundImgs[count].setImageBitmap(b);
                } else {
                    foundImgs[count].setImageDrawable(null); // or set a placeholder
                }

                String allTags = c.isNull(idxAllTags) ? "unavailable" : c.getString(idxAllTags);
                String dateTime = c.isNull(idxDateTime) ? "" : c.getString(idxDateTime);

                foundTags[count].setText(allTags);
                foundDate[count].setText(dateTime);

                count++;

            } while (count < 3 && c.moveToNext()); // check count first to avoid extra moveToNext()
        }
    }
}