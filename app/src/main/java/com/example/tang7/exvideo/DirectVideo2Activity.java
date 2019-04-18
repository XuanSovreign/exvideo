package com.example.tang7.exvideo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by licht on 2018/12/5.
 */

public class DirectVideo2Activity extends Activity {
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private VideoView video;
    private String outPath;
    private int first = 1;

    private Socket socket = null;
    private OutputStream outputStream;
    private String dataStr = "gettime";
    private InputStream inputStream;

    ScheduledExecutorService mExecutorService;
    private ConnectServerRunnable mConnetServerRunnable;
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
//                    Toast.makeText(DirectVideoActivity.this,"空",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

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
        mExecutorService = new ScheduledThreadPoolExecutor(10);
        getPermission();
        initListener();
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


    private void connectService() {

        try {
            if (socket == null) {
                socket = new Socket("192.168.30.205", 10088);
            }
        } catch (IOException e) {
            Log.e("", "");
        }
    }

    private boolean isServiceColse() {
        try {
            if (socket != null) {
                socket.sendUrgentData(0);
            }
            return false;
        } catch (IOException e1) {
            e1.printStackTrace();
            return true;
        }
    }


    private void parseStr(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            String localtime = jsonObject.getString("Localtime");
            double videotime = jsonObject.getDouble("Videotime");
            Log.e("gggggggggggg", "parseStr: " + localtime + "=============" + videotime);

            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSSS");
            String nowTimeStr = dateFormat.format(new Date());
            long time = TimeUtil.string2Milliseconds(localtime, dateFormat);
            long nowTime = TimeUtil.string2Milliseconds(nowTimeStr, dateFormat);
            Log.e("hhhhhhhhhhhh", "parseStr: " + time + "==================" + nowTime);
            long playTime = nowTime - time + (long) videotime * 1000;
            Log.e("jjjjjjjjjjjjjjj", "parseStr: " + playTime);
            video.seekTo((int) playTime);
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

        if (mConnetServerRunnable == null) {
            mConnetServerRunnable = new ConnectServerRunnable(socket);
        }
        mExecutorService.execute(mConnetServerRunnable);

//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                backThread();
//                mHandler.postDelayed(this, 120000);
//            }
//        }, 0);
        MediaController mediaController = new MediaController(this);
        video.setMediaController(mediaController);
        video.setVideoPath(outPath);
        video.seekTo(0);
        video.requestFocus();
        video.start();
    }

    private void initListener() {
        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        closeResource();
    }

    private void closeResource() {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
            outputStream = null;
            inputStream = null;
            socket = null;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class ConnectServerRunnable implements Runnable {
        private Socket mSocket;

        public ConnectServerRunnable(Socket socket) {
            mSocket = socket;
        }

        @Override
        public void run() {
            try {
                if (mSocket == null || mSocket.isClosed()) {
                    mSocket = new Socket(Constants.SERVER_IP, Constants.SERVER_PORT);
                }

                if (mSocket.isConnected()) {
                    Log.e("fffffffff", "connectService: ==============" + first);

                    if (mSocket.isClosed())
                        //发送getTime消息，第一次不发送
                        if (first == 1) {
                            first = 2;
                        } else {
                            //每10s执行一次发送getTime消息任务
                            if (outputStream == null) {
                                outputStream = mSocket.getOutputStream();
                                Log.e("mmmmmmmmmmmmm", "connectService: ==============");
                            }
                            outputStream.write(dataStr.getBytes());
                            Log.e("iiiiiiii", "connectService: ==============");
                        }

                    if (inputStream == null) {
                        inputStream = mSocket.getInputStream();
                        Log.e("llllllllll", "connectService: ==============");
                    }
                    byte[] bytes = new byte[128];
                    Log.e("eeeeeee", "connectService: " + bytes.length);
                    int len = 0;
                    while ((len = inputStream.read(bytes)) > 0) {
                        String message = new String(bytes, 0, len);
                        Log.e("ddddddddd", "connectService: " + message);
                        Message msg = Message.obtain();
                        msg.what = 0;
                        msg.obj = message;
                        mHandler.sendMessage(msg);
                    }
                } else {
                    Log.e("999999999999999", "check server is started");

                }
                mExecutorService.schedule(mConnetServerRunnable, 10, TimeUnit.SECONDS);
            } catch (IOException e) {//重连
                e.printStackTrace();
                Log.e("88888888888", "socket exception:" + e.getMessage());
                try {
                    outputStream.close();
                    inputStream.close();
                    //mSocket.close();
                    mSocket = null;
                } catch (IOException e1) {
                    e1.printStackTrace();
                } finally {
                    mExecutorService.schedule(mConnetServerRunnable, 10, TimeUnit.SECONDS);
                }
            }
        }
    }


}
