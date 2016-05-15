package com.cl.android.chartonlyforyou.password;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.cl.android.chartonlyforyou.MyApplication;
import com.cl.android.chartonlyforyou.R;
import com.cl.android.chartonlyforyou.baidulocation.LocationActivity;
import com.cl.android.chartonlyforyou.rongchart.ChartListActivity;
import com.cl.android.chartonlyforyou.rongchart.RongCloudEvent;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;

public class DrawPwdActivity extends AppCompatActivity {

    private LocusPassWordView mPwdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_pwd);
//        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences sharedPreferences = MyApplication.getSharedPreferences();
        mPwdView = (LocusPassWordView) this.findViewById(R.id.mPassWordView);
        mPwdView.setOnCompleteListener(new LocusPassWordView.OnCompleteListener() {
            @Override
            public void onComplete(String mPassword) {

                String pwd = sharedPreferences.getString("password", "");
                Md5Utils md5 = new Md5Utils();
                boolean passed = false;
                if (pwd.length() == 0) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("password", md5.toMd5(mPassword, ""));
                    editor.commit();
                    passed = true;
                } else {
                    String encodedPwd = md5.toMd5(mPassword, "");
                    if (encodedPwd.equals(pwd)) {
                        passed = true;
                    } else {
                        mPwdView.markError();
                    }
                }

                if (passed) {
                    //这里进行融云的连接,进入新界面
                    connectRongyun(MyApplication.token02);
                    /*Intent intent = new Intent(DrawPwdActivity.this,
                            LocationActivity.class);
                    startActivity(intent);
                    finish();*/
                }
            }
        });
    }

    private void connectRongyun(String token) {
            RongIM.connect(token, new RongIMClient.ConnectCallback() {
                @Override
                public void onSuccess(String s) {
                    Log.i("slack", "success:" + s);
                    RongCloudEvent.init(getApplicationContext()).initListener();
//                    startActivity(new Intent(DrawPwdActivity.this, LocationActivity.class));
                    startActivity(new Intent(DrawPwdActivity.this, ChartListActivity.class));
                    finish();
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {
                    //目前登录错误就直接退出
                    finish();
                }

                @Override
                public void onTokenIncorrect() {

                }
            });

    }
}
