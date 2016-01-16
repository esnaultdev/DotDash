package net.aohayo.dotdash.morse;

import java.util.Comparator;

public class CodePair {
    private Character character;
    private MorseElement[] code;

    public CodePair(Character character, MorseElement[] code) {
        this.character = character;
        this.code = code;
    }

    public Character getCharacter() {
        return character;
    }

    public MorseElement[] getCode() {
        return code;
    }

    public static class CodePairComparator implements Comparator<CodePair> {
        @Override
        public int compare(CodePair lhs, CodePair rhs) {
            boolean isDigitLhs = Character.isDigit(lhs.getCharacter());
            boolean isDigitRhs = Character.isDigit(rhs.getCharacter());
            if (isDigitLhs && !isDigitRhs) {
                return 1;
            } else if (!isDigitLhs && isDigitRhs) {
                return -1;
            } else {
                return lhs.getCharacter().compareTo(rhs.getCharacter());
            }
        }
    }
}
