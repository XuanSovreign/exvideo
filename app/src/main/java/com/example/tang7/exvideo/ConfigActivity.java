package com.example.tang7.exvideo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 配置文件
 * IP：port
 * Created by licht on 2018/12/10.
 */

public class ConfigActivity extends Activity {

    private Button mBtnConnection;
    private EditText mEdtIp;
    private SharedPreferences mSp;
    private EditText mEdtPort;
    private EditText mEdtDuration;
    private EditText mEdtCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_config);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        mEdtIp = findViewById(R.id.edt_ip);
        mEdtPort = findViewById(R.id.edt_port);
        mEdtDuration = findViewById(R.id.edt_duration);
        mEdtCount = findViewById(R.id.edt_count);
        mBtnConnection = findViewById(R.id.btv_connection);
    }

    private void initData() {
        mSp = getSharedPreferences("oppovideo", MODE_PRIVATE);
        String ip = mSp.getString(Constants.IP_NAME, "");
        int port = mSp.getInt(Constants.PORT_NAME, 10088);
        int duration = mSp.getInt(Constants.DURATION_NAME,200);
        int count = mSp.getInt(Constants.COUNT_NAME,5);
        mEdtPort.setText(port+"");
        mEdtDuration.setText(duration+"");
        mEdtCount.setText(count+"");
        if (!TextUtils.isEmpty(ip)) {
            startActivity(new Intent(this,DirectVideoActivity.class));
            finish();
            Log.e("==========", "initData:3 " );
        }
    }

    private void initListener() {
        mBtnConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionIp();
            }
        });
    }

    private void connectionIp() {
        String inputIp = mEdtIp.getText().toString().trim();
        String portStr = mEdtPort.getText().toString().trim();
        String durationStr = mEdtDuration.getText().toString().trim();
        String countStr = mEdtCount.getText().toString().trim();
        if (TextUtils.isEmpty(inputIp)) {
            Toast.makeText(this,"ip地址不为空",Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(portStr)) {
            Toast.makeText(this,"端口号不为空",Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(durationStr)) {
            Toast.makeText(this,"忽略时间不为空",Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(countStr)) {
            Toast.makeText(this,"次数不为空",Toast.LENGTH_SHORT).show();
            return;
        }
        int port = Integer.parseInt(portStr);
        int duration = Integer.parseInt(durationStr);
        int count = Integer.parseInt(countStr);
        saveIp(inputIp,port,duration,count);
    }

    private void saveIp(String inputIp, int port, int duration, int count) {
        SharedPreferences.Editor editor = mSp.edit();
        editor.putString(Constants.IP_NAME,inputIp);
        editor.putInt(Constants.PORT_NAME,port);
        editor.putInt(Constants.DURATION_NAME,duration);
        editor.putInt(Constants.COUNT_NAME,count);
        editor.apply();
        startActivity(new Intent(this,DirectVideoActivity.class));
        finish();
    }
}
