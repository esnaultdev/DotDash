package net.aohayo.dotdash.morse;

public enum CodeType {
    LETTER,
    NUMBER,
    PUNCTUATION;

    public static CodeType fromString(String type) {
        switch (type) {
            case "letter":
                return LETTER;
            case "number":
                return NUMBER;
            case "punctuation":
            default:
                return PUNCTUATION;
        }
    }
}
