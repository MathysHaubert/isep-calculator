package org.isep.cleancode.calculator;

import org.isep.cleancode.regex.RegexType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculator {
    private final static String OPERATORS = "+-*/%";


    /**
     * Evaluates a mathematical expression given as a string using the Shunting-yard algorithm.
     *
     * @param expression the mathematical expression to evaluate
     * @return the result of the evaluation
     */
    public double evaluateMathExpression(String expression) {

        Pattern pattern = Pattern.compile(RegexType.NO_OPERATORS.pattern(), Pattern.DOTALL);
        Matcher matcher = pattern.matcher(expression);
        if (matcher.find()) {
            throw new IllegalArgumentException("Expression invalide : pas d'opérateurs entre les opérandes");
        }

        expression = expression.replaceAll(RegexType.WHITESPACE.pattern(), "");

        expression = expression.replaceAll(RegexType.UNARY.pattern(), "~$1");

        expression = expression.replaceAll(RegexType.UNARY_BEFORE_PARENTHESIS.pattern(), "~(");

        if (expression.isEmpty()) {
            throw new IllegalArgumentException("Expression vide");
        }

        List<String> tokens = tokenize(expression);

        List<String> rpn = toRPN(tokens);

        return evaluateRPN(rpn);
    }

    private List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder number = new StringBuilder();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || c == '.' || c == '~') {
                number.append(c);
            } else {
                if (!number.isEmpty()) {
                    String num = number.toString();
                    number.setLength(0);

                    if (num.startsWith("~")) {
                        String value = num.substring(1);
                        this.replaceNegativeNumberByOperation(tokens, value);
                    } else {
                        tokens.add(num);
                    }
                }

                if ("+-*/()%".indexOf(c) >= 0) {
                    tokens.add(String.valueOf(c));
                } else {
                    throw new IllegalArgumentException("Invalid character: " + c);
                }
            }
        }

        if (!number.isEmpty()) {
            String num = number.toString();
            if (num.startsWith("~")) {
                String value = num.substring(1);
                this.replaceNegativeNumberByOperation(tokens, value);
            } else {
                tokens.add(num);
            }
        }

        return tokens;
    }


    private List<String> toRPN(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Stack<String> operators = new Stack<>();

        Map<String, Integer> precedence = Map.of(
                "+", 1,
                "-", 1,
                "*", 2,
                "/", 2,
                "%", 2
        );

        for (String token : tokens) {
            if (token.startsWith("~")) {
                String value = token.substring(1);
                output.add("-" + value);

            } else if (token.matches("\\d+(\\.\\d+)?")) {
                output.add(token);

            } else if (Calculator.OPERATORS.contains(token)) {
                int tokenPrecedence = precedence.getOrDefault(token, -1);
                if (Calculator.OPERATORS.contains(token)) {

                    while (
                            !operators.isEmpty() &&
                                    !operators.peek().equals("(") &&
                                    precedence.getOrDefault(operators.peek(), 0) >= tokenPrecedence

                    ) {
                        output.add(operators.pop());
                    }
                    operators.push(token);
                }
            } else if (token.equals("(")) {
                operators.push(token);

            } else if (token.equals(")")) {
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    output.add(operators.pop());
                }
                if (!operators.isEmpty() && operators.peek().equals("(")) {
                    operators.pop();
                } else {
                    throw new IllegalArgumentException("Mismatched parentheses");
                }
            }
        }

        while (!operators.isEmpty()) {
            if (operators.peek().equals("(")) {
                throw new IllegalArgumentException("Mismatched parentheses");
            }
            output.add(operators.pop());
        }

        return output;
    }

    private double evaluateRPN(List<String> rpn) {
        Stack<Double> stack = new Stack<>();

        for (String token : rpn) {
            if (token.matches("-?\\d+(\\.\\d+)?")) {
                stack.push(Double.parseDouble(token));
            } else {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Expression invalide : pas assez d'opérandes pour '" + token + "'");
                }

                double b = stack.pop();
                double a = stack.pop();
                switch (token) {
                    case "+" -> stack.push(a + b);
                    case "-" -> stack.push(a - b);
                    case "*" -> stack.push(a * b);
                    case "%" -> stack.push(a % b);
                    case "/" -> {
                        if (b == 0) {
                            throw new ArithmeticException("Division par zéro");
                        }
                        stack.push(a / b);
                    }
                    default -> throw new IllegalArgumentException("Opérateur inconnu: " + token);
                }
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("Expression invalide : trop d'opérandes");
        }

        return stack.pop();
    }

    private void replaceNegativeNumberByOperation(List<String> tokens, String value) {
        tokens.add("(");
        tokens.add("0");
        tokens.add("-");
        tokens.add(value);
        tokens.add(")");
    }

}
