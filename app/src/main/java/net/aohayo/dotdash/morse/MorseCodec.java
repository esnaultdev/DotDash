package net.aohayo.dotdash.morse;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.preference.PreferenceManager;
import android.util.Log;

import net.aohayo.dotdash.R;
import net.aohayo.dotdash.main.SettingsActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MorseCodec {
    private EnumMap<MorseElement, Integer> durations; // in milliseconds
    private EnumMap<MorseElement, Integer> rDurations; // relative durations
    private HashMap<Character, MorseElement[]> codes;
    private Context context;
    private boolean isInit = false;

    // Private constructor to prevent instanciation from other classes.
    private MorseCodec() { }

    private static class MorseCodecSingletonHolder {
        private static final MorseCodec INSTANCE = new MorseCodec();
    }

    public static MorseCodec getInstance() {
        return MorseCodecSingletonHolder.INSTANCE;
    }

    public void init(Context context, int codeId) {
        isInit = true;
        codes = new HashMap<>();
        durations = new EnumMap<>(MorseElement.class);
        rDurations = new EnumMap<>(MorseElement.class);
        this.context = context;

        parseXML(context, codeId);
        computeDurations(context);
    }

    public boolean isInit() {
        return isInit;
    }

    public void refreshDurations() {
        computeDurations(context);
    }

    public void parseXML(Context context, int codeId) {
        XmlResourceParser parser = context.getResources().getXml(codeId);
        boolean inDuration = false;
        boolean inCode = false;
        Character character = '\0';
        MorseElement element = MorseElement.DOT;
        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (parser.getName().equals("duration")) {
                        inDuration = true;
                        String elementName = parser.getAttributeValue(null, "element");
                        switch (elementName) {
                            case "dot":
                                element = MorseElement.DOT;
                                break;
                            case "dash":
                                element = MorseElement.DASH;
                                break;
                            case "tiny_gap":
                                element = MorseElement.TINY_GAP;
                                break;
                            case "short_gap":
                                element = MorseElement.SHORT_GAP;
                                break;
                            case "medium_gap":
                                element = MorseElement.MEDIUM_GAP;
                                break;
                            default:
                                throw new XmlPullParserException("Unknown element: " + elementName);
                        }
                    } else if (parser.getName().equals("code")) {
                        inCode = true;
                        character = parser.getAttributeValue(null, "character").charAt(0);
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (parser.getName().equals("duration")) {
                        inDuration = false;
                    } else if (parser.getName().equals("code")) {
                        inCode = false;
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    String text = parser.getText();
                    if (text != null) {
                        if (inDuration) {
                            rDurations.put(element, Integer.parseInt(text));
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
        if (durations.containsKey(element)) {
            return durations.get(element);
        } else {
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

    public List<CodePair> getCodePairs() {
        ArrayList<CodePair> pairs = new ArrayList<>();
        for (Map.Entry<Character, MorseElement[]> entry : codes.entrySet()) {
            pairs.add(new CodePair(entry.getKey(), entry.getValue()));
        }
        Collections.sort(pairs, new CodePair.CodePairComparator());
        return pairs;
    }

    private void computeDurations(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        String prefWpm = sharedPref.getString(
                SettingsActivity.KEY_PREF_WPM,
                context.getResources().getString(R.string.pref_wpm_default));
        int wpm = Integer.parseInt(prefWpm);

        String prefRefWord = sharedPref.getString(
                SettingsActivity.KEY_PREF_WPM_REF_WORD,
                context.getResources().getString(R.string.pref_wpm_ref_word_default));
        int wpmRefWordElements = Integer.parseInt(prefRefWord);

        int elementDuration = (int) (60.0f/wpmRefWordElements*1000)/wpm;
        int gapElementDuration = elementDuration;

        boolean prefUseLargerGaps = sharedPref.getBoolean(SettingsActivity.KEY_PREF_LONGER_GAPS, false);

        if (prefUseLargerGaps) {
            String prefWpmGaps = sharedPref.getString(
                    SettingsActivity.KEY_PREF_WPM_GAPS,
                    context.getResources().getString(R.string.pref_wpm_gaps_default));
            int wpmGaps = Integer.parseInt(prefWpmGaps);
            gapElementDuration = (int) (60.0f/wpmRefWordElements*1000)/wpmGaps;
        }

        for (Map.Entry<MorseElement, Integer> entry : rDurations.entrySet()) {
            switch (entry.getKey()) {
                case DOT:
                case DASH:
                case TINY_GAP:
                    durations.put(entry.getKey(), entry.getValue() * elementDuration);
                    break;
                case SHORT_GAP:
                case MEDIUM_GAP:
                    durations.put(entry.getKey(), entry.getValue() * gapElementDuration);
                    break;
                default:
                    break;
            }
        }
    }
}
