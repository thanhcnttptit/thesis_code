package com.example.myapplication.helper;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ResponseLabel implements Serializable {
    @SerializedName("label")
    @Expose
    private String label;

    @SerializedName("time")
    @Expose
    private long time;

    @SerializedName("starttime")
    @Expose
    private float startTime;

    @SerializedName("endtime")
    @Expose
    private float endTime;

    public float getStartTime() {
        return startTime;
    }

    public void setStartTime(float startTime) {
        this.startTime = startTime;
    }

    public float getEndTime() {
        return endTime;
    }

    public void setEndTime(float endTime) {
        this.endTime = endTime;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public ResponseLabel(String label, float startTime, float endTime) {
        this.label = label;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ResponseLabel(String label) {
        this.label = label;
    }
}
