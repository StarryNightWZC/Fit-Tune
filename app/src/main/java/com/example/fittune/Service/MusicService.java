package com.example.fittune.Service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.fittune.GetMusicBank;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class MusicService extends Service implements SensorEventListener {
    public int exercisetype=0;
    public Boolean isbpmdetected=true;
    //Sensors
    public SensorManager sensorManager;
    public Sensor accSensor;
    public Sensor accelerometer;
    public int cadence;
    private FirebaseFirestore firestoreDB;

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
    private static final int NUMBER_OF_FOOT_FALLS = 10;
    private static final long SECOND_TO_NANOSECOND = (long) 1e9;

    // private Sensor accelerometer;
    private boolean active = false;
    public final LinkedList<Acceleration> values = new LinkedList<Acceleration>();

    ////////////////////////////////////////////////////

    //init rolling average storage
    List<Float>[] rollingAverage = new List[3];
    private static final int MAX_SAMPLE_SIZE = 100;


    public final IBinder binder = new MyBinder();
    public class MyBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public static Integer currentsong=100;
    public MediaPlayer mediaPlayer=new MediaPlayer();
    public MusicService(){
    }

    private int count=0;

    public Integer musicflago=0;
    private HashMap<Integer, List> Song_info=new HashMap<>();
    private String Test_url;
    private GetMusicBank getMusicBank;

    private HashMap<Integer, List> EDM_Song_info=new HashMap<>();
    private HashMap<Integer, List> POP_Song_info=new HashMap<>();
    private HashMap<Integer, List> ROCK_Song_info=new HashMap<>();

    public String musicStyle="Rock";

    private Boolean musicinitFlag=true;


    @Override
    public void onCreate() {
        // Toast.makeText(this, "ExerciseService Created", Toast.LENGTH_SHORT).show();
        sensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        init();
        //getMusicBank=new GetMusicBank();
    }




    public void initMediaPlayer() {
            try {
                Log.d("InstanceState", "Music Service Init");
                for (int i = 1; i < 4; i++) {
                    List<String> p = new ArrayList<String>();
                    File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/EDM/" + String.valueOf(i));
                    p = Listdir(root, i);

                    EDM_Song_info.put(i, p);
                }
                Log.d("InstanceState", "EDM Init");
                for (int i = 1; i < 4; i++) {
                    List<String> p = new ArrayList<String>();
                    File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/Pop/" + String.valueOf(i));
                    p = Listdir(root, i);

                    POP_Song_info.put(i, p);
                }
                Log.d("InstanceState", "Pop Init");
                for (int i = 1; i < 4; i++) {
                    List<String> p = new ArrayList<String>();
                    File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/Rock/" + String.valueOf(i));
                    p = Listdir(root, i);

                    ROCK_Song_info.put(i, p);
                }
                Log.d("InstanceState", "Rock Service Init");



                String init_path;
                switch (musicStyle) {
                    case "EDM":
                        init_path = getrandommusic(1, EDM_Song_info);
                        break;
                    case "Pop":
                        init_path = getrandommusic(1, POP_Song_info);
                        break;
                    case "Rock":
                        init_path = getrandommusic(1, ROCK_Song_info);
                        break;
                    default:
                        init_path = getrandommusic(1, Song_info);
                }
                //String init_path=getrandommusic(1,Song_info);

                Log.d("InstanceState", init_path);

                mediaPlayer = new MediaPlayer();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(init_path);
                mediaPlayer.prepare();
                currentsong = 1;
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        try {
                            playnewmusic(musicflago);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }


    }



    private List Listdir(File f,int type){
        List<String> filelist=new ArrayList<String>();
        File[] files=f.listFiles();
        filelist.clear();
        for(File file:files){
            filelist.add(file.getAbsolutePath());
        }
        Log.d("Path",filelist.toString());
        return filelist;
    }

    private String getrandommusic(int type,HashMap<Integer, List> SongLib){
        count++;
        String song_path="";
        List<String> f=SongLib.get(type);
        int max=f.size();

        int r=count%(max);
        song_path=f.get(r);
        Log.d("Files",song_path);
        return song_path;
    }

    public void changemusic(Integer type){


        switch (type){
            case 1:
                playnewmusic(type);
                currentsong=1;
                break;
            case 2:
                playnewmusic(type);
                currentsong=2;
                break;
            case 3:
                playnewmusic(type);
                currentsong=3;
                break;
        }

    }

    public void change_music_speed(float currentspeed, float threshold,int scenario){
        if(scenario==1){
            if(currentspeed>threshold){
                float speed=(currentspeed-threshold)/currentspeed;
                speed= (float) (1.1+(float)(Math.round(speed*1000)/1000f));
                Log.d("Speed",String.valueOf(speed));
                changeplayerSpeed(speed);
            }
        }else {
            float diff=threshold-currentspeed;
            List<Float> speed_choice = new  ArrayList<Float>();
            speed_choice.add(1.1f);
            speed_choice.add(1.2f);
            speed_choice.add(1.3f);
            if (diff<=2&&diff>0){
                changeplayerSpeed(speed_choice.get(2));
            }else if(diff<=4&&diff>2){
                changeplayerSpeed(speed_choice.get(1));
            }else if(diff<=5&&diff>4) {
                changeplayerSpeed(speed_choice.get(0));
            }
        }

    }


    public void changeplayerSpeed(float speed) {
        if (mediaPlayer == null)  {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // API 23 （6.0）以上 ，通过设置Speed改变音乐的播放速率
            if (mediaPlayer.isPlaying()) {
                // 判断是否正在播放，未播放时，要在设置Speed后，暂停音乐播放
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
            } else {
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
                mediaPlayer.pause();
            }
        } else {

        }
    }



    private void playnewmusic(Integer type){
        String path;
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
        try {
            switch (musicStyle){
                case "EDM":
                    path=getrandommusic(type,EDM_Song_info);
                    Log.d("Path",path);
                    break;
                case "Pop":
                    path=getrandommusic(type,POP_Song_info);
                    Log.d("Path",path);
                    break;
                case "Rock":
                    path=getrandommusic(type,ROCK_Song_info);
                    Log.d("Path",path);
                    break;
                default:
                    path=getrandommusic(type,Song_info);
                    Log.d("Path",path);
            }
            //path=getrandommusic(type,Song_info);

            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();

        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "播放错误", Toast.LENGTH_SHORT).show();
        }
    }


    public static String which = "";

    @SuppressLint("WrongConstant")
    public void playOrPause() {
        which = "pause";
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            if(exercisetype==1){
                exercisetype=3;
            }

        } else {
            mediaPlayer.start();
            if(exercisetype==3){
                exercisetype=1;
            }
        }


    }
    public void stop() {
        which = "stop";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        }
        if(mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.stop();
            mediaPlayer.reset();

            try {
                String init_path;
                switch (musicStyle){
                    case "EDM":
                        init_path=getrandommusic(1,EDM_Song_info);
                        break;
                    case "Pop":
                        init_path=getrandommusic(1,POP_Song_info);
                        break;
                    case "Rock":
                        init_path=getrandommusic(1,ROCK_Song_info);
                        break;
                    default:
                        init_path=getrandommusic(1,Song_info);
                }
                mediaPlayer.setDataSource(init_path);
                //mediaPlayer.reset();//
                mediaPlayer.prepare();
               // mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //------------------------------------------------------//
    //---------------Sensors--------------------------------//

    private void init() {

        try {
            Log.d("InstanceState","Exerciseservices init");

            sensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
            //registering Sensor
            accSensor=sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


            sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

            //init rolling average for linear acceleration on xyz axis
            rollingAverage[0] = new ArrayList<Float>();
            rollingAverage[1] = new ArrayList<Float>();
            rollingAverage[2] = new ArrayList<Float>();

        } catch (Exception e) {
            e.printStackTrace();
        }
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


    @Override
    public void onSensorChanged(SensorEvent event) {
        if(exercisetype==1) {
            //Log.d("InstanceState", "onSensorChanged");
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
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

                cadence = getCurrentcadence();
                if(mediaPlayer.isPlaying()){
                    if(cadence<115) {
                        musicflago=1;
                        if(!currentsong.equals(1)){
                            changemusic(musicflago);
                        }
                        change_music_speed(cadence,75,1);
                    }else {
                        musicflago=2;
                        if(!currentsong.equals(2)){
                            changemusic(musicflago);
                        }
                        change_music_speed(cadence,135,1);
                    }
                }
            }

            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                double acc;

                //rolling average
                rollingAverage[0] = roll(rollingAverage[0], event.values[0]);
                rollingAverage[1] = roll(rollingAverage[1], event.values[1]);
                rollingAverage[2] = roll(rollingAverage[2], event.values[2]);
                double x = averageList(rollingAverage[0]);
                double y = averageList(rollingAverage[1]);
                double z = averageList(rollingAverage[2]);
                acc = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)) * 10;

            }
        }else if(exercisetype!=3){
            cadence=0;
            values.clear();

        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public int getCurrentcadence(){
        try {
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



    //------------------------------------------------------//
    @Override
    public void onDestroy() {
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            Log.d("InstanceStateinit","musicservicedestroy");
        }


        super.onDestroy();
    }

    /**
     * onBind 是 Service 的虚方法，因此我们不得不实现它。
     * 返回 null，表示客服端不能建立到此服务的连接。
     */
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

}
