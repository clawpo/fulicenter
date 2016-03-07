package cn.ucai.fulicenter.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import cn.ucai.fulicenter.R;

/**
 * Created by ucai001 on 2016/3/7.
 */
public class FragmentUtils {
    public static void startFragment(FragmentActivity context, Fragment fragment){
        Log.i("main","FragmentUtils.............");
        FragmentManager manager = context.getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.fragment_container,fragment).commit();
    }
}
