package com.cl.android.chartonlyforyou.rongchart;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.widget.TextView;

import com.cl.android.chartonlyforyou.R;

import java.util.Locale;

import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imlib.model.Conversation;

/**会话界面，加上title
 * Created by chenling on 2016/3/19.
 */
public class ConversationChatActivity extends FragmentActivity {

    /**
     * 目标 Id
     */
    private String mTargetId;

    /**
     * 刚刚创建完讨论组后获得讨论组的id 为targetIds，需要根据 为targetIds 获取 targetId
     */
    private String mTargetIds;

    /**
     * 会话类型
     */
    private Conversation.ConversationType mConversationType;

    private TextView chartTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation);
        //设置聊天背景图片
        getWindow().setBackgroundDrawableResource(R.drawable.background);
        //获取用户的信息，聊天时加上title，如果这个title是显示用户手机号，可以设置点击拨打电话嘛
        chartTitle = (TextView)findViewById(R.id.charttitle);
        Intent intent = getIntent();
        getIntentDate(intent);
    }

    /**
     * 展示如何从 Intent 中得到 融云会话页面传递的 Uri
     */
    private void getIntentDate(Intent intent) {
        mTargetId = intent.getData().getQueryParameter("targetId");
        String title = intent.getData().getQueryParameter("title");//需要用户信息提供
        if(!TextUtils.isEmpty(title)){
            chartTitle.setText(title);
        }

//        mTargetIds = intent.getData().getQueryParameter("targetIds");
        intent.getData().getLastPathSegment();//获得当前会话类型
        mConversationType = Conversation.ConversationType.valueOf(intent.getData().getLastPathSegment().toUpperCase(Locale.getDefault()));

        enterFragment(mConversationType, mTargetId);
    }

    /**
     * 加载会话页面 ConversationFragment
     *以会话页面的启动 Uri 为例说明：
     *  rong://{packagename:应用包名}/conversation/[private|discussion|group]?targetId={目标Id}&[title={开启会话名称}]
     *  上面的例子，如果你的包名为 io.rong.imkit.demo，目标 Id 为 12345 的私聊会话，
     *  拼接后的 Uri 就是 rong://io.rong.imkit.demo/conversation/private?targetId=12345，
     * @param mConversationType 会话类型
     * @param mTargetId 目标 Id
     */
    private void enterFragment(Conversation.ConversationType mConversationType, String mTargetId) {

        ConversationFragment fragment = (ConversationFragment) getSupportFragmentManager().findFragmentById(R.id.conversation);

        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                .appendPath("conversation").appendPath(mConversationType.getName().toLowerCase())
                .appendQueryParameter("targetId", mTargetId).build();

        fragment.setUri(uri);
    }

}
