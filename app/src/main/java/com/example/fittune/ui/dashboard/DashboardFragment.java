package com.example.fittune.ui.dashboard;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
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
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.fittune.Dialog_Edit;
import com.example.fittune.Dialog_chooseScenario;
import com.example.fittune.ExerciseStats;
import com.example.fittune.MainActivity;
import com.example.fittune.MusicService;
import com.example.fittune.R;
import com.example.fittune.SignedInActivity;
import com.example.fittune.StepDetectorService;
import com.example.fittune.Userprofile;
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
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class DashboardFragment extends Fragment implements SensorEventListener {

    /////////////////////////
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
    ////////////////

    private DashboardViewModel dashboardViewModel;
    private TextView Taptostart,speed,choiceone,choicetwo,choiceoneshow,choicetwoshow;
    private boolean Flag=true;

    DecimalFormat decimalFormat =new DecimalFormat("0.00");
    DecimalFormat onedecimalFormat=new DecimalFormat("0.0");

    private Chronometer durationtimer;
    private long lastPause;

    private double totalsecond=0;

    ///TotalDistance
    private float totaldistance=0;
    private float intervaldistance=0;

    ////////pace
    private String currentpace="";

    //Total Kcal
    private double totalkcal=0;
    private float averageweight= (float) 80.3;

    private Button pause,stop;
    private SeekBar speed_seekbar;
    private float  seedseekbarvalue;

    private boolean isTimerStop=false,isDurationStop=false;
    private boolean isPacechoosed=false,isDistancechoosed=true,isFatburnchoosed=true;

    private MusicService musicService;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

   // private StepDetectorService.MyBinder myBinder;

    private SensorManager sensorManager;
    private Sensor counterSensor;
    private Sensor accSensor;
    private boolean running=false;

    ////////Test
    private Button speedchange;

    private int exerciseTypeFlag = 0;


    private Handler mHandler=new Handler();


    java.util.Timer timer = new java.util.Timer(true);

    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicService = ((MusicService.MyBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicService = null;
        }
    };

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

        Taptostart = root.findViewById(R.id.text_taptostart);
        choiceone=root.findViewById(R.id.Choiceone);
        choicetwo=root.findViewById(R.id.Choicetwo);

        //Choice value show
        choiceoneshow=root.findViewById(R.id.Choiceoneshow);
        choicetwoshow=root.findViewById(R.id.Choicetwoshow);
        durationtimer=root.findViewById(R.id.DurationChronmeter);


        speed=root.findViewById(R.id.speedkm);

        sensorManager=(SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        //counterSensor=sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        accSensor=sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        pause=root.findViewById(R.id.Pause);
        stop=root.findViewById(R.id.Finish);

        speed_seekbar=root.findViewById(R.id.speedbar);
        //////////////////////////
        bindServiceConnection();
        musicService = new MusicService();

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
                final Dialog_Edit dialog_edit=new Dialog_Edit();
                dialog_edit.setdoneOnclickListener(new Dialog_Edit.onDoneOnclickListener() {
                    @Override
                    public void onDoneClick(StringBuilder sb) {
                        String [] temp = null;
                        String temps = sb.toString();
                        boolean status = temps.contains(",");
                        if(status){
                            SetChoicevalue(temps);
                        }else {
                            choiceone.setText(temps);
                            choicetwo.setText("");
                        }
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
                if (Flag){
                    final Dialog_chooseScenario dialog_chooseScenario=new Dialog_chooseScenario();
                    dialog_chooseScenario.setoutdoorOnclickListener(new Dialog_chooseScenario.onOutdoorOnclickListener() {
                        @Override
                        public void onOutdoorClick() {
                            exerciseTypeFlag = 1;
                            Flag=false;
                            //Taptostart.setText("0 BPM");
                            Taptostart.setText("0 BPM");
                            musicService.playOrPause();

                            running=true;
                            pause.setVisibility(View.VISIBLE);
                            pause.setText("Pause");
                            stop.setVisibility(View.VISIBLE);
                            startTimer();
                            dialog_chooseScenario.dismiss();
                        }
                    });
                    dialog_chooseScenario.setTreadmillOnclickListener(new Dialog_chooseScenario.onTreadmillOnclickListener() {
                        @Override
                        public void ontreadmillClick() {
                            exerciseTypeFlag = 2;
                            Flag=false;
                            musicService.playOrPause();
                            Taptostart.setVisibility(View.GONE);
                            speed.setVisibility(View.VISIBLE);
                            speed_seekbar.setVisibility(View.VISIBLE);
                            pause.setVisibility(View.VISIBLE);
                            stop.setVisibility(View.VISIBLE);
                            startTimer();
                            updateRunnable.run();
                            //new Thread(updateRunnable).start();
                            dialog_chooseScenario.dismiss();
                        }
                    });
                    dialog_chooseScenario.show(getActivity().getSupportFragmentManager(),"Taptostart");
                }
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePlay();
                musicService.playOrPause();
                PlayorPausedurationtimer();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pause.getText()=="Pause"){
                    changePlay();
                }
                //
                durationtimer.getBase();

                double temp = Math.round(totaldistance*100.0)/100.0;
                //float temp = totaldistance;
                Map<String, Object> note = new HashMap<>();
                note.put("duration", String.valueOf(temp));
                firestoreDB.collection("Users").document(userID).update(note);
                //update stats
                updateStats();
                //upload stats
                uploadStats();

                ///
                speed_seekbar.setProgress(0);
                musicService.stop();
                totaldistance=0;
                intervaldistance=0;
                totalkcal=0;
                currentpace="";
                mHandler.removeCallbacks(updateRunnable);
                stopTimer();

            }
        });

        speed_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seedseekbarvalue=progress/10f;
                Integer flag=0;
                speed.setText("SPEED\n"+Float.toString(seedseekbarvalue)+" Km/h");
                //change music
                if(seedseekbarvalue<=6&&seedseekbarvalue>=0){
                    flag=1;
                    if(!musicService.currentsong.equals(1)){
                        musicService.changemusic(flag);
                    }else {
                        change_music_speed(seedseekbarvalue,6);
                    }
                }else if(seedseekbarvalue<=12&&seedseekbarvalue>6){
                    flag=2;
                    if(!musicService.currentsong.equals(2)){
                        musicService.changemusic(flag);
                    }else {
                        change_music_speed(seedseekbarvalue,12);
                    }
                }else{
                    flag=3;
                    if(!musicService.currentsong.equals(3)){
                        musicService.changemusic(flag);
                    }else {
                        change_music_speed(seedseekbarvalue,18);
                    }
                }
                //Calculate total distance

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

    private void change_music_speed(float currentspeed, float threshold){
        float diff=threshold-currentspeed;
        List<Float> speed_choice = new  ArrayList<Float>();
        speed_choice.add(1.25f);
        speed_choice.add(1.5f);
        speed_choice.add(1.75f);
        if (diff<=2&&diff>0){
            musicService.changeplayerSpeed(speed_choice.get(2));
        }else if(diff<=4&&diff>2){
            musicService.changeplayerSpeed(speed_choice.get(1));
        }else if(diff<=5&&diff>4) {
            musicService.changeplayerSpeed(speed_choice.get(0));
        }
    }

    private void updateStats(){

        firestoreDB.collection("Users").document(userID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document=task.getResult();
                            if(document.exists()){
                                Userprofile profile=document.toObject(Userprofile.class);
                                double savedistance = Float.valueOf(profile.getDuration()) + Float.valueOf(profile.getDistance());
                                savedistance = Math.round(savedistance* 100.0) / 100.0;
                                Map<String, Object> note = new HashMap<>();
                                note.put("distance", String.valueOf(savedistance));
                                firestoreDB.collection("Users").document(userID).update(note);
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
        if(exerciseTypeFlag==1){
            exerciseType = "outdoor";
        }else if(exerciseTypeFlag==2){
            exerciseType = "treadmill";
        }
        double distance = Math.round(totaldistance*1000.0)/1000.0;
        double calories = Math.round(totalkcal*1000.0)/1000.0;
        DocumentReference docRef = firestoreDB.collection("Exercise").document();
        ExerciseStats upload = new ExerciseStats(userID, exerciseType, distance,
                getChronometerSeconds(durationtimer), currentpace, calories, timeStamp, docRef.getId());
        docRef.set(upload);
        Toast.makeText(getActivity(), "Exercise Stats Uploaded to Database!", Toast.LENGTH_LONG).show();
    }

    private Runnable updateRunnable=new Runnable() {
        @Override
        public void run() {
            //Distance
            intervaldistance=seedseekbarvalue*2/3600;
            totaldistance+=intervaldistance;
            intervaldistance=0;
            String distanceString = decimalFormat.format(totaldistance) + "Km";
            if(isDistancechoosed){
                choiceoneshow.setText(distanceString);
            }
            //Fat-burning
            totalkcal=averageweight*totaldistance*1.036;
            String kcalString = onedecimalFormat.format(totalkcal) + "kcal";
            if(isFatburnchoosed){
                choicetwoshow.setText(kcalString);
            }
            mHandler.postDelayed(this,2000);
            //Pace

            totalsecond+=2;
            currentpace=CalculateCunrrentpace(totalsecond);
            if(isPacechoosed){
                choicetwoshow.setText(currentpace);
            }
            //String totalsecond=getChronometerSeconds(durationtimer);
            Log.d("TotalSecond",currentpace);
        }
    };




    private void SetChoicevalue(String temps){
        if(temps.contains("Distance")&&temps.contains("Fat Burning")){

            isFatburnchoosed=true;
            isDistancechoosed=true;
            durationtimer.setVisibility(View.GONE);
            choiceoneshow.setVisibility(View.VISIBLE);
            choiceone.setText("Distance");
            choicetwo.setText("Fat Burning");
        }else if(temps.contains("Distance")&&temps.contains("Pace")){

            durationtimer.setVisibility(View.GONE);
            choiceone.setText("Distance");
            choicetwo.setText("Pace");

        }else if(temps.contains("Distance")&&temps.contains("Duration")){

            durationtimer.setVisibility(View.VISIBLE);
            choiceone.setText("Distance");
            choicetwo.setText("Duration");

        }else if(temps.contains("Pace")&&temps.contains("Fat Burning")){

            durationtimer.setVisibility(View.GONE);
            choiceone.setText("Pace");
            choicetwo.setText("Fat Burning");

        }else if(temps.contains("Duration")&&temps.contains("Fat Burning")){

            durationtimer.setVisibility(View.VISIBLE);
            choiceone.setText("Fat Burning");
            choicetwo.setText("Duration");

        }else if(temps.contains("Duration")&&temps.contains("Pace")){

            isPacechoosed=true;
            choiceoneshow.setVisibility(View.GONE);
            durationtimer.setVisibility(View.VISIBLE);
            choiceone.setText("Duration");
            choicetwo.setText("Pace");
        }
    }


    private void PlayorPausedurationtimer(){
        if(isTimerStop){
            isTimerStop=false;
            startTimer();
        }else {
            lastPause= SystemClock.elapsedRealtime();
            durationtimer.stop();
            isTimerStop=true;
        }
        if(isDurationStop){
            isDurationStop=false;
            updateRunnable.run();
        }else {
            isDurationStop=true;
            intervaldistance=0;
            mHandler.removeCallbacks(updateRunnable);
        }


    }



    private void startTimer(){
        //Duration
        if(lastPause!=0){
            durationtimer.setBase(durationtimer.getBase()+SystemClock.elapsedRealtime()-lastPause);
        }else {
            durationtimer.setBase(SystemClock.elapsedRealtime());
        }
       // int second=(int) ((SystemClock.elapsedRealtime() - durationtimer.getBase()) / 1000 );
       // Log.d("timer",Long.toString(durationtimer.getBase()));
        int hour = (int) ((SystemClock.elapsedRealtime() - durationtimer.getBase()) / 1000 / 60);
        durationtimer.setFormat("0"+String.valueOf(hour)+":%s");
        durationtimer.start();

    }

    private void stopTimer(){
        durationtimer.stop();
        durationtimer.setBase(SystemClock.elapsedRealtime());
        int hour = (int) ((SystemClock.elapsedRealtime() - durationtimer.getBase()) / 1000 / 60);
        durationtimer.setFormat("0"+String.valueOf(hour)+":%s");
        lastPause=0;
    }

    private void changePlay() {

        if(musicService.mediaPlayer.isPlaying()){
            pause.setText("Play");
            //animator.pause();
        } else {
            pause.setText("Pause");

        }
    }



    private void bindServiceConnection() {
        Intent intent = new Intent(getActivity(), MusicService.class);
        getActivity().startService(intent);
        getActivity().bindService(intent, sc, getActivity().BIND_AUTO_CREATE);
    }



    @Override
    public void onResume(){
        super.onResume();

        if(accSensor!=null){
            sensorManager.registerListener(this,accSensor,SensorManager.SENSOR_DELAY_NORMAL);
            //Toast.makeText(getActivity(),"Sensor found",Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getActivity(),"Sensor not found",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        running=false;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        //Toast.makeText(getActivity(),"Sensor running",Toast.LENGTH_LONG).show();
        if(running&&exerciseTypeFlag==1){
            //int count=(int)event.values[0];
            //Taptostart.setText(String.valueOf(count)+" bpm");
            //Toast.makeText(getActivity(),"Sensor running",Toast.LENGTH_LONG).show();

            double acc;
            double x = event.values[0];
            double y = event.values[1];
            double z = event.values[2];
            acc = Math.sqrt(Math.pow(y,2)+Math.pow(z,2));
            acc = Math.round(acc*100.0)/100.0;
            Log.d("walking",String.valueOf(acc));
            ///////QZH
            if(acc<1){
                acc=0;
                Taptostart.setText("0.00"+"\n m2/s");
            }else {
                Taptostart.setText(String.valueOf(acc)+"\n m2/s");
            }

            if(acc>5)
            {
                Integer flago=3;
                if(!musicService.currentsong.equals(3)){
                    musicService.changemusic(flago);
                }
            }else if(acc<=3){
                Integer flago=1;
                if(!musicService.currentsong.equals(1)){
                    musicService.changemusic(flago);
                }
            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private boolean isApplicationBroughtToBackground() {
        ActivityManager am = (ActivityManager)getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(getActivity().getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private String CalculateCunrrentpace(Double second){
        String pace="";
        double min=0;
        double sec=0;
        if(totaldistance==0){
            pace="0'00";
            return pace;
        }else {
            min=Math.floor(second/totaldistance/60);
            Integer intmin=(int)min;
            double difference=(second/totaldistance/60)-min;
            sec=Math.floor(difference*60);
            Integer intsec=(int)sec;
            pace=Integer.toString(intmin)+"'"+Integer.toString(intsec)+"''";
            return pace;
        }

    }


    public  static String getChronometerSeconds(Chronometer cmt) {
        int totalss = 0;
        String string = cmt.getText().toString();
        //Log.d("Length",Integer.toString(string.length()));
            String[] split = string.split(":");
            String string2 = split[0];
            int hour = Integer.parseInt(string2);
            int Hours =hour*3600;
            String string3 = split[1];
            int min = Integer.parseInt(string3);
            int Mins =min*60;
            int  SS =Integer.parseInt(split[2]);
            //totalss = Hours+Mins+SS;
            return hour+":"+min+":"+SS;


    }



}