package cn.ucai.fulicenter.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cn.ucai.fulicenter.D;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.GoodDetailsActivity;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.utils.ImageLoader;

/**
 * Created by clawpo on 16/3/18.
 */
public class GoodAdapterRS extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = GoodAdapterRS.class.getName();

    Context mContext;
    ArrayList<NewGoodBean> goodList;
    ImageLoader imageLoader;
    /** 还有更多的数据可供下载*/
    boolean misMore;
    int sortBy;

    
    /**RecyclerView*/
    ViewGroup parent;
    String footerText;
    GoodViewHolder goodHolder;
    FooterViewHolder footerHolder;

    public GoodAdapterRS(Context mContext, ArrayList<NewGoodBean> goodList, int sortBy) {
        this.mContext = mContext;
        this.goodList = goodList;
        this.imageLoader = ImageLoader.getInstance(mContext);
        setMore(true);
        this.sortBy = sortBy;
    }

    /** 设置底部用于提示上拉刷新的文本*/
    public void setFooterText(String text) {
        footerText=text;
        notifyDataSetChanged();
    }

    public boolean isMore() {
        return misMore;
    }

    public void setMore(boolean isMore) {
        this.misMore = isMore;
    }

    public void setSortBy(int sortBy) {
        this.sortBy = sortBy;
        sort(sortBy);
        notifyDataSetChanged();
    }

    public void initItems(ArrayList<NewGoodBean> data){
        goodList = data;
        sort(sortBy);
        notifyDataSetChanged();
    }
    
    public void addItems(ArrayList<NewGoodBean> data) {
        goodList.addAll(data);
        sort(sortBy);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return goodList==null?0:goodList.size();
    }
	
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.parent = parent;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        RecyclerView.ViewHolder holder = null;
        switch (viewType){
            case I.TYPE_ITEM:
                holder = new GoodViewHolder(inflater.inflate(R.layout.item_new_good, parent,false));
                break;
            case I.TYPE_FOOTER:
                holder = new FooterViewHolder(inflater.inflate(R.layout.item_footer, parent,false));
                break;
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof FooterViewHolder){
            footerHolder  = (FooterViewHolder) holder;
            footerHolder.tvFooter.setText(footerText);
            footerHolder.tvFooter.setVisibility(View.VISIBLE);
            return;
        }
        if(position == goodList.size()){
            return;
        }
        if(holder instanceof GoodViewHolder) {
            goodHolder = (GoodViewHolder) holder;

            final NewGoodBean good = goodList.get(position);
            goodHolder.tvCurrencyPrice.setText(good.getCurrencyPrice());
            goodHolder.tvGoodsName.setText(good.getGoodsName());
            String goodsThumb = good.getGoodsThumb();
            goodHolder.ivGoodsThumb.setTag(goodsThumb);
            String savePath = goodsThumb.substring(goodsThumb.indexOf("/images") + 1);
            String url = I.SERVER_ROOT
                    + "?" + I.KEY_REQUEST + "=" + I.REQUEST_DOWNLOAD_NEW_GOOD
                    + "&" + I.FILE_NAME + "=" + goodsThumb;
            Bitmap thumb = imageLoader.displayImage(url, savePath, 150, 250, new ImageLoader.OnImageLoadListener() {
                @Override
                public void onSuccess(String path, Bitmap bitmap) {
                    ImageView iv = (ImageView) parent.findViewWithTag(path);
                    if (iv != null) {
                        iv.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void error(String errorMsg) {
                }
            });
            if (thumb != null) {
                goodHolder.ivGoodsThumb.setImageBitmap(thumb);
            } else {
                goodHolder.ivGoodsThumb.setImageResource(R.drawable.nopic);
            }

            goodHolder.layoutGood.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(mContext, GoodDetailsActivity.class)
                            .putExtra(D.NewGood.KEY_GOODS_ID, good.getGoodsId()));
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == getItemCount()){
            return I.TYPE_FOOTER;
        }else{
            return I.TYPE_ITEM;
        }
    }

    private void sort(final int sortBy){
        Collections.sort(goodList, new Comparator<NewGoodBean>() {
            @Override
            public int compare(NewGoodBean g1, NewGoodBean g2) {
                int result =0;
                switch (sortBy){
                    case I.SORT_BY_ADDTIME_ASC:
                        result = (int) (g1.getAddTime()-g2.getAddTime());
                        break;
                    case I.SORT_BY_ADDTIME_DESC:
                        result = (int) (g2.getAddTime()-g1.getAddTime());
                        break;
                    case I.SORT_BY_PRICE_ASC:
                    {
                        int p1 = convertPrice(g1.getCurrencyPrice());
                        int p2 = convertPrice(g2.getCurrencyPrice());
                        result = p1-p2;
                    }
                    break;
                    case I.SORT_BY_PRICE_DESC:
                    {
                        int p1 = convertPrice(g1.getCurrencyPrice());
                        int p2 = convertPrice(g2.getCurrencyPrice());
                        result = p2-p1;
                    }
                    break;
                }
                return result;
            }
            private int convertPrice(String price){
                price = price.substring(price.indexOf("￥")+1);
                int p1 = Integer.parseInt(price);
                return p1;
            }
        });
    }

    class GoodViewHolder extends RecyclerView.ViewHolder{
        LinearLayout layoutGood;
        ImageView ivGoodsThumb;
        TextView tvGoodsName;
        TextView tvCurrencyPrice;

        public GoodViewHolder(View itemView) {
            super(itemView);
            layoutGood = (LinearLayout) itemView.findViewById(R.id.layoutGood);
            ivGoodsThumb=(ImageView) itemView.findViewById(R.id.ivGoodThumb);
            tvGoodsName=(TextView) itemView.findViewById(R.id.tvGoodName);
            tvCurrencyPrice=(TextView) itemView.findViewById(R.id.tvCurrencyPrice);
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder{
        TextView tvFooter;
        public FooterViewHolder(View itemView) {
            super(itemView);
            tvFooter = (TextView) itemView.findViewById(R.id.tvFooter);
        }
    }
}

