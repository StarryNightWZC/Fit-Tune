package com.example.fittune;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MusicService extends Service {
    public final IBinder binder = new MyBinder();
    public class MyBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public static Integer currentsong;
    public static MediaPlayer mediaPlayer = new MediaPlayer();
    public static ObjectAnimator animator;
    public MusicService() {
        initMediaPlayer();
    }

    private final HashMap<Integer,String> Songinfo=new HashMap<>();

    public void initMediaPlayer() {
        try {
            String file_path_1=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/1.mp3";
            String file_path_2=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/2.mp3";
            String file_path_3=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/3.mp3";
            Songinfo.put(1,file_path_1);
            Songinfo.put(2,file_path_2);
            Songinfo.put(3,file_path_3);
            //String file_path=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/Test.mp3";
            mediaPlayer.setDataSource(file_path_1);
            currentsong=1;
            mediaPlayer.prepare();
            //mediaPlayer.setLooping(true);  // 设置循环播放
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    ///////////Test
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
/*
    public void changemusicoutdoor(Integer type){

        switch (type){
            case 1:
                playnewmusic(type);
                currentsong=1;
                break;
            case 2:
                playnewmusic(type);
                currentsong=2;
                break;
        }

    }
*/
    private void playnewmusic(Integer type){
        String path;
        mediaPlayer.pause();
        mediaPlayer.stop();
        mediaPlayer.reset();
        try {
            path=Songinfo.get(type);
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            // mediaPlayer.seekTo(0);
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            }
        } else {
            mediaPlayer.start();
        }
    }
    public void stop() {
        which = "stop";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        }
        if(mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
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
