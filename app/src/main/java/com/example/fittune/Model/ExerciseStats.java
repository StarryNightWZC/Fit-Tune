package com.example.fittune.Model;

import java.util.List;

public class ExerciseStats {

    private String userId;
    private String exerciseType;
    private double distance;
    private String duration;
    private String pace;
    private double calories;
    private String timeStamp;
    private String docRef;
    private List<String> averagespeedtenseconds;

    public ExerciseStats(){

    }

    public ExerciseStats(String uid, String exercise, double dist, String dura, String pac,
                         double cal, String time, String dRef){
        this.userId = uid;
        this.exerciseType = exercise;
        this.distance = dist;
        this.duration = dura;
        this.pace = pac;
        this.calories = cal;
        this.timeStamp = time;
        this.docRef = dRef;
    }

    public ExerciseStats(String uid, String exercise, double dist, String dura, String pac,
                         double cal, String time, String dRef, List<String> speed){
        this.userId = uid;
        this.exerciseType = exercise;
        this.distance = dist;
        this.duration = dura;
        this.pace = pac;
        this.calories = cal;
        this.timeStamp = time;
        this.docRef = dRef;
        this.averagespeedtenseconds = speed;
    }

    public String getUserId() {return userId;}
    public String getExerciseType() {return exerciseType;}
    public double getDistance() {return distance;}
    public String getDuration() {return duration;}
    public String getPace() {return pace;}
    public double getCalories() {return calories;}
    public String getTimeStamp() {return timeStamp;}
    public String getDocRef() {return docRef;}
    public List<String> getAveragespeedtenseconds() {
        return averagespeedtenseconds;
    }
}
