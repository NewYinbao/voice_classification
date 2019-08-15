package com.example.sayyes;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static com.example.sayyes.Utils.writeShortToData;
import static java.lang.Math.abs;
import static java.lang.Math.log10;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static String Currentlabel;
    private static final String LOG_TAG = "NYB: ";

    // ui 组件
    private Button btnstartRecording, btnstopRecording;
    private TextView textresult, textrecording, textrecognize, textvoicedb;
    private SeekBar sb_normal;
    private TextView textthrestold;
    private ToggleButton tbtn_openrecord;
    // dont known
    private static final int REQUEST_RECORD_AUDIO = 13;

    private List<String> labels = new ArrayList<String>();
    private static final String LABEL_FILENAME = "file:///android_asset/conv_actions_labels.txt";
    private static final String MODEL_FILENAME = "file:///android_asset/conv_actions_frozen.tflite";
//    private Interpreter tfLite;
    private boolean recognizflag =false;

    // 录音相关
    boolean shouldContinue = true;
    private Thread recordingThread;
    private final ReentrantLock recordingBufferLock = new ReentrantLock();
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_DURATION_MS = 1000;
    private static final int RECORDING_LENGTH = (int) (SAMPLE_RATE * SAMPLE_DURATION_MS / 1000);

    // 缓存相关
    private double voicethreshold = 40;
    private int numChunck = 0;
    private int recordingOffset = 0;
    private int recognizeOffset = -1;
    short[] recordingBuffer = new short[RECORDING_LENGTH];

    //测试用
    private int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



