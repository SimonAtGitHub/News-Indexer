/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author harish.v
 *
 */
public class NumericTokenFilter extends TokenFilter {

        private static String IS_NUMBER = "(?:\\+|\\-|$)?(?:\\d+[,\\.]?)*";
        
        //Use find()
	private static Pattern pattern_IS_NUMBER = Pattern.compile(IS_NUMBER);
    
        //private ArrayList<Token> token;
	/**
	 * @param stream
	 */
	public NumericTokenFilter(TokenStream stream) {
		// TODO Auto-generated constructor stub
		super(stream);
		//token = new ArrayList<Token>();
	}

	/* (non-Javadoc)
	 * @see edu.buffalo.cse.irf14.analysis.Analyzer#increment()
	 */
	@Override
	public boolean increment() throws TokenizerException {
		// TODO Auto-generated method stub
		boolean output = false;
		Matcher matcher_IS_NUMBER;
		if (getStream().hasNext()){
			output = true;
			try {
				Token tok = getStream().next();
				String text = tok.toString();
				
                                /*
                                if (!text.matches("^([01][0-9]|20)[0-9][0-9](0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])$")){
					if ((text.matches("^[0-9]*$")) || (text.matches("^[0-9]*\\.?\\,?[0-9]*$"))){
						getStream().remove();
					} else if (text.matches("^[0-9]+\\.?\\/?[0-9]+(.*)$")){
						text = text.replaceAll("[0-9]+\\.?[0-9]+", "");
						tok.setTermText(text);
					} else 
						tok.setTermText(text);
				}
                                */
                                if(!tok.isDateTime()){
                                    matcher_IS_NUMBER = pattern_IS_NUMBER.matcher(text);
                                    
                                    if(matcher_IS_NUMBER.find()){
                                        tok.setTermText(text.replaceAll(IS_NUMBER, ""));
                                    }
                                    
                                    matcher_IS_NUMBER.reset();//For reusing the same matcher
                                }
                                
			}catch (Exception e){
				throw new TokenizerException(e);
			}			
		}
		return output;
	}

	/* (non-Javadoc)
	 * @see edu.buffalo.cse.irf14.analysis.Analyzer#getStream()
	 */
	@Override
	public TokenStream getStream() {
		// TODO Auto-generated method stub
		return getTokenStream();
	}
	
	public TokenStream processEntireTokenStream() throws TokenizerException{
		TokenStream out = getStream();
		
		try{
			while(increment()){
				
			}
                        getStream().reset();
		}catch(Exception e){
			throw new TokenizerException(e);
		}
				
		return out;
	}
	
	public static void main(String[] args){
		Tokenizer to = new Tokenizer();
		try {
			TokenStream tstream = to.consume("The number 13 is the sixth prime number");	
			TokenFilter obj = new NumericTokenFilter(tstream);
			
			while(obj.increment());
			
			tstream = obj.getStream();
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
			
			for (String prin:rv) System.out.print(prin + " ");
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
