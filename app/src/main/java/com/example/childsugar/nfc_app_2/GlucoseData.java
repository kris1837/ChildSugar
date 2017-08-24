package com.example.childsugar.nfc_app_2;


public class GlucoseData {

    //private
    int _id;
    long _date;
    double _glucose_level;
    String _comment;

    //public
    public enum Prediction{FALLING, FALLING_SLOW, CONSTANT, RISING_SLOW, RISING };

    // Empty constructor
    public GlucoseData(){

    }
    // constructor
    public GlucoseData(int id, long date, double glucose_level,  String comment){
        this._id = id;
        this._date = date;
        this._glucose_level = glucose_level;
        this._comment = comment;
    }


    // getting ID
    public int getID(){
        return this._id;
    }

    // setting id
    public void setID(int id){
        this._id = id;
    }

    // getting date
    public long getDate(){
        return this._date;
    }

    // setting date
    public void setDate(long date){
        this._date = date;
    }

    // getting glucose level
    public double getGlucoseLevel(){
        return this._glucose_level;
    }


    // setting glucose level
    public void setGlucoseLevel(double glucose_level){
        this._glucose_level = glucose_level;
    }

    // getting comment
    public String getComment(){
        return this._comment;
    }

    // setting comment
    public void setComment(String comment){
        this._comment = comment;
    }

}
