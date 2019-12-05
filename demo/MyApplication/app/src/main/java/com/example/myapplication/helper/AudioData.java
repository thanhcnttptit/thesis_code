package com.example.myapplication.helper;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class AudioData {
    @SerializedName("data")
    @Expose
    private float[] data;

    @SerializedName("time")
    @Expose
    private long time;
    @SerializedName("data1")
    @Expose
    private byte[] data1;
    @SerializedName("data2")
    @Expose
    private int[] data2;
    @SerializedName("name")
    @Expose
    private String name;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public AudioData(float[] data, long time) {
        this.data = data;
        this.time = time;
    }

    public float[] getData() {
        return data;
    }

    public void setData(float[] data) {
        this.data = data;
    }

    public AudioData(float[] data) {
        this.time = (new Date()).getTime() - 3000;
        this.data = data;
    }
    public AudioData(byte[] data1) {
        this.time = (new Date()).getTime() - 3000;
        this.data1 = data1;
    }
    public AudioData(int[] data2) {
        this.time = (new Date()).getTime() - 3000;
        this.data2 = data2;
    }
    public AudioData(String name) {
        this.time = (new Date()).getTime() - 3000;
        this.name = name;
    }
}
