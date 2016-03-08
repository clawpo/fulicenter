package cn.ucai.fulicenter.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cn.ucai.fulicenter.D;
import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.adapter.MessageAdapter;
import cn.ucai.fulicenter.bean.AlbumBean;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.bean.UserBean;
import cn.ucai.fulicenter.utils.ImageLoader;
import cn.ucai.fulicenter.utils.NetUtil;
import cn.ucai.fulicenter.utils.Utils;
import cn.ucai.fulicenter.view.FlowIndicator;
import cn.ucai.fulicenter.view.SlideAutoLoopView;

/**
 * Created by ucai001 on 2016/3/8.
 */
public class GoodDetailsActivity extends BaseActivity {
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
    TextView mtvCartCount;

//    GoodDetailsUpdateReceiver mReceiver;
//
//    CartChangedReceiver mCartChangedReceiver;

    ArrayList<CartBean> mCartList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_details);
        mContext=this;
        initView();
        initData();
    }

    /**
     * 下载商品详情信息
     */
    private void initData() {
        mGoodsId=getIntent().getIntExtra(D.GoodDetails.KEY_GOODS_ID, 0);
        new DownloadGoodDetailsTask(mContext, mGoodsId).execute();
    }

    private void initView() {
        mivCollect=getViewById(R.id.ivCollect);
        mivAddCart=getViewById(R.id.ivAddCart);
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
            imageLoader=ImageLoader.getInstance(context);
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

            mHolder.tvCurrencyPrice.setText(result.getCurrencyPrice());
            mHolder.tvGoodEngishName.setText(result.getGoodsEnglishName());
            mHolder.tvGoodName.setText(result.getGoodsName());
            mHolder.wvGoodBrief.loadDataWithBaseURL(null, result.getGoodsBrief(), D.TEXT_HTML, D.UTF_8, null);

            //初始化颜色面板
            initColorsBanner();
        }

        private void initColorsBanner() {
            //设置第一个颜色的图片轮播
            updateColor(0);
            for(int i=0;i<goodDetails.getProperties().length;i++){
                mCurrentColor=i;
                View layout=View.inflate(context, R.layout.layout_property_color, null);
                final ImageView ivColor=(ImageView) layout.findViewById(R.id.ivColorItem);
                Log.i("main",goodDetails.getProperties()[i].toString());
                String colorImg = goodDetails.getProperties()[i].getColorImg();
                if(colorImg.isEmpty()){
                    continue;
                }
                String url= I.SERVER_ROOT
                        +"?"+I.KEY_REQUEST+"="+I.REQUEST_DOWNLOAD_COLOR_IMG
                        +"&"+I.Color.COLOR_IMG+"="+colorImg;
                String imgName="images/"+colorImg;
                Bitmap bitmap = imageLoader.displayImage(url, imgName, 32, 32, new ImageLoader.OnImageLoadListener() {

                    @Override
                    public void onSuccess(String path, Bitmap bitmap) {
                        ivColor.setImageBitmap(bitmap);
                    }

                    @Override
                    public void error(String errorMsg) {
                        // TODO Auto-generated method stub

                    }
                });
                if(bitmap==null){
                    ivColor.setImageResource(R.drawable.bg_good);
                }else{
                    ivColor.setImageBitmap(bitmap);
                }
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
}
