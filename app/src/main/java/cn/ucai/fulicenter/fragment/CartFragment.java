package cn.ucai.fulicenter.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.utils.BitmapUtils;
import cn.ucai.fulicenter.utils.ImageLoader;
import cn.ucai.fulicenter.utils.NetUtil;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by ucai001 on 2016/3/9.
 */
public class CartFragment extends Fragment {
    public static final String TAG = CartFragment.class.getName();
    ListView mlvCart;
    CartAdapter mAdapter;
    
    TextView mtvSumPrice;
    TextView mtvSavePrice;
    TextView mtvNothing;
    
    CartChangedReceiver mCartChangedReceiver;
    
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout=View.inflate(getActivity(), R.layout.fragment_cart, null);
        initView(layout);
        registerCartChangedReceiver();
        return layout;
    }

    private void initView(View layout) {
        mtvSumPrice=(TextView) layout.findViewById(R.id.tvSumPrice);
        mtvSavePrice=(TextView) layout.findViewById(R.id.tvSavePrice);
        mtvNothing= (TextView) layout.findViewById(R.id.tv_nothing);
        
        mlvCart=(ListView) layout.findViewById(R.id.lvCart);
        mlvCart.setEmptyView(mtvNothing);
        mAdapter=new CartAdapter(getActivity(),mtvSumPrice,mtvSavePrice);
        mlvCart.setAdapter(mAdapter);
    }

    /**
     * 购物车适配器
     */
    class CartAdapter extends BaseAdapter {
        Context context;
        ArrayList<CartBean> cartList;
        ImageLoader imageLoader;
        TextView tvSumPrice,tvSavePrice;
        boolean isUpdate;

        public CartAdapter(Context context, TextView tvSumPrice, TextView tvSavePrice) {
            super();
            this.context = context;
            this.tvSumPrice = tvSumPrice;
            this.tvSavePrice=tvSavePrice;

            cartList=FuLiCenterApplication.getInstance().getCartList();
            imageLoader=ImageLoader.getInstance(context);
            isUpdate=true;
        }

        @Override
        public void notifyDataSetChanged() {
            sumPrice(tvSumPrice,tvSavePrice);
            super.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return cartList==null?0:cartList.size();
        }

        @Override
        public CartBean getItem(int position) {
            return cartList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View layout, final ViewGroup parent) {
            ViewHolder holder=null;
            if(layout==null){
                layout=View.inflate(context, R.layout.item_cart, null);
                holder=new ViewHolder();
                holder.tvCartCount=(TextView) layout.findViewById(R.id.tvCartCount);
                holder.tvGoodsName=(TextView) layout.findViewById(R.id.tvGoodsName);
                holder.ivAddCart=(ImageView) layout.findViewById(R.id.ivAddCart);
                holder.ivReduceCart=(ImageView) layout.findViewById(R.id.ivReduceCart);
                holder.ivGoodsThumb=(ImageView) layout.findViewById(R.id.ivGoodsThumb);
                holder.tvGoodsPrice=(TextView) layout.findViewById(R.id.tvGoodsPrice);
                holder.chkChecked=(CheckBox) layout.findViewById(R.id.chkSelect);
                layout.setTag(holder);
            }else{
                holder=(ViewHolder) layout.getTag();
            }
            final CartBean cart = getItem(position);
            GoodDetailsBean goods = cart.getGoods();
            if(goods==null){
                return layout;
            }
            holder.tvGoodsName.setText(goods.getGoodsName());
            holder.tvCartCount.setText("("+cart.getCount()+")");
            holder.tvGoodsPrice.setText(goods.getCurrencyPrice());
            
            Bitmap bitmap = BitmapUtils.showGoodsThumb(context,imageLoader,
                    parent, holder.ivGoodsThumb, 
                    I.DOWNLOAD_GOODS_THUMB_URL+cart.getGoods().getGoodsThumb(),
                    cart.getGoods().getGoodsThumb());
            if(bitmap==null){
                holder.ivGoodsThumb.setImageResource(R.drawable.nopic);
            }else{
                holder.ivGoodsThumb.setImageBitmap(bitmap);
            }
            AddDelCartListener listener=new AddDelCartListener(cart);
            holder.ivAddCart.setOnClickListener(listener);
            holder.ivReduceCart.setOnClickListener(listener);
            holder.chkChecked.setChecked(cart.isChecked());
            holder.chkChecked.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    cart.setChecked(isChecked);

                    cart.setCount(cart.getCount()-1);
                    Utils.addCart(getActivity(), cart.getGoods());
                    notifyDataSetChanged();
                }
            });
            return layout;
        }

        class ViewHolder{
            TextView tvGoodsName;
            TextView tvCartCount;
            ImageView ivAddCart;
            ImageView ivReduceCart;
            ImageView ivGoodsThumb;
            TextView tvGoodsPrice;
            
            CheckBox chkChecked;
        }
    }



    class DownloadGoodsDetailsInCartTask extends AsyncTask<Void,Void,Boolean>{
        Context context;
        ArrayList<CartBean> cartList;

        public DownloadGoodsDetailsInCartTask(Context context) {
            super();
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean isSuccess=false;
            ArrayList<CartBean> cartList = FuLiCenterApplication.getInstance().getCartList();
            for(int i=0;i<cartList.size();i++){
                CartBean cart = cartList.get(i);
                if(cart.getGoods()==null){
                    GoodDetailsBean goods = NetUtil.findGoodDetails(cart.getGoodsId());
                    cart.setGoods(goods);
                    isSuccess=goods!=null;
                }
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result){
                mAdapter.notifyDataSetChanged();
            }
        }
    }
    
    /**
     * 购物车中商品件数增减的事件监听
     * @author yao
     *
     */
    class AddDelCartListener implements OnClickListener{
        CartBean cart;
        
        public AddDelCartListener(CartBean cart) {
            super();
            this.cart = cart;
        }

        @Override
        public void onClick(View v) {
            cart.setChecked(true);
            int count = cart.getCount();
            switch (v.getId()) {
            case R.id.ivAddCart:
                Utils.addCart(getActivity(), cart.getGoods());
                break;
            case R.id.ivReduceCart:
                Utils.delCart(getActivity(), cart.getGoods());
                if(count ==1){
                    ArrayList<CartBean> cartList = FuLiCenterApplication.getInstance().getCartList();
                    if(cartList.contains(cart)){
                        cartList.remove(cart);
                    }
                }
                break;
            }
            mAdapter.notifyDataSetChanged();
        }
    }
    
    /**
     * 统计购物车中所有商品的总价和打折节省的钱
     * @param tvSumPrice：购物车中所有商品的总价格
     * @param tvSavePrice：节省下来的价格
     */
    protected void sumPrice(
            TextView tvSumPrice,TextView tvSavePrice) {
        ArrayList<CartBean> cartList = FuLiCenterApplication.getInstance().getCartList();
        int sumRankPrice=0;//人民币折扣价
        int sumCurrentPrice=0;//人民币价
        //遍历购物车
        for(int i=0;i<cartList.size();i++){
            CartBean cart = cartList.get(i);
            GoodDetailsBean goods = cart.getGoods();
            if(cart.isChecked()){
                //当同一种商品有多件时，需要多次累加该商品的价格
                for(int k=0;k<cart.getCount();k++){
                    int rankPrice = convertPrice(goods.getRankPrice());
                    int currentPrice=convertPrice(goods.getCurrencyPrice());
                    sumRankPrice+=rankPrice;
                    sumCurrentPrice+=currentPrice;
                }
            }
        }
        int sumSavePrice=sumCurrentPrice-sumRankPrice;
        tvSumPrice.setText("合计:￥"+sumRankPrice);
        tvSavePrice.setText("节省:￥"+sumSavePrice);
    }

    /**
     * 将头部带￥的商品价格转换为int类型
     * @param strPrice
     * @return
     */
    private int convertPrice(String strPrice) {
        strPrice=strPrice.substring(strPrice.indexOf("￥")+1);
        int price=Integer.parseInt(strPrice);
        return price;
    }

    /**
     * 接收来自DownloadCartTask发送的购物车数据改变的广播
     * @author yao
     */
    class CartChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            new DownloadGoodsDetailsInCartTask(getActivity()).execute();
        }
    }

    private void registerCartChangedReceiver() {
        mCartChangedReceiver=new CartChangedReceiver();
        IntentFilter filter=new IntentFilter("cartChanged");
        getActivity().registerReceiver(mCartChangedReceiver, filter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mCartChangedReceiver!=null) {
            getActivity().unregisterReceiver(mCartChangedReceiver);
        }
    }
}
