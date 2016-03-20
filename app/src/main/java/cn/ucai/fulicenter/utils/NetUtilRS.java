package cn.ucai.fulicenter.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.adapter.GoodAdapterRS;
import cn.ucai.fulicenter.bean.NewGoodBean;

/**
 * Created by clawpo on 16/3/20.
 */
public final class NetUtilRS {
    public static final String TAG = NetUtilRS.class.getName();

    /**
     * 下载新品首页或精选二级页面的商品信息
     * @param catId:新品或精选的下载请求
     * @param pageId
     * @param pageSize
     * @return
     * @throws Exception
     */
    public static void findNewandBoutiqueGoods(final GoodAdapterRS mAdapter, int catId, int pageId,
                       int pageSize, final int action, final SwipeRefreshLayout swipeRefreshLayout,
                       final TextView hint) throws Exception {

        //requestParams集合封装了向服务端发送的get请求的参数
        ArrayList<Param> requestParams = new ArrayList<>();
        requestParams.add(new Param(I.KEY_REQUEST, I.REQUEST_FIND_NEW_BOUTIQUE_GOODS));
        requestParams.add(new Param(I.NewAndBoutiqueGood.CAT_ID, catId+""));
        requestParams.add(new Param(I.PAGE_ID, pageId+""));
        requestParams.add(new Param(I.PAGE_SIZE, pageSize+""));

        //将URL和请求参数转换为url字符串格式
        String url=getUrl(FuLiCenterApplication.SERVER_ROOT, requestParams);

        //创建用gson直接将文本解析为NewGoodBean数组的请求对象
        GsonRequest<NewGoodBean[]> request=new GsonRequest<>(url, new Response.Listener<NewGoodBean[]>() {
            @Override
            public void onResponse(NewGoodBean[] apps) {
                swipeRefreshLayout.setRefreshing(false);
                hint.setVisibility(View.GONE);
                //将数组转换为集合
                ArrayList<NewGoodBean> list = NetUtilRS.array2List(apps);
                if(action == I.ACTION_DOWNLOAD || action == I.ACTION_PULL_DOWN){
                    mAdapter.initItems(list);
                }else if(action == I.ACTION_PULL_UP) {
                    mAdapter.addItems(list);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        },NewGoodBean[].class);
        request.setTag(I.REQUEST_FIND_NEW_BOUTIQUE_GOODS);
        FuLiCenterApplication.getInstance().getRequestQueue().add(request);
    }

    /**
     * 将String由iso8859-1转换为utf-8
     *
     * @param text
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String iso2Utf8(String text) throws UnsupportedEncodingException {
        String result = new String(text.getBytes("iso8859-1"), "utf-8");
        return result;
    }

    /**
     * 将服务端根地址和请求参数集合转换为GET格式的url
     * @param rootUrl:根地址
     * @param params：GET请求参数集合
     * @return
     * @throws Exception
     */
    public static String getUrl(String rootUrl, ArrayList<Param> params) throws Exception {
        StringBuilder url = new StringBuilder(rootUrl);
        if (params == null || params.isEmpty()) {
            return rootUrl;
        }
        url.append("?");
        for (Param param : params) {
            url.append(param.getKey())
                    .append("=")
                    .append(URLEncoder.encode(param.getValue(), "utf-8"))
                    .append("&");
        }
        url.deleteCharAt(url.length() - 1);
        return url.toString();
    }

    /**
     * 封装GET请求参数
     */
    public static class Param extends HashMap<String, String> {
        HashMap<String, String> param;

        public Param(String key, String value) {
            param = new HashMap<>();
            param.put(key,value);
        }

        public String getKey() {
            Set<String> set = param.keySet();
            for (String key : set) {
                return key;
            }
            return null;
        }

        public String getValue() {
            Collection<String> values = param.values();
            for (String value : values) {
                return value;
            }
            return null;
        }
    }

    public static <T> ArrayList<T> array2List(T[] array){
        List<T> list = Arrays.asList(array);
        ArrayList<T> arrayList = new ArrayList<>(list);
        return arrayList;
    }

    /**
     * 缓存图片的类,将图片缓存在内存和sd卡
     */
    public static class BitmapCaches implements ImageLoader.ImageCache{
        LruCache<String,Bitmap> mCaches;

        Context mContext;
        public BitmapCaches(Context context) {
            mContext=context;
            //获取app的内存容量
            int maxCacheSize= (int) Runtime.getRuntime().maxMemory();
            mCaches=new LruCache<String, Bitmap>(maxCacheSize/4){
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getRowBytes()*value.getHeight();
                }
            };
        }

        @Override
        public Bitmap getBitmap(String url) {
            if (mCaches.get(url) != null) {
                return mCaches.get(url);
            }
            //获取图片缓存在sd的文件名：imgName
            String imgName=url.substring(url.lastIndexOf("/")+1);
            //从sd卡获取缓存的图片
            return BitmapUtils.getBitmap(FileUtils.getDir(mContext, imgName));
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            //将图片缓存在内存中
            mCaches.put(url,bitmap);
            //将图片缓存在sd卡指定路径，文件名：imgName
            String imgName=url.substring(url.lastIndexOf("/")+1);
            BitmapUtils.saveBitmap(bitmap, FileUtils.getDir(mContext,imgName));
        }
    }

}
