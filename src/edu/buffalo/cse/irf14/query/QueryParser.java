/**
 * 
 */
package edu.buffalo.cse.irf14.query;

import java.util.ArrayList;
import java.util.Stack;

/**
 * @author nikhillo
 * Static parser that converts raw text to Query objects
 */
public class QueryParser {
    
    
    
	/**
	 * MEthod to parse the given user query into a Query object
	 * @param userQuery : The query to parse
	 * @param defaultOperator : The default operator to use, one amongst (AND|OR)
	 * @return Query object if successfully parsed, null otherwise
	 */
	public static Query parse(String userQuery, String defaultOperator) {
		QueryData qd = null;
        try {
			if(defaultOperator == null){
                defaultOperator = "OR";
            } else if (!(defaultOperator.equals("AND") || defaultOperator.equals("OR"))){
                return null;
            }
            qd = new QueryData(userQuery, defaultOperator);
            if(qd.isValidQueryString()){
                qd.processValidQuery();
            } else {
                return null;
            }
        } catch(Exception e){
        	e.printStackTrace();
        }
                
		//TODO: YOU MUST IMPLEMENT THIS METHOD
		return new Query(qd);
	}
        
}
