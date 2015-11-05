package net.aohayo.dotdash;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class ContinuousToneGenerator extends Thread implements AudioTrack.OnPlaybackPositionUpdateListener{
    // Code inspired by http://stackoverflow.com/a/3731075

    private static final int SAMPLE_RATE = 8000;
    private static final int NB_EASING_PERIODS = 8;
    private static final double FRAME_DROP_TOLERANCE = 0.1;
    private static final int MIN_FRAME_BUFFER_FACTOR = 16;

    private Handler handler;

    private AudioTrack audioTrack;
    private short soundBuffer[];
    private short fadeInBuffer[];
    private short fadeOutBuffer[];
    private int frequency;

    public ContinuousToneGenerator(int frequency) {
        this.frequency = frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    @Override
    public void onMarkerReached(AudioTrack audioTrack) {
        updateBuffer();
    }

    @Override
    public void onPeriodicNotification(AudioTrack audioTrack) {
        updateBuffer();
    }

    public void startTone() {
        handler.removeMessages(ToneGeneratorHandler.UPDATE_BUFFER_MESSAGE);
        Message msg = Message.obtain();
        msg.what = ToneGeneratorHandler.START_TONE_MESSAGE;
        handler.sendMessage(msg);
        // Log.d("ToneManager", "StartToneMessage sent");
    }

    public void updateBuffer() {
        Message msg = Message.obtain();
        msg.what = ToneGeneratorHandler.UPDATE_BUFFER_MESSAGE;
        handler.sendMessage(msg);
        // Log.d("ToneManager", "UpdateBufferMessage sent");
    }

    public void stopTone() {
        handler.removeMessages(ToneGeneratorHandler.UPDATE_BUFFER_MESSAGE);
        Message msg = Message.obtain();
        msg.what = ToneGeneratorHandler.STOP_TONE_MESSAGE;
        handler.sendMessage(msg);
        // Log.d("ToneManager", "StopToneMessage sent");
    }

    public void finish() {
        Looper looper = Looper.myLooper();
        if (looper != null) {
            looper.quit();
        }
    }

    @Override
    public void run() {
        preExecute();
        Looper.prepare();
        handler = new ToneGeneratorHandler();
        Looper.loop();
        postExecute();
    }

    private void preExecute() {
        createBuffers();
        generateSounds();

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, soundBuffer.length * Short.SIZE / 8, AudioTrack.MODE_STREAM);
        audioTrack.setPlaybackPositionUpdateListener(this);
    }

    private void createBuffers() {
        int nbSamplesMin = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT) / (Short.SIZE / 8);
        double nbSamplesPerPeriod = (double) SAMPLE_RATE / frequency;
        int nbSamplesSoundBuffer = nbSamplesMin * MIN_FRAME_BUFFER_FACTOR;
        double nbPeriods = nbSamplesSoundBuffer / nbSamplesPerPeriod;
        while (nbPeriods - Math.floor(nbPeriods) > FRAME_DROP_TOLERANCE) {
            nbSamplesSoundBuffer++;
            nbPeriods = nbSamplesSoundBuffer / nbSamplesPerPeriod;
        }

        int fadeBufferSize = NB_EASING_PERIODS * SAMPLE_RATE / frequency;

        soundBuffer = new short[nbSamplesSoundBuffer];
        fadeInBuffer = new short[fadeBufferSize];
        fadeOutBuffer = new short[fadeBufferSize];
    }

    private void generateSounds() {
        double phase;
        for (int i = 0; i < soundBuffer.length; i++) {
            phase = 2 * Math.PI * i * frequency / SAMPLE_RATE;
            soundBuffer[i] = (short) ((Math.sin(phase) * Short.MAX_VALUE)); // scale to maximum amplitude
        }
        for (int i = 0; i < fadeInBuffer.length; i++) {
            phase = 2 * Math.PI * i* frequency / SAMPLE_RATE;
            short val = (short) ((Math.sin(phase) * Short.MAX_VALUE));
            fadeInBuffer[i] = (short) (val * easing(i, fadeInBuffer.length, true));
            fadeOutBuffer[i] = (short) (val * easing(i, fadeOutBuffer.length, false));
        }
    }

    private void postExecute() {
        audioTrack.pause();
        audioTrack.release();
    }

    /**
     * Generate an easing coefficient
     * @param currentFrame The current frame
     * @param frameMax The frame at which the final value is reached
     * @param increasing If true, goes from 0 to 1, goes from 1 to 0 otherwise
     * @return A coefficient between 0 and 1
     */
    private double easing(int currentFrame, int frameMax, boolean increasing) {
        if (currentFrame > frameMax) {
            return increasing ? 1.0 : 0.0;
        }
        double t = (double) (currentFrame) / frameMax;
        if (increasing) {
            return t;
        } else {
            return 1 - t;
        }
    }

    private class ToneGeneratorHandler extends Handler {
        private static final int START_TONE_MESSAGE = 1;
        private static final int UPDATE_BUFFER_MESSAGE = 2;
        private static final int STOP_TONE_MESSAGE = 3;

        @Override
        public void handleMessage(Message msg) {
            int currentFrame;
            switch (msg.what) {
                case START_TONE_MESSAGE:
                    if (audioTrack.getState() != AudioTrack.PLAYSTATE_STOPPED) {
                        audioTrack.pause();
                        audioTrack.flush();
                    }
                    audioTrack.write(fadeInBuffer, 0, fadeInBuffer.length);
                    audioTrack.write(soundBuffer, 0, soundBuffer.length);
                    audioTrack.play();
                    audioTrack.setNotificationMarkerPosition(soundBuffer.length/2);
                    break;

                case UPDATE_BUFFER_MESSAGE:
                    currentFrame = audioTrack.getPlaybackHeadPosition();
                    audioTrack.setNotificationMarkerPosition(currentFrame + soundBuffer.length);
                    audioTrack.write(soundBuffer, 0, soundBuffer.length);
                    break;

                case STOP_TONE_MESSAGE:
                    currentFrame = audioTrack.getPlaybackHeadPosition();

                    // Finish the current period
                    if (currentFrame < fadeInBuffer.length) {
                        audioTrack.pause();
                        audioTrack.flush();
                        audioTrack.write(fadeInBuffer, currentFrame, fadeInBuffer.length - currentFrame);
                    } else {
                        int frameInBuffer = (currentFrame - fadeInBuffer.length) % soundBuffer.length;
                        double nbSamplesPerPeriod = (double) SAMPLE_RATE / frequency;
                        int currentPeriod = (int) (frameInBuffer / nbSamplesPerPeriod);
                        int nextPeriodEndFrame = (int) (nbSamplesPerPeriod * (currentPeriod + 1));
                        audioTrack.pause();
                        audioTrack.flush();
                        audioTrack.write(soundBuffer, frameInBuffer, nextPeriodEndFrame - frameInBuffer);
                    }
                    audioTrack.write(fadeOutBuffer, 0, fadeOutBuffer.length);
                    audioTrack.play();
                    audioTrack.setNotificationMarkerPosition(0);
                    audioTrack.stop();
                    break;
                default:
                    break;
            }
        }
    }
}