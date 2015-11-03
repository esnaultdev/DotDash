package net.aohayo.dotdash;

import android.media.AudioTrack;
import android.util.Log;

public class ToneManager implements AudioTrack.OnPlaybackPositionUpdateListener{
    private ToneGenerator generator;

    public ToneManager(int frequency) {
        generator = new ToneGenerator(this, frequency);
        generator.start();
    }

    public void release() {
        generator.finish();
    }

    public void setFrequency(int frequency) {
        generator.setFrequency(frequency);
    }

    public void startTone() {
        generator.startTone();
    }

    public void stopTone() {
        generator.stopTone();
    }

    @Override
    public void onMarkerReached(AudioTrack audioTrack) {
        generator.updateBuffer();
    }

    @Override
    public void onPeriodicNotification(AudioTrack audioTrack) {
        generator.updateBuffer();
    }
}
