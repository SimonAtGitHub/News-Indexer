/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.buffalo.cse.irf14.analysis;

import edu.buffalo.cse.irf14.util.text.Stemmer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author san
 */
public class StemmerTokenFilter extends TokenFilter {

    private static Pattern patrn_NON_ALPHABETS = Pattern.compile("[^\\p{Alpha}]");
    
    /**
     * @param stream
     */
    public StemmerTokenFilter(TokenStream stream) {
        super(stream);
    }

    /* (non-Javadoc)
     * @see edu.buffalo.cse.irf14.analysis.Analyzer#increment()
     */
    @Override
    public boolean increment() throws TokenizerException {
        // TODO Auto-generated method stub
        boolean out = false;
        Matcher mtchr_NON_ALPHABETS = null;
        
        try {
            //Write the unit business process for cleaning a single token.
            if (getStream().hasNext()) {
                Stemmer stemmer = new Stemmer();
                Token wip = getStream().next();

                char[] currTokenContent = wip.getTermBuffer();
                String originalTokenContent = String.copyValueOf(currTokenContent);
                mtchr_NON_ALPHABETS = patrn_NON_ALPHABETS.matcher(originalTokenContent);
//                if (originalTokenContent.matches("^[A-Za-z]*$")) {
                if (!mtchr_NON_ALPHABETS.find()) {    
                    for (char c : currTokenContent) {
                        stemmer.add(c);
                    }

                    stemmer.stem();
                    String stemmedContent = stemmer.toString();
                    if (!originalTokenContent.equals(stemmedContent)) {
                        wip.setTermText(stemmedContent);
                    }
                }
                
                mtchr_NON_ALPHABETS.reset();
                out = true;

            }

        } catch (Exception e) {
            throw new TokenizerException(e);
        }
        return out;
    }

    /* (non-Javadoc)
     * @see edu.buffalo.cse.irf14.analysis.Analyzer#getStream()
     */
    @Override
    public TokenStream getStream() {
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

}