//        String actualModelFilename = MODEL_FILENAME.split("file:///android_asset/", -1)[1];
//        try {
//            tfLite = new Interpreter(loadModelFile(getAssets(), actualModelFilename));
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        // Load the labels for the model
//        String actualLabelFilename = LABEL_FILENAME.split("file:///android_asset/", -1)[1];
//        Log.i(LOG_TAG, "Reading labels from: " + actualLabelFilename);
//        BufferedReader br = null;
//        try {
//            br = new BufferedReader(new InputStreamReader(getAssets().open(actualLabelFilename)));
//            String line;
//            while ((line = br.readLine()) != null) {
//                labels.add(line);
//            }
//            br.close();
//        } catch (IOException e) {
//            throw new RuntimeException("Problem reading label file!", e);
//        }


        initUI();
        requestMicrophonePermission();

    }

    private void initUI(){

        btnstartRecording = (Button)findViewById(R.id.btnstartRecording);
        btnstartRecording.setOnClickListener(this);

        btnstopRecording = (Button)findViewById(R.id.btnstopRecording);
        btnstopRecording.setOnClickListener(this);

        textrecognize = (TextView)findViewById(R.id.textRecognizestate);
        textrecording = (TextView)findViewById(R.id.textRcordingstate);
        textresult = (TextView)findViewById(R.id.textRecognizeresult);
        textvoicedb = (TextView)findViewById(R.id.textvoicedb);

        // 开关
        tbtn_openrecord = (ToggleButton) findViewById(R.id.tbtn_openrecord);
        tbtn_openrecord.setOnCheckedChangeListener(MainActivity.this);
        // 滑条
        sb_normal = (SeekBar) findViewById(R.id.sb_normal);
        textthrestold = (TextView) findViewById(R.id.textvoicethreshold);
        sb_normal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textthrestold.setText("音量阈值:" + progress + " db");
                voicethreshold = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
//                Toast.makeText(MainActivity.this, "触碰SeekBar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                Toast.makeText(MainActivity.this, "放开SeekBar", Toast.LENGTH_SHORT).show();

            }
        });


        // 下拉菜单
        // 初始化控件
        Spinner spinner = findViewById(R.id.TestSpinner);
        // 建立数据源
        final String[] labels = {   "一号", "二号", "三号", "四号", "全体",
                "前进", "后退", "上升", "下降", "向左", "向右", "左转", "右转", "停止",
                "搜索", "跟踪", "治疗", "拍照", "识别", "其他"};
        // 建立Adapter并且绑定数据源
        ArrayAdapter<String> adapter= new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //绑定 Adapter到控件
        spinner .setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                Currentlabel = labels[pos];

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });


    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.btnstartRecording:
                Toast.makeText(MainActivity.this,"btnstartRecording", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnstopRecording:
                Toast.makeText(MainActivity.this,"btnstopRecording", Toast.LENGTH_SHORT).show();
                break;

        }
    }

    private synchronized void startrecord(){
        if (recordingThread != null) {
            return;
        }
        shouldContinue = true;
        recordingThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                record();
                            }
                        });
        recordingThread.start();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textrecording.setText("录音状态：正在录音...");
            }
        });
    }

    /*
        录音函数
     */
    private void record() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        // Estimate the buffer size we'll need for this device.
        int bufferSize =
                AudioRecord.getMinBufferSize(
                        SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            // 默认chunk大小
            bufferSize = 40 / 1000 * SAMPLE_RATE;
        }

        Log.v(LOG_TAG, "recording chunk bufferSize = " + bufferSize);
        short[] chunkBuffer = new short[bufferSize / 2];

        AudioRecord recorder =
                new AudioRecord(
                        MediaRecorder.AudioSource.DEFAULT,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize);

        if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,
                            "Audio Record can't initialize!!", Toast.LENGTH_LONG).show();
                }
            });
            Log.e(LOG_TAG, "Audio Record can't initialize!");
            return;
        }

        recorder.startRecording();
        Log.v(LOG_TAG, "Start recording");

        short olddata = 0, nowdata = 0;
        // Loop, gathering audio data and copying it to a round-robin buffer.
        while (shouldContinue) {
            double voiceDB = 0;
            int numberRead = recorder.read(chunkBuffer, 0, chunkBuffer.length);
            int maxLength = recordingBuffer.length;
            int newRecordingOffset = recordingOffset + numberRead;
            int secondCopyLength = Math.max(0, newRecordingOffset - maxLength);
            int firstCopyLength = numberRead - secondCopyLength;
            // We store off all the data for the recognition thread to access. The ML
            // thread will copy out of this buffer into its own, while holding the
            // lock, so this should be thread safe.
            recordingBufferLock.lock();
            try {
                System.arraycopy(chunkBuffer, 0, recordingBuffer, recordingOffset, firstCopyLength);
                System.arraycopy(chunkBuffer, firstCopyLength, recordingBuffer, 0, secondCopyLength);
                recordingOffset = newRecordingOffset % maxLength;

                if(recognizeOffset == -1){
                    voiceDB = getPcmDB(chunkBuffer);

                    final double finalVoiceDB = voiceDB;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textvoicedb.setText("音量："+ finalVoiceDB);
                        }
                    });

                    if(voiceDB > voicethreshold){
                        recognizeOffset = recordingOffset - numberRead*2;
                        if (recognizeOffset<0){
                            recognizeOffset = maxLength + recognizeOffset;
                        }
                        numChunck = 0;
                    }
                }
                else{
                    numChunck ++;
                    if(numChunck >= 23){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                startrecognize();
                            }
                        }).start();
                    }
//                    }
                }
            } finally {
                recordingBufferLock.unlock();
            }

//            if(olddata != nowdata && recognizeOffset !=- 1){
//
//                startrecognize();
//                recognizeOffset = -1;
//            }

        }

        recorder.stop();
        recorder.release();
    }

    private static double getPcmDB(short[] Pcmdata){

        double db = 0;
        double sum = 0;
        for(short data : Pcmdata){
            sum += abs(data);
        }
        sum = sum / Pcmdata.length; //求平均值
        if(sum > 0)
        {
            db = (20.0*log10(sum));
        }
        return db;
    }


    private void startrecognize(){
        recognize();
    }

