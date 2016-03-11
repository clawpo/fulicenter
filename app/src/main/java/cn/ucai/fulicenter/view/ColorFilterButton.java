package cn.ucai.fulicenter.view;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.PopupWindow;
import android.widget.TextView;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.CategoryChildActivity;
import cn.ucai.fulicenter.bean.CategoryChildBean;
import cn.ucai.fulicenter.bean.CategoryGroupBean;
import cn.ucai.fulicenter.bean.ColorBean;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.utils.NetUtil;
import cn.ucai.fulicenter.utils.Utils;
/**
 * 自定义控件,显示颜色列表
 * @author yao
 */
public class ColorFilterButton extends Button {
    Context mContext;
    ColorFilterButton mbtnTop;
    PopupWindow mPopupWindow;
    GridView mgvColor;
    ColorFilterAdapter mAdapter;
    ArrayList<CategoryChildBean> mChildList;
    String mGroupName;
    
    public ColorFilterButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
        mbtnTop=this;
        initGridView();
    }

    private void initGridView() {
        mgvColor=new GridView(mContext);
        mgvColor.setColumnWidth(Utils.px2dp(mContext, 80));
        mgvColor.setNumColumns(GridView.AUTO_FIT);
        mgvColor.setHorizontalSpacing(Utils.px2dp(mContext, 3));
        mgvColor.setVerticalSpacing(Utils.px2dp(mContext, 3));
        mgvColor.setScrollingCacheEnabled(true);
        mgvColor.setCacheColorHint(0);
        mgvColor.setBackgroundColor(Color.TRANSPARENT);
        mgvColor.setPadding(3, 3, 3, 3);
    }

    class ColorFilterAdapter extends BaseAdapter{
        Context context;
        ArrayList<ColorBean> colorList;
        public ColorFilterAdapter(Context context,
                ArrayList<ColorBean> colorList) {
            super();
            this.context = context;
            this.colorList = colorList;
        }
        
        @Override
        public int getCount() {
            return colorList==null?0:colorList.size();
        }

        @Override
        public ColorBean getItem(int position) {
            return colorList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View layout, ViewGroup parent) {
            ViewHolder holder=null;
            if(layout==null){
                layout=View.inflate(context, R.layout.item_color_filter, null);
                holder=new ViewHolder();
                holder.layoutItem=(LinearLayout) layout.findViewById(R.id.layout_color_filter);
                holder.tvName=(TextView) layout.findViewById(R.id.tvName);
                layout.setTag(holder);
            }else{
                holder=(ViewHolder) layout.getTag();
            }
            final ColorBean color = getItem(position);
            String colorName=color.getColorName();
            if(colorName.length()>4){
                colorName=colorName.substring(0,3);
            }
            holder.tvName.setText(colorName);
            holder.layoutItem.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DownloadCatChildTask(context, color.getCatId()).execute();
                }
            });
            return layout;
        }
        
        class ViewHolder{
            LinearLayout layoutItem;
            TextView tvName;
        }
    }
    
    /**
     * 下载分类的大类和小类商品的数据
     * @author yao
     *
     */
    class DownloadCatChildTask extends AsyncTask<Void, Void, Boolean>{
        Context context;
        int catId;
        ArrayList<NewGoodBean> goodList;
        
        public DownloadCatChildTask(Context context,int catId) {
            super();
            this.context = context;
            this.catId=catId;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                goodList=NetUtil.findGoodsDetails(context, catId, 0, 20);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return goodList!=null&&goodList.size()>0;
        }
        
        @Override
        protected void onPostExecute(Boolean result) {
            if(result){
                Intent intent=new Intent(mContext, CategoryChildActivity.class);
                intent.putExtra(I.CategoryGroup.NAME, mGroupName);
                intent.putExtra(I.CategoryChild.CAT_ID, catId);
                intent.putExtra("children", mChildList);
                intent.putExtra("goodList", goodList);
                context.startActivity(intent);
                ((Activity)mContext).finish();
            }else{
                Utils.showToast(context, "分类列表下载失败", Toast.LENGTH_LONG);
            }
        }
    }
    
    public void setOnColorFilterClickListener(String groupName,ArrayList<CategoryChildBean> childList,
            final ArrayList<ColorBean> colorList){
        mChildList=childList;
        mGroupName=groupName;
        mbtnTop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPopupWindow!=null){
                    if(mPopupWindow.isShowing()){
                        mPopupWindow.dismiss();
                        mPopupWindow=null;
                    }
                }else{
                    initGridView();
                    mAdapter=new ColorFilterAdapter(mContext, colorList);
                    mgvColor.setAdapter(mAdapter);
                    
                    mPopupWindow=new PopupWindow();
                    mPopupWindow.setWidth(LayoutParams.MATCH_PARENT);
                    if(mAdapter.getCount()<16){
                        mPopupWindow.setHeight(LayoutParams.WRAP_CONTENT);
                    }else{
                        mPopupWindow.setHeight(Utils.px2dp(mContext, 200));
                    }
                    mPopupWindow.setBackgroundDrawable(new ColorDrawable(0xbb000000));
                    mPopupWindow.setFocusable(true);
                    mPopupWindow.setTouchable(true);
                    mPopupWindow.setOutsideTouchable(true);
                    mPopupWindow.setContentView(mgvColor);
                    mPopupWindow.showAsDropDown(mbtnTop);
                }
            }
        });
    }
}
