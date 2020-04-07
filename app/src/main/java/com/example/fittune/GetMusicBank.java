package com.example.fittune;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GetMusicBank {

    private HashMap<Integer, List> EDM_Song_info=new HashMap<>();
    private HashMap<Integer,List> POP_Song_info=new HashMap<>();
    private HashMap<Integer,List> ROCK_Song_info=new HashMap<>();
    private StorageReference mMusicStorageRef;

    public HashMap<Integer, List> getEDM_Song_info() {
        return EDM_Song_info;
    }

    public HashMap<Integer, List> getPOP_Song_info() {
        return POP_Song_info;
    }

    public HashMap<Integer, List> getROCK_Song_info() {
        return ROCK_Song_info;
    }

    public GetMusicBank() {
        MusicBank();
    }

    private void MusicBank() {
        mMusicStorageRef= FirebaseStorage.getInstance().getReference("MusicBank");

        ///////EDM
        mMusicStorageRef.child("EDM/1").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                EDM_Song_info.put(1,new ArrayList());
                for(final StorageReference fileReference: listResult.getItems()){
                    //String test=fileReference.getName();
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("Test",uri.toString());
                            EDM_Song_info.get(1).add(uri.toString());

                        }

                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                // Handle any errors
                Log.d("Test","Fail");
            }
        });

        mMusicStorageRef.child("EDM/2").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                EDM_Song_info.put(2,new ArrayList());
                for(final StorageReference fileReference: listResult.getItems()){
                    //String test=fileReference.getName();
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("Test",uri.toString());
                            EDM_Song_info.get(2).add(uri.toString());

                        }

                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                // Handle any errors
                Log.d("Test","Fail");
            }
        });
        mMusicStorageRef.child("EDM/3").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                EDM_Song_info.put(3,new ArrayList());
                for(final StorageReference fileReference: listResult.getItems()){
                    //String test=fileReference.getName();
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("Test",uri.toString());
                            EDM_Song_info.get(3).add(uri.toString());

                        }

                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                // Handle any errors
                Log.d("Test","Fail");
            }
        });

        ///////ROCK
        mMusicStorageRef.child("ROCK/1").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                ROCK_Song_info.put(1,new ArrayList());
                for(final StorageReference fileReference: listResult.getItems()){
                    //String test=fileReference.getName();
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("Test",uri.toString());
                            ROCK_Song_info.get(1).add(uri.toString());

                        }

                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                // Handle any errors
                Log.d("Test","Fail");
            }
        });

        mMusicStorageRef.child("ROCK/2").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
               ROCK_Song_info.put(2,new ArrayList());
                for(final StorageReference fileReference: listResult.getItems()){
                    //String test=fileReference.getName();
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("Test",uri.toString());
                            ROCK_Song_info.get(2).add(uri.toString());

                        }

                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                // Handle any errors
                Log.d("Test","Fail");
            }
        });
        mMusicStorageRef.child("ROCK/3").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                ROCK_Song_info.put(3,new ArrayList());
                for(final StorageReference fileReference: listResult.getItems()){
                    //String test=fileReference.getName();
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("Test",uri.toString());
                            ROCK_Song_info.get(3).add(uri.toString());
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                // Handle any errors
                Log.d("Test","Fail");
            }
        });

        /////////POP

        mMusicStorageRef.child("POP/1").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                POP_Song_info.put(1,new ArrayList());
                for(final StorageReference fileReference: listResult.getItems()){
                    //String test=fileReference.getName();
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("Test",uri.toString());
                            POP_Song_info.get(1).add(uri.toString());

                        }

                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                // Handle any errors
                Log.d("Test","Fail");
            }
        });

        mMusicStorageRef.child("POP/2").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                POP_Song_info.put(2,new ArrayList());
                for(final StorageReference fileReference: listResult.getItems()){
                    //String test=fileReference.getName();
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("Test",uri.toString());
                            POP_Song_info.get(2).add(uri.toString());

                        }

                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                // Handle any errors
                Log.d("Test","Fail");
            }
        });
        mMusicStorageRef.child("POP/3").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                POP_Song_info.put(3,new ArrayList());
                for(final StorageReference fileReference: listResult.getItems()){
                    //String test=fileReference.getName();
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("Test",uri.toString());
                            POP_Song_info.get(3).add(uri.toString());
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                // Handle any errors
                Log.d("Test","Fail");
            }
        });

    }


}
