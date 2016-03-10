package cn.ucai.fulicenter.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import cn.ucai.fulicenter.utils.ImageLoader.OnImageLoadListener;

public final class BitmapUtils {
	/**
	 * 按指定尺寸转换图片
	 * @param data：图片的二进制数据
	 * @param width：图片的预期宽度
	 * @param height：图片的预期高度
	 * @return Bitmap类型
	 */
	public static Bitmap getBitmap(byte[] data,int width,int height){
		Options options=new Options();
		options.inJustDecodeBounds=true;
		//只获取图片的宽和高
		BitmapFactory.decodeByteArray(data, 0, data.length, options);
		int scaleX=options.outWidth/width;
		int scaleY=options.outHeight/height;
		int scale=scaleX;
		if(scale<scaleY){
			scale=scaleY;
		}
		options.inJustDecodeBounds=false;
		options.inSampleSize=scale;
		Bitmap bitmap=BitmapFactory.decodeByteArray(data, 0, data.length,options);
		return bitmap;
	}
	/**
	 * 从本地文件读取图片
	 * @param path：图片文件的本地路径
	 * @return 图片的Bitmap类型
	 */
	public static Bitmap getBitmap(String path){
		File file=new File(path);
		if(!file.exists()){
			return null;
		}
		if(file.length()==0){
			file.delete();
			return null;
		}
		Bitmap bitmap= BitmapFactory.decodeFile(path);
		return bitmap;
	}
	/**
	 *  将图片保存至本地
	 * @param bitmap：图片
	 * @param path：保存的路径
	 * @throws IOException
	 */
	public static void saveBitmap(Bitmap bitmap,String path) {
		File file=new File(path);
		if(!file.getParentFile().exists()){//若不存在目录，则创建
			boolean isSuccess = file.getParentFile().mkdirs();
			if(!isSuccess){//若文件所在目录创建失败，则返回
				return ;
			}
		}
		try {
			FileOutputStream out=new FileOutputStream(file);
			bitmap.compress(CompressFormat.JPEG, 100, out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 异步加载图片，在适配器的getView方法中显示指定的图片
	 * @param context:适配器所在的Activity
	 * @param imageLoader：图片异步加载的线程
	 * @param parent：ListView或GridView
	 * @param ivImg：显示图片的view
	 * @param imgUrl：图片的下载的完整地址
	 * @param imgPath:图片下载的不完整地址,保存在手机sd卡的路径
	 * @return
	 */
	public static Bitmap showGoodsThumb(Context context, ImageLoader imageLoader,
			final ViewGroup parent, ImageView ivImg, String imgUrl, String imgPath) {
        ivImg.setTag(imgUrl);
        Bitmap bitmap = imageLoader.displayImage(imgUrl, imgPath, Utils.dp2px(context, 42), Utils.dp2px(context, 42), new OnImageLoadListener() {
            @Override
            public void onSuccess(String path, Bitmap bitmap) {
                ImageView iv=(ImageView) parent.findViewWithTag(path);
                if(iv!=null){
                    iv.setImageBitmap(bitmap);
                }
            }
            
            @Override
            public void error(String errorMsg) {
                // TODO Auto-generated method stub
            }
        });
        return bitmap;
    }
	
}
