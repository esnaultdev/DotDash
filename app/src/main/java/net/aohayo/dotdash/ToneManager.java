package net.aohayo.dotdash;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class ToneManager implements AudioTrack.OnPlaybackPositionUpdateListener{
    private int frequency;
    private Thread generatorThread;
    private ToneGenerator generatorRunnable;

    public ToneManager(int frequency) {
        this.frequency = frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getFrequency() {
        return frequency;
    }

    public void startTone() {
        generatorRunnable = new ToneGenerator(this);
        generatorThread = new Thread(generatorRunnable);
        generatorThread.start();
    }

    public void stopTone() {
        generatorRunnable.finish();
        generatorThread.interrupt();
    }

    @Override
    public void onMarkerReached(AudioTrack audioTrack) {
        generatorRunnable.resume();
        generatorThread.interrupt();
    }

    @Override
    public void onPeriodicNotification(AudioTrack audioTrack) {
        generatorRunnable.resume();
        generatorThread.interrupt();
    }

    private class ToneGenerator implements Runnable {
        // Code inspired by http://stackoverflow.com/a/3731075 and http://stackoverflow.com/a/6776463

        private static final int SAMPLE_RATE = 44100;
        private static final int BUFFER_UPDATE_FREQUENCY = 10;

        private final Object pauseLock;
        private boolean paused;
        private boolean finished;
        private ToneManager toneManager;

        private AudioTrack audioTrack;
        private int bufferSize;
        private byte generatedSound[];
        private double phase = 0.0;


        public ToneGenerator(ToneManager toneManager) {
            this.toneManager = toneManager;
            pauseLock = new Object();
            paused = false;
            finished = false;
        }

        @Override
        public void run() {
            preExecute();
            generateSound();
            postExecute();
        }

        private void preExecute() {
            bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            generatedSound = new byte[bufferSize];
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
            audioTrack.setPlaybackPositionUpdateListener(toneManager);
            audioTrack.play();
        }

        private void generateSound() {
            boolean firstIteration = true;
            while (!finished) {

                int numSamplesToGenerate;
                if (firstIteration) {
                    firstIteration = false;
                    numSamplesToGenerate = bufferSize;
                    audioTrack.setPositionNotificationPeriod(bufferSize / (BUFFER_UPDATE_FREQUENCY * 2));
                } else {
                    numSamplesToGenerate = bufferSize / BUFFER_UPDATE_FREQUENCY;
                }

                for (int i = 0; i < numSamplesToGenerate/2; i++) {
                    phase += 2 * Math.PI / (SAMPLE_RATE / frequency);
                    // scale to maximum amplitude
                    final short val = (short) ((Math.sin(phase) * 32767));
                    // in 16 bit wav PCM, first byte is the low order byte
                    generatedSound[2*i] = (byte) (val & 0x00ff);
                    generatedSound[2*i + 1] = (byte) ((val & 0xff00) >>> 8);
                }

                int currentMarkerFrame = audioTrack.getNotificationMarkerPosition();
                audioTrack.write(generatedSound, currentMarkerFrame, bufferSize / BUFFER_UPDATE_FREQUENCY);

                paused = true;
                synchronized (pauseLock) {
                    while (paused) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            // Thread resumed, nothing to do here
                        }
                    }
                }
            }
        }

        private void postExecute() {
            audioTrack.stop();
            audioTrack.release();
        }

        public void resume() {
            synchronized (pauseLock) {
                paused = false;
                pauseLock.notifyAll();
            }
        }

        public void finish() {
            synchronized (pauseLock) {
                paused = false;
                finished = true;
                pauseLock.notifyAll();
            }
        }
    }
}
