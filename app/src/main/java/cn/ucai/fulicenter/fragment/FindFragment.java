package cn.ucai.fulicenter.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import cn.ucai.fulicenter.R;

public class FindFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View layout=inflater.inflate(R.layout.fragment_find, container, false);
        setListener(layout);
        return layout;
    }

    private void setListener(View layout) {
        setNearPeopleClickListener(layout);
        setScanQRCodeClickListener(layout);
    }

    /** 扫描二维码*/
    private void setScanQRCodeClickListener(View layout) {
        layout.findViewById(R.id.layoutScanQRCode).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                
            }
        });
    }

    /** 附近人*/
    private void setNearPeopleClickListener(View layout) {
    }
}
