package cn.ucai.fulicenter.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.adapter.OrderAdapter;
import cn.ucai.fulicenter.bean.UserBean;

/**
 * Created by ucai001 on 2016/3/12.
 */
public class PersonalCenterFragment extends Fragment {
    Context mContext;

    GridView mOrderList;
    //资源文件
    private int[] pic_path={R.drawable.order_list1,R.drawable.order_list2,R.drawable.order_list3,R.drawable.order_list4,R.drawable.order_list5};
    OrderAdapter mAdapter;
    ImageView mUserAvarar;
    TextView mUserName;
    UserBean mUser;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        View layout = View.inflate(mContext, R.layout.fragment_personal_center,null);
        mUser = FuLiCenterApplication.getInstance().getUserBean();
        initView(layout);
        initData();
        return layout;
    }

    private void initData() {
        mUserName.setText(mUser.getNick());

    }

    private void initView(View layout) {
        mOrderList = (GridView) layout.findViewById(R.id.center_user_order_lis);
        mOrderList.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mAdapter = new OrderAdapter(mContext,pic_path);
        mOrderList.setAdapter(mAdapter);
        mUserAvarar = (ImageView) layout.findViewById(R.id.iv_user_avatar);
        mUserName = (TextView) layout.findViewById(R.id.tv_user_name);
    }

}
