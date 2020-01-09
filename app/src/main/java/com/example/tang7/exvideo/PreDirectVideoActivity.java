package com.example.tang7.exvideo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 *同步播放视频2
 * Created by tang7 on 2018/11/13.
 */

public class PreDirectVideoActivity extends Activity {

    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private VideoView video;
    private String outPath;
    private int first = 0;
    private boolean isFirst = true;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    String parseStr = (String) msg.obj;
                    parseStr(parseStr);
                    break;
                case 1:
                    if (isFirst) {
                        SharedPreferences.Editor editor = mSp.edit();
                        editor.putString(Constants.IP_NAME, "");
                        editor.apply();
                        startActivity(new Intent(PreDirectVideoActivity.this, ConfigActivity.class));
                        finish();
                        isFirst = false;
                    }
                    break;
            }
        }
    };
    private SharedPreferences mSp;
    private String mIp;
    private long lastRecTime = 0;
    private long nowRecTime = 0;
    private int mPort;
    private int mDuration;
    private int mCount = 5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_video);
        video = findViewById(R.id.video);
        initData();
        getPermission();
        initListener();
    }

    private void initData() {
        mSp = getSharedPreferences("oppovideo", MODE_PRIVATE);
        mIp = mSp.getString(Constants.IP_NAME, "");
        mPort = mSp.getInt(Constants.PORT_NAME, 0);
        mDuration = mSp.getInt(Constants.DURATION_NAME, 0);
        mCount = mSp.getInt(Constants.COUNT_NAME, 0);
        if (TextUtils.isEmpty(mIp)) {
            startActivity(new Intent(this, ConfigActivity.class));
            finish();
            Log.e("==========", "initData:1 ");
        }
    }

    private void getPermission() {
        // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, permissions, 321);
        } else {
            startVideo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            startVideo();
        } else {
            Toast.makeText(this, "请申请权限", Toast.LENGTH_SHORT).show();
        }
    }

    private void backThread() {
        new Thread() {
            @Override
            public void run() {
                connectService();
            }
        }.start();
    }

    private Socket socket = null;
    private OutputStream outputStream;
    private String dataStr = "gettime";
    private InputStream inputStream;

    private void connectService() {

        try {
            if (socket == null) {
                socket = new Socket(mIp, mPort);
            }
            if (isServiceColse()) {
                Log.e("CCCdddCCC", "connectService: ");
            }

            if (inputStream == null) {
                inputStream = socket.getInputStream();
                Log.e("llllllllll", "connectService: ==============");
            }

            byte[] bytes = new byte[1024];
//            Log.e("eeeeeee", "connectService: " + bytes.length);
            int len = 0;
            isFirst = false;
            while ((len = inputStream.read(bytes)) > 0) {
                nowRecTime = System.currentTimeMillis();
                String message = new String(bytes, 0, len);
                Log.e("ddddddddd", "connectService: " + message);
                Message msg = Message.obtain();
                msg.what = 0;
                msg.obj = message;
                mHandler.sendMessage(msg);
            }

        } catch (IOException e) {
            Log.e("CCCCCC", "connectService: " + e.getMessage());
            mHandler.sendEmptyMessage(1);
            closeResource();
            connectService();
        }

    }

    private boolean isServiceColse() {
        try {
            if (socket != null) {
                socket.sendUrgentData(0);
                Log.e("CCCdddddddddddddCCC", "connectService: ");
            }
            return false;
        } catch (IOException e1) {
            e1.printStackTrace();
            return true;
        }
    }

    /**
     * 解析json字符串，同步时间，这个是从半路开始同步播放
     * @param message
     */
    private void parseStr(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            double localtime = jsonObject.getDouble("Localtime");
            double videotime = jsonObject.getDouble("Videotime");
            long playTime = 0;
            long videoTime = (long) (videotime * 1000);
            int currentPosition = video.getCurrentPosition();
            long restTime = nowRecTime - lastRecTime;
            Log.e("mmmmmmmm", "parseStr: localTime=" + localtime + "  videoTime=" + videotime + " videoTime2=" + videoTime);
            Log.e("mmmnnnnmm", "parseStr: nowRecTime=" + nowRecTime + "  lastRecTime=" + lastRecTime + "  rest=" + restTime);

            if (videoTime <= 13000) {
                videoTime += 10000;
            } else {
                videoTime -= 13000;
            }
            if (restTime + videoTime - currentPosition > mDuration) {
                if (restTime == nowRecTime) {
                    video.seekTo((int) videoTime);
                    Log.e("====++++++=======", "parseStr: ");
                }

                if (first >= mCount) {
                    Log.e("====++++++=======", "parseStr: currentPosition=" + currentPosition + "  seek=" + (restTime + videoTime) + "first=" + first);
                    video.seekTo((int) (restTime + videoTime));
                    first = 0;
                }
                Log.e("===========", "parseStr: ");
            }
            Log.e("ssssssss", "parseStr: " + currentPosition + "  duration:" + video.getDuration());
            lastRecTime = nowRecTime;
            first++;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean checkFile() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return false;
        }
        File directory = new File(Environment.getExternalStorageDirectory(), "/selfVideo");
        if (!directory.exists()) {
            if (directory.mkdir()) {
                Log.e("bbb", "checkFile: ");
            }
        }
        File videoFile = new File(directory, "/video.mp4");
        if (!videoFile.exists()) {
            return false;
        }
        outPath = videoFile.getAbsolutePath();
        return true;
    }

    private void startVideo() {
        if (!checkFile()) {
            Toast.makeText(this, "没有资源", Toast.LENGTH_SHORT).show();
            return;
        }

        backThread();
        video.setVideoPath(outPath);
        video.seekTo(1100);
        video.requestFocus();
        video.start();
    }

    private void initListener() {
        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                mp.start();
            }
        });

    }


    @Override
    protected void onDestroy() {
        closeResource();
        super.onDestroy();
    }

    private void closeResource() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
            inputStream = null;
            socket = null;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
