package cn.ucai.fulicenter.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.CollectActivity;
import cn.ucai.fulicenter.activity.SettingsActivity;
import cn.ucai.fulicenter.activity.WeChatActivity;
import cn.ucai.fulicenter.adapter.OrderAdapter;
import cn.ucai.fulicenter.bean.UserBean;
import cn.ucai.fulicenter.task.DownloadCollectCountTask;
import cn.ucai.fulicenter.utils.ImageLoader;

/**
 * Created by ucai001 on 2016/3/12.
 */
public class PersonalCenterFragment extends Fragment {
    public static final String TAG = PersonalCenterFragment.class.getName();
    Context mContext;

    GridView mOrderList;
    //资源文件
    private int[] pic_path={R.drawable.order_list1,R.drawable.order_list2,R.drawable.order_list3,R.drawable.order_list4,R.drawable.order_list5};
    OrderAdapter mAdapter;
    ImageView mivUserAvarar;
    TextView mtvUserName;
    TextView mtvCollectCount;
    TextView mtvSettings;
    ImageView mivMessage;
    LinearLayout mLayoutCenterCollet;
    RelativeLayout mLyaoutCenterUserInfo;

    ImageLoader mImageLoader;

    int mCollectCount = 0;
    UserBean mUser;
    DownloadCollectCountTask mDownloadCollectCountTask;
    CollectCountChangedReceiver mReceiver;
    UpdateCollectCountChangedReceiver mUpdateReceiver;
    MyClickListener listener;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        View layout = View.inflate(mContext, R.layout.fragment_personal_center,null);

        checkUser();
        mImageLoader = ImageLoader.getInstance(mContext);
        initView(layout);
        initData();
        setListener();
        registerCollectCountReceiver();
        registerUpdateCollectCountChangedReceiver();
        registerUpdateUserChangedReceiver();
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkUser();
    }


    private void checkUser(){
        mUser = FuLiCenterApplication.getInstance().getUserBean();
        Log.e(TAG,"checkUser,mUser="+mUser);
    }

    private void setListener() {
        listener = new MyClickListener();
        mLayoutCenterCollet.setOnClickListener(listener);
        mtvSettings.setOnClickListener(listener);
        mivMessage.setOnClickListener(listener);
        mLyaoutCenterUserInfo.setOnClickListener(listener);
    }

    class MyClickListener implements OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.layout_center_collect:
                    startActivity(new Intent(mContext, CollectActivity.class));
                    break;
                case R.id.tv_center_settings:
                case R.id.center_user_info:
                    startActivity(new Intent(mContext, SettingsActivity.class));
                    break;
                case R.id.iv_persona_center_msg:
                    startActivity(new Intent(mContext, WeChatActivity.class));
                    break;
            }
        }
    }

    private void initData() {
        Log.e(TAG,"initData,mtvUserName="+mtvUserName);
        Log.e(TAG,"initData,mUser="+mUser);
        mtvUserName.setText(mUser.getNick());
        Bitmap avatar = null;
        String path = mUser.getAvatar();
        Log.e(TAG,"initData,mCollectCount="+mCollectCount);
        mtvCollectCount.setText(""+mCollectCount);
        Log.e(TAG,"initData,path="+path);
        if(path!=null){
            Log.e(TAG,"getView,----1----path="+path);
            mivUserAvarar.setTag(path);
            avatar = mImageLoader.displayImage(path, mUser.getUserName(), 80, 80, new ImageLoader.OnImageLoadListener() {
                @Override
                public void onSuccess(String path, Bitmap bitmap) {
                    Log.e(TAG,"getView,----2----path="+path);
                    mivUserAvarar.setImageBitmap(bitmap);
                }
                @Override
                public void error(String errorMsg) {
                    // TODO Auto-generated method stub
                    Log.e(TAG,"getView,----3----errorMsg="+errorMsg);
                }
            });
        }
        if(avatar==null){
            mivUserAvarar.setImageResource(R.drawable.default_avatar);
            Log.e(TAG,"getView,--------holder.avatar.setImageResource------default");
        }else{
            mivUserAvarar.setImageBitmap(avatar);
            Log.e(TAG,"getView,--------holder.avatar.setImageBitmap");
        }
        mDownloadCollectCountTask = new DownloadCollectCountTask(mContext,mUser.getUserName());
        mDownloadCollectCountTask.execute();
    }

    private void initView(View layout) {
        mivUserAvarar = (ImageView) layout.findViewById(R.id.iv_user_avatar);
        mtvUserName = (TextView) layout.findViewById(R.id.tv_user_name);
        mLayoutCenterCollet = (LinearLayout) layout.findViewById(R.id.layout_center_collect);
        mtvCollectCount = (TextView) layout.findViewById(R.id.tv_collect_count);
        mtvSettings = (TextView) layout.findViewById(R.id.tv_center_settings);
        mivMessage = (ImageView) layout.findViewById(R.id.iv_persona_center_msg);
        mOrderList = (GridView) layout.findViewById(R.id.center_user_order_lis);
        mOrderList.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mAdapter = new OrderAdapter(mContext,pic_path);
        mOrderList.setAdapter(mAdapter);
        mLyaoutCenterUserInfo = (RelativeLayout) layout.findViewById(R.id.center_user_info);
    }

    class CollectCountChangedReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            mCollectCount = intent.getIntExtra("collect_count",0);
            Log.e(TAG,"CollectCountChangedReceiver,mCollectCount="+mCollectCount);
            refresh();
        }
    }

    private void registerCollectCountReceiver(){
        mReceiver = new CollectCountChangedReceiver();
        IntentFilter filter = new IntentFilter("update_collect_count");
        mContext.registerReceiver(mReceiver,filter);
    }

    private void refresh(){
        Log.e(TAG,"refresh...............");
        mtvCollectCount.setText(""+mCollectCount);
        mUser = FuLiCenterApplication.getInstance().getUserBean();
        mtvUserName.setText(mUser.getNick());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mReceiver!=null){
            mContext.unregisterReceiver(mReceiver);
        }
    }

    class UpdateCollectCountChangedReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            new DownloadCollectCountTask(mContext,mUser.getUserName()).execute();
        }
    }
    private void registerUpdateCollectCountChangedReceiver(){
        mUpdateReceiver = new UpdateCollectCountChangedReceiver();
        IntentFilter filter = new IntentFilter("good_details_update");
        mContext.registerReceiver(mUpdateReceiver,filter);
    }

    class UpdateUserChangerReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }
    }
    UpdateUserChangerReceiver mUpdateUserReceiver;
    private void registerUpdateUserChangedReceiver(){
        mUpdateUserReceiver = new UpdateUserChangerReceiver();
        IntentFilter filter = new IntentFilter("update_user");
        mContext.registerReceiver(mUpdateUserReceiver,filter);
    }
}
