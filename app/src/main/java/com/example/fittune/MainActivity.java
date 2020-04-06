package com.example.fittune;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.demon.errorinfocatch.CrashHandler;
import com.example.fittune.ui.LeaderBoard.GalleryFragment;
import com.example.fittune.ui.Myaccount.MyaccountFragment;
import com.example.fittune.ui.Dashboard.DashboardFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {


    public Boolean isdashBoardfinish;
    final Fragment fragment1=new DashboardFragment();
    final Fragment fragment2=new GalleryFragment();
    final Fragment fragment3=new MyaccountFragment();
    final FragmentManager fm = getSupportFragmentManager();
    Fragment active = fragment1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());

        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        //AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
        //        R.id.navigation_gallery, R.id.navigation_dashboard, R.id.navigation_Myaccount)
        //        .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);


        navView.setOnNavigationItemSelectedListener(monNavigationItemSelectedListener);

        fm.beginTransaction().add(R.id.nav_host_fragment, fragment3, "3").hide(fragment3).commit();
        fm.beginTransaction().add(R.id.nav_host_fragment, fragment2, "2").hide(fragment2).commit();
        fm.beginTransaction().add(R.id.nav_host_fragment,fragment1, "1").commit();


    }

    public void getsignalfromdashboard(Boolean flag){
        isdashBoardfinish=flag;
    }


    private BottomNavigationView.OnNavigationItemSelectedListener monNavigationItemSelectedListener=new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            //isdashBoardfinish=fm.findFragmentById(R.id.navigation_dashboard).
            switch (menuItem.getItemId()) {
                case R.id.navigation_dashboard:
                    if(isdashBoardfinish){
                        fm.beginTransaction().hide(active).detach(fragment1).attach(fragment1).show(fragment1).commit();
                       // fm.beginTransaction().hide(active).replace()
                        active=fragment1;
                        return true;

                    }else {
                        fm.beginTransaction().hide(active).show(fragment1).commit();
                        active = fragment1;
                        return true;
                    }

                case R.id.navigation_gallery:
                    fm.beginTransaction().hide(active).show(fragment2).commit();
                    active = fragment2;
                    return true;

                case R.id.navigation_Myaccount:
                    fm.beginTransaction().hide(active).show(fragment3).commit();
                    active = fragment3;
                    return true;
            }

            return false;
        }
    };




    @Override
    public void onDestroy() {
        //getActivity().unbindService(scmusic);
        Log.d("U","Activity in Destroy");

        super.onDestroy();
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

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




}
