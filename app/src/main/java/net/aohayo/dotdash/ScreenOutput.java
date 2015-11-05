package net.aohayo.dotdash;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.View;

public class ScreenOutput implements MorseOutput {
    View outputView;
    Context context;
    CountDownTimer timer;

    public ScreenOutput(Context context, View view) {
        this.context = context;
        outputView = view;
    }

    @Override
    public void start() {
        outputView.setBackgroundColor(context.getResources().getColor(R.color.screenOutputOn));
    }

    @Override
    public void start(int duration) {
        outputView.setBackgroundColor(context.getResources().getColor(R.color.screenOutputOn));
        timer = new CountDownTimer(duration, -1) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                outputView.setBackgroundColor(context.getResources().getColor(R.color.screenOutputOff));
            }
        }.start();
    }

    @Override
    public void stop() {
        outputView.setBackgroundColor(context.getResources().getColor(R.color.screenOutputOff));
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
