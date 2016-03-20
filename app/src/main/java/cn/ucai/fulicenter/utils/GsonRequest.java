package cn.ucai.fulicenter.utils;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;

/**
 * Created by clawpo on 16/3/20.
 */
public class GsonRequest<T> extends Request<T> {

    /**解析成功的事件处理*/
    Response.Listener mListener;
    /** gson解析*/
    private Gson mGson;
    /** 解析目标类的class对象*/
    private Class<T> mClass;

    /**
     * 构造器
     * @param method：http请求
     * @param url：资源的地址
     * @param listener：下载成功的事件响应
     * @param errorListener：下载失败的事件响应
     * @param clazz：解析目标类的class对象
     */
    public GsonRequest(int method, String url, Response.Listener listener, Response.ErrorListener errorListener, Class<T> clazz) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mClass = clazz;
        mGson=new Gson();
    }

    /**
     * 不包含http请求类型的构造器
     * @param url
     * @param listener
     * @param errorListener
     * @param mClass
     */
    public GsonRequest(String url, Response.Listener listener, Response.ErrorListener errorListener, Class<T> mClass) {
        this(Method.GET, url, listener,errorListener, mClass);
    }

    /**
     * 解析数据
     * @param response
     * @return
     */
    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            //将response.data转换为String类型，默认的编码为iso8859-1
            String text = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            //按utf-8重新解码
            text = NetUtilRS.iso2Utf8(text);
            return Response.success(mGson.fromJson(text, mClass),HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void deliverResponse(T t) {
        mListener.onResponse(t);
    }
}
