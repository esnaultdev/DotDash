package net.aohayo.dotdash;

import android.media.AudioManager;
import android.media.ToneGenerator;

public class AudioOutput extends MorseOutput {
    private ToneGenerator generator;

    public AudioOutput() {
        generator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
    }

    @Override
    public void start() {
        generator.startTone(ToneGenerator.TONE_CDMA_DIAL_TONE_LITE);
    }

    @Override
    public void start(int duration) {
        generator.startTone(ToneGenerator.TONE_CDMA_DIAL_TONE_LITE, duration);
    }

    @Override
    public void stop() {
        generator.stopTone();
    }
}
