package com.example.quizapp_m82;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class quiz5 extends AppCompatActivity {
    RadioGroup rg;
    RadioButton rb;
    Button bNext;
    int score;
    String RepCorrect="Extention du territoire";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz5);
        rg=(RadioGroup) findViewById(R.id.rg);
        bNext=(Button) findViewById(R.id.bNext);
        Intent intent=getIntent();
        score=intent.getIntExtra("score",0) ;
        ArrayList<String> array = intent.getStringArrayListExtra("log");
        // Initialisation du storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(getIntent().getStringExtra("login").toLowerCase()+"");

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
        String audioFileName = getIntent().getStringExtra("login")+"_"+getClass().getSimpleName()+"_"+formattedDateTime+".m4a";
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
        //Toast.makeText(getApplicationContext(),score+"",Toast.LENGTH_SHORT).show();
        bNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb=(RadioButton) findViewById(rg.getCheckedRadioButtonId());
                if(rg.getCheckedRadioButtonId()==-1){
                    Toast.makeText(getApplicationContext(),"Merci de choisir une réponse S.V.P !",Toast.LENGTH_SHORT).show();
                }
                else {
                    //Toast.makeText(getApplicationContext(),rb.getText().toString(),Toast.LENGTH_SHORT).show();
                    if(rb.getText().toString().equals(RepCorrect)){
                        score+=1;
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
                    Intent intent=new Intent(quiz5.this,quiz_dynamique.class);
                    intent.putExtra("score",score);
                    intent.putExtra("login",getIntent().getStringExtra("login"));
                    intent.putExtra("log",intent.getStringArrayListExtra("log")+rb.getText().toString());
                    //ArrayList<String> array = intent.getStringArrayListExtra("log");
                    ArrayList<String> log = new ArrayList<>();
                    for (String x:
                            array) {
                        log.add(x);
                    }
                    log.add(rb.getText().toString());
                    intent.putExtra("log",log);
                    startActivity(intent);
                    //overridePendingTransition(R.anim.fadein,R.anim.fadeout);
                    overridePendingTransition(R.anim.exit,R.anim.entry);
                    finish();
                }

            }
        });

    }
}
