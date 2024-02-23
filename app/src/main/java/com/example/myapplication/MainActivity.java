/package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions;

import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ImageView imageView;
    private Button buttonTranslate, gallery;
    private TextView textViewTranslatedText;

    Spinner langs, fromLang;
    final int GALARY_REQ_CODE = 1000;
    final int CAMERA_CODE = 9000;
    final int REQUEST_IMAGE_CAPTURE = 2344;
    final int EXTERNAL_STORAGE_CODE = 7238;
    TreeMap<String, Integer> langMap = new TreeMap<>();


    int curLan = FirebaseTranslateLanguage.HI;
    int fromCurLang = FirebaseTranslateLanguage.EN;
    String languages[], imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gallery = (Button) findViewById(R.id.gallery);

        imageView = findViewById(R.id.imageView);
        buttonTranslate = findViewById(R.id.buttonTranslate);
        textViewTranslatedText = findViewById(R.id.textViewTranslatedText);
        textViewTranslatedText.setPaintFlags(0);
        langs = (Spinner) findViewById(R.id.langs);
        langs.setOnItemSelectedListener(this);

        fromLang = (Spinner) findViewById(R.id.fromLangs);


        // different languages
        langMap.put("Hindi", FirebaseTranslateLanguage.HI);
        langMap.put("English", FirebaseTranslateLanguage.EN);
        langMap.put("Bengali", FirebaseTranslateLanguage.BN);
        langMap.put("Gujarati", FirebaseTranslateLanguage.GU);
        langMap.put("Japanese", FirebaseTranslateLanguage.JA);
        langMap.put("Tamil", FirebaseTranslateLanguage.TA);
        langMap.put("Telugu", FirebaseTranslateLanguage.TE);
        langMap.put("Russian", FirebaseTranslateLanguage.RU);
        langMap.put("Portugual", FirebaseTranslateLanguage.PT);

        languages = langMap.keySet().toArray(new String[7]);

        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,languages);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        langs.setAdapter(aa);
        langs.setSelection(3);


        fromLang.setAdapter(aa);
        fromLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fromCurLang = langMap.get(languages[i]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        fromLang.setSelection(1);

        // Select image from URI
        gallery.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_CODE);
                return;
            }
            // Code to change the image from gallery
            loadGalleryImage();
        });

        // Load an image into the ImageView
        // You can use any method to load an image, such as from the gallery or camera
        buttonTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recognizeTextAndTranslate();
            }
        });
    }

    private void loadGalleryImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALARY_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // for gallery
        if (requestCode == GALARY_REQ_CODE && resultCode == RESULT_OK && data != null) {
            Uri imagePath = data.getData();
            imageView.setImageURI(imagePath);
        }
    }


    // Here text is recognizing
    private void recognizeTextAndTranslate() {
        InputImage image = InputImage.fromBitmap(((BitmapDrawable) imageView.getDrawable()).getBitmap(), 0);
        TextRecognizer recognizer = TextRecognition.getClient(new DevanagariTextRecognizerOptions.Builder().build());

        recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text text) {

                        translateText(text.getText());
//                        Toast.makeText(getApplicationContext(), text.getText().toString(),Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        textViewTranslatedText.setText("Error: " + e.getMessage());
                    }
                });
    }


    void identifyLang(String text){

        FirebaseLanguageIdentification languageIdentifier =
                FirebaseNaturalLanguage.getInstance().getLanguageIdentification();
        languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@Nullable String languageCode) {
                                if (languageCode != "und") {
                                    System.out.print("\n\n\n\n" + languageCode + "\n\n\n\n");
                                } else {
                                    System.out.print("\n\n\n\n" + "not identified" + "\n\n\n\n");
                                }

                                System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be loaded or other internal error.
                                System.out.println("\n\n\n\n\n\n\n\n\n\n " + "error" + "\n\n\n\n\n\n\n\n");
                            }
                        });

    }


    // Here text is being translating.
    private void translateText(String textToTranslate) {

//        identifyLang(textToTranslate);
        System.out.println(fromCurLang + " " + curLan + "\n\n\n\n\n\n");

        // Create an English-German translator:
        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(fromCurLang)
                        .setTargetLanguage(curLan)
                        .build();
        final FirebaseTranslator translator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();

        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                // Model downloaded successfully. Okay to start translating.
                                // (Set a flag, unhide the translation UI, etc.)
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be downloaded or other internal error.
                                // ...
                                Toast.makeText(getApplicationContext(), "Unable to download the model", Toast.LENGTH_SHORT).show();
                            }
                        });


        translator.translate(textToTranslate)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String translatedText) {
                        textViewTranslatedText.setText(translatedText);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        textViewTranslatedText.setText("Error: " + e.getMessage());
                    }
                });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        curLan = langMap.get(languages[i]);
//        Toast.makeText(getApplicationContext(), languages[i], Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == EXTERNAL_STORAGE_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start the camera
                loadGalleryImage();
            } else {
                // Permission denied, show an error message
                Toast.makeText(this, "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show();
            }
        }
    }
}