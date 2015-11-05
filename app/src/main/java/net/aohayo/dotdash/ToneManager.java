package net.aohayo.dotdash;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;

public class ToneManager {
    private ToneGenerator generator;

    public ToneManager() {
        generator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
    }

    public void startTone() {
        generator.startTone(ToneGenerator.TONE_CDMA_DIAL_TONE_LITE);
    }

    public void stopTone() {
        generator.stopTone();
    }
}
