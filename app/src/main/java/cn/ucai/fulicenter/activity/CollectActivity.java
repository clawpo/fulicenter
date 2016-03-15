package cn.ucai.fulicenter.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.I.ActionType;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.CollectBean;
import cn.ucai.fulicenter.bean.MessageBean;
import cn.ucai.fulicenter.task.DownloadCollectCountTask;
import cn.ucai.fulicenter.utils.ImageLoader;
import cn.ucai.fulicenter.utils.ImageLoader.OnImageLoadListener;
import cn.ucai.fulicenter.utils.NetUtil;
import cn.ucai.fulicenter.utils.PullRefreshView;
import cn.ucai.fulicenter.utils.PullRefreshView.LoadStatus;
import cn.ucai.fulicenter.utils.PullRefreshView.OnRefreshListener;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by ucai001 on 2016/3/15.
 */
public class CollectActivity extends BaseActivity {
    public static final String TAG = CollectActivity.class.getName();
    Context mContext;
    PullRefreshView<GridView> mprvCollect;
    GridView mgvCollect;
    ArrayList<CollectBean> mCollectList;
    CollectAdapter mAdapter;

    DownloadCollectTask mDownloadCollectTask;
    int mPageId;
    final int PAGE_SIZE=10;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_collect);
        initView();
        initData();
        setListener();
    }
    
    private void setListener() {
        setPullDownRefreshListener();
        setPullUpRefreshListener();
        setReturnClickListener();
    }

    private void setPullUpRefreshListener() {
        mgvCollect.setOnScrollListener(new OnScrollListener() {
            int lastPosition;
            
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(lastPosition==mAdapter.getCount() && scrollState==OnScrollListener.SCROLL_STATE_IDLE
                    &&mAdapter.isMore){
                    mPageId++;
                    mDownloadCollectTask=new DownloadCollectTask(mContext, mCollectList, ActionType.ACTION_SCROLL);
                    mDownloadCollectTask.execute(mPageId,PAGE_SIZE);
                }
            }
            
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
                lastPosition=firstVisibleItem+totalItemCount;
            }
        });
    }

    private void setPullDownRefreshListener() {
        mprvCollect.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void loadData() {
                mPageId=0;
                mDownloadCollectTask=new DownloadCollectTask(mContext, mCollectList, ActionType.ACTION_PULL_DOWN);
                mDownloadCollectTask.execute(mPageId,PAGE_SIZE);
            }

            @Override
            public LoadStatus getLoadStatus() {
                return mDownloadCollectTask.getLoadStatus();
            }
        }, mgvCollect);
    }

    private void setReturnClickListener() {
        findViewById(R.id.ivReturn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initData() {
        mPageId=0;
        mDownloadCollectTask=new DownloadCollectTask(mContext, mCollectList,ActionType.ACTION_DOWNLOAD);
        mDownloadCollectTask.execute(mPageId,PAGE_SIZE);
    }

    private void initView() {
        mprvCollect=(PullRefreshView<GridView>) getViewById(R.id.prvCollect);
        mgvCollect=(GridView) getViewById(R.id.gvCollect);
        mCollectList=new ArrayList<CollectBean>();
        mAdapter=new CollectAdapter(mContext, mCollectList);
        mgvCollect.setAdapter(mAdapter);
    }

    /**
     * 收藏适配器
     * @author yao
     *
     */
    class CollectAdapter extends BaseAdapter {
        Context context;
        ArrayList<CollectBean> collectList;
        ImageLoader imageLoader;
        boolean isMore;
        
        public CollectAdapter(Context context,
                ArrayList<CollectBean> collectList) {
            super();
            this.context = context;
            this.collectList = collectList;
            imageLoader=ImageLoader.getInstance(context);
        }

        public void addItems(ArrayList<CollectBean> collectList){
            this.collectList.addAll(collectList);
            notifyDataSetChanged();
        }
        
        public void initItems(ArrayList<CollectBean> collectList){
            this.collectList.clear();
            addItems(collectList);
        }
        
        public void removeItem(int position){
            collectList.remove(position);
            notifyDataSetChanged();
        }
        
        public void setMore(boolean isMore){
            this.isMore=isMore;
        }
        
        public boolean isMore(boolean isMore){
            return this.isMore;
        }
        
        @Override
        public int getCount() {
            return collectList==null?0:collectList.size();
        }

        @Override
        public CollectBean getItem(int position) {
            return collectList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(final int position, View layout, final ViewGroup parent) {
            ViewHoloder holder=null;
            if(layout==null){
                layout=View.inflate(context, R.layout.item_collect, null);
                holder=new ViewHoloder();
                holder.layoutItem=(RelativeLayout) layout.findViewById(R.id.layout_collect_item);
                holder.ivDelGoods=(ImageView) layout.findViewById(R.id.ivDelGoods);
                holder.ivGoodsThumb=(ImageView) layout.findViewById(R.id.ivGoodsThumb);
                holder.tvGoodsName=(TextView) layout.findViewById(R.id.tvGoodsName);
                
                layout.setTag(holder);
            }else{
                holder=(ViewHoloder) layout.getTag();
            }
            
            final CollectBean collect = getItem(position);
            holder.tvGoodsName.setText(collect.getGoodsName());
            String url=I.DOWNLOAD_ALBUM_IMG_URL+collect.getGoodsThumb();
            holder.ivGoodsThumb.setTag(url);
            Bitmap bitmap = imageLoader.displayImage(url, collect.getGoodsThumb(), 100, 100, new OnImageLoadListener() {
                @Override
                public void onSuccess(String path, Bitmap bitmap) {
                    ImageView ivThumb=(ImageView) parent.findViewWithTag(path);
                    if(ivThumb!=null){
                        ivThumb.setImageBitmap(bitmap);
                    }
                }
                
                @Override
                public void error(String errorMsg) {
                    // TODO Auto-generated method stub
                    
                }
            });
            if(bitmap==null){
                holder.ivGoodsThumb.setImageResource(R.drawable.nopic);
            }else{
                holder.ivGoodsThumb.setImageBitmap(bitmap);
            }
            
            //删除列表项
            holder.ivDelGoods.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userName=FuLiCenterApplication.getInstance().getUserBean().getUserName();
                    new DelCollectTask(context, userName, collect.getGoodsId(), position).execute();
                }
            });
            
            holder.layoutItem.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(context, GoodDetailsActivity.class);
                    intent.putExtra(I.Collect.GOODS_ID, getItem(position).getGoodsId());
                    context.startActivity(intent);
                }
            });
            return layout;
        }
        
        class ViewHoloder{
            RelativeLayout layoutItem;
            ImageView ivGoodsThumb;
            TextView tvGoodsName;
            ImageView ivDelGoods;
        }
    }
    
    class DownloadCollectTask extends AsyncTask<Integer, Void, Boolean>{
        Context context;
        ArrayList<CollectBean> collectList;
        ProgressDialog dialog;
        ActionType actionType;
        LoadStatus loadStatus;
        
        public DownloadCollectTask(Context context,
                ArrayList<CollectBean> collectList,ActionType actionType) {
            super();
            this.context = context;
            this.collectList = collectList;
            this.actionType=actionType;
        }

        public LoadStatus getLoadStatus(){
            return this.loadStatus;
        }
        
        @Override
        protected void onPreExecute() {
            if(actionType==ActionType.ACTION_DOWNLOAD){
                dialog=new ProgressDialog(context);
                dialog.setTitle("下载收藏的商品");
                dialog.setMessage("加载数据中...");
                dialog.show();
            }
        }
        
        @Override
        protected Boolean doInBackground(Integer... params) {
            int pageId=params[0];
            int pageSize=params[1];
            String userName= FuLiCenterApplication.getInstance().getUserBean().getUserName();
            collectList=NetUtil.findCollects(userName,pageId,pageSize);
            if(collectList==null){
                loadStatus=LoadStatus.SUCCESS;
            }else{
                loadStatus=loadStatus.FAILURE;
            }
            return collectList!=null;
        }
        
        @Override
        protected void onPostExecute(Boolean result) {
            if(!result){
                if(actionType==ActionType.ACTION_SCROLL){
                    mAdapter.setMore(false);
                }
                Utils.showToast(context, "加载收藏商品失败", Toast.LENGTH_SHORT);
                return ;
            }
            switch (actionType) {
            case ACTION_DOWNLOAD:
                dialog.dismiss();
                mAdapter.addItems(collectList);
                break;
            case ACTION_PULL_DOWN:
                mAdapter.initItems(collectList);
                break;
            case ACTION_SCROLL:
                mAdapter.addItems(collectList);
                break;
            }
        }
    }
    
    /**
     * 删除指定的收藏商品
     * @author yao
     *
     */
    class DelCollectTask extends AsyncTask<Void, Void, MessageBean>{
        Context context;
        String userName;
        int goodsId;
        int position;
        
        public DelCollectTask(Context context, String userName, int goodsId,
                int position) {
            super();
            this.context = context;
            this.userName = userName;
            this.goodsId = goodsId;
            this.position = position;
        }

        @Override
        protected MessageBean doInBackground(Void... params) {
            MessageBean msg=NetUtil.deleteCollect(userName, goodsId);
            return msg;
        }
        
        @Override
        protected void onPostExecute(MessageBean result) {
            Utils.showToast(context, result.getMsg(), Toast.LENGTH_SHORT);
            mAdapter.removeItem(position);
            if(result.isSuccess()){
                new DownloadCollectCountTask(mContext,userName).execute();
            }
        }
    }
}
