package com.example.fittune.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class StepDetectorService extends Service implements SensorEventListener {
    SensorManager sensorManager;

    public static final String ACTIVITY_RECOGNITION = null;
    private MyBinder mBinder = new MyBinder();

    public StepDetectorService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager=(SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(StepDetectorService.this, "StartCommand", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }




    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    class MyBinder extends Binder {

        public int DetectStep(){

            return 0;
        }

        public String startDownload() {

            Toast.makeText(StepDetectorService.this, "Binder", Toast.LENGTH_SHORT).show();
            return "Success";
        }

    }

}
