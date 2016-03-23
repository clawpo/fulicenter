/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ucai.fulicenter.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.UserBean;
import cn.ucai.fulicenter.fragment.BoutiqueFragmentRS;
import cn.ucai.fulicenter.fragment.CartFragmentRS;
import cn.ucai.fulicenter.fragment.CategoryFragment;
import cn.ucai.fulicenter.fragment.NewGoodFragmentRS;
import cn.ucai.fulicenter.fragment.PersonalCenterFragment;
import cn.ucai.fulicenter.task.DownloadCartTask;
import cn.ucai.fulicenter.utils.FragmentUtils;
import cn.ucai.fulicenter.utils.Utils;

public class MainActivity extends BaseActivity {

	protected static final String TAG = MainActivity.class.getName();

	// 菜单项按钮
	TextView mtvCartHint;
    RadioButton mLayoutCart;
    RadioButton mLayoutNewGood;
    RadioButton mLayoutBoutique;
    RadioButton mLayoutCategory;
    RadioButton mLayoutPersonalCenter;

	private NewGoodFragmentRS mNewGoodFragment;
	private BoutiqueFragmentRS mBoutiqueFragment;
    private CategoryFragment mCategoryFragment;
    private CartFragmentRS mCartFragment;
	private PersonalCenterFragment mPersonalCenterFragment;
	private Fragment[] mFragments;
	private int index;
	// 当前fragment的index
	private int currentTabIndex = -1;

    FragmentActivity mContext;
    private RadioButton[] mRadios= new RadioButton[5];
    UserBean mUser;
    CartChangedReceiver mCartChangedReceiver;
    private String action;


	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext=this;
		setContentView(R.layout.activity_main);
		initView();
		initFragment();
        mUser = FuLiCenterApplication.getInstance().getUserBean();
		new DownloadCartTask(mContext, I.PAGE_ID_DEFAULT,I.PAGE_SIZE_DEFAULT).execute();
	}

    @Override
    protected void onStart() {
        action = getIntent().getStringExtra("action");
        super.onStart();
    }


    private void showWhat(){
        action = getIntent().getStringExtra("action");
        mUser = FuLiCenterApplication.getInstance().getUserBean();
		if(action == null || action.equals("back")){
            if(mUser==null && (currentTabIndex==-1 || currentTabIndex ==4 || currentTabIndex==0)){
                initNewGood();
            }else {
                setFragment(currentTabIndex);
            }
		} else if(action.equals("person") || action.equals("settings")){
            if(mUser==null){
                initNewGood();
            }else {
                sendBroadcast(new Intent("update_user"));
                sendBroadcast(new Intent("update_collect_count"));
                setFragment(4);
            }
		} else if(action.equals("buy")){
            setFragment(3);
        }
	}

    private void initFragment() {
		mNewGoodFragment = new NewGoodFragmentRS();
        mBoutiqueFragment = new BoutiqueFragmentRS();
		mCategoryFragment = new CategoryFragment();
		mCartFragment = new CartFragmentRS();
		mPersonalCenterFragment = new PersonalCenterFragment();
		mFragments = new Fragment[] {
				mNewGoodFragment,mBoutiqueFragment,
				mCategoryFragment,mCartFragment,mPersonalCenterFragment};
    }

    private void initNewGood(){
        currentTabIndex = 0;
        index = 0;
        FragmentUtils.startFragment(mContext, mNewGoodFragment);
        FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
        trx.hide(mFragments[1]).hide(mFragments[2]).hide(mFragments[3]).hide(mFragments[4]).show(mFragments[0]).commit();
        currentTabIndex = index;
        mLayoutNewGood.setChecked(true);
        mLayoutPersonalCenter.setChecked(false);
    }

    private void setFragment(int newIndex){
        if(newIndex==-1){
            newIndex=0;
        }
        Log.e(TAG,"setFragment,currentTabIndex="+currentTabIndex+",newIndex="+newIndex);
        setRadioDefaultChecked(newIndex);
        FragmentUtils.showFragment(mContext,currentTabIndex,newIndex,mFragments);
        currentTabIndex = newIndex;
        index = newIndex;
    }

    private void setRadioDefaultChecked(int index){
        for(int i=0;i<mRadios.length;i++){
            if(i==index){
                mRadios[i].setChecked(true);
            }else{
                mRadios[i].setChecked(false);
            }
        }
    }

    public void onCheckedChange(View view){
        switch (view.getId()) {
            case R.id.layout_new_good:
                index = 0;
                break;
            case R.id.layout_boutique:
                index = 1;
                break;
            case R.id.layout_category:
                index=2;
                break;
            case R.id.layout_cart:
                index =3;
                break;
            case R.id.layout_personal_center:
                mUser = FuLiCenterApplication.getInstance().getUserBean();
                Log.e(TAG,"mUser="+mUser);
                if(mUser!=null) {
                    index = 4;
                }else{
                    startLogin("person");
                    return;
                }
                break;
        }

        setRadioDefaultChecked(index);
        if (currentTabIndex != index) {
            FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
            if(currentTabIndex>-1){
                trx.hide(mFragments[currentTabIndex]);
            }
            if (!mFragments[index].isAdded()) {
                trx.add(R.id.fragment_container, mFragments[index]);
            }
            trx.show(mFragments[index]).commit();
            currentTabIndex = index;
        }
    }

	/**
	 * 初始化组件
	 */
	private void initView() {
		mtvCartHint = getViewById(R.id.tvCartHint);
		mLayoutBoutique=getViewById(R.id.layout_boutique);
		mLayoutCart=getViewById(R.id.layout_cart);
		mLayoutCategory=getViewById(R.id.layout_category);
		mLayoutNewGood=getViewById(R.id.layout_new_good);
		mLayoutPersonalCenter=getViewById(R.id.layout_personal_center);

        mRadios[0]= mLayoutNewGood;
        mRadios[1]= mLayoutBoutique;
        mRadios[2]= mLayoutCategory;
        mRadios[3]= mLayoutCart;
        mRadios[4]= mLayoutPersonalCenter;
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
        if(mCartChangedReceiver!=null){
            unregisterReceiver(mCartChangedReceiver);
        }
	}


	@Override
	protected void onResume() {
		super.onResume();
        registerCartChangedReceiver();
        showWhat();
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
        setIntent(intent);
        action = getIntent().getStringExtra("action");
	}

    private void startLogin(String action){
        Intent intent = new Intent(MainActivity.this,LoginActivity.class).putExtra("action",action);
        startActivity(intent);
    }

    /**
     * 接收来自DownloadCartTask发送的购物车数据改变的广播
     * @author yao
     */
    class CartChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //统计购物车中的商品件数
			int count=Utils.sumCartCount();
			if(count>0){
				//显示购物车中的商品件数
				mtvCartHint.setText(""+count);
				mtvCartHint.setVisibility(View.VISIBLE);
			} else {
				mtvCartHint.setVisibility(View.GONE);
			}
        }
    }

    private void registerCartChangedReceiver() {
        mCartChangedReceiver=new CartChangedReceiver();
        IntentFilter filter=new IntentFilter("cartChanged");
        registerReceiver(mCartChangedReceiver, filter);
    }
    
    
}
