package cn.ucai.fulicenter.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.ucai.fulicenter.R;

/**
 * 自定义View，实现下拉刷新
 * 
 * @author yw
 */
public class PullRefreshView<T extends AdapterView<?>> extends RelativeLayout
		implements OnTouchListener {
	/**
	 * 加载状态的枚举类
	 * @author yao
	 *
	 */
	public enum LoadStatus {
		SUCCESS, FAILURE, LOADING
	}

	T mAdapterView;// 被刷新的控件，可以是ListView、GridView

	private static final int SCROLL_SPEED = 5;
	// 尚未刷新状态
	private static final int STATUS_NOT_REFRESH = 0;
	// 下拉状态
	private static final int STATUS_PULL = 1;
	// 释放刷新状态
	private static final int STATUS_RELEASE = 2;
	// 刷新状态
	private static final int STATUS_REFRESHING = 3;
	// 刷新成功
	private static final int STATUS_REFRESH_SUCCESS = 4;
	// 刷新完成状态
	private static final int STATUS_REFRESH_FAILURE = 5;

	// 代表当前的下拉状态，取值来自于以上四个常量
	private int mCurrentStatus = STATUS_NOT_REFRESH;

	TextView mtvHint;// 显示状态状态文本的控件

	ImageView mivArrow;// 显示箭头的控件

	ProgressBar mProgressBar;// 显示刷新中的控件

	RelativeLayout mHeadLayout;// 包括以上三个控件的布局view

	LayoutParams mParams;// 表示mHeardLayout的布局参数

	int mHeadHeight;// 下拉刷新头高度的负值

	boolean mLoaded;// 下拉头是否加载过,true：加载

	Context mContext;// 上下文

	// 设置加载和是否加载完成的监听器
	OnRefreshListener mListner;

	boolean mAblePull;//true:可下拉，false：不可下拉

	private float mDownY;
	
	/**
	 * 构造器
	 * 
	 * @param context
	 * @param attrs
	 */
	public PullRefreshView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();// 初始化控件
	}

	// 初始化控件
	private void initView() {
		// 创建下拉刷新头布局对象
		mHeadLayout = (RelativeLayout) View.inflate(mContext,
				R.layout.pull_refresh_view, null);
		mtvHint = (TextView) mHeadLayout.findViewById(R.id.tvHint);
		mivArrow = (ImageView) mHeadLayout.findViewById(R.id.ivArrow);
		mProgressBar = (ProgressBar) mHeadLayout.findViewById(R.id.pb);
		this.addView(mHeadLayout);
	}

	// 重新布局，包括设置view的位置、尺寸
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed && !mLoaded) {
			// 若是第一次调用onLayout，获取下拉刷新头的原始尺寸
			mParams = (LayoutParams) mHeadLayout.getLayoutParams();
			mHeadHeight = -mHeadLayout.getHeight();
			// 隐藏下拉刷新头
			mParams.topMargin = mHeadHeight;
			mHeadLayout.setLayoutParams(mParams);
			mLoaded = true;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(!isAblePull(event)){
			return false;
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownY=event.getRawY();
			mtvHint.setText("下拉可刷新");
			break;
		case MotionEvent.ACTION_MOVE:
			float moveY=event.getRawY();
			float distance=moveY-mDownY;
			if(distance<0){
				return false;
			}
			if(mCurrentStatus==STATUS_REFRESHING){
				return false;
			}
			if(mParams.topMargin<0){
				mCurrentStatus=STATUS_PULL;
				mParams.topMargin=(int) Math.min(0, distance/2+mHeadHeight);
				mHeadLayout.setLayoutParams(mParams);
			}else{
				mCurrentStatus=STATUS_RELEASE;
			}
			refreshHeadView();
			break;
		case MotionEvent.ACTION_UP:
			if((int)event.getRawY()==(int)mDownY){//若是单击，则退出
				return false;
			}
			if(mCurrentStatus==STATUS_PULL){//若是下拉状态，则隐藏下拉头
				finishRefresh(STATUS_NOT_REFRESH);
			}else if(mCurrentStatus==STATUS_RELEASE){
				if(mListner!=null){
					mListner.loadData();
					new RefreshingThread().start();
				}
			}
			break;
		}
		return true;
	}

	private boolean isAblePull(MotionEvent event) {
		View firstChild = mAdapterView.getChildAt(0);
		if(null==firstChild){
			mAblePull=true;
			return true;
		}
		int position = mAdapterView.getFirstVisiblePosition();
		if(position==0&&firstChild.getTop()==0){
			if(!mAblePull){
				mDownY=event.getRawY();
			}
			mHeadLayout.setVisibility(View.VISIBLE);
			mAblePull=true;
		}else{
			mAblePull=false;
			//隐藏下拉头
			mParams.topMargin=mHeadHeight;
			mHeadLayout.setLayoutParams(mParams);
		}
		return mAblePull;
	}

	/**
	 * 刷新下拉头：根据不同的下拉状态，显示不同的文本 隐藏或显示箭头、进度条
	 */
	private void refreshHeadView() {
		switch (mCurrentStatus) {
		case STATUS_NOT_REFRESH:// 尚未拉动、刷新
			mtvHint.setText("尚未刷新");
			mivArrow.setVisibility(View.GONE);
			mProgressBar.setVisibility(GONE);
			break;
		case STATUS_PULL:// 下拉状态
			mtvHint.setText("下拉可以刷新");
			mivArrow.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);
			rotateArrow();// 旋转箭头
			break;
		case STATUS_RELEASE:// 释放状态
			mtvHint.setText("释放立即刷新");
			mivArrow.setVisibility(View.VISIBLE);
			rotateArrow();// 旋转箭头
			mProgressBar.setVisibility(View.GONE);
			break;
		case STATUS_REFRESHING:// 刷新中
			mtvHint.setText("刷新中...");
			mivArrow.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.VISIBLE);
			break;
		case STATUS_REFRESH_SUCCESS:// 刷新完成
			mtvHint.setText("刷新成功");
			mivArrow.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.GONE);
			break;
		case STATUS_REFRESH_FAILURE:
			mtvHint.setText("刷新失败");
			mProgressBar.setVisibility(View.GONE);
			break;
		}
	}

	// 旋转箭头
	private void rotateArrow() {
		float fromDegrees = 0;// 初始角度
		float toDegrees = 0;// 最终的角度
		if (mCurrentStatus == STATUS_PULL) {
			fromDegrees = 180;
			toDegrees = 360;
		} else if (mCurrentStatus == STATUS_RELEASE) {
			fromDegrees = 0;
			toDegrees = 360;
		}
		// 创建用于旋转的动画对象
		RotateAnimation anim = new RotateAnimation(fromDegrees, toDegrees,
				mivArrow.getWidth() / 2, mivArrow.getHeight() / 2);
		anim.setDuration(100);
		// 执行旋转动画
		mivArrow.startAnimation(anim);
	}

	/**
	 * 用于加载数据
	 * 
	 * @author yw
	 * 
	 */
	public interface OnRefreshListener {
		/**
		 * 加载数据
		 */
		void loadData();

		/**
		 * 检测加载是否完成
		 * 
		 * @return
		 */
		LoadStatus getLoadStatus();
	}

	// 隐藏下拉头
	public void finishRefresh(int refreshFlag) {
		new HideHeadLayoutThread(refreshFlag).start();
	}
	
	public void setOnRefreshListener(OnRefreshListener listener,T adapterView){
		mListner=listener;
		mAdapterView=adapterView;
		mAdapterView.setOnTouchListener(this);
	}
	
	Handler mHandler;
	/**
	 * 下拉刷新
	 */
	class RefreshingThread extends Thread{
	    final int REFRESHING=1;
	    final int REFRESH_FINISH=10;
	    
	    LoadStatus loadStatus;
	    
	    public RefreshingThread() {
	     // 设置该控件不接收按下操作
            mAdapterView.setPressed(false);
            // 设置该控件失去焦点
            mAdapterView.setFocusable(false);
            // 设置该控件不可触摸
            mAdapterView.setFocusableInTouchMode(false);
            
            mHandler=new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                    case REFRESHING:
                        refreshHeadView();// 显示正在刷新
                        break;
                    case REFRESH_FINISH://刷新结束
                        LoadStatus loadStatus=(LoadStatus) msg.obj;
                        if(loadStatus==null){
                            mCurrentStatus=STATUS_REFRESH_FAILURE;
                            finishRefresh(mCurrentStatus);// 隐藏下拉头
                            return ;
                        }
                        // 设置当前状态为刷新完成
                        switch (loadStatus) {
                        case SUCCESS:
                            mCurrentStatus=STATUS_REFRESH_SUCCESS;
                            break;
                        case FAILURE:
                            mCurrentStatus=STATUS_REFRESH_FAILURE;
                            break;
                        }
                        finishRefresh(mCurrentStatus);// 隐藏下拉头
                        break;
                    }
                }  
            };
        }
	    
	    @Override
	    public void run() {
	        mCurrentStatus = STATUS_REFRESHING;
            LoadStatus loadStatus = mListner.getLoadStatus();
            // 当数据未加载完时
            while (loadStatus == LoadStatus.LOADING) {
                SystemClock.sleep(10);
                loadStatus = mListner.getLoadStatus();
                Message msg=Message.obtain();
                msg.what=REFRESHING;
                msg.obj=loadStatus;
                mHandler.sendMessage(msg);
//                Log.i("main","刷新中...");
            }
            Message msg=Message.obtain();
            msg.what=REFRESH_FINISH;
            msg.obj=loadStatus;
            mHandler.sendMessage(msg);
	    }
	}
	
	   /**
     * 隐藏下拉头
     * @author yao
     *
     */
    class HideHeadLayoutThread extends Thread{
        final int REFRESHING=0;
        final int REFRESH_FINISH=1;
        int refreshFlag;
        Handler handler;
        
        public HideHeadLayoutThread(int refreshFlag) {
            this.refreshFlag = refreshFlag;
            // 设置该控件不接收按下操作
            mAdapterView.setPressed(false);
            // 设置该控件失去焦点
            mAdapterView.setFocusable(false);
            // 设置该控件不可触摸
            mAdapterView.setFocusableInTouchMode(false);
            refreshHeadView();
            
            handler=new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                    case REFRESHING:
                        mParams.topMargin = msg.arg1;
                        mHeadLayout.setLayoutParams(mParams);
                        break;
                    case REFRESH_FINISH:
                        mHeadLayout.setVisibility(View.GONE);
                        break;
                    }
                }
            };
        }
        
        @Override
        public void run() {
            int topMargin = mParams.topMargin;
            while (topMargin > mHeadHeight) {
                if(refreshFlag==STATUS_NOT_REFRESH){
                    topMargin -= SCROLL_SPEED;
                }else{
                    topMargin-=SCROLL_SPEED/3;
                }
                SystemClock.sleep(15);
                Message msg=Message.obtain();
                msg.arg1=topMargin;
                msg.what=REFRESHING;
                handler.sendMessage(msg);
            }
            Message msg=Message.obtain();
            msg.what=REFRESH_FINISH;
            handler.sendMessage(msg);
        }
    }
    
}
