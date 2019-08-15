package com.example.yes;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import static com.example.yes.MainActivity.Currentlabel;

public class Utils {
    public static List<String> getFilesAllName(String path) {
        File file=new File(path);
        File[] files=file.listFiles();
        if (files == null){
            Log.e("error","空目录");return null;}
        List<String> s = new ArrayList<>();
        for(int i =0;i<files.length;i++){
            s.add(files[i].getAbsolutePath());
        }
        return s;
    }

    public static void writeShortToData(short[] verts, int count) {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "SayYes" + File.separator);
        if(!file.exists()){
            file.mkdirs();
        }
        String mCurrentFilePathString = Environment.getExternalStorageDirectory() + File.separator + "SayYes" + File.separator + generalFileName();
        file = new File(mCurrentFilePathString);
        //如果存在，就先删除再创建
        if (file.exists()){
            file.delete();
            Log.i("writeShortToData","删除文件");
        }

        try {
            file.createNewFile();
            Log.i("writeShortToData","创建文件");
        } catch (IOException e) {
            Log.i("writeShortToData","未能创建");
            throw new IllegalStateException("未能创建" + file.toString());
        }

        try {
            RandomAccessFile aFile = new RandomAccessFile(mCurrentFilePathString, "rw");
            FileChannel outChannel = aFile.getChannel();
            //one float 4 bytes
            ByteBuffer buf = ByteBuffer.allocate(2 * count);
            buf.clear();
            buf.asShortBuffer().put(verts);

            outChannel.write(buf);

            buf.rewind();
            outChannel.close();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private static String generalFileName() {
        return Currentlabel + System.currentTimeMillis() + ".pcm";
    }
}
