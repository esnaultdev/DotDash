package net.aohayo.dotdash.inputoutput;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class ToneGenerator {
    // Code inspired by http://stackoverflow.com/a/3731075

    private static final int duration = 3; // seconds
    private static final int sampleRate = 8000;
    private int numSamples;
    private byte generatedSnd[];
    private byte silenceSnd[];
    private int toneFrequency;
    private AudioTrack audioTrack;

    public ToneGenerator(int frequency) {
        toneFrequency = frequency;
        generateTone();
        init();
    }

    public void setFrequency(int frequency) {
        toneFrequency = frequency;
        init();
        generateTone();
    }

    public int getFrequency() {
        return toneFrequency;
    }

    private void generateTone() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                generatedSnd = new byte[2*numSamples];
                silenceSnd = new byte[2*numSamples];

                // fill out the array
                final double sample[] = new double[numSamples];
                for (int i = 0; i < numSamples; ++i) {
                    sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/toneFrequency));
                }

                // convert to 16 bit pcm sound array
                // assumes the sample buffer is normalised.
                for (int i = 0; i < sample.length; i++) {
                    final double dVal = sample[i];
                    // scale to maximum amplitude
                    final short val = (short) ((dVal * 32767));
                    // in 16 bit wav PCM, first byte is the low order byte
                    generatedSnd[2*i] = (byte) (val & 0x00ff);
                    generatedSnd[2*i + 1] = (byte) ((val & 0xff00) >>> 8);
                    silenceSnd[2*i] = silenceSnd[2*i + 1] = 0;
                }
            }
        });
        thread.start();
    }

    public void startTone() {
        if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
            audioTrack.flush();
        }
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.setLoopPoints(0, generatedSnd.length / 4, -1);
        audioTrack.play();
    }

    public void stopTone() {
        if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
            return;
        }
        int position = audioTrack.getPlaybackHeadPosition();
        audioTrack.stop();
        audioTrack.flush();
        int factor =  sampleRate / toneFrequency;
        int endPosition = factor * (position/factor + 1);
        audioTrack.write(silenceSnd, 0, silenceSnd.length);
        audioTrack.write(generatedSnd, position * 2, (endPosition - position)*2);

        audioTrack.setLoopPoints(0, 0, 0);
        audioTrack.play();
    }

    public void finish() {
        if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
            return;
        }
        audioTrack.stop();
        audioTrack.release();
    }

    public void resume() {
        init();
    }

    private void init() {
        numSamples = duration * toneFrequency*(sampleRate/toneFrequency);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, numSamples,
                AudioTrack.MODE_STATIC);
    }
}
