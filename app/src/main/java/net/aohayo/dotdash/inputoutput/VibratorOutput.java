package net.aohayo.dotdash.inputoutput;

import android.content.Context;
import android.os.Vibrator;

public class VibratorOutput extends MorseOutput {
    private Vibrator vibrator;

    public VibratorOutput(Context context) {
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void start() {
        vibrator.vibrate(new long[]{1000}, 0);
    }

    @Override
    public void stop() {
        vibrator.cancel();
    }

    public static boolean isAvailable(Context context) {
        return ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).hasVibrator();
    }
}
