package com.example.tang7.exvideo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * Created by tang7 on 2018/11/13.
 */

public class VideoActivity extends Activity {

    private VideoView video;

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
        MediaController mediaController = new MediaController(this);
        Intent intent = getIntent();
        Uri videoUrl = intent.getParcelableExtra("videoUrl");
        video.setMediaController(mediaController);
        video.setVideoURI(videoUrl);
        video.seekTo(0);
        video.requestFocus();
        video.start();
        initListener();
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

}
