package com.example.fittune.ui.Dashboard;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fittune.Adapter.ExerciseblockAdapter;
import com.example.fittune.Dialog_Edit;
import com.example.fittune.Dialog_chooseScenario;
import com.example.fittune.Model.ExerciseBlock;
import com.example.fittune.Model.ExerciseStats;
import com.example.fittune.MainActivity;
import com.example.fittune.Service.ExerciseService;
import com.example.fittune.Service.MusicService;
import com.example.fittune.R;
import com.example.fittune.Model.Userprofile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.math.Stats;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DashboardFragment extends Fragment  {


    private String userID;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
    private FirebaseUser mUser;

    String timeStamp;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private StorageTask mUploadTask;
    private UploadTask uploadTask;


    private DashboardViewModel dashboardViewModel;
    private TextView Taptostart,speed;
    private static boolean Flag=true;

    DecimalFormat decimalFormat =new DecimalFormat("0.00");
    DecimalFormat onedecimalFormat=new DecimalFormat("0.0");


    private Button pause,stop;
    private SeekBar speed_seekbar;
    private float  seedseekbarvalue;

    private Handler updateHandler=new Handler();
    private Handler CalculateAverageSpeed=new Handler();


    private MusicService musicService;
    boolean isMusicBind=false;
    private ExerciseService exerciseService;
    boolean isExerciseBind=false;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};


    private boolean running=false;

    private String musicstyle="Rock";

    private Boolean isdistance=false,isfatburning=false,ispace=false,isduration=false;
    private Boolean isedm=false,isrock=false,ispop=false;

    RecyclerView exercise_block;
    ExerciseblockAdapter exerciseAdapter;
    private List<ExerciseBlock> eblock;

    private List<String> averagespeedtenseconds=new ArrayList<>();
    private ArrayList<Double> speedtemp=new ArrayList<>();

    Integer flag;

    //Music Service
    private ServiceConnection scmusic = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicService = ((MusicService.MyBinder) iBinder).getService();
            isMusicBind=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isMusicBind=false;
            musicService = null;

        }
    };

    //Exercise Service
    private ServiceConnection scexercise=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ExerciseService.LocalService localService=(ExerciseService.LocalService)service;
            exerciseService=localService.getService();
            isExerciseBind=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            isExerciseBind=false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        //Firebase Setup
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userID = mUser.getUid();
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        firestoreDB = FirebaseFirestore.getInstance();


        SendFlagtoActivity(Flag);

        Taptostart = root.findViewById(R.id.text_taptostart);


        exercise_block=root.findViewById(R.id.block);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity()){
            //禁止水平滑动
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        exercise_block.setLayoutManager(manager);
        exercise_block.getItemAnimator().setChangeDuration(0);

        SetChoicevalue("Distance,Fat Burning",musicstyle,true);

        speed=root.findViewById(R.id.speedkm);


        pause=root.findViewById(R.id.Pause);
        stop=root.findViewById(R.id.Finish);

        speed_seekbar=root.findViewById(R.id.speedbar);

        Log.d("InstanceState","OncreateView");
        bindServiceConnection();
        musicService = new MusicService();
        exerciseService=new ExerciseService();

        dashboardViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });


        //Edit
        root.findViewById(R.id.Edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog_Edit dialog_edit=new Dialog_Edit(isdistance,isfatburning,ispace,isduration,isedm,isrock,ispop);

                dialog_edit.setdoneOnclickListener(new Dialog_Edit.onDoneOnclickListener() {
                    @Override
                    public void onDoneClick(StringBuilder sb,StringBuilder music) {
                        String [] temp = null;
                        String exercisetemps = sb.toString();
                        musicstyle=music.toString();
                        musicService.musicStyle=musicstyle;
                        if(exerciseService.exerciseTypeFlag ==1){
                            musicService.changemusic(musicService.musicflago);
                        }else {
                            musicService.changemusic(flag);
                        }

                        SetChoicevalue(exercisetemps,musicstyle,false);
                        dialog_edit.dismiss();
                    }
                });
                dialog_edit.show(getActivity().getSupportFragmentManager(),"Edit");

            }
        });

        //Tap to Start
        root.findViewById(R.id.text_taptostart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Flag1",String.valueOf(Flag));
                final Dialog_chooseScenario dialog_chooseScenario=new Dialog_chooseScenario();
                dialog_chooseScenario.show(getActivity().getSupportFragmentManager(),"Taptostart");
                dialog_chooseScenario.setoutdoorOnclickListener(new Dialog_chooseScenario.onOutdoorOnclickListener() {
                    @Override
                    public void onOutdoorClick() {
                        musicService.initMediaPlayer();
                        exerciseService.exerciseTypeFlag = 1;
                        musicService.exercisetype=1;
                        //musicService.isbpmdetected=true;
                        Flag=false;
                        SendFlagtoActivity(Flag);
                        //Taptostart.setText("0 BPM");
                        Taptostart.setText("0 \n BPM");
                        musicService.playOrPause();

                        exerciseService.updateRunnable.run();
                        updatedatarunnable.run();
                        calculateaveragespeed.run();

                        Taptostart.setEnabled(false);

                        running=true;
                        pause.setVisibility(View.VISIBLE);
                        pause.setText("Pause");
                        stop.setVisibility(View.VISIBLE);
                        dialog_chooseScenario.dismiss();
                    }
                });
                dialog_chooseScenario.setTreadmillOnclickListener(new Dialog_chooseScenario.onTreadmillOnclickListener() {
                    @Override
                    public void ontreadmillClick() {

                        musicService.initMediaPlayer();
                        exerciseService.exerciseTypeFlag = 2;
                        musicService.exercisetype=2;
                        Flag=false;
                        SendFlagtoActivity(Flag);


                        musicService.playOrPause();

                        Taptostart.setEnabled(false);
                        Taptostart.setText("");

                        speed.setVisibility(View.VISIBLE);
                        speed_seekbar.setVisibility(View.VISIBLE);
                        speed_seekbar.setEnabled(true);
                        speed_seekbar.setProgress(0);

                        pause.setVisibility(View.VISIBLE);
                        stop.setVisibility(View.VISIBLE);
                        pause.setEnabled(true);
                        stop.setEnabled(true);

                        exerciseService.updateRunnable.run();
                        updatedatarunnable.run();
                        calculateaveragespeed.run();

                        dialog_chooseScenario.dismiss();
                    }
                });
            }

        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePlay();
                musicService.playOrPause();
                exerciseService.PlayorPause();

            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pause.getText()=="Pause"){
                    changePlay();
                }
                //
                double temp = Math.round(exerciseService.getTotaldistance()*100.0)/100.0;
                //float temp = totaldistance;
                Map<String, Object> note = new HashMap<>();
                note.put("duration", String.valueOf(temp));
                firestoreDB.collection("Users").document(userID).update(note);
                //update stats
                updateStats(temp);
                //upload stats
                uploadStats(averagespeedtenseconds);

                //todo: Upload average Speed


                averagespeedtenseconds.clear();

                Flag=true;
                SendFlagtoActivity(Flag);

                pause.setEnabled(false);
                stop.setEnabled(false);
                musicService.exercisetype=0;
                if(musicService.exercisetype==1){
                    Taptostart.setText(String.valueOf(0)+"\nBPM");
                }

                musicService.stop();
                exerciseService.stop();

                updateHandler.removeCallbacks(updatedatarunnable);
                CalculateAverageSpeed.removeCallbacks(calculateaveragespeed);

                speed_seekbar.setProgress(0);
                speed_seekbar.setEnabled(false);

            }
        });

        speed_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seedseekbarvalue=progress/10f;
                exerciseService.seedseekbarvalue=seedseekbarvalue;
                 flag=0;
                speed.setText(Float.toString(seedseekbarvalue)+" Km/h");
                //change music
                if(!Flag){
                    if(seedseekbarvalue<=6&&seedseekbarvalue>=0){
                        flag=1;
                        musicService.musicflago=1;
                        if(!musicService.currentsong.equals(1)){
                            musicService.changemusic(flag);
                        }else {
                            musicService.change_music_speed(seedseekbarvalue,6,2);
                        }
                    }else if(seedseekbarvalue<=12&&seedseekbarvalue>6){
                        flag=2;
                        musicService.musicflago=2;
                        if(!musicService.currentsong.equals(2)){
                            musicService.changemusic(flag);
                        }else {
                            musicService.change_music_speed(seedseekbarvalue,12,2);
                        }
                    }else{
                        flag=3;
                        musicService.musicflago=3;
                        if(!musicService.currentsong.equals(3)){
                            musicService.changemusic(flag);
                        }else {
                            musicService.change_music_speed(seedseekbarvalue,15,2);
                        }
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        return root;
    }

    private void bindServiceConnection() {
        //Bind Music Service
        Intent intentmu = new Intent(getActivity(), MusicService.class);
        getActivity().bindService(intentmu, scmusic, getActivity().BIND_AUTO_CREATE);

        //Bind Exercise Service
        Intent intentex=new Intent(getActivity(), ExerciseService.class);
        getActivity().bindService(intentex,scexercise,getActivity().BIND_AUTO_CREATE);

    }


    //Update Data
    private Runnable updatedatarunnable=new Runnable() {
        @Override
        public void run() {
            int length=exerciseAdapter.getItemCount();
            if(length>1){
                for(int i=1;i<length;i++){
                    UpdateValue(i);
                }
            }

            if(musicService.exercisetype==1){
                int cadence=musicService.cadence;
                if(pause.getText()!="Play"){
                    Integer currentcadence=musicService.cadence;
                    Double currentspeed=currentcadence*0.8*60/1000;
                    exerciseService.currentspeed=currentspeed;
                    speedtemp.add((double) currentspeed);
                }else {
                    speedtemp.add(0.0);
                }

                Taptostart.setText(String.valueOf(cadence)+"\nBPM");
            }
            if(musicService.exercisetype==2){
                if(pause.getText()!="Play"){
                    speedtemp.add((double) seedseekbarvalue);
                }else {
                    speedtemp.add(0.0);
                }

            }

            updateHandler.postDelayed(this,100);
        }
    };

    //todo: Save average speed in ten seconds
    //Calculate average speed in ten seconds
    private Runnable calculateaveragespeed=new Runnable() {
        @Override
        public void run() {
            if(!speedtemp.isEmpty()){
                double averagespeed= Stats.meanOf(speedtemp);
                speedtemp.clear();
                String speed=decimalFormat.format(averagespeed);
                averagespeedtenseconds.add(speed);
                Log.d("Averagespeed",speed);
            }

            CalculateAverageSpeed.postDelayed(this,5000);
        }
    };


    private void UpdateValue(int i){

        if(exerciseAdapter.getItemCount()>1) {
            String name = exerciseAdapter.getItemName(i);
            switch (name) {
                case "Distance":
                    String distanceString = decimalFormat.format(exerciseService.getTotaldistance()) + "Km";
                    eblock.get(i).setValue(distanceString);
                    exerciseAdapter.notifyItemChanged(i);
                    break;
                case "Fat Burning":
                    String kcalString = onedecimalFormat.format(exerciseService.getTotalkcal()) + "kcal";
                    eblock.get(i).setValue(kcalString);
                    exerciseAdapter.notifyItemChanged(i);
                    break;
                case "Pace":
                    eblock.get(i).setValue(exerciseService.getCurrentpace());
                    exerciseAdapter.notifyItemChanged(i);
                    break;
                case "Duration":
                    String duration = exerciseService.getTotalsecond();
                    eblock.get(i).setValue(duration);
                    exerciseAdapter.notifyItemChanged(i);
                    break;
            }
        }
    }

    private void updateStats(final double distanceIncrement){

        firestoreDB.collection("Users").document(userID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document=task.getResult();
                            if(document.exists()){
                                Userprofile profile=document.toObject(Userprofile.class);

                                Calendar calendar = Calendar.getInstance();
                                int day = calendar.get(Calendar.DAY_OF_WEEK);
                                double updated_distance;
                                if (day == Calendar.MONDAY){
                                    updated_distance = distanceIncrement + Float.valueOf(profile.getMon());
                                    updated_distance = Math.round(updated_distance * 100.0) / 100.0;
                                    Map<String, Object> note = new HashMap<>();
                                    note.put("mon", String.valueOf(updated_distance));
                                    firestoreDB.collection("Users").document(userID).update(note);
                                }
                                if (day == Calendar.TUESDAY){
                                    updated_distance = distanceIncrement + Float.valueOf(profile.getTue());
                                    updated_distance = Math.round(updated_distance * 100.0) / 100.0;
                                    Map<String, Object> note = new HashMap<>();
                                    note.put("tue", String.valueOf(updated_distance));
                                    firestoreDB.collection("Users").document(userID).update(note);
                                }
                                if (day == Calendar.WEDNESDAY){
                                    updated_distance = distanceIncrement + Float.valueOf(profile.getWed());
                                    updated_distance = Math.round(updated_distance * 100.0) / 100.0;
                                    Map<String, Object> note = new HashMap<>();
                                    note.put("wed", String.valueOf(updated_distance));
                                    firestoreDB.collection("Users").document(userID).update(note);
                                }
                                if (day == Calendar.THURSDAY){
                                    updated_distance = distanceIncrement + Float.valueOf(profile.getThu());
                                    updated_distance = Math.round(updated_distance * 100.0) / 100.0;
                                    Map<String, Object> note = new HashMap<>();
                                    note.put("thu", String.valueOf(updated_distance));
                                    firestoreDB.collection("Users").document(userID).update(note);
                                }
                                if (day == Calendar.FRIDAY){
                                    updated_distance = distanceIncrement + Float.valueOf(profile.getFri());
                                    updated_distance = Math.round(updated_distance * 100.0) / 100.0;
                                    Map<String, Object> note = new HashMap<>();
                                    note.put("fri", String.valueOf(updated_distance));
                                    firestoreDB.collection("Users").document(userID).update(note);
                                }
                                if (day == Calendar.SATURDAY){
                                    updated_distance = distanceIncrement + Float.valueOf(profile.getSat());
                                    updated_distance = Math.round(updated_distance * 100.0) / 100.0;
                                    Map<String, Object> note = new HashMap<>();
                                    note.put("sat", String.valueOf(updated_distance));
                                    firestoreDB.collection("Users").document(userID).update(note);
                                }
                                if (day == Calendar.SUNDAY){
                                    updated_distance = distanceIncrement + Float.valueOf(profile.getSun());
                                    updated_distance = Math.round(updated_distance * 100.0) / 100.0;
                                    Map<String, Object> note = new HashMap<>();
                                    note.put("sun", String.valueOf(updated_distance));
                                    firestoreDB.collection("Users").document(userID).update(note);
                                }
                                double totalUpdatedDistance = 0;
                                totalUpdatedDistance += Float.valueOf(profile.getMon())+Float.valueOf(profile.getTue())+
                                        Float.valueOf(profile.getWed())+Float.valueOf(profile.getThu())+Float.valueOf(profile.getFri())+
                                        Float.valueOf(profile.getSat())+Float.valueOf(profile.getSun());
                                totalUpdatedDistance = Math.round(totalUpdatedDistance * 100.0) / 100.0;
                                Map<String, Object> note = new HashMap<>();
                                note.put("distance", String.valueOf(totalUpdatedDistance));
                                firestoreDB.collection("Users").document(userID).update(note);

                                /////////////////////////
                                //double savedistance = Float.valueOf(profile.getDuration()) + Float.valueOf(profile.getDistance());
                                //savedistance = Math.round(savedistance* 100.0) / 100.0;
                                //Map<String, Object> note = new HashMap<>();
                                //note.put("distance", String.valueOf(savedistance));
                                //firestoreDB.collection("Users").document(userID).update(note);
                                /////////////////////////
                            }else{
                            }
                        }else{
                        }
                    }
                });
    }

    private void uploadStats(final List<String> averagespeedtenseconds){
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String exerciseType ="";
        if(exerciseService.exerciseTypeFlag==1){
            exerciseType = "outdoor";
        }else if(exerciseService.exerciseTypeFlag==2){
            exerciseType = "treadmill";
        }
        double distance = Math.round(exerciseService.getTotaldistance()*1000.0)/1000.0;
        double calories = Math.round(exerciseService.getTotalkcal()*1000.0)/1000.0;

        DocumentReference docRef = firestoreDB.collection("Exercise").document();
        ExerciseStats upload = new ExerciseStats(userID, exerciseType, distance, exerciseService.getTotalsecond(),
                exerciseService.getCurrentpace(), calories, timeStamp, docRef.getId(), averagespeedtenseconds);
        docRef.set(upload);
        Toast.makeText(getActivity(), "Exercise Stats Uploaded to Database!", Toast.LENGTH_LONG).show();
    }



    private void SetChoicevalue(String temps, String Musicstyle, Boolean init){

        ispop=false;isrock=false;isedm=false;
        isduration=false;isdistance=false;isfatburning=false;ispace=false;
        if(init){
            if(Musicstyle.isEmpty()){
                Musicstyle="EDM";
            }
            if(temps.isEmpty()){
                temps="Distance,Fat Burning";
            }
        }else {
            if(Musicstyle.isEmpty()){
                Musicstyle=exerciseAdapter.getItemValue(0);
            }
            if(temps.isEmpty()){
                temps=",";
            }
        }
        eblock=new ArrayList<>();
        ExerciseBlock etemp=new ExerciseBlock();
        etemp.setname("Music Style");
        etemp.setValue(Musicstyle);
        eblock.add(etemp);

        switch (Musicstyle){
            case "EDM":
                isedm=true;
                break;
            case "Rock":
                isrock=true;
                break;
            case  "Pop":
                ispop=true;
                break;
        }

        if(temps!=","){
            String[] label=temps.split(",");
            for(int i=0;i<label.length;i++){
                etemp=getspecificlabel(label[i],init);
                eblock.add(etemp);
            }
        }

        exerciseAdapter=new ExerciseblockAdapter(getActivity(),eblock);
        exercise_block.setAdapter(exerciseAdapter);

    }

    private ExerciseBlock getspecificlabel(String name,Boolean init){
        ExerciseBlock temp=new ExerciseBlock();

        if(init){
            switch (name) {
                case "Distance":
                    isdistance=true;
                    temp.setname("Distance");
                    String distanceString = "0.00Km";
                    temp.setValue(distanceString);
                    break;
                case "Fat Burning":
                    isfatburning=true;
                    temp.setname("Fat Burning");
                    String kcalString = "0.0kcal";
                    temp.setValue(kcalString);
                    break;
                case "Pace":
                    ispace=true;
                    temp.setname("Pace");
                    temp.setValue("0'00''");
                    break;
                case "Duration":
                    isduration=true;
                    temp.setname("Duration");
                    String duration = "00:00:00";
                    temp.setValue(duration);
                    break;
            }

        }else {
            switch (name){
                case "Distance":
                    isdistance=true;
                    temp.setname("Distance");
                    String distanceString = decimalFormat.format(exerciseService.getTotaldistance()) + "Km";
                    temp.setValue(distanceString);
                    break;
                case "Fat Burning":
                    isfatburning=true;
                    temp.setname("Fat Burning");
                    String kcalString = onedecimalFormat.format(exerciseService.getTotalkcal()) + "kcal";
                    temp.setValue(kcalString);
                    break;
                case "Pace":
                    ispace=true;
                    temp.setname("Pace");
                    temp.setValue(exerciseService.getCurrentpace());
                    break;
                case "Duration":
                    isduration=true;
                    temp.setname("Duration");
                    String duration=exerciseService.getTotalsecond();
                    temp.setValue(duration);
                    break;
            }

        }

        return temp;
    }


    private void changePlay() {

        if(musicService.mediaPlayer.isPlaying()){

            pause.setText("Play");
            //animator.pause();
        } else {
            pause.setText("Pause");

        }
    }

    private void SendFlagtoActivity(Boolean Flag){
        MainActivity mainActivity= (MainActivity) getActivity();
        mainActivity.getsignalfromdashboard(Flag);
    }




    @Override
    public void onResume(){
        super.onResume();
        Log.d("InstanceState","onResume");
    }

    @Override
    public void onPause(){
        super.onPause();
        running=false;
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.d("InstanceState","onStop");
        if(Flag){
            Log.d("InstanceState","onStopFlag=true");
            if(isMusicBind){
                Log.d("InstanceState","unbindmusicService");
                Log.d("InstanceStateflag",String.valueOf(Flag));
                // Log.d("U","Success unbindmusciservice in Destroy");
                getActivity().unbindService(scmusic);
                isMusicBind=false;
            }
            if(isExerciseBind){
                Log.d("InstanceState","unbindexweciseService");
                Log.d("InstanceStateflag",String.valueOf(Flag));
                getActivity().unbindService(scexercise);
                isExerciseBind=false;
            }
        }

    }

    @Override
    public void onDestroy() {
        Log.d("InstanceState","onDestroy");
        //getActivity().unbindService(scmusic);
        // Log.d("U","Fragment in Destroy");

        super.onDestroy();
    }

}
