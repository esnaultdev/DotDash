package net.aohayo.dotdash;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class ToneGenerator extends Thread {
    // Code inspired by http://stackoverflow.com/a/3731075

    private static final int SAMPLE_RATE = 8000;
    private static final int NB_EASING_PERIODS = 5;

    private Handler handler;
    private ToneManager toneManager;

    private AudioTrack audioTrack;
    private short soundBuffer[];
    private short fadeInBuffer[];
    private short fadeOutBuffer[];
    private int frequency;

    public ToneGenerator(ToneManager toneManager, int frequency) {
        this.toneManager = toneManager;
        this.frequency = frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void startTone() {
        // Log.d("ToneManager", "StartToneMessage sent");
        Message msg = Message.obtain();
        msg.obj = ToneGeneratorHandler.START_TONE_MESSAGE_STRING;
        handler.sendMessage(msg);
    }

    public void updateBuffer() {
        // Log.d("ToneManager", "UpdateBufferMessage sent");
        Message msg = Message.obtain();
        msg.obj = ToneGeneratorHandler.UPDATE_BUFFER_MESSAGE_STRING;
        handler.sendMessage(msg);
    }

    public void stopTone() {
        // Log.d("ToneManager", "StopToneMessage sent");
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
        int minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT) / (Short.SIZE / 8);
        double nbFramesPerPeriod = (double) SAMPLE_RATE / frequency;
        int i = (int) (minBufferSize / nbFramesPerPeriod) + 1;
        // TODO: Compute better buffer sizes for smooth transition
        while (nbFramesPerPeriod * i != (int)(nbFramesPerPeriod * i)) {
            i++;
        }
        int soundBufferSize = (int) (nbFramesPerPeriod * i) * 10;
        soundBuffer = new short[soundBufferSize];
        int fadeBufferSize = NB_EASING_PERIODS * SAMPLE_RATE / frequency;
        fadeInBuffer = new short[fadeBufferSize];
        fadeOutBuffer = new short[fadeBufferSize];
        generateSounds();

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, soundBufferSize * Short.SIZE / 8, AudioTrack.MODE_STREAM);
        audioTrack.setPlaybackPositionUpdateListener(toneManager);
    }

    private void generateSounds() {
        double phase = 0.0;
        for (int i = 0; i < soundBuffer.length; i++) {
            phase += 2 * Math.PI * frequency / SAMPLE_RATE;
            soundBuffer[i] = (short) ((Math.sin(phase) * Short.MAX_VALUE)); // scale to maximum amplitude
        }
        phase = 0.0;
        for (int i = 0; i < fadeInBuffer.length; i++) {
            phase += 2 * Math.PI *frequency / SAMPLE_RATE;
            short val = (short) ((Math.sin(phase) * Short.MAX_VALUE));
            fadeInBuffer[i] = (short) (val * easeOutCubic(i, fadeInBuffer.length, false));
            fadeOutBuffer[i] = (short) (val * easeOutCubic(i, fadeOutBuffer.length, true));
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
                    if (audioTrack.getState() != AudioTrack.PLAYSTATE_STOPPED) {
                        audioTrack.pause();
                        audioTrack.flush();
                    }
                    audioTrack.write(fadeInBuffer, 0, fadeInBuffer.length);
                    audioTrack.write(soundBuffer, 0, soundBuffer.length);
                    audioTrack.play();
                    audioTrack.setNotificationMarkerPosition(soundBuffer.length);
                    break;

                case UPDATE_BUFFER_MESSAGE_STRING:
                    int currentFrame = audioTrack.getPlaybackHeadPosition();
                    audioTrack.setNotificationMarkerPosition(currentFrame + soundBuffer.length);
                    audioTrack.write(soundBuffer, 0, soundBuffer.length);
                    break;

                case STOP_TONE_MESSAGE_STRING:
                    audioTrack.pause();
                    audioTrack.flush();
                    audioTrack.setNotificationMarkerPosition(0);
                    // TODO: Write soundBuffer frames until next period then write fadeOutBuffer frames
                    audioTrack.stop();
                    break;
            }
        }
    }
}