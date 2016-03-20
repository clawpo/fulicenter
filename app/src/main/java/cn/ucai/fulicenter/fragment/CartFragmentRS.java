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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.BuyActivity;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.utils.BitmapUtils;
import cn.ucai.fulicenter.utils.ImageLoader;
import cn.ucai.fulicenter.utils.NetUtil;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by ucai001 on 2016/3/9.
 */
public class CartFragmentRS extends Fragment {
    public static final String TAG = CartFragmentRS.class.getName();
    CartAdapterRS mAdapter;
    RecyclerView mrvCart;
    
    TextView mtvSumPrice;
    TextView mtvSavePrice;
    TextView mtvNothing;
    
    CartChangedReceiver mCartChangedReceiver;
    
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout=View.inflate(getActivity(), R.layout.fragment_cart_rs, null);
        initView(layout);
        setListener(layout);
        registerCartChangedReceiver();
        return layout;
    }

    private void setListener(View layout) {
        setBuyClickListener(layout);
    }

    private void setBuyClickListener(View layout) {
        layout.findViewById(R.id.btnBuy).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), BuyActivity.class);
                getActivity().startActivity(intent);
            }
        });
    }

    private void initView(View layout) {
        mtvSumPrice=(TextView) layout.findViewById(R.id.tvSumPrice);
        mtvSavePrice=(TextView) layout.findViewById(R.id.tvSavePrice);
        mtvNothing= (TextView) layout.findViewById(R.id.tv_nothing);

        mrvCart = (RecyclerView) layout.findViewById(R.id.rv_cart);
        mrvCart.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter=new CartAdapterRS(getActivity(),mtvSumPrice,mtvSavePrice);
        mrvCart.setHasFixedSize(true);
        mrvCart.setAdapter(mAdapter);
        mtvNothing.setVisibility(View.VISIBLE);
    }

    /**
     * 购物车适配器
     */
    class CartAdapterRS extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        Context context;
        ArrayList<CartBean> cartList;
        ImageLoader imageLoader;
        TextView tvSumPrice,tvSavePrice;
        boolean isUpdate;

        FooterViewHolder footerHolder;
        CartViewHolder cartHolder;

        /**RecyclerView*/
        ViewGroup parent;
        String footerText;

        public CartAdapterRS(Context context, TextView tvSumPrice, TextView tvSavePrice) {
            super();
            this.context = context;
            this.tvSumPrice = tvSumPrice;
            this.tvSavePrice=tvSavePrice;

            cartList=FuLiCenterApplication.getInstance().getCartList();
            imageLoader=ImageLoader.getInstance(context);
            isUpdate=true;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            this.parent = parent;
            LayoutInflater inflater = LayoutInflater.from(context);
            RecyclerView.ViewHolder holder = null;
            switch (viewType){
                case I.TYPE_ITEM:
                    holder = new CartViewHolder(inflater.inflate(R.layout.item_cart, parent,false));
                    break;
                case I.TYPE_FOOTER:
                    holder = new FooterViewHolder(inflater.inflate(R.layout.item_footer, parent,false));
                    break;
            }

            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if(holder instanceof FooterViewHolder){
                footerHolder  = (FooterViewHolder) holder;
                footerHolder.tvFooter.setText(footerText);
                footerHolder.tvFooter.setVisibility(View.VISIBLE);
                return;
            }
            if(position == cartList.size()){
                return;
            }
            if(holder instanceof CartViewHolder){
                cartHolder = (CartViewHolder) holder;
                final CartBean cart = cartList.get(position);
                GoodDetailsBean goods = cart.getGoods();
                if(goods==null){
                    return;
                }
                cartHolder.tvGoodsName.setText(goods.getGoodsName());
                cartHolder.tvCartCount.setText("("+cart.getCount()+")");
                cartHolder.tvGoodsPrice.setText(goods.getCurrencyPrice());

                Bitmap bitmap = BitmapUtils.showGoodsThumb(context,imageLoader,
                        parent, cartHolder.ivGoodsThumb,
                        I.DOWNLOAD_GOODS_THUMB_URL+cart.getGoods().getGoodsThumb(),
                        cart.getGoods().getGoodsThumb());
                if(bitmap==null){
                    cartHolder.ivGoodsThumb.setImageResource(R.drawable.nopic);
                }else{
                    cartHolder.ivGoodsThumb.setImageBitmap(bitmap);
                }
                AddDelCartListener listener=new AddDelCartListener(cart);
                cartHolder.ivAddCart.setOnClickListener(listener);
                cartHolder.ivReduceCart.setOnClickListener(listener);
                cartHolder.chkChecked.setChecked(cart.isChecked());
                cartHolder.chkChecked.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        cart.setChecked(isChecked);

                        cart.setCount(cart.getCount()-1);
                        Utils.addCart(getActivity(), cart.getGoods());
                        sumPrice(tvSumPrice,tvSavePrice);
                        notifyDataSetChanged();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            int count = cartList==null?0:cartList.size();
            if(count>0){
                mtvNothing.setVisibility(View.GONE);
            }else{
                mtvNothing.setVisibility(View.VISIBLE);
            }
            return count;
        }


        class CartViewHolder extends RecyclerView.ViewHolder{
            TextView tvGoodsName;
            TextView tvCartCount;
            ImageView ivAddCart;
            ImageView ivReduceCart;
            ImageView ivGoodsThumb;
            TextView tvGoodsPrice;
            
            CheckBox chkChecked;

            public CartViewHolder(View itemView) {
                super(itemView);
                tvCartCount=(TextView) itemView.findViewById(R.id.tvCartCount);
                tvGoodsName=(TextView) itemView.findViewById(R.id.tvGoodsName);
                ivAddCart=(ImageView) itemView.findViewById(R.id.ivAddCart);
                ivReduceCart=(ImageView) itemView.findViewById(R.id.ivReduceCart);
                ivGoodsThumb=(ImageView) itemView.findViewById(R.id.ivGoodsThumb);
                tvGoodsPrice=(TextView) itemView.findViewById(R.id.tvGoodsPrice);
                chkChecked=(CheckBox) itemView.findViewById(R.id.chkSelect);
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
                sumPrice(mtvSumPrice,mtvSavePrice);
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
                    if(goods!=null) {
                        int rankPrice = convertPrice(goods.getRankPrice());
                        int currentPrice = convertPrice(goods.getCurrencyPrice());
                        sumRankPrice += rankPrice;
                        sumCurrentPrice += currentPrice;
                    }
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
