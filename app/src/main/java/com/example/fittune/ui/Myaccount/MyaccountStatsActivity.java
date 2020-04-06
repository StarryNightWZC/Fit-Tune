package com.example.fittune.ui.Myaccount;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TextView;

import com.example.fittune.Model.ExerciseStats;
import com.example.fittune.R;
import com.example.fittune.Model.UploadFile;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;
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

import java.util.ArrayList;
import java.util.List;

public class MyaccountStatsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore firestoreDB;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private UploadTask uploadTask;

    private List<UploadFile> mUploads;
    private String userID;

    private PieChart pieChart;
    private LineChart lineChart;
    //private BarChart barChart;

    private TextView distance;
    private TextView pace;
    private TextView duration;
    private TextView calories;

    private String docRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myaccount_stats);
        getSupportActionBar().setTitle("Detail");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userID = mUser.getUid();
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        firestoreDB = FirebaseFirestore.getInstance();

        docRef = getIntent().getStringExtra("docRef");

        distance = findViewById(R.id.distance);
        pace = findViewById(R.id.pace);
        duration = findViewById(R.id.duration);
        calories = findViewById(R.id.calories);

        pieChart = findViewById(R.id.piechart);
        lineChart = findViewById(R.id.linechart);
        //barChart = findViewById(R.id.barchart);

        //create pie chart
        createPieChart();
        //create line chart
        createLineChart();
        //create barchart
        //createBarChart();

        loadProfile(userID);

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

//    public void createBarChart(){
//        List<BarEntry> entries = new ArrayList<>();
//        entries.add(new BarEntry(0f, 30f));
//        entries.add(new BarEntry(1f, 80f));
//        entries.add(new BarEntry(2f, 60f));
//        entries.add(new BarEntry(3f, 50f));
//        // gap of 2f
//        entries.add(new BarEntry(5f, 70f));
//        entries.add(new BarEntry(6f, 60f));
//        BarDataSet set = new BarDataSet(entries, "Distance");
//        BarData data = new BarData(set);
//        data.setBarWidth(0.9f); // set custom bar width
//        barChart.setData(data);
//        barChart.setFitBars(true); // make the x-axis fit exactly all bars
//        barChart.invalidate();
//
//        barChart.animateY(500);
//        set.setColors(ColorTemplate.COLORFUL_COLORS);
//
//        barChart.getAxisLeft().setDrawLabels(false);
//        barChart.getAxisRight().setDrawLabels(false);
//        barChart.getXAxis().setDrawLabels(false);
//        barChart.getLegend().setEnabled(false);
//
//        Description description = barChart.getDescription();
//        description.setEnabled(false);
//    }
    public void createLineChart(){
        lineChart.setTouchEnabled(true);
        lineChart.setPinchZoom(true);
        Description description = lineChart.getDescription();
        description.setEnabled(false);

        lineChart.animateY(500);
        lineChart.getAxisLeft().setDrawLabels(false);
        lineChart.getAxisRight().setDrawLabels(false);
        lineChart.getXAxis().setDrawLabels(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getAxisRight().setDrawGridLines(false);
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.setDrawBorders(true);

        ArrayList<Entry> values = new ArrayList<>();
        values.add(new Entry(1, 50));
        values.add(new Entry(2, 100));
        values.add(new Entry(3, 70));
        values.add(new Entry(4, 20));

        LineDataSet set1;
        if (lineChart.getData() != null &&
                lineChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
        } else {
            set1 = new LineDataSet(values, "Sample Data");
            set1.setDrawIcons(false);
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.DKGRAY);
            set1.setCircleColor(Color.DKGRAY);
            set1.setLineWidth(1f);
            set1.setCircleRadius(5f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);
            if (Utils.getSDKInt() >= 18) {
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_blue);
                set1.setFillDrawable(drawable);
            } else {
                set1.setFillColor(Color.DKGRAY);
            }
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            LineData data = new LineData(dataSets);
            lineChart.setData(data);
        }
    }

    private void loadProfile(String userID){
        firestoreDB.collection("Exercise").document(docRef).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document=task.getResult();
                            if(document.exists()){
                                ExerciseStats profile=document.toObject(ExerciseStats.class);
                                distance.setText(Double.toString(profile.getDistance())+"km");
                                pace.setText(profile.getPace());
                                duration.setText(profile.getDuration());
                                calories.setText(Double.toString(profile.getCalories())+"kcal");
                            }else{
                            }
                        }else{
                        }
                    }
                });
    }
}
