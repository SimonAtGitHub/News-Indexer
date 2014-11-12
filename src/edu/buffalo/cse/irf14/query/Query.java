package edu.buffalo.cse.irf14.query;

import edu.buffalo.cse.irf14.analysis.Analyzer;
import edu.buffalo.cse.irf14.analysis.AnalyzerFactory;
import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.commons.KeyValueVO;
import edu.buffalo.cse.irf14.document.FieldNames;
import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexType;
import edu.buffalo.cse.irf14.index.IndexerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

/**
 * Class that represents a parsed query
 * @author nikhillo
 *
 */
public class Query {
	
        String  inputQueryString;
        QueryData qd;
        
        public Query(QueryData qd){
            this.qd = qd;
        }
    
        public HashSet<Integer> execute(){
            try {
                qd.applyFilterOnTermContents();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            if(qd.returnZeroResults)
            	return null;
            
        	ArrayList<Token> postFixTokensToExecute = qd.getQueryAsPostfix();
            Stack st = new Stack();
            for(int i = 0; i < postFixTokensToExecute.size(); i++){
                Token currToken = postFixTokensToExecute.get(i);

                switch (currToken.getType()){
                    case AND_OP:
                    case OR_OP:
                    case NOT_OP:
                        HashSet<Integer> t2 = (HashSet<Integer>) st.pop(); //This order is important for NOT Op
                        HashSet<Integer> t1 = (HashSet<Integer>) st.pop();
                        HashSet<Integer> result = getOpResultTwoTerms(t1,t2, currToken.getType());
                        st.push(result);
                        break;
                    default:
                        st.push(getPostingListForTerm(currToken));
                }
            }
            
            return ((HashSet<Integer>) st.pop());
        }
        
        private HashSet<Integer> getOpResultTwoTerms(HashSet<Integer> t1, HashSet<Integer> t2, TokenType opToPerform){
            
            switch(opToPerform){
                case AND_OP:
                    t1.retainAll(t2);
                    break;
                case OR_OP:
                    t1.addAll(t2);
                    break;
                case NOT_OP:
                    t1.removeAll(t2);
            }
            
            return t1;
        }
        
        private HashSet<Integer> getPostingListForTerm(Token tok){
            HashSet<Integer> out;
            if(tok.getIndexType().equals("Author") ){
            	return getPostingListForNonPositionalTerm(tok);
            }
            
            if(tok.isPhraseTerm){
                out = phraseQuery(tok.getContent());
            } else {
                out = getPostingListForNonPositionalTerm(tok);
            }
            
            return out;
        }
        
        private HashSet<Integer> getPostingListForNonPositionalTerm(Token tok){
            HashSet<Integer> out = new HashSet<Integer>();
            
            String tokenIndexType = tok.getIndexType();
            String tokenContent = tok.getContent();
            
            Map<String, Integer[]> idMap = new HashMap<String, Integer[]>();
            Map<Integer, List<Integer[]>> postingsListIndex = new HashMap<Integer, List<Integer[]>>();
            
            //System.out.println(tokenIndexType);
            char ch = tokenIndexType.charAt(0);
            switch (ch){
                case 'T':
                    idMap = IndexReader.IWMock_termMap;
                    postingsListIndex = IndexReader.IWMock_termDict;
                    break;
                case 'A':
                    idMap = IndexReader.IWMock_authorMap;
                    postingsListIndex = IndexReader.IWMock_authorDict;
                    break;
                case 'P':
                    idMap = IndexReader.IWMock_placeMap;
                    postingsListIndex = IndexReader.IWMock_placeDict;
                    break;
            }
            
            if(!tokenIndexType.equals("Category")){
                
                if(idMap.get(tokenContent) != null) {
                    List<Integer[]> baseTermPostings = postingsListIndex.get(idMap.get(tokenContent)[0]);
                    for (Integer[] temp : baseTermPostings) {
                        out.add(new Integer(temp[0].intValue()));
                    }
                } //else is not required as the out will return an empty non null HashSet<Integer>
            } else if(tokenIndexType.equals("Category")){
                out = getDocIdsForCategory(tokenContent);
            }
            
            return out;
        }
        
        private HashSet<Integer> getDocIdsForCategory(String category){
            HashSet<Integer> out = new HashSet<Integer>();
            List<String> fileNamesForCategory = IndexReader.IWMock_categoryDict.get(category);
            if(fileNamesForCategory != null) {
                    for (String fileName : fileNamesForCategory) {
                        out.add(new Integer(getDocIDForFileName(fileName)));
                    }
            }
            return out;
        }
        
        private int getDocIDForFileName(String fileName){
            int out = -1;
            for(Entry<Integer,String> e : IndexReader.IWMock_docMap.entrySet()){
                if(fileName.equals(e.getValue())){
                    out = e.getKey().intValue();
                    break;
                }
            }
            return out;
        }
        
        //For Harish data requirement
        //Returns list of query term content alone without Index etc
        public List<String> getQueryTerms(){
            return qd.getQueryTerms();
        }
        
        public List<KeyValueVO<String,IndexType>> getTermsAndIndexType(){
            return qd.getTermsAndIndexType();
        }
        
        /**
	 * Method to convert given parsed query into string
	 */
	public String toString() {
		//TODO: YOU MUST IMPLEMENT THIS
		return qd.getOutputQueryString();
	}
        
	private String filterStream(FieldNames fieldName, String content) throws IndexerException{
		String out = null;
		try {
			ArrayList<edu.buffalo.cse.irf14.analysis.Token> temp = new ArrayList<edu.buffalo.cse.irf14.analysis.Token>();
			temp.add(new edu.buffalo.cse.irf14.analysis.Token(content));
			TokenStream in = new TokenStream(temp);
			AnalyzerFactory analyzerFact = AnalyzerFactory.getInstance();
			Analyzer analyzer = analyzerFact.getAnalyzerForField(fieldName, in);
			analyzer.increment();
			in.reset();
			if(in.hasNext()){
				out = in.next().toString();
			} 
			return out;
        } catch(Exception e){
            e.printStackTrace();
            throw new IndexerException(e);
        }      
	}
	
	
        //For Phrase Query from Harish
        //******************************* STARTS HERE *****************************************
        public HashSet<Integer> phraseQuery(String phrase){
		String[] queryList = phrase.split(" ");
		HashSet<Integer> docList = new HashSet<Integer>();
		Map<Integer, Map<Integer, ArrayList<Integer>>> invertedIndex = new HashMap<Integer, Map<Integer,ArrayList<Integer>>>();
		int phraseTermCount = 0;
		
		for(String query: queryList){
			int termId = IndexReader.IWMock_termMap.get(query)[0];
			invertedIndex.put(phraseTermCount++, IndexReader.positionIndexMap.get(termId));
		}
		
		for(int i = 0; i < invertedIndex.size() - 1; i++){
			docList = mergePosting(invertedIndex.get(i), invertedIndex.get(i + 1), docList);
			if (docList.size() == 0)				//Phrase is not available
				break;
		}
		
		for(Integer docId: docList)
			System.out.println(IndexReader.IWMock_docMap.get(docId));
		return docList;
	}
	
	private HashSet<Integer> mergePosting(Map<Integer, ArrayList<Integer>> posting1, Map<Integer, ArrayList<Integer>> posting2, HashSet<Integer> docList){
		HashSet<Integer> matchingDocList = new HashSet<Integer>();
		int result = -1;
		
		if(docList.size() == 0){					//Invoking this method for the first time
			for(Integer docId: posting1.keySet()){
				if(posting2.containsKey(docId)){
					result = checkForMatch(posting1, posting2, docId);
					if (result != -1)				//-1 means documentId does not match
						matchingDocList.add(result);
				}
			}
		} else {
			for(Integer docId: docList){			//Iterate only in the documents that have matched for previous terms of the given phrase
				if(posting1.containsKey(docId) && posting2.containsKey(docId)){
					result = checkForMatch(posting1, posting2, docId);
					if (result != -1)				//-1 means documentId does not match
						matchingDocList.add(result);
				}
			}
		}
		
		return matchingDocList;
	}
	
	private int checkForMatch(Map<Integer, ArrayList<Integer>> posting1, Map<Integer, ArrayList<Integer>> posting2, Integer docId){
		int matchingDocId = -1;
		boolean matchFound = false;
		boolean notRelevant = false;
		
		for(int i = 0; i < posting1.get(docId).size(); i++){
			for(int j = 0; j < posting2.get(docId).size(); j++){
				if(posting2.get(docId).get(j) == (posting1.get(docId).get(i) + 1)){		//Match found since it occurs next to next
					matchFound = true;
					matchingDocId = docId;
					break;
				} else if (posting2.get(docId).get(j) > posting1.get(docId).get(i)){	//If posting2 is greater, then iterate posting1 alone
					break;
				} else if (posting1.get(docId).get(i) > posting2.get(docId).get(j)){	//If posting1 is greater, then there is no need to iterate.
					notRelevant = true;
					break;
				}
			}
			if (matchFound)
				break;
			else if (notRelevant)
				break;
		}
		
		return matchingDocId;
	}
        
        //******************************* ENDS HERE *****************************************
	public static void main(String [] args){
		String a = null;
		System.out.println(a + " " + "|");
	}
}
