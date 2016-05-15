package com.cl.android.chartonlyforyou.rongchart;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;


import com.cl.android.chartonlyforyou.MyApplication;
import com.cl.android.chartonlyforyou.R;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.rong.imkit.RongIM;

/**
 * 聊天界面
 * Created by chenling on 2016/3/24.
 */
public class SettingFragment extends Fragment {
    //这里做一个单例
    public static SettingFragment settingFragment = null;
    private View view;
    private Switch aSwitch;
    private  EditText phone;
    public static SettingFragment getInstance(){
        if(settingFragment == null){
            settingFragment = new SettingFragment();
        }
        return settingFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        Log.i("slack", "HomeFragment onCreateView...............");
        view = inflater.inflate(R.layout.setting_fragment,null);
        view.findViewById(R.id.chartslack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RongIM.getInstance().startPrivateChat(getContext(), "chenling002", "私人聊天");
            }
        });
        view.findViewById(R.id.clearpassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("确定要重置?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                MyApplication.getSharedPreferences().edit().putString("password", "").commit();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });
        aSwitch = (Switch)view.findViewById(R.id.dnd);
        if(!TextUtils.isEmpty( MyApplication.getSharedPreferences().getString("DND","") )){
            aSwitch.setChecked(true);
        }
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //进入勿扰模式
                    MyApplication.getSharedPreferences().edit().putString("DND", "DND").commit();
                }else{
                    MyApplication.getSharedPreferences().edit().putString("DND", "").commit();
                }
            }
        });
        phone = (EditText) view.findViewById(R.id.verify_mobileNumber);
        phone.setText( MyApplication.getSharedPreferences().getString("tel","") );
        view.findViewById(R.id.save_phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phonenumber = phone.getText().toString();
                if (phonenumber != null && isMobileNumberValid(phonenumber)) {
                    MyApplication.getSharedPreferences().edit().putString("tel",phonenumber).commit();
                    Toast.makeText(getActivity(), "保存成功", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity(),"不是合法手机号",Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    /**
     * 验证手机号是否符合大陆的标准格式
     */
    public static boolean isMobileNumberValid(String mobiles) {
//        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Pattern p = Pattern.compile("^(1[3|4|5|7|8])\\d{9}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }
}
