package com.example.sayyes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    AudioRecordButton arb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                                    "搜索", "跟踪", "治疗", "拍照", "识别"};
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

    }
}
