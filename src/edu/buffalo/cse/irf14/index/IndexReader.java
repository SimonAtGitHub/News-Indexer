/**
 *
 */
package edu.buffalo.cse.irf14.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
//import java.util.Map.Entry;
//import edu.buffalo.cse.irf14.index.FileOccuranceVO;
import java.util.Set;

/**
 * @author nikhillo Class that emulates reading data back from a written index
 */
public class IndexReader {

    public static Map<Integer, List<Integer[]>> IWMock_termDict = null;
    public static Map<Integer, List<Integer[]>> IWMock_authorDict = null;
    public static Map<String, List<String>> IWMock_categoryDict = null;
    public static Map<Integer, List<Integer[]>> IWMock_placeDict = null;
    //Document Mapping
    //public static Map<String, Integer> IWMock_docMap = null;
    public static Map<Integer,String> IWMock_docMap = null;
    //Term ID mapping
    public static Map<String, Integer[]> IWMock_termMap = null;
    public static Map<String, Integer[]> IWMock_authorMap = null;
    public static Map<String, Integer[]> IWMock_placeMap = null;
    
    //PositionIndex -> TermID - {docID:{positionIndex}}
    public static Map<Integer,Map<Integer,ArrayList<Integer>>> positionIndexMap = null;

    IndexType type;
    List<Integer> topK;
    Map<String, Integer[]> idMap;
    Map<Integer, List<Integer[]>> postDic;
    //Map<Integer, String> docMap;

