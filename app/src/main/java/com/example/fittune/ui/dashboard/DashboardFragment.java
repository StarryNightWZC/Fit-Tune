package com.example.fittune.ui.dashboard;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
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
import androidx.core.app.ActivityCompat;
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

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import de.hdodenhof.circleimageview.CircleImageView;



public class DashboardFragment extends Fragment implements SensorEventListener {


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


    private MusicService musicService;
    boolean isMusicBind=false;
    private ExerciseService exerciseService;
    boolean isExerciseBind=false;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};


    //private SensorManager sensorManager;
    //private Sensor accSensor;
    //private Sensor accelerometer;

    private boolean running=false;

    private Boolean isdistance=false,isfatburning=false,ispace=false,isduration=false;
    private Boolean isedm=false,isclassic=false,ispop=false;



    RecyclerView exercise_block;
    ExerciseblockAdapter exerciseAdapter;
    private List<ExerciseBlock> eblock;





    private class Acceleration {
        public long timestamp;
        public float[] lowPassFilteredValues = new float[3];
        public float[] averagedValues = new float[3];

        @Override
        public String toString() {
            return String.format("Time,average,filtered,:,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f", timestamp,
                    averagedValues[0], averagedValues[1], averagedValues[2], lowPassFilteredValues[0],
                    lowPassFilteredValues[1], lowPassFilteredValues[2]);
        }
    }

    /**
     * Cutoff frequency (fc) in low-pass filter for foot fall detection.
     *
     * 3.5 * 60 = 210 footfalls/min
     */
    //private static final float FC_FOOT_FALL_DETECTION = 3.5F;

    /**
     * Cutoff frequency (fc) in low-pass filter for earth gravity detection
     */
   // private static final float FC_EARTH_GRAVITY_DETECTION = 0.25F;
   // private static final int ACCELERATION_VALUE_KEEP_SECONDS = 10;
   // private static final int NUMBER_OF_FOOT_FALLS = 10;
   // private static final long SECOND_TO_NANOSECOND = (long) 1e9;


    private boolean active = false;
   // private final LinkedList<Acceleration> values = new LinkedList<Acceleration>();

    ////////////////////////////////////////////////////

    //init rolling average storage
    //List<Float>[] rollingAverage = new List[3];
  //  private static final int MAX_SAMPLE_SIZE = 100;


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

        SetChoicevalue("Distance,Fat Burning","EDM",true);

        speed=root.findViewById(R.id.speedkm);


        pause=root.findViewById(R.id.Pause);
        stop=root.findViewById(R.id.Finish);

        speed_seekbar=root.findViewById(R.id.speedbar);

        //////////////////////////
        Log.d("InstanceState","OncreateView");
        bindServiceConnection();
        musicService = new MusicService();
        exerciseService=new ExerciseService();


        exerciseService.sensorManager=(SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        exerciseService.accSensor=exerciseService.sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        exerciseService.accelerometer = exerciseService.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        exerciseService.sensorManager.registerListener(this, exerciseService.accSensor, SensorManager.SENSOR_DELAY_NORMAL);
        exerciseService.sensorManager.registerListener(this, exerciseService.accelerometer, SensorManager.SENSOR_DELAY_GAME);




        //init rolling average for linear acceleration on xyz axis
       // rollingAverage[0] = new ArrayList<Float>();
       // rollingAverage[1] = new ArrayList<Float>();
       // rollingAverage[2] = new ArrayList<Float>();

        //todo need to remove note_temp
        //Map<String, Object> note_temp = new HashMap<>();
        //note_temp.put("distance", "18.2");
        //firestoreDB.collection("Users").document(userID).update(note_temp);

        dashboardViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });


        //Edit
        root.findViewById(R.id.Edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog_Edit dialog_edit=new Dialog_Edit(isdistance,isfatburning,ispace,isduration,isedm,isclassic,ispop);

                dialog_edit.setdoneOnclickListener(new Dialog_Edit.onDoneOnclickListener() {
                    @Override
                    public void onDoneClick(StringBuilder sb,StringBuilder music) {
                        String [] temp = null;
                        String exercisetemps = sb.toString();
                        String musictemps=music.toString();
                        SetChoicevalue(exercisetemps,musictemps,false);
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
                        exerciseService.exerciseTypeFlag = 1;
                        Flag=false;
                        SendFlagtoActivity(Flag);
                        //Taptostart.setText("0 BPM");
                        Taptostart.setText("0 \n BPM");
                        musicService.playOrPause();
                        exerciseService.updateRunnable.run();
                        updatedatarunnable.run();
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
                        exerciseService.exerciseTypeFlag = 2;
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
                uploadStats();

                Flag=true;
                SendFlagtoActivity(Flag);

                pause.setEnabled(false);
                stop.setEnabled(false);
                musicService.stop();
                exerciseService.stop();

                updateHandler.removeCallbacks(updatedatarunnable);
                speed_seekbar.setProgress(0);
                speed_seekbar.setEnabled(false);

            }
        });

        speed_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seedseekbarvalue=progress/10f;
                exerciseService.seedseekbarvalue=seedseekbarvalue;
                Integer flag=0;
                speed.setText("SPEED\n"+Float.toString(seedseekbarvalue)+" Km/h");
                //change music
                if(!Flag){
                    if(seedseekbarvalue<=6&&seedseekbarvalue>=0){
                        flag=1;
                        if(!musicService.currentsong.equals(1)){
                            musicService.changemusic(flag);
                        }else {
                            change_music_speed(seedseekbarvalue,6,2);
                        }
                    }else if(seedseekbarvalue<=12&&seedseekbarvalue>6){
                        flag=2;
                        if(!musicService.currentsong.equals(2)){
                            musicService.changemusic(flag);
                        }else {
                            change_music_speed(seedseekbarvalue,12,2);
                        }
                    }else{
                        flag=3;
                        if(!musicService.currentsong.equals(3)){
                            musicService.changemusic(flag);
                        }else {
                            change_music_speed(seedseekbarvalue,15,2);
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
/*
    //calculate rolling average
    public List<Float> roll(List<Float> list, float newMember){
        if(list.size() == MAX_SAMPLE_SIZE){
            list.remove(0);
        }
        list.add(newMember);
        return list;
    }

    public float averageList(List<Float> tallyUp){

        float total=0;
        for(float item : tallyUp ){
            total+=item;
        }
        total = total/tallyUp.size();

        return total;
    }*/

    private void change_music_speed(float currentspeed, float threshold,int scenario){
        if(scenario==1){
            if(currentspeed>threshold){
                float speed=(currentspeed-threshold)/currentspeed;
                speed= (float) (1.1+(float)(Math.round(speed*1000)/1000f));
                Log.d("Speed",String.valueOf(speed));
                musicService.changeplayerSpeed(speed);
            }
        }else {
            float diff=threshold-currentspeed;
            List<Float> speed_choice = new  ArrayList<Float>();
            speed_choice.add(1.1f);
            speed_choice.add(1.2f);
            speed_choice.add(1.3f);
            if (diff<=2&&diff>0){
                musicService.changeplayerSpeed(speed_choice.get(2));
            }else if(diff<=4&&diff>2){
                musicService.changeplayerSpeed(speed_choice.get(1));
            }else if(diff<=5&&diff>4) {
                musicService.changeplayerSpeed(speed_choice.get(0));
            }
        }

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
            updateHandler.postDelayed(this,1000);
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

    private void uploadStats(){
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
        ExerciseStats upload = new ExerciseStats(userID, exerciseType, distance,
                exerciseService.getTotalsecond(), exerciseService.getCurrentpace(), calories, timeStamp, docRef.getId());
        docRef.set(upload);
        Toast.makeText(getActivity(), "Exercise Stats Uploaded to Database!", Toast.LENGTH_LONG).show();
    }



    private void SetChoicevalue(String temps, String Musicstyle, Boolean init){

        ispop=false;isclassic=false;isedm=false;
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
            case "Classic":
                isclassic=true;
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

        if(exerciseService.accSensor!=null){
            exerciseService.sensorManager.registerListener(this,exerciseService.accSensor,SensorManager.SENSOR_DELAY_NORMAL);
            //Toast.makeText(getActivity(),"Sensor found",Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getActivity(),"Linear accelerometer not found",Toast.LENGTH_LONG).show();
        }

        if(exerciseService.accelerometer!=null){
            exerciseService.sensorManager.registerListener(this,exerciseService.accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
            //Toast.makeText(getActivity(),"Sensor found",Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getActivity(),"Accelerometer not found",Toast.LENGTH_LONG).show();
        }
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




    @Override
    public synchronized void onSensorChanged(SensorEvent event) {

        if(running&&exerciseService.exerciseTypeFlag==1){

            exerciseService.onSensorChanged(event);
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){

                /*Acceleration acceleration = new Acceleration();
                acceleration.timestamp = event.timestamp;

                Acceleration prevValue = values.isEmpty() ? null : values.getFirst();
                if (prevValue == null) {
                    for (int i = 0; i < 3; i++) {
                        acceleration.averagedValues[i] = event.values[i];
                        acceleration.lowPassFilteredValues[i] = event.values[i];
                    }
                } else {
                    lowPassFilter(acceleration.averagedValues, event.values, event.timestamp, prevValue.averagedValues,
                            prevValue.timestamp, FC_EARTH_GRAVITY_DETECTION);
                    lowPassFilter(acceleration.lowPassFilteredValues, event.values, event.timestamp,
                            prevValue.lowPassFilteredValues, prevValue.timestamp, FC_FOOT_FALL_DETECTION);
                }
                values.addFirst(acceleration);

                removeValuesOlderThan(event.timestamp - ACCELERATION_VALUE_KEEP_SECONDS * SECOND_TO_NANOSECOND);*/
                int cadence = getCurrentCadence();
                exerciseService.currentcadence=cadence;
                Taptostart.setText(String.valueOf(cadence)+"\nBPM");
                if(!Flag){
                    if(cadence<100) {
                        Integer flago=1;
                        if(!musicService.currentsong.equals(1)){
                            musicService.changemusic(flago);
                        }
                        change_music_speed(cadence,65,1);
                    }else {
                        Integer flago=2;
                        if(!musicService.currentsong.equals(2)){
                            musicService.changemusic(flago);
                        }
                        change_music_speed(cadence,115,1);
                    }
                }

            }
/*
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
                double acc;

                //rolling average
                rollingAverage[0] = roll(rollingAverage[0], event.values[0]);
                rollingAverage[1] = roll(rollingAverage[1], event.values[1]);
                rollingAverage[2] = roll(rollingAverage[2], event.values[2]);
                double x = averageList(rollingAverage[0]);
                double y = averageList(rollingAverage[1]);
                double z = averageList(rollingAverage[2]);
                acc = Math.sqrt(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2))*10;

            }*/
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



    /**
     * Get current cadence, in steps per minute.
     *
     * @return null if data isn't available
     */
    public synchronized int getCurrentCadence() {
       /* try {
            int axisIndex = findVerticalAxis();
            float g = values.getFirst().averagedValues[axisIndex];
            float threshold = (float) Math.abs(g / 2.5);
            long[] footFallTimestamps = new long[NUMBER_OF_FOOT_FALLS];
            int numberOfFootFalls = 0;
            boolean inThreshold = false;
            int i = 0;
            while (true) {
                Acceleration acceleration = values.get(i++);
                float a = acceleration.lowPassFilteredValues[axisIndex] - g;
                if (inThreshold) {
                    if (a < 0) {
                        inThreshold = false;
                    }
                } else {
                    if (a > threshold) {
                        inThreshold = true;
                        footFallTimestamps[numberOfFootFalls++] = acceleration.timestamp;
                    }
                }
                if (numberOfFootFalls == NUMBER_OF_FOOT_FALLS) {
                    break;
                }
            }
            return calculateCadenceByFootFallTimestamp(footFallTimestamps);
        } catch (NoSuchElementException e) {
            Log.d("MyTag", "No sensor event");
            return 0;
        } catch (IndexOutOfBoundsException e) {
            Log.d("MyTag", "No enough sensor events");
            return 0;
        }*/
       return exerciseService.getCurrentcadence();
    }

    /**
     * Calculate cadence by timestamp of last foot falls, return the average of middle values.
     *
     * @param footFallTimestamps
     * @return strides per minute


    private int calculateCadenceByFootFallTimestamp(long[] footFallTimestamps) {
        long[] footFallIntervale = new long[NUMBER_OF_FOOT_FALLS - 1];
        for (int i = 0; i < (NUMBER_OF_FOOT_FALLS - 1); i++) {
            footFallIntervale[i] = footFallTimestamps[i] - footFallTimestamps[i + 1];
        }
        Arrays.sort(footFallIntervale);
        long sum = 0;
        for (int i = 1; i < NUMBER_OF_FOOT_FALLS - 2; i++) {
            sum += footFallIntervale[i];
        }
        long average = sum / NUMBER_OF_FOOT_FALLS - 3;
        return (int) (60 * SECOND_TO_NANOSECOND / 2 / average);

    }
     */

    /**
     * The axis which has biggest average acceleration value is close to
     * vertical. Because the earth gravity is a constant.
     *
     * @return index of the axis (0~2)

    private int findVerticalAxis() {
        Acceleration latestValue = values.getFirst();
        float maxValue = 0;
        int maxValueAxis = 0;
        for (int i = 0; i < 3; i++) {
            float absValue = Math.abs(latestValue.averagedValues[i]);
            if (absValue > maxValue) {
                maxValue = absValue;
                maxValueAxis = i;
            }
        }
        return maxValueAxis;
    }


    private void removeValuesOlderThan(long timestamp) {
        while (!values.isEmpty()) {
            if (values.getLast().timestamp < timestamp) {
                values.removeLast();
            } else {
                return;
            }
        }
    }

    private void lowPassFilter(float[] result, float[] currentValue, long currentTime, float[] prevValue,
                               long prevTime, float cutoffFequency) {
        long deltaTime = currentTime - prevTime;
        float alpha = (float) (cutoffFequency * 3.14 * 2 * deltaTime / SECOND_TO_NANOSECOND);
        if (alpha > 1) {
            alpha = 1;
        }
        for (int i = 0; i < 3; i++) {
            result[i] = prevValue[i] + alpha * (currentValue[i] - prevValue[i]);
        }
    }*/




}
