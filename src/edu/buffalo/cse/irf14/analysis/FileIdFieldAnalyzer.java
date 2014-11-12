/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.buffalo.cse.irf14.analysis;

/**
 *
 * @author san
 */
public class FileIdFieldAnalyzer implements Analyzer{
    
    private TokenStream stream;

    public FileIdFieldAnalyzer(TokenStream stream) {
        //TODO : YOU MUST IMPLEMENT THIS METHOD
        this.stream = stream;
    }

    @Override
    public boolean increment() throws TokenizerException {
        boolean out = false;
        //NO FILTERING REQUIRED
//        try {
//            TokenFilterFactory tokenFilterFactory = TokenFilterFactory.getInstance();
//
//            TokenFilter symbolTokenFilter = tokenFilterFactory.getFilterByType(TokenFilterType.SYMBOL, getStream());
//            ((SymbolsTokenFilter) symbolTokenFilter).processEntireTokenStream();
//
//            TokenFilter accentTokenFilter = tokenFilterFactory.getFilterByType(TokenFilterType.ACCENT, getStream());
//            ((AccentsTokenFilter) accentTokenFilter).processEntireTokenStream();
//
//            TokenFilter specialcharsTokenFilter = tokenFilterFactory.getFilterByType(TokenFilterType.SPECIALCHARS, getStream());
//            ((SpecialCharsTokenFilter) specialcharsTokenFilter).processEntireTokenStream();
//
////            TokenFilter dateTokenFilter = tokenFilterFactory.getFilterByType(TokenFilterType.DATE, getStream());
////            ((DateTokenFilter) dateTokenFilter).processEntireTokenStream();
//
////            TokenFilter numbersTokenFilter = tokenFilterFactory.getFilterByType(TokenFilterType.NUMERIC, getStream());
////            ((NumericTokenFilter) numbersTokenFilter).processEntireTokenStream();
//
//            TokenFilter capitalizationTokenFilter = tokenFilterFactory.getFilterByType(TokenFilterType.CAPITALIZATION, getStream());
//            ((CapitalizationTokenFilter) capitalizationTokenFilter).processEntireTokenStream();
//
////            TokenFilter stemmerTokenFilter = tokenFilterFactory.getFilterByType(TokenFilterType.STEMMER, getStream());
////            ((StemmerTokenFilter) stemmerTokenFilter).processEntireTokenStream();
//
////            TokenFilter stopwordTokenFilter = tokenFilterFactory.getFilterByType(TokenFilterType.STOPWORD, getStream());
////            ((StopwordTokenFilter) stopwordTokenFilter).processEntireTokenStream();
//
//        } catch (Exception e) {
//            throw new TokenizerException(e);
//        }

        return out;
    }

    @Override
    public TokenStream getStream() {
        return this.stream;
    }
    
}