    /**
     * Default constructor
     *
     * @param indexDir : The root directory from which the index is to be read.
     * This will be exactly the same directory as passed on IndexWriter. In case
     * you make subdirectories etc., you will have to handle it accordingly.
     * @param type The {@link IndexType} to read from
     */
    public IndexReader(String indexDir, IndexType type) {
        String FileNameToRead = "";
        try {
            
            //TODO
            this.type = type;
            topK = new ArrayList<Integer>();

            //Change File Name for each
            if (IWMock_docMap == null) {
                FileNameToRead = "docMap";
                IWMock_docMap = populateIWdocMap(indexDir + File.separator + FileNameToRead);
            }

            if (type == IndexType.CATEGORY) {
                //TODO
                if (IWMock_categoryDict == null) {
                    FileNameToRead = "categoryPosting";
                    IWMock_categoryDict = populateIWCatDict(indexDir + File.separator + FileNameToRead);
                }
            } else {
                if (type == IndexType.TERM) {
                    if (IWMock_termMap == null) {
                        FileNameToRead = "termMap";
                        IWMock_termMap = populateIWTermAuthPlaceMap(indexDir + File.separator + FileNameToRead);
                    }
                    if (IWMock_termDict == null) {
                        FileNameToRead = "termPosting";
                        IWMock_termDict = populateIWTermAuthPlaceDict(indexDir + File.separator + FileNameToRead);
                    }
                    idMap = IWMock_termMap;
                    postDic = IWMock_termDict;
                    positionIndexMap = populateIWpositionIndexDict(indexDir + File.separator + "termPositionalIndex");
                } else if (type == IndexType.AUTHOR) {
                    if (IWMock_authorMap == null) {
                        FileNameToRead = "authorMap";
                        IWMock_authorMap = populateIWTermAuthPlaceMap(indexDir + File.separator + FileNameToRead);
                    }

                    if (IWMock_authorDict == null) {
                        FileNameToRead = "authorPosting";
                        IWMock_authorDict = populateIWTermAuthPlaceDict(indexDir + File.separator + FileNameToRead);
                    }

                    idMap = IWMock_authorMap;
                    postDic = IWMock_authorDict;
                } else {

                    if (IWMock_placeMap == null) {
                        FileNameToRead = "placeMap";
                        IWMock_placeMap = populateIWTermAuthPlaceMap(indexDir + File.separator + FileNameToRead);
                    }

                    if (IWMock_placeDict == null) {
                        FileNameToRead = "placePosting";
                        IWMock_placeDict = populateIWTermAuthPlaceDict(indexDir + File.separator + FileNameToRead);
                    }

                    idMap = IWMock_placeMap;
                    postDic = IWMock_placeDict;

                }

                //Take the term-collection frequency and build a list based on the occurrences alone.
                for (String key : idMap.keySet()) {
                    topK.add(idMap.get(key)[2]);
                }
            }
            Collections.sort(topK);
            Collections.reverse(topK);

            //Reverse docMap
            /*docMap = new HashMap<Integer, String>();
            for (Integer doc : IWMock_docMap.keySet()) {
                docMap.put(doc, IWMock_docMap.get(doc));
            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get total number of terms from the "key" dictionary associated with this
     * index. A postings list is always created against the "key" dictionary
     *
     * @return The total number of terms
     */
    public int getTotalKeyTerms() {
        //TODO : YOU MUST IMPLEMENT THIS
        return (type == IndexType.CATEGORY) ? IWMock_categoryDict.size() : idMap.size();
    }

    /**
     * Get total number of terms from the "value" dictionary associated with
     * this index. A postings list is always created with the "value" dictionary
     *
     * @return The total number of terms
     */
    public int getTotalValueTerms() {
        //TODO: YOU MUST IMPLEMENT THIS
        return IWMock_docMap.size();
    }

    /**
     * Method to get the postings for a given term. You can assume that the raw
     * string that is used to query would be passed through the same Analyzer as
     * the original field would have been.
     *
     * @param term : The "analyzed" term to get postings for
     * @return A Map containing the corresponding fileid as the key and the
     * number of occurrences as values if the given term was found, null
     * otherwise.
     */
    public Map<String, Integer> getPostings(String term) {
        //TODO:YOU MUST IMPLEMENT THIS
        Integer id = null;
        Map<String, Integer> result = new HashMap<String, Integer>();

        if (type == IndexType.CATEGORY) {
            List<String> files = IWMock_categoryDict.get(term);
            if (files != null) {
                for (String file : files) {
                    result.put(file, 1);
                }
            }
        } else {
            List<Integer[]> posting = null;
            Map<Integer, Integer> occurrence = new HashMap<Integer, Integer>();
            if (type == IndexType.TERM) {
                if (IWMock_termMap.get(term) != null) {
                    id = IWMock_termMap.get(term)[0];
                    posting = IWMock_termDict.get(id);
                } else {
                    return null;
                }
            } else if (type == IndexType.AUTHOR) {
                if (IWMock_authorMap.get(term) != null) {
                    id = IWMock_authorMap.get(term)[0];
                    posting = IWMock_authorDict.get(id);
                } else {
                    return null;
                }
            } else {
                if (IWMock_placeMap.get(term) != null) {
                    id = IWMock_placeMap.get(term)[0];
                    posting = IWMock_placeDict.get(id);
                } else {
                    return null;
                }
            }

            for (Integer[] temp : posting) {
            	//Integer[] - 1. Document ID 2.Term Frequency in each document
                occurrence.put(temp[0], temp[1]);
            }

            /*for (String doc : IWMock_docMap.keySet()) {
                if (occurrence.containsKey(IWMock_docMap.get(doc))) {
                    result.put(doc, occurrence.get(IWMock_docMap.get(doc)));
                }
            }*/
            for (Integer doc : IWMock_docMap.keySet()) {
                if (occurrence.containsKey(doc)) {
                    result.put(IWMock_docMap.get(doc), occurrence.get(IWMock_docMap.get(doc)));
                }
            }
        }

        return result.size() == 0 ? null : result;
    }

    /**
     * Method to get the top k terms from the index in terms of the total number
     * of occurrences.
     *
     * @param k : The number of terms to fetch
     * @return : An ordered list of results. Must be <=k fr valid k values null
     * for invalid k values
     */
    public List<String> getTopK(int k) {
        //TODO YOU MUST IMPLEMENT THIS
        Integer occurrence;
        List<String> hits = null;

        if (type != IndexType.CATEGORY) {

            if (k > 0) {
                hits = new ArrayList<String>();
                for (int index = 0; index < k; index++) {
                    occurrence = topK.get(index);

                    for (String key : idMap.keySet()) {
                        if (idMap.get(key)[2] == occurrence) {
                            hits.add(key);
                        }
                    }
                }
            }
        }
        return hits;
    }

    /**
     * Method to implement a simple boolean AND query on the given index
     *
     * @param terms The ordered set of terms to AND, similar to getPostings()
     * the terms would be passed through the necessary Analyzer.
     * @return A Map (if all terms are found) containing FileId as the key and
     * number of occurrences as the value, the number of occurrences would be
     * the sum of occurrences for each participating term. return null if the
     * given term list returns no results BONUS ONLY
     */
    public Map<String, Integer> query(String... terms) {
        //TODO : BONUS ONLY
        
        Map<String, Integer> out = null;
        boolean isFirstTerm = true;

        if (terms != null) {
            out = new HashMap<String, Integer>();
            List<HashMap<Integer, Integer>> queryTermPostingsColl = new ArrayList<HashMap<Integer, Integer>>();
            for (String term : terms) {
                HashMap<Integer, Integer> termPostings = new HashMap<Integer, Integer>();
                if(idMap.get(term) != null) {
                    List<Integer[]> baseTermPostings = postDic.get(idMap.get(term)[0]);
                    for (Integer[] singledocIdOccur : baseTermPostings) {
                        termPostings.put(singledocIdOccur[0], singledocIdOccur[1]);
                    }

                    queryTermPostingsColl.add(termPostings);
                } else {
                    return null;
                }
            }
            
            //Moreupdate
            Set<FileOccuranceVO> AND_Data = mergeAND(queryTermPostingsColl);
            for(FileOccuranceVO rec: AND_Data){
            	out.put(IWMock_docMap.get(rec.getDocID()), rec.getNumOfOcc());
            }
        }
        return (out.size() == 0) ? null : out;
    }
        
    /**
     *
     * @param in
     * @return
     */
    public Set<FileOccuranceVO> mergeAND(List<HashMap<Integer, Integer>> in) {
        Set<FileOccuranceVO> out = new HashSet<FileOccuranceVO>();
        Set<Integer> mergedUniq = new HashSet<Integer>();
        int indexOfSmallestPostList = -1;

        //Find the index of smallest PostingsList Occuring term
        for (int i = 0, tempSize = 0; i < in.size(); i++) {
            if ((in.get(i).size()) > tempSize) {
                indexOfSmallestPostList = i;
            }
        }

        if (indexOfSmallestPostList > -1) {
            HashMap<Integer, Integer> smallestPostingList = in.get(indexOfSmallestPostList);
            mergedUniq = smallestPostingList.keySet();
            for (int i = 0; i < in.size(); i++) {
                HashMap<Integer, Integer> eachPostingList = in.get(i);
                if (!mergedUniq.isEmpty() && i != indexOfSmallestPostList) {
                    mergedUniq.retainAll(eachPostingList.keySet());
                }
            }
        }

        if (!mergedUniq.isEmpty()) {
            for (Integer uniqDocID : mergedUniq) {

                int numOfOccur = 0;
                for (HashMap<Integer, Integer> i : in) {

                    numOfOccur += i.get(uniqDocID);

                }
                out.add(new FileOccuranceVO(uniqDocID, numOfOccur));

            }
        }
        return out;
    }
    
    public static Map<Integer, List<Integer[]>> populateIWTermAuthPlaceDict(String filename) throws IndexerException {

        BufferedReader buf = null;
        Map<Integer, List<Integer[]>> IWDictWIP = new HashMap<Integer, List<Integer[]>>();

        try {

            try {
                buf = new BufferedReader(new FileReader(filename));
                String line = null;

                while ((line = buf.readLine()) != null) {
                    String[] temp = line.split(" ");
                    List postingsListInfo = new ArrayList();

                    String[] postingsListExtracted = temp[1].split(",");

                    for (int i = 0, size = postingsListExtracted.length; i < size; i++) {
                        Integer[] docIdOccur = new Integer[2];

                        String[] finalExtraction = postingsListExtracted[i].split(":");
                        docIdOccur[0] = Integer.parseInt(finalExtraction[0]);
                        docIdOccur[1] = Integer.parseInt(finalExtraction[1]);
                        postingsListInfo.add(docIdOccur);
                    }

                    IWDictWIP.put(Integer.parseInt(temp[0]), postingsListInfo);
                }
            } finally {
                try {
                    if (buf != null) {
                        buf.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    throw e;
                }
            }

        } catch (Exception e) {
            throw new IndexerException(e);
        }
        return IWDictWIP;
    }

    public static Map<String, List<String>> populateIWCatDict(String filename) throws IndexerException {

        BufferedReader buf = null;
        Map<String, List<String>> IWCatDictWIP = new HashMap<String, List<String>>();

        try {

            try {

                buf = new BufferedReader(new FileReader(filename));
                String line = null;

                while ((line = buf.readLine()) != null) {
                    String[] temp = line.split(" ");

                    String[] fileNameListExtracted = temp[1].split(",");
                    List<String> fileNameList = new ArrayList<String>(fileNameListExtracted.length);

                    for (int i = 0, size = fileNameListExtracted.length; i < size; i++) {
                        fileNameList.add(fileNameListExtracted[i]);
                    }

                    IWCatDictWIP.put(temp[0], fileNameList);
                }
            } finally {
                try {
                    if (buf != null) {
                        buf.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    throw e;
                }
            }

        } catch (Exception e) {
            throw new IndexerException(e);
        }
        return IWCatDictWIP;
    }

    public static Map<String, Integer[]> populateIWTermAuthPlaceMap(String filename) throws IndexerException {

        BufferedReader buf = null;
        Map<String, Integer[]> IWMapWIP = new HashMap<String, Integer[]>();

        try {

            try {

                buf = new BufferedReader(new FileReader(filename));
                String line = null;

                while ((line = buf.readLine()) != null) {
                    String[] temp = line.split("#!:=:!#");
                    Integer[] moreInfo = new Integer[3];
                    String[] moreInfoExtracted = temp[1].split(",");
                    for (int i = 0, size = moreInfoExtracted.length; i < size; i++) {
                        moreInfo[i] = Integer.parseInt(moreInfoExtracted[i]);
                    }

                    IWMapWIP.put(temp[0], moreInfo);
                }
            } finally {
                try {
                    if (buf != null) {
                        buf.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    throw e;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new IndexerException(e);
        }
        return IWMapWIP;
    }

    public static Map<Integer,String> populateIWdocMap(String filename) throws IndexerException {

        BufferedReader buf = null;
        //Map<String, Integer> docMapWIP = new HashMap<String, Integer>();
        Map<Integer, String> docMapWIP = new HashMap<Integer, String>();

        try {

            try {

                buf = new BufferedReader(new FileReader(filename));
                String line = null;

                while ((line = buf.readLine()) != null) {
                    String[] temp = line.split(" ");
                    docMapWIP.put(Integer.parseInt(temp[0]), temp[1]);
                }
            } finally {
                try {
                    if (buf != null) {
                        buf.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    throw e;
                }
            }

        } catch (Exception e) {
            throw new IndexerException(e);
        }
        return docMapWIP;
    }

    public static Map<Integer,Map<Integer,ArrayList<Integer>>> populateIWpositionIndexDict(String filename) throws IndexerException {
    	BufferedReader buf = null;
        Map<Integer, Map<Integer,ArrayList<Integer>>> positionIndexDict = new HashMap<Integer, Map<Integer,ArrayList<Integer>>>();

        try {
            try {
                buf = new BufferedReader(new FileReader(filename));
                String line = null;

                while ((line = buf.readLine()) != null) {
                	Map<Integer,ArrayList<Integer>> posListPerDoc = new HashMap<Integer,ArrayList<Integer>>();
                	
                    String[] temp = line.split(" ");					//Delimiter for separating term and posListPerDoc pair
                    Integer termID = Integer.valueOf(temp[0]);
         
                    temp = temp[1].split("\\*"); 						// Delimiter for separating each doc-positionIndex pair
                    
                    for (int i = 0; i < temp.length; i++){
                    	ArrayList<Integer> posIndex = new ArrayList<Integer>();
                    	String[] posList = temp[i].split(":\\{");		//Delimiter for separating doc and positonIndex
                    	Integer doc = Integer.valueOf(posList[0]);
                    	posList = posList[1].split(",");				//Delimiter for separating each positionIndex
                    	
                    	//The maximum length is posList.length-1 because the list will have the closing '}' bracket.
                    	//Instead of writing a split function, this method is used
                    	for (int j = 0; j < posList.length-1; j++){
                    		posIndex.add(Integer.valueOf(posList[j]));
                    	}
                    	
                    	posList = posList[posList.length - 1].split("}");
                    	//System.out.println(posList[0]);
                    	posIndex.add(Integer.valueOf(posList[0]));
                    	posListPerDoc.put(doc, posIndex);
                    }
                    
                    positionIndexDict.put(termID, posListPerDoc);
                    //posIndex.clear();
                    //posListPerDoc.clear();
                }
                //System.out.println("Position Index size in IR:" + positionIndexDict.size());
            } finally {
                try {
                    if (buf != null) {
                        buf.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    throw e;
                }
            }

        } catch (Exception e) {
            throw new IndexerException(e);
        }
        return positionIndexDict;
    }

    /*
     public static void main(String[] args) {
     TermIdPostListRecord t = new TermIdPostListRecord();
     t.setTermId(401);
     List<FileOccuranceVO> plist = new ArrayList<FileOccuranceVO>();
     plist.add(new FileOccuranceVO(3, 1));
     plist.add(new FileOccuranceVO(1, 2));
     plist.add(new FileOccuranceVO(2, 1));

     plist.add(new FileOccuranceVO(4, 1));
     t.setDocIdOccList(plist);

     System.out.println(t);
     t.sortPostingList();
     System.out.println(t);
     }
     */
}
