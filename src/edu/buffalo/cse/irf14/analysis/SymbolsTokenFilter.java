/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import java.util.ArrayList;
//import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author harish.v
 *
 */
public class SymbolsTokenFilter extends TokenFilter {

	private ArrayList<Token> token;
	private static HashMap<String,String> contraction= new HashMap<String,String>();
	static {
		contraction.put("ain't", "am not");
		contraction.put("aren't", "are not");
		contraction.put("can't", "cannot");
		contraction.put("could've", "could have");
		contraction.put("couldn't", "could not");
		contraction.put("couldn't've", "could not have");
		contraction.put("didn't", "did not");
		contraction.put("doesn't", "does not");
		contraction.put("don't", "do not");
		contraction.put("hadn't", "had not");
		contraction.put("hadn't've", "had not have");
		contraction.put("hasn't", "has not");
		contraction.put("haven't", "have not");
		contraction.put("he'd", "he would");
		contraction.put("he'd've", "he would have");
		contraction.put("he'll", "he will");
		contraction.put("i'd", "I would");
		contraction.put("i'd've", "I would have");
		contraction.put("i'll", "I will");
		contraction.put("i'm", "I am");
		contraction.put("i've", "I have");
		contraction.put("isn't", "is not");
		contraction.put("it'd", "it would");
		contraction.put("it'd've", "it would have");
		contraction.put("it'll", "it will");
		contraction.put("ma'am", "madam");
		contraction.put("mightn't", "might not");
		contraction.put("mightn't've", "might not have");
		contraction.put("might've", "might have");
		contraction.put("mustn't", "must not");
		contraction.put("must've", "must have");
		contraction.put("needn't", "need not");
		contraction.put("not've", "not have");
		contraction.put("o'clock", "of the clock");
		contraction.put("shan't", "shall not");
		contraction.put("she'd", "she would");
		contraction.put("she'd've", "she would have");
		contraction.put("she'll", "she will");
		contraction.put("should've", "should have");
		contraction.put("shouldn't", "should not");
		contraction.put("shouldn't've", "should not have");
		contraction.put("there'd", "there would");
		contraction.put("there'd've", "there would have");
		contraction.put("there're", "there are");
		contraction.put("they'd", "they would");
		contraction.put("they'd've", "they would have");
		contraction.put("they'll", "they will");
		contraction.put("they're", "they are");
		contraction.put("they've", "they have");
		contraction.put("wasn't", "was not");
		contraction.put("we'd", "we would");
		contraction.put("we'd've", "we would have");
		contraction.put("we'll", "we will");
		contraction.put("we're", "we are");
		contraction.put("we've", "we have");
		contraction.put("weren't", "were not");
		contraction.put("what'll", "what will");
		contraction.put("what're", "what are");
		contraction.put("what've", "what have");
		contraction.put("where'd", "where did");
		contraction.put("where've", "where have");
		contraction.put("who'd", "who would");
		contraction.put("who'll", "who will");
		contraction.put("who're", "who are");
		contraction.put("who've", "who have");
		contraction.put("why'll", "why will");
		contraction.put("why're", "why are");
		contraction.put("won't", "will not");
		contraction.put("would've", "would have");
		contraction.put("would'nt've", "would not have");
		contraction.put("y'all", "you all");
		contraction.put("y'all'd've", "you all would have");
		contraction.put("you'd", "you would");
		contraction.put("you'd've", "you would have");
		contraction.put("you'll", "you will");
		contraction.put("you're", "you are");
		contraction.put("you've", "you have");
		contraction.put("'em", "them");
	}
	/**
	 * @param stream
	 */
	public SymbolsTokenFilter(TokenStream stream) {		
		// TODO Auto-generated constructor stub
		super(stream);
		token = new ArrayList<Token>();
	}

