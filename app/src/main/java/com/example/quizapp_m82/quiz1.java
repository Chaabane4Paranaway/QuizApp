package com.example.quizapp_m82;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.sql.Array;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.hardware.Camera.CameraInfo;


public class quiz1 extends AppCompatActivity {
    RadioGroup rg;
    RadioButton rb;
    Button bNext;
    int score = 0;
    ArrayList<String> log;
    String RepCorrect = "Non";
    private ImageReader mImageReader;
    //private CameraSize mPreviewSize;


    private boolean mIsBackPressed = false;
    @Override
    public void onStop() {
        super.onStop();

        if (!mIsBackPressed) {
            // L'utilisateur est revenu à l'application en utilisant l'affichage des applications récentes
            Toast.makeText(this, "STOP", Toast.LENGTH_SHORT).show();
            //moveTaskToBack(true);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz1);
        rg = (RadioGroup) findViewById(R.id.rg);
        bNext = (Button) findViewById(R.id.bNext);

        // Initialisation du storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(getIntent().getStringExtra("login").toLowerCase() + "");

        LocalDateTime currentDateTime = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentDateTime = LocalDateTime.now();
        }
        DateTimeFormatter formatter = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        }
        String formattedDateTime = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            formattedDateTime = currentDateTime.format(formatter);
        }

        System.out.println(formattedDateTime);

// Créer une référence pour le fichier audio de chaque activité
        String audioFileName = getIntent().getStringExtra("login") + "_" + getClass().getSimpleName() + "_" + formattedDateTime + ".m4a";
        StorageReference audioRef = storageRef.child(audioFileName);

// Créer un MediaRecorder pour l'enregistrement audio
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        String audioFilePath = getExternalCacheDir().getAbsolutePath() + "/" + audioFileName;
        recorder.setOutputFile(audioFilePath);
        try {
            recorder.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        recorder.start();


        bNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (rg.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(getApplicationContext(), "Merci de choisir une réponse S.V.P !", Toast.LENGTH_SHORT).show();
                } else {
                    rb = (RadioButton) findViewById(rg.getCheckedRadioButtonId());
                    //Toast.makeText(getApplicationContext(),rb.getText().toString(),Toast.LENGTH_SHORT).show();
                    if (rb.getText().toString().equals(RepCorrect)) {
                        score += 1;
                        //Toast.makeText(getApplicationContext(),score+"",Toast.LENGTH_SHORT).show();
                    }
                    recorder.stop();
                    recorder.release();
                    // Enregistrer le fichier audio sur Firebase Storage lorsque l'activité est terminée
                    Uri audioUri = Uri.fromFile(new File(audioFilePath));
                    audioRef.putFile(audioUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Le fichier audio a été enregistré avec succès sur Firebase Storage
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Une erreur s'est produite lors de l'enregistrement du fichier audio sur Firebase Storage
                        }
                    });

                    //capturePhoto();

                    log = new ArrayList<String>();
                    Intent intent = new Intent(quiz1.this, quiz2.class);
                    intent.putExtra("login", getIntent().getStringExtra("login"));
                    intent.putExtra("score", score);
                    log.add(rb.getText().toString());
                    intent.putExtra("log", log);
                    startActivity(intent);
                    //overridePendingTransition(R.anim.fadein,R.anim.fadeout);
                    overridePendingTransition(R.anim.exit, R.anim.entry);
                    finish();
                }

            }
        });

    }



}
