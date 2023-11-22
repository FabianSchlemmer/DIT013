import java.util.*;

import static java.lang.Double.NaN;
import static java.lang.Math.pow;


/*
 *   A calculator for rather simple arithmetic expressions
 *
 *   This is not the program, it's a class declaration (with methods) in it's
 *   own file (which must be named Calculator.java)
 *
 *   NOTE:
 *   - No negative numbers implemented
 */
public class Calculator {

    // Here are the only allowed instance variables!
    // Error messages (more on static later)
    final static String MISSING_OPERAND = "Missing or bad operand";
    final static String DIV_BY_ZERO = "Division with 0";
    final static String MISSING_OPERATOR = "Missing operator or parenthesis";
    final static String OP_NOT_FOUND = "Operator not found";

    // Definition of operators
    final static String OPERATORS = "+-*/^";

    // Method used in REPL
    double eval(String expr) {
        if (expr.length() == 0) {
            return NaN;
        }
        List<String> tokens = tokenize(expr);
        List<String> postfix = infix2Postfix(tokens);
        return evalPostfix(postfix);
    }

    // ------  Evaluate RPN expression -------------------

    double evalPostfix(List<String> postfix) {
        Stack<Double> numbers = new Stack<>();
        for (String token : postfix) {
            // if it's a number, put it on the stack
            if (Character.isDigit(token.charAt(0))) {
                numbers.push(Double.parseDouble(token));
            }
            else if (OPERATORS.contains(token)) {
                // not enough numbers, too many operators
                if (numbers.size() < 2) {
                    throw new IllegalArgumentException(MISSING_OPERAND);
                }
                // apply the operator
                numbers.push(applyOperator(token, numbers.pop(), numbers.pop()));
            }
        }
        if (numbers.size() > 1) {
            // too many numbers, not enough operators
            throw new IllegalArgumentException(MISSING_OPERATOR);
        }
        // final remaining number is the result of the expression
        return numbers.pop();
    }

    double applyOperator(String op, double d1, double d2) {
        switch (op) {
            case "+":
                return d1 + d2;
            case "-":
                return d2 - d1;
            case "*":
                return d1 * d2;
            case "/":
                if (d1 == 0) {
                    throw new IllegalArgumentException(DIV_BY_ZERO);
                }
                return d2 / d1;
            case "^":
                return pow(d2, d1);
        }
        throw new RuntimeException(OP_NOT_FOUND);
    }

    // ------- Infix 2 Postfix ------------------------

    List<String> infix2Postfix(List<String> infix) {
        ArrayList<String> postfix = new ArrayList<>();
        Stack<String> operators = new Stack<>();
        for (String currentElement : infix) {
            // if the first character is a digit then this token is a number, and we just add it to the stack
            if (Character.isDigit(currentElement.charAt(0))) {
                postfix.add(currentElement);
            // if it's an operator, we need to handle a variety of conditions according to the shunting yard algorithm
            } else if (OPERATORS.contains(currentElement)) {
                queueOperators(postfix, operators, currentElement);
            // left parenthesis just get pushed onto the stack
            } else if (currentElement.equals("(")) {
                operators.push(currentElement);
            // for right parenthesis we have special handling according to the conditions of the algorithm
            } else if (currentElement.equals(")")) {
                resolveParenthesis(postfix, operators);
            }
        }
        // add any remaining operators to the list
        while (!operators.isEmpty()) {
            String operator = operators.pop();
            // too many left parenthesis, not enough right parenthesis
            if (operator.equals("(")) {
                throw new IllegalArgumentException(MISSING_OPERATOR);
            }
            postfix.add(operator);
        }
        return postfix;
    }
    
    void resolveParenthesis(ArrayList<String> postfix, Stack<String> operators) {
        // if there are no operators on the stack, that means we have mismatched parenthesis
        if (operators.isEmpty()) {
            throw new IllegalArgumentException(MISSING_OPERATOR);
        }
        // until we find left parenthesis just add all operators into the list
        while (!operators.peek().equals("(")) {
            postfix.add(operators.pop());
            // same deal as above: if we don't find left parenthesis that means we have mismatched parenthesis
            if (operators.isEmpty()) {
                throw new IllegalArgumentException(MISSING_OPERATOR);
            }
        }
        // remove the left parenthesis
        operators.pop();
    }

    void queueOperators(ArrayList<String> postfix, Stack<String> operators, String nextOp) {
        if (operators.isEmpty() || operators.peek().equals("(")) {
            operators.push(nextOp);
            return;
        }
        String queuedOp = operators.peek();
        int nextPrec = getPrecedence(nextOp);
        int queuedPrec = getPrecedence(queuedOp);
        // condition taken from linked wikipedia article (shunting yard)
        while (queuedPrec > nextPrec || (queuedPrec == nextPrec && getAssociativity(nextOp) == Assoc.LEFT)) {
            postfix.add(operators.pop());
            if (operators.isEmpty() || operators.peek().equals("(")) {
                break;
            }
            queuedOp = operators.peek();
            queuedPrec = getPrecedence(queuedOp);
        }
        operators.push(nextOp);
    }

    int getPrecedence(String op) {
        if ("+-".contains(op)) {
            return 2;
        } else if ("*/".contains(op)) {
            return 3;
        } else if ("^".contains(op)) {
            return 4;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    Assoc getAssociativity(String op) {
        if ("+-*/".contains(op)) {
            return Assoc.LEFT;
        } else if ("^".contains(op)) {
            return Assoc.RIGHT;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    enum Assoc {
        LEFT,
        RIGHT
    }

    // ---------- Tokenize -----------------------

    // List String (not char) because numbers (with many chars)
    // NOTE: This function is implemented recursively, that means we are always dealing with the FIRST token in the
    // expression, we are just cutting down the expression into smaller pieces as the recursion runs deeper
    List<String> tokenize(String expr) {
        String token;
        ArrayList<String> tokens = new ArrayList<>();
        char firstElement = (expr.charAt(0));
        // if it's a number, make sure we get all digits, not just the first
        if (Character.isDigit(firstElement)) {
            token = findNumber(expr);
        } else {
            token = String.valueOf(firstElement);
        }
        // add the token if it's NOT a space
        if (!Character.isWhitespace(firstElement)) {
            tokens.add(token);
        }
        if (expr.length() > token.length()) {
            // after getting the first token, cut the expression at token.length (often 1, but might be larger
            // because of multi-digit numbers), and call tokenize recursively on the "tail" of the expression
            // add all the results to tokens, and finally return it
            tokens.addAll(tokenize(expr.substring(token.length())));
        }
        return tokens;
    }

    String findNumber(String expr) {
        String number = "";
        for (int i = 0; i < expr.length(); i++) {
            char firstElement = expr.charAt(i);
            // if it's a number or a decimal dot, append it
            if (Character.isDigit(firstElement) || firstElement == '.') {
                number += firstElement;
            // if it's not a number or a decimal dot then the number is finished, so we break the loop
            } else {
                break;
            }
        }
        return number;
    }
}
