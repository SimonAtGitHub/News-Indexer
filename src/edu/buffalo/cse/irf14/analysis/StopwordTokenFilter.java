/**
 *
 */
package edu.buffalo.cse.irf14.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * @author sankar
 *
 */
public class StopwordTokenFilter extends TokenFilter {

    private static HashSet<String> listOfStopwords = new HashSet<String>();

    //Initializes the static stopword list.
    static {
        stopWordListPopulator();
    }

    /**
     * @param stream
     */
    public StopwordTokenFilter(TokenStream stream) {
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
        boolean isStopword = false;

        try {
            //Write the unit business process for cleaning a single token.
            if (getStream().hasNext()) {
                Token wip = getStream().next();
                String currTokenContent = String.copyValueOf(wip.getTermBuffer());
                if (!currTokenContent.matches("^[A-Z]+")) {
                    isStopword = listOfStopwords.contains(currTokenContent.toLowerCase());
                }

                if (isStopword) {
                    getStream().remove();
                }
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

    //Populates the list of stop words which needs to be removed from the TokenStream
    private static void stopWordListPopulator() {

        String stopwordSource = "a,able,about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because,been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from,get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just,least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on,only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your,am not,are not,cannot,could have,could not,could not have,did not,does not,do not,had not,had not have,has not,have not,he would,he would have,he will,I would,I would have,I will,I am,I have,is not,it would,it would have,it will,madam,might not,might not have,might have,must not,must have,need not,not have,of the clock,shall not,she would,she would have,she will,should have,should not,should not have,there would,there would have,there are,they would,they would have,they will,they are,they have,was not,we would,we would have,we will,we are,we have,were not,what will,what are,what have,where did,where have,who would,who will,who are,who have,why will,why are,will not,would have,would not have,you all,you all would have,you would,you would have,you will,you are,you have,them";

        listOfStopwords.addAll(Arrays.asList(stopwordSource.split(",")));

    }

    //TODO: Sankar: REMOVE THE BELOW TWO METHODS FOR PROD
    //**********************************************************REMOVE THE BELOW TWO METHODS FOR PROD***********************************************
    //**********************************************************REMOVE THE BELOW TWO METHODS FOR PROD***********************************************
    //**********************************************************REMOVE THE BELOW TWO METHODS FOR PROD***********************************************
    public static void main(String[] args) throws TokenizerException {
        
        TokenFilter test = new StopwordTokenFilter(null);
        
        String rv[] = ((StopwordTokenFilter) test).runTest(TokenFilterType.STOPWORD,"valid sentence");
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
