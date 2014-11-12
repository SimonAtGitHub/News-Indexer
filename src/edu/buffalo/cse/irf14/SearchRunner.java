package edu.buffalo.cse.irf14;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.commons.KeyValueVO;
import edu.buffalo.cse.irf14.commons.VO;
import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexType;
import edu.buffalo.cse.irf14.query.Query;
import edu.buffalo.cse.irf14.query.QueryParser;

/**
 * Main class to run the searcher.
 * As before implement all TODO methods unless marked for bonus
 * @author nikhillo
 *
 */
public class SearchRunner {
	public enum ScoringModel {TFIDF, OKAPI};
	PrintStream stream;
	String corpusDir;
	String mergedDir;
	int lineCount = 0;
	
	/**
	 * Default (and only public) constuctor
	 * @param indexDir : The directory where the index resides
	 * @param corpusDir : Directory where the (flattened) corpus resides
	 * @param mode : Mode, one of Q or E
	 * @param stream: Stream to write output to
	 */
	public SearchRunner(String indexDir, String corpusDir, 
			char mode, PrintStream stream) {
		//TODO: IMPLEMENT THIS METHOD
		this.stream = stream;
		this.corpusDir = corpusDir;
		mergedDir = "Yankees";
		
		IndexReader reader = new IndexReader(indexDir, IndexType.TERM);
		reader = new IndexReader(indexDir, IndexType.AUTHOR);
		reader = new IndexReader(indexDir, IndexType.PLACE);
		reader = new IndexReader(indexDir, IndexType.CATEGORY);
		
		mergeDirsToSingle(corpusDir, mergedDir);
	}
	
	public void mergeDirsToSingle(String corpusDir, String toDir){
        File fileBaseDir = new File(corpusDir);
        File fileToDir = new File(corpusDir+ File.separator+ toDir);
        
        HashSet<String> filesMoved = new HashSet<String>();
        String[] catDirs = fileBaseDir.list();
        fileToDir.mkdir();
        //System.out.println("Number of Dirs to move : " + catDirs.length);
        int actualFilesPresent = 0;
        for (String cat : catDirs) {
            File catDir = new File(corpusDir+ File.separator+ cat);
            String [] files = catDir.list();
            
            if (files == null){
                catDir.delete();
                continue;
            }
            for (String f : files) {
                try {
                    File currFile = new File(catDir.getAbsolutePath() + File.separator+ f);
                    actualFilesPresent++;
                    if(!filesMoved.contains(f)){
                        currFile.renameTo(new File(fileToDir.getAbsolutePath() + File.separator+ f));
                        filesMoved.add(f);
                        //currFile.delete();
                    } else {
                        currFile.delete();
                    }
                } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }					
            }
            catDir.delete();
        }
        
