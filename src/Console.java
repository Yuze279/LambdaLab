import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;

public class Console {
    private static Scanner scanner;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        Lexer lexer = new Lexer();
        Parser parser = new Parser();

        String input = cleanConsoleInput();

        while (!input.equalsIgnoreCase("exit")) {
            ArrayList<String> tokens = lexer.tokenize(input);
            boolean isVariableAssignment = tokens.size() > 2 && tokens.get(1).equals("=");
            String variableName = "";
            if (isVariableAssignment) {
                variableName = tokens.get(0);
            }

            String output = "";
            try {
                if (tokens.isEmpty()) {
                    input = cleanConsoleInput();
                    continue;
                }
                Expression exp = parser.parse(tokens);

                if (isVariableAssignment && exp == null) {
                    output = tokens.get(0) + " has already been defined.";
                }
                else if (isVariableAssignment) {
                    output = "Added " + exp + " as " + variableName;
                } else {
                    output = exp.toString();
                }
            } catch (Exception e) {
                System.out.println("Unparsable expression, input was: \"" + input + "\"");
                input = cleanConsoleInput();
                continue;
            }

            System.out.println(output);

            input = cleanConsoleInput();
        }

        System.out.println("Goodbye!");
    }

    private static String cleanConsoleInput() {
        System.out.print("> ");
        String raw = scanner.nextLine();
        String deBOMified = raw.replaceAll("\uFEFF", ""); // remove Byte Order Marker from UTF

        String clean = removeWeirdWhitespace(deBOMified);

        return clean.replaceAll("λ", "\\\\");
    }

    public static String removeWeirdWhitespace(String input) {
    	 String whitespaceChars = "\\u0009" // CHARACTER TABULATION
                 + "\\u000A" // LINE FEED (LF)
                 + "\\u000B" // LINE TABULATION
                 + "\\u000C" // FORM FEED (FF)
                 + "\\u000D" // CARRIAGE RETURN (CR)
                 + "\\u0020" // SPACE
                 + "\\u0085" // NEXT LINE (NEL)
                 + "\\u00A0" // NO-BREAK SPACE
                 + "\\u1680" // OGHAM SPACE MARK
                 + "\\u180E" // MONGOLIAN VOWEL SEPARATOR
                 + "\\u2000" // EN QUAD
                 + "\\u2001" // EM QUAD
                 + "\\u2002" // EN SPACE
                 + "\\u2003" // EM SPACE
                 + "\\u2004" // THREE-PER-EM SPACE
                 + "\\u2005" // FOUR-PER-EM SPACE
                 + "\\u2006" // SIX-PER-EM SPACE
                 + "\\u2007" // FIGURE SPACE
                 + "\\u2008" // PUNCTUATION SPACE
                 + "\\u2009" // THIN SPACE
                 + "\\u200A" // HAIR SPACE
                 + "\\u2028" // LINE SEPARATOR
                 + "\\u2029" // PARAGRAPH SEPARATOR
                 + "\\u202F" // NARROW NO-BREAK SPACE
                 + "\\u205F" // MEDIUM MATHEMATICAL SPACE
                 + "\\u3000"; // IDEOGRAPHIC SPACE
    	 
        Pattern whitespace = Pattern.compile(whitespaceChars);
        Matcher matcher = whitespace.matcher(input);
        String result = input;
        if (matcher.find()) {
            result = matcher.replaceAll(" ");
        }

        return result;
    }

    public static final HashSet<String> freeVarNames = new HashSet<>();

    private static Expression runHelper(Expression exp) {
        while (true) {
            if (exp instanceof Application app) {
                if (app.left instanceof Function func) {
                    Variable var = func.variable;
                    Expression funcExp = func.expression;
                    Expression subExp = app.right.copy();
                    if (subExp instanceof Variable freeVar) {
                        freeVarNames.add(freeVar.name);
                    }
                    exp = funcExp.sub(var, subExp);
                } else {
                    Expression temp = runHelper(app.left);
                    if (temp != null) {
                        app.left = temp;
                        return app;
                    }
                    temp = runHelper(app.right);
                    if (temp != null) {
                        app.right = temp;
                        return app;
                    }
                    return null;
                }
            } else if (exp instanceof Function func) {
                Expression temp = runHelper(func.expression);
                if (temp != null) {
                    func.expression = temp;
                    return func;
                }
                return null;
            } else {
                return null;
            }
        }
    }

    public static Expression run(Expression exp) {
        freeVarNames.clear();
        Expression subExp = runHelper(exp);
        while (subExp != null) {
            exp = subExp;
            subExp = runHelper(exp);
        }
        for (String key : Parser.storedVariables.keySet()) {
            if (exp.equals(Parser.storedVariables.get(key))) {
                return new Variable(key);
            }
        }
        return exp;
    }
}
