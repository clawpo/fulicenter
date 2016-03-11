package cn.ucai.fulicenter.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cn.ucai.fulicenter.D;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.I.ActionType;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.BoutiqueChildActivity;
import cn.ucai.fulicenter.bean.BoutiqueBean;
import cn.ucai.fulicenter.utils.ImageLoader;
import cn.ucai.fulicenter.utils.ImageLoader.OnImageLoadListener;
import cn.ucai.fulicenter.utils.NetUtil;
import cn.ucai.fulicenter.utils.PullRefreshView;
import cn.ucai.fulicenter.utils.PullRefreshView.LoadStatus;
import cn.ucai.fulicenter.utils.PullRefreshView.OnRefreshListener;
import cn.ucai.fulicenter.utils.Utils;
/**
 * Created by ucai001 on 2016/3/10.
 */
public class BoutiqueFragment extends Fragment {
    Context mContext;
    PullRefreshView<ListView> mprvBoutique;
    ListView mlvBoutique;
    ArrayList<BoutiqueBean> mBoutiqueList;
    BoutiqueAdapter mAdapter;
    DownloadBoutiqueTask mDownloadBoutiqueTask;
    
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext=getActivity();
        View layout = View.inflate(getActivity(), R.layout.fragment_boutique,null);
        initView(layout);
        initData();
        setListener();
        return layout;
    }
    
    private void setListener() {
        mprvBoutique.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void loadData() {
                mDownloadBoutiqueTask=new DownloadBoutiqueTask(mContext, mBoutiqueList, mAdapter,
                    ActionType.ACTION_PULL_DOWN);
                mDownloadBoutiqueTask.execute();
            }

            @Override
            public LoadStatus getLoadStatus() {
                return mDownloadBoutiqueTask.getLoadStatus();
            }
        }, mlvBoutique);
    }

    private void initData() {
        new DownloadBoutiqueTask(mContext, mBoutiqueList, mAdapter,ActionType.ACTION_DOWNLOAD).execute();
    }

    private void initView(View layout) {
        mprvBoutique=(PullRefreshView<ListView>) layout.findViewById(R.id.prfvBoutique);
        mlvBoutique=(ListView) layout.findViewById(R.id.lvBoutique);
        mBoutiqueList=new ArrayList<BoutiqueBean>();
        mAdapter=new BoutiqueAdapter(mContext, mBoutiqueList);
        mlvBoutique.setAdapter(mAdapter);
    }

    /**
     * 精选首页是适配器
     * @author yao
     *
     */
    class BoutiqueAdapter extends BaseAdapter {
        Context context;
        ArrayList<BoutiqueBean> boutiqueList;
        ImageLoader imageLoader;
        
        public void clearItems(){
            boutiqueList.clear();
            notifyDataSetChanged();
        }
        
        public void addItems(ArrayList<BoutiqueBean> list){
            boutiqueList.addAll(list);
            notifyDataSetChanged();
        }
        
        public BoutiqueAdapter(Context context,
                ArrayList<BoutiqueBean> boutiqueList) {
            super();
            this.context = context;
            this.boutiqueList = boutiqueList;
            imageLoader= ImageLoader.getInstance(context);
        }

        @Override
        public int getCount() {
            return boutiqueList==null?0:boutiqueList.size();
        }

        @Override
        public BoutiqueBean getItem(int position) {
            return boutiqueList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View layout, final ViewGroup parent) {
            ViewHolder holder=null;
            if(layout==null){
                layout=View.inflate(context, R.layout.item_boutique, null);
                holder=new ViewHolder();
                holder.layoutItem=(RelativeLayout) layout.findViewById(R.id.layout_boutique_item);
                holder.iv=(ImageView) layout.findViewById(R.id.ivBoutiqueImg);
                holder.tvDescription=(TextView) layout.findViewById(R.id.tvBoutiqueDescription);
                holder.tvName=(TextView) layout.findViewById(R.id.tvBoutiqueName);
                holder.tvTitle=(TextView) layout.findViewById(R.id.tvBoutiqueTitle);
                layout.setTag(holder);
            }else{
                holder=(ViewHolder) layout.getTag();
            }
            final BoutiqueBean boutique = getItem(position);
            holder.tvDescription.setText(boutique.getDescription());
            holder.tvName.setText(boutique.getName());
            holder.tvTitle.setText(boutique.getTitle());
            
            String url = I.DOWNLOAD_BOUTIQUE_IMG_URL+boutique.getImageurl();
            String imgName="images/"+boutique.getImageurl();
            Bitmap bitmap = imageLoader.displayImage(url, imgName, D.Boutique.IMG_WIDTH, D.Boutique.IMG_HEIGHT, new OnImageLoadListener() {
                @Override
                public void onSuccess(String path, Bitmap bitmap) {
                    ImageView iv=(ImageView) parent.findViewWithTag(path);
                    if(iv!=null){
                        iv.setImageBitmap(bitmap);
                    }
                }
                
                @Override
                public void error(String errorMsg) {
                    // TODO Auto-generated method stub
                }
            });
            if(bitmap==null){
                holder.iv.setImageResource(R.drawable.nopic);
            }else{
                holder.iv.setImageBitmap(bitmap);
            }
            
            holder.layoutItem.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("main",boutique.toString());
                    Intent intent=new Intent(context, BoutiqueChildActivity.class);
                    intent.putExtra(I.Boutique.ID, boutique.getId());
                    intent.putExtra(I.Boutique.NAME, boutique.getName());
                    context.startActivity(intent);
                }
            });
            return layout;
        }

        class ViewHolder{
            RelativeLayout layoutItem;
            ImageView iv;
            TextView tvTitle;
            TextView tvName;
            TextView tvDescription;

        }
    }
    /**
     * 下载精选首页数据
     * @author yao
     *
     */
    class DownloadBoutiqueTask extends AsyncTask<Void, Void, Boolean>{
        Context context;
        ArrayList<BoutiqueBean> boutiqueList;
        BoutiqueAdapter adapter;
        ProgressDialog dialog;
        LoadStatus loadStatus;
        ActionType actionType;

        public DownloadBoutiqueTask(Context context,
                                    ArrayList<BoutiqueBean> boutiqueList, BoutiqueAdapter adapter,
                                    ActionType actionType) {
            super();
            this.context = context;
            this.boutiqueList = boutiqueList;
            this.adapter = adapter;
            this.actionType=actionType;
        }

        @Override
        protected void onPreExecute() {
            if(actionType==ActionType.ACTION_DOWNLOAD){
                this.loadStatus=LoadStatus.LOADING;
                dialog=new ProgressDialog(context);
                dialog.setTitle("下载精选首页数据");
                dialog.setMessage("加载中...");
                dialog.show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boutiqueList=NetUtil.findBoutiqueList();
            return boutiqueList!=null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(boutiqueList==null){
                loadStatus=LoadStatus.FAILURE;
                Utils.showToast(context, "数据加载失败", Toast.LENGTH_LONG);
                return ;
            }
            loadStatus=LoadStatus.SUCCESS;
            switch (actionType) {
                case ACTION_DOWNLOAD:
                    dialog.dismiss();
                    adapter.addItems(boutiqueList);
                case ACTION_PULL_DOWN:
                    adapter.clearItems();
                    adapter.addItems(boutiqueList);
                    break;
            }
        }

        public LoadStatus getLoadStatus(){
            return loadStatus;
        }
    }

}
