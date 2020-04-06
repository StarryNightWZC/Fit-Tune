package com.example.fittune.Model;

public class ExerciseBlock {

    private String name;
    private String value;

    public ExerciseBlock() {
    }

    public ExerciseBlock(String name, String value) {
        this.name=name;
        this.value=value;
    }

    public String getname() {
        return name;
    }
    public String getvalue() {
        return value;
    }

    public void setname(String name){
        this.name=name;
    }

    public void setValue(String value){
        this.value=value;
    }
}
