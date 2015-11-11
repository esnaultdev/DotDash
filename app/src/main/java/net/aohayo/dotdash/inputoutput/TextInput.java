package net.aohayo.dotdash.inputoutput;

import android.content.Context;
import android.os.AsyncTask;

import net.aohayo.dotdash.morse.MorseCodec;
import net.aohayo.dotdash.morse.MorseElement;
import net.aohayo.dotdash.R;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class TextInput {

    public interface InputListener {
        void onOutputStart();
        void onOutputStop();
    }

    private InputListener listener;
    private MorseCodec morseCodec;
    private Queue<String> texts;
    private Queue<MorseElement> elements;
    private TranslationTask trTask;
    private SendTask sendTask;
    private int unitDuration = 80; // in milliseconds

    public TextInput(Context context, InputListener listener) {
        this.listener = listener;
        morseCodec = new MorseCodec(context, R.xml.morse_code_itu);
        texts = new LinkedList<>();
        elements = new LinkedList<>();
    }

    public void sendText(String text) {
        texts.add(text.toUpperCase());
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
            sendTask.cancel(true);
        }
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
                isSpace[i] = text[i] < 'A' || text[i] > 'Z';
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
        @Override
        protected Void doInBackground(Void... params) {
            if (elements.size() == 0) {
                return null;
            }
            MorseElement currentElement = elements.poll();
            while (currentElement != null && !isCancelled()) {
                int duration = morseCodec.getDuration(currentElement) * unitDuration;
                if (currentElement == MorseElement.DOT || currentElement == MorseElement.DASH) {
                    listener.onOutputStart();
                }
                try {
                    Thread.sleep(duration);
                } catch (Exception e) {
                    e.getLocalizedMessage();
                }
                listener.onOutputStop();
                currentElement = elements.poll();
            }
            return null;
        }
    }
}
