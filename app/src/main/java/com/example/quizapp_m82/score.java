package com.example.quizapp_m82;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class score extends AppCompatActivity {
    Button bLogout, bTry;
    ProgressBar progressBar;
    TextView tvScore;
    private int nbrQuestion ;
    int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);


        tvScore =(TextView) findViewById(R.id.tvScore);
        progressBar=(ProgressBar) findViewById(R.id.progressBar);
        bLogout=(Button) findViewById(R.id.bLogout);
        bTry=(Button) findViewById(R.id.bTry);
        Intent intent=getIntent();
        score=intent.getIntExtra("score",0) ;
        nbrQuestion=intent.getIntExtra("nb",5);

        progressBar.setProgress(100*score/nbrQuestion);
        tvScore.setText(100*score/nbrQuestion+" %");
        //Toast.makeText(getApplicationContext(),nbrQuestion+"",Toast.LENGTH_SHORT).show();
        bLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Merci de votre Participation !", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        bTry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(score.this,quiz1.class));
            }
        });

    }
}
