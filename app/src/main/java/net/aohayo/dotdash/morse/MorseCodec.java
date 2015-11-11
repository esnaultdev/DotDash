package net.aohayo.dotdash.morse;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;

public class MorseCodec {
    private int[] durations = new int[5];
    private HashMap<Character, MorseElement[]> codes;

    public MorseCodec(Context context, int codeId) {
        codes = new HashMap<>();
        parseXML(context, codeId);
    }

    public void parseXML(Context context, int codeId) {
        XmlResourceParser parser = context.getResources().getXml(codeId);
        boolean inDuration = false;
        boolean inCode = false;
        int durationIndex = 0;
        Character character = '\0';
        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (parser.getName().equals("duration")) {
                        inDuration = true;
                    } else if (parser.getName().equals("code")) {
                        inCode = true;
                        character = parser.getAttributeValue(null, "character").charAt(0);
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (parser.getName().equals("duration")) {
                        inDuration = false;
                        durationIndex++;
                    } else if (parser.getName().equals("code")) {
                        inCode = false;
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    String text = parser.getText();
                    if (text != null) {
                        if (inDuration) {
                            durations[durationIndex] = Integer.parseInt(text);
                        } else if (inCode) {
                            codes.put(character, getElements(text));
                        }
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
        MorseElement[] result = new MorseElement[2*dotsAndDashes.length() - 1];
        for (int i = 0; i < dotsAndDashes.length(); i++) {
            char current = dotsAndDashes.charAt(i);
            if (current == '.') {
                result[2*i] = MorseElement.DOT;
            } else if (current == '-') {
                result[2*i] = MorseElement.DASH;
            }
            if (2*i + 1 < result.length) {
                result[2*i + 1] = MorseElement.TINY_GAP;
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

    public MorseElement[] getCode(Character c) {
        if (!codes.containsKey(c)) {
            return new MorseElement[0];
        } else {
            return codes.get(c);
        }
    }

}
