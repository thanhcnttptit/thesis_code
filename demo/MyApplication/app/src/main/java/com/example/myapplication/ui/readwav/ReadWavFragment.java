package com.example.myapplication.ui.readwav;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.myapplication.R;
import com.example.myapplication.helper.ResponseLabel;
import com.example.myapplication.helper.Utils;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class ReadWavFragment extends Fragment {

    private ReadWavViewModel readWavViewModel;

    Button chooseBtn;
    TextView pathnameTxt;
    Intent myFileIntent;
    ProgressBar bar;
    ArrayList<ResponseLabel> data;
    TableLayout tableLayout;
    View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        readWavViewModel =
                ViewModelProviders.of(this).get(ReadWavViewModel.class);
        root = inflater.inflate(R.layout.fragment_readwav, container, false);

        chooseBtn = root.findViewById(R.id.wavAct_chooseWavBtn);
        pathnameTxt = root.findViewById(R.id.wavAct_pathnameTxt);
        bar = root.findViewById(R.id.wavAct_progressBar);
        bar.setVisibility(View.GONE);

        tableLayout = root.findViewById(R.id.displayResultAct_tableLayout);
        chooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                myFileIntent.setType("*/*");
                startActivityForResult(myFileIntent, 10);
            }
        });

        readWavViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                Log.d("viewmodel", "changed "+s);
                if (s != null)
                    pathnameTxt.setText(s.split("\\/")[s.split("\\/").length-1]);
            }
        });

        readWavViewModel.getLabels().observe(this, new Observer<ArrayList<ResponseLabel>>() {
            @Override
            public void onChanged(@Nullable ArrayList<ResponseLabel> labels) {
                if (labels != null) {


                    int k = tableLayout.getChildCount();

                    for (int i = 1; i < k; i++)
                        tableLayout.removeViewAt(1);

                    for (ResponseLabel r: labels) {
                        TableRow tr = new TableRow(getActivity().getApplicationContext());

                        String colorCode;

                        if (r.getLabel().equals("Wheeze"))
                            colorCode = "#001f3f";
                        else if (r.getLabel().equals("Breath"))
                            colorCode = "#0074D9";
                        else colorCode = "#7FDBFF";

                        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                        tr.setLayoutParams(layoutParams);

                        TextView tv1 = new TextView(getActivity().getApplicationContext());
                        tv1.setText(r.getStartTime()+" s");
                        tv1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT, 1f));
                        tv1.setGravity(Gravity.CENTER);
                        tv1.setTextColor(Color.parseColor(colorCode));
                        tr.addView(tv1);

                        TextView tv2 = new TextView(getActivity().getApplicationContext());
                        tv2.setText(r.getEndTime()+" s");
                        tv2.setTextColor(Color.parseColor(colorCode));
                        tv2.setGravity(Gravity.CENTER);
                        tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT, 1f));
                        tr.addView(tv2);

                        TextView tv3 = new TextView(getActivity().getApplicationContext());
                        tv3.setText(r.getLabel());
                        tv3.setTextColor(Color.parseColor(colorCode));
                        tv3.setGravity(Gravity.CENTER);
                        tv3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT, 1f));
                        tr.addView(tv3);

                        tableLayout.addView(tr);
                    }


                    Log.d("haha", labels.size()+"");

                }

            }
        });


        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10 && resultCode == RESULT_OK) {
            String path = Utils.getActualPath(getActivity(), data.getData());

            ReadWavAsync readWavAsync = new ReadWavAsync(getActivity(), path, bar, chooseBtn, tableLayout, readWavViewModel);
            readWavAsync.execute();

            pathnameTxt.setText(path.split("\\/")[path.split("\\/").length-1]);



        }

    }
}