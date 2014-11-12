/**
 *
 */
package edu.buffalo.cse.irf14.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
//import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * @author nikhillo Class that represents a stream of Tokens. All
 * {@link Analyzer} and {@link TokenFilter} instances operate on this to
 * implement their behavior
 */
public class TokenStream implements Iterator<Token> {

    private ArrayList<Token> tokenList = null;
    private int posIndex = -1;
    private boolean isRemoved = false;
    private Map<String, ArrayList<Integer>> positionalIndexMap;
    private static Map<String, Integer> docLength = new HashMap<String, Integer>();
    private static int totalWordCount = 0;
    
    public TokenStream(ArrayList<Token> tokenList) {
        this.tokenList = tokenList;
    }

    /**
     * Method that checks if there is any Token left in the stream with regards
     * to the current pointer. DOES NOT ADVANCE THE POINTER
     *
     * @return true if at least one Token exists, false otherwise
     */
    @Override
    public boolean hasNext() {
        // TODO YOU MUST IMPLEMENT THIS
        return (posIndex < tokenList.size() - 1);
    }

    /**
     * Method to return the next Token in the stream. If a previous hasNext()
     * call returned true, this method must return a non-null Token. If for any
     * reason, it is called at the end of the stream, when all tokens have
     * already been iterated, return null
     */
    @Override
    public Token next() {
        // TODO YOU MUST IMPLEMENT THIS
        Token output = null;
        isRemoved = false;

        if (hasNext()) {
            output = tokenList.get(++posIndex);
        } else {
            ++posIndex;
        }

        return output;
    }

    /**
     * Method to remove the current Token from the stream. Note that "current"
     * token refers to the Token just returned by the next method. Must thus be
     * NO-OP when at the beginning of the stream or at the end
     */
    @Override
    public void remove() {
        // TODO YOU MUST IMPLEMENT THIS
        if (posIndex >= 0 && posIndex < tokenList.size()) {
            tokenList.remove(posIndex--);
            isRemoved = true;
        }
    }

    /**
     * Method to reset the stream to bring the iterator back to the beginning of
     * the stream. Unless the stream has no tokens, hasNext() after calling
     * reset() must always return true.
     */
    public void reset() {
        //TODO : YOU MUST IMPLEMENT THIS
        posIndex = -1;
    }

    /**
     * Method to append the given TokenStream to the end of the current stream
     * The append must always occur at the end irrespective of where the
     * iterator currently stands. After appending, the iterator position must be
     * unchanged Of course this means if the iterator was at the end of the
     * stream and a new stream was appended, the iterator hasn't moved but that
     * is no longer the end of the stream.
     *
     * @param stream : The stream to be appended
     */
    public void append(TokenStream stream) {
        //TODO : YOU MUST IMPLEMENT THIS
        if (stream != null && stream.tokenList.size() > 0) {
            tokenList.addAll(stream.tokenList);
        }
    }

    /**
     * Method to get the current Token from the stream without iteration. The
     * only difference between this method and {@link TokenStream#next()} is
     * that the latter moves the stream forward, this one does not. Calling this
     * method multiple times would not alter the return value of
     * {@link TokenStream#hasNext()}
     *
     * @return The current {@link Token} if one exists, null if end of stream
     * has been reached or the current Token was removed
     */
    public Token getCurrent() {
        //TODO: YOU MUST IMPLEMENT THIS
        if (isRemoved)
            return null;
        else
            return (posIndex > -1 && posIndex < tokenList.size()) ? tokenList.get(posIndex) : null;
    }

    
    //This will get count # of extra tokens from the current position of TokenStream
    //This method will not update the current poistion pointer of the token stream
    public ArrayList<Token> getExtraTokensCopy(int count) {
        ArrayList<Token> out = new ArrayList<Token>();
//        int extraTokensRequested = count;
        
        try {
            if(hasNext()){
                for (int i = 1; i <= count; i++) {
                    out.add(tokenList.get(posIndex+i));
                }
            }
        
//            //Incase there is no more Tokens to give, this will make up extra Tokens with empty string
//            while(extraTokensRequested > 0){
//                out.add(new Token(""));
//            }
        } catch(IndexOutOfBoundsException e){
            //Do nothing DONT MODIFY THIS CATCH or never throw this exception
        }
        return out;
    }
    
    //Gets a copy of Nth Token from current index of the pointer.
    //Returns null if the Token doesn't exist
    public Token getNthTokenCopy(int i) {
        Token out = null;
        try {
            out = tokenList.get(posIndex+i);
        } catch(IndexOutOfBoundsException e){
            //Do nothing DONT MODIFY THIS CATCH or never throw this exception
        }
        return out;
    }
    
    
    public Map<String,Integer> getUniqueSortedTerms(String doc){
        Map<String,Integer> temp = new HashMap<String,Integer>();
        int termPos = 0;
        
        //Store the number of words per doc
        if(docLength.containsKey(doc)){
        	docLength.put(doc, docLength.get(doc) + tokenList.size());
        } else {
        	docLength.put(doc, tokenList.size());
        }
        
        //Initialize/Truncate Map each time
        positionalIndexMap = new HashMap<String, ArrayList<Integer>>();
        
        for(Token tok: tokenList){
            if(tok.toString().isEmpty()){
                continue;
            }
                
            termPos++;  
            totalWordCount++;			//Count the number of words - to be used in ranking
        	String tokText = tok.toString();
        	
        	if(temp.containsKey(tokText)){
        		temp.put(tokText, temp.get(tokText)+1);
        		ArrayList<Integer> positionalList = positionalIndexMap.get(tokText);
        		positionalList.add(termPos);
        		positionalIndexMap.put(tokText, positionalList);
        	} else {
        		temp.put(tokText, 1);
        		positionalIndexMap.put(tokText, new ArrayList<Integer>(Arrays.asList(termPos)));
        	}
        }
        
        return temp;
    } 
    
    public Map<String,ArrayList<Integer>> getPositionalIndex(){
    	return positionalIndexMap;
    }
    
    //Return the doc length for the requested doc
    public static Integer getDocLength(String doc){
    	return docLength.get(doc);
    }
    
    //Return the total word count of the corpus
    public static int getTotalWordCount(){
    	return totalWordCount;
    }
}
