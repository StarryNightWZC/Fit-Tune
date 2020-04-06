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
import android.widget.Toast;

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

public class ExerciseService extends Service{

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
    public float currentspeed=0;







    public ExerciseService() {
       // init();
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
                currentspeed= (float) (currentcadence*0.5*60/1000);
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
