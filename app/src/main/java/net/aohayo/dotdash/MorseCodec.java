package net.aohayo.dotdash;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;

public class MorseCodec {
    private int[] durations = new int[5];
    private HashMap<String, MorseElement[]> codes;

    public MorseCodec(Context context, int codeId) {
        codes = new HashMap<>();
        parseXML(context, codeId);
    }

    public void parseXML(Context context, int codeId) {
        XmlResourceParser parser = context.getResources().getXml(codeId);
        boolean inDuration = false;
        boolean inCode = false;
        int durationIndex = 0;
        String character = "";
        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (parser.getName() == "duration") {
                        inDuration = true;
                    } else if (parser.getName() == "code") {
                        inCode = true;
                        character = parser.getAttributeValue("", "character");
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (parser.getName() == "duration") {
                        inDuration = false;
                        durationIndex++;
                    } else if (parser.getName() == "code") {
                        inCode = false;
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    String text = parser.getText();
                    if (inDuration) {
                        durations[durationIndex] = Integer.getInteger(text);
                    } else if (inCode) {
                        codes.put(character, getElements(text));
                    }
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            Log.e("MorseCodec", "Corrupted xml file (Bad event)");
        } catch (IOException e) {
            Log.e("MorseCodec", "Corrupted xml file (Could not read)");
        }
    }

    private MorseElement[] getElements(String dotsAndDashes) {
        MorseElement[] result = new MorseElement[dotsAndDashes.length()];
        for (int i = 0; i < dotsAndDashes.length(); i++) {
            char current = dotsAndDashes.charAt(i);
            if (current == '.') {
                result[i] = MorseElement.DOT;
            } else if (current == '-') {
                result[i] = MorseElement.DASH;
            }
        }
        return result;
    }

    public int getDuration(MorseElement element) {
        switch (element) {
            case DOT:
                return durations[0];
            case DASH:
                return durations[1];
            case TINY_GAP:
                return durations[2];
            case SHORT_GAP:
                return durations[3];
            case MEDIUM_GAP:
                return durations[4];
            default:
                return 0;
        }
    }

    public MorseElement[] getCode(String character) {
        return codes.get(character);
    }

}
