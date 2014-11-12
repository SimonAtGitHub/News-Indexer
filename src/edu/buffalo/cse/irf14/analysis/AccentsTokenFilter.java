/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.buffalo.cse.irf14.analysis;

import java.util.HashMap;

/**
 *
 * @author san
 */
public class AccentsTokenFilter extends TokenFilter {

    private static HashMap<Character, String> listOfAccents = new HashMap<Character, String>();

    //Initializes the static Accent and replacement list.
    static {
        accentListPopulator();
    }

    /**
     * @param stream
     */
    public AccentsTokenFilter(TokenStream stream) {
        super(stream);
    }

    /* (non-Javadoc)
     * @see edu.buffalo.cse.irf14.analysis.Analyzer#increment()
     */
    @Override
    public boolean increment() throws TokenizerException {
        // TODO Auto-generated method stub
        boolean out = false;

        try {
            //Write the unit business process for cleaning a single token.
            if (getStream().hasNext()) {

                Token wip = getStream().next();
                char[] currTokenContent = wip.getTermBuffer();
                String currTokenStringWIP = String.copyValueOf(currTokenContent);

                for (int i = 0, size = currTokenContent.length; i < size; i++) {
                    char currChar = currTokenContent[i];
                    if (listOfAccents.containsKey(currChar)) {
                        currTokenStringWIP = currTokenStringWIP.replaceAll(String.valueOf(currChar), listOfAccents.get(currChar));
                    }
                }

                wip.setTermText(currTokenStringWIP);

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

    //Populates the list of accent characters which needs to be replaced in the Tokens
    private static void accentListPopulator() {

        //# À => A
        listOfAccents.put('\u00C0', "A");
        //"\u00C0" => "A"

        //# Á => A
        listOfAccents.put('\u00C1', "A");
        //"\u00C1" => "A"

        //# Â => A
        listOfAccents.put('\u00C2', "A");
        //"\u00C2" => "A"

        //# Ã => A
        listOfAccents.put('\u00C3', "A");
        //"\u00C3" => "A"

        //# Ä => A
        listOfAccents.put('\u00C4', "A");
        //"\u00C4" => "A"

        //# Å => A
        listOfAccents.put('\u00C5', "A");
        //"\u00C5" => "A"

        //# Æ => AE
        listOfAccents.put('\u00C6', "AE");
        //"\u00C6" => "AE"

        //# Ç => C
        listOfAccents.put('\u00C7', "C");
        //"\u00C7" => "C"

        //# È => E
        listOfAccents.put('\u00C8', "E");
        //"\u00C8" => "E"

        //# É => E
        listOfAccents.put('\u00C9', "E");
        //"\u00C9" => "E"

        //# Ê => E
        listOfAccents.put('\u00CA', "E");
        //"\u00CA" => "E"

        //# Ë => E
        listOfAccents.put('\u00CB', "E");
        //"\u00CB" => "E"

        //# Ì => I
        listOfAccents.put('\u00CC', "I");
        //"\u00CC" => "I"

        //# Í => I
        listOfAccents.put('\u00CD', "I");
        //"\u00CD" => "I"

        //# Î => I
        listOfAccents.put('\u00CE', "I");
        //"\u00CE" => "I"

        //# Ï => I
        listOfAccents.put('\u00CF', "I");
        //"\u00CF" => "I"

        //# Ĳ => IJ
        listOfAccents.put('\u0132', "IJ");
        //"\u0132" => "IJ"

        //# Ð => D
        listOfAccents.put('\u00D0', "D");
        //"\u00D0" => "D"

        //# Ñ => N
        listOfAccents.put('\u00D1', "N");
        //"\u00D1" => "N"

        //# Ò => O
        listOfAccents.put('\u00D2', "O");
        //"\u00D2" => "O"

        //# Ó => O
        listOfAccents.put('\u00D3', "O");
        //"\u00D3" => "O"

        //# Ô => O
        listOfAccents.put('\u00D4', "O");
        //"\u00D4" => "O"

        //# Õ => O
        listOfAccents.put('\u00D5', "O");
        //"\u00D5" => "O"

        //# Ö => O
        listOfAccents.put('\u00D6', "O");
        //"\u00D6" => "O"

        //# Ø => O
        listOfAccents.put('\u00D8', "O");
        //"\u00D8" => "O"

        //# Œ => OE
        listOfAccents.put('\u0152', "OE");
        //"\u0152" => "OE"

        //# Þ
        listOfAccents.put('\u00DE', "TH");
        //"\u00DE" => "TH"

        //# Ù => U
        listOfAccents.put('\u00D9', "U");
        //"\u00D9" => "U"

        //# Ú => U
        listOfAccents.put('\u00DA', "U");
        //"\u00DA" => "U"

        //# Û => U
        listOfAccents.put('\u00DB', "U");
        //"\u00DB" => "U"

        //# Ü => U
        listOfAccents.put('\u00DC', "U");
        //"\u00DC" => "U"

        //# Ý => Y
        listOfAccents.put('\u00DD', "Y");
        //"\u00DD" => "Y"

        //# Ÿ => Y
        listOfAccents.put('\u0178', "Y");
        //"\u0178" => "Y"

        //# à => a
        listOfAccents.put('\u00E0', "a");
        //"\u00E0" => "a"

        //# á => a
        listOfAccents.put('\u00E1', "a");
        //"\u00E1" => "a"

        //# â => a
        listOfAccents.put('\u00E2', "a");
        //"\u00E2" => "a"

        //# ã => a
        listOfAccents.put('\u00E3', "a");
        //"\u00E3" => "a"

        //# ä => a
        listOfAccents.put('\u00E4', "a");
        //"\u00E4" => "a"

        //# å => a
        listOfAccents.put('\u00E5', "a");
        //"\u00E5" => "a"

        //# æ => ae
        listOfAccents.put('\u00E6', "ae");
        //"\u00E6" => "ae"

        //# ç => c
        listOfAccents.put('\u00E7', "c");
        //"\u00E7" => "c"

        //# è => e
        listOfAccents.put('\u00E8', "e");
        //"\u00E8" => "e"

        //# é => e
        listOfAccents.put('\u00E9', "e");
        //"\u00E9" => "e"

        //# ê => e
        listOfAccents.put('\u00EA', "e");
        //"\u00EA" => "e"

        //# ë => e
        listOfAccents.put('\u00EB', "e");
        //"\u00EB" => "e"

        //# ì => i
        listOfAccents.put('\u00EC', "i");
        //"\u00EC" => "i"

        //# í => i
        listOfAccents.put('\u00ED', "i");
        //"\u00ED" => "i"

        //# î => i
        listOfAccents.put('\u00EE', "i");
        //"\u00EE" => "i"

        //# ï => i
        listOfAccents.put('\u00EF', "i");
        //"\u00EF" => "i"

        //# ĳ => ij
        listOfAccents.put('\u0133', "ij");
        //"\u0133" => "ij"

        //# ð => d
        listOfAccents.put('\u00F0', "d");
        //"\u00F0" => "d"

        //# ñ => n
        listOfAccents.put('\u00F1', "n");
        //"\u00F1" => "n"

        //# ò => o
        listOfAccents.put('\u00F2', "o");
        //"\u00F2" => "o"

        //# ó => o
        listOfAccents.put('\u00F3', "o");
        //"\u00F3" => "o"

        //# ô => o
        listOfAccents.put('\u00F4', "o");
        //"\u00F4" => "o"

        //# õ => o
        listOfAccents.put('\u00F5', "o");
        //"\u00F5" => "o"

        //# ö => o
        listOfAccents.put('\u00F6', "o");
        //"\u00F6" => "o"

        //# ø => o
        listOfAccents.put('\u00F8', "o");
        //"\u00F8" => "o"

        //# œ => oe
        listOfAccents.put('\u0153', "oe");
        //"\u0153" => "oe"

        //# ß => ss
        listOfAccents.put('\u00DF', "ss");
        //"\u00DF" => "ss"

        //# þ => th
        listOfAccents.put('\u00FE', "th");
        //"\u00FE" => "th"

        //# ù => u
        listOfAccents.put('\u00F9', "u");
        //"\u00F9" => "u"

        //# ú => u
        listOfAccents.put('\u00FA', "u");
        //"\u00FA" => "u"

        //# û => u
        listOfAccents.put('\u00FB', "u");
        //"\u00FB" => "u"

        //# ü => u
        listOfAccents.put('\u00FC', "u");
        //"\u00FC" => "u"

        //# ý => y
        listOfAccents.put('\u00FD', "y");
        //"\u00FD" => "y"

        //# ÿ => y
        listOfAccents.put('\u00FF', "y");
        //"\u00FF" => "y"

        //# ﬀ => ff
        listOfAccents.put('\uFB00', "ff");
        //"\uFB00" => "ff"

        //# ﬁ => fi
        listOfAccents.put('\uFB01', "fi");
        //"\uFB01" => "fi"

        //# ﬂ => fl
        listOfAccents.put('\uFB02', "fl");
        //"\uFB02" => "fl"

        //# ﬃ => ffi
        listOfAccents.put('\uFB03', "ffi");
        //"\uFB03" => "ffi"

        //# ﬄ => ffl
        listOfAccents.put('\uFB04', "ffl");
        //"\uFB04" => "ffl"

        //# ﬅ => ft
        listOfAccents.put('\uFB05', "ft");
        //"\uFB05" => "ft"

        //# ﬆ => st
        listOfAccents.put('\uFB06', "st");
        //"\uFB06" => "st"

    }

    //TODO: Sankar: REMOVE THE BELOW TWO METHODS FOR PROD
    //**********************************************************REMOVE THE BELOW TWO METHODS FOR PROD***********************************************
    //**********************************************************REMOVE THE BELOW TWO METHODS FOR PROD***********************************************
    //**********************************************************REMOVE THE BELOW TWO METHODS FOR PROD***********************************************
    public static void main(String[] args) throws TokenizerException {

    }

}
