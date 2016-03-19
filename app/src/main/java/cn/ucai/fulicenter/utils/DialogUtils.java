package cn.ucai.fulicenter.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by clawpo on 16/3/18.
 */
public class DialogUtils {
    static ProgressDialog mProgressDialog;
    public static final int STYLE_DEFAULT = 0;
    public static final int STYLE_HORIZONTAL = 1;

    /**
     * 显示无进度对话框
     * @param context:当前的Android组件，如Activity.this
     * @param title:对话框标题
     * @param message:对话框的消息
     */
    public static void showProgressDialog(Activity context, String title, String message) {
        ProgressDialog dialog=getInstance(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        if (!context.isFinishing()&& !dialog.isShowing()) {
            dialog.show();
        }
    }

    /**
     * 显示水平进度对话框
     * @param context:当前的Android组件，如Activity.this
     * @param title:对话框标题
     * @param max:下载文件的字节数
     * @param progress:当前已下载的字节数
     */
    public static void showProgressDialog(Context context, String title, int max, int progress) {
        ProgressDialog dialog=getInstance(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMax(max);
        dialog.setProgress(progress);
        dialog.setTitle(title);
        dialog.show();
    }

    /**
     * 获取当前进度对话框对象
     * @param context
     * @return
     */
    private static ProgressDialog getInstance(Context context) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context);
        }
        return mProgressDialog;
    }

    /**
     * 关闭进度对话框
     */
    public static void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog=null;
        }
    }
}

