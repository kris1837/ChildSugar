package com.example.childsugar.nfc_app_2;

/**
 * Created by kirill on 5/15/17.
 */

public class HistoryData {
    public long historyId;
    public double glucoseLevel;
    public long time;
    private String foodName;
    private String amount;
    int foodtype = 0; // Default - 0;

    public HistoryData(){};

    public HistoryData(long historyId, double glucoseLevel, long time, String foodName, String amount, int foodtype) {
        this.historyId = historyId;
        this.glucoseLevel = glucoseLevel;
        this.time = time;
        this.foodName = foodName;
        this.amount = amount;
        this.foodtype = foodtype;
    }

    public long getHistoryId() {
        return historyId;
    }

    public double getGlucoseLevel() {
        return glucoseLevel;
    }

    public long getTime() {
        return time;
    }

    public String getFoodName() {
        return foodName;
    }

    public String getAmount() {
        return amount;
    }

    public int getFoodtype() {
        return foodtype;
    }

    public void setHistoryId(long historyId) {
        this.historyId = historyId;
    }

    public void setGlucoseLevel(double glucoseLevel) {
        this.glucoseLevel = glucoseLevel;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setFoodtype(int foodtype) {
        this.foodtype = foodtype;
    }
}
