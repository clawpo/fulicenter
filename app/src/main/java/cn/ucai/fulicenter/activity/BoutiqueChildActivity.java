package cn.ucai.fulicenter.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.adapter.GoodAdapter;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.task.DownloadGoodsTask;
import cn.ucai.fulicenter.utils.PullRefreshView;
import cn.ucai.fulicenter.utils.PullRefreshView.OnRefreshListener;

/**
 * Created by ucai001 on 2016/3/11.
 */
public class BoutiqueChildActivity extends BaseActivity {
    Context mContext;
    ArrayList<NewGoodBean> mGoodList;
    
    PullRefreshView<GridView> mprvBoutiqueChild;
    GridView mgvBoutiqueChild;
    TextView mtvBoutiqueChildName;
    
    GoodAdapter mAdapter;
    DownloadGoodsTask mDownloadGoodsTask;
    
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

    private void setPullDownRefreshListener() {
        mprvBoutiqueChild.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void loadData() {
                mDownloadGoodsTask = new DownloadGoodsTask(mContext,mAdapter,
                        mGoodList, I.ActionType.ACTION_PULL_DOWN,mCatId,I.NEW_GOOD);
                mDownloadGoodsTask.execute(mPageId,I.PAGE_ID_DEFAULT);
            }

            @Override
            public PullRefreshView.LoadStatus getLoadStatus() {
                return mDownloadGoodsTask.getLoadStatus();
            }
        },mgvBoutiqueChild);
    }

    private void setPullUpRefreshListener() {
        mgvBoutiqueChild.setOnScrollListener(new AbsListView.OnScrollListener() {
            int lastPosition;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState== AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                        && lastPosition==mAdapter.getCount()
                        && mAdapter.isMore()){
                    mPageId=mPageId+I.PAGE_SIZE_DEFAULT;
                    mDownloadGoodsTask = new DownloadGoodsTask(mContext,mAdapter,
                            mGoodList, I.ActionType.ACTION_SCROLL,mCatId,I.NEW_GOOD);
                    mDownloadGoodsTask.execute(mPageId,I.PAGE_SIZE_DEFAULT);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastPosition=firstVisibleItem+totalItemCount-1;
            }
        });
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
        mDownloadGoodsTask=new DownloadGoodsTask(mContext, mAdapter, mGoodList, 
            I.ActionType.ACTION_DOWNLOAD,mCatId,I.NEW_GOOD);
        mDownloadGoodsTask.execute(mPageId,I.PAGE_SIZE_DEFAULT);
    }

    private void initView() {
        mtvBoutiqueChildName=getViewById(R.id.tvBoutiqueChildName);
        String boutiqueChildName=getIntent().getStringExtra(I.Boutique.NAME);
        mtvBoutiqueChildName.setText(boutiqueChildName);

        mprvBoutiqueChild=getViewById(R.id.prfvBoutiqueChild);
        mgvBoutiqueChild=getViewById(R.id.gvBoutiqueChild);
        mGoodList=new ArrayList<NewGoodBean>();
        mAdapter=new GoodAdapter(mContext, mGoodList,I.SORT_BY_ADDTIME_DESC);
        mgvBoutiqueChild.setAdapter(mAdapter);
    }

}
