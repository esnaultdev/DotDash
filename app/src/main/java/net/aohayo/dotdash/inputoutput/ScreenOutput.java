package net.aohayo.dotdash.inputoutput;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.View;

import net.aohayo.dotdash.R;

public class ScreenOutput extends MorseOutput {
    private View outputView;
    private int colorOn;
    private int colorOff;

    @SuppressLint("NewApi")
    public ScreenOutput(Context context, View view) {
        outputView = view;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            colorOn = context.getResources().getColor(R.color.screenOutputOn, context.getTheme());
            colorOff = context.getResources().getColor(R.color.screenOutputOff, context.getTheme());
        } else {
            colorOn = context.getResources().getColor(R.color.screenOutputOn);
            colorOff = context.getResources().getColor(R.color.screenOutputOff);
        }
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
                outputView.setBackgroundColor(colorOn);
            }
        });
    }

    private void screenOff() {
        outputView.post(new Runnable() {
            @Override
            public void run() {
                outputView.setBackgroundColor(colorOff);
            }
        });
    }
}
