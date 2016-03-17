package cn.ucai.fulicenter.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.SharedElementCallback;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.EMConnectionListener;
import com.easemob.EMError;
import com.easemob.EMEventListener;
import com.easemob.EMGroupChangeListener;
import com.easemob.EMNotifierEvent;
import com.easemob.EMValueCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactListener;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.util.EMLog;
import com.easemob.util.HanziToPinyin;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.ucai.fulicenter.Constant;
import cn.ucai.fulicenter.DemoHXSDKHelper;
import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.applib.controller.HXSDKHelper;
import cn.ucai.fulicenter.bean.ContactBean;
import cn.ucai.fulicenter.bean.UserBean;
import cn.ucai.fulicenter.db.EMUserDao;
import cn.ucai.fulicenter.db.InviteMessgeDao;
import cn.ucai.fulicenter.domain.InviteMessage;
import cn.ucai.fulicenter.domain.User;
import cn.ucai.fulicenter.fragment.ChatAllHistoryFragment;
import cn.ucai.fulicenter.fragment.ContactlistFragment;
import cn.ucai.fulicenter.task.DeleteContactsTask;
import cn.ucai.fulicenter.utils.CommonUtils;
import cn.ucai.fulicenter.utils.NetUtil;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by ucai001 on 2016/3/16.
 */
public class WeChatActivity extends BaseActivity  implements EMEventListener {
    protected static final String TAG = WeChatActivity.class.getName();

    Context mContext;

    ImageView mivReturn;
    TextView mtvTitle;
    RelativeLayout mLayoutConversation;
    RelativeLayout mLayoutContactList;
    Drawable drawableConversation,drawableContactList;
    ImageView mivConversation;
    ImageView mivContactList;
    private int [] mMenuDrawableNormal = {R.drawable.iconfont_xinxi,R.drawable.iconfont_pengyou};
    private int [] mMenuDrawableSelected = {R.drawable.iconfont_xinxifill,R.drawable.iconfont_pengyoufill};
    private ImageView[] mImageViews = new ImageView[2];
    private Drawable[] mDrawable = new Drawable[2];

    // 未读消息textview
    private TextView unreadLabel;
    // 未读通讯录textview
    private TextView unreadAddressLable;

    ChatAllHistoryFragment mChatHistoryFragment;
    ContactlistFragment mContactListFragment;

    Fragment[] mFragments;
    UserBean mUser;

    private InviteMessgeDao inviteMessgeDao;
    private EMUserDao userDao;


    private MyContactListener contactListener = null;
    private MyConnectionListener connectionListener = null;

    // 未读消息textview
//	private TextView unreadLabel;
    // 未读通讯录textview
//	private TextView unreadAddressLable;
    float mDensity;

    private int index;
    // 当前fragment的index
    private int currentTabIndex = -1;

    // 账号在别处登录
    public boolean isConflict = false;
    // 账号被移除
    private boolean isCurrentAccountRemoved = false;
    /**
     * 检查当前用户是否被删除
     */
    public boolean getCurrentAccountRemoved() {
        return isCurrentAccountRemoved;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_wechat);

        initView();
        initFragment();
        setListener();


        // MobclickAgent.setDebugMode( true );
        // --?--
        MobclickAgent.updateOnlineConfig(this);

        if (getIntent().getBooleanExtra("conflict", false)
                && !isConflictDialogShow) {
            showConflictDialog();
        } else if (getIntent().getBooleanExtra(Constant.ACCOUNT_REMOVED, false)
                && !isAccountRemovedDialogShow) {
            showAccountRemovedDialog();
        }

        initDB();

