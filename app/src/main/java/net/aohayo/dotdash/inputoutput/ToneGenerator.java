package net.aohayo.dotdash.inputoutput;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;

public class ToneGenerator {
    // Code inspired by http://stackoverflow.com/a/3731075

    private static final int SAMPLE_RATE = 8400;
    private static final int NB_FADE_PERIODS = 4;
    private static final int NB_NORMAL_PERIODS = 4; // > 1 because of the loop implementation of the AudioTrack

    private int numSamples;
    private int numSamplesPerPeriod;
    private int toneFrequency;
    private AudioTrack audioTrack;
    private boolean ready;
    private boolean shouldStartWhenReady;

    public ToneGenerator(int frequency) {
        toneFrequency = frequency;
        init();
    }

    public void setFrequency(int frequency) {
        toneFrequency = frequency;
        init();
    }

    public int getFrequency() {
        return toneFrequency;
    }

    public void startTone() {
        if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
        }
        if (!ready) {
            shouldStartWhenReady = true;
            return;
        }
        audioTrack.setLoopPoints(numSamplesPerPeriod*NB_FADE_PERIODS,
                numSamplesPerPeriod*(NB_FADE_PERIODS+NB_NORMAL_PERIODS), -1);
        audioTrack.play();
    }

    public void stopTone() {
        if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
            return;
        }
        if (!ready) {
            shouldStartWhenReady = false;
            return;
        }
        audioTrack.pause();
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
        ready = false;
        shouldStartWhenReady = false;
        numSamplesPerPeriod = SAMPLE_RATE/toneFrequency;
        numSamples = (2*NB_FADE_PERIODS + NB_NORMAL_PERIODS) * numSamplesPerPeriod;
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, 2*numSamples,
                AudioTrack.MODE_STATIC);
        GenerationTask genTask = new GenerationTask();
        genTask.execute();
    }

    private class GenerationTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            byte[] generatedSnd = new byte[2*numSamples];

            // prepare the samples out the array
            final double sample[] = new double[numSamplesPerPeriod];
            for (int i = 0; i < numSamplesPerPeriod; i++) {
                sample[i] = Math.sin(2 * Math.PI * i / numSamplesPerPeriod);
            }

            // convert to 16 bit pcm sound array
            // assumes the sample buffer is normalised
            int offset = NB_FADE_PERIODS*numSamplesPerPeriod;
            int frame = 0;
            for (int i = 0; i < NB_NORMAL_PERIODS*numSamplesPerPeriod; i++) {
                // scale to maximum amplitude
                final short val = (short) ((sample[frame] * 32767));
                // in 16 bit wav PCM, first byte is the low order byte
                generatedSnd[2*(i+offset)] = (byte) (val & 0x00ff);
                generatedSnd[2*(i+offset) + 1] = (byte) ((val & 0xff00) >>> 8);

                if (++frame >= numSamplesPerPeriod) frame = 0;
            }

            // generate fade in/out sound
            int numFadeSamples = NB_FADE_PERIODS * numSamplesPerPeriod;
            offset = (NB_FADE_PERIODS + NB_NORMAL_PERIODS)*numSamplesPerPeriod;
            for (int i = 0; i < numFadeSamples; i++) {
                final double dVal = sample[frame];
                final double t = (double) i/numFadeSamples;
                final short fadeInVal = (short) (dVal*t*32767);
                final short fadeOutVal = (short) (dVal*(1-t)*32767);
                generatedSnd[2*i] = (byte) (fadeInVal & 0x00ff);
                generatedSnd[2*i + 1] = (byte) ((fadeInVal & 0xff00) >>> 8);
                generatedSnd[2*(i+offset)] = (byte) (fadeOutVal & 0x00ff);
                generatedSnd[2*(i+offset) + 1] = (byte) ((fadeOutVal & 0xff00) >>> 8);

                if (++frame >= numSamplesPerPeriod) frame = 0;
            }

            audioTrack.write(generatedSnd, 0, generatedSnd.length);
            ready = true;

            if (shouldStartWhenReady) {
                startTone();
            }
            return null;
        }
    }
}
