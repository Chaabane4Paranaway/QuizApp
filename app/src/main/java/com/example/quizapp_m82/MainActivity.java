package com.example.quizapp_m82;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.content.pm.PackageManager;
public class MainActivity extends AppCompatActivity {
    //Step 1: Declaration
    EditText etLogin, etPassword;
    Button bLogin;
    TextView tvRegister;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
    private boolean mLocationPermissionGranted;


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
                    String locationFileName = login.toLowerCase()+"_StartLocation("+ System.currentTimeMillis() + ").txt";
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
        setContentView(R.layout.activity_main);

        //Step 2: Recuperation des ids
        etLogin = (EditText) findViewById(R.id.etMail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        bLogin = (Button) findViewById(R.id.bLogin);
        tvRegister = (TextView) findViewById(R.id.tvRegister);
        //Step 3: Association de listeners


// Dans la méthode onCreate() ou onStart() de votre activité:
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }

        // Demander l'autorisation de la caméra si elle n'a pas encore été accordée
        ActivityCompat.requestPermissions(this, permissions, CAMERA_PERMISSION_REQUEST_CODE);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Step 4: Traitement
                String email = etLogin.getText().toString();
                String mdp = etPassword.getText().toString();

                if (email.isEmpty() || mdp.isEmpty())
                {
                    Toast.makeText(MainActivity.this, "Veuillez entrer les informations !", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(mdp.length()<6)
                    {
                        Toast.makeText(MainActivity.this, "Veuillez entrer un mot de passe d'au moins 6 caractères !", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    auth.signInWithEmailAndPassword(email,mdp)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    String login = email.toUpperCase().substring(0,email.indexOf('@'));
                                    Toast.makeText(MainActivity.this, "Welcome back "+login, Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this,quiz1.class);

                                    // Vérifier si l'application a la permission de localisation
                                    if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                                            == PackageManager.PERMISSION_GRANTED) {
                                        // Demander les mises à jour de localisation à l'aide du système de localisation par défaut d'Android
                                        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                    } else {
                                        // Si l'application n'a pas la permission de localisation, la demander
                                        ActivityCompat.requestPermissions(MainActivity.this, new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                                    }
                                    uploadLastKnownLocationToFirebase(login);
                                    intent.putExtra("login",login);
                                    startActivity(intent);
                                    //startActivity(new Intent(MainActivity.this,quiz_dynamique.class));
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(),"Login failed !",Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Step 4: Traitement

                startActivity(new Intent(MainActivity.this, register.class));
            }
        });
    }
}