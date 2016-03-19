package cn.ucai.fulicenter.task;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.activity.MainActivity;
import cn.ucai.fulicenter.adapter.GoodAdapterRS;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.utils.DialogUtils;
import cn.ucai.fulicenter.utils.NetUtil;

/**
 * Created by clawpo on 16/3/19.
 */
public class DownloadGoodsTaskRS  extends AsyncTask<Integer,Void,ArrayList<NewGoodBean>> {
    public final String TAG = DownloadGoodsTaskRS.class.getName();

    int action;//加载数据的类型：0：重新下载，1：添加新的数据
    int catId;
    /** 0:新品或者精选;1:分类 */
    int goodType;

    MainActivity mContext;
    GoodAdapterRS mAdapter;
    /** 下拉刷新控件*/
    SwipeRefreshLayout mSwipeRefreshLayout;
    TextView mtvHint;

    public DownloadGoodsTaskRS(MainActivity context, GoodAdapterRS adapter,
                               SwipeRefreshLayout swipeRefreshLayout, ArrayList<NewGoodBean> mGoodList,
                               TextView hint, int action, int catId, int goodType) {
        this.mContext = context;
        this.mAdapter = adapter;
        this.mSwipeRefreshLayout = swipeRefreshLayout;
        this.mtvHint = hint;
        this.action = action;
        this.catId = catId;
        this.goodType = goodType;
    }


    @Override
    protected void onPreExecute() {
        mSwipeRefreshLayout.setRefreshing(true);
        switch (action) {
            case I.ACTION_DOWNLOAD:
                DialogUtils.showProgressDialog(mContext, "加载商品", "加载中...");
                break;
            case I.ACTION_PULL_UP:
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
            if (action == I.ACTION_PULL_UP) {
                mAdapter.setFooterText("没有更多数据");
            }
            return;
        }
        mAdapter.setMore(true);
        switch (action){
            case I.ACTION_DOWNLOAD:
            case I.ACTION_PULL_DOWN:
                mAdapter.initItems(goods);
                break;
            case I.ACTION_PULL_UP:
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