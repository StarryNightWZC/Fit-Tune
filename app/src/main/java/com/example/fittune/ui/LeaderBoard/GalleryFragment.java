package com.example.fittune.ui.LeaderBoard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fittune.R;
import com.example.fittune.SignedInActivity;
import com.example.fittune.Model.UploadFile;
import com.example.fittune.Model.Userprofile;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class GalleryFragment extends Fragment implements GlobalViewAdapter.OnPicListener{

    private GalleryViewModel homeViewModel;
    private ImageView profileAlice;

    private static final String TAG = "GalleryFragment";

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore firestoreDB;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private UploadTask uploadTask;

    private List<Userprofile> mUploads;
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

    private final TreeMap<String,UploadFile> Picinfo=new TreeMap<>(Collections.reverseOrder());

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        mUploads = new ArrayList<>();

        recyclerView = root.findViewById(R.id.recyclerView);
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

        mAdapter = new GlobalViewAdapter(mUploads,this, userID);
        recyclerView.setAdapter(mAdapter);

        //TODO need to update change distance mechanism
        //setDistance();
        //setDistanceWeek();
        loadGallery();

        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
               // textView.setText(s);
            }
        });
        return root;



    }

    private void loadGallery() {
        Query photoTimeOrderDescend = firestoreDB.collection("Users")
                .orderBy("distance", Query.Direction.DESCENDING);
        //realtime updates
        photoTimeOrderDescend.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null){
                    return;
                }else{
                    mUploads.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()){
                        Userprofile userInfo = document.toObject(Userprofile.class);
                        mUploads.add(userInfo);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    public void backToSignedIn(View view) {
        startActivity(new Intent(getActivity(), SignedInActivity.class));
    }

    public void setDistance(){
        /*Map<String, Object> note = new HashMap<>();
        note.put("distance", "18.2");
        note.put("pace", "pace_placeholder");
        note.put("duration", "duration_placeholder");
        note.put("calories", "calories_placeholder");
        firestoreDB.collection("Users").document(userID).update(note);*/
    }

    public void setDistanceWeek(){
        Random rand = new Random();
        double min = 0.0;
        double max = 3.5;
        double randomdouble;

        Map<String, Object> note = new HashMap<>();

        randomdouble = min + rand.nextDouble() * (max - min);
        randomdouble = Math.round(randomdouble*100.0)/100.0;
        note.put("mon", Double.toString(randomdouble));

        randomdouble = min + rand.nextDouble() * (max - min);
        randomdouble = Math.round(randomdouble*100.0)/100.0;
        note.put("tue", Double.toString(randomdouble));

        randomdouble = min + rand.nextDouble() * (max - min);
        randomdouble = Math.round(randomdouble*100.0)/100.0;
        note.put("wed", Double.toString(randomdouble));

        randomdouble = min + rand.nextDouble() * (max - min);
        randomdouble = Math.round(randomdouble*100.0)/100.0;
        note.put("thu", Double.toString(randomdouble));

        randomdouble = min + rand.nextDouble() * (max - min);
        randomdouble = Math.round(randomdouble*100.0)/100.0;
        note.put("fri", Double.toString(randomdouble));

        randomdouble = min + rand.nextDouble() * (max - min);
        randomdouble = Math.round(randomdouble*100.0)/100.0;
        note.put("sat", Double.toString(randomdouble));

        randomdouble = min + rand.nextDouble() * (max - min);
        randomdouble = Math.round(randomdouble*100.0)/100.0;
        note.put("sun", Double.toString(randomdouble));

        firestoreDB.collection("Users").document(userID).update(note);
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

        //Intent intent = new Intent(getActivity(), ProfileActivity.class);
        //TODO might need to change back to ProfileActivity for demo
        Intent intent = new Intent(getActivity(), LeaderboardProfileActivity.class);
        startActivity(intent);
    }
}