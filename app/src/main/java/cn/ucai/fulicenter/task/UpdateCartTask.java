package cn.ucai.fulicenter.task;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import cn.ucai.fulicenter.utils.NetUtil;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by ucai001 on 2016/3/9.
 */
public class UpdateCartTask extends AsyncTask<Void, Void, Boolean> {
    public static final String TAG = UpdateCartTask.class.getName();
    Context context;
    int count;
    int cartId;
    boolean isChecked;
    
    public UpdateCartTask(Context context, int cartId, int count,boolean isChecked) {
        super();
        this.context = context;
        this.count = count;
        this.cartId = cartId;
        this.isChecked = isChecked;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean isSuccess = false;
        if(count<=0){
            isSuccess = NetUtil.deleteCart(cartId);
        }else{
            isSuccess = NetUtil.updateCart(cartId,count,isChecked);
        }
        Log.e(TAG,"doInBackground,isSuccess="+isSuccess);
        return isSuccess;
    }
    
    @Override
    protected void onPostExecute(Boolean result) {
        Log.e(TAG,"onPostExecute,result="+result);
        if(result){
            Utils.showToast(context, "操作成功", Toast.LENGTH_SHORT);
            Intent intent=new Intent("cartChanged");
            context.sendBroadcast(intent);
        }else{
            Utils.showToast(context, "操作失败", Toast.LENGTH_SHORT);
        }
    }

}
