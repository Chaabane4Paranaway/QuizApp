package com.example.quizapp_m82;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class register extends AppCompatActivity {
    EditText etMail, etPassword, etPassword1;
    Button bRegister;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    /* private final int REQUEST_IMAGE_CAPTURE = 1;



    private void prendrePhotoSelfie() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        private ActivityResultLauncher<Intent> cameraLauncher = null;
        if (intent.resolveActivity(getPackageManager()) != null) {
            //startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            cameraLauncher.Launch(intent);
            //registerForActivityResult(intent,REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("selfies/" + System.currentTimeMillis() + ".jpg");
            UploadTask uploadTask = storageRef.putBytes(imageData);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // La photo selfie a été envoyée avec succès à Firebase
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // La photo selfie n'a pas été envoyée à Firebase
                }
            });
        }
    }
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etMail=(EditText) findViewById(R.id.etMail);
        etPassword=(EditText) findViewById(R.id.etPassword);
        etPassword1=(EditText)findViewById(R.id.etPassword1);
        bRegister=(Button)findViewById(R.id.bRegister);
        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail=etMail.getText().toString();
                String password=etPassword.getText().toString();
                String password1=etPassword1.getText().toString();
                if(TextUtils.isEmpty(mail)){
                    Toast.makeText(getApplicationContext(),"Please fill in the required fields",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(getApplicationContext(),"Please fill in the required fields",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password1)){
                    Toast.makeText(getApplicationContext(),"Please confirm your password",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password.length()<6){
                    Toast.makeText(getApplicationContext(),"Password must be at least 6 characters",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!password.equals(password1)){
                    Toast.makeText(getApplicationContext(),"Please enter correct password",Toast.LENGTH_SHORT).show();
                    return;
                }
                auth.createUserWithEmailAndPassword(mail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {

                            //prendrePhotoSelfie();
                            // Créer une référence à la racine du stockage
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();

                            // Créer une référence à un dossier "myFolder"
                            StorageReference folderRef = storageRef.child(mail.substring(0,mail.indexOf('@')).toLowerCase()+"/");

                            // Créer une référence à un fichier nommé "myFile.txt" dans le dossier "myFolder"
                            StorageReference fileRef = folderRef.child("readme.txt");

                            // Convertir une chaîne de caractères en octets pour créer un fichier
                            byte[] fileData = "METADATA".getBytes();








                            // Envoyer les données du fichier sur Firebase Storage
                            fileRef.putBytes(fileData)
                                    .addOnSuccessListener(taskSnapshot -> {
                                        // Le fichier a été créé avec succès
                                        Log.d(TAG, "Fichier créé avec succès");
                                    })
                                    .addOnFailureListener(exception -> {
                                        // Une erreur s'est produite lors de la création du fichier
                                        Log.e(TAG, "Erreur lors de la création du fichier", exception);
                                    });


                            Toast.makeText(register.this, "You are now signed in !", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(register.this, MainActivity.class));
                            finish();
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"ERROR",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                //Commen.login=Mail;
                //Commen.password=password;
                /*Toast.makeText(getApplicationContext(),"Registration Successful!  inscription réussi! التسجيل ناجح! 註冊成功  ",Toast.LENGTH_LONG).show();
                startActivity(new Intent(register.this,MainActivity.class));
                finish();*/
            }
        });
    }
}
