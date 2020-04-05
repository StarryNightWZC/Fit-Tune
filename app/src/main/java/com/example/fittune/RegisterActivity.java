package com.example.fittune;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.fittune.Model.UploadFile;
import com.example.fittune.Model.Userprofile;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final String KEY_NAME = "name";
    private static final String KEY_BIO = "bio";
    private static final String KEY_DIST = "distance";
    private static final String KEY_UID = "UID";
    private static final String KEY_PICPATH = "displayPicPath";

    //private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int PICK_IMAGE_REQUEST = 2;

    private TextInputLayout inputUsername;
    private TextInputLayout inputEmail;
    private TextInputLayout inputPassword;
    private TextInputLayout inputConfirmPW;
    private TextInputLayout inputBio;
    private ProgressBar progressBar;

    private String userID;
    private String mCurrentPhotoPath;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
    private FirebaseUser mUser;

    CircleImageView Profile;
    Bitmap bitmapOriginal;
    Bitmap bitmapThumbNail;
    Uri mImageUri;
    Uri mImageUriAvatar;
    String timeStamp;
    private File storageDir;

    private String userName;
    private String userBio;
    private String dist;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private StorageTask mUploadTask;
    private UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputUsername = findViewById(R.id.username);
        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        inputConfirmPW = findViewById(R.id.confirmpassword);
        inputBio = findViewById(R.id.bio);

        progressBar = findViewById(R.id.progressBar);
        Profile = findViewById(R.id.profile_image);

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dispatchTakePictureIntent();
                openFileChooser();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
            //ThumbNail
            bitmapThumbNail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(mCurrentPhotoPath), 100, 100);
            Profile.setImageBitmap(bitmapThumbNail);

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.outWidth = 1024;
            bmOptions.outHeight = 1024;
            bitmapOriginal = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

            String fname = timeStamp +".jpg";

            File image = new File(storageDir, fname);
            if (image.exists()) image.delete ();
            try {
                FileOutputStream out = new FileOutputStream(image);
                bitmapOriginal.compress(Bitmap.CompressFormat.JPEG, 50, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mImageUri = FileProvider.getUriForFile(this,
                    "com.example.fittune",
                    image);

            Profile.setImageBitmap(bitmapOriginal);
        }
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUriAvatar = data.getData();

            Picasso.get().load(mImageUriAvatar).into(Profile);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        // if user logged in, go to sign-in screen
        //TODO register onstart to Mainactivity, might need to change to SignedinActivity
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

    private boolean validateUsername() {
        String username = inputUsername.getEditText().getText().toString().trim();
        if (username.isEmpty()) {
            inputUsername.setError("Field can't be empty");
            return false;
        } else {
            inputUsername.setError(null);
            return true;
        }
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

    private boolean confirmPassword() {
        String password = inputPassword.getEditText().getText().toString().trim();
        String confrimpassword = inputConfirmPW.getEditText().getText().toString().trim();

        if (confrimpassword.isEmpty()){
            inputConfirmPW.setError("Field can't be empty");
            return false;
        }else if (!password.equals(confrimpassword)) {
            inputConfirmPW.setError("Password don't match");
            return false;
        } else {
            inputPassword.setError(null);
            return true;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(RegisterActivity.this, "Failed to create image file", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.fittune",
                        photoFile);
                takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


    public void firebaseStore(String username, String bio){
        mUser = mAuth.getCurrentUser();
        if (mUser != null) {
            userID = mUser.getUid();
        }else{
            Toast.makeText(RegisterActivity.this, "Authentication Failed." ,Toast.LENGTH_LONG).show();
        }
        uploadFile(username, bio);
    }

    public void onRegisterClicked(View view) {
        if (!validateUsername() | !validateEmail() | !validatePassword() | !confirmPassword()) {
            return;
        }
        String email = inputEmail.getEditText().getText().toString().trim();
        String password = inputPassword.getEditText().getText().toString().trim();
        userName = inputUsername.getEditText().getText().toString().trim();
        userBio = inputBio.getEditText().getText().toString().trim();


        progressBar.setVisibility(View.VISIBLE);
        //create user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Create User Failure." + task.getException(),
                                    Toast.LENGTH_LONG).show();
                            Log.e("Tag", task.getException().toString());
                        } else {
                            Toast.makeText(RegisterActivity.this, "Create User Successful.", Toast.LENGTH_LONG).show();
                            firebaseStore(userName, userBio);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                }
                            }, 1000);
                            //TODO after create new user to SignedInActivity, might need to change to MainActivity
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.putExtra("ProfilePic",bitmapThumbNail);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

    private void uploadFile(String username, String bio) {
        //Upload userinfo
        Map<String, Object> note = new HashMap<>();
        note.put(KEY_NAME, username);
        note.put(KEY_BIO, bio);
        note.put(KEY_DIST, "10");
        dist = "10";
        //Upload profile picture
        /*firestoreDB.collection("Users").document(userID).set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e(TAG, "User Info Upload Successful to Firebase");
                Toast.makeText(RegisterActivity.this, "Success upload info", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "User Info Upload Failed", e);
                Toast.makeText(RegisterActivity.this, "Failed upload info", Toast.LENGTH_SHORT).show();
            }
        });*/
        /*if (mImageUri != null) {
            final StorageReference fileReference = mStorageRef.child(userID).child("displayPic.jpg");
            uploadTask = fileReference.putFile(mImageUri);*/
        if (mImageUriAvatar != null) {
            final StorageReference fileReference = mStorageRef.child(userID).child("displayPic.jpg");
            uploadTask = fileReference.putFile(mImageUriAvatar);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        UploadFile upload = new UploadFile(downloadUri.toString(), timeStamp);
                        Userprofile userUpload = new Userprofile(userBio, userName, dist, userID ,downloadUri.toString());
                        firestoreDB.collection("Profile").document(userID).set(upload);
                        firestoreDB.collection("Users").document(userID).set(userUpload);
                    } else {
                        // Handle failures
                        Toast.makeText(RegisterActivity.this, "Failed to get photo downloadUrl", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }
}