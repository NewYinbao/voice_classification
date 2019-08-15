package com.example.sayyes;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

//录音核心类
public class myRecorder {

    public static String Currentlabel = "";
    private String TAG = "nyb: ";
    // 录音相关设置
    //16K采集率
    private int frequency = 16000;
    //格式
    private int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    //16Bit
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    //正在录音
    private boolean isRecording = false;
    //
    private int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
    private AudioRecord audioRecord;


    private MediaRecorder mRecorder;
    //文件夹位置
//    private String mDirString;
    //录音文件保存路径
    private String mCurrentFilePathString = Environment.getExternalStorageDirectory() + File.separator + "audioLibs";
    //是否真备好开始录音
    private boolean isPrepared;

    /**
     * 单例化这个类
     */
    private static myRecorder mInstance;

    private myRecorder() {
    }

    public static myRecorder getInstance() {
        if (mInstance == null) {
            synchronized (myRecorder.class) {
                if (mInstance == null) {
                    mInstance = new myRecorder();

                }
            }
        }
        return mInstance;

    }

    /**
     * 回调函数，准备完毕，准备好后，button才会开始显示录音
     */
    public interface AudioStageListener {
        void wellPrepared();
    }

    public AudioStageListener mListener;

    public void setOnAudioStageListener(AudioStageListener listener) {
        mListener = listener;
    }

    // 准备方法
    public void prepareAudio() {
        // 一开始应该是false的
        isPrepared = false;
        //创建所属文件夹
        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "audioLibs");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);

        // 准备结束
        isPrepared = true;
        // 已经准备好了，可以录制了
        if (mListener != null) {
            mListener.wellPrepared();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                StartRecord();
            }
        }).start();

    }


    /**
     * 随机生成文件的名称
     * @return
     */
    private String generalFileName() {
        return Currentlabel + System.currentTimeMillis() + ".pcm";
    }

    // 获得声音的level
    public int getVoiceLevel(int maxLevel) {
        if (isPrepared && null != mRecorder) {
            try {
                int ratio = mRecorder.getMaxAmplitude() / 600;
                int db = 0;// 分贝
                if (ratio > 1) {
                    db = (int) (20 * Math.log10(ratio));
                }
                int level = 1;
                switch (db / 4) {
                    case 0:
                        level = 1;
                        break;
                    case 1:
                        level = 2;
                        break;
                    case 2:
                        level = 3;
                        break;
                    case 3:
                        level = 4;
                        break;
                    case 4:
                        level = 5;
                        break;
                    case 5:
                        level = 6;
                        break;
                    default:
                        level = 7;
                        break;
                }
                return level;

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return 1;
    }

    // 释放资源
    public void release() {
        //mRecorder.release();
//        audioRecord = null;
        this.isRecording = false;

    }

    // 取消,因为prepare时产生了一个文件，所以cancel方法应该要删除这个文件，
    // 这是与release的方法的区别
    public void cancel() {
        release();
        if (mCurrentFilePathString != null) {
            File file = new File(mCurrentFilePathString);
            file.delete();
            mCurrentFilePathString = null;

        }
        Log.i(TAG, "录音取消");
    }

    public String getCurrentFilePath() {
        return mCurrentFilePathString;
    }


    public void StartRecord() {
        Log.i(TAG,"进入录音");
        //生成PCM文件
        mCurrentFilePathString = Environment.getExternalStorageDirectory() + File.separator + "audioLibs" + File.separator + generalFileName();
        File file = new File(mCurrentFilePathString);
        Log.i(TAG,"生成文件");
        //如果存在，就先删除再创建
        if (file.exists()){
            file.delete();
            Log.i(TAG,"删除文件");
        }

        try {
            file.createNewFile();
            Log.i(TAG,"创建文件");
        } catch (IOException e) {
            Log.i(TAG,"未能创建");
            throw new IllegalStateException("未能创建" + file.toString());
        }
        try {
            //输出流
            OutputStream os = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            DataOutputStream dos = new DataOutputStream(bos);

            short[] buffer = new short[bufferSize];
            audioRecord.startRecording();
            Log.i(TAG, "开始录音");
            this.isRecording = true;
            while (this.isRecording) {
                int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                for (int i = 0; i < bufferReadResult; i++) {
                    dos.writeShort(buffer[i]);
                }
            }
            Log.i(TAG, "录音结束");
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            dos.close();

        } catch (Throwable t) {
            Log.e(TAG, "录音失败");
        }
    }

    public void stop(){
        this.isRecording = false;

    }
}