        //System.out.println("Number of Files encountered : " + actualFilesPresent);
        //System.out.println("Number of Files Moved : " + filesMoved.size());
    }

	/**
	 * Method to execute given query in the Q mode
	 * @param userQuery : Query to be parsed and executed
	 * @param model : Scoring Model to use for ranking results
	 */
	public void query(String userQuery, ScoringModel model) {
		//TODO: IMPLEMENT THIS METHOD
		//Forward Index -> docId - {term:term freq}
		Map<Integer, Map<String, Integer>> forwardIndex = null; // = forwardIndex();
		Map<Integer,Double> scoringMap = null;
		long startTime = System.currentTimeMillis();
		boolean isQueryValid = true;
		boolean isDocAvailable = true;
		
		Query query = QueryParser.parse(userQuery, "OR");
		
		if(query != null){
			HashSet<Integer> docIdList = query.execute();			//Evaluate the given query
			if(docIdList != null){
				List<KeyValueVO<String, IndexType>> queryIndexMap = query.getTermsAndIndexType();
				forwardIndex = forwardIndexCreation(docIdList, queryIndexMap);
				
				if(model == ScoringModel.TFIDF){	
					scoringMap = tfIdfRanking(forwardIndex, queryIndexMap);
				}else if(model == ScoringModel.OKAPI){
					scoringMap = okapiRanking(forwardIndex, queryIndexMap); 
				}
			}else {
				isDocAvailable = false;
			}
		} else {
			isQueryValid = false;
		}
		
		ValueBasedSort cmp = new ValueBasedSort(scoringMap);
		TreeMap<Integer,Double> sortedMap = new TreeMap<Integer,Double>(cmp);
		sortedMap.putAll(scoringMap);
		
		long totalTime = System.currentTimeMillis() - startTime;
		stream.println(query.toString());
		stream.println(totalTime);
		int rank = 1;
		if(sortedMap.size() > 0){
			for(Integer docId: sortedMap.keySet()){
				stream.println(rank++);
				String docName = IndexReader.IWMock_docMap.get(docId);
				String fileName = corpusDir + File.separator + mergedDir + File.separator + docName;
				
				snippetGeneration(fileName, query);				
				stream.println(Math.round(scoringMap.get(docId)*100000.0)/100000.0);
			}
		} else if(!isDocAvailable){
			stream.println("No Matching Documents found");
		} else if(!isQueryValid){
			stream.println("Invalid Query");
		}		
		
		stream.flush();
	}
	
	public void snippetGeneration(String fileName, Query query){
		BufferedReader reader;
		
		try{
			reader = new BufferedReader(new FileReader(fileName));
			String line = "";
			String title = "";
			boolean hasLineStarted = false;
			boolean titleCompleted = false;
			ArrayList<VO> list = new ArrayList<VO>();
			
			while((line = reader.readLine())!= null){
				if(!titleCompleted){
					if(!hasLineStarted && line.isEmpty()){
						continue;
					}else if(line.isEmpty()){
						titleCompleted = true;
					}else{
						hasLineStarted = true;
						title += line;
					}
				}else{
					list.add(new VO(line));
				}
			}
			
			stream.println(title);
			List<String> queryList = query.getQueryTerms();
			Map<Integer,Integer> match = new HashMap<Integer, Integer>(queryList.size());
			for(String term: queryList){
				for(int i = 0; i< list.size(); i++){
					if(list.get(i).getLineContent().contains(term)){
						list.get(i).incrementMatchcount();
						match.put(i+1, list.get(i).getNumOfMatches());
					}
				}
			}
			
			lineCount = 0;
			StringBuilder text = new StringBuilder();
			if(match.size() == 1){
				for(Integer key: match.keySet()){
					if(key == 1){
						text.append(list.get(key - 1).getLineContent());
						text.append(System.lineSeparator());
						text.append(list.get(key).getLineContent());
					} else {
						text.append(list.get(key - 2).getLineContent());
						text.append(System.lineSeparator());
						text.append(list.get(key - 1).getLineContent());
					}
				}
			} else if((match.size() == 2) || (match.size() == 3)){
				for(Integer key: match.keySet()){
					text.append(list.get(key - 1).getLineContent());
					text.append(System.lineSeparator());
				}
			} else {
				for(Integer key: match.keySet()){
					for (String term: queryList){
						if(list.get(key - 1).getLineContent().contains(term)){
							String temp = list.get(key - 1).getLineContent();
							String[] arr = temp.split(" ");
							int count = 0;
							for(String element: arr){
								count++;
								element = element.replaceAll("\\p{Punct}", "");
								if(element.equals(term)){
									if(count > 2){
										text.append(arr[count-3] + " ");
										text = checkForEndOfLine(text);
										text.append(arr[count-2] + " "); 
										text = checkForEndOfLine(text);
										text.append(arr[count-1] + " ");
										text = checkForEndOfLine(text);
									} else {
										text.append(arr[count-1] + " ");
										text = checkForEndOfLine(text);
									}
									
									if(arr.length > (count + 2)){
										text.append(arr[count] + " " );
										text = checkForEndOfLine(text);
										text.append(arr[count+1] + " ");
										text = checkForEndOfLine(text);
										text.append(arr[count+2] + "...");
										text = checkForEndOfLine(text);
									} else if (arr.length >= (count +1)){
										text.append(arr[count] + "...");
										text = checkForEndOfLine(text);
									} else {
										text.append("...");
										text = checkForEndOfLine(text);
									}
									break;
								}
							}
							break;
						}
					}
					
					if(lineCount == 2)
						break;
				}
			}			
			
			stream.println(text);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public StringBuilder checkForEndOfLine(StringBuilder text){
		if(text.length() > 60){
			stream.println(text.toString());
			text = new StringBuilder();
			lineCount++;
		}
		return text;
	}
	
	/**
	 * Method to execute queries in E mode
	 * @param queryFile : The file from which queries are to be read and executed
	 */
	public void query(File queryFile) {
		//TODO: IMPLEMENT THIS METHOD
		BufferedReader reader = null;
		int queryCount = 0;
		int results = 0;
		String output = "";
		Map<Integer,Double> scoringMap = null;
		Map<Integer, Map<String, Integer>> forwardIndex = null;
		
		try {
			reader = new BufferedReader(new FileReader(queryFile));
			String line;
			
			line = reader.readLine();
			queryCount = Integer.parseInt(line.split("=")[1]);
			
			while ((line = reader.readLine()) != null){
				String[] queryArray = line.split(":\\{");
				String queryID = queryArray[0];	
				String query = queryArray[1].split("}")[0];
				//System.out.println(queryID + " " + query);
				
				Query queryObj = QueryParser.parse(query, "OR");
				//Query queryObj = new Query(data);	//Check for null???		
				
				if(queryObj == null)
					continue;
				
				HashSet<Integer> docIdList = queryObj.execute();			//Evaluate the given query
				if(docIdList == null)
					continue;
				List<KeyValueVO<String, IndexType>> queryIndexMap = queryObj.getTermsAndIndexType();
				forwardIndex = forwardIndexCreation(docIdList, queryIndexMap);
				
				//Call the query parser function here and then add the o/p after scoring to queryIDMap.
				
				scoringMap = tfIdfRanking(forwardIndex, queryIndexMap);
				ValueBasedSort cmp = new ValueBasedSort(scoringMap);
				TreeMap<Integer,Double> sortedMap = new TreeMap<Integer,Double>(cmp);
				sortedMap.putAll(scoringMap);
				
				if(sortedMap.size() > 0){
					results++;
					StringBuilder str = new StringBuilder();
					str.append(queryID + ":{");
					int docCount = 0;
					for(Integer docID: sortedMap.keySet()){
						Double temp = Math.round(scoringMap.get(docID)*100000.0)/100000.0;
						str.append(IndexReader.IWMock_docMap.get(docID) + "#" + temp + ",");					
						if(++docCount > 10)				//Exit if there are more than 10 results
							break;
					}
					str.delete(str.length()-1, str.length());
					str.append("}" + System.lineSeparator());
					output += str;
				}
			}
		}catch (IOException e){
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch(Exception e){
				e.printStackTrace();
			}
		}
		
		stream.println("numResults=" + results);
		stream.print(output);
		stream.flush();
		//stream.close();
	}
	
	public Map<Integer, Map<String, Integer>> forwardIndexCreation(HashSet<Integer> docIdList, List<KeyValueVO<String, IndexType>> queryMap){
		Map<Integer, Map<String, Integer>> forwardIndex = new HashMap<Integer, Map<String,Integer>>();
		
		for(KeyValueVO<String, IndexType> keyValueObj: queryMap){
			String term = keyValueObj.getKey();
			IndexType index = keyValueObj.getValue();
			if ((index == IndexType.AUTHOR) || (index == IndexType.TERM) || (index == IndexType.PLACE)){
				Map<Integer, List<Integer[]>> postingList = null;
				Integer termId = null;
				if (index == IndexType.TERM){
					if(IndexReader.IWMock_termMap.containsKey(term)){
						termId = IndexReader.IWMock_termMap.get(term)[0];
						postingList = IndexReader.IWMock_termDict;
					}else {
						continue;
					}
				} else if (index == IndexType.AUTHOR){
					if(IndexReader.IWMock_authorMap.containsKey(term)){						
						termId = IndexReader.IWMock_authorMap.get(term)[0];
						postingList = IndexReader.IWMock_authorDict;
					}else {
						continue;
					}
				} else {
					if(IndexReader.IWMock_placeMap.containsKey(term)){
						termId = IndexReader.IWMock_placeMap.get(term)[0];
						postingList = IndexReader.IWMock_placeDict;
					}else {
						continue;
					}
				}
				
				for(Integer docId : docIdList){
					List<Integer[]> temp = postingList.get(termId);
					for(Integer[] docPosting: temp){
						if(docId.equals(docPosting[0])){
							Map<String, Integer> forwardPosting = null;
							if (forwardIndex.containsKey(docId)){
								forwardPosting = forwardIndex.get(docId);
							} else {
								forwardPosting = new HashMap<String, Integer>();
							}
							forwardPosting.put(term, docPosting[1]);
							forwardIndex.put(docId, forwardPosting);
							break;
						}
					}
				}
			} else {
				for(Integer docId : docIdList){
					String doc = IndexReader.IWMock_docMap.get(docId);
					for(String docName: IndexReader.IWMock_categoryDict.get(term)){
						if (docName.equals(doc)){
							Map<String,Integer> forwardPosting;
							if(forwardIndex.containsKey(docId)){
								forwardPosting = forwardIndex.get(docId);
							} else {
								forwardPosting = new HashMap<String, Integer>();
							}
							forwardPosting.put(term, 1);
							forwardIndex.put(docId, forwardPosting);
							break;
						}
					}
				}
			}
		}
		
		return forwardIndex;
	}
	
	//lnc.ltc model
	public Map<Integer, Double> tfIdfRanking(Map<Integer, Map<String, Integer>> forwardIndex, List<KeyValueVO<String,IndexType>> queryIndex){
		//Doc-Score Map
		Map<Integer, Double> score = new HashMap<Integer, Double>();
			
		for(Integer docId: forwardIndex.keySet()){
			Map<String,Integer> tf = forwardIndex.get(docId);
			Integer termFreq = null;
			double tfWeight, idf;
			int termId, docFreq = 0;
			double finalScore = 0.0;
			double docLength = 0.0;
			double temp = 0.0;
			
			//Evaluate score for the document
			Map<String, Double> docScore = new HashMap<String, Double>();
			for(String term: tf.keySet()){
				termFreq = tf.get(term);
				tfWeight = 1 + Math.log10(termFreq);
				docScore.put(term, tfWeight);
				temp = temp + (tfWeight*tfWeight);
			}
			
			temp = Math.sqrt(temp);
			for(String term: docScore.keySet()){
				docScore.put(term, docScore.get(term)/temp);
			}
			
			//Evaluate the score for the query
			temp = 0.0;
			Map<String,Double> queryMap = new HashMap<String, Double>();
			for(KeyValueVO<String, IndexType> queryValueObj: queryIndex){	
				double queryScore = 0.0;
				String query = queryValueObj.getKey();
				if (tf.containsKey(query)){
					termFreq = tf.get(query);
					tfWeight = 1 + Math.log10(termFreq);
					
					//Obtain Document Frequency based on the index of the query term
					IndexType type = queryValueObj.getValue();
					if(type == IndexType.TERM){
						if(IndexReader.IWMock_termMap.containsKey(query))
							docFreq = IndexReader.IWMock_termMap.get(query)[1];					
					} else if(type == IndexType.AUTHOR){
						if(IndexReader.IWMock_authorMap.containsKey(query))
							docFreq = IndexReader.IWMock_authorMap.get(query)[1];
					} else if(type == IndexType.PLACE){
						if(IndexReader.IWMock_placeMap.containsKey(query))
							docFreq = IndexReader.IWMock_placeMap.get(query)[1];
					} else {
						if(IndexReader.IWMock_categoryDict.containsKey(query))
							docFreq = IndexReader.IWMock_categoryDict.get(query).size();
					}
					
					if(docFreq == 0) docFreq = 1;			//Change docFreq to 1 if the value is 0 or else infinity would result in 'idf'
					idf = Math.log10(IndexReader.IWMock_docMap.size()/docFreq);
					queryScore = tfWeight * idf;
				} else {
					queryScore = 0.0;
				}
				
				temp = temp + (queryScore*queryScore);
				queryMap.put(query, queryScore);
				/*if(docScore.containsKey(query)){
					finalScore += (docScore.get(query) * queryScore);
				}*/
			}
			
			temp = Math.sqrt(temp);
			for(KeyValueVO<String, IndexType> queryValueObj: queryIndex){
				double queryScore = 0.0;
				String query = queryValueObj.getKey();
				queryScore = queryMap.get(query)/temp;
				
				if(docScore.containsKey(query)){
					finalScore += (docScore.get(query) * queryScore);
				}
			}
			
			docLength = TokenStream.getDocLength(IndexReader.IWMock_docMap.get(docId));
			score.put(docId, (finalScore));
		}
		
		return score;
	}
	
	public Map<Integer, Double> okapiRanking(Map<Integer, Map<String, Integer>> forwardIndex, List<KeyValueVO<String,IndexType>> queryIndex){
		//Doc-Score Map
		Map<Integer, Double> score = new HashMap<Integer, Double>();
		int docLengthAvg = (TokenStream.getTotalWordCount() / IndexReader.IWMock_docMap.size());
		
		for(Integer docId: forwardIndex.keySet()){
			Map<String,Integer> tf = forwardIndex.get(docId);
			double termFreq = 0.0;
			double idf = 0.0;
			int termId, docFreq = 0;
			double docScore = 0.0;
			//double queryScore = 0.0;
			
			for(String term: tf.keySet()){
				termFreq = tf.get(term);
				termFreq = 1 + Math.log10(termFreq);
				IndexType type = null;
				//Obtain Document Frequency based on the index of the query term
				for(KeyValueVO<String, IndexType> query: queryIndex){
					if(term.equals(query.getKey())){
						type = query.getValue();
						break;
					}
				}
				
				if(type == IndexType.TERM){
					if(IndexReader.IWMock_termMap.containsKey(term))
						docFreq = IndexReader.IWMock_termMap.get(term)[1];					
				} else if(type == IndexType.AUTHOR){
					if(IndexReader.IWMock_authorMap.containsKey(term))
						docFreq = IndexReader.IWMock_authorMap.get(term)[1];
				} else if(type == IndexType.PLACE){
					if(IndexReader.IWMock_placeMap.containsKey(term))
						docFreq = IndexReader.IWMock_placeMap.get(term)[1];
				} else {
					if(IndexReader.IWMock_categoryDict.containsKey(term))
						docFreq = IndexReader.IWMock_categoryDict.get(docId).size();
				}
				
				if(docFreq == 0) docFreq = 1;			//Change docFreq to 1 if the value is 0 or else infinity would result in 'idf'
				idf = Math.log10(IndexReader.IWMock_docMap.size()/docFreq);
				
				int docLength = TokenStream.getDocLength(IndexReader.IWMock_docMap.get(docId));
				
				//k1 = 0.5, b = 0.5, k3 = 1.2
				docScore += (idf) * (((0.5 + 1.0) * termFreq)/ (0.5 * ((1.0 - 0.5) + (0.5 * (docLength/docLengthAvg))) + termFreq));
				//queryScore += (idf) * (((1.2 + 1.0) * termFreq)/ (1.2 * ((1.0 - 0.75) + (0.75 * (docLength/docLengthAvg))) + termFreq)) * (((1.2 + 1.0) * termFreq) / (1.2 + termFreq));
			}
			
			score.put(docId, docScore/docLengthAvg);
			//TODO: How to get term frequency in a given query			
		}
		
		return score;
	}
	
	/**
	 * General cleanup method
	 */
	public void close() {
		//TODO : IMPLEMENT THIS METHOD
		stream.flush();
		stream.close();
	}
	
	/**
	 * Method to indicate if wildcard queries are supported
	 * @return true if supported, false otherwise
	 */
	public static boolean wildcardSupported() {
		//TODO: CHANGE THIS TO TRUE ONLY IF WILDCARD BONUS ATTEMPTED
		return false;
	}
	
	/**
	 * Method to get substituted query terms for a given term with wildcards
	 * @return A Map containing the original query term as key and list of
	 * possible expansions as values if exist, null otherwise
	 */
	public Map<String, List<String>> getQueryTerms() {
		//TODO:IMPLEMENT THIS METHOD IFF WILDCARD BONUS ATTEMPTED
		return null;
		
	}
	
	/**
	 * Method to indicate if speel correct queries are supported
	 * @return true if supported, false otherwise
	 */
	public static boolean spellCorrectSupported() {
		//TODO: CHANGE THIS TO TRUE ONLY IF SPELLCHECK BONUS ATTEMPTED
		return true;
	}
	
	/**
	 * Method to get ordered "full query" substitutions for a given misspelt query
	 * @return : Ordered list of full corrections (null if none present) for the given query
	 */
	public List<String> getCorrections() {
		//TODO: IMPLEMENT THIS METHOD IFF SPELLCHECK EXECUTED
		String query = "economic";
		for(String key: IndexReader.IWMock_termMap.keySet()){
			int[][] cost = new int[query.length()][key.length()];
			
			for(int count=0;count<key.length();count++){
				cost[0][count] = count;
			}
			
			for(int count=0;count<query.length();count++){
				cost[count][0] = count;
			}
			
			for(int input=1; input <query.length();input++){
				for(int term=1; term<key.length(); term++){
					if(query.charAt(input) == key.charAt(term)){
						cost[input][term] = cost[input-1][term-1];
					} else {
						cost[input][term] = minValue(cost[input][term-1] + 1, cost[input-1][term] + 1, cost[input-1][term-1] + 1);
					}
				}
			}
			System.out.println(key + cost[query.length() - 1][key.length() - 1]);
		}
		return null;
	}
	
	//Return the smallest value
	private int minValue(int x, int y, int z){
		return Math.min(Math.min(x, y), z);
	}
	
	//Source: http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-the-values-in-java
	class ValueBasedSort implements Comparator<Integer>{
		Map<Integer, Double> map;
		
		ValueBasedSort(Map<Integer,Double> map){
			this.map = map;
		}
		
		public int compare(Integer set1, Integer set2){
			if(map.get(set1)>= map.get(set2))
				return -1;
			else
				return 1;
		}
	}
}