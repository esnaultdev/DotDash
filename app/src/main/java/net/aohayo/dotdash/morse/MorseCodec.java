package net.aohayo.dotdash.morse;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.aohayo.dotdash.R;
import net.aohayo.dotdash.main.SettingsActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MorseCodec {
    private EnumMap<MorseElement, Integer> durations; // in milliseconds
    private EnumMap<MorseElement, Integer> rDurations; // relative durations from xml
    private HashMap<Character, CodePair> codes;
    private boolean isInit = false;

    // Private constructor to prevent instanciation from other classes.
    private MorseCodec() { }

    private static class MorseCodecSingletonHolder {
        private static final MorseCodec INSTANCE = new MorseCodec();
    }

    public static MorseCodec getInstance() {
        return MorseCodecSingletonHolder.INSTANCE;
    }

    public void init(Context context) {
        isInit = true;
        durations = new EnumMap<>(MorseElement.class);

        MorseCodeParser parser = new MorseCodeParser();
        int codeId = R.xml.morse_code_itu; // TODO: use a shared preference
        parser.parse(context, codeId);
        codes = parser.getCodes();
        rDurations = parser.getDurations();
        computeDurations(context);
    }

    public boolean isInit() {
        return isInit;
    }

    public void refreshDurations(Context context) {
        computeDurations(context);
    }

    public static MorseElement[] getElementsFromString(String dotsAndDashes) {
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
            return codes.get(c).getCode();
        }
    }

    public boolean canTranslate(Character c) {
         return codes.containsKey(c);
    }

    public List<CodePair> getCodePairs(CodeType type) {
        ArrayList<CodePair> pairs = new ArrayList<>();
        for (Map.Entry<Character, CodePair> entry : codes.entrySet()) {
            if( type == null || entry.getValue().getType() == type) {
                pairs.add(entry.getValue());
            }
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
