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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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

    ////////////////////////////////////////////////////
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
    private static final float FC_FOOT_FALL_DETECTION = 3.5F;

    /**
     * Cutoff frequency (fc) in low-pass filter for earth gravity detection
     */
    private static final float FC_EARTH_GRAVITY_DETECTION = 0.25F;
    private static final int ACCELERATION_VALUE_KEEP_SECONDS = 10;
    private static final int NUMBER_OF_FOOT_FALLS = 6;
    private static final long SECOND_TO_NANOSECOND = (long) 1e9;

    private Sensor accelerometer;
    private boolean active = false;
    private final LinkedList<Acceleration> values = new LinkedList<Acceleration>();

    ////////////////////////////////////////////////////

    //init rolling average storage
    List<Float>[] rollingAverage = new List[3];
    private static final int MAX_SAMPLE_SIZE = 100;

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
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        pause=root.findViewById(R.id.Pause);
        stop=root.findViewById(R.id.Finish);

        speed_seekbar=root.findViewById(R.id.speedbar);
        //////////////////////////
        bindServiceConnection();
        musicService = new MusicService();

        //init rolling average for linear acceleration on xyz axis
        rollingAverage[0] = new ArrayList<Float>();
        rollingAverage[1] = new ArrayList<Float>();
        rollingAverage[2] = new ArrayList<Float>();

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
            //Log.d("TotalSecond",currentpace);
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
            Toast.makeText(getActivity(),"Linear accelerometer not found",Toast.LENGTH_LONG).show();
        }

        if(accelerometer!=null){
            sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_FASTEST);
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
    public synchronized void onSensorChanged(SensorEvent event) {

        if(running&&exerciseTypeFlag==1){

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                Acceleration acceleration = new Acceleration();
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

                removeValuesOlderThan(event.timestamp - ACCELERATION_VALUE_KEEP_SECONDS * SECOND_TO_NANOSECOND);
                int cadence = getCurrentCadence();
                Taptostart.setText(String.valueOf(cadence)+"bpm");
                if(cadence<150) {
                    Integer flago=3;
                    if(!musicService.currentsong.equals(1)){
                        musicService.changemusic(flago);
                    }
                }else {
                    Integer flago=1;
                    if(!musicService.currentsong.equals(3)){
                        musicService.changemusic(flago);
                    }
                }
            }

            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
                //int count=(int)event.values[0];
                //Taptostart.setText(String.valueOf(count)+" bpm");
                //Toast.makeText(getActivity(),"Sensor running",Toast.LENGTH_LONG).show();

                double acc;
                /*double x = event.values[0];
                double y = event.values[1];
                double z = event.values[2];
                acc = Math.sqrt(Math.pow(y,2)+Math.pow(z,2));*/
                //rolling average
                rollingAverage[0] = roll(rollingAverage[0], event.values[0]);
                rollingAverage[1] = roll(rollingAverage[1], event.values[1]);
                rollingAverage[2] = roll(rollingAverage[2], event.values[2]);
                double x = averageList(rollingAverage[0]);
                double y = averageList(rollingAverage[1]);
                double z = averageList(rollingAverage[2]);
                acc = Math.sqrt(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2))*10;

                acc = Math.round(acc*100.0)/100.0;
                //Log.d("walking",String.valueOf(acc));
                //todo: acceleration text removed to show cadence info, might need to change back
                /*if(acc<1){
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
                }*/

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

    /**
     * Get current cadence, in steps per minute.
     *
     * @return null if data isn't available
     */
    public synchronized int getCurrentCadence() {
        try {
            int axisIndex = findVerticalAxis();
            float g = values.getFirst().averagedValues[axisIndex];
            float threshold = Math.abs(g / 2);
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
        }
    }

    /**
     * Calculate cadence by timestamp of last foot falls, return the average of middle values.
     *
     * @param footFallTimestamps
     * @return strides per minute
     */
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

    /**
     * The axis which has biggest average acceleration value is close to
     * vertical. Because the earth gravity is a constant.
     *
     * @return index of the axis (0~2)
     */
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
    }



}