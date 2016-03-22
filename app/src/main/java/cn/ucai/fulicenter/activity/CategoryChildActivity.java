package cn.ucai.fulicenter.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.adapter.GoodAdapterRS;
import cn.ucai.fulicenter.bean.CategoryChildBean;
import cn.ucai.fulicenter.bean.ColorBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.task.DownloadGoodsTaskRS;
import cn.ucai.fulicenter.utils.NetUtil;
import cn.ucai.fulicenter.utils.NetUtilRS;
import cn.ucai.fulicenter.view.CatChildFilterButton;
import cn.ucai.fulicenter.view.ColorFilterButton;

/**
 * Created by ucai001 on 2016/3/11.
 */
public class CategoryChildActivity extends BaseActivity {
    public static final String TAG = CategoryChildActivity.class.getName();
    Context mContext;
    /** 当前小类的数据集*/
    ArrayList<CategoryChildBean> mChildList;
    /** 大类的名称*/
    String mGroupName;
    ArrayList<GoodDetailsBean> mGoodDetailsList;
    ArrayList<NewGoodBean> mGoodList;
    DownloadGoodsTaskRS mDownloadGoodsTask;
//    PullRefreshView<GridView> mprvChild;
//    GridView mgvChild;

    /** 下拉刷新控件*/
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mRecyclerView;
    TextView mtvHint;
    GridLayoutManager mGridLayoutManager;
    GoodAdapterRS mAdapter;
    
    CatChildFilterButton mbtnCatFilter;
    ColorFilterButton mbtnColorFilter;
    /** 分类的id*/
    int mCatId;
    int mPageId;
    final int PAGE_SIZE=6;
    
    GoodDetailsUpdateReceiver mReceiver;
    
