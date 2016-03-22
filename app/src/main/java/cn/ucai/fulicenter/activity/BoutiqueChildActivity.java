package cn.ucai.fulicenter.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.adapter.GoodAdapterRS;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.utils.NetUtilRS;

/**
 * Created by ucai001 on 2016/3/11.
 */
public class BoutiqueChildActivity extends BaseActivity {
    Context mContext;
    ArrayList<NewGoodBean> mGoodList;

    TextView mtvBoutiqueChildName;

    /** 下拉刷新控件*/
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mRecyclerView;
    TextView mtvHint;
    GridLayoutManager mGridLayoutManager;
    GoodAdapterRS mAdapter;
    
    int mPageId;
    int mCatId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boutique_child);
        mContext=this;
        initView();
        initData();
        setListener();
    }

    private void setListener() {
        setReturn();
        setPullUpRefreshListener();
        setPullDownRefreshListener();
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
                                mPageId=mPageId+I.PAGE_SIZE_DEFAULT;
                                try {
                                    NetUtilRS.findNewandBoutiqueGoods(mAdapter,mCatId,mPageId,
                                            I.PAGE_SIZE_DEFAULT,I.ACTION_PULL_UP,mSwipeRefreshLayout,mtvHint);
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
                            NetUtilRS.findNewandBoutiqueGoods(mAdapter,mCatId,mPageId,I.PAGE_SIZE_DEFAULT,
                                    I.ACTION_PULL_DOWN, mSwipeRefreshLayout,mtvHint);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }


    private void setReturn() {
        findViewById(R.id.ivReturn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    /**
     * 初始化精选二级页面的数据
     */
    private void initData() {
        mCatId=getIntent().getIntExtra(I.Boutique.ID, 0);
        try {
            NetUtilRS.findNewandBoutiqueGoods(mAdapter,mCatId,mPageId,I.PAGE_SIZE_DEFAULT,I.ACTION_DOWNLOAD,
                    mSwipeRefreshLayout,mtvHint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        mSwipeRefreshLayout = getViewById(R.id.srl_boutique_child);
        mSwipeRefreshLayout.setColorSchemeColors(
                R.color.google_blue,
                R.color.google_green,
                R.color.google_red,
                R.color.google_yellow
        );
        mtvHint = getViewById(R.id.tv_refresh_hint);
        mGridLayoutManager = new GridLayoutManager(mContext, I.COLUM_NUM);
        mGridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = getViewById(R.id.rv_boutique_child);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mGoodList=new ArrayList<NewGoodBean>();
        mAdapter = new GoodAdapterRS(mContext,mGoodList,I.SORT_BY_ADDTIME_DESC);
        mRecyclerView.setAdapter(mAdapter);

        mtvBoutiqueChildName=getViewById(R.id.tvBoutiqueChildName);
        String boutiqueChildName=getIntent().getStringExtra(I.Boutique.NAME);
        mtvBoutiqueChildName.setText(boutiqueChildName);

    }

}
