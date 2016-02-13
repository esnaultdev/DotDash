package net.aohayo.dotdash.morse;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;

public class MorseCodeParser {

    private final String ns = null;

    private EnumMap<MorseElement, Integer> durations;
    private HashMap<Character, CodePair> codes;

    public MorseCodeParser() {
        durations = new EnumMap<>(MorseElement.class);
        codes = new HashMap<>();
    }

    public EnumMap<MorseElement, Integer> getDurations() {
        return durations;
    }

    public HashMap<Character, CodePair> getCodes() {
        return codes;
    }

    public void parse(Context context, int codeId) {
        XmlResourceParser parser = context.getResources().getXml(codeId);
        try {
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String tag = parser.getName();
                if (tag.equals("code")) {
                    CodePair code = parseCode(parser);
                    codes.put(code.getCharacter(), code);
                } else if (tag.equals("duration")) {
                    parseDuration(parser);
                }
            }
        } catch (XmlPullParserException e) {
            Log.e("MorseCodec", "Corrupted xml file (Bad event)");
        } catch (IOException e) {
            Log.e("MorseCodec", "Corrupted xml file (Could not read)");
        }
    }

    private void parseDuration(XmlPullParser parser) throws IOException, XmlPullParserException {
        MorseElement element;

        parser.require(XmlPullParser.START_TAG, ns, "duration");
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
        String durationText = readText(parser);
        durations.put(element, Integer.parseInt(durationText));
        parser.require(XmlPullParser.END_TAG, ns, "duration");
    }

    /***
     * Parse a code element
     */
    private CodePair parseCode(XmlPullParser parser) throws IOException, XmlPullParserException {
        Character character;
        CodeType type;

        parser.require(XmlPullParser.START_TAG, ns, "code");
        character = parser.getAttributeValue(null, "character").charAt(0);
        type = CodeType.fromString(parser.getAttributeValue(null, "type"));
        String elementsString = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "code");
        return new CodePair(character, MorseCodec.getElementsFromString(elementsString), type);
    }

    /***
     * Read the text value for a node
     */
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }
}
