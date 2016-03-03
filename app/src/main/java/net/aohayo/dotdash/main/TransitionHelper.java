package net.aohayo.dotdash.main;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.transition.Transition;
import android.transition.TransitionInflater;

public class TransitionHelper {
    public static void setEnterWindowAnimations(Activity activity, int transitionId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition transition = TransitionInflater.from(activity).inflateTransition(transitionId);
            activity.getWindow().setEnterTransition(transition);
        }
    }

    public static void setExitWindowAnimations(Activity activity, int transitionId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition transition = TransitionInflater.from(activity).inflateTransition(transitionId);
            activity.getWindow().setExitTransition(transition);
        }
    }

    public static void startActivityTransition(Activity activity, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation(activity).toBundle());
        } else {
            activity.startActivity(intent);
        }
    }
}
