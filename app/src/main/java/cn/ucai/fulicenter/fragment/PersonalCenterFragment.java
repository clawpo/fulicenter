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
import android.widget.TextView;

import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.CollectActivity;
import cn.ucai.fulicenter.activity.LoginActivity;
import cn.ucai.fulicenter.activity.SettingsActivity;
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
    LinearLayout mLayoutCenterCollet;

    ImageLoader mImageLoader;

    int mCollectCount = 0;
    UserBean mUser;
    DownloadCollectCountTask mDownloadCollectCountTask;
    CollectCountChangedReceiver mReceiver;
    UpdateCollectCountChangedReceiver mUpdateReceiver;
    MyClickListener listener;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        checkUser();
        mContext = getActivity();
        View layout = View.inflate(mContext, R.layout.fragment_personal_center,null);

        mImageLoader = ImageLoader.getInstance(mContext);
        initView(layout);
        initData();
        setListener();
        registerCollectCountReceiver();
        registerUpdateCollectCountChangedReceiver();
        return layout;
    }

    private void checkUser(){
        mUser = FuLiCenterApplication.getInstance().getUserBean();
        if(mUser==null){
            Intent intent = new Intent(mContext,LoginActivity.class);
            startActivityForResult(intent, I.REQUEST_CODE_LOGIN);
        }
    }

    private void setListener() {
        Log.e(TAG,"setListener....");
        listener = new MyClickListener();
        mLayoutCenterCollet.setOnClickListener(listener);
        mtvSettings.setOnClickListener(listener);
    }

    class MyClickListener implements OnClickListener{
        @Override
        public void onClick(View v) {
            Log.e(TAG,"MyClickListener....v.getId()="+v.getId());
            switch (v.getId()){
                case R.id.layout_center_collect:
                    Log.e(TAG,"MyClickListener....startActivity(new Intent(mContext, CollectActivity.class));");
                    startActivity(new Intent(mContext, CollectActivity.class));
                    break;
                case R.id.tv_center_settings:
                    startActivity(new Intent(mContext, SettingsActivity.class));
                    break;
            }
        }
    }

    private void initData() {
        mtvUserName.setText(mUser.getNick());
        Log.e(TAG,"initData,mUser="+mUser);
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
        mOrderList = (GridView) layout.findViewById(R.id.center_user_order_lis);
        mOrderList.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mAdapter = new OrderAdapter(mContext,pic_path);
        mOrderList.setAdapter(mAdapter);
        mivUserAvarar = (ImageView) layout.findViewById(R.id.iv_user_avatar);
        mtvUserName = (TextView) layout.findViewById(R.id.tv_user_name);
        mLayoutCenterCollet = (LinearLayout) layout.findViewById(R.id.layout_center_collect);
        mtvCollectCount = (TextView) layout.findViewById(R.id.tv_collect_count);
        mtvSettings = (TextView) layout.findViewById(R.id.tv_center_settings);
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
        mtvCollectCount.setText(""+mCollectCount);
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
}
