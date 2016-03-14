package cn.ucai.fulicenter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import cn.ucai.fulicenter.R;

/**
 * Created by ucai001 on 2016/3/14.
 */
public class OrderAdapter extends BaseAdapter {

    Context mContext;
    int[] patchList;

    public OrderAdapter(Context mContext, int[] patchList) {
        this.mContext = mContext;
        this.patchList = patchList;
    }

    @Override
    public int getCount() {
        return patchList.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View layout,final ViewGroup parent) {
        HolderView holder=null;
        if(layout==null){
            holder = new HolderView();
            layout= LayoutInflater.from(mContext).inflate(R.layout.item_order,null);
            holder.ivOrder= (ImageView) layout.findViewById(R.id.iv_order_pic);
            layout.setTag(holder);
        }else{
            holder= (HolderView) layout.getTag();
        }
        holder.ivOrder.setImageResource(patchList[position]);
        return layout;
    }

    class HolderView{
        private ImageView ivOrder;
    }
}
