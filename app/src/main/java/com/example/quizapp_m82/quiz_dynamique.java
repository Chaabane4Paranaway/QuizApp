package com.example.quizapp_m82;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class quiz_dynamique extends AppCompatActivity {
    private TextView questionTextView;
    private RadioGroup answerRadioGroup;
    private RadioButton answerRadioButton1;
    private RadioButton answerRadioButton2;
    private RadioButton answerRadioButton3;
    private RadioButton answerRadioButton4;
    private Button nextButton;

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private int questionNumber;
    private int nbQuest;

    private String correctAnswer;
    private int score;
    public void uploadLastKnownLocationToFirebase(String login) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    // Utiliser la dernière localisation connue pour l'envoyer à Firebase Storage
                    double latitude = lastKnownLocation.getLatitude();
                    double longitude = lastKnownLocation.getLongitude();
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference().child(login.toLowerCase()+"");
                    String locationFileName = login.toLowerCase()+"_EndLocation("+ System.currentTimeMillis() + ").txt";
                    StorageReference locationRef = storageRef.child(locationFileName);
                    String locationStr = "Latitude: " + latitude + "\nLongitude: " + longitude;
                    byte[] locationBytes = locationStr.getBytes();
                    UploadTask uploadTask = locationRef.putBytes(locationBytes);
                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Location uploaded to Firebase Storage");
                            } else {
                                Log.e(TAG, "Error uploading location to Firebase Storage", task.getException());
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_dynamique);

        Intent intent=getIntent();
        score=intent.getIntExtra("score",0) ;
        ArrayList<String> array = intent.getStringArrayListExtra("log");
        questionNumber=1;
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

        // Récupération des vues du layout
        questionTextView = findViewById(R.id.question_textview);
        answerRadioGroup = findViewById(R.id.option_radiogroup);
        answerRadioButton1 = findViewById(R.id.option1_radiobutton);
        answerRadioButton2 = findViewById(R.id.option2_radiobutton);
        answerRadioButton3 = findViewById(R.id.option3_radiobutton);
        answerRadioButton4 = findViewById(R.id.option4_radiobutton);
        nextButton = findViewById(R.id.next_button);

        // Référence à la base de données Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference().child("questions");

        // Écouteur de valeur pour récupérer les informations de la question
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Récupération de la question et des réponses
                String question = dataSnapshot.child("question"+questionNumber).child("question").getValue().toString();
                String answer1 = dataSnapshot.child("question"+questionNumber).child("option1").getValue().toString();
                String answer2 = dataSnapshot.child("question"+questionNumber).child("option2").getValue().toString();
                String answer3 = dataSnapshot.child("question"+questionNumber).child("option3").getValue().toString();
                String answer4 = dataSnapshot.child("question"+questionNumber).child("option4").getValue().toString();
                nbQuest= Integer.parseInt(dataSnapshot.child("count").getValue().toString());

                // Attribution des valeurs aux vues du layout
                questionTextView.setText(question);
                answerRadioButton1.setText(answer1);
                answerRadioButton2.setText(answer2);
                answerRadioButton3.setText(answer3);
                answerRadioButton4.setText(answer4);

                // Récupération de la réponse correcte
                String posAnswer = dataSnapshot.child("question"+questionNumber).child("answer").getValue().toString();
                correctAnswer = dataSnapshot.child("question"+questionNumber).child("option"+posAnswer).getValue().toString();
                //Toast.makeText(quiz_dynamique.this, ""+correctAnswer, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException());
            }
        };
        databaseReference.addValueEventListener(valueEventListener);

        // Écouteur de clic pour le bouton "Next"
        String finalFormattedDateTime = formattedDateTime;
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Vérification de la réponse sélectionnée
                RadioButton selectedAnswer = findViewById(answerRadioGroup.getCheckedRadioButtonId());
                if (selectedAnswer == null) {
                    Toast.makeText(quiz_dynamique.this, "Veuillez sélectionner une réponse.", Toast.LENGTH_SHORT).show();
                } else {
                    // Vérification si la réponse est correcte
                    if (selectedAnswer.getText().toString().equals(correctAnswer)) {
                        score++;
                    }

                    if(questionNumber<nbQuest){
                        questionNumber++;
                        array.add(selectedAnswer.getText().toString());
                        databaseReference.addListenerForSingleValueEvent(valueEventListener);
                        answerRadioGroup.clearCheck();
                    }else {
                        // Passage à la question suivante
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
                        Intent intent = new Intent(quiz_dynamique.this, score.class);
                        intent.putExtra("score", score);
                        //intent.putExtra("log",intent.getStringArrayListExtra("log")+selectedAnswer.getText().toString());
                        //Toast.makeText(quiz_dynamique.this, ""+array.toString(), Toast.LENGTH_SHORT).show();

                        // Crée une référence à un fichier nommé "data.txt"
                        StorageReference dataRef = storageRef.child("LOG_"+ finalFormattedDateTime +".txt");
                        array.add(selectedAnswer.getText().toString());
                        // Convertit la chaîne de caractères en tableau de bytes
                        byte[] dataBytes = (array.toString()).getBytes();

                        // Envoie les données à Firebase Storage
                        dataRef.putBytes(dataBytes)
                                .addOnSuccessListener(taskSnapshot -> {
                                    // L'envoi des données est réussi
                                    Log.d(TAG, "Données envoyées avec succès.");
                                })
                                .addOnFailureListener(exception -> {
                                    // L'envoi des données a échoué
                                    Log.e(TAG, "Erreur lors de l'envoi des données : " + exception.getMessage());
                                });

                        intent.putExtra("nb", 5+nbQuest);
                        uploadLastKnownLocationToFirebase(getIntent().getStringExtra("login"));
                        startActivity(intent);
                        overridePendingTransition(R.anim.exit, R.anim.entry);
                        finish();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Suppression de l'écouteur de valeur pour éviter les fuites de mémoire
        databaseReference.removeEventListener(valueEventListener);
    }
}
