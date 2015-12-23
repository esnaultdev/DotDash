package net.aohayo.dotdash.morse;

public enum MorseElement {
    DOT,
    DASH,
    TINY_GAP,
    SHORT_GAP,
    MEDIUM_GAP;

    public String toString() {
        switch (this) {
            case DOT:
                return ".";
            case DASH:
                return "-";
            case TINY_GAP:
                return "";
            case SHORT_GAP:
                return " ";
            case MEDIUM_GAP:
                return " / ";
            default:
                return "";
        }
    }
}
