package cn.ucai.fulicenter.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.CategoryChildActivity;
import cn.ucai.fulicenter.bean.CategoryChildBean;
import cn.ucai.fulicenter.bean.CategoryGroupBean;
import cn.ucai.fulicenter.utils.ImageLoader;
import cn.ucai.fulicenter.utils.ImageLoader.OnImageLoadListener;
import cn.ucai.fulicenter.utils.NetUtil;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by ucai001 on 2016/3/11.
 */
public class CategoryFragment extends Fragment {
    Context mContext;
    ArrayList<CategoryGroupBean> mGroupList;
    ArrayList<ArrayList<CategoryChildBean>> mChildList;
    ExpandableListView melvCategory;
    
    CategoryAdapter mAdapter;
    /** 列表项右侧显示展开/收缩的view*/
    ImageView mivGroupIndicator;
    
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext=getActivity();
        View layout=View.inflate(getActivity(), R.layout.fragment_category, null);
        initView(layout);
        initData();
        setListener();
        return layout;
    }
    
    private void setListener() {
        setCategoryGroupExpandOnListener();
        setCategoryGroupExpandOffListener();
        setCategoryGroupClickListener();
    }

    /** 设置分类列表项大类被点击的事件监听*/
    private void setCategoryGroupClickListener() {
        melvCategory.setOnGroupClickListener(new OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                    int groupPosition, long id) {
                mivGroupIndicator=(ImageView) v.findViewById(R.id.ivIndicator);
                return false;
            }
        });
    }

    /**
     * 设置大类列表项收缩事件监听
     */
    private void setCategoryGroupExpandOffListener() {
        melvCategory.setOnGroupCollapseListener(new OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                mivGroupIndicator.setImageResource(R.drawable.expand_on);
            }
        });
    }

    /**
     * 列表项展开的事件监听
     */
    private void setCategoryGroupExpandOnListener() {
        melvCategory.setOnGroupExpandListener(new OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                mivGroupIndicator.setImageResource(R.drawable.expand_off);
            }
        });
    }

    private void initData() {
        mGroupList=new ArrayList<CategoryGroupBean>();
        mChildList=new ArrayList<ArrayList<CategoryChildBean>>();
        new DownloadCategoryTask(mContext, mGroupList, mChildList).execute();
    }

    private void initView(View layout) {
        melvCategory=(ExpandableListView) layout.findViewById(R.id.elvCategory);
        mGroupList=new ArrayList<CategoryGroupBean>();
        mChildList=new ArrayList<ArrayList<CategoryChildBean>>();
        mAdapter=new CategoryAdapter(mContext, mGroupList, mChildList);
        melvCategory.setAdapter(mAdapter);
    }

    class CategoryAdapter extends BaseExpandableListAdapter{
        Context context;
        ArrayList<CategoryGroupBean> groupList;
        ArrayList<ArrayList<CategoryChildBean>> childList;
        ImageLoader imageLoader;

        public CategoryAdapter(Context context, ArrayList<CategoryGroupBean> groupList, ArrayList<ArrayList<CategoryChildBean>> childList) {
            super();
            this.context = context;
            this.groupList = groupList;
            this.childList = childList;
            imageLoader = ImageLoader.getInstance(context);
        }

        @Override
        public int getGroupCount() {
            return groupList==null?0:groupList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return childList==null||childList.get(groupPosition)==null?0:childList.get(groupPosition).size();
        }

        @Override
        public CategoryGroupBean getGroup(int groupPosition) {
            return groupList.get(groupPosition);
        }

        @Override
        public CategoryChildBean getChild(int groupPosition, int childPosition) {
            return childList.get(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                View layout, final ViewGroup parent) {
            ViewGroupHolder holder=null;
            if(layout==null){
                layout=View.inflate(context, R.layout.item_category_group, null);
                holder=new ViewGroupHolder();
                holder.ivIndicator=(ImageView) layout.findViewById(R.id.ivIndicator);
                holder.ivThumb=(ImageView) layout.findViewById(R.id.ivGroupThumb);
                holder.tvGroupName=(TextView) layout.findViewById(R.id.tvGroupName);
                layout.setTag(holder);
            }else{
                holder=(ViewGroupHolder) layout.getTag();
            }
            CategoryGroupBean group = getGroup(groupPosition);
            holder.tvGroupName.setText(group.getName());
            String imgUrl=group.getImageUrl();
            String url= I.DOWNLOAD_DOWNLOAD_CATEGORY_GROUP_IMAGE_URL+imgUrl;
            holder.ivThumb.setTag(url);
            String imgName="images/"+imgUrl;
            Bitmap bitmap = imageLoader.displayImage(url, imgName, Utils.dp2px(context, 66),
                    Utils.dp2px(context, 44), new OnImageLoadListener() {
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
            if(bitmap==null){
                holder.ivThumb.setImageResource(R.drawable.nopic);
            }else{
                holder.ivThumb.setImageBitmap(bitmap);
            }
            return layout;
        }

        class ViewGroupHolder{
            ImageView ivIndicator;
            ImageView ivThumb;
            TextView tvGroupName;
        }
        
        @Override
        public View getChildView(final int groupPosition, int childPosition,
                boolean isLastChild, View layout, final ViewGroup parent) {
            ViewChildHolder holder=null;
            if(layout==null){
                layout=View.inflate(context, R.layout.item_cateogry_child, null);
                holder=new ViewChildHolder();
                holder.layoutItem=(RelativeLayout) layout.findViewById(R.id.layout_category_child);
                holder.ivThumb=(ImageView) layout.findViewById(R.id.ivCategoryChildThumb);
                holder.tvChildName=(TextView) layout.findViewById(R.id.tvCategoryChildName);
                layout.setTag(holder);
            }else{
                holder=(ViewChildHolder) layout.getTag();
            }
            final CategoryChildBean child = getChild(groupPosition, childPosition);
            String name=child.getName();
            holder.tvChildName.setText(name);
            
            String imgUrl=child.getImageUrl();
            String url=I.DOWNLOAD_DOWNLOAD_CATEGORY_CHILD_IMAGE_URL+imgUrl;
            String imgName="images/"+imgUrl;
            Bitmap bitmap = imageLoader.displayImage(url, imgName, 
                    Utils.dp2px(context, 44), Utils.dp2px(context, 44), new OnImageLoadListener() {
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
            if(bitmap==null){
                holder.ivThumb.setImageResource(R.drawable.nopic);
            }else{
                holder.ivThumb.setImageBitmap(bitmap);
            }
            
            holder.layoutItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(mContext, CategoryChildActivity.class);
                    intent.putExtra(I.CategoryChild.CAT_ID, child.getId());
                    ArrayList<CategoryChildBean> children = childList.get(groupPosition);
                    intent.putExtra("children", children);
                    intent.putExtra(I.CategoryGroup.NAME, getGroup(groupPosition).getName());
                    mContext.startActivity(intent);
                }
            });
            return layout;
        }

        class ViewChildHolder{
            RelativeLayout layoutItem;
            ImageView ivThumb;
            TextView tvChildName;
        }
		

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
        
        public void addItems(ArrayList<CategoryGroupBean> groupList,
            ArrayList<ArrayList<CategoryChildBean>> childList){
            this.groupList.addAll(groupList);
            this.childList.addAll(childList);
            notifyDataSetChanged();
        }
    }
    
    /**
     * 下载分类的大类和小类商品的数据
     * @author yao
     *
     */
    class DownloadCategoryTask extends AsyncTask<Void, Void, Boolean> {
        Context context;
        ArrayList<CategoryGroupBean> groupList;
        ArrayList<ArrayList<CategoryChildBean>> childList;
        ProgressDialog dialog;
        
        public DownloadCategoryTask(Context context,
                ArrayList<CategoryGroupBean> groupList,
                ArrayList<ArrayList<CategoryChildBean>> childList) {
            super();
            this.context = context;
            this.groupList = groupList;
            this.childList = childList;
            dialog=new ProgressDialog(context);
        }
        @Override
        protected void onPreExecute() {
            dialog=new ProgressDialog(context);
            dialog.setTitle("下载商品分类信息");
            dialog.setMessage("加载数据...");
            dialog.show();
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            groupList= NetUtil.findCategoryGroup();
            for(int i=0;i<groupList.size();i++){
                CategoryGroupBean group = groupList.get(i);
                ArrayList<CategoryChildBean> list=NetUtil.findCategoryChild(group.getId(),0,20);
                childList.add(list);
            }
            return groupList!=null;
        }
        
        @Override
        protected void onPostExecute(Boolean result) {
            dialog.dismiss();
            if(result){
                mAdapter.addItems(groupList, childList);
            }else{
                Utils.showToast(context, "分类列表下载失败", Toast.LENGTH_LONG);
            }
        }
    }
    
}
