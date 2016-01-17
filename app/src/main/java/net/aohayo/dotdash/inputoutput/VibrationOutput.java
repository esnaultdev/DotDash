package net.aohayo.dotdash.inputoutput;

import android.content.Context;
import android.os.Vibrator;

public class VibrationOutput extends MorseOutput {
    private Vibrator vibrator;
    private long[] pattern = {0, 10000, 0};

    public VibrationOutput(Context context) {
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void start() {
        vibrator.vibrate(pattern, 0);
    }

    @Override
    public void stop() {
        vibrator.cancel();
    }

    public static boolean isAvailable(Context context) {
        return ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).hasVibrator();
    }
}
