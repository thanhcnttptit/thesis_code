package com.example.myapplication.ui.readwav;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.helper.ResponseLabel;

import java.util.ArrayList;

public class ReadWavViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<ArrayList<ResponseLabel>> mLabels;

    public ReadWavViewModel() {
        Log.d("viewmodel", "access");

        if (mText != null)
            Log.d("viewmodel", mText.getValue() != null ? mText.getValue() : "null");
        mText = new MutableLiveData<>();
        mLabels = new MutableLiveData<>();
    }

    public ReadWavViewModel(String txt, ArrayList<ResponseLabel> labels) {
        mText = new MutableLiveData<>();
        mLabels = new MutableLiveData<>();

        mText.postValue(txt);
        mLabels.postValue(labels);
    }

    public void setmText(String str) {
        mText.postValue(str);
    }

    public void setLables(ArrayList<ResponseLabel> lables) {
        mLabels.postValue(lables);
    }

    public LiveData<String> getText() {
        return mText;
    }
    public LiveData<ArrayList<ResponseLabel>> getLabels() {
        return mLabels;
    }

}