package cn.ucai.fulicenter.task;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.utils.NetUtil;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by ucai001 on 2016/3/9.
 */
public class UploadCartTask extends AsyncTask<Void, Void, Boolean> {
    Context context;
    CartBean cart;
    
    public UploadCartTask(Context context, CartBean cart) {
        super();
        this.context = context;
        this.cart = cart;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        int id= NetUtil.addCart(cart);
        if(id>=0){
            cart.setId(id);
            return true;
        }
        return false;
    }
    @Override
    protected void onPostExecute(Boolean result) {
        if(result){
            Utils.showToast(context, "添加成功", Toast.LENGTH_SHORT);
            Intent intent=new Intent("cartChanged");
            context.sendBroadcast(intent);
        }else{
            Utils.showToast(context, "添加失败", Toast.LENGTH_SHORT);
        }
    }

}
