package cn.ucai.fulicenter.activity;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.UserBean;
import cn.ucai.fulicenter.utils.Encoder;
import cn.ucai.fulicenter.utils.NetUtilRS;

/**
 * Created by clawpo on 16/3/25.
 */
public class QRCodeActivity extends BaseActivity {

    private ImageView mQRCodeImage;
    private Encoder mEncoder;
    private DecodeTask mDecodeTask;
    private ImageView mReturn;
    private UserBean mUser;
    private TextView mtvUser;
    private NetworkImageView mivAvatar;
    private ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        mQRCodeImage = (ImageView) findViewById(R.id.qrcode_image);
        mReturn = (ImageView) findViewById(R.id.ivReturn);
        mtvUser = (TextView) findViewById(R.id.tv_user_name);
        mivAvatar = (NetworkImageView) findViewById(R.id.iv_avatar);


        final WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        final Display display = manager.getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        final int width = displaySize.x;
        final int height = displaySize.y;
        final int dimension = width < height ? width : height;

        mEncoder = new Encoder.Builder()
                .setBackgroundColor(0xFFFFFF)
                .setCodeColor(0xFF000000)
                .setOutputBitmapPadding(0)
                .setOutputBitmapWidth(dimension)
                .setOutputBitmapHeight(dimension)
                .build();

        mDecodeTask = new DecodeTask();
        ReturnBack();
        mUser = FuLiCenterApplication.getInstance().getUserBean();
        imageLoader = new ImageLoader(FuLiCenterApplication.getInstance().getRequestQueue(),
                new NetUtilRS.BitmapCaches(getApplicationContext()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDecodeTask.execute("user_id=1");
        mtvUser.setText(mUser.getNick());
        loadUserAvatar();
    }

    private void loadUserAvatar(){
        String url = I.DOWNLOAD_AVATAR_URL+mUser.getAvatar();
        Log.e("main","url="+url);
        mivAvatar.setDefaultImageResId(R.drawable.contactlogo);
        mivAvatar.setErrorImageResId(R.drawable.contactlogo);
        mivAvatar.setImageUrl(url,imageLoader);
    }

    private void ReturnBack(){
        mReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private class DecodeTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            Log.e("main","params[0]="+params[0]);
            return mEncoder.encode(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mQRCodeImage.setImageBitmap(bitmap);
        }
    }
}
