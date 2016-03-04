package cn.ucai.fulicenter.fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.MainActivity;
import cn.ucai.fulicenter.adapter.GoodAdapter;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.task.DownloadGoodsTask;
import cn.ucai.fulicenter.utils.PullRefreshView;

/**
 * Created by ucai001 on 2016/3/3.
 */
public class NewGoodFragment extends Fragment {
    MainActivity mContext;

    ArrayList<NewGoodBean> mGoodList;
    GoodAdapter mAdaper;
    PullRefreshView<GridView> mprfvNewGood;
    GridView mgvNewGood;

    /**分页下载商品的页码*/
    int mPageId=0;
    /** 每页下载商品的数量*/
    final int PAGE_SIZE=10;

    DownloadGoodsTask mDownloadGoodsTask;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = (MainActivity) getActivity();
        View layout = View.inflate(mContext, R.layout.fragment_new_good,null);
        initView(layout);
        setListener(layout);

        return layout;
    }

    private void setListener(View layout) {

    }

    private void initView(View layout) {
        mprfvNewGood = (PullRefreshView<GridView>) layout.findViewById(R.id.prfvNewGood);
        mgvNewGood = (GridView) layout.findViewById(R.id.gvNewGood);
        mGoodList = new ArrayList<NewGoodBean>();
        mAdaper = new GoodAdapter(mContext,mGoodList, I.SORT_BY_ADDTIME_DESC);
        mgvNewGood.setAdapter(mAdaper);
    }
}
