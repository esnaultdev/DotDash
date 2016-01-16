package net.aohayo.dotdash.inputoutput;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.aohayo.dotdash.R;
import net.aohayo.dotdash.main.SettingsActivity;

public class AudioOutput extends MorseOutput {
    private ToneGenerator generator;
    private Context context;

    public AudioOutput(Context context) {
        this.context = context;
        int frequency = getFrequencyFromPreferences(context);
        generator = new ToneGenerator(frequency);
    }

    @Override
    public void start() {
        generator.startTone();
    }

    @Override
    public void stop() {
        generator.stopTone();
    }

    @Override
    public void finish() {
        generator.finish();
    }

    @Override
    public void resume() {
        generator.resume();
        int frequency = getFrequencyFromPreferences(context);
        if (frequency != generator.getFrequency()) {
            generator.setFrequency(frequency);
        }
    }

    static private int getFrequencyFromPreferences(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String prefFrequency = sharedPref.getString(
                SettingsActivity.KEY_PREF_TONE_FREQUENCY,
                context.getResources().getString(R.string.pref_tone_frequency_default));
        return Integer.parseInt(prefFrequency);
    }
}
