package com.example.fittune.Model;

public class Userprofile {
    public String bio;
    public String name;
    public String distance;
    public String userId;
    public String storageRef;
    public String duration;
    public String pace;
    public String calories;
    public String mon;
    public String tue;
    public String wed;
    public String thu;
    public String fri;
    public String sat;
    public String sun;
    public String musicStyle;


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

    public Userprofile(String userbio, String username, String dist, String id,
                       String ref, String dur, String pa, String cal, String mo,
                       String tu, String we, String th, String fr, String sa, String su){
        this.bio=userbio;
        this.name=username;
        this.distance=dist;
        this.userId = id;
        this.storageRef = ref;
        this.duration = dur;
        this.pace = pa;
        this.calories = cal;
        this.mon = mo;
        this.tue = tu;
        this.wed = we;
        this.thu = th;
        this.fri = fr;
        this.sat = sa;
        this.sun = su;
    }

    public Userprofile(String userbio, String username, String dist, String id,
                       String ref, String dur, String pa, String cal, String mo,
                       String tu, String we, String th, String fr, String sa, String su, String music){
        this.bio=userbio;
        this.name=username;
        this.distance=dist;
        this.userId = id;
        this.storageRef = ref;
        this.duration = dur;
        this.pace = pa;
        this.calories = cal;
        this.mon = mo;
        this.tue = tu;
        this.wed = we;
        this.thu = th;
        this.fri = fr;
        this.sat = sa;
        this.sun = su;
        this.musicStyle = music;
    }

    public String getBio() {
        return bio;
    }

    public void setbio(String userbio) {
        this.bio = userbio;
    }

    public String getName() {
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

    public String getMon() {
        return mon;
    }

    public String getTue() {
        return tue;
    }

    public String getWed() {
        return wed;
    }

    public String getThu() {
        return thu;
    }

    public String getFri() {
        return fri;
    }

    public String getSat() {
        return sat;
    }

    public String getSun() {
        return sun;
    }

    public String getMusicStyle() { return musicStyle; }
}
