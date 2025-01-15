import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

public class Parser {
    public static final HashMap<String, Expression> storedVariables = new HashMap<>();
    private ArrayList<String> tokenList;

    private void scanLambda(int startIndex) throws ParseException {
        int lambdaStart = startIndex;
        int parenthesesCounter = 0;
        while (startIndex < tokenList.size()) {
            String currentToken = tokenList.get(startIndex);
            if (currentToken.equals("(")) {
                parenthesesCounter++;
            } else if (currentToken.equals(")")) {
                parenthesesCounter--;
                if (parenthesesCounter < -1) {
                    throw new ParseException("Scope closed without being opened", startIndex);
                } else if (parenthesesCounter == -1) {
                    tokenList.add(lambdaStart, "(");
                    tokenList.add(++startIndex, ")");
                    return;
                }
            }
            startIndex++;
        }

        if (parenthesesCounter < 0) {
            throw new ParseException("Scope closed without being opened", startIndex);
        } else if (parenthesesCounter == 0) {
            tokenList.add(lambdaStart, "(");
            tokenList.add(++startIndex, ")");
        }
    }

    private void preParse() throws ParseException {
        for (int index = 0; index < tokenList.size(); ++index) {
            if (tokenList.get(index).equals("\\") && (index == 0 || !tokenList.get(index - 1).equals("("))) {
                scanLambda(index);
            }
        }
    }

    private Expression scanVariable(String token) {
        if (storedVariables.containsKey(token)) {
            return storedVariables.get(token);
        }
        return new Variable(token);
    }

    private Expression parseHelper(int startIndex, int endIndex) {
        if (tokenList.get(startIndex).equals("\\")) {
            return new Function(new Variable(tokenList.get(startIndex + 1)), parseHelper(startIndex + 3, endIndex));
        } else if (startIndex == endIndex) {
            return scanVariable(tokenList.get(startIndex));
        } else {
            int oldEndIndex = endIndex;
            int parenthesesCounter = 0;
            int rightPartStart = endIndex;
            for (int index = endIndex; index >= startIndex; --index) {
                String currentToken = tokenList.get(index);
                if (currentToken.equals(")")) {
                    if (parenthesesCounter == 0) {
                        endIndex = index;
                    }
                    parenthesesCounter++;
                } else if (currentToken.equals("(")) {
                    parenthesesCounter--;
                    if (parenthesesCounter == 0) {
                        rightPartStart = index;
                        break;
                    }
                } else if (parenthesesCounter == 0) {
                    rightPartStart = index;
                    break;
                }
            }

            if (rightPartStart == endIndex) {
                return new Application(parseHelper(startIndex, rightPartStart - 1), scanVariable(tokenList.get(endIndex)));
            } else if (rightPartStart == startIndex && endIndex == oldEndIndex) {
                return parseHelper(startIndex + 1, endIndex - 1);
            } else {
                return new Application(parseHelper(startIndex, rightPartStart - 1),
                        parseHelper(rightPartStart + 1, endIndex - 1));
            }
        }
    }

    public Expression parse(ArrayList<String> tokens) throws ParseException {
        this.tokenList = tokens;

        if (tokens.size() > 2 && tokens.get(1).equals("=")) {
            String variableName = tokens.get(0);

            if (storedVariables.containsKey(variableName)) {
                return null;
            } else {
                tokens.remove(0);
                tokens.remove(0);

                if (tokens.get(0).equals("run")) {
                    tokens.remove(0);
                }

                preParse();
                Expression value = parseHelper(0, tokens.size() - 1);

                if (tokens.get(0).equals("run")) {
                    value = Console.run(value);
                }

                storedVariables.put(variableName, value);
                return value;
            }
        }

        if (tokens.get(0).equals("run")) {
            tokens.remove(0);
        }

        preParse();
        Expression value = parseHelper(0, tokens.size() - 1);

        if (tokens.get(0).equals("run")) {
            value = Console.run(value);
        }

        return value;
    }
}
