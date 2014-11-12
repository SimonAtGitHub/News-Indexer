/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import java.util.ArrayList;

/**
 * @author harish.v
 *
 */
public class SpecialCharsTokenFilter extends TokenFilter {

	/**
	 * @param stream
	 */
	public SpecialCharsTokenFilter(TokenStream stream) {
		super(stream);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see edu.buffalo.cse.irf14.analysis.Analyzer#increment()
	 */
	@Override
	public boolean increment() throws TokenizerException {
		// TODO Auto-generated method stub
		boolean output = false;
		
		if (getStream().hasNext()){
			output = true;
			try{
				//Write the unit business process for cleaning a single token.
				Token tok = getStream().next();
				
				String text = tok.getTermText();
				if (text.matches("^.*([a-zA-Z][+*/-][a-zA-Z])+.*$"))
					text = text.replaceAll("-", "");
				text = text.replaceAll("[\\p{Punct}&&[^\\.\\?\\!\\-]]","");

				if(text.isEmpty())
					getStream().remove();
				else
					tok.setTermText(text);
			}catch(Exception e){
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
			TokenStream tstream = to.consume("email is test@buffalo.edu");
			TokenFilter obj = new SpecialCharsTokenFilter(tstream);
			
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
			
			for (String prin:rv) System.out.println(prin);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
