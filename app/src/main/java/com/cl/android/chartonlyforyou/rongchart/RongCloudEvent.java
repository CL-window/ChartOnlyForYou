package com.cl.android.chartonlyforyou.rongchart;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cl.android.chartonlyforyou.MyApplication;
import com.cl.android.chartonlyforyou.baidulocation.LocationProvider;
import com.cl.android.chartonlyforyou.rongchart.picture.PhotoInputProvider;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.provider.CameraInputProvider;
import io.rong.imkit.widget.provider.ImageInputProvider;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imkit.widget.provider.LocationInputProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ImageMessage;
import io.rong.message.PublicServiceMultiRichContentMessage;
import io.rong.message.PublicServiceRichContentMessage;
import io.rong.message.RichContentMessage;
import io.rong.message.TextMessage;
import io.rong.notification.PushNotificationMessage;

/** 设置时间的监听
 * Created by chenling on 2016/3/25.
 */
public class RongCloudEvent implements RongIM.ConversationBehaviorListener,RongIMClient.OnReceiveMessageListener,
        RongIM.UserInfoProvider,RongIMClient.OnReceivePushMessageListener{
    private static final String TAG = RongCloudEvent.class.getSimpleName();
    private static RongCloudEvent mRongCloudInstance;
    private Context mContext;

    private SharedPreferences sharedPreferences;//本地的一些设置


    /**
     * 初始化 RongCloud.
     *
     * @param context 上下文。
     */
    public static  RongCloudEvent init(Context context) {

        if (mRongCloudInstance == null) {

            synchronized (RongCloudEvent.class) {

                if (mRongCloudInstance == null) {
                    mRongCloudInstance = new RongCloudEvent(context);
                }
            }
        }
        return mRongCloudInstance;
    }

    /**
     * 构造方法。
     *
     * @param context 上下文。
     */
    private RongCloudEvent(Context context) {
        mContext = context;

        initDefaultListener();
    }

    /**
     * RongIM.init(this) 后直接可注册的Listener。
     */
    private void initDefaultListener() {

        RongIM.setConversationBehaviorListener(this);//设置会话界面操作的监听器。
        RongIM.setUserInfoProvider(this, true);
        /**
         * 设置接收 push 消息的监听器。
         */
        RongIM.setOnReceivePushMessageListener(this);

    }
    /**
     * 需要 rongcloud connect 成功后设置的 listener
     */
    public void initListener(){
        //接收消息的监听
        RongIM.getInstance().getRongIMClient().setOnReceiveMessageListener(this);

        //扩展功能自定义
        InputProvider.ExtendProvider[] provider = {
                new PhotoInputProvider(RongContext.getInstance()),//自定义图片
//                new ImageInputProvider(RongContext.getInstance()),//图片
                new CameraInputProvider(RongContext.getInstance()),//相机
                new LocationProvider(RongContext.getInstance()),//自定义的地理位置

//                new VoIPInputProvider(RongContext.getInstance()),// 语音通话
//                new ContactsProvider(RongContext.getInstance())//自定义通讯录
        };
        RongIM.getInstance().resetInputExtensionProvider(Conversation.ConversationType.PRIVATE, provider);
    }

    /*点击头像的监听*/
    @Override
    public boolean onUserPortraitClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo) {
        Toast.makeText(context, userInfo.getName(), Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onUserPortraitLongClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo) {
        return false;
    }

    /**
     * 会话界面操作的监听器：ConversationBehaviorListener 的回调方法，当点击消息时执行。
     *
     * @param context 应用当前上下文。
     * @param message 被点击的消息的实体信息。
     * @return 返回True不执行后续SDK操作，返回False继续执行SDK操作。
     */
    @Override
    public boolean onMessageClick(Context context, View view, Message message) {
        Log.e(TAG, "----onMessageClick");

        if (message.getContent() instanceof RichContentMessage) {
            RichContentMessage mRichContentMessage = (RichContentMessage) message.getContent();
            Log.d(TAG, "extra:" + mRichContentMessage.getExtra());
            Log.e(TAG, "----RichContentMessage-------");

        } else if (message.getContent() instanceof ImageMessage) {
            //图片信息，想做到点击查看大图 ,使用融云默认的图片加载时使用
            //实现会话界面点击查看大图逻辑  依赖 PhotoActivity 和 其布局 以及 menu/de_fix_username.xml
            ImageMessage imageMessage = (ImageMessage) message.getContent();
            Intent intent = new Intent(context, PhotoActivity.class);
            intent.putExtra("photo", imageMessage.getLocalUri() == null ? imageMessage.getRemoteUri() : imageMessage.getLocalUri());
            if (imageMessage.getThumUri() != null)
                intent.putExtra("thumbnail", imageMessage.getThumUri());

            context.startActivity(intent);
            Log.i(TAG,"uri:"+ (imageMessage.getLocalUri() == null ? imageMessage.getRemoteUri() : imageMessage.getLocalUri()));



        } else if (message.getContent() instanceof PublicServiceMultiRichContentMessage) {
            Log.e(TAG, "----PublicServiceMultiRichContentMessage-------");

        } else if (message.getContent() instanceof PublicServiceRichContentMessage) {
            Log.e(TAG, "----PublicServiceRichContentMessage-------");

        }

        Log.d(TAG, message.getObjectName() + ":" + message.getMessageId());

        return false;
    }

    @Override
    public boolean onMessageLinkClick(Context context, String s) {
        return false;
    }

    @Override
    public boolean onMessageLongClick(Context context, View view, Message message) {
        return false;
    }
    /**
     * 接收消息的监听器：OnReceiveMessageListener 的回调方法，接收到消息后执行。
     *
     * @param message 接收到的消息的实体信息。
     * @param left    剩余未拉取消息数目。
     */
    @Override
    public boolean onReceived(Message message, int left) {
        sharedPreferences = MyApplication.getSharedPreferences();
        //这里设置黑科技，接收指令，远程控制
        String DND = sharedPreferences.getString("DND", "");//Do Not Disturb

        MessageContent messageContent = message.getContent();
        Log.i("slack",messageContent.toString()+".......get");
        if (messageContent instanceof TextMessage) {
            String info =  ((TextMessage) messageContent).getContent();
            Log.i("slack", "receive："+info);
            Log.i("slack", "receive："+info.indexOf("#location"));
            //开启紧急模式,获取本机定位 ，默认为勿扰模式
            if(info.indexOf("#locationToSMS") >= 0) {
                Log.i("slack", "locationToSMS");
                MyApplication.sendMessage();
                return true;
            }
            if(info.indexOf("#location") >= 0) {
                Log.i("slack", "location");
                return urgent(messageContent);
            }

        }

        /*这种后台消息
            ##COMMON_io.rong.notification.PushNotificationManager:onReceiveMessage: chenling002|slack
            PushNotificationManager: send. title:slack
        */
        if(TextUtils.isEmpty(DND)){
            //非勿扰模式,后台消息有提示
            return false;
        }else{
            return true;
        }
    }

    private Boolean urgent(MessageContent messageContent) {

        //这里最好能进入 勿扰模式
        LocationProvider locationProvider = new LocationProvider(RongContext.getInstance());
        //往那个用户那边发这个信息，这里是被动的发消息，发送的是发送这条指令的人那里
        locationProvider.getLocation("chenling002");

        return true;
    }

    //用户信息提供者
    @Override
    public UserInfo getUserInfo(String userId) {
        Log.i("slack",userId);
        if (userId == null)
            return null;
        //登录用户的user，用户信息提供者，可以得到用户的头像，
        //发现了，这地方在调用用户信息提供者时，会获取当前聊天的两个用户的信息，所以代码里要提供对话双方的信息
        if(userId.equals("chenling002")){
            //UserInfo(java.lang.String id,java.lang.String name, android.net.Uri portraitUri)
//            return new UserInfo("chenling02","slack", Uri.parse("https://dn-gtaoljip.qbox.me/LfE2Y9R9UWqxlBjcVJjHGlA"));
            //{"code":200,"userId":"chenling002","token":"FgHa3+S58iy9E/UcG/4NI1AaIj884ZIPjiEYlxpZXJaOkHNgP8OYA7sPvuX00tkx8rr3hrSSidzwLgqaAwicRPabOW3CYFve"}
            return new UserInfo("chenling002","slack", Uri.parse("http://img3.3lian.com/2013/v8/91/d/9.jpg"));
        }else {
//            return new UserInfo("chenling01","chenling01", Uri.parse("https://dn-gtaoljip.qbox.me/250ab385a4e33616.jpg"));
            //{"code":200,"userId":"chenling001","token":"pUWTJzVVjVeaIl6fGdY0/+7HOY8NyemRZGLd2y9cigDYvuEqaB2tovYOteEA8JH8UQwoKQusoO5bSRgV/pVJTbaVjr61WdEo"}
            return new UserInfo("chenling001","颦儿", Uri.parse("http://att2.citysbs.com/hangzhou/sns01/forum/2011/01/28-12/20110128_8b88010365e5ec50b56edd8mndwmyUQV.jpg"));
        }
            //这个地方的图片我可以随便填写（用户更改头像就不需要重新获取token了），但是第一个 不可以
//        return new UserInfo("chenling02","chenling02", Uri.parse("https://dn-gtaoljip.qbox.me/250ab385a4e33616.jpg"));


    }

    /**
     * 收到 push 消息的处理。
     *
     * @param pushNotificationMessage push 消息实体。
     * @return true 自己来弹通知栏提示，false 融云 SDK 来弹通知栏提示。
     */
    @Override
    public boolean onReceivePushMessage(PushNotificationMessage pushNotificationMessage) {
        pushNotificationMessage.getObjectName();//判断消息类型
        Log.i("slack", "onReceived-onPushMessageArrive:" + pushNotificationMessage.getPushContent());
        return false;
    }
}
