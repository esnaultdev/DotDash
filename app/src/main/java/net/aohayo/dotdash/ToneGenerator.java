package net.aohayo.dotdash;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class ToneGenerator extends Thread {
    // Code inspired by http://stackoverflow.com/a/3731075

    private static final int SAMPLE_RATE = 44100;
    private static final int EASING_DURATION = 400;// frames

    private Handler handler;
    private ToneManager toneManager;
    private Status status;

    private AudioTrack audioTrack;
    private int bufferSize;
    private byte generatedSound[];
    private double phase;
    private int frequency;

    public ToneGenerator(ToneManager toneManager, int frequency) {
        this.toneManager = toneManager;
        this.frequency = frequency;
        status = Status.STOPPED;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void startTone() {
        Message msg = Message.obtain();
        msg.obj = ToneGeneratorHandler.START_TONE_MESSAGE_STRING;
        handler.sendMessage(msg);
    }

    public void updateBuffer() {
        Message msg = Message.obtain();
        msg.obj = ToneGeneratorHandler.UPDATE_BUFFER_MESSAGE_STRING;
        handler.sendMessage(msg);
    }

    public void stopTone() {
        Message msg = Message.obtain();
        msg.obj = ToneGeneratorHandler.STOP_TONE_MESSAGE_STRING;
        handler.sendMessage(msg);
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
        bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        generatedSound = new byte[bufferSize];
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
        audioTrack.setPlaybackPositionUpdateListener(toneManager);
    }

    private void generateSound(boolean fadeIn, boolean fadeOut) {
        for (int i = 0; i < bufferSize/2; i++) {
            phase += 2 * Math.PI / (SAMPLE_RATE / frequency);
            // scale to maximum amplitude
            short val = (short) ((Math.sin(phase) * 32767));
            if (fadeIn) {
                val *= easeOutCubic(i, EASING_DURATION, false);
            } else if (fadeOut) {
                val *= easeOutCubic(i, EASING_DURATION, true);
            }
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSound[2*i] = (byte) (val & 0x00ff);
            generatedSound[2*i + 1] = (byte) ((val & 0xff00) >>> 8);
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
     * @param reversed If true, y-axis symmetrical of the classical easeOutCubic
     * @return A coefficient between 0 and 1
     */
    private double easeOutCubic(int currentFrame, int frameMax, boolean reversed) {
        if (currentFrame > frameMax) {
            return reversed ? 0.0 : 1.0;
        }
        double t = (double) (currentFrame) / frameMax;
        if (!reversed) {
            return 1 - (1-t)*(1-t)*(1-t);
        } else {
            return 1 - t*t*t;
        }
    }

    private class ToneGeneratorHandler extends Handler {
        private static final String START_TONE_MESSAGE_STRING = "startTone";
        private static final String UPDATE_BUFFER_MESSAGE_STRING = "updateBuffer";
        private static final String STOP_TONE_MESSAGE_STRING = "stopTone";

        @Override
        public void handleMessage(Message msg) {
            String messageString = (String) msg.obj;
            switch (messageString) {
                case START_TONE_MESSAGE_STRING:
                    audioTrack.pause();
                    audioTrack.flush();
                    phase = 0.0;
                    audioTrack.setPositionNotificationPeriod(bufferSize/3);
                    generateSound(true, false);
                    audioTrack.write(generatedSound, 0, bufferSize);
                    audioTrack.play();

                    status = Status.PLAYING;
                    break;

                case UPDATE_BUFFER_MESSAGE_STRING:
                    Log.d("ToneGenerator", "UpdateBuffer frame: " + audioTrack.getPlaybackHeadPosition() + ", bufferSize: " + bufferSize);
                    if (status == Status.STOPPING) {
                        audioTrack.pause();
                        audioTrack.flush();
                        status = Status.STOPPED;
                    } else {
                        generateSound(false, false);
                        audioTrack.write(generatedSound, 0, bufferSize);
                    }
                    break;

                case STOP_TONE_MESSAGE_STRING:
                    audioTrack.pause();
                    audioTrack.flush();
                    audioTrack.setPositionNotificationPeriod(0);
                    int currentFrame = audioTrack.getPlaybackHeadPosition();
                    phase = 2 * Math.PI * currentFrame / (SAMPLE_RATE / frequency);
                    generateSound(false, true);
                    audioTrack.write(generatedSound, 0, bufferSize);
                    audioTrack.play();

                    status = Status.STOPPING;
                    break;
            }
        }
    }

    private enum Status {
        PLAYING,
        STOPPING,
        STOPPED
    }
}