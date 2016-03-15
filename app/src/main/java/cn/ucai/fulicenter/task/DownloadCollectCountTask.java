package cn.ucai.fulicenter.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import cn.ucai.fulicenter.bean.MessageBean;
import cn.ucai.fulicenter.utils.NetUtil;

/**
 * Created by ucai001 on 2016/3/15.
 */
public class DownloadCollectCountTask extends AsyncTask<Integer, Void, Integer> {
    public static String TAG = "DownloadCollectCountTask";
    Context context;
    String userName;

    public DownloadCollectCountTask(Context context, String userName) {
        super();
        this.context = context;
        this.userName = userName;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        int count = 0;
        count = NetUtil.findCollectCount(userName);
        return count;
    }

    @Override
    protected void onPostExecute(Integer count) {
        if(count>0) {
            Intent intent = new Intent("update_collect_count").putExtra("collect_count",count);
            context.sendBroadcast(intent);
        }
    }

}
