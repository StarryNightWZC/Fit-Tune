package com.example.fittune.ui.Myaccount;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fittune.Model.ExerciseStats;
import com.example.fittune.LoginActivity;
import com.example.fittune.R;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyaccountFragment extends Fragment implements MyaccountViewAdapter.OnPicListener{

    private MyaccountViewModel notificationsViewModel;
    private static final String TAG = "MyaccountFragment";

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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                ViewModelProviders.of(this).get(MyaccountViewModel.class);
        View root = inflater.inflate(R.layout.fragment_myaccount, container, false);

        mUploads = new ArrayList<>();

        recyclerView = root.findViewById(R.id.exerciseRecyclerView);
        layoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userID = mUser.getUid();
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        firestoreDB = FirebaseFirestore.getInstance();

        progressBar = root.findViewById(R.id.progressBar);

        mAdapter = new MyaccountViewAdapter(mUploads,this);
        recyclerView.setAdapter(mAdapter);

        final Button button = root.findViewById(R.id.backtoSignIn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });

        loadGallery();
        return root;
    }
    private void loadGallery() {
        Query photoTimeOrderDescend = firestoreDB.collection("Exercise")
                .orderBy("timeStamp", Query.Direction.DESCENDING);
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
        String docRef = mUploads.get(position).getDocRef();
        Intent intent = new Intent(getActivity(), MyaccountStatsActivity.class);
        intent.putExtra("docRef", docRef);
        startActivity(intent);
    }
}
