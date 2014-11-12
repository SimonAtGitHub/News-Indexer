/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.buffalo.cse.irf14.analysis;

import edu.buffalo.cse.irf14.util.text.StringManipulator;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 *
 * @author san
 */
public class CapitalizationTokenFilter extends TokenFilter {

    /**
     * @param stream
     */
    public CapitalizationTokenFilter(TokenStream stream) {
        super(stream);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see edu.buffalo.cse.irf14.analysis.Analyzer#increment()
     */
    @Override
    public boolean increment() throws TokenizerException {
        // TODO Auto-generated method stub
        boolean out = false;
        
        try {
            if (getStream().hasNext()) {
                Token currToken = getStream().next();
                String currTokenContent = currToken.toString();
                
                if(StringManipulator.isAllUpperCase_safe(currTokenContent)){
                    //do nothing
                    return getStream().hasNext();
                } 
                if(StringManipulator.isCamelCase_safe(currTokenContent)) {
                    StringBuilder contentToUpdate = new StringBuilder(currTokenContent.toString());
                    int extraTokensRead = 0;
                    int nthCopyOfTokenNeeded = 1;
                    boolean proceedNext = false;
                    do {
                            
                            Token nextToken = getStream().getNthTokenCopy(nthCopyOfTokenNeeded++);
                            if(nextToken != null && StringManipulator.isCamelCase_safe(nextToken.toString())){
                                contentToUpdate.append(" ").append(nextToken.toString());
                                proceedNext = true;
                                extraTokensRead++;
                            } else {
                                proceedNext = false;
                            }
                        }while(proceedNext);
                    
                    if(extraTokensRead == 0){
                        if(currToken.isFirstWordInSent()){
                            currToken.setTermText(currToken.getTermText().toLowerCase());
                        }
                    } else {
                        currToken.setTermText(contentToUpdate.toString());
                        while(extraTokensRead != 0){
                            getStream().next().setTermText("");
                            extraTokensRead--;
                        }
                    }
                }
                return getStream().hasNext();
            }
        } catch (Exception e) {
            throw new TokenizerException(e);
        }
        return getStream().hasNext();
    }

    /* (non-Javadoc)
     * @see edu.buffalo.cse.irf14.analysis.Analyzer#getStream()
     */
    @Override
    public TokenStream getStream() {
        // TODO Auto-generated method stub
        return getTokenStream();
    }

    public TokenStream processEntireTokenStream() throws TokenizerException {
        TokenStream out = getStream();

        try {
            while (increment()) {

            }
            getStream().reset();
        } catch (Exception e) {
            throw new TokenizerException(e);
        }

        return out;
    }

    
    public static void main(String[] args) throws TokenizerException {
        
        TokenFilter test = new CapitalizationTokenFilter(null);
        
        String rv[] = ((CapitalizationTokenFilter) test).runTest(TokenFilterType.CAPITALIZATION,"The. Though. For each. JAMES Cannot Be is Java Cup.");
        System.out.println("\n");
        for(String i: rv){
            System.out.print(i + "|");
        }

    }
    
    public static String[] runTest(TokenFilterType type, String str) throws TokenizerException {
		Tokenizer tkizer = new Tokenizer();
		TokenStream tstream = tkizer.consume(str);
		TokenFilterFactory factory = TokenFilterFactory.getInstance();
		TokenFilter filter = factory.getFilterByType(type, tstream);
		
		while (filter.increment()) {
			//Do nothing :/
		}
		
		tstream = filter.getStream();
		tstream.reset();
		
		ArrayList<String> list = new ArrayList<String>();
		String s;
		Token t;

		while (tstream.hasNext()) {
			t = tstream.next();

			if (t != null) {
				s = t.toString();
				
				if (s!= null && !s.isEmpty())
					list.add(s);	
			}
		}
		
		String[] rv = new String[list.size()];
		rv = list.toArray(rv);
		tkizer = null;
		tstream = null;
		filter = null;
		list = null;
		return rv;
	}
    
}
