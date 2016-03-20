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
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.UserBean;
import cn.ucai.fulicenter.fragment.BoutiqueFragment;
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
	TextView mtvCart;
	TextView mtvNewGood;
	TextView mtvBoutique;
	TextView mtvCategory;
	TextView mtvPersonalCenter;

	ImageView mivCart;
	ImageView mivNewGood;
	ImageView mivBoutique;
	ImageView mivCategory;
	ImageView mivPersonalCenter;

	RelativeLayout mLayoutCart;
	RelativeLayout mLayoutNewGood;
	RelativeLayout mLayoutBoutique;
	RelativeLayout mLayoutCategory;
	RelativeLayout mLayoutPersonalCenter;

	private NewGoodFragmentRS mNewGoodFragment;
	private BoutiqueFragment mBoutiqueFragment;
	 private CategoryFragment mCategoryFragment;
    private CartFragmentRS mCartFragment;
	private PersonalCenterFragment mPersonalCenterFragment;
//	private FindFragment mFindFragment;
	private Fragment[] mFragments;
	private int index;
	// 当前fragment的index
	private int currentTabIndex = -1;


    FragmentActivity mContext;

    Drawable drawableNewGood,drawableBoutique,drawableCategory,drawableCart,drawablePersonalCenter;

    private int [] mMenuDrawableNormal = {R.drawable.menu_item_new_good_normal,R.drawable.boutique_normal,
            R.drawable.menu_item_category_normal,R.drawable.menu_item_cart_normal,R.drawable.menu_item_personal_center_normal};
    private int [] mMenuDrawableSelected = {R.drawable.menu_item_new_good_selected,R.drawable.boutique_selected,
            R.drawable.menu_item_category_selected,R.drawable.menu_item_cart_selected,R.drawable.menu_item_personal_center_selected};
    private ImageView[] mImageViews = new ImageView[5];
    private Drawable[] mDrawable = new Drawable[5];
	
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
        setListener();
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
		}
	}

    private void initFragment() {
		mNewGoodFragment = new NewGoodFragmentRS();
        mBoutiqueFragment = new BoutiqueFragment();
		mCategoryFragment = new CategoryFragment();
		mCartFragment = new CartFragmentRS();
		mPersonalCenterFragment = new PersonalCenterFragment();
		mFragments = new Fragment[] {
				mNewGoodFragment,mBoutiqueFragment,
				mCategoryFragment,mCartFragment,mPersonalCenterFragment};
    }

    private void initNewGood(){
        setMenuItemDefaultDrawable();
        setMenuItemDrawable();
        currentTabIndex = 0;
        index = 0;
        drawableNewGood = getmDrawable(R.drawable.menu_item_new_good_selected);
        mivNewGood.setImageDrawable(drawableNewGood);
        FragmentUtils.startFragment(mContext, mNewGoodFragment);
//        FragmentUtils.showFragment(mContext,currentTabIndex,0,mFragments);
            FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
            trx.hide(mFragments[1]).hide(mFragments[2]).hide(mFragments[3]).hide(mFragments[4]).show(mFragments[0]).commit();
            currentTabIndex = index;
            setMenuItemDrawable();
    }

    private void setFragment(int newIndex){
        if(newIndex==-1){
            newIndex=0;
        }
        setMenuItemDefaultDrawable();
        setMenuItemDrawable();
        mDrawable[newIndex] = getmDrawable(mMenuDrawableSelected[newIndex]);
        mImageViews[newIndex].setImageDrawable(mDrawable[newIndex]);
        FragmentUtils.showFragment(mContext,currentTabIndex,newIndex,mFragments);
        currentTabIndex = newIndex;
        index = newIndex;
    }

	/**
	 * 注册设置菜单项单击事件监听
	 */
	private void setListener() {
        setMenuItemClickListener();
    }

	/**
	 * 设置菜单项单击事件监听
	 */
    private void setMenuItemClickListener() {
        MenuItemClickListener listener=new MenuItemClickListener();
        mLayoutNewGood.setOnClickListener(listener);
        mLayoutBoutique.setOnClickListener(listener);
		mLayoutCategory.setOnClickListener(listener);
		mLayoutCart.setOnClickListener(listener);
		mLayoutPersonalCenter.setOnClickListener(listener);
    }

	/**
	 * 初始化组件
	 */
	private void initView() {
		mtvCartHint = getViewById(R.id.tvCartHint);
		mtvCart = getViewById(R.id.tvCart);
		mtvBoutique = getViewById(R.id.tvBoutique);
		mtvCategory = getViewById(R.id.tvCategory);
		mtvNewGood = getViewById(R.id.btnNewGood);
		mtvPersonalCenter = getViewById(R.id.tvPersonalCenter);

		mivBoutique=getViewById(R.id.ivBoutique);
		mivCart=getViewById(R.id.ivCart);
		mivCategory=getViewById(R.id.ivCategory);
		mivNewGood=getViewById(R.id.ivNewGood);
		mivPersonalCenter=getViewById(R.id.ivPersonalCenter);

		mLayoutBoutique=getViewById(R.id.layout_boutique);
		mLayoutCart=getViewById(R.id.layout_cart);
		mLayoutCategory=getViewById(R.id.layout_category);
		mLayoutNewGood=getViewById(R.id.layout_new_good);
		mLayoutPersonalCenter=getViewById(R.id.layout_personal_center);

        mImageViews[0]=mivNewGood;
        mImageViews[1]=mivBoutique;
        mImageViews[2]=mivCategory;
        mImageViews[3]=mivCart;
        mImageViews[4]=mivPersonalCenter;
        setMenuItemDefaultDrawable();
        mDrawable[0]=drawableNewGood;
        mDrawable[1]=drawableBoutique;
        mDrawable[2]=drawableCategory;
        mDrawable[3]=drawableCart;
        mDrawable[4]=drawablePersonalCenter;
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
        Log.e(TAG,"MenuItemClickListener startLogin,action="+action);
        Intent intent = new Intent(MainActivity.this,LoginActivity.class).putExtra("action",action);
        startActivity(intent);
//        startActivityForResult(intent,I.REQUEST_CODE_LOGIN);
    }
	
	/**
	 * 底部菜单项点击事件监听器
	 * @author yao
	 *
	 */
	class MenuItemClickListener implements View.OnClickListener {
	    @Override
	    public void onClick(View v) {
            setMenuItemDefaultDrawable();
            Fragment fragment = null;
            switch (v.getId()) {
	        case R.id.layout_new_good:
                index = 0;
                drawableNewGood = getmDrawable(R.drawable.menu_item_new_good_selected);
                fragment = mNewGoodFragment;
	            break;
	        case R.id.layout_boutique:
	            index = 1;
                drawableBoutique = getmDrawable(R.drawable.boutique_selected);
                fragment = mBoutiqueFragment;
	            break;
	        case R.id.layout_category:
	            index=2;
                drawableCategory = getmDrawable(R.drawable.menu_item_category_selected);
				fragment = mCategoryFragment;
	            break;
	        case R.id.layout_cart:
	            index =3;
                drawableCart = getmDrawable(R.drawable.menu_item_cart_selected);
                fragment = mCartFragment;
	            break;
            case R.id.layout_personal_center:
                mUser = FuLiCenterApplication.getInstance().getUserBean();
                Log.e(TAG,"mUser="+mUser);
                if(mUser!=null) {
                    index = 4;
                    drawablePersonalCenter = getmDrawable(R.drawable.menu_item_personal_center_selected);
                    fragment = mPersonalCenterFragment;
                }else{
                    Log.e(TAG,"MenuItemClickListener startActivityForResult");
//                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
//                    Intent intent = new Intent(MainActivity.this,LoginActivity.class);
//                    startActivityForResult(intent,I.REQUEST_CODE_LOGIN);
                    startLogin("person");
                }
                break;
	        }

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
                setMenuItemDrawable();
//                if(fragment!=null){
//                    Log.e(TAG,"MenuItemClickListener.FragmentUtils.startFragment,fragment="+fragment);
//                    FragmentUtils.startFragment(mContext,fragment);
//                }
	        }
	    }
	}

    //    @SuppressLint("Override")
    public Drawable getmDrawable(int id){
        Resources res = getResources();
        Drawable drawable = res.getDrawable(id);
        return drawable;
    }
    /** 设置菜单项按钮顶部缺省显示的图片 */
    private void setMenuItemDefaultDrawable() {
        drawableNewGood = getmDrawable(R.drawable.menu_item_new_good_normal);
        drawableBoutique = getmDrawable(R.drawable.boutique_normal);
        drawableCategory = getmDrawable(R.drawable.menu_item_category_normal);
        drawableCart = getmDrawable(R.drawable.menu_item_cart_normal);
        drawablePersonalCenter = getmDrawable(R.drawable.menu_item_personal_center_normal);
    }
    float mDensity;
    /** 设置菜单项按钮顶部某菜单项被选择后显示的图片 */
    private void setMenuItemDrawable() {
        int width = (int) (mDensity * 32);
        int height = width;

        Rect bounds = new Rect(0, 0, width, height);
        drawableNewGood.setBounds(bounds);
        mivNewGood.setImageDrawable(drawableNewGood);
        drawableBoutique.setBounds(bounds);
        mivBoutique.setImageDrawable(drawableBoutique);
		drawableCategory.setBounds(bounds);
		mivCategory.setImageDrawable(drawableCategory);
        drawableCart.setBounds(bounds);
        mivCart.setImageDrawable(drawableCart);
		drawablePersonalCenter.setBounds(bounds);
		mivPersonalCenter.setImageDrawable(drawablePersonalCenter);
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
