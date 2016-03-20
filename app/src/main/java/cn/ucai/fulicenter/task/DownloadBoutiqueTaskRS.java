package cn.ucai.fulicenter.task;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.activity.MainActivity;
import cn.ucai.fulicenter.adapter.BoutiqueAdapterRS;
import cn.ucai.fulicenter.bean.BoutiqueBean;
import cn.ucai.fulicenter.utils.DialogUtils;
import cn.ucai.fulicenter.utils.NetUtil;

/**
 * Created by clawpo on 16/3/19.
 */
public class DownloadBoutiqueTaskRS  extends AsyncTask<Void, Void, ArrayList<BoutiqueBean>> {
    public static final String TAG = DownloadBoutiqueTaskRS.class.getName();
    int action;//加载数据的类型：0：重新下载，1：添加新的数据
    MainActivity mContext;
    ArrayList<BoutiqueBean> boutiqueList;
    BoutiqueAdapterRS mAdapter;
    /** 下拉刷新控件*/
    SwipeRefreshLayout mSwipeRefreshLayout;
    TextView mtvHint;

    public DownloadBoutiqueTaskRS(int action, MainActivity mContext, ArrayList<BoutiqueBean> boutiqueList,
                                  BoutiqueAdapterRS adapter, SwipeRefreshLayout mSwipeRefreshLayout, TextView mtvHint) {
        this.action = action;
        this.mContext = mContext;
        this.boutiqueList = boutiqueList;
        this.mAdapter = adapter;
        this.mSwipeRefreshLayout = mSwipeRefreshLayout;
        this.mtvHint = mtvHint;
    }

    @Override
    protected void onPreExecute() {
        mSwipeRefreshLayout.setRefreshing(true);
        switch (action) {
            case I.ACTION_DOWNLOAD:
//                DialogUtils.showProgressDialog(mContext, "加载商品", "加载中...");
                break;
            case I.ACTION_PULL_UP:
                if (mAdapter.isMore()) {
                    mAdapter.setFooterText("正在加载数据...");
                }
                break;
        }
    }


    @Override
    protected ArrayList<BoutiqueBean> doInBackground(Void... params) {
        boutiqueList = NetUtil.findBoutiqueList();
        Log.e(TAG,"doInBackground,list="+boutiqueList.size());
        return boutiqueList;
    }

    @Override
    protected void onPostExecute(ArrayList<BoutiqueBean> list) {
        Log.e(TAG,"onPostExecute,list="+list.size());
        DialogUtils.closeProgressDialog();
        mSwipeRefreshLayout.setRefreshing(false);
        mtvHint.setVisibility(View.GONE);
        if (list == null || list.size()==0) {
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
                mAdapter.initItems(list);
                break;
            case I.ACTION_PULL_UP:
                if(list!=null){
                    mAdapter.addItems(list);
                    mAdapter.setMore(true);
                } else {
                    mAdapter.setMore(false);
                }
                break;
        }
    }

}
