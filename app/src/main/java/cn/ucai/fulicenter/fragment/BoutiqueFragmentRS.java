package cn.ucai.fulicenter.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import cn.ucai.fulicenter.adapter.BoutiqueAdapterRS;
import cn.ucai.fulicenter.bean.BoutiqueBean;
import cn.ucai.fulicenter.task.DownloadBoutiqueTaskRS;
import cn.ucai.fulicenter.utils.NetUtilRS;

/**
 * Created by clawpo on 16/3/19.
 */
public class BoutiqueFragmentRS extends Fragment {
    public static final String TAG = BoutiqueFragmentRS.class.getName();

    MainActivity mContext;

    ArrayList<BoutiqueBean> mBoutiqueList;
    BoutiqueAdapterRS mAdapter;
    /** 下拉刷新控件*/
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mRecyclerView;
    TextView mtvHint;
    LinearLayoutManager mLinearLayoutManager;

    DownloadBoutiqueTaskRS mDownloadBoutiqueTaskRS;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = (MainActivity)getActivity();
        View layout = inflater.inflate(R.layout.fragment_boutique_rs,container,false);
        mBoutiqueList=new ArrayList<BoutiqueBean>();
        initView(layout);
        initData();
        setListener();
        return layout;
    }

    private void setListener() {
        setPullDownRefreshListener();
        setPullUpRefreshListener();
    }
    private void initData(){
        try {
            NetUtilRS.findBoutiqueList(mAdapter,I.ACTION_DOWNLOAD,mSwipeRefreshLayout,mtvHint);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                                try {
                                    NetUtilRS.findBoutiqueList(mAdapter,I.ACTION_PULL_UP,mSwipeRefreshLayout,mtvHint);
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
                        lastItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();
                    }
                }
        );
    }

    /**
     * 下拉刷新事件监听
     */
    private void setPullDownRefreshListener() {
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener(){
                    @Override
                    public void onRefresh() {
                        mtvHint.setVisibility(View.VISIBLE);
                        try {
                            NetUtilRS.findBoutiqueList(mAdapter,I.ACTION_DOWNLOAD,mSwipeRefreshLayout,mtvHint);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }


    private void initView(View layout) {
        mAdapter = new BoutiqueAdapterRS(mContext,mBoutiqueList);
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.sfl_boutique);
        mSwipeRefreshLayout.setColorSchemeColors(
                R.color.google_blue,
                R.color.google_green,
                R.color.google_red,
                R.color.google_yellow
        );
        mtvHint = (TextView) layout.findViewById(R.id.tv_refresh_hint);
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.rv_boutique);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }
}
