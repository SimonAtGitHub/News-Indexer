/**
 *
 */
package edu.buffalo.cse.irf14.analysis;

import java.util.ArrayList;
//import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author nikhillo Class that converts a given string into a
 * {@link TokenStream} instance
 */
public class Tokenizer {

    private String delimiter;
    
    //To find if the prior Token is last word in a sentense (Note: Use find method)
    private static Pattern isPrevTokenWithDot_Pattern = Pattern.compile("\\.$", Pattern.CASE_INSENSITIVE);
    //To find if the Current Token starts with Upper case (Note: Use find method)
    private static Pattern isCurrentTokenStartsUpper_Pattern = Pattern.compile("^[A-Z]");
    /**
     * Default constructor. Assumes tokens are whitespace delimited
     */
    public Tokenizer() {
        //TODO : YOU MUST IMPLEMENT THIS METHOD
        this.delimiter = "\\s+";
    }

    /**
     * Overloaded constructor. Creates the tokenizer with the given delimiter
     *
     * @param delim : The delimiter to be used
     */
    public Tokenizer(String delim) {
        //TODO : YOU MUST IMPLEMENT THIS METHOD
    	this.delimiter = delim;
    }

    /**
     * Method to convert the given string into a TokenStream instance. This must
     * only break it into tokens and initialize the stream. No other processing
     * must be performed. Also the number of tokens would be determined by the
     * string and the delimiter. So if the string were "hello world" with a
     * whitespace delimited tokenizer, you would get two tokens in the stream.
     * But for the same text used with lets say "~" as a delimiter would return
     * just one token in the stream.
     *
     * @param str : The string to be consumed
     * @return : The converted TokenStream as defined above
     * @throws TokenizerException : In case any exception occurs during
     * tokenization
     */
    public TokenStream consume(String str) throws TokenizerException {
        //TODO : YOU MUST IMPLEMENT THIS METHOD
        TokenStream out = null;
        try {

            if (str != null && !str.isEmpty()) {
                String[] t = str.split(this.delimiter);
                ArrayList<Token> a = new ArrayList<Token>();
                Matcher findPrevWordEndsWithDot = null;
                Matcher findCurrWordStartsWithUpp = null;
                
                Token prevToken = null;
                Token currToken = null;
                for(String temp :t){
                    
                    currToken = new Token(temp);
                    if(prevToken == null) {
                        currToken.setFirstWordInSentFlag(true);
                    } else {
                        findPrevWordEndsWithDot = isPrevTokenWithDot_Pattern.matcher(prevToken.toString());
                        if(findPrevWordEndsWithDot.find()){
                            findCurrWordStartsWithUpp = isCurrentTokenStartsUpper_Pattern.matcher(currToken.toString());
                            if(findCurrWordStartsWithUpp.find()){
                                currToken.setFirstWordInSentFlag(true);
                            } 
                        }
                    }
                    
                    a.add(currToken);
                    
                    prevToken = currToken;
                    
                    //Resets the matcher to increase re-use of the same matcher
                    if(findPrevWordEndsWithDot != null){
                        findPrevWordEndsWithDot.reset();
                    }
                    if(findCurrWordStartsWithUpp != null){
                        findCurrWordStartsWithUpp.reset();
                    }
                }
                
                out = new TokenStream(a);
                
            } else {
            	throw new TokenizerException("String passed for tokenizer is either null or Empty");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new TokenizerException(e);
        }
        return out;
    }
    
    

    public static void main(String[] args) {
        Tokenizer tok = new Tokenizer();
        try {
            String t = "Computer Terminal also said it sold the technolgy rights to its Dot Matrix impact technology, including any future improvements, to <Woodco Inc> of Houston, Tex. for 200,000 dlrs. But, it said it would continue to be the exclusive worldwide licensee of the technology for Woodco.";
            
            TokenStream temp = tok.consume(t);
            
            
            while(temp.hasNext()){
                Token token = temp.next();
                if(token.isFirstWordInSent())
                System.out.println(token.toString() + "|" + token.isFirstWordInSent());
            }
            /*
            TokenStream temp2 = tok.consume("XXX YYY ZZZ");
            
            int size = 0;
            temp2.remove();
            
            while(temp.hasNext()){
            	System.out.print(temp.next().getTermText() + "||");
//            	temp.next();
            	size++;
            	if(size > 3) temp.reset();
            }
            
            temp.append(temp2);
            System.out.println("");
            while(temp.hasNext()){
            	
            	System.out.print(temp.next().getTermText() + "||");
//            	temp.next();
            	size++;
            }
            System.out.println("Total size: " + size);
            */
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
