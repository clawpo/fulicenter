package cn.ucai.fulicenter.view;

import android.view.View;
import android.view.ViewPropertyAnimator;

/**
 * Created by clawpo on 16/3/24.
 */
public class RotateUtil {
    public static void startRotateY(View view) {
        ViewPropertyAnimator animate;
        animate= view.animate();
        if (view.getRotationY() < 360) {
            animate.rotationY(360);
        } else {
            animate.rotationY(0);
        }
        animate.setDuration(1000);
        animate.start();
    }
}
