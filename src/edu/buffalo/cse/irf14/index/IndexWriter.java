/**
 * 
 */
package edu.buffalo.cse.irf14.index;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.buffalo.cse.irf14.analysis.Analyzer;
import edu.buffalo.cse.irf14.analysis.AnalyzerFactory;
import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.analysis.Tokenizer;
import edu.buffalo.cse.irf14.document.Document;
import edu.buffalo.cse.irf14.document.FieldNames;

/**
 * @author nikhillo
 * Class responsible for writing indexes to disk
 */
public class IndexWriter {
	Tokenizer tokenizer;
	TokenStream stream;
	//Postings List
	public static Map<Integer,List<Integer[]>> termDict = new HashMap<Integer,List<Integer[]>>();
	public static Map<Integer,List<Integer[]>> authorDict = new HashMap<Integer,List<Integer[]>>();
	public static Map<String,List<String>> categoryDict = new HashMap<String,List<String>>();
	public static Map<Integer,List<Integer[]>> placeDict = new HashMap<Integer,List<Integer[]>>();
	//Document Mapping
	//public static Map<String,Integer> docMap = new HashMap<String,Integer>();
	public static Map<Integer,String> docMap = new HashMap<Integer,String>();
	//Term ID mapping
	public static Map<String,Integer[]> termMap = new HashMap<String, Integer[]>();
	public static Map<String,Integer[]> authorMap = new HashMap<String, Integer[]>();
	public static Map<String,Integer[]> placeMap = new HashMap<String, Integer[]>();
	
	//Positional Index Mapping
	public static Map<Integer, Map<Integer,ArrayList<Integer>>> termPosIndxMap = new HashMap<Integer,Map<Integer,ArrayList<Integer>>>();
	int docID;
	int termID;
	int authorID;
	int placeID;
	String indexDir;
	
	/**
	 * Default constructor
	 * @param indexDir : The root directory to be sued for indexing
	 */
	public IndexWriter(String indexDir) {
		//TODO : YOU MUST IMPLEMENT THIS
		this.indexDir = indexDir;
		termID = 0;
		docID = 0;
		authorID = 0;
		placeID = 0;
	}
	
	/**
	 * Method to add the given Document to the index
	 * This method should take care of reading the filed values, passing
	 * them through corresponding analyzers and then indexing the results
	 * for each indexable field within the document. 
	 * @param d : The Document to be added
	 * @throws IndexerException : In case any error occurs
	 */
	public void addDocument(Document d) throws IndexerException {
		//TODO : YOU MUST IMPLEMENT THIS	
		try {
			tokenizer = new Tokenizer();
			//Build Doc-DocID dict. No need to get this done inside the while loop since we need only filename
			//if (!docMap.containsKey(d.getField(FieldNames.FILEID)[0]))
			docMap.put(++docID, d.getField(FieldNames.FILEID)[0]);
			
			if(d.getField(FieldNames.CATEGORY) != null){
				if(!categoryDict.containsKey(d.getField(FieldNames.CATEGORY)[0])){
					List<String> temp = new ArrayList<String>();
					temp.add(d.getField(FieldNames.FILEID)[0]);
					categoryDict.put(d.getField(FieldNames.CATEGORY)[0], temp);
				} else {
					List<String> temp = categoryDict.get(d.getField(FieldNames.CATEGORY)[0]);
					temp.add(d.getField(FieldNames.FILEID)[0]);
					categoryDict.put(d.getField(FieldNames.CATEGORY)[0], temp);
				}
			}
			
			populateIndex(d, FieldNames.CONTENT);
			populateIndex(d, FieldNames.AUTHOR);
			populateIndex(d, FieldNames.AUTHORORG);
			populateIndex(d, FieldNames.NEWSDATE);
			populateIndex(d, FieldNames.PLACE);
			populateIndex(d, FieldNames.TITLE);
		} catch (Exception e) {
            e.printStackTrace();
            throw new IndexerException(e);
        }
	}
	