        setListener();
        //异步获取当前用户的昵称和头像
        ((DemoHXSDKHelper) HXSDKHelper.getInstance()).getUserProfileManager().asyncGetCurrentUserInfo();
    }

    private void initFragment() {
        mChatHistoryFragment = new ChatAllHistoryFragment();
        mContactListFragment = new ContactlistFragment();


        mFragments = new Fragment[] {mChatHistoryFragment, mContactListFragment};
        // 添加显示第一个fragment
        getSupportFragmentManager()
                .beginTransaction().add(R.id.fragment_container_wechat, mChatHistoryFragment)
                .add(R.id.fragment_container_wechat, mContactListFragment)
                .hide(mContactListFragment).show(mChatHistoryFragment)
                .commit();
        if(currentTabIndex==-1){
            setFragment(currentTabIndex);
        }
    }

    private void initDB() {
        inviteMessgeDao = new InviteMessgeDao(this);
        userDao = new EMUserDao(this);
    }

    private void setListener() {
        if (mUser != null) {
            // setContactListener监听联系人的变化等
            contactListener = new MyContactListener();
            EMContactManager.getInstance().setContactListener(contactListener);

            // 注册一个监听连接状态的listener
            connectionListener = new MyConnectionListener();
            EMChatManager.getInstance().addConnectionListener(connectionListener);
        }

        mivReturn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
            }
        });
        setMenuItemClickListener();
    }

    private void setFragment(int newIndex){
        if(newIndex==-1){
            newIndex=0;
        }
//        setMenuItemDefaultDrawable();
//        setMenuItemDrawable();
        if(newIndex==0){
            drawableConversation = getmDrawable(R.drawable.iconfont_xinxifill);
            mivConversation.setImageDrawable(drawableConversation);
            drawableContactList = getmDrawable(R.drawable.iconfont_pengyou);
            mivContactList.setImageDrawable(drawableContactList);
        }else{
            drawableContactList = getmDrawable(R.drawable.iconfont_pengyoufill);
            mivContactList.setImageDrawable(drawableContactList);
            drawableConversation = getmDrawable(R.drawable.iconfont_xinxi);
            mivConversation.setImageDrawable(drawableConversation);
        }
        currentTabIndex = (newIndex+1)%2;
        index = newIndex;
        FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
        trx.hide(mFragments[currentTabIndex]).show(mFragments[index]).commit();
        currentTabIndex = index;
//        setMenuItemDrawable();
    }

    private void setMenuItemClickListener() {
        MenuItemClickListener listener=new MenuItemClickListener();
        mLayoutConversation.setOnClickListener(listener);
        mLayoutContactList.setOnClickListener(listener);
    }

    //    @SuppressLint("Override")
    public Drawable getmDrawable(int id){
        Resources res = getResources();
        Drawable drawable = res.getDrawable(id);
        return drawable;
    }
    /** 设置菜单项按钮顶部缺省显示的图片 */
    private void setMenuItemDefaultDrawable() {
        drawableConversation = getmDrawable(R.drawable.iconfont_xinxi);
        drawableContactList = getmDrawable(R.drawable.iconfont_pengyou);
    }

    /** 设置菜单项按钮顶部某菜单项被选择后显示的图片 */
    private void setMenuItemDrawable() {
        int width = (int) (mDensity * 32);
        int height = width;

        Rect bounds = new Rect(0, 0, width, height);
        drawableConversation.setBounds(bounds);
        mivConversation.setImageDrawable(drawableConversation);
        drawableContactList.setBounds(bounds);
        mivContactList.setImageDrawable(drawableContactList);
    }

    private void initView() {
        mivReturn = getViewById(R.id.ivReturn);
        mtvTitle = getViewById(R.id.tv_wechat_title);

        mLayoutConversation=getViewById(R.id.layout_wechat_conversation);
        mLayoutContactList=getViewById(R.id.layout_wechat_contact_list);
        mivConversation = getViewById(R.id.iv_wechat_conversation);
        mivContactList = getViewById(R.id.iv_wechat_contact_list);

        mImageViews[0]=mivConversation;
        mImageViews[1]=mivContactList;
        setMenuItemDefaultDrawable();
        mDrawable[0]=drawableConversation;
        mDrawable[1]=drawableContactList;
    }

    @Override
    public void setEnterSharedElementCallback(SharedElementCallback callback) {
        super.setEnterSharedElementCallback(callback);
    }

    @Override
    public void onEvent(EMNotifierEvent event) {
        switch (event.getEvent()) {
            case EventNewMessage: // 普通消息
            {
                EMMessage message = (EMMessage) event.getData();

                // 提示新消息
                HXSDKHelper.getInstance().getNotifier().onNewMsg(message);

                refreshUI();
                break;
            }

            case EventOfflineMessage: {
                refreshUI();
                break;
            }

            case EventConversationListChanged: {
                refreshUI();
                break;
            }

            default:
                break;
        }
    }



    @Override
    public void back(View view) {
        super.back(view);
    }

    @Override
    protected void onStop() {
        EMChatManager.getInstance().unregisterEventListener(this);
        DemoHXSDKHelper sdkHelper = (DemoHXSDKHelper) DemoHXSDKHelper.getInstance();
        sdkHelper.popActivity(this);

        super.onStop();
    }


    @Override
    public void onCreateContextMenu(android.view.ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        //getMenuInflater().inflate(R.menu.context_tab_contact, menu);
    }


    /**
     * 底部菜单项点击事件监听器
     * @author yao
     *
     */
    class MenuItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
//            setMenuItemDefaultDrawable();
            switch (v.getId()) {
                case R.id.layout_wechat_conversation:
                    index = 0;
                    break;
                case R.id.layout_wechat_contact_list:
                    index = 1;
                    break;
            }
            if (currentTabIndex != index) {
//                FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
//                trx.hide(mFragments[currentTabIndex]);
//                if (!mFragments[index].isAdded()) {
//                    trx.add(R.id.fragment_container_wechat, mFragments[index]);
//                }
//                trx.show(mFragments[index]).commit();
                setFragment(index);
            }
