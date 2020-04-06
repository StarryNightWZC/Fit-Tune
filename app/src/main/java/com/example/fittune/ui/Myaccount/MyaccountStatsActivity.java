package com.example.fittune.ui.Myaccount;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TextView;

import com.example.fittune.Model.ExerciseStats;
import com.example.fittune.Model.Userprofile;
import com.example.fittune.R;
import com.example.fittune.Model.UploadFile;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
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
import com.squareup.picasso.Picasso;

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

        loadStats();

    }

    public void createPieChart(List<PieEntry> entries){
        PieDataSet set = new PieDataSet(entries, "Workout Intensity");
        PieData data = new PieData(set);
        pieChart.setData(data);
        set.setColors(ColorTemplate.COLORFUL_COLORS);
        set.setDrawValues(false);
        pieChart.animateXY(1000, 1000);
        pieChart.invalidate();

        Legend l = pieChart.getLegend();
        l.setEnabled(false);
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

    public void createLineChart(ArrayList<Entry> values) {
        lineChart.setTouchEnabled(true);
        lineChart.setPinchZoom(true);
        Description description = lineChart.getDescription();
        description.setEnabled(true);
        description.setText("time/s");

        lineChart.animateY(1000);
        lineChart.getAxisLeft().setDrawLabels(false);
        lineChart.getAxisLeft().setDrawAxisLine(false);
        lineChart.getAxisRight().setDrawLabels(false);
        lineChart.getAxisRight().setDrawAxisLine(false);
        lineChart.getXAxis().setDrawLabels(true);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getLegend().setEnabled(false);
        lineChart.getAxisRight().setDrawGridLines(false);
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.setDrawBorders(false);

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

    private void loadStats(){
        firestoreDB.collection("Exercise").document(docRef).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document=task.getResult();
                            if(document.exists()){
                                ExerciseStats stats=document.toObject(ExerciseStats.class);
                                distance.setText(Double.toString(stats.getDistance())+"km");
                                pace.setText(stats.getPace());
                                duration.setText(stats.getDuration());
                                calories.setText(Double.toString(stats.getCalories())+"kcal");
                                //LineChart Data
                                ArrayList<Entry> lineChartEntries = new ArrayList<>();
                                int i = 0;
                                lineChartEntries.add(new Entry(0, 0));
                                float high = 0.5f;
                                float medium = 0.5f;
                                float low = 0.5f;
                                for (String stat : stats.getAveragespeedtenseconds()) {
                                    float speed = Float.valueOf(stat);
                                    if(speed <= 5.0f){
                                        low += 1.0f;
                                    }else if(speed < 10.0f){
                                        medium += 1.0f;
                                    }else{
                                        high += 1.0f;
                                    }
                                    i += 5;
                                    lineChartEntries.add(new Entry(i, speed));
                                }
                                //Piechart Data
                                List<PieEntry> pieChartEntries = new ArrayList<>();
                                pieChartEntries.add(new PieEntry(high, "High"));
                                pieChartEntries.add(new PieEntry(medium, "Medium"));
                                pieChartEntries.add(new PieEntry(low, "Low"));
                                createLineChart(lineChartEntries);
                                createPieChart(pieChartEntries);
                            }else{
                            }
                        }else{
                        }
                    }
                });
    }
}
