package com.example.fittune;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.example.fittune.Adapter.ProfileViewAdapter;
import com.example.fittune.Model.UploadFile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ExerciseStatsActivity extends AppCompatActivity implements ProfileViewAdapter.OnPicListener{

    private static final String TAG = "ExerciseStatsActivity";

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore firestoreDB;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private UploadTask uploadTask;

    private List<ExerciseStats> mUploads;
    private String userID;
    private String mCurrentPhotoPath;
    private String mPhotoPath;

    private TextView username;
    private TextView bio;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter mAdapter;

    Bitmap bitmap;
    CircleImageView Profile;

    private static final int REQUEST_TAKE_PHOTO = 1;
    private ProgressBar progressBar;
    Bitmap bitmapOriginal;
    Bitmap bitmapThumbNail;
    Uri mImageUri;
    String timeStamp;
    private File storageDir;

    private final TreeMap<String, UploadFile> Picinfo=new TreeMap<>(Collections.reverseOrder());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_stats);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mUploads = new ArrayList<>();

        recyclerView = findViewById(R.id.exerciseRecyclerView);
        layoutManager = new GridLayoutManager(ExerciseStatsActivity.this, 1);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userID = mUser.getUid();
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        firestoreDB = FirebaseFirestore.getInstance();

        progressBar = findViewById(R.id.progressBar);

        mAdapter = new ProfileViewAdapter(mUploads,this);
        recyclerView.setAdapter(mAdapter);

        loadGallery();

    }

    private void loadGallery() {
        //TODO photolist doesn't update coming back from photocaption

        Query photoTimeOrderDescend = firestoreDB.collection("Exercise")
                .orderBy("timeStamp", Query.Direction.DESCENDING);
        /*photoTimeOrderDescend.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    mUploads.clear();
                    for (QueryDocumentSnapshot document : task.getResult()){
                        UploadFile userPhoto = document.toObject(UploadFile.class);
                        mUploads.add(userPhoto);
                    }
                }else{
                    Log.d(TAG, "Error getting documents:", task.getException());
                }
                mAdapter = new GlobalViewAdapter(mUploads,onPicListener);
                recyclerView.setAdapter(mAdapter);
            }
        });*/
        //realtime updates
        photoTimeOrderDescend.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null){
                    return;
                }else{
                    mUploads.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()){
                        ExerciseStats userInfo = document.toObject(ExerciseStats.class);
                        mUploads.add(userInfo);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onPicClick(int position) {
        /*String commentPhotoUrl = mUploads.get(position).getStorageRef();
        String commentPhotoCaption = mUploads.get(position).getCaption();
        String photoId = mUploads.get(position).getPhotoId();
        Intent intent = new Intent(GlobalActivity.this, CommentsActivity.class);
        intent.putExtra("CommentPhotoUrl", commentPhotoUrl);
        intent.putExtra("CommentPhotoCaption", commentPhotoCaption);
        intent.putExtra("PhotoID", photoId);
        startActivity(intent);*/
    }
}
