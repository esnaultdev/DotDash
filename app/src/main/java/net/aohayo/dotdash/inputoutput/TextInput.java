package net.aohayo.dotdash.inputoutput;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import net.aohayo.dotdash.morse.MorseCodec;
import net.aohayo.dotdash.morse.MorseElement;
import net.aohayo.dotdash.R;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class TextInput {

    public interface InputListener {
        void onOutputStart();
        void onOutputStop();
    }

    private static final String ELEMENTS_STATE = "elementsState";

    private InputListener listener;
    private MorseCodec morseCodec;
    private Queue<String> texts;
    private Queue<MorseElement> elements;
    private TranslationTask trTask;
    private SendTask sendTask;
    private Context context;

    public TextInput(Context context, InputListener listener) {
        this(context, listener, null);
    }

    public TextInput(Context context, InputListener listener, Bundle savedInstanceState) {
        this.context = context;
        this.listener = listener;
        morseCodec = MorseCodec.getInstance();
        if (!morseCodec.isInit()) {
            morseCodec.init(context);
        }
        texts = new LinkedList<>();
        elements = new LinkedList<>();

        if (savedInstanceState != null) {
            LinkedList<MorseElement> savedElements;
            savedElements = (LinkedList<MorseElement>) savedInstanceState.getSerializable(ELEMENTS_STATE);
            if (savedElements != null && savedElements.size() > 0) {
                elements = savedElements;
                sendTask = new SendTask();
                sendTask.execute();
            }
        }
    }

    public Bundle getInstanceState() {
        Bundle state = new Bundle();
        state.putSerializable(ELEMENTS_STATE, (LinkedList<MorseElement>) elements);
        return state;
    }

    public void sendText(String text) {
        String formattedText = Normalizer.normalize(text, Normalizer.Form.NFD);
        formattedText = formattedText.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        formattedText = formattedText.toUpperCase();
        texts.add(formattedText);
        if (texts.size() == 1) {
            trTask = new TranslationTask();
            trTask.execute();
        }
    }

    public void cancel() {
        if (trTask != null) {
            trTask.cancel(false);
        }
        if (sendTask != null) {
            sendTask.resume();
            sendTask.cancel(true);
        }
    }

    public void clear() {
        texts.clear();
        elements.clear();
    }

    public void pause() {
        if (sendTask != null) {
            sendTask.pause();
        }
    }

    public void resume() {
        if (sendTask != null) {
            sendTask.resume();
        }
        morseCodec.refreshDurations(context);
    }

    private class TranslationTask extends AsyncTask<Void, Void, Void> {

        private char[] text;
        private boolean[] isSpace;

        protected void onPreExecute() {
            text = texts.peek().toCharArray();
            isSpace = new boolean[text.length];
            if (elements.size() > 0) {
                elements.add(MorseElement.MEDIUM_GAP);
            }
        }

        @Override
        protected Void doInBackground(Void... v) {
            for (int i = 0; i < text.length; i++) {
                isSpace[i] = !morseCodec.canTranslate(text[i]);
            }
            for (int i = 0; i < text.length; i++) {
                if (isCancelled()) {
                    break;
                }
                if (isSpace[i]) {
                    elements.add(MorseElement.MEDIUM_GAP);
                } else {
                    MorseElement[] code = morseCodec.getCode(text[i]);
                    elements.addAll(Arrays.asList(code));
                    if (i < text.length - 1 && !isSpace[i + 1]) {
                        elements.add(MorseElement.SHORT_GAP);
                    }
                }
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            texts.poll();
            if (texts.size() > 0) {
                trTask = new TranslationTask();
                trTask.execute();
            }
            if (sendTask == null || sendTask.getStatus() != Status.RUNNING) {
                sendTask = new SendTask();
                sendTask.execute();
            }
        }
    }

    private class SendTask extends AsyncTask<Void, Void, Void> {
        static final long RETRY_DURATION = 200;
        private boolean paused = false;
        private Thread backgroundThread;

        @Override
        protected Void doInBackground(Void... params) {
            if (elements.size() == 0) {
                return null;
            }
            backgroundThread = Thread.currentThread();
            MorseElement currentElement = elements.poll();
            while (currentElement != null && !isCancelled()) {
                while (paused) {
                    try {
                        Thread.sleep(RETRY_DURATION);
                    } catch (InterruptedException e) {
                        Log.d("SendTask", "task interrupted while paused");
                    }
                }

                if (currentElement == MorseElement.DOT || currentElement == MorseElement.DASH) {
                    listener.onOutputStart();
                }
                try {
                    Thread.sleep(morseCodec.getDuration(currentElement));
                } catch (Exception e) {
                    e.getLocalizedMessage();
                }
                if (currentElement == MorseElement.DOT || currentElement == MorseElement.DASH) {
                    listener.onOutputStop();
                }
                currentElement = elements.poll();
            }
            return null;
        }

        public void pause() {
            paused = true;
        }

        public void resume() {
            paused = false;
            if (backgroundThread != null) {
                backgroundThread.interrupt();
            }
        }
    }
}
