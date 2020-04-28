package com.example.fittune;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private TextInputLayout inputEmail;
    private TextInputLayout inputPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    Integer downloadFlag=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the view now
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        downloadFile("MusicBank","EDM/1");
        downloadFile("MusicBank","EDM/2");
        downloadFile("MusicBank","EDM/3");

        downloadFile("MusicBank","ROCK/1");
        downloadFile("MusicBank","ROCK/2");
        downloadFile("MusicBank","ROCK/3");

        downloadFile("MusicBank","POP/1");
        downloadFile("MusicBank","POP/2");
        downloadFile("MusicBank","POP/3");


    }

    private boolean validateEmail() {
        String email = inputEmail.getEditText().getText().toString().trim();

        if (email.isEmpty()) {
            inputEmail.setError("Field can't be empty");
            return false;
        } else {
            inputEmail.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String password = inputPassword.getEditText().getText().toString().trim();

        if (password.isEmpty()) {
            inputPassword.setError("Field can't be empty");
            return false;
        } else {
            inputPassword.setError(null);
            return true;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // if user logged in, go to sign-in screen
        //TODO LoginActivity onstart to SignedInActivity, might need to change to MainActivity
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    public void loginButtonClicked(View view) {
        if (!validateEmail() | !validatePassword()) {
            return;
        }
        String email = inputEmail.getEditText().getText().toString();
        String password = inputPassword.getEditText().getText().toString();
        progressBar.setVisibility(View.VISIBLE);

        //authenticate user
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Authentication failed." + task.getException(),
                                    Toast.LENGTH_LONG).show();
                            Log.e("MyTag", task.getException().toString());
                        } else {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }
    public void signupButtonClicked(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    private void downloadFile(String rootFolder, final String childFolder) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference(rootFolder);
        StorageReference  islandRef = storageRef.child(childFolder);


        final File rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath()+"/"+childFolder);
        if(!rootPath.exists()) {
            rootPath.mkdirs();
        }



        islandRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                int count=0;
                for(final StorageReference fileReference: listResult.getItems()){

                     File localFile=new File(rootPath,String.valueOf(count)+".mp3");
                     if (!localFile.exists()){
                         fileReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                             @Override
                             public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                 downloadFlag++;
                                 Toast.makeText(LoginActivity.this,childFolder+" download success",Toast.LENGTH_LONG).show();
                             }
                         }).addOnFailureListener(new OnFailureListener() {
                             @Override
                             public void onFailure(@NonNull Exception e) {
                                 Log.e("firebase ",";local tem file not created  created " +e.toString());
                             }
                         });
                         count++;
                     }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("firebase ",";local tem file not created  created " +e.toString());
            }
        });
    }

}
