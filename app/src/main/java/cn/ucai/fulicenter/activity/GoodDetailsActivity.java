package cn.ucai.fulicenter.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.ucai.fulicenter.D;
import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.AlbumBean;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.bean.MessageBean;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.bean.UserBean;
import cn.ucai.fulicenter.utils.NetUtil;
import cn.ucai.fulicenter.utils.NetUtilRS;
import cn.ucai.fulicenter.utils.Utils;
import cn.ucai.fulicenter.view.FlowIndicator;
import cn.ucai.fulicenter.view.SlideAutoLoopView;

/**
 * Created by ucai001 on 2016/3/8.
 */
public class GoodDetailsActivity extends BaseActivity {
    public static final String TAG = GoodDetailsActivity.class.getName();
    GoodDetailsActivity mContext;
    GoodDetailsBean mGoodDetails;
    int mGoodsId;
    /** 用于收藏、支付的商品信息实体*/
    NewGoodBean mGood;
    /** 封装了显示商品信息的view*/
    ViewHolder mHolder;

    SlideAutoLoopView mSlideAutoLoopView;
    FlowIndicator mFlowIndicator;
    /** 显示颜色的容器布局*/
    LinearLayout mLayoutColors;
    /** 添加收藏*/
    final static int ACTION_ADD_COLLECT=1;
    /** 取消收藏*/
    final static int ACTION_DELETE_COLLECT=2;
    /** 收藏的操作码*/
    boolean misCollect;

    ImageView mivCollect;
    ImageView mivAddCart;
    ImageView mivShare;
    TextView mtvCartCount;

    GoodDetailsUpdateReceiver mReceiver;
    
    CartChangedReceiver mCartChangedReceiver;

