package cn.ucai.fulicenter.task;

/**
 * Created by ucai001 on 2016/2/25.
 */

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import cn.ucai.fulicenter.bean.ContactBean;
import cn.ucai.fulicenter.utils.NetUtil;

/**
 * 删除应用服务端的联系人记录
 * @author yao
 *
 */
public class DeleteContactsTask extends AsyncTask<Void, Void, Boolean> {
    public static String TAG = "DeleteContactsTask";
    Context context;
    ArrayList<ContactBean> contacts;

    public DeleteContactsTask(Context context,
                              ArrayList<ContactBean> contacts) {
        super();
        this.context = context;
        this.contacts = contacts;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean isDelete = false;
        for(int i=0;i<contacts.size();i++){
            Log.e(TAG, "DeleteContactsTask.contacts.size="+contacts.size());
            ContactBean contact = contacts.get(i);
            Log.e(TAG, "DeleteContactsTask.contacts="+contact.toString());
            boolean isSuccess = NetUtil.deleteContact(contact.getMyuid(), contact.getCuid());
            Log.e(TAG, "DeleteContactsTask.contacts the one is ="+isSuccess);
            if(isSuccess){
                isSuccess = NetUtil.deleteContact(contact.getCuid(), contact.getMyuid());
                Log.e(TAG, "DeleteContactsTask.contacts the tow is ="+isSuccess);
                isDelete = isSuccess;
            }
        }
        return isDelete;
    }
    @Override
    protected void onPostExecute(Boolean result) {
        if(result){
            Intent intent = new Intent("update_contacts");
            context.sendBroadcast(intent);
        }
    };
}
