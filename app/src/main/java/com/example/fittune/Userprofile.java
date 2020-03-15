package com.example.fittune;

public class Userprofile {
    public String bio;
    public String name;
    public String distance;
    public String userId;
    public String storageRef;
    public String duration;
    public String pace;
    public String calories;


    public Userprofile(){

    }

    public Userprofile(String userbio, String username){
        this.bio=userbio;
        this.name=username;
    }

    public Userprofile(String userbio, String username, String dist){
        this.bio=userbio;
        this.name=username;
        this.distance=dist;
    }

    public Userprofile(String userbio, String username, String dist, String id, String ref){
        this.bio=userbio;
        this.name=username;
        this.distance=dist;
        this.userId = id;
        this.storageRef = ref;
    }

    public Userprofile(String userbio, String username, String dist, String id,
                       String ref, String dur, String pa, String cal){
        this.bio=userbio;
        this.name=username;
        this.distance=dist;
        this.userId = id;
        this.storageRef = ref;
        this.duration = dur;
        this.pace = pa;
        this.calories = cal;
    }

    public String getbio() {
        return bio;
    }

    public void setbio(String userbio) {
        this.bio = userbio;
    }

    public String getname() {
        return name;
    }

    public void setname(String username) {
        this.name = username;
    }

    public String getDistance() {return distance;}

    public String getUserId() {return userId;}

    public String getStorageRef() {return storageRef;}

    public String getDuration() {return duration;}

    public String getPace() {return pace;}

    public String getCalories() {return calories;}
}
