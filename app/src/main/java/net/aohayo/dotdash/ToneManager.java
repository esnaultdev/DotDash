package net.aohayo.dotdash;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class ToneManager implements AudioTrack.OnPlaybackPositionUpdateListener{
    private int frequency = 450;
    private Thread generatorThread;
    private ToneGenerator generatorRunnable;

    public ToneManager() {

    }

    public ToneManager(int frequency) {
        super();
        this.frequency = frequency;
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

        private Object pauseLock;
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
        }

        private void generateSound() {
            while(!finished) {
                for(int i = 0; i < bufferSize/2; i++){
                    phase += 2 * Math.PI / (SAMPLE_RATE / frequency);
                    // scale to maximum amplitude
                    final short val = (short) ((Math.sin(phase) * 32767));
                    // in 16 bit wav PCM, first byte is the low order byte
                    generatedSound[2*i] = (byte) (val & 0x00ff);
                    generatedSound[2*i + 1] = (byte) ((val & 0xff00) >>> 8);
                }

                int currentMarkerFrame = audioTrack.getNotificationMarkerPosition();
                if (currentMarkerFrame == 0) {
                    audioTrack.setPositionNotificationPeriod(bufferSize / 4);
                } else {
                    audioTrack.setPositionNotificationPeriod(bufferSize / 2);
                }
                audioTrack.write(generatedSound, currentMarkerFrame, bufferSize);
                audioTrack.play();

                paused = true;
                synchronized (pauseLock) {
                    while (paused) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
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