//            currentTabIndex = index;
//            setMenuItemDrawable();
        }
    }

    /**
     * 在应用服务器创添加登陆用户的好友,并将创建的好友的ContactBean和UserBean对象下载
     * 添加至内存集合
     * @author yao
     *
     */
    class AddContactsTask extends AsyncTask<Void, Void, Boolean> {
        Context context;
        ArrayList<String> userNameList;

        public AddContactsTask(Context context, ArrayList<String> userNameList) {
            super();
            this.context = context;
            this.userNameList = userNameList;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //获取登陆用户
            String userName = FuLiCenterApplication.getInstance().getUserName();
            //获取登陆用户的所有好友
            ArrayList<UserBean> contactList = FuLiCenterApplication.getInstance().getContactList();
            //获取好友集合
            HashMap<Integer, ContactBean> contacts = FuLiCenterApplication.getInstance().getContacts();
            boolean addSuccess = false;
            for(String contactName:userNameList){
                //向服务器添加新好友
                ContactBean contact = NetUtil.addContact(userName, contactName);
                if(contact!=null && "ok".equals(contact.getResult())){
                    //若添加成功，则将新好友保存在内存集合
                    contacts.put(contact.getMyuid(), contact);
                    //从服务端下载新好友
                    UserBean user = NetUtil.findUserByUserName(contactName);
                    if(user!=null){
                        contactList.add(user);
                        addSuccess = true;
                    }
                }
            }
            return addSuccess;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result){//若有新好友，向ContactListFragment发送刷新牧场好友的广播
                Intent intent = new Intent("update_contacts");
                context.sendStickyBroadcast(intent);
                Utils.showToast(context, R.string.Add_buddy_success, Toast.LENGTH_SHORT);
            }
        }

    }


    static void asyncFetchGroupsFromServer(){
        HXSDKHelper.getInstance().asyncFetchGroupsFromServer(new EMCallBack(){

            @Override
            public void onSuccess() {
                HXSDKHelper.getInstance().noitifyGroupSyncListeners(true);

                if(HXSDKHelper.getInstance().isContactsSyncedWithServer()){
                    HXSDKHelper.getInstance().notifyForRecevingEvents();
                }
            }

            @Override
            public void onError(int code, String message) {
                HXSDKHelper.getInstance().noitifyGroupSyncListeners(false);
            }

            @Override
            public void onProgress(int progress, String status) {

            }

        });
    }

    static void asyncFetchContactsFromServer(){
        HXSDKHelper.getInstance().asyncFetchContactsFromServer(new EMValueCallBack<List<String>>(){

            @Override
            public void onSuccess(List<String> usernames) {
                Context context = HXSDKHelper.getInstance().getAppContext();

                System.out.println("----------------"+usernames.toString());
                EMLog.d("roster", "contacts size: " + usernames.size());
                Map<String, User> userlist = new HashMap<String, User>();
                for (String username : usernames) {
                    User user = new User();
                    user.setUsername(username);
                    setUserHearder(username, user);
                    userlist.put(username, user);
                }
                // 添加user"申请与通知"
                User newFriends = new User();
                newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
                String strChat = context.getString(R.string.Application_and_notify);
                newFriends.setNick(strChat);

                userlist.put(Constant.NEW_FRIENDS_USERNAME, newFriends);
                // 添加"群聊"
                User groupUser = new User();
                String strGroup = context.getString(R.string.group_chat);
                groupUser.setUsername(Constant.GROUP_USERNAME);
                groupUser.setNick(strGroup);
                groupUser.setHeader("");
                userlist.put(Constant.GROUP_USERNAME, groupUser);

                // 添加"聊天室"
                User chatRoomItem = new User();
                String strChatRoom = context.getString(R.string.chat_room);
                chatRoomItem.setUsername(Constant.CHAT_ROOM);
                chatRoomItem.setNick(strChatRoom);
                chatRoomItem.setHeader("");
                userlist.put(Constant.CHAT_ROOM, chatRoomItem);

                // 添加"Robot"
                User robotUser = new User();
                String strRobot = context.getString(R.string.robot_chat);
                robotUser.setUsername(Constant.CHAT_ROBOT);
                robotUser.setNick(strRobot);
                robotUser.setHeader("");
                userlist.put(Constant.CHAT_ROBOT, robotUser);

                // 存入内存
                ((DemoHXSDKHelper)HXSDKHelper.getInstance()).setContactList(userlist);
                // 存入db
                EMUserDao dao = new EMUserDao(context);
                List<User> users = new ArrayList<User>(userlist.values());
                dao.saveContactList(users);

                HXSDKHelper.getInstance().notifyContactsSyncListener(true);

                if(HXSDKHelper.getInstance().isGroupsSyncedWithServer()){
                    HXSDKHelper.getInstance().notifyForRecevingEvents();
                }

                ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getUserProfileManager().asyncFetchContactInfosFromServer(usernames,new EMValueCallBack<List<User>>() {

                    @Override
                    public void onSuccess(List<User> uList) {
                        ((DemoHXSDKHelper)HXSDKHelper.getInstance()).updateContactList(uList);
                        ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getUserProfileManager().notifyContactInfosSyncListener(true);
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                HXSDKHelper.getInstance().notifyContactsSyncListener(false);
            }

        });
    }

    static void asyncFetchBlackListFromServer(){
        HXSDKHelper.getInstance().asyncFetchBlackListFromServer(new EMValueCallBack<List<String>>(){

            @Override
            public void onSuccess(List<String> value) {
                EMContactManager.getInstance().saveBlackList(value);
                HXSDKHelper.getInstance().notifyBlackListSyncListener(true);
            }

            @Override
            public void onError(int error, String errorMsg) {
                HXSDKHelper.getInstance().notifyBlackListSyncListener(false);
            }

        });
    }

    /**
     * 设置hearder属性，方便通讯中对联系人按header分类显示，以及通过右侧ABCD...字母栏快速定位联系人
     *
     * @param username
     * @param user
     */
    private static void setUserHearder(String username, User user) {
        String headerName = null;
        if (!TextUtils.isEmpty(user.getNick())) {
            headerName = user.getNick();
        } else {
            headerName = user.getUsername();
        }
        if (username.equals(Constant.NEW_FRIENDS_USERNAME)) {
            user.setHeader("");
        } else if (Character.isDigit(headerName.charAt(0))) {
            user.setHeader("#");
        } else {
            user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1)).get(0).target.substring(0, 1)
                    .toUpperCase());
            char header = user.getHeader().toLowerCase().charAt(0);
            if (header < 'a' || header > 'z') {
                user.setHeader("#");
            }
        }
    }


    /**
     * 帐号被移除的dialog
     */
    private void showAccountRemovedDialog() {
        isAccountRemovedDialogShow = true;
        DemoHXSDKHelper.getInstance().logout(true,null);
        String st5 = getResources().getString(R.string.Remove_the_notification);
        if (!WeChatActivity.this.isFinishing()) {
            // clear up global variables
            try {
                if (accountRemovedBuilder == null)
                    accountRemovedBuilder = new android.app.AlertDialog.Builder(WeChatActivity.this);
                accountRemovedBuilder.setTitle(st5);
                accountRemovedBuilder.setMessage(R.string.em_user_remove);
                accountRemovedBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        accountRemovedBuilder = null;
                        finish();
                        startActivity(new Intent(WeChatActivity.this, LoginActivity.class));
                    }
                });
                accountRemovedBuilder.setCancelable(false);
                accountRemovedBuilder.create().show();
                isCurrentAccountRemoved = true;
            } catch (Exception e) {
                EMLog.e(TAG, "---------color userRemovedBuilder error" + e.getMessage());
            }

        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private android.app.AlertDialog.Builder conflictBuilder;
    private android.app.AlertDialog.Builder accountRemovedBuilder;
    private boolean isConflictDialogShow;
    private boolean isAccountRemovedDialogShow;

    /**
     * 显示帐号在别处登录dialog
     */
    private void showConflictDialog() {
        isConflictDialogShow = true;
        DemoHXSDKHelper.getInstance().logout(false,null);
        String st = getResources().getString(R.string.Logoff_notification);
        if (!WeChatActivity.this.isFinishing()) {
            // clear up global variables
            try {
                if (conflictBuilder == null)
                    conflictBuilder = new android.app.AlertDialog.Builder(WeChatActivity.this);
                conflictBuilder.setTitle(st);
                conflictBuilder.setMessage(R.string.connect_conflict);
                conflictBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        conflictBuilder = null;
                        finish();
                        startActivity(new Intent(WeChatActivity.this, LoginActivity.class));
                    }
                });
                conflictBuilder.setCancelable(false);
                conflictBuilder.create().show();
                isConflict = true;
            } catch (Exception e) {
                EMLog.e(TAG, "---------color conflictBuilder error" + e.getMessage());
            }

        }

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (getIntent().getBooleanExtra("conflict", false) && !isConflictDialogShow) {
            showConflictDialog();
        } else if (getIntent().getBooleanExtra(Constant.ACCOUNT_REMOVED, false) && !isAccountRemovedDialogShow) {
            showAccountRemovedDialog();
        }
    }

    /**
     * 刷新未读消息数
     */
    public void updateUnreadLabel() {
        int count = getUnreadMsgCountTotal();
//		if (count > 0) {
//			unreadLabel.setText(String.valueOf(count));
//			unreadLabel.setVisibility(View.VISIBLE);
//		} else {
//			unreadLabel.setVisibility(View.INVISIBLE);
//		}
    }

    /**
     * 刷新申请与通知消息数
     */
    public void updateUnreadAddressLable() {
        runOnUiThread(new Runnable() {
            public void run() {
                int count = getUnreadAddressCountTotal();
                Log.e("main","WeChatActivity.updateUnreadAddressLable="+count);
//				if (count > 0) {
////					unreadAddressLable.setText(String.valueOf(count));
//					unreadAddressLable.setVisibility(View.VISIBLE);
//				} else {
//					unreadAddressLable.setVisibility(View.INVISIBLE);
//				}
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isConflict && !isCurrentAccountRemoved && mUser!=null) {
            updateUnreadLabel();
            updateUnreadAddressLable();
            EMChatManager.getInstance().activityResumed();
        }

        // unregister this event listener when this activity enters the
        // background
        DemoHXSDKHelper sdkHelper = (DemoHXSDKHelper) DemoHXSDKHelper.getInstance();
        sdkHelper.pushActivity(this);

        // register the event listener when enter the foreground
        EMChatManager.getInstance().registerEventListener(this,
                new EMNotifierEvent.Event[] { EMNotifierEvent.Event.EventNewMessage ,EMNotifierEvent.Event.EventOfflineMessage, EMNotifierEvent.Event.EventConversationListChanged});

    }

    /**
     * 获取未读申请与通知消息
     *
     * @return
     */
    public int getUnreadAddressCountTotal() {
        int unreadAddressCountTotal = 0;
        Log.e("main","WeChatActivity.1unreadAddressCountTotal="+unreadAddressCountTotal);
        if (((DemoHXSDKHelper)HXSDKHelper.getInstance()).getContactList().get(Constant.NEW_FRIENDS_USERNAME) != null)
            unreadAddressCountTotal = ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getContactList().get(Constant.NEW_FRIENDS_USERNAME)
                    .getUnreadMsgCount();
        Log.e("main","WeChatActivity.2unreadAddressCountTotal="+unreadAddressCountTotal);
        return unreadAddressCountTotal;
    }

    /**
     * 获取未读消息数
     *
     * @return
     */
    public int getUnreadMsgCountTotal() {
        int unreadMsgCountTotal = 0;
        int chatroomUnreadMsgCount = 0;
        unreadMsgCountTotal = EMChatManager.getInstance().getUnreadMsgsCount();
        for(EMConversation conversation:EMChatManager.getInstance().getAllConversations().values()){
            if(conversation.getType() == EMConversation.EMConversationType.ChatRoom)
                chatroomUnreadMsgCount=chatroomUnreadMsgCount+conversation.getUnreadMsgCount();
        }
        return unreadMsgCountTotal-chatroomUnreadMsgCount;
    }


    private void refreshUI() {
        runOnUiThread(new Runnable() {
            public void run() {
                // 刷新bottom bar消息未读数
                updateUnreadLabel();
                if (currentTabIndex == 0) {
                    // 当前页面如果为聊天历史页面，刷新此页面
//					if (mChatHistoryFragment != null) {
//						mChatHistoryFragment.refresh();
//					}
                }
            }
        });
    }
    
    /**
     * MyGroupChangeListener
     */
    public class MyGroupChangeListener implements EMGroupChangeListener {
        /**
         * 当前用户收到加群邀请
         */
        @Override
        public void onInvitationReceived(String groupId, String groupName, String inviter, String reason) {

            boolean hasGroup = false;
            for (EMGroup group : EMGroupManager.getInstance().getAllGroups()) {
                if (group.getGroupId().equals(groupId)) {
                    hasGroup = true;
                    break;
                }
            }
            if (!hasGroup)
                return;

            // 被邀请
            String st3 = getResources().getString(R.string.Invite_you_to_join_a_group_chat);
            EMMessage msg = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
            msg.setChatType(EMMessage.ChatType.GroupChat);
            msg.setFrom(inviter);
            msg.setTo(groupId);
            msg.setMsgId(UUID.randomUUID().toString());
            msg.addBody(new TextMessageBody(inviter + " " +st3));
            // 保存邀请消息
            EMChatManager.getInstance().saveMessage(msg);
            // 提醒新消息
            HXSDKHelper.getInstance().getNotifier().viberateAndPlayTone(msg);

            runOnUiThread(new Runnable() {
                public void run() {
                    updateUnreadLabel();
                    // 刷新ui
                    if (currentTabIndex == 0)
//						mChatHistoryFragment.refresh();
                        if (CommonUtils.getTopActivity(WeChatActivity.this).equals(GroupsActivity.class.getName())) {
                            GroupsActivity.instance.onResume();
                        }
                }
            });

        }

        /**
         * 群邀请被用户接受
         */
        @Override
        public void onInvitationAccpted(String groupId, String inviter, String reason) {

        }

        /**
         * 群组邀请被用户拒绝
         */
        @Override
        public void onInvitationDeclined(String groupId, String invitee, String reason) {

        }

        /**提示用户被T了，demo省略此步骤*/
        @Override
        public void onUserRemoved(String groupId, String groupName) {

            // 提示用户被T了，demo省略此步骤
            // 刷新ui
            runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        updateUnreadLabel();
                        if (currentTabIndex == 0)
//							mChatHistoryFragment.refresh();
                            if (CommonUtils.getTopActivity(WeChatActivity.this).equals(GroupsActivity.class.getName())) {
                                GroupsActivity.instance.onResume();
                            }
                    } catch (Exception e) {
                        EMLog.e(TAG, "refresh exception " + e.getMessage());
                    }
                }
            });
        }

        // 群被解散
        @Override
        public void onGroupDestroy(String groupId, String groupName) {

            // 群被解散
            // 提示用户群被解散,demo省略
            // 刷新ui
            runOnUiThread(new Runnable() {
                public void run() {
                    updateUnreadLabel();
                    if (currentTabIndex == 0)
//						mChatHistoryFragment.refresh();
                        if (CommonUtils.getTopActivity(WeChatActivity.this).equals(GroupsActivity.class.getName())) {
                            GroupsActivity.instance.onResume();
                        }
                }
            });

        }

        /**
         * 收到用户加群要求
         */
        @Override
        public void onApplicationReceived(String groupId, String groupName, String applyer, String reason) {

            // 用户申请加入群聊
            InviteMessage msg = new InviteMessage();
            msg.setFrom(applyer);
            msg.setTime(System.currentTimeMillis());
            msg.setGroupId(groupId);
            msg.setGroupName(groupName);
            msg.setReason(reason);
            Log.d(TAG, applyer + " 申请加入群聊：" + groupName);
            msg.setStatus(InviteMessage.InviteMesageStatus.BEAPPLYED);
            notifyNewIviteMessage(msg);
        }

        /**
         * 加群申请被接受
         */
        @Override
        public void onApplicationAccept(String groupId, String groupName, String accepter) {
            //以下两行代码，将登陆用户添加至groupName代表的群(服务端)
            String userName= FuLiCenterApplication.getInstance().getUserName();
//		    GroupBean group=NetUtil.addGroupMember(groupName, userName);
//		    if(group!=null){
//                //将新增的群添加至当前用户所属的群组集合中
//		        ArrayList<GroupBean> groupList = FuLiCenterApplication.getInstance().getGroupList();
//		        groupList.add(group);
//                //将新增的群成员的UserBean类型的对象添加至groupMembers集合中
//		        HashMap<String, ArrayList<UserBean>> groupMembers = FuLiCenterApplication.getInstance().getGroupMembers();
//		        ArrayList<UserBean> groupUsers = groupMembers.get(group.getGroupId());
//		        UserBean user = NetUtil.findUserByUserName(userName);
//		        groupUsers.add(user);
//		    }

            String st4 = getResources().getString(R.string.Agreed_to_your_group_chat_application);
            // 加群申请被同意
            EMMessage msg = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
            msg.setChatType(EMMessage.ChatType.GroupChat);
            msg.setFrom(accepter);
            msg.setTo(groupId);
            msg.setMsgId(UUID.randomUUID().toString());
            msg.addBody(new TextMessageBody(accepter + " " +st4));
            // 保存同意消息
            EMChatManager.getInstance().saveMessage(msg);
            // 提醒新消息
            HXSDKHelper.getInstance().getNotifier().viberateAndPlayTone(msg);

            runOnUiThread(new Runnable() {
                public void run() {
                    updateUnreadLabel();
                    // 刷新ui
//					if (currentTabIndex == 0)
//						mChatHistoryFragment.refresh();
                    //注释掉以下两行代码
//					if (CommonUtils.getTopActivity(WeChatActivity.this).equals(GroupsActivity.class.getName())) {
//						GroupsActivity.instance.onResume();
//					}
                }
            });
        }

        /**
         * 加群申请被拒绝
         */
        @Override
        public void onApplicationDeclined(String groupId, String groupName, String decliner, String reason) {
            // 加群申请被拒绝，demo未实现
        }
    }


    /**
     * 保存提示新消息
     *
     * @param msg
     */
    private void notifyNewIviteMessage(InviteMessage msg) {
        saveInviteMsg(msg);
        // 提示有新消息
        HXSDKHelper.getInstance().getNotifier().viberateAndPlayTone(null);

        // 刷新bottom bar消息未读数
        updateUnreadAddressLable();
        // 刷新好友页面ui
//		if (currentTabIndex == 1)
//			mContactListFragment.refresh();
    }

    /**
     * 保存邀请等msg
     *
     * @param msg
     */
    private void saveInviteMsg(InviteMessage msg) {
        // 保存msg
        inviteMessgeDao.saveMessage(msg);
        // 未读数加1
        User user = ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getContactList().get(Constant.NEW_FRIENDS_USERNAME);
        if (user.getUnreadMsgCount() == 0)
            user.setUnreadMsgCount(user.getUnreadMsgCount() + 1);
    }

    /**
     * set head
     *
     * @param username
     * @return
     */
    User setUserHead(String username) {
        User user = new User();
        user.setUsername(username);
        String headerName = null;
        if (!TextUtils.isEmpty(user.getNick())) {
            headerName = user.getNick();
        } else {
            headerName = user.getUsername();
        }
        if (username.equals(Constant.NEW_FRIENDS_USERNAME)) {
            user.setHeader("");
        } else if (Character.isDigit(headerName.charAt(0))) {
            user.setHeader("#");
        } else {
            user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1)).get(0).target.substring(0, 1)
                    .toUpperCase());
            char header = user.getHeader().toLowerCase().charAt(0);
            if (header < 'a' || header > 'z') {
                user.setHeader("#");
            }
        }
        return user;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (conflictBuilder != null) {
            conflictBuilder.create().dismiss();
            conflictBuilder = null;
        }

        if (connectionListener != null) {
            EMChatManager.getInstance().removeConnectionListener(connectionListener);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isConflict", isConflict);
        outState.putBoolean(Constant.ACCOUNT_REMOVED, isCurrentAccountRemoved);
        super.onSaveInstanceState(outState);
    }

    /***
     * 好友变化listener
     *
     */
    public class MyContactListener implements EMContactListener {

        @Override
        public void onContactAdded(List<String> usernameList) {
            Log.e("main","MyContactListener.onContactAdded-------------begin--------------");
            // 保存增加的联系人
            Map<String, User> localUsers = ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getContactList();
            Map<String, User> toAddUsers = new HashMap<String, User>();
            Log.e("main","MyContactListener.onContactAdded---usernameList="+usernameList+",localUsers="+localUsers+",toAddUsers="+toAddUsers);
            boolean exists = false;
            for (String username : usernameList) {
                User user = setUserHead(username);
                // 添加好友时可能会回调added方法两次
                if (!localUsers.containsKey(username)) {
                    userDao.saveContact(user);
                    exists = true;
                }
                toAddUsers.put(username, user);
            }
            localUsers.putAll(toAddUsers);
            // 刷新ui
//			if (currentTabIndex == 1)
//				mContactListFragment.refresh();

            FuLiCenterApplication instance = FuLiCenterApplication.getInstance();
            ArrayList<UserBean> contactList = instance.getContactList();
            ArrayList<String> addContactList = new ArrayList<String>();
            for(int i=0;i<usernameList.size();i++){
                UserBean u = new UserBean(usernameList.get(i));
                if(!contactList.contains(u)){
                    addContactList.add(usernameList.get(i));
                }
            }
            Log.e("main","MyContactListener.onContactAdded.AddContactsTask.addContactList="+addContactList.size()+",exists="+exists);
            if(exists && addContactList.size()>0){
                Log.e("main","MyContactListener.onContactAdded.AddContactsTask.execute");
                new AddContactsTask(mContext,addContactList).execute();
            }
            Log.e("main","MyContactListener.onContactAdded-------------end--------------");

        }

        @Override
        public void onContactDeleted(final List<String> usernameList) {
            Log.e("main","MyContactListener.onContactDeleted-------------begin--------------");
            //删除应用服务器的好友关系
            UserBean user = FuLiCenterApplication.getInstance().getUserBean();
            HashMap<Integer, ContactBean> contacts = FuLiCenterApplication.getInstance().getContacts();
            ArrayList<UserBean> contactList = FuLiCenterApplication.getInstance().getContactList();
            ArrayList<ContactBean> deleteContacts = new ArrayList<ContactBean>();
            ArrayList<UserBean> deleteContactList = new ArrayList<UserBean>();
            //删除内存中好友，删除的好友存放在deleteContactList和deleteContacts集合中
            for(int i=0;i<contactList.size();i++){
                UserBean contactUser = contactList.get(i);
                if(usernameList.contains(contactUser.getUserName())){
                    ContactBean contact = contacts.remove(contactUser.getId());
                    deleteContacts.add(contact);
                    deleteContactList.add(contactUser);
                }
            }
            Log.e(TAG,"onContactDeleted.(deleteContacts.size="+deleteContacts.size());
            if(deleteContacts.size()>0){
                contactList.removeAll(deleteContactList);//删除内存中好友
                // 删除应用服务器的联系人记录
                new DeleteContactsTask(mContext,deleteContacts).execute();
            }
            // 被删除
            // 删除环信的好友关系
            Map<String, User> localUsers = ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getContactList();
            for (String username : usernameList) {
                localUsers.remove(username);
                userDao.deleteContact(username);
                inviteMessgeDao.deleteMessage(username);
            }
            runOnUiThread(new Runnable() {
                public void run() {
                    // 如果正在与此用户的聊天页面
                    String st10 = getResources().getString(R.string.have_you_removed);
                    if (ChatActivity.activityInstance != null
                            && usernameList.contains(ChatActivity.activityInstance.getToChatUsername())) {
                        Toast.makeText(WeChatActivity.this, ChatActivity.activityInstance.getToChatUsername() + st10, Toast.LENGTH_SHORT)
                                .show();
                        ChatActivity.activityInstance.finish();
                    }
                    updateUnreadLabel();
                    // 刷新ui
//					mContactListFragment.refresh();
//					mChatHistoryFragment.refresh();
                }
            });

            Log.e("main","MyContactListener.onContactDeleted-------------end--------------");
        }

        @Override
        public void onContactInvited(String username, String reason) {

            // 接到邀请的消息，如果不处理(同意或拒绝)，掉线后，服务器会自动再发过来，所以客户端不需要重复提醒
            List<InviteMessage> msgs = inviteMessgeDao.getMessagesList();

            for (InviteMessage inviteMessage : msgs) {
                if (inviteMessage.getGroupId() == null && inviteMessage.getFrom().equals(username)) {
                    inviteMessgeDao.deleteMessage(username);
                }
            }
            // 自己封装的javabean
            InviteMessage msg = new InviteMessage();
            msg.setFrom(username);
            msg.setTime(System.currentTimeMillis());
            msg.setReason(reason);
            Log.d(TAG, username + "请求加你为好友,reason: " + reason);
            // 设置相应status
            msg.setStatus(InviteMessage.InviteMesageStatus.BEINVITEED);
            notifyNewIviteMessage(msg);

        }

        @Override
        public void onContactAgreed(String username) {
            List<InviteMessage> msgs = inviteMessgeDao.getMessagesList();
            for (InviteMessage inviteMessage : msgs) {
                if (inviteMessage.getFrom().equals(username)) {
                    return;
                }
            }
            // 自己封装的javabean
            InviteMessage msg = new InviteMessage();
            msg.setFrom(username);
            msg.setTime(System.currentTimeMillis());
            Log.d(TAG, username + "同意了你的好友请求");
            msg.setStatus(InviteMessage.InviteMesageStatus.BEAGREED);
            notifyNewIviteMessage(msg);

        }

        @Override
        public void onContactRefused(String username) {

            // 参考同意，被邀请实现此功能,demo未实现
            Log.d(username, username + "拒绝了你的好友请求");
        }

    }


    /**
     * 连接监听listener
     *
     */
    public class MyConnectionListener implements EMConnectionListener {

        @Override
        public void onConnected() {
            boolean groupSynced = HXSDKHelper.getInstance().isGroupsSyncedWithServer();
            boolean contactSynced = HXSDKHelper.getInstance().isContactsSyncedWithServer();

            // in case group and contact were already synced, we supposed to notify sdk we are ready to receive the events
            if (groupSynced && contactSynced) {
                new Thread() {
                    @Override
                    public void run() {
                        HXSDKHelper.getInstance().notifyForRecevingEvents();
                    }
                }.start();
            } else {
                if (!groupSynced) {
                    asyncFetchGroupsFromServer();
                }

                if (!contactSynced) {
                    asyncFetchContactsFromServer();
                }

                if (!HXSDKHelper.getInstance().isBlackListSyncedWithServer()) {
                    asyncFetchBlackListFromServer();
                }
            }

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
//                    mChatHistoryFragment.errorItem.setVisibility(View.GONE);
                }

            });
        }

        @Override
        public void onDisconnected(final int error) {
            final String st1 = getResources().getString(R.string.can_not_connect_chat_server_connection);
            final String st2 = getResources().getString(R.string.the_current_network);
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (error == EMError.USER_REMOVED) {
                        // 显示帐号已经被移除
                        showAccountRemovedDialog();
                    } else if (error == EMError.CONNECTION_CONFLICT) {
                        // 显示帐号在其他设备登陆dialog
                        showConflictDialog();
                    } else {
//						mChatHistoryFragment.errorItem.setVisibility(View.VISIBLE);
//						if (NetUtils.hasNetwork(WeChatActivity.this))
//							mChatHistoryFragment.errorText.setText(st1);
//						else
//							mChatHistoryFragment.errorText.setText(st2);

                    }
                }

            });
        }
    }
}
