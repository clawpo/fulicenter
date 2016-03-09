package cn.ucai.fulicenter.task;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.util.ArrayList;

import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.bean.UserBean;
import cn.ucai.fulicenter.utils.NetUtil;

/**
 * Created by ucai001 on 2016/3/9.
 */
public class DownloadCartTask extends AsyncTask<Void, Void, Boolean> {
    
    Context context;
    int pageId;
    int pageSize;
    
    public DownloadCartTask(Context context,int pageId, int pageSize) {
        super();
        this.context = context;
        this.pageId = pageId;
        this.pageSize = pageSize;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        String userName=null;
        UserBean user=FuLiCenterApplication.getInstance().getUserBean();
        if(user!=null){
            userName = user.getUserName();
        }else{
            userName = "";
        }
        ArrayList<CartBean> cartList = NetUtil.findcartList(userName,pageId,pageSize);
        if(cartList!=null){
            ArrayList<CartBean> cartList2 = FuLiCenterApplication.getInstance().getCartList();
            if(!cartList2.containsAll(cartList)){
                cartList2.addAll(cartList);
            }
            Intent intent=new Intent("cartChanged");
            context.sendStickyBroadcast(intent);
        }
        return cartList!=null;
    }

}