    /**排序状态，以下常量值之一
     * SORT_BY_PRICE_ASC
     * SORT_BY_PRICE_DESC
     * SORT_BY_ADDTIME_ASC
     * SORT_BY_ADDTIME_DESC
     */
    int mSortBy;
    /**按价格排序*/
    Button mbtnPriceSort;
    /** 按上架时间排序*/
    Button mbtnAddTimeSort;
    /** 商品按价格排序，
     * true：升序排序
     * false：降序排序*/
    boolean mSortByPriceAsc;
    /**
     * 商品按上架时间排序
     * true：升序排序
     * false：降序排序
     */
    boolean mSortByAddTimeAsc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_child);
        mContext=this;
        mGoodList = new ArrayList<NewGoodBean>();
        initView();
        initData();
        setListener();
        registerGoodsDetailsUpdateReceiver();
    }
    private void setListener() {
        setReturnClickListener();
        setPullDownRefreshListener();
        setPullUpRefreshListener();
        SortStateChangedListener listener=new SortStateChangedListener();
        mbtnPriceSort.setOnClickListener(listener);
        mbtnAddTimeSort.setOnClickListener(listener);
        mbtnCatFilter.setOnCatFilterClickListener(mGroupName, mChildList);
        
    }

    private void setPullUpRefreshListener() {
        mRecyclerView.setOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    int lastItemPosition;
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if(newState == RecyclerView.SCROLL_STATE_IDLE &&
                                lastItemPosition == mAdapter.getItemCount()-1){
                            if(mAdapter.isMore()){
                                mSwipeRefreshLayout.setRefreshing(true);
//                                mPageId++;
                                mPageId +=PAGE_SIZE;
                                try {
                                    NetUtilRS.findNewandBoutiqueGoods(mAdapter,mCatId,mPageId,
                                            PAGE_SIZE,I.ACTION_PULL_UP,mSwipeRefreshLayout,mtvHint);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        //获取最后列表项的下标
                        lastItemPosition = mGridLayoutManager.findLastVisibleItemPosition();
                    }
                }
        );
    }

    private void setPullDownRefreshListener() {
        mSwipeRefreshLayout.setOnRefreshListener(
            new SwipeRefreshLayout.OnRefreshListener(){
                @Override
                public void onRefresh() {
                    mtvHint.setVisibility(View.VISIBLE);
                    mPageId = 0;
                    try {
                        NetUtilRS.findNewandBoutiqueGoods(mAdapter,mCatId,mPageId,PAGE_SIZE,
                                I.ACTION_DOWNLOAD,mSwipeRefreshLayout,mtvHint);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        );
    }

    private void setReturnClickListener() {
        findViewById(R.id.ivReturn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * 商品详情集合下载完成的广播接收者
     * 接收来自NetUtil.findGoodsDetails方法发送的广播 
     */
    private void registerGoodsDetailsUpdateReceiver() {
        mReceiver=new GoodDetailsUpdateReceiver();
        IntentFilter filter=new IntentFilter("good_details_update");
        registerReceiver(mReceiver, filter);
    }

    private void initData() {
        mSortBy=I.SORT_BY_ADDTIME_DESC;
        mCatId=getIntent().getIntExtra(I.CategoryChild.CAT_ID, 0);
        Log.e(TAG,"initData,mCatId="+mCatId);
        mGroupName=getIntent().getStringExtra(I.CategoryGroup.NAME);
        mChildList=(ArrayList<CategoryChildBean>) getIntent().getSerializableExtra("children");
        
        mGoodList=(ArrayList<NewGoodBean>) getIntent().getSerializableExtra("goodList");
        if(mGoodList==null){
            try {
                NetUtilRS.findNewandBoutiqueGoods(mAdapter,mCatId,mPageId,PAGE_SIZE,I.ACTION_DOWNLOAD,
                        mSwipeRefreshLayout,mtvHint);
                Log.e(TAG,"initData,mGoodList.1size="+mGoodList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            Log.e(TAG,"initData,mGoodList.2size="+mGoodList.size());
            mAdapter.initItems(mGoodList);
        }
        
        new DownloadColorFilterTask(mContext, mCatId).execute();
    }
    private void initView() {
        mSwipeRefreshLayout = getViewById(R.id.srl_category_child);
        mSwipeRefreshLayout.setColorSchemeColors(
                R.color.google_blue,
                R.color.google_green,
                R.color.google_red,
                R.color.google_yellow
        );
        mtvHint = getViewById(R.id.tv_refresh_hint);
        mGridLayoutManager = new GridLayoutManager(mContext, I.COLUM_NUM);
        mGridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = getViewById(R.id.rv_category_child);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mGoodList=new ArrayList<NewGoodBean>();
        mAdapter = new GoodAdapterRS(mContext,mGoodList,I.SORT_BY_ADDTIME_DESC);
        mRecyclerView.setAdapter(mAdapter);
        
        mbtnCatFilter=getViewById(R.id.btnCatChildFilter);
        mbtnColorFilter=getViewById(R.id.btnColorFilter);
        mbtnColorFilter.setVisibility(View.INVISIBLE);
        
        mbtnAddTimeSort=getViewById(R.id.btnAddTimeSort);
        mbtnPriceSort=getViewById(R.id.btnPriceSort);

    }

    class GoodDetailsUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mGoodDetailsList=(ArrayList<GoodDetailsBean>) getIntent().getSerializableExtra("goods_details");
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mReceiver!=null){
            unregisterReceiver(mReceiver);
        }
    }
    
    class SortStateChangedListener implements OnClickListener{
        @Override
        public void onClick(View v) {
            Drawable right=null;
            switch (v.getId()) {
            case R.id.btnPriceSort:
                if(mSortByPriceAsc){
                    mSortBy=I.SORT_BY_PRICE_ASC;
                    right=mContext.getResources().getDrawable(R.drawable.arrow_order_up);
                }else{
                    mSortBy=I.SORT_BY_PRICE_DESC;
                    right=mContext.getResources().getDrawable(R.drawable.arrow_order_down);
                }
                mSortByPriceAsc=!mSortByPriceAsc;
                right.setBounds(0, 0, 17, 20);
                mbtnPriceSort.setCompoundDrawables(null, null, right, null);
                break;
            case R.id.btnAddTimeSort:
                if(mSortByAddTimeAsc){
                    mSortBy=I.SORT_BY_ADDTIME_ASC;
                    right=mContext.getResources().getDrawable(R.drawable.arrow_order_up);
                }else{
                    mSortBy=I.SORT_BY_ADDTIME_DESC;
                    right=mContext.getResources().getDrawable(R.drawable.arrow_order_down);
                }
                mSortByAddTimeAsc=!mSortByAddTimeAsc;
                right.setBounds(0, 0, 17, 20);
                mbtnAddTimeSort.setCompoundDrawables(null, null, right, null);
                break;
            }
            mAdapter.setSortBy(mSortBy);
        }
    }
    
    /**
     * 下载颜色列表
     */
    class DownloadColorFilterTask extends AsyncTask<Void, Void, Boolean> {
        Context context;
        int catId;
        ArrayList<ColorBean> colorList;
        
        public DownloadColorFilterTask(Context context, int catId) {
            super();
            this.context = context;
            this.catId = catId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            colorList= NetUtil.findColors(catId);
            return colorList!=null;
        }
        
        @Override
        protected void onPostExecute(Boolean result) {
            if(result){
                mbtnColorFilter.setVisibility(View.VISIBLE);
                mbtnColorFilter.setOnColorFilterClickListener(mGroupName,mChildList,colorList);
            }
        }
    }
}
