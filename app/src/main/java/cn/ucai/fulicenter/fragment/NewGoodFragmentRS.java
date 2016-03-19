package cn.ucai.fulicenter.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.MainActivity;
import cn.ucai.fulicenter.adapter.GoodAdapterRS;
import cn.ucai.fulicenter.adapter.HeaderViewRecyclerAdapter;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.utils.DialogUtils;
import cn.ucai.fulicenter.utils.NetUtil;

/**
 * Created by clawpo on 16/3/18.
 */
public class NewGoodFragmentRS extends Fragment {
    public static final String TAG = NewGoodFragmentRS.class.getName();
    MainActivity mContext;

    ArrayList<NewGoodBean> mGoodList;
    GoodAdapterRS mAdapter;
    HeaderViewRecyclerAdapter mHeaderAdapter;
    /** 下拉刷新控件*/
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mRecyclerView;
    TextView mtvHint;
    GridLayoutManager mGridLayoutManager;

    /** 分页下载商品的页码*/
    int mPageId=0;
    /** 每页下载商品的数量*/
    final int PAGE_SIZE=10;
    /** 每行显示的数量*/
    int columNum = 2;

    /** 下拉刷新*/
    static final int ACTION_DOWNLOAD=0;
    /** 第一次下载*/
    static final int ACTION_PULL_DOWN=1;
    /** 上拉刷新*/
    static final int ACTION_PULL_UP=2;

    DownloadGoodsTaskRS mDownloadGoodsTask;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = (MainActivity)getActivity();
        View layout = View.inflate(mContext, R.layout.fragment_new_good_swipe_refresh, null);
        initView(layout);
        setListener(layout);
        mDownloadGoodsTask = new DownloadGoodsTaskRS(
                mGoodList, ACTION_DOWNLOAD,I.CAT_ID,I.NEW_GOOD);
        mDownloadGoodsTask.execute(mPageId,PAGE_SIZE);
        return layout;

    }

    private void setListener(View layout) {
        setPullDownRefreshListener();
        setPullUpRefreshListener();
    }

    /**
     * 上拉刷新事件监听
     */
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
                                mPageId +=PAGE_SIZE;
                                mDownloadGoodsTask = new DownloadGoodsTaskRS(
                                        mGoodList, ACTION_PULL_UP,I.CAT_ID,I.NEW_GOOD);
                                mDownloadGoodsTask.execute(mPageId, PAGE_SIZE);
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

    /**
     * 下拉刷新事件监听
     */
    private void setPullDownRefreshListener() {
        mSwipeRefreshLayout.setOnRefreshListener(
                new OnRefreshListener(){
                    @Override
                    public void onRefresh() {
                        mtvHint.setVisibility(View.VISIBLE);
                        mPageId = 0;
                        mDownloadGoodsTask = new DownloadGoodsTaskRS(
                                mGoodList, ACTION_PULL_DOWN,I.CAT_ID,I.NEW_GOOD);
                        mDownloadGoodsTask.execute(mPageId,PAGE_SIZE);
                    }
                }
        );
    }


    private void initView(View layout) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.sfl_newgood);
        mSwipeRefreshLayout.setColorSchemeColors(
                R.color.google_blue,
                R.color.google_green,
                R.color.google_red,
                R.color.google_yellow
        );
        mtvHint = (TextView) layout.findViewById(R.id.tv_refresh_hint);
        mGridLayoutManager = new GridLayoutManager(mContext, columNum);
        mGridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.rv_newgood);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mAdapter = new GoodAdapterRS(mContext,mGoodList,I.SORT_BY_ADDTIME_DESC);
        mHeaderAdapter = new HeaderViewRecyclerAdapter(mAdapter);
        mRecyclerView.setAdapter(mHeaderAdapter);
        //添加分隔条,分隔条为网格布局方式
//        mRecyclerView.addItemDecoration(
//        new DividerGridItemDecoration(mContext));
    }

    class DownloadGoodsTaskRS extends AsyncTask<Integer,Void,ArrayList<NewGoodBean>> {
        public final String TAG = DownloadGoodsTaskRS.class.getName();

        int action;//加载数据的类型：0：重新下载，1：添加新的数据
        int catId;
        /** 0:新品或者精选;1:分类 */
        int goodType;

        public DownloadGoodsTaskRS(ArrayList<NewGoodBean> mGoodList, int action, int catId, int goodType) {
            this.action = action;
            this.catId = catId;
            this.goodType = goodType;
        }


        @Override
        protected void onPreExecute() {
            mSwipeRefreshLayout.setRefreshing(true);
            switch (action) {
                case ACTION_DOWNLOAD:
                    DialogUtils.showProgressDialog(mContext, "加载商品", "加载中...");
                    break;
                case ACTION_PULL_UP:
                    if (mAdapter.isMore()) {
                        mAdapter.setFooterText("正在加载数据...");
                    }
                    break;
            }
        }

        @Override
        protected ArrayList<NewGoodBean> doInBackground(Integer... params) {
            int pageId=params[0];
            int pageSize=params[1];
            ArrayList<NewGoodBean> goodList=null;
            try{
                switch (goodType){
                    case I.NEW_GOOD:
                        goodList = NetUtil.findNewandBoutiqueGoods(catId,pageId,pageSize);
                        break;
                    case I.CATEGORY_GOOD:
                        goodList = NetUtil.findGoodsDetails(mContext,catId,pageId,pageSize);
                        break;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return goodList;
        }

        @Override
        protected void onPostExecute(ArrayList<NewGoodBean> goods) {
            DialogUtils.closeProgressDialog();
            mSwipeRefreshLayout.setRefreshing(false);
            mtvHint.setVisibility(View.GONE);
            if (goods == null || goods.size()==0) {
                mAdapter.setMore(false);
                if (action == ACTION_PULL_UP) {
                    mAdapter.setFooterText("没有更多数据");
                }
                return;
            }
            mAdapter.setMore(true);
            switch (action){
                case ACTION_DOWNLOAD:
                case ACTION_PULL_DOWN:
                    mAdapter.initItems(goods);
                    break;
                case ACTION_PULL_UP:
                    if(goods!=null){
                        mAdapter.addItems(goods);
                        mAdapter.setMore(true);
                    } else {
                        mAdapter.setMore(false);
                    }
                    break;
            }
        }
    }
}
