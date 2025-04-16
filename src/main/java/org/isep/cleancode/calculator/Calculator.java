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
     * Évalue une expression mathématique fournie sous forme de chaîne de caractères.
     *
     * @param expression l'expression mathématique à évaluer
     * @return le résultat de l'évaluation
     */
    public double evaluateMathExpression(String expression) {

        validatePresenceOfOperator(expression);


        String formattedExpression = formatExpression(expression);


        List<String> tokens = tokenizeExpression(formattedExpression);


        List<String> rpnTokens = convertTokensToRPN(tokens);


        return evaluateRPN(rpnTokens);
    }


    /**
     * Vérifie que l'expression contient au moins un opérateur entre les opérandes.
     *
     * @param expression l'expression à valider
     */
    private void validatePresenceOfOperator(String expression) {
        Pattern pattern = Pattern.compile(RegexType.NO_OPERATORS.pattern(), Pattern.DOTALL);
        Matcher matcher = pattern.matcher(expression);
        if (matcher.find()) {
            throw new IllegalArgumentException("Expression invalide : pas d'opérateurs entre les opérandes");
        }
    }


    /**
     * Formate l'expression en supprimant les espaces et en gérant les opérateurs unaires.
     *
     * @param expression l'expression à formater
     * @return l'expression formatée
     */
    private String formatExpression(String expression) {
        String withoutSpaces = expression.replaceAll(RegexType.WHITESPACE.pattern(), "");
        String handleUnaryOperators = withoutSpaces.replaceAll(RegexType.UNARY.pattern(), "~$1");
        handleUnaryOperators = handleUnaryOperators.replaceAll(RegexType.UNARY_BEFORE_PARENTHESIS.pattern(), "~(");
        if (handleUnaryOperators.isEmpty()) {
            throw new IllegalArgumentException("Expression vide");
        }
        return handleUnaryOperators;
    }


    /**
     * Découpe l'expression formatée en une liste de tokens.
     *
     * @param expression l'expression à tokeniser
     * @return la liste des tokens
     */
    private List<String> tokenizeExpression(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder numberBuffer = new StringBuilder();

        for (int i = 0; i < expression.length(); i++) {
            char currentChar = expression.charAt(i);

            if (isCharPartOfNumber(currentChar)) {
                numberBuffer.append(currentChar);
            } else {
                if (!numberBuffer.isEmpty()) {
                    processNumberBuffer(tokens, numberBuffer);
                }
                processOperatorOrParenthesis(tokens, currentChar);
            }
        }

        if (!numberBuffer.isEmpty()) {
            processNumberBuffer(tokens, numberBuffer);
        }
        return tokens;
    }

    /**
     * Vérifie si le caractère fait partie d'un nombre (chiffres, point décimal ou indicateur d'unaire).
     */
    private boolean isCharPartOfNumber(char c) {
        return Character.isDigit(c) || c == '.' || c == '~';
    }

    /**
     * Traite le contenu du buffer de chiffre.
     */
    private void processNumberBuffer(List<String> tokens, StringBuilder numberBuffer) {
        String tokenNumber = numberBuffer.toString();
        numberBuffer.setLength(0);
        if (tokenNumber.startsWith("~")) {
            addNegativeNumberTransformation(tokens, tokenNumber.substring(1));
        } else {
            tokens.add(tokenNumber);
        }
    }

    /**
     * Ajoute un token correspondant à un opérateur ou une parenthèse.
     */
    private void processOperatorOrParenthesis(List<String> tokens, char currentChar) {
        if ("+-*/()%".indexOf(currentChar) >= 0) {
            tokens.add(String.valueOf(currentChar));
        } else {
            throw new IllegalArgumentException("Caractère invalide: " + currentChar);
        }
    }

    /**
     * Transforme un nombre négatif représenté par un '~' en une opération explicite (0 - valeur).
     */
    private void addNegativeNumberTransformation(List<String> tokens, String value) {
        tokens.add("(");
        tokens.add("0");
        tokens.add("-");
        tokens.add(value);
        tokens.add(")");
    }


    /**
     * Convertit la liste de tokens en notation polonaise inverse.
     *
     * @param tokens la liste de tokens
     * @return la liste de tokens en RPN
     */
    private List<String> convertTokensToRPN(List<String> tokens) {
        List<String> outputQueue = new ArrayList<>();
        Stack<String> operatorStack = new Stack<>();

        Map<String, Integer> operatorPrecedence = Map.of(
                "+", 1,
                "-", 1,
                "*", 2,
                "/", 2,
                "%", 2
        );

        for (String token : tokens) {
            if (token.startsWith("~")) {
                outputQueue.add(convertNegativeToken(token));
            } else if (isNumeric(token)) {
                outputQueue.add(token);
            } else if (isOperator(token)) {
                pushOperatorWithPrecedence(operatorStack, outputQueue, token, operatorPrecedence);
            } else if ("(".equals(token)) {
                operatorStack.push(token);
            } else if (")".equals(token)) {
                popOperatorsUntilLeftParenthesis(operatorStack, outputQueue);
            }
        }

        while (!operatorStack.isEmpty()) {
            String op = operatorStack.pop();
            if ("(".equals(op)) {
                throw new IllegalArgumentException("Parenthèses non appariées");
            }
            outputQueue.add(op);
        }
        return outputQueue;
    }

    /**
     * Transforme un token négatif préfixé par '~' en valeur négative.
     */
    private String convertNegativeToken(String token) {
        return "-" + token.substring(1);
    }

    /**
     * Vérifie si le token est un nombre (entier ou décimal).
     */
    private boolean isNumeric(String token) {
        return token.matches("\\d+(\\.\\d+)?");
    }

    /**
     * Vérifie si le token correspond à un opérateur.
     */
    private boolean isOperator(String token) {
        return OPERATORS.contains(token);
    }

    /**
     * Traite l'empilement de l'opérateur en prenant en compte sa priorité.
     */
    private void pushOperatorWithPrecedence(Stack<String> operatorStack, List<String> outputQueue,
                                            String currentOperator, Map<String, Integer> operatorPrecedence) {
        int currentPrecedence = operatorPrecedence.get(currentOperator);
        while (!operatorStack.isEmpty() && !"(".equals(operatorStack.peek())
                && operatorPrecedence.getOrDefault(operatorStack.peek(), 0) >= currentPrecedence) {
            outputQueue.add(operatorStack.pop());
        }
        operatorStack.push(currentOperator);
    }

    /**
     * Vide la pile des opérateurs jusqu'à trouver la parenthèse ouvrante.
     */
    private void popOperatorsUntilLeftParenthesis(Stack<String> operatorStack, List<String> outputQueue) {
        while (!operatorStack.isEmpty() && !"(".equals(operatorStack.peek())) {
            outputQueue.add(operatorStack.pop());
        }
        if (!operatorStack.isEmpty() && "(".equals(operatorStack.peek())) {
            operatorStack.pop();
        } else {
            throw new IllegalArgumentException("Parenthèses non appariées");
        }
    }


    /**
     * Évalue l'expression en notation polonaise inverse.
     *
     * @param rpnTokens la liste des tokens en RPN
     * @return le résultat numérique
     */
    private double evaluateRPN(List<String> rpnTokens) {
        Stack<Double> evaluationStack = new Stack<>();

        for (String token : rpnTokens) {
            if (isNumeric(token) || token.matches("-\\d+(\\.\\d+)?")) {
                evaluationStack.push(Double.parseDouble(token));
            } else {
                if (evaluationStack.size() < 2) {
                    throw new IllegalArgumentException("Nombre insuffisant d'opérandes pour l'opérateur : " + token);
                }
                double operand2 = evaluationStack.pop();
                double operand1 = evaluationStack.pop();
                evaluationStack.push(applyOperator(token, operand1, operand2));
            }
        }

        if (evaluationStack.size() != 1) {
            throw new IllegalArgumentException("Expression invalide : trop d'opérandes");
        }
        return evaluationStack.pop();
    }

    /**
     * Applique l'opérateur sur les deux opérandes.
     *
     * @param operator l'opérateur à appliquer
     * @param operand1 premier opérande
     * @param operand2 second opérande
     * @return le résultat du calcul
     */
    private double applyOperator(String operator, double operand1, double operand2) {
        return switch (operator) {
            case "+" -> operand1 + operand2;
            case "-" -> operand1 - operand2;
            case "*" -> operand1 * operand2;
            case "%" -> operand1 % operand2;
            case "/" -> {
                if (operand2 == 0) {
                    throw new ArithmeticException("Division par zéro");
                }
                yield operand1 / operand2;
            }
            default -> throw new IllegalArgumentException("Opérateur inconnu : " + operator);
        };
    }
}
