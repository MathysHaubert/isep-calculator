package org.isep.cleancode.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum RegexType {

    UNARY("(?:^|[\\(\\+\\-\\*/])\\s*-\\s*(\\d+(?:\\.\\d+)?)"),
    UNARY_BEFORE_PARENTHESIS("(?:^|[\\(\\+\\-\\*/])\\s*-\\s*\\("),
    NO_OPERATORS("(\\d+(?:\\.\\d+)?|\\))\\s+(?=\\d|~|\\()"),
    WHITESPACE("\\s+");

    private final String pattern;

    RegexType(String pattern) {
        this.pattern = pattern;
    }

    public String pattern() {
        return pattern;
    }

    public Pattern toPattern() {
        return Pattern.compile(pattern);
    }

    public Matcher matcher(String input) {
        return Pattern.compile(pattern).matcher(input);
    }
}
