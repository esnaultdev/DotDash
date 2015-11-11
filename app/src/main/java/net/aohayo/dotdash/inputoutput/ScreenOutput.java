package net.aohayo.dotdash.inputoutput;

import android.content.Context;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;

import net.aohayo.dotdash.R;

public class ScreenOutput extends MorseOutput {
    View outputView;
    Context context;
    CountDownTimer timer;

    public ScreenOutput(Context context, View view) {
        this.context = context;
        outputView = view;
    }

    @Override
    public void init() {
        outputView.setBackgroundColor(context.getResources().getColor(R.color.screenOutputOff));
    }

    @Override
    public void finish() {
        outputView.setBackgroundColor(context.getResources().getColor(R.color.screenOutputOn));
    }

    @Override
    public void start() {
        outputView.setBackgroundColor(context.getResources().getColor(R.color.screenOutputOn));
    }

    @Override
    public void stop() {
        outputView.setBackgroundColor(context.getResources().getColor(R.color.screenOutputOff));
        if (timer != null) {
            timer.cancel();
        }
    }
}
