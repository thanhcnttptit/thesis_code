package com.example.myapplication.helper;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RequestLabelApi {
    @Headers("Content-Type: application/json")
    @POST("api")
    Call<ResponseLabel> detectLabel(@Body AudioData audioData);

    @Headers("Content-Type: application/json")
    @POST("api_wav")
    Call<ResponseLabel[]> detectWav(@Body AudioData audioData);
}
