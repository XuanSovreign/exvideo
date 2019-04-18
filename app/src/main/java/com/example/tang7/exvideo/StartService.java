package com.example.tang7.exvideo;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by licht on 2019/3/8.
 */

public class StartService extends Service {
    private Handler mHandler=new Handler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startIntent();
            }
        },1000);
        return START_STICKY;
    }

    private void startIntent() {
        Intent intent = new Intent(this, DirectVideoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        stopSelf();
    }
}
