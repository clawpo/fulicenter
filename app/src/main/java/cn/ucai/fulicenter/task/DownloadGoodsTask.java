package cn.ucai.fulicenter.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import cn.ucai.fulicenter.D;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.I.ActionType;
import cn.ucai.fulicenter.adapter.GoodAdapter;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.utils.NetUtil;
import cn.ucai.fulicenter.utils.PullRefreshView.LoadStatus;

import cn.ucai.fulicenter.utils.PullRefreshView;

/**
 * Created by ucai001 on 2016/3/3.
 */
public class DownloadGoodsTask extends AsyncTask<Integer,Void,ArrayList<NewGoodBean>> {
    public final String TAG = "DownloadGoodsTask";
    ProgressDialog mdialog;
    Context mContext;
    LoadStatus loadStatus;
    ArrayList<NewGoodBean> mGoodList;
    GoodAdapter mAdapter;
    ActionType actionType;
    int catId;
    /** 0:新品或者精选;1:分类 */
    int goodType;

    public DownloadGoodsTask(Context context, GoodAdapter mAdapter,
             ArrayList<NewGoodBean> mGoodList, ActionType actionType, int catId, int goodType) {
        this.mContext = context;
        this.mAdapter = mAdapter;
        this.mGoodList = mGoodList;
        loadStatus = LoadStatus.LOADING;
        this.actionType = actionType;
        this.catId = catId;
        this.goodType = goodType;
    }

    @Override
    protected void onPreExecute() {
        mdialog = null;
        if(actionType == ActionType.ACTION_DOWNLOAD){
            mdialog = new ProgressDialog(mContext);
            mdialog.setTitle(D.NewGood.HINT_DOWNLOAD_TITLE);
            mdialog.setMessage(D.NewGood.HINT_DOWNLOADING);
            mdialog.show();
        }
    }

    @Override
    protected ArrayList<NewGoodBean> doInBackground(Integer... params) {
        int pageId=params[0];
        int pageSize=params[1];
        Log.i(TAG,"DownloadGoodsTask...");
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
            this.loadStatus = LoadStatus.SUCCESS;
        }catch (Exception e){
            this.loadStatus = LoadStatus.FAILURE;
        }
        return goodList;
    }

    @Override
    protected void onPostExecute(ArrayList<NewGoodBean> goods) {
        if(loadStatus == LoadStatus.FAILURE){
            if(mdialog!=null){
                mdialog.dismiss();
            }
            Toast.makeText(mContext,D.NewGood.HINT_DOWNLOAD_FAILURE,Toast.LENGTH_SHORT).show();
            return;
        }
        switch (actionType){
            case  ACTION_DOWNLOAD:
                mdialog.dismiss();
                mAdapter.initItems(goods);
                break;
            case ACTION_PULL_DOWN:
                mAdapter.initItems(goods);
                break;
            case ACTION_SCROLL:
                if(goods!=null){
                    mAdapter.initItems(goods);
                    mAdapter.setMore(true);
                } else {
                  mAdapter.setMore(false);
                }
                break;
        }
    }
}
