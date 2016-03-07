package cn.ucai.fulicenter.fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.I.ActionType;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.MainActivity;
import cn.ucai.fulicenter.adapter.GoodAdapter;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.task.DownloadGoodsTask;
import cn.ucai.fulicenter.utils.PullRefreshView;
import cn.ucai.fulicenter.utils.PullRefreshView.OnRefreshListener;

/**
 * Created by ucai001 on 2016/3/3.
 */
public class NewGoodFragment extends Fragment {
    public static final String TAG = NewGoodFragment.class.getName();
    MainActivity mContext;

    ArrayList<NewGoodBean> mGoodList;
    GoodAdapter mAdaper;
    PullRefreshView<GridView> mprfvNewGood;
    GridView mgvNewGood;

    /**分页下载商品的页码*/
    int mPageId=0;
    /** 每页下载商品的数量*/
    final int PAGE_SIZE=10;

    DownloadGoodsTask mDownloadGoodsTask;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG,"onCreateView...");
        mContext = (MainActivity) getActivity();
        View layout = View.inflate(mContext, R.layout.fragment_new_good,null);
        initView(layout);
        setListener(layout);
        mDownloadGoodsTask = new DownloadGoodsTask(mContext,mAdaper,mGoodList,
                ActionType.ACTION_DOWNLOAD,I.CAT_ID,I.NEW_GOOD);
        mDownloadGoodsTask.execute(mPageId,PAGE_SIZE);
        return layout;
    }

    private void setListener(View layout) {
        setPullDownRefreshListener();
        setPullUpRefreshListener();
    }

    /**
     * 设置上拉刷新事件监听
     */
    private void setPullUpRefreshListener() {
        mgvNewGood.setOnScrollListener(new OnScrollListener() {
            int lastPosition;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState==OnScrollListener.SCROLL_STATE_IDLE
                        && lastPosition==mAdaper.getCount()-1 && mAdaper.isMore()){
                    mPageId=mPageId+PAGE_SIZE;
                    mDownloadGoodsTask=new DownloadGoodsTask(mContext,mAdaper,
                            mGoodList, ActionType.ACTION_SCROLL,I.CAT_ID,I.NEW_GOOD);
                    mDownloadGoodsTask.execute(mPageId,PAGE_SIZE);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, 
			int visibleItemCount, int totalItemCount) {
                lastPosition=firstVisibleItem+visibleItemCount-1;
            }
        });
    }

    /**
     * 设置下拉刷新事件监听
     */
    private void setPullDownRefreshListener() {
        mprfvNewGood.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void loadData() {
                mPageId=0;
                mDownloadGoodsTask=new DownloadGoodsTask(mContext,mAdaper,
                        mGoodList,ActionType.ACTION_PULL_DOWN,I.CAT_ID,I.NEW_GOOD);
                mDownloadGoodsTask.execute(mPageId,PAGE_SIZE);
            }

            @Override
            public PullRefreshView.LoadStatus getLoadStatus() {
                return mDownloadGoodsTask.getLoadStatus();
            }
        },mgvNewGood);
    }

    private void initView(View layout) {
        mprfvNewGood = (PullRefreshView<GridView>) layout.findViewById(R.id.prfvNewGood);
        mgvNewGood = (GridView) layout.findViewById(R.id.gvNewGood);
        mGoodList = new ArrayList<NewGoodBean>();
        mAdaper = new GoodAdapter(mContext,mGoodList, I.SORT_BY_ADDTIME_DESC);
        mgvNewGood.setAdapter(mAdaper);
    }
}
