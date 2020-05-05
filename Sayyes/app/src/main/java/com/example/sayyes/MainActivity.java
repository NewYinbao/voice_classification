package com.example.sayyes;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    AudioRecordButton arb;
    private static final int REQUEST_RECORD_AUDIO = 13;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 创建目录
        isFolderExists(Environment.getExternalStorageDirectory() + File.separator + "SayYes");
        arb = findViewById(R.id.arb);
        arb.setAudioFinishRecorderListener(new AudioRecordButton.AudioFinishRecorderListener() {
            @Override
            public void onFinished(float seconds, String filePath) {
                Log.e("syw","filePath:"+filePath);
                Toast.makeText(MainActivity.this, "保存成功:"+"filePath:"+filePath, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNormal() {
                Log.e("syw","onNormal:");
            }

            @Override
            public void onRecord() {
                Log.e("syw","onRecord:");
            }

            @Override
            public void onCancel() {
                Log.e("syw","onCancel:");
            }

            @Override
            public void onShort() {
                Log.e("syw","onShort:");
            }
        });


        // 初始化控件
        Spinner spinner = (Spinner) findViewById(R.id.TestSpinner);
        // 建立数据源
        final String[] labels = {   "一号", "二号", "三号", "四号", "全体",
                                    "前进", "后退", "上升", "下降", "向左", "向右", "左转", "右转", "停止",
                                    "搜索", "跟踪", "治疗", "拍照", "识别","其他"};
        // 建立Adapter并且绑定数据源
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //绑定 Adapter到控件
        spinner .setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                myRecorder.Currentlabel = labels[pos];

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        requestMicrophonePermission();
    }
    private void requestMicrophonePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[] {android.Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_RECORD_AUDIO);
        }
    }

    private boolean isFolderExists(String strFolder)
    {
        File file = new File(strFolder);

        if (!file.exists())
        {
            if (file.mkdir())
            {
                return true;
            }
            else
                return false;
        }
        return true;
    }
}