	private void populateIndex(Document d,FieldNames field) throws IndexerException{
		Map<String, Integer[]> index = new HashMap<String, Integer[]>();
		Map<Integer,List<Integer[]>> dict = new HashMap<Integer,List<Integer[]>>();
		int id = 0;
		
		switch(field){
		case CONTENT:
		case TITLE:
		case NEWSDATE:
			dict = termDict;
			index = termMap;
			id = termID;
			break;
		case AUTHOR:
		case AUTHORORG:
			dict = authorDict;
			index = authorMap;
			id = authorID;
			break;
		case PLACE:		
			dict = placeDict;
			index = placeMap;
			id = placeID;
			break;
		}
		
		try {
			String[] arr = d.getField(field);
			
                        if(field == FieldNames.TITLE){
                            if(arr != null && arr.length == 1 && arr[0] != null && !arr[0].isEmpty()){
                                arr[0] = arr[0].toLowerCase();
                            }
                        }
			stream = consumeText(arr);
			//TODO: Check if this is required once filter is added.
			//consumeText can return null if the input has no value
			if (stream == null) return;
			
			filterStream(field, stream);
			//Get unique terms in a particular document
			Map<String,Integer> uniDataFromTokenStream = stream.getUniqueSortedTerms(d.getField(FieldNames.FILEID)[0]);
			//For Positional Index Mapping
			Map<String,ArrayList<Integer>> posPerTerm = stream.getPositionalIndex();	
				        
	        for(String buf: uniDataFromTokenStream.keySet() ){
				if(buf.isEmpty() || buf == null) continue;
								
				//Build Term-{TermID,docFreq} dict
				//Check if the term has occurred in any previous documents
				if (!index.containsKey(buf)){
					//Integer[] - 1. Term ID, 2. Document Frequency 3. Term Frequency in the given document
					index.put(buf, new Integer[]{++id,1,uniDataFromTokenStream.get(buf)});					
				}
			
				//Build TermID-PostingList pair
				if (dict.containsKey(index.get(buf)[0])){
					Integer[] foo = new Integer[2];
					List<Integer[]> temp = dict.get(index.get(buf)[0]);
					if(temp.get(temp.size()-1)[0] != docID){
						//Update Postings list
						foo[0] = docID;		//Update the postings list for this doc
						foo[1] = uniDataFromTokenStream.get(buf);	//Add term count for this doc
						temp.add(foo);
						dict.put(index.get(buf)[0],temp);
						
						Integer[] intArray = index.get(buf);
						intArray[1]++;	//Update doc frequency
						intArray[2]+= uniDataFromTokenStream.get(buf);	//Update total term frequency
						index.put(buf, intArray);
					}
				}else {
					//Create new ArrayList every time since list returned by asList cannot be used to perform 'add' operation later
					Integer[] foo = { docID, uniDataFromTokenStream.get(buf)};
					List<Integer[]> bar = new ArrayList<Integer[]>();
					bar.add(foo);
					dict.put(index.get(buf)[0], bar);					
				}
				
				if((field == FieldNames.CONTENT)||(field == FieldNames.TITLE)||(field == FieldNames.NEWSDATE)){
					//Populate Positional Index Map
					Map<Integer,ArrayList<Integer>> posPerDoc = termPosIndxMap.get(index.get(buf)[0]);
					if(posPerDoc == null)
						posPerDoc = new HashMap<Integer, ArrayList<Integer>>();
					posPerDoc.put(docID, posPerTerm.get(buf));
					termPosIndxMap.put(index.get(buf)[0], posPerDoc);
				}
			}
	        
	        /*//For Positional Index Mapping
			Map<String,ArrayList<Integer>> posPerDoc = stream.getPositionalIndex();	
			termPosIndxMap.put(index.get(key), value)
	        */
	        switch(field){
			case CONTENT:
			case TITLE:
			case NEWSDATE:
				termID = id;
				break;
			case AUTHOR:
			case AUTHORORG:
				authorID = id;
				break;
			case PLACE:			
				placeID = id;
				break;
			}
		} catch (Exception e) {
            e.printStackTrace();
            throw new IndexerException(e);
        }
	}
	
