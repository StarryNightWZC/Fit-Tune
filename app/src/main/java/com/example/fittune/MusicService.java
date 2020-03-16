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
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

    private HashMap<Integer, List> Song_info=new HashMap<>();


    public void initMediaPlayer() {
        try {
            /////////////////Test
            for (int i=1;i<4;i++){
                List<String> p=new ArrayList<String>();
                File root=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath()+'/'+String.valueOf(i));
                p=Listdir(root,i);
                Song_info.put(i,p);
            }

            String init_path=getrandommusic(1,Song_info);
            mediaPlayer.setDataSource(init_path);
            currentsong=1;
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playnewmusic(currentsong);
                }
            });
            //mediaPlayer.setLooping(true);  // 设置循环播放
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
        return filelist;
    }

    private String getrandommusic(int type,HashMap<Integer, List> SongLib){
        String song_path="";
        List<String> f=SongLib.get(type);
        int max=f.size();
        Random random = new Random();
        int r = random.nextInt(max)%(max+1);
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

    private void playnewmusic(Integer type){
        String path;
        mediaPlayer.pause();
        mediaPlayer.stop();
        mediaPlayer.reset();
        try {
            //path=Songinfo.get(type);
            path=getrandommusic(type,Song_info);
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
