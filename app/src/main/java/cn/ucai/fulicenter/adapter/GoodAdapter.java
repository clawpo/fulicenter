package cn.ucai.fulicenter.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cn.ucai.fulicenter.D;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.utils.ImageLoader;

/**
 * Created by ucai001 on 2016/3/3.
 */
public class GoodAdapter extends BaseAdapter {
    public final String TAG = "GoodAdapter";
    Context context;
    ArrayList<NewGoodBean> goodList;
    ImageLoader imageLoader;
    boolean misMore;
    int sortBy;

    public GoodAdapter(Context context,ArrayList<NewGoodBean> goodList, int sortBy) {
        this.context = context;
        this.goodList = goodList;
        this.imageLoader = ImageLoader.getInstance(context);
        setMore(true);
        this.sortBy = sortBy;
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
        goodList.clear();
        addItems(data);
    }

    private void addItems(ArrayList<NewGoodBean> data) {
        goodList.addAll(data);
        sort(sortBy);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return goodList==null?0:goodList.size();
    }

    @Override
    public NewGoodBean getItem(int position) {
        return goodList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View layout,final ViewGroup parent) {
        ViewHolder holder = null;
        if(layout==null){
            layout= View.inflate(context, R.layout.item_new_good,null);
            holder = new ViewHolder();
            holder.layoutGood = (LinearLayout) layout.findViewById(R.id.layoutGood);
            holder.ivGoodsThumb=(ImageView) layout.findViewById(R.id.ivGoodThumb);
            holder.tvGoodsName=(TextView) layout.findViewById(R.id.tvGoodName);
            holder.tvCurrencyPrice=(TextView) layout.findViewById(R.id.tvCurrencyPrice);
            layout.setTag(holder);
        }else{
            holder = (ViewHolder) layout.getTag();
        }
        final NewGoodBean good = getItem(position);
        holder.tvCurrencyPrice.setText(good.getCurrencyPrice());
        holder.tvGoodsName.setText(good.getGoodsName());
        String goodsThumb = good.getGoodsThumb();
        holder.ivGoodsThumb.setTag(goodsThumb);
        String savePath = goodsThumb.substring(goodsThumb.indexOf("/images")+1);
        String uri = I.SERVER_ROOT
                +"?"+ I.KEY_REQUEST+"="+ I.REQUEST_DOWNLOAD_NEW_GOOD
                +"&"+ I.FILE_NAME+"="+goodsThumb;
        Bitmap thumb = imageLoader.displayImage(uri, savePath, 150, 250, new ImageLoader.OnImageLoadListener() {
            @Override
            public void onSuccess(String path, Bitmap bitmap) {
                ImageView iv = (ImageView) parent.getTag();
                if(iv!=null){
                    iv.setImageBitmap(bitmap);
                }
            }

            @Override
            public void error(String errorMsg) {

            }
        });
        if(thumb!=null){
            holder.ivGoodsThumb.setImageBitmap(thumb);
        }else{
            holder.ivGoodsThumb.setImageResource(R.drawable.nopic);
        }

        holder.layoutGood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"new good item on click,good="+good.toString());
//                context.startActivity(new Intent(context,GoodDetailsActivity.class)
//                        .putExtra(D.NewGood.KEY_GOODS_ID,good.getGoodsId()));
            }
        });
        return layout;
    }

    class ViewHolder{
        LinearLayout layoutGood;
        ImageView ivGoodsThumb;
        TextView tvGoodsName;
        TextView tvCurrencyPrice;
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
}