    ArrayList<CartBean> mCartList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_details);
        mContext=this;
        initView();
        initData();
        setListener();
        registerGoodDetailsUpdateReceiver();
        registerCartChangedReceiver();
    }

    private void registerCartChangedReceiver() {
        mCartChangedReceiver=new CartChangedReceiver();
        IntentFilter filter=new IntentFilter("cartChanged");
        registerReceiver(mCartChangedReceiver, filter);
    }

    private void registerGoodDetailsUpdateReceiver() {
        mReceiver=new GoodDetailsUpdateReceiver();
        IntentFilter filter=new IntentFilter("good_details_update");
        registerReceiver(mReceiver, filter);
    }
    private void setListener() {
        setCollectClickListener();
        setReturnClickListener();
        setShareListener();
        setAddCartClickListener();
    }
    private void setShareListener(){
        mivShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showShare();
            }
        });
    }
    private void showShare() {
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(mGoodDetails.getGoodsName());
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl("http://sharesdk.cn");
        // text是分享文本，所有平台都需要这个字段
        oks.setText(mGoodDetails.getGoodsBrief());
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        //oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        String url = I.SERVER_ROOT
                + "?" + I.KEY_REQUEST + "=" + I.REQUEST_FIND_GOOD_DETAILS
                + "&" + I.CategoryGood.GOODS_ID + "=" + mGoodDetails.getId();
        oks.setUrl(url);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment(mGoodDetails.getGoodsBrief());
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(url);

        // 启动分享GUI
        oks.show(this);
    }

    private void setAddCartClickListener() {
        mivAddCart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG,"mGoodDetails="+mGoodDetails);
                Utils.addCart(mContext,mGoodDetails);
            }
        });
    }

    private void setReturnClickListener() {
        findViewById(R.id.ivReturn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * 设置收藏/取消收藏按钮的点击事件监听
     */
    private void setCollectClickListener() {
        mivCollect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName=FuLiCenterApplication.getInstance().getUserName();
                if(userName==null){
                    Utils.showToast(mContext, "请先登陆", Toast.LENGTH_LONG);
                    return ;
                }
                new CollectTask(mContext,userName,mGoodDetails,mivCollect,misCollect).execute();
            }
        });
    }

    /**
     * 下载商品详情信息
     */
    private void initData() {
        mGoodsId=getIntent().getIntExtra(D.GoodDetails.KEY_GOODS_ID, 0);
        new DownloadGoodDetailsTask(mContext, mGoodsId).execute();
        initCartData();
    }

    private void initView() {
        mivCollect=getViewById(R.id.ivCollect);
        mivAddCart=getViewById(R.id.ivAddCart);
        mivShare = getViewById(R.id.ivShare);
        mtvCartCount=getViewById(R.id.tvCartCount);

        mSlideAutoLoopView=getViewById(R.id.salv);
        mFlowIndicator=getViewById(R.id.indicator);
        mLayoutColors=getViewById(R.id.layoutColorSelector);
        mHolder=new ViewHolder();
        mHolder.tvCurrencyPrice=getViewById(R.id.tvCurrencyPrice);
        mHolder.tvGoodEngishName=getViewById(R.id.tvGoodEnglishName);
        mHolder.tvGoodName=getViewById(R.id.tvGoodName);
        mHolder.tvShopPrice=getViewById(R.id.tvShopPrice);
        mHolder.wvGoodBrief=getViewById(R.id.wvGoodBrief);
        WebSettings settings = mHolder.wvGoodBrief.getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setBuiltInZoomControls(true);
    }

    /**
     * 封装了显示商品信息的view
     */
    class ViewHolder{
        TextView tvGoodName;
        TextView tvGoodEngishName;
        TextView tvShopPrice;
        TextView tvCurrencyPrice;
        WebView wvGoodBrief;
    }

    /**
     * 下载商品详情信息
     * @author yao
     *
     */
    class DownloadGoodDetailsTask extends AsyncTask<Void, Void, GoodDetailsBean> {
        Context context;
        int goodsId;
        ProgressDialog dialog;
        ImageLoader imageLoader;
        boolean isCollect;
        /** 当前的颜色值*/
        int mCurrentColor;

        GoodDetailsBean goodDetails;
        public DownloadGoodDetailsTask(Context context,int goodsId) {
            super();
            this.context = context;
            this.goodsId=goodsId;
//            imageLoader=ImageLoader.getInstance(context);
            this.imageLoader = new com.android.volley.toolbox.ImageLoader(FuLiCenterApplication.getInstance().getRequestQueue(),
                    new NetUtilRS.BitmapCaches(mContext));
        }

        public GoodDetailsBean getGoodDetails(){
            return this.goodDetails;
        }

        @Override
        protected void onPreExecute() {
            dialog=new ProgressDialog(context);
            dialog.setTitle(D.GoodDetails.HINT_DOWNLOAD_TITLE);
            dialog.setMessage(D.GoodDetails.HINT_DOWNLOADING);
            dialog.show();
        }

        @Override
        protected GoodDetailsBean doInBackground(Void... param) {
            goodDetails = NetUtil.findGoodDetails(goodsId);
            UserBean user= FuLiCenterApplication.getInstance().getUserBean();
            if(user!=null){
                this.isCollect = NetUtil.isCollect(user.getUserName(),goodsId);
            }else{
                this.isCollect=false;
            }
            Log.i(TAG,"getDetail="+goodDetails);
            return goodDetails;
        }

        @Override
        protected void onPostExecute(GoodDetailsBean result) {
            dialog.dismiss();
            if(null==result){
                Utils.showToast(context, "商品详情下载失败", Toast.LENGTH_LONG);
                return ;
            }

            /**
             * 向GoodDetailsActivity发送商品详情对象下载完成的广播，并将该对象传递
             * 给GoodDetailsActivity.mGoodDetails
             */
            context.sendBroadcast(new Intent("good_details_update")
                    .putExtra("good_details", goodDetails)
                    .putExtra("is_collect", isCollect));

            if(isCollect){
                mivCollect.setImageResource(R.drawable.bg_collect_out);
            }else{
                mivCollect.setImageResource(R.drawable.bg_collect_in);
            }

//            mHolder.tvShopPrice.setText(result.getShopPrice());
            mHolder.tvCurrencyPrice.setText(result.getCurrencyPrice());
            mHolder.tvGoodEngishName.setText(result.getGoodsEnglishName());
            mHolder.tvGoodName.setText(result.getGoodsName());
            mHolder.wvGoodBrief.loadDataWithBaseURL(null, result.getGoodsBrief().trim(), D.TEXT_HTML, D.UTF_8, null);

            //初始化颜色面板
            initColorsBanner();
        }

        private void initColorsBanner() {
            //设置第一个颜色的图片轮播
            updateColor(0);
            for(int i=0;i<goodDetails.getProperties().length;i++){
                mCurrentColor=i;
                View layout=View.inflate(context, R.layout.layout_property_color, null);
                final NetworkImageView ivColor=(NetworkImageView) layout.findViewById(R.id.ivColorItem);
                Log.i(TAG,"initColorsBanner.goodDetails="+goodDetails.getProperties()[i].toString());
                String colorImg = goodDetails.getProperties()[i].getColorImg();
                if(colorImg.isEmpty()){
                    continue;
                }
                String url= I.SERVER_ROOT
                        +"?"+I.KEY_REQUEST+"="+I.REQUEST_DOWNLOAD_COLOR_IMG
                        +"&"+I.Color.COLOR_IMG+"="+colorImg;
                ivColor.setDefaultImageResId(R.drawable.bg_good);
                ivColor.setErrorImageResId(R.drawable.bg_good);
                ivColor.setImageUrl(url,imageLoader);
                mLayoutColors.addView(layout);
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateColor(mCurrentColor);
                    }
                });
            }
        }

        /**
         * 设置指定属性的图片轮播
         * @param i
         */
        private void updateColor(int i) {
            AlbumBean[] albums = goodDetails.getProperties()[i].getAlbums();
            String[] albumImgUrl=new String[albums.length];
            for(int j=0;j<albumImgUrl.length;j++){
                albumImgUrl[j]=albums[j].getImgUrl();
            }
            mSlideAutoLoopView.startPlayLoop(mFlowIndicator, albumImgUrl, albumImgUrl.length);
        }

    }
    /**
     * 收藏操作
     */
    class CollectTask extends AsyncTask<Void, Void, MessageBean>{
        Context context;
        String userName;
        GoodDetailsBean goodDetails;
        ImageView ivCollect;
        boolean isCollect;
        public CollectTask(Context context,String userName,GoodDetailsBean goodDetails,
                ImageView ivCollect,boolean isCollect) {
            super();
            this.context=context;
            this.userName=userName;
            this.goodDetails=goodDetails;
            this.ivCollect=ivCollect;
            this.isCollect=isCollect;
        }
        
        @Override
        protected MessageBean doInBackground(Void... params) {
            MessageBean msg=null;
            if(isCollect){
                msg=NetUtil.deleteCollect(userName, goodDetails.getGoodsId());
            }else {
                msg=NetUtil.addCollect(userName,goodDetails);
            }
            return msg;
        }
        
        @Override
        protected void onPostExecute(MessageBean result) {
            if(result==null){
                Utils.showToast(context, "操作失败", Toast.LENGTH_LONG);
                return ;
            }
            Utils.showToast(context, result.getMsg(), Toast.LENGTH_LONG);
            if(result.getMsg().equals("收藏成功")){
                if(result.isSuccess()){
                    mivCollect.setImageResource(R.drawable.bg_collect_out);
                    sendBroadcast(new Intent("good_details_update")
                        .putExtra("is_collect", true));
                }
            }else if(result.getMsg().equals("删除收藏成功")){
                if(result.isSuccess()){
                    mivCollect.setImageResource(R.drawable.bg_collect_in);
                    sendBroadcast(new Intent("good_details_update")
                    .putExtra("is_collect", false));
                }
            }
        }
    }
    
    /**
     * 接收商品详情下载的广播
     * @author yao
     *
     */
    class GoodDetailsUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            GoodDetailsBean goodDetails=(GoodDetailsBean) intent.getSerializableExtra("good_details");
            if(goodDetails!=null){
                mGoodDetails=goodDetails;
            }
            misCollect=intent.getBooleanExtra("is_collect", false);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mReceiver!=null){
            unregisterReceiver(mReceiver);
        }
        if(mCartChangedReceiver!=null){
            unregisterReceiver(mCartChangedReceiver);
        }
    }

    private void initCartData(){
        mCartList=FuLiCenterApplication.getInstance().getCartList();
        Log.e(TAG,"CartChangedReceiver.mCartList="+mCartList);
        if(!mCartList.isEmpty()){
            mtvCartCount.setVisibility(View.VISIBLE);
            int count=Utils.sumCartCount();
            Log.e(TAG,"CartChangedReceiver.count="+count);
            mtvCartCount.setText(""+count);
        }
    }

    /**
     * 接收来自DownloadCartTask发送的购物车数据改变的广播
     * @author yao
     */
    class CartChangedReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            initCartData();
        }
    }
}
