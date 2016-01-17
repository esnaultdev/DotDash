package net.aohayo.dotdash.inputoutput;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.View;

import net.aohayo.dotdash.R;

public class ScreenOutput extends MorseOutput {
    View outputView;
    Context context;

    public ScreenOutput(Context context, View view) {
        this.context = context;
        outputView = view;
    }

    @Override
    public void init() {
        screenOff();
    }

    @Override
    public void finish() {
        screenOn();
    }

    @Override
    public void resume() {
        screenOff();
    }

    @Override
    public void start() {
        screenOn();
    }

    @Override
    public void stop() {
        screenOff();
    }

    private void screenOn() {
        outputView.post(new Runnable() {
            @Override
            public void run() {
                outputView.setBackgroundColor(context.getResources().getColor(R.color.screenOutputOn));
            }
        });
    }

    private void screenOff() {
        outputView.post(new Runnable() {
            @Override
            public void run() {
                outputView.setBackgroundColor(context.getResources().getColor(R.color.screenOutputOff));
            }
        });
    }
}
