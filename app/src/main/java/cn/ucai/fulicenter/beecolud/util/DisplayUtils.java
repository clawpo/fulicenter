/**
 * DisplayUtils.java
 * <p/>
 * Created by xuanzhui on 2015/9/10.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.ucai.fulicenter.beecolud.util;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.ucai.fulicenter.R;

public class DisplayUtils {

    public static void initBack(final Activity activity) {
        LinearLayout clickArea = (LinearLayout) activity.findViewById(R.id.backClickArea);

        clickArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.finish();
                }
            });
    }

    public static void initBackWithTitle(final Activity activity, String title) {
        TextView tvTitle = (TextView) activity.findViewById(R.id.tv_head_title);
        tvTitle.setText(title);
        initBack(activity);
    }
}
