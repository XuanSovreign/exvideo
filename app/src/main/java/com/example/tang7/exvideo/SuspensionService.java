package com.example.tang7.exvideo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by licht on 2018/12/6.
 */

public class SuspensionService extends Service {

    private WindowManager.LayoutParams mParams;
    private WindowManager mWindowManager;
    private View mView;
    private Handler mHandler = new Handler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createToucher();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startIntent();
            }
        },1000);
        return super.onStartCommand(intent, flags, startId);
    }

    private void startIntent() {
        Intent intent = new Intent(this, DirectVideoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        stopSelf();
    }

    private void createToucher() {
        mParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        //设置type为系统窗口值
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        //设置背景透明
        mParams.format = PixelFormat.TRANSPARENT;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //设置窗口初始停靠位置.
        mParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        mParams.x=0;
        mParams.y=0;

        //设置窗口宽高
//        mParams.width = DPTool.dp2px(this, 200);
//        mParams.height = DPTool.dp2px(this, 200);
        mParams.width=1;
        mParams.height=1;

        mView = LayoutInflater.from(this).inflate(R.layout.suspension_window, null);
        mWindowManager.addView(mView,mParams);
        ImageView wIcon = mView.findViewById(R.id.iv_window_icon);
        initListener();

    }

    private long lastTime=0;
    private void initListener() {
        mView.setOnTouchListener(new View.OnTouchListener() {

            private float mDy;
            private float mDx;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mDx = event.getRawX();
                        mDy = event.getRawY();
                        if (System.currentTimeMillis() - lastTime>200) {
                            lastTime = System.currentTimeMillis();
                        } else {
                            Toast.makeText(getApplication(),"双击了",Toast.LENGTH_SHORT).show();
                        }
                        Log.e("=============", "onTouch: "+ mDx +"==============="+ mDy);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float moveX = event.getRawX();
                        float moveY = event.getRawY();
                        float dx=moveX-mDx;
                        float dy=moveY-mDy;
                        mDx=moveX;
                        mDy=moveY;
                        movePoint(dx,dy);
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return true;
            }
        });
    }

    private void movePoint(float moveX, float moveY) {
        mParams.x= mParams.x+(int) moveX;
        mParams.y= mParams.y+(int) moveY;
        mWindowManager.updateViewLayout(mView,mParams);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWindowManager.removeView(mView);
    }
}
