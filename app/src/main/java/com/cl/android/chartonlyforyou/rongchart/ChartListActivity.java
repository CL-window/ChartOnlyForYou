package com.cl.android.chartonlyforyou.rongchart;


import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.cl.android.chartonlyforyou.R;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.model.Conversation;

/**
 * 聊天会话列表界面，再加一个设置页面，设置页面就需要对设置信息进行文件保存了
 * */
public class ChartListActivity extends FragmentActivity {

    private Fragment conversationListFragment = null;//使用融云的会话列表
    private FragmentPagerAdapter fragmentPagerAdapter;
    private ViewPager viewPager;
    private List<Fragment> fragments = new ArrayList<Fragment>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_list);
        viewPager = (ViewPager)findViewById(R.id.viewpager);
        //融云会话列表对象，类型不对，是Fragment 的子类呀,原来是包导错了 import android.support.v4.app.Fragment;
        conversationListFragment = initConversationlist();

        fragments.add(conversationListFragment);
        fragments.add(SettingFragment.getInstance());

        fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()){
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }
            @Override
            public int getCount() {
                return fragments.size();
            }
        };
        viewPager.setAdapter(fragmentPagerAdapter);


    }
    //初始化融云会话列表,使用融云自带的
    private Fragment initConversationlist(){
        if(conversationListFragment == null){
            ConversationListFragment fragment = ConversationListFragment.getInstance();
            Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                    .appendPath("conversationlist")
                    .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") //设置私聊会话非聚合显示
                    .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "true")//设置群组会话聚合显示
                    .appendQueryParameter(Conversation.ConversationType.DISCUSSION.getName(), "false")//设置讨论组会话非聚合显示
                    .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "false")//设置系统会话非聚合显示
                    .build();
            // rong://com.cl.android.tse03/conversationlist?private=false&group=true&discussion=false&system=false
            Log.i("slack", "uri" + uri.toString());
            fragment.setUri(uri);
            return fragment;
        }else{
            return conversationListFragment;
        }
    }
}
