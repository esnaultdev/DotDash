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
        Log.d("ToneManager", "StartTone signal");
        generator.startTone();
    }

    public void stopTone() {
        Log.d("ToneManager", "StopTone signal");
        generator.stopTone();
    }

    @Override
    public void onMarkerReached(AudioTrack audioTrack) {

    }

    @Override
    public void onPeriodicNotification(AudioTrack audioTrack) {
        Log.d("ToneManager", "UpdateBuffer signal");
        generator.updateBuffer();
    }
}