	/* (non-Javadoc)
	 * @see edu.buffalo.cse.irf14.analysis.Analyzer#increment()
	 */
	@Override
	public boolean increment() throws TokenizerException {
		// TODO Auto-generated method stub
		boolean out = false;
		List<String> result;
		
		if (getStream().hasNext()){
			out = true;
			try{
				Token tok = getStream().next();
				
				String text = tok.getTermText();
				text = checkApostrophe(text);
				text = checkPunctuations(text);
				text = checkContractions(text);
				/*Token[] list = new Token[result.size()];
				int cnt = 0;
				for (String buf: result){
					if (buf != null && !buf.isEmpty())
						list[cnt++] = new Token(buf);
				}
				
				if (cnt > 1)
                     tok.merge(list);
                else
                     tok.setTermText(result.get(0));*/
				
				text = checkHyphens(text);
				/*if (result.size() > 1) {
					list = new Token[result.size()];
					cnt = 0;
					for (String buf: result){
						if (buf != null && !buf.isEmpty())
							list[cnt++] = new Token(buf);
					}
					
					tok.merge(list);
				} else */
				tok.setTermText(text);
			}catch(Exception e){
                            e.printStackTrace();
				throw new TokenizerException(e);
			}
		}
		return out;
	}

	/* (non-Javadoc)
	 * @see edu.buffalo.cse.irf14.analysis.Analyzer#getStream()
	 */
	@Override
	public TokenStream getStream() {
		// TODO Auto-generated method stub
		return getTokenStream();
	}
	
	private String checkApostrophe(String buffer) throws TokenizerException {
		String result = "";
		
		try {
			if (buffer.contains("'")) {												
				if (buffer.matches("^(.*)\'[^a-zA-Z0-9](.*)$")) {
					for (int i = 0; i < buffer.length();i++){
						if (buffer.charAt(i) != '\'')
							result+=buffer.charAt(i);
					}					
				}else {
					result = buffer.substring(0, buffer.indexOf("'"));
					for (int ch = buffer.indexOf("'"); ch < buffer.length(); ch++) {
						if (buffer.charAt(ch) == '\'') {
							if (ch + 1 == buffer.length())
								break;
							else if ((buffer.charAt(ch + 1) == 's') &&(ch + 2 == buffer.length()))
								break;
							else if ((ch == 0) &&(!contraction.containsKey(buffer)))
								continue;
						}
						result+= buffer.charAt(ch);
					}
				}
			}else
				result = buffer;
		}catch (Exception e){
			throw new TokenizerException(e);
		}
		
		return result;
	}
	
	private String checkContractions(String buffer){
		String result = "";
		
		if (contraction.containsKey(buffer.toLowerCase())){
			String temp = contraction.get(buffer.toLowerCase());
			if (buffer.matches("^[A-Z](.*)$")){
				String s = String.valueOf(temp.charAt(0));
				temp = temp.replaceFirst(String.valueOf(temp.charAt(0)), s.toUpperCase());
			}
			result = temp;
		}
		else
			result = buffer;
		return result;
	}
	
	private String checkPunctuations(String buffer){
		String result = "";
		
		if (buffer.matches("^(.*)[.!?]*")){
			if (!((buffer.equals("!true")) || (buffer.equals("!false"))))
				result = buffer.replaceAll("^([\\.!\\?])*|([\\.!\\?])*$", "");
			else
				result = buffer;
		}
		else
			result = buffer;
		return result;
	}
	
	private String checkHyphens(String buffer) throws TokenizerException{
		String result = "";
		
		try {
			if (buffer.contains("-")) {
				//if (buffer.matches("^[A-Z0-9]+[-][A-Z0-9]+$"))
				if(buffer.startsWith("-") || buffer.endsWith("-")){
					buffer = buffer.replaceAll("-", "");
					result = buffer;
				}else if ((buffer.matches("^.*[0-9]+[-].*$")) || (buffer.matches("^.*[-].*[0-9]+.*$")))
					result = buffer;
				else if (buffer.matches("^.*([a-zA-Z0-9][+*/-][a-zA-Z0-9]){2,}.*$"))
					result = buffer;
				else {
					String[] temp = buffer.split("-");
					result = temp[0] + " " + temp[1];
				}
			} else
				result = buffer;
		} catch (IndexOutOfBoundsException e){
			throw new TokenizerException(e);
		}
		
		return result;
	}
	
	public TokenStream processEntireTokenStream() throws TokenizerException{
		TokenStream out = getStream();
		
		try{
			while(increment()){
				
			}
                        getStream().reset();
		}catch(Exception e){
                    e.printStackTrace();
			throw new TokenizerException(e);
		}		
		
		return out;
	}
	
	public static void main(String[] args){
		Tokenizer to = new Tokenizer();
		try {
			TokenStream tstream = to.consume("I'm");
			TokenFilter obj = new SymbolsTokenFilter(tstream);
			
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
			
		}
	}
}
