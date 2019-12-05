package com.example.myapplication.ui.record;

import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.helper.AudioData;
import com.example.myapplication.helper.GlobalVariables;
import com.example.myapplication.helper.RequestLabelApi;
import com.example.myapplication.helper.ResponseLabel;
import com.example.myapplication.ui.readwav.ReadWavFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RecordFragment extends Fragment {
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_DURATION_MS = 1500;
    private static final int RECORDING_LENGTH = (int) (SAMPLE_RATE * SAMPLE_DURATION_MS / 1000);
    private static final long MINIMUM_TIME_BETWEEN_SAMPLES_MS = 3;

    private static final String LOG_TAG = "LOG";
    private float[] recordingBuffer = new float[RECORDING_LENGTH];
    //private short[] recordingBuffer = new short[RECORDING_LENGTH];
    private int recordingOffset = 0;
    private boolean shouldContinue = true;
    private Thread recordingThread;
    private boolean shouldContinueRecognition = true;
    private Thread recognitionThread;
    private final ReentrantLock recordingBufferLock = new ReentrantLock();
    int count;
    View layout;
    boolean isRecording;

    private RecordViewModel recordViewModel;
    TextView labelTxt;
    Button startRecordBtn;
    Button stopRecordBtn;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        recordViewModel =
                ViewModelProviders.of(this).get(RecordViewModel.class);
        View root = inflater.inflate(R.layout.fragment_record, container, false);
        labelTxt = root.findViewById(R.id.recordAct_labelTxt);
        startRecordBtn = root.findViewById(R.id.recordAct_startRecordBtn);
        isRecording = false;
        layout = root.findViewById(R.id.recordAct_layout);
        stopRecordBtn = root.findViewById(R.id.recordAct_stopRecordBtn);
        recordViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                labelTxt.setText(s);
            }
        });

        startRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording)
                    return;
                isRecording = true;
                showToast("Started Recording...");
                startRecording();
                startRecognition();
            }
        });
        stopRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecordHandling();
            }
        });
        return root;
    }

    private void stopRecordHandling() {
        if (!isRecording)
            return;
        isRecording = false;
        showToast("Stopped Recording...");
        stopRecording();
        stopRecognition();
        layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
        labelTxt.setText("Unknown");
        labelTxt.setTextColor(Color.parseColor("#111111"));
    }



    @Override
    public void onPause() {
        super.onPause();
        stopRecordHandling();
    }

    @Override
    public void onStop () {
        //do your stuff here
        super.onStop();
        stopRecordHandling();
    }

    void showToast(String message) {
        Toast.makeText( getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public synchronized void startRecording() {
        if (recordingThread != null) {
            return;
        }
        shouldContinue = true;
        recordingThread =
                new Thread(
                        new Runnable() {
                            @RequiresApi(api = Build.VERSION_CODES.M)
                            @Override
                            public void run() {
                                record();
                            }
                        });
        recordingThread.start();
    }

    public synchronized void stopRecording() {
        if (recordingThread == null) {
            return;
        }
        shouldContinue = false;
        recordingThread = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void record() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        // Estimate the buffer size we'll need for this device.
        int bufferSize =
                AudioRecord.getMinBufferSize(
                        SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT);
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }
        //short[] audioBuffer = new short[bufferSize / 2];
        float[] audioBuffer = new float[bufferSize / 2];
        AudioRecord record =
                new AudioRecord(
                        MediaRecorder.AudioSource.DEFAULT,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_FLOAT,
                        bufferSize);

        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(LOG_TAG, "Audio Record can't initialize!");
            return;
        }

        record.startRecording();

        Log.v(LOG_TAG, "Start recording");

        // Loop, gathering audio data and copying it to a round-robin buffer.
        while (shouldContinue) {
            int numberRead = record.read(audioBuffer, 0, audioBuffer.length, AudioRecord.READ_BLOCKING);
            //int numberRead = record.read(audioBuffer, 0, audioBuffer.length);
            int maxLength = recordingBuffer.length;
            int newRecordingOffset = recordingOffset + numberRead;
            int secondCopyLength = Math.max(0, newRecordingOffset - maxLength);
            int firstCopyLength = numberRead - secondCopyLength;
            // We store off all the data for the recognition thread to access. The ML
            // thread will copy out of this buffer into its own, while holding the
            // lock, so this should be thread safe.
            recordingBufferLock.lock();
            try {
                System.arraycopy(audioBuffer, 0, recordingBuffer, recordingOffset, firstCopyLength);
                System.arraycopy(audioBuffer, firstCopyLength, recordingBuffer, 0, secondCopyLength);
                recordingOffset = newRecordingOffset % maxLength;
            } finally {
                recordingBufferLock.unlock();
            }
        }

        record.stop();
        record.release();
    }

    public synchronized void startRecognition() {
        if (recognitionThread != null) {
            return;
        }
        shouldContinueRecognition = true;
        recognitionThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                recognize();
                            }
                        });
        recognitionThread.start();
    }

    public synchronized void stopRecognition() {
        if (recognitionThread == null) {
            return;
        }
        shouldContinueRecognition = false;
        recognitionThread = null;
    }
    private void recognize() {
        Log.v(LOG_TAG, "Start recognition");
        float[] inputBuffer = new float[RECORDING_LENGTH];
        //short[] inputBuffer = new short[RECORDING_LENGTH];

        // Loop, grabbing recorded data and running the recognition model on it.
        while (shouldContinueRecognition) {
            // The recording thread places data in this round-robin buffer, so lock to
            // make sure there's no writing happening and then copy it to our own
            // local version.
            recordingBufferLock.lock();
            try {
                int maxLength = recordingBuffer.length;
                int firstCopyLength = maxLength - recordingOffset;
                int secondCopyLength = recordingOffset;
                System.arraycopy(recordingBuffer, recordingOffset, inputBuffer, 0, firstCopyLength);
                System.arraycopy(recordingBuffer, 0, inputBuffer, firstCopyLength, secondCopyLength);
            } finally {
                recordingBufferLock.unlock();
            }

            // We need to feed in float values between -1.0f and 1.0f, so divide the
            // signed 16-bit inputs.
            float[] samples = new float[RECORDING_LENGTH];

            for (int i = 0; i < RECORDING_LENGTH; ++i) {
                if(samples[i] > 1 || samples[i] < -1)
                    Log.d("error", "testtt");
                samples[i] = inputBuffer[i];
                //samples[i] = inputBuffer[i]/ 32768.0f;
                //if (samples[i] > 1) samples[i] = 1;
                //if (samples[i] < -1) samples[i] = -1;
//                Log.d("haha", samples[i]+"");
            }

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(GlobalVariables.host)
                    .addConverterFactory(GsonConverterFactory.create())
//                .client(client)
                    .build();
            Log.d("nhandien", "gui thong tin " + System.currentTimeMillis());
            retrofit.create(RequestLabelApi.class)
                    .detectLabel(new AudioData(samples))
                    .enqueue(new Callback<ResponseLabel>() {
                        @Override
                        public void onResponse(Call<ResponseLabel> call, Response<ResponseLabel> response) {
                            Log.d("nhandien", "nhan duoc thong tin " + System.currentTimeMillis());
                            if (!isRecording)
                                return;

                            ResponseLabel responseLabel = response.body();


                            String colorCode;

                            if (responseLabel.getLabel().equals("Wheeze"))
                                colorCode = "#001f3f";
                            else if (responseLabel.getLabel().equals("Breath"))
                                colorCode = "#0074D9";
                            else colorCode = "#7FDBFF";
                            layout.setBackgroundColor(Color.parseColor(colorCode));
                            labelTxt.setTextColor(Color.parseColor("#ffffff"));
                            labelTxt.setText(responseLabel.getLabel());
                            float dl = ((new Date()).getTime() - responseLabel.getTime())/1000f;
                            Log.d("delay", dl+"");
                        }

                        @Override
                        public void onFailure(Call<ResponseLabel> call, Throwable t) {
                            showToast("Can not connect to server...");
                            t.printStackTrace();
                        }
                    });
            /*try {
                // We don't need to run too frequently, so snooze for a bit.
                Thread.sleep(MINIMUM_TIME_BETWEEN_SAMPLES_MS);
            } catch (InterruptedException e) {
                // Ignore
            }*/
        }
    }
}