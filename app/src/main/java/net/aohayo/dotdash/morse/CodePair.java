package net.aohayo.dotdash.morse;

import java.util.Comparator;

public class CodePair {
    private Character character;
    private MorseElement[] code;
    private CodeType type;

    public CodePair(Character character, MorseElement[] code, CodeType type) {
        this.character = character;
        this.code = code;
        this.type = type;
    }

    public Character getCharacter() {
        return character;
    }

    public MorseElement[] getCode() {
        return code;
    }

    public CodeType getType() {
        return type;
    }

    public static class CodePairComparator implements Comparator<CodePair> {
        @Override
        public int compare(CodePair lhs, CodePair rhs) {
            return lhs.getCharacter().compareTo(rhs.getCharacter());
        }
    }
}