	private void filterStream(FieldNames fieldName, TokenStream in) throws IndexerException{
		try {
			
			AnalyzerFactory analyzerFact = AnalyzerFactory.getInstance();
			Analyzer analyzer = analyzerFact.getAnalyzerForField(fieldName, in);
			analyzer.increment();
        } catch(Exception e){
            e.printStackTrace();
            throw new IndexerException(e);
        }      
	}

	//Function to append the values in dictionary into a single tokenStream.
	private TokenStream consumeText(String[] arr) throws IndexerException{
		TokenStream stream = null;
		//TODO: Check in the exception
		
		try {
			int count = 0;
			if(arr != null){
				for (String value: arr){
					if (value != null && !value.isEmpty()){
						if (count == 0){
                                                    stream = tokenizer.consume(value);
                                                    count++;
                                                }
                                                else{
                                                    stream.append(tokenizer.consume(value));
                                                    count++;
                                                }
					}
				}
			}
		} catch(Exception e){
			e.printStackTrace();
			throw new IndexerException(e);
		}
		return stream;
	}
	
	/**
	 * Method that indicates that all open resources must be closed
	 * and cleaned and that the entire indexing operation has been completed.
	 * @throws IndexerException : In case any error occurs
	 */
	public void close() throws IndexerException {
		//TODO
		BufferedWriter writer;
		StringBuilder buffer = new StringBuilder();
		File fileName = new File(indexDir + File.separator + "termPosting");
		
		if(!fileName.exists()){
			try {
				fileName.createNewFile();
				writer = new BufferedWriter(new FileWriter(fileName));
				
		for (Integer key : termDict.keySet()) {
                    buffer.append(key + " ");
                    for (Integer[] value : termDict.get(key)) {
                        buffer.append(value[0] + ":" + value[1] + ",");
                    }
                    buffer.deleteCharAt(buffer.length() - 1);
                    buffer.append(System.lineSeparator());
                }
				writer.write(buffer.toString());
				buffer.delete(0, buffer.length());
				writer.flush();
				writer.close();
			} catch (Exception e){
				throw new IndexerException(e);
			}
		}
		
		fileName = new File(indexDir + File.separator + "authorPosting");
		if(!fileName.exists()){
			try {
				fileName.createNewFile();
				writer = new BufferedWriter(new FileWriter(fileName));
				
				for (Integer key : authorDict.keySet()) {
                	buffer.append(key + " ");
                    for (Integer[] value : authorDict.get(key)) {
                        buffer.append(value[0] + ":" + value[1] + ",");
                    }
                    buffer.deleteCharAt(buffer.length() - 1);
                    buffer.append(System.lineSeparator());
                }
				writer.write(buffer.toString());
				buffer.delete(0, buffer.length());
				writer.flush();
				writer.close();
			} catch (Exception e){
				throw new IndexerException(e);
			}
		}
		
		fileName = new File(indexDir + File.separator + "placePosting");
		if(!fileName.exists()){
			try {
				fileName.createNewFile();
				writer = new BufferedWriter(new FileWriter(fileName));
				
				for (Integer key : placeDict.keySet()) {
                	buffer.append(key + " ");
                    for (Integer[] value : placeDict.get(key)) {
                        buffer.append(value[0] + ":" + value[1] + ",");
                    }
                    buffer.deleteCharAt(buffer.length() - 1);
                    buffer.append(System.lineSeparator());
                }
				writer.write(buffer.toString());
				buffer.delete(0, buffer.length());
				writer.flush();
				writer.close();
			} catch (Exception e){
				throw new IndexerException(e);
			}
		}
		
		fileName = new File(indexDir + File.separator + "categoryPosting");
		if(!fileName.exists()){
			try {
				fileName.createNewFile();
				writer = new BufferedWriter(new FileWriter(fileName));
				
				for (String key : categoryDict.keySet()) {
                	buffer.append(key + " ");
                    for (String value : categoryDict.get(key)) {
                        buffer.append(value + ",");
                    }
                    buffer.deleteCharAt(buffer.length() - 1);
                    buffer.append(System.lineSeparator());
                }
				writer.write(buffer.toString());
				buffer.delete(0, buffer.length());
				writer.flush();
				writer.close();
			} catch (Exception e){
				throw new IndexerException(e);
			}
		}
		
		fileName = new File(indexDir + File.separator + "docMap");
		if(!fileName.exists()){
			try {
				fileName.createNewFile();
				writer = new BufferedWriter(new FileWriter(fileName));
				
				for (Integer key : docMap.keySet()) {
                	buffer.append(key + " ");
                    buffer.append(docMap.get(key));
                    buffer.append(System.lineSeparator());
                }
				writer.write(buffer.toString());
				buffer.delete(0, buffer.length());
				writer.flush();
				writer.close();
			} catch (Exception e){
				throw new IndexerException(e);
			}
		}
		
		fileName = new File(indexDir + File.separator + "termMap");
		if(!fileName.exists()){
			try {
				fileName.createNewFile();
				writer = new BufferedWriter(new FileWriter(fileName));
				
				for (String key : termMap.keySet()) {
                	buffer.append(key + "#!:=:!#");
                	buffer.append(termMap.get(key)[0]+ "," + termMap.get(key)[1] + "," + termMap.get(key)[2]);
                    buffer.append(System.lineSeparator());
                }
				writer.write(buffer.toString());
				buffer.delete(0, buffer.length());
				writer.flush();
				writer.close();
			} catch (Exception e){
				throw new IndexerException(e);
			}
		}
		
		fileName = new File(indexDir + File.separator + "authorMap");
		if(!fileName.exists()){
			try {
				fileName.createNewFile();
				writer = new BufferedWriter(new FileWriter(fileName));
				
				for (String key : authorMap.keySet()) {
                	buffer.append(key + "#!:=:!#");
                	buffer.append(authorMap.get(key)[0]+ "," + authorMap.get(key)[1] + "," + authorMap.get(key)[2]);
                    buffer.append(System.lineSeparator());
                }
				writer.write(buffer.toString());
				buffer.delete(0, buffer.length());
				writer.flush();
				writer.close();
			} catch (Exception e){
				throw new IndexerException(e);
			}
		}
		
		fileName = new File(indexDir + File.separator + "placeMap");
		if(!fileName.exists()){
			try {
				fileName.createNewFile();
				writer = new BufferedWriter(new FileWriter(fileName));
				
				for (String key : placeMap.keySet()) {
                	buffer.append(key + "#!:=:!#");
                	buffer.append(placeMap.get(key)[0]+ "," + placeMap.get(key)[1] + "," + placeMap.get(key)[2]);
                    buffer.append(System.lineSeparator());
                }
				writer.write(buffer.toString());
				buffer.delete(0, buffer.length());
				writer.flush();
				writer.close();
			} catch (Exception e){
				throw new IndexerException(e);
			}
		}
		
		fileName = new File(indexDir + File.separator + "termPositionalIndex");
		//System.out.println("Index size in IW:" + termPosIndxMap.size());
		if(!fileName.exists()){
			try {
				fileName.createNewFile();
				writer = new BufferedWriter(new FileWriter(fileName));
				
				/*for (String key : termPosIndxMap.keySet()) {
                	buffer.append(key + "#!:=:!#");
                	buffer.append(placeMap.get(key)[0]+ "," + placeMap.get(key)[1] + "," + placeMap.get(key)[2]);
                    buffer.append("\n");
                }*/
				for(Integer termID: termPosIndxMap.keySet()){
					buffer.append(termID+ " ");
					Map<Integer,ArrayList<Integer>> docPosMap = termPosIndxMap.get(termID);
					for(Integer documentID: docPosMap.keySet()){
						buffer.append(documentID + ":{");
						for(Integer pos: docPosMap.get(documentID)){
							buffer.append(pos + ",");
						}
						buffer.delete(buffer.length()-1, buffer.length());
						buffer.append("}*");
					}
					buffer.delete(buffer.length()-1, buffer.length());
					buffer.append(System.lineSeparator());
				}
				
				writer.write(buffer.toString());
				buffer.delete(0, buffer.length());
				writer.flush();
				writer.close();
			} catch (Exception e){
				throw new IndexerException(e);
			}
		}
	}
}