//    private void recognize() {
//
//        Log.v(LOG_TAG, "Start recognition");
//
//        short[] inputBuffer = new short[RECORDING_LENGTH];
//        float[][] floatInputBuffer = new float[RECORDING_LENGTH][1];
//        float[][] outputScores = new float[1][labels.size()];
//        int[] sampleRateList = new int[] {SAMPLE_RATE};
//
//        // Loop, grabbing recorded data and running the recognition model on it.
//        while (true) {
//            long startTime = new Date().getTime();
//            // The recording thread places data in this round-robin buffer, so lock to
//            // make sure there's no writing happening and then copy it to our own
//            // local version.
//            if (recognizflag){
//                recordingBufferLock.lock();
//                try {
//                    int maxLength = recordingBuffer.length;
//                    int firstCopyLength = maxLength - recognizeOffset;
//                    int secondCopyLength = recognizeOffset;
//                    System.arraycopy(recordingBuffer, recognizeOffset, inputBuffer, 0, firstCopyLength);
//                    System.arraycopy(recordingBuffer, 0, inputBuffer, firstCopyLength, secondCopyLength);
//                } finally {
//                    recordingBufferLock.unlock();
//                }
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        textrecognize.setText("识别状态：正在识别");
//                    }
//                });
//                Log.v(LOG_TAG, "End recognition");
//
//            }else{
//                try {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            textrecognize.setText("识别状态：未识别");
//                        }
//                    });
//
//                    // We don't need to run too frequently, so snooze for a bit.
//                    Thread.sleep(10);
//                } catch (InterruptedException e) {
//                    // Ignore
//                }
//            }
//
//
////            // We need to feed in float values between -1.0f and 1.0f, so divide the
////            // signed 16-bit inputs.
////            for (int i = 0; i < RECORDING_LENGTH; ++i) {
////                floatInputBuffer[i][0] = inputBuffer[i] / 32767.0f;
////            }
////
////            Object[] inputArray = {floatInputBuffer};
////            Map<Integer, Object> outputMap = new HashMap<>();
////            outputMap.put(0, outputScores);
////
////            // Run the model.
////            tfLite.runForMultipleInputsOutputs(inputArray, outputMap);
////
////            // Use the smoother to figure out if we've had a real recognition event.
////            long currentTime = System.currentTimeMillis();
////            final RecognizeCommands.RecognitionResult result =
////                    recognizeCommands.processLatestResults(outputScores[0], currentTime);
////            lastProcessingTimeMs = new Date().getTime() - startTime;
//        }
//    }


    private void recognize() {
        Log.v(LOG_TAG, "Start recognition");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textrecognize.setText("识别状态：正在识别");
            }
        });
        count ++;
        short[] inputBuffer = new short[RECORDING_LENGTH];

        //debug
        Log.v(LOG_TAG, "recognize:"+count);


        recordingBufferLock.lock();
        try {
            int maxLength = recordingBuffer.length;
            int firstCopyLength = maxLength - recognizeOffset;
            int secondCopyLength = recognizeOffset;
            System.arraycopy(recordingBuffer, recognizeOffset, inputBuffer, 0, firstCopyLength);
            System.arraycopy(recordingBuffer, 0, inputBuffer, firstCopyLength, secondCopyLength);
        } finally {
            recordingBufferLock.unlock();
        }

        writeShortToData(inputBuffer, inputBuffer.length);

        numChunck = 0;
        recognizeOffset = -1;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textrecognize.setText("识别状态：等待识别, "+recognizeOffset);
                textresult.setText("recognize:"+count);
            }
        });
        Log.v(LOG_TAG, "End recognition");
    }


    private synchronized void stoprecord(){
        if (recordingThread == null) {
            return;
        }
        shouldContinue = false;
        recordingThread = null;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textrecording.setText("录音状态：未开始录音");
            }
        });
    }


    private void requestMicrophonePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[] {android.Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_RECORD_AUDIO);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.tbtn_openrecord:
                if (buttonView.isChecked()) {
                    Toast.makeText(this, "开始录音", Toast.LENGTH_SHORT).show();
                    startrecord();
                } else {
                    Toast.makeText(this, "停止录音", Toast.LENGTH_SHORT).show();
                    stoprecord();
                }
                break;
        }
    }
}
