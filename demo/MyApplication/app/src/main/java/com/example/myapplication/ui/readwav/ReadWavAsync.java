package com.example.myapplication.ui.readwav;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.myapplication.R;
import com.example.myapplication.helper.AudioData;
import com.example.myapplication.helper.GlobalVariables;
import com.example.myapplication.helper.RequestLabelApi;
import com.example.myapplication.helper.ResponseLabel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.file.Files;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReadWavAsync extends AsyncTask<Void, Integer, Void> {

    Context context;
    String path;
    RandomAccessFile file;
    ArrayList<Float> listSamples;
    byte[] bytes;
    ProgressBar bar;
    Button chooseBtn;
    TableLayout tableLayout;
    ReadWavViewModel readWavViewModel;
    int[] data2;

    public ReadWavAsync(Context context, String path, ProgressBar bar, Button chooseBtn, TableLayout tableLayout, ReadWavViewModel readWavViewModel) {
        this.context = context;
        this.readWavViewModel = readWavViewModel;
        this.path = path;
        this.bar = bar;
        this.chooseBtn = chooseBtn;
        this.tableLayout = tableLayout;
    }



    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        int k = tableLayout.getChildCount();

        for (int i = 1; i < k; i++)
            tableLayout.removeViewAt(1);

        bar.setVisibility(View.VISIBLE);
        chooseBtn.setVisibility(View.GONE);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected Void doInBackground(Void... voids) {

        try {
            //Test region

            try{
                File file1 = new File(path + "xxx");
                int size = (int) file1.length();
                bytes = new byte[size];
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file1));
                buf.read(bytes, 0, bytes.length);
                data2 = new int[bytes.length];
                for (int i = 0; i < bytes.length; i++) {
                    data2[i] = bytes[i] & 0xFF;
                }

                buf.close();
            }catch (Exception e1)
            {
                Log.d("fffff", "ffff");
                return null;
            }
            //End Test region
            file = new RandomAccessFile(path,"r");
            for (int i = 0; i < 58; i++) {
                try {
                    file.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        listSamples = new ArrayList<>();
        ArrayList<Double> listSamples1 = new ArrayList<>();
        while (true) {
            try {
                //byte[] bb = new byte[8];
                //file.readFully(bb);

                //float foo = byteArrayToFloat(bb);
                //float foo = ByteBuffer.wrap(bb).getFloat();
                //Log.d("kt_float", foo + " ");
                //Log.d("hihi", foo+"");
                //Log.d("ffff", "seperate");
                double foo = file.readDouble();
                Log.d("doc: ", foo + " ");
                listSamples1.add(foo);
            } catch (IOException e) {
                Log.d("test1", listSamples.size() + " ");
                break;
            }
        }
        return null;
    }



    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected void  onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        //float[] samples = convertIntegers(listSamples);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GlobalVariables.host)
                .addConverterFactory(GsonConverterFactory.create())
//                .client(client)
                .build();

        retrofit.create(RequestLabelApi.class)
                .detectWav(new AudioData(path))
                .enqueue(new Callback<ResponseLabel[]>() {
                    @Override
                    public void onResponse(Call<ResponseLabel[]> call, Response<ResponseLabel[]> response) {
                        ResponseLabel[] responseLabels = response.body();
                        ArrayList<ResponseLabel> list = new ArrayList<>();

                        for (ResponseLabel r: responseLabels) {
                            list.add(r);
                            TableRow tr = new TableRow(context);

                            String colorCode;

                            if (r.getLabel().equals("Wheeze"))
                                colorCode = "#001f3f";
                            else if (r.getLabel().equalsIgnoreCase("Other Lung Sounds"))
                                colorCode = "#d90000";
                            else colorCode = "#d90000";

                            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                            tr.setLayoutParams(layoutParams);

                            TextView tv1 = new TextView(context);
                            tv1.setText(r.getStartTime()+" s");
                            tv1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT, 1f));
                            tv1.setGravity(Gravity.CENTER);
                            tv1.setTextColor(Color.parseColor(colorCode));
                            tr.addView(tv1);

                            TextView tv2 = new TextView(context);
                            tv2.setText(r.getEndTime()+" s");
                            tv2.setTextColor(Color.parseColor(colorCode));
                            tv2.setGravity(Gravity.CENTER);
                            tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT, 1f));
                            tr.addView(tv2);

                            TextView tv3 = new TextView(context);
                            tv3.setText(r.getLabel());
                            tv3.setTextColor(Color.parseColor(colorCode));
                            tv3.setGravity(Gravity.CENTER);
                            tv3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT, 1f));
                            tr.addView(tv3);

                            tableLayout.addView(tr);
                        }


                        Log.d("haha", responseLabels.length+"");

                        readWavViewModel.setmText(path);
                        readWavViewModel.setLables(list);

                        bar.setVisibility(View.GONE);
                        chooseBtn.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(Call<ResponseLabel[]> call, Throwable t) {
                        showToast("Can not connect to server...");
                        t.printStackTrace();

                        bar.setVisibility(View.GONE);
                        chooseBtn.setVisibility(View.VISIBLE);
                    }
                });
    }


    void showToast(String str) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    private float[] convertIntegers(ArrayList<Float> listSamples) {

        float[] ret = new float[listSamples.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = listSamples.get(i);
        }
        return ret;
    }

    private float byteArrayToFloat(byte[] bytes) {
        int intBits =
                bytes[3] << 24 | (bytes[2] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF);
        return Float.intBitsToFloat(intBits);
    }
}
