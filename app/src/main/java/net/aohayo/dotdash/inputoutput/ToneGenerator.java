package net.aohayo.dotdash.inputoutput;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class ToneGenerator {
    // Code inspired by http://stackoverflow.com/a/3731075

    private final int duration = 2; // seconds
    private final int sampleRate = 8000;
    private final int numSamples = duration * sampleRate;
    private final byte generatedSnd[] = new byte[2 * numSamples];
    private double toneFrequency;
    private AudioTrack audioTrack;


    public ToneGenerator(int frequency) {
        toneFrequency = frequency;
        generateTone();
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, numSamples,
                AudioTrack.MODE_STATIC);
    }

    private void generateTone() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                // fill out the array
                final double sample[] = new double[numSamples];
                for (int i = 0; i < numSamples; ++i) {
                    sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/toneFrequency));
                }

                // convert to 16 bit pcm sound array
                // assumes the sample buffer is normalised.
                int idx = 0;
                for (final double dVal : sample) {
                    // scale to maximum amplitude
                    final short val = (short) ((dVal * 32767));
                    // in 16 bit wav PCM, first byte is the low order byte
                    generatedSnd[idx++] = (byte) (val & 0x00ff);
                    generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

                }
            }
        });
        thread.start();
    }

    public void startTone() {
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.setLoopPoints(0, generatedSnd.length / 4, -1);
        audioTrack.play();
    }

    public void stopTone() {
        audioTrack.stop();
        audioTrack.flush();
    }
}
