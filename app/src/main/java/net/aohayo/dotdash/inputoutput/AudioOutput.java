package net.aohayo.dotdash.inputoutput;

import android.media.AudioManager;
import android.media.ToneGenerator;

public class AudioOutput extends MorseOutput {
    private ToneGenerator generator;

    public AudioOutput() {
        generator = new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
    }

    @Override
    public void start() {
        generator.startTone(ToneGenerator.TONE_CDMA_DIAL_TONE_LITE);
    }

    @Override
    public void stop() {
        generator.stopTone();
    }
}
