package cn.ucai.fulicenter.fragment;

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
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.utils.NetUtilRS;

/**
 * Created by clawpo on 16/3/18.
 */
public class NewGoodFragmentRS extends Fragment {
    public static final String TAG = NewGoodFragmentRS.class.getName();
    MainActivity mContext;

    ArrayList<NewGoodBean> mGoodList;
    GoodAdapterRS mAdapter;
    /** 下拉刷新控件*/
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mRecyclerView;
    TextView mtvHint;
    GridLayoutManager mGridLayoutManager;

    /** 分页下载商品的页码*/
    int mPageId=0;
    /** 每页下载商品的数量*/
    final int PAGE_SIZE=10;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = (MainActivity)getActivity();
        View layout = View.inflate(mContext, R.layout.fragment_new_good_rs, null);
        mGoodList = new ArrayList<NewGoodBean>();
        initView(layout);
        setListener();
        initData();
        return layout;

    }

    private void initData() {
        try {
            NetUtilRS.findNewandBoutiqueGoods(mAdapter,I.CAT_ID,mPageId,PAGE_SIZE,I.ACTION_DOWNLOAD,
                    mSwipeRefreshLayout,mtvHint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setListener() {
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
                                try {
                                    NetUtilRS.findNewandBoutiqueGoods(mAdapter,I.CAT_ID,mPageId,
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
                        try {
                            NetUtilRS.findNewandBoutiqueGoods(mAdapter,I.CAT_ID,mPageId,PAGE_SIZE,
                                    I.ACTION_DOWNLOAD,mSwipeRefreshLayout,mtvHint);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
        mGridLayoutManager = new GridLayoutManager(mContext, I.COLUM_NUM);
        mGridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.rv_newgood);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mAdapter = new GoodAdapterRS(mContext,mGoodList,I.SORT_BY_ADDTIME_DESC);
        mRecyclerView.setAdapter(mAdapter);
        //添加分隔条,分隔条为网格布局方式
//        mRecyclerView.addItemDecoration(
//        new DividerGridItemDecoration(mContext));
    }
}
