package com.example.fittune.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;

import com.example.fittune.R;
import com.example.fittune.ui.dashboard.DashboardFragment;

import java.io.File;
import java.security.PublicKey;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

public class ExerciseService extends Service {


    //Sensors
    public SensorManager sensorManager;
    public Sensor accSensor;
    public Sensor accelerometer;

    //TotalSecond
    private long lastPause;
    private double totalsecond=0;

    ///TotalDistance
    private double totaldistance=0;
    private double intervaldistance=0;

    ////////pace
    private String currentpace="";

    //Total Kcal
    private double totalkcal=0;
    private float averageweight= (float) 80.3;

    private boolean isDurationStop=false;

    private Handler mHandler=new Handler();

    private final IBinder mBinder= new LocalService();

    public static int exerciseTypeFlag=0;
    public static float seedseekbarvalue=0;
    public static int currentcadence=0;

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



    public ExerciseService() {
        init();
    }


    private void init() {
        Log.d("InstanceState","Exerciseservice init");
        try {

            /*
            sensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
            accSensor=sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            sensorManager.registerListener((SensorEventListener) this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener((SensorEventListener) this, accelerometer, SensorManager.SENSOR_DELAY_GAME);*/

            //init rolling average for linear acceleration on xyz axis
            rollingAverage[0] = new ArrayList<Float>();
            rollingAverage[1] = new ArrayList<Float>();
            rollingAverage[2] = new ArrayList<Float>();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;

    }

    public class LocalService extends Binder{
        public ExerciseService getService(){
            return ExerciseService.this;
        }
    }


    public Runnable updateRunnable=new Runnable() {
        @Override
        public void run() {
            if(exerciseTypeFlag==1){
                //Current Speed
                float currentspeed= (float) (currentcadence*0.5*60/1000);
                //Log.d("CurrentSpeed",String.valueOf(currentspeed));
                //Distance
                intervaldistance=currentspeed*1/3600;
                totaldistance+=intervaldistance;
                intervaldistance=0;
                //Fat-burning
                totalkcal=averageweight*totaldistance*1.036;
               // String kcalString = onedecimalFormat.format(totalkcal) + "kcal";
                mHandler.postDelayed(this,1000);
                //Pace

                totalsecond+=1;
                currentpace=CalculateCunrrentpace(totalsecond);
               // String totalsecond=getChronometerSeconds(durationtimer);
                //Log.d("TotalSecond",currentpace);*/

            }else {
                //Distance
                intervaldistance=seedseekbarvalue*1/3600;
                totaldistance+=intervaldistance;
                intervaldistance=0;
               // String distanceString = decimalFormat.format(totaldistance) + "Km";
                totalkcal=averageweight*totaldistance*1.036;
               // String kcalString = onedecimalFormat.format(totalkcal) + "kcal";
                mHandler.postDelayed(this,1000);
                //Pace
                totalsecond+=1;
                currentpace=CalculateCunrrentpace(totalsecond);
               // String totalsecond=getChronometerSeconds(durationtimer);



            }
        }
    };

    public void PlayorPause(){
        if(isDurationStop){
            isDurationStop=false;
            updateRunnable.run();
        }else {
            isDurationStop=true;
            intervaldistance=0;
            mHandler.removeCallbacks(updateRunnable);
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

    public double getTotaldistance(){
        return totaldistance;
    }

    public double getTotalkcal(){
        return totalkcal;
    }

    public String getTotalsecond(){
        int hour=(int)(totalsecond)/3600;
        int minutes = (int) (totalsecond-hour*60) / 60;
        int seconds = (int) (totalsecond ) % 60;

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d",hour, minutes, seconds);

        return timeFormatted;
    }

    public String getCurrentpace(){
        return currentpace;
    }

    public void stop(){
        totaldistance=0;
        intervaldistance=0;
        totalkcal=0;
        currentpace="0'00''";
        totalsecond=0;
        mHandler.removeCallbacks(updateRunnable);
    }


    //---------------------------------//
    //********Calculate Cadence********//
    //---------------------------------//

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


    public void onSensorChanged(SensorEvent event) {
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
        }

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

        }


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


    /**
     * 服务销毁时的回调
     */
    @Override
    public void onDestroy() {
        Log.d("InstanceState","Exerciseservice Destroy");
        System.out.println("onDestroy invoke");
        super.onDestroy();
    }

}
