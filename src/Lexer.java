import java.util.ArrayList;
import java.util.Arrays;

public class Lexer {
    static final ArrayList<String> SPECIAL_CHARACTERS = new ArrayList<>(Arrays.asList("\\", ".", "(", ")", "λ", "="));

    /*
     * Given the input
     * (\bat .bat flies)cat λg.joy! )
     * you should output the ArrayList of strings
     * [(, \, bat, ., bat, flies, ), cat, \, g, ., joy!, )]
     * 
     * Seperate special chars from words and spaces building words, spaces, characters to tokens
     */
    public ArrayList<String> tokenize(String input) {
        ArrayList<String> tokens = new ArrayList<>();
        // Use an Array List for Lexer
        
        for (int index = 0; index < input.length() && input.charAt(index) != ';';) {
            String current = input.substring(index, index + 1);
            if (current.equals(";")) {
                break;
            } else if (current.equals(" ")) {
                ++index;
            } else if (SPECIAL_CHARACTERS.contains(current)) {
                
            	
            	if (current.equals("λ")) {
                    tokens.add("\\");
                } 
            	else {
                    tokens.add(current);
                }
                ++index;
            } 
            
            
            //If we are getting a nonspace or special character
            else {
                int start = index;
                while (!(index == input.length() 
                		|| input.charAt(index) == ';' 
                		|| input.charAt(index) == ' '
                        || SPECIAL_CHARACTERS.contains(input.substring(index, index + 1)))) {
                    ++index;
                }
                tokens.add(input.substring(start, index));
            }
        }
        return tokens;
    }
}
