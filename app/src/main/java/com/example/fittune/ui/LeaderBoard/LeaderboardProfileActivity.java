package com.example.fittune.ui.LeaderBoard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fittune.Model.ExerciseStats;
import com.example.fittune.R;
import com.example.fittune.Model.Userprofile;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardProfileActivity extends AppCompatActivity {

    private PieChart pieChart;
    private BarChart barChart;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore firestoreDB;
    private List<ExerciseStats> mUploads;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private UploadTask uploadTask;

    private String userID;

    List<BarEntry> entries;

    private TextView bio;
    private ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard_profile);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pieChart = findViewById(R.id.piechart);
        barChart = findViewById(R.id.barchart);
        bio = findViewById(R.id.bio);
        profileImage = findViewById(R.id.profile_image);

        mUploads = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userID = mUser.getUid();
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        firestoreDB = FirebaseFirestore.getInstance();

        //create pie chart
        //createPieChart();
        //create barchart
        createBarChart();

    }

    public void createPieChart(){
        ArrayList NoOfEmp = new ArrayList();

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(18.5f, "High"));
        entries.add(new PieEntry(26.7f, "Medium"));
        entries.add(new PieEntry(24.0f, "Low"));
        PieDataSet set = new PieDataSet(entries, "Workout Intensity");
        PieData data = new PieData(set);
        pieChart.setData(data);
        set.setColors(ColorTemplate.COLORFUL_COLORS);
        set.setDrawValues(false);
        pieChart.animateXY(500, 500);
        pieChart.invalidate();

        Legend l = pieChart.getLegend();
        l.setFormSize(10f); // set the size of the legend forms/shapes
        l.setForm(Legend.LegendForm.CIRCLE); // set what type of form/shape should be used
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setTextSize(12f);
        l.setTextColor(Color.BLACK);
        l.setXEntrySpace(5f); // space between the legend entries on the x-axis
        l.setYEntrySpace(5f);

        Description description = pieChart.getDescription();
        description.setEnabled(false);
    }

    public void createBarChart(){
        entries = new ArrayList<>();
        firestoreDB.collection("Users").document(userID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document=task.getResult();
                            if(document.exists()){
                                Userprofile profile=document.toObject(Userprofile.class);
                                //username.setText(profile.getName());
                                bio.setText(profile.getBio());
                                Picasso.get().load(profile.getStorageRef()).fit().centerCrop().into(profileImage);
                                //distance.setText(profile.getDistance());
                                entries.add(new BarEntry(0f, Float.valueOf(profile.getMon())));
                                entries.add(new BarEntry(1f, Float.valueOf(profile.getTue())));
                                entries.add(new BarEntry(2f, Float.valueOf(profile.getWed())));
                                entries.add(new BarEntry(3f, Float.valueOf(profile.getThu())));
                                entries.add(new BarEntry(4f, Float.valueOf(profile.getFri())));
                                entries.add(new BarEntry(5f, Float.valueOf(profile.getSat())));
                                entries.add(new BarEntry(6f, Float.valueOf(profile.getSun())));
                                BarDataSet set = new BarDataSet(entries, "Distance");
                                BarData data = new BarData(set);
                                data.setBarWidth(0.9f); // set custom bar width
                                barChart.setData(data);
                                barChart.setFitBars(true); // make the x-axis fit exactly all bars
                                barChart.invalidate();

                                final ArrayList<String> xAxisLabel = new ArrayList<>();
                                xAxisLabel.add("Mon");
                                xAxisLabel.add("Tue");
                                xAxisLabel.add("Wed");
                                xAxisLabel.add("Thu");
                                xAxisLabel.add("Fri");
                                xAxisLabel.add("Sat");
                                xAxisLabel.add("Sun");


                                XAxis xAxis = barChart.getXAxis();
                                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                                ValueFormatter formatter = new ValueFormatter() {


                                    @Override
                                    public String getFormattedValue(float value) {
                                        return xAxisLabel.get((int) value);
                                    }
                                };

                                xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
                                xAxis.setValueFormatter(formatter);

                                barChart.animateY(500);
                                set.setColors(ColorTemplate.COLORFUL_COLORS);

                                barChart.getAxisLeft().setDrawLabels(false);
                                barChart.getAxisRight().setDrawLabels(false);
                                //barChart.getXAxis().setDrawLabels(false);
                                barChart.getAxisRight().setDrawGridLines(false);
                                barChart.getAxisLeft().setDrawGridLines(false);
                                barChart.getXAxis().setDrawGridLines(false);
                                barChart.getLegend().setEnabled(false);
                                barChart.setDrawBorders(true);

                                Description description = barChart.getDescription();
                                description.setEnabled(false);
                            }else{
                            }
                        }else{
                        }
                    }
                });
//        entries.add(new BarEntry(0f, 30f));
//        entries.add(new BarEntry(1f, 80f));
//        entries.add(new BarEntry(2f, 60f));
//        entries.add(new BarEntry(3f, 50f));
//        entries.add(new BarEntry(4f, 20f));
//        entries.add(new BarEntry(5f, 70f));
//        entries.add(new BarEntry(6f, 60f));
//        BarDataSet set = new BarDataSet(entries, "Distance");
//        BarData data = new BarData(set);
//        data.setBarWidth(0.9f); // set custom bar width
//        barChart.setData(data);
//        barChart.setFitBars(true); // make the x-axis fit exactly all bars
//        barChart.invalidate();
//
//        final ArrayList<String> xAxisLabel = new ArrayList<>();
//        xAxisLabel.add("Mon");
//        xAxisLabel.add("Tue");
//        xAxisLabel.add("Wed");
//        xAxisLabel.add("Thu");
//        xAxisLabel.add("Fri");
//        xAxisLabel.add("Sat");
//        xAxisLabel.add("Sun");
//
//
//        XAxis xAxis = barChart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
//
//        ValueFormatter formatter = new ValueFormatter() {
//
//
//            @Override
//            public String getFormattedValue(float value) {
//                return xAxisLabel.get((int) value);
//            }
//        };
//
//        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
//        xAxis.setValueFormatter(formatter);
//
//        barChart.animateY(500);
//        set.setColors(ColorTemplate.COLORFUL_COLORS);
//
//        barChart.getAxisLeft().setDrawLabels(false);
//        barChart.getAxisRight().setDrawLabels(false);
//        //barChart.getXAxis().setDrawLabels(false);
//        barChart.getAxisRight().setDrawGridLines(false);
//        barChart.getAxisLeft().setDrawGridLines(false);
//        barChart.getXAxis().setDrawGridLines(false);
//        barChart.getLegend().setEnabled(false);
//        barChart.setDrawBorders(true);
//
//        Description description = barChart.getDescription();
//        description.setEnabled(false);
    }
}
