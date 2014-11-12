/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.buffalo.cse.irf14.query;

import edu.buffalo.cse.irf14.analysis.Analyzer;
import edu.buffalo.cse.irf14.analysis.AnalyzerFactory;
import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.analysis.Tokenizer;
import edu.buffalo.cse.irf14.commons.KeyValueVO;
import edu.buffalo.cse.irf14.document.FieldNames;
import edu.buffalo.cse.irf14.index.IndexType;
import edu.buffalo.cse.irf14.index.IndexerException;
import edu.buffalo.cse.irf14.util.text.StringManipulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author san
 */
public class QueryData {

    private ArrayList<Token> tokens = new ArrayList<Token>();
    private int leftParenCount = 0;
    private int rightParenCount = 0;
    private int openQoutesCount = 0;
    private int closeQoutesCount = 0;
    private int operatorCount = 0;
    private boolean isInvalidQuery = false;
    private boolean phraseInProgress = false;
    private String tokenContent = "";
    private String defaultOp = "";
    private String userQuery = "";
    private String parsedOutputQuery = null;
    public boolean returnZeroResults = false;
    
    private Pattern p = Pattern.compile("(\\:|\\s|\\(|^)\"([^\"]+)\"(\\s|\\(|\\)|$)");
    private HashMap<String, String> phrases = new HashMap<String, String>();
    
    public QueryData(){
        
    }
    
    public QueryData(String userQuery, String defaultOperator) {
        this.userQuery = userQuery;
        processRawString(userQuery);
        this.defaultOp = defaultOperator;
    }
    
    public void addToken(TokenType type){
        //For TERM call the addToken(type,content) directly
        switch(type){
            case OPEN_QOUTES:
                addToken(type,"\"");
                this.openQoutesCount++;
                this.phraseInProgress = true;
                break;
            case CLOSE_QOUTES:
                addToken(type,"\"");
                this.closeQoutesCount++;
                this.phraseInProgress = false;
                this.tokenContent = "";
                break;
            case OPEN_PAREN:
                addToken(type,"(");
                this.leftParenCount++;
                break;
            case CLOSE_PAREN:
                addToken(type,")");
                this.rightParenCount++;
                break;
            case AND_OP:
                addToken(type,"AND");
                operatorCount++;
                break;
            case OR_OP:
                addToken(type,"OR");
                operatorCount++;
                break;
            case NOT_OP:
                addToken(type,"NOT");
                operatorCount++;
                break;
            case START:
                addToken(type,"");
                break;
            case END:
                addToken(type,"");
                break;
        }
    }

    public void addToken(TokenType type, String content){
        Token tok = new Token(content,type);
        tokens.add(tok);
    }
    
    public void processRawString(String rawQueryString){
        
        if(rawQueryString != null && !rawQueryString.matches("\\s*")){
            rawQueryString = rawQueryString.trim();// Dont remove otherwise any whitespace to the start will create empty first element in the split used next
            Matcher m = p.matcher(rawQueryString);
            while(m.find()){
                String phraseContent = m.group(2);
                String intCode = StringManipulator.getIntCodeForString(phraseContent);
                phrases.put(intCode, phraseContent);
                rawQueryString = rawQueryString.replaceFirst("\"" + phraseContent + "\"", intCode);
            }
            String [] wordSliptTemp = rawQueryString.split("\\s+");
            processRawStringTokens(wordSliptTemp);
        } else {
            isInvalidQuery = true;

        }
    }
    
    public void processRawStringTokens(String[] rawStringTokens) {
        

        addToken(TokenType.START);
        
        for (int i = 0; i < rawStringTokens.length; i++) {
            String currContent = (tokenContent.isEmpty() ? rawStringTokens[i] : ( tokenContent + " "+ rawStringTokens[i]));
            char firstChar = currContent.charAt(0); //Its assumed there is atleast one char at this point due to split before
            char lastChar = (currContent.length() > 1) ? currContent.charAt(currContent.length() - 1) : ' '; //Its not guaranteed last char will be present
            
            
            switch (firstChar) {
                
                case '"':
                    if (!phraseInProgress) {
                        addToken(TokenType.OPEN_QOUTES);
                        switch (lastChar) {
                                case '"':
                                    if(currContent.length() == 2){
                                        addToken(TokenType.CLOSE_QOUTES);
                                    } else {
                                        tokenContent = tokenContent + currContent.substring(1, currContent.length() - 1);
                                        addToken(TokenType.TERM, tokenContent);
                                        addToken(TokenType.CLOSE_QOUTES);
                                    }
                                    break;
                                default:
                                    tokenContent = tokenContent + (tokenContent.isEmpty() ? "" : " ") + ((currContent.length() > 1) ? currContent.substring(1, currContent.length()) : ""); 
                        }
                    } //else is not required the last if before the for loop ends will handle it as phrase is in-progress
                    break;
                case '(': //Handles ((("ABC
                    if (!phraseInProgress) {
                        Matcher m3 = p3.matcher(currContent);
                        m3.find();
                        //Adds the Left Parens
                        String leftParens = m3.group(1);
                        int leftParensCount = leftParens.length();
                        while(leftParensCount > 0){
                            addToken(TokenType.OPEN_PAREN);
                            leftParensCount--;
                        }
                        
                        String suffix_content = currContent.replaceFirst("^(\\(+)(\\\"?)","");
                        if(!m3.group(2).isEmpty()) {
                            addToken(TokenType.OPEN_QOUTES);
                            if(suffix_content != null && !suffix_content.isEmpty()){
                                addToken(TokenType.TERM, suffix_content);
                            }
                        } else {
                            if(suffix_content != null && !suffix_content.isEmpty()){
                                addToken(TokenType.TERM, suffix_content);
                            }
                        } 
                    }//else is not required the last if before the for loop ends will handle it as phrase is in-progress
                    break;
                case 'A':
                case 'O':
                case 'N':
                    if (!phraseInProgress) {
                        if (currContent.equals("AND")) {
                            addToken(TokenType.AND_OP);
                        } else if(currContent.equals("OR")){
                            addToken(TokenType.OR_OP);
                        } else if(currContent.equals("NOT")){
                            addToken(TokenType.NOT_OP);
                        } else {
                            Matcher m2 = p2.matcher(currContent);
                            if(m2.find()){
                                String prefix_content = currContent.replaceFirst("\\)*$","");
                                if(prefix_content.equals("AND") || prefix_content.equals("OR") || prefix_content.equals("NOT")){
                                    if(!m2.group(0).isEmpty()){
                                        isInvalidQuery = true;
                                        return;
                                    }
                                }
                            }
                        }
                    } //else is not required the last if before the for loop ends will handle it as phrase is in-progress
                    break;
            }
            
            if (!phraseInProgress && ("\"(".indexOf(firstChar) == -1)
                    && !currContent.equals("AND") && !currContent.equals("OR")
                    && !currContent.equals("NOT")) { //Handles XYZ))) scenario as well but not XYZ" in this case " is treated part of text
                
                String currContentInProgress = currContent;
                
                {
                    Matcher m4 = p4.matcher(currContentInProgress);
                    if(m4.find()){
                        addToken(TokenType.INDEX_TYPE, m4.group(2));
                        currContentInProgress = currContentInProgress.replaceFirst("^"+m4.group(1), "");
                        if(!currContentInProgress.isEmpty() && currContentInProgress.charAt(0) == '"'){
                            addToken(TokenType.OPEN_QOUTES);
                            currContentInProgress = currContentInProgress.replaceFirst("\"", "");
                        } else {
                            Matcher m3 = p3.matcher(currContentInProgress);
                            m3.find();
                            //Adds the Left Parens
                            String leftParens = m3.group(1);
                            int leftParensCount = leftParens.length();
                            while(leftParensCount > 0){
                                addToken(TokenType.OPEN_PAREN);
                                leftParensCount--;
                            }

                            currContentInProgress = currContentInProgress.replaceFirst("^(\\(+)(\\\"?)","");
                            if(!m3.group(2).isEmpty()) {
                                addToken(TokenType.OPEN_QOUTES);
                            }
                        }
                        
                    }
                    currContent = currContentInProgress;
                }
                
                if(!phraseInProgress){
                    Matcher m1 = p1.matcher(currContent);
                    if(m1.find()){
                        String rightParens = m1.group(2);

                        //Adds the content portion in the prefix
                        String prefix_content = currContent.replaceFirst("(\\\"?)(\\)*)$","");
                        if(!prefix_content.isEmpty()){
                            addToken(TokenType.TERM, prefix_content);
                        }
                        if(!m1.group(1).isEmpty()){
                            //Adds the double qoutes
                            addToken(TokenType.CLOSE_QOUTES);
                        }
                        //Adds the right Parenthesis one or more
                        int rightParensCount = rightParens.length();
                        while(rightParensCount > 0){
                            addToken(TokenType.CLOSE_PAREN);
                            rightParensCount--;
                        }
                    } //No else needed as the find will always be true
                }
            } 
            
            if (phraseInProgress) {  //Handles XYZ")))
                Matcher m1 = p1.matcher(currContent);
                if(m1.find()){
                    if(!m1.group(1).isEmpty()){
                        String rightParens = m1.group(2);
                        //Adds the content portion in the prefix
                        String prefix_content = currContent.replaceFirst("(\\\"?)(\\)*)$","");
                        if(!prefix_content.isEmpty()){
                            addToken(TokenType.TERM, prefix_content);
                        }
                        //Adds the double qoutes
                        addToken(TokenType.CLOSE_QOUTES);
                        //Adds the right Parenthesis one or more
                        if(!rightParens.isEmpty()){
                            int rightParensCount = rightParens.length();
                            while(rightParensCount > 0){
                                addToken(TokenType.CLOSE_PAREN);
                                rightParensCount--;
                            }
                        } //No else required
                    } else {
                        tokenContent = tokenContent + (tokenContent.isEmpty() ? "" : " ") + currContent;
                    }
                } //No else needed as the find will always be true
            }
        
        }
        addToken(TokenType.END);
    }

    public boolean isValidQueryString(){
        boolean out = false;
        if(!this.isInvalidQuery){
            if(validateForOperators() && validateForQoutes() && validateForParens()){
                out = true;
            }
        } else {
            out = !this.isInvalidQuery;
        }
        
        return out;
    }
    
    public boolean validateForOperators(){
        boolean out = true;
        Stack queryParsingStack = new Stack();
        Token priorToken = null;
        for(int i = 0; i < tokens.size(); i++){
            Token currToken = tokens.get(i);
            switch(currToken.getType()){
                case AND_OP:
                case OR_OP:
                case NOT_OP:
                    priorToken = (Token) queryParsingStack.peek();
                    if(priorToken.getType() == TokenType.START
                            || priorToken.getType() == TokenType.AND_OP
                            || priorToken.getType() == TokenType.OR_OP
                            || priorToken.getType() == TokenType.NOT_OP
                            || priorToken.getType() == TokenType.OPEN_PAREN){
                        return false;
                    }
                    
                    break;
                case CLOSE_PAREN:
                    priorToken = (Token) queryParsingStack.peek();
                    if(priorToken.getType() == TokenType.AND_OP
                            || priorToken.getType() == TokenType.OR_OP
                            || priorToken.getType() == TokenType.NOT_OP){ //This is redundant as the parsing step itself handles this
                        return false;
                    }
                    break;
                case END:
                    priorToken = (Token) queryParsingStack.peek();
                    if(priorToken.getType() == TokenType.AND_OP
                            || priorToken.getType() == TokenType.OR_OP
                            || priorToken.getType() == TokenType.NOT_OP){
                        return false;
                    }
                    break;
            }
            queryParsingStack.push(currToken);
        }
        return out;
    }
    
    public boolean validateForQoutes(){ //TODO: Sankar - This validation can be improved if time permits
        boolean out = false;
        out = (!phraseInProgress) && (openQoutesCount == closeQoutesCount);
        return out;
    }
    
    public boolean validateForParens(){ //TODO: Sankar - This validation can be improved if time permits
        boolean out = false;
        out = (leftParenCount == rightParenCount);
        return out;
    }
    
    public void cleanUpQoutes(){
        ArrayList<Token> l_tokens = new ArrayList<Token>();
        boolean isPhraseInProgress = false;
        for(int i=0; i< tokens.size(); i++){
            Token currTok = tokens.get(i);
            switch (currTok.getType()){
                case OPEN_QOUTES:
                    isPhraseInProgress = true;
                    break;
                case CLOSE_QOUTES:
                    isPhraseInProgress = false;
                    break;
                case TERM:
                    currTok.isPhraseTerm = isPhraseInProgress;
                    l_tokens.add(currTok);
                    break;
                default:
                    l_tokens.add(currTok);
                    break;
            }
        }
        tokens = l_tokens;
    
    }
    
    public void applyIndexToTerms(){
        //String baseIndexType = "";
        String multiIndexType = "";
        String singleIndexType = "";
        ArrayList<Token> l_tokens = new ArrayList<Token>();
        
        Stack indexType = new Stack();
        
        for(int i=0; i< tokens.size(); i++){
            Token currTok = tokens.get(i);
            switch (currTok.getType()){
                case INDEX_TYPE:
                    singleIndexType = currTok.getContent();
                    break;
                case CLOSE_PAREN:
                    multiIndexType = singleIndexType = "";
                    l_tokens.add(currTok);
                    break;
                case OPEN_PAREN:
                    if(!singleIndexType.isEmpty()){
                        multiIndexType = singleIndexType;
                        singleIndexType = "";
                    }
                    l_tokens.add(currTok);
                    break;
                case TERM:
                    String currTokenContent = currTok.getContent();
                    Matcher m5 = p5.matcher(currTokenContent);
                    if(m5.find()){
                        currTok.setIndexType(m5.group(1));
                        currTokenContent = currTokenContent.replaceFirst(p5.toString(), "");
                        currTok.setContent(currTokenContent);
                        currTok.setDefaultIndexType(false);
                    } else if(!singleIndexType.isEmpty()){
                        currTok.setIndexType(singleIndexType);
                        singleIndexType = "";
                        currTok.setDefaultIndexType(false);
                    } else if(!multiIndexType.isEmpty()){
                        currTok.setIndexType(multiIndexType);
                        currTok.setDefaultIndexType(false);
                    } else {
                        currTok.setDefaultIndexType(true);
                    }
                    l_tokens.add(currTok);
                    break;
                default:
                    l_tokens.add(currTok);
                    break;
            }
        }
        tokens = l_tokens;
    }
    
    public int getLeftParenCount() {
        return leftParenCount;
    }

    public int getRightParenCount() {
        return rightParenCount;
    }

    public int getOpenQoutesCount() {
        return openQoutesCount;
    }

    public int getCloseQoutesCount() {
        return closeQoutesCount;
    }

    
    public void applyParenForDefaultTerms(){
        boolean nonDefaultTermExists = doesNonDefaultTermExist();
        boolean operatorExists = doesOperatorExist();
        ArrayList<Token> l_tokens = new ArrayList<Token>();
        
        if(nonDefaultTermExists || operatorExists){
            TokenType priorTermType = null;
            boolean groupingInProgress = false;
            int numOfTermsGrouped = 0;
            int indexWhereLeftParenInserted = -1;
            boolean isPriorTokendefault = false;
            for(int i=0; i< tokens.size(); i++){
                Token currTok = tokens.get(i);
                
                switch(currTok.getType()){
                    case TERM:
                        if(currTok.isDefaultIndexType()){
                            if(!groupingInProgress){
                                if(priorTermType != TokenType.OPEN_PAREN && !isPriorTokendefault) {
                                    l_tokens.add(new Token("(",TokenType.OPEN_PAREN));
                                    indexWhereLeftParenInserted = l_tokens.size() - 1;
                                    groupingInProgress = true;
                                    numOfTermsGrouped++;
                                }
                            } else {
                                numOfTermsGrouped++;
                            }
                        } else {
                            if(numOfTermsGrouped > 1){
                                l_tokens.add(new Token(")",TokenType.CLOSE_PAREN));
                                groupingInProgress = false;
                                numOfTermsGrouped = 0;
                                indexWhereLeftParenInserted = -1;
                            } else if(numOfTermsGrouped == 1){
                                l_tokens.remove(indexWhereLeftParenInserted);
                                groupingInProgress = false;
                                numOfTermsGrouped = 0;
                                indexWhereLeftParenInserted = -1;
                            }
                        }
                        l_tokens.add(currTok);
                        break;
                    default:
                        if(numOfTermsGrouped > 1){
                            l_tokens.add(new Token(")",TokenType.CLOSE_PAREN));
                            groupingInProgress = false;
                            numOfTermsGrouped = 0;
                            indexWhereLeftParenInserted = -1;
                        } else if(numOfTermsGrouped == 1){
                            l_tokens.remove(indexWhereLeftParenInserted);
                            groupingInProgress = false;
                            numOfTermsGrouped = 0;
                            indexWhereLeftParenInserted = -1;
                        }
                        l_tokens.add(currTok);
                }
                
                
                priorTermType = l_tokens.get(l_tokens.size()-1).getType();
                isPriorTokendefault = l_tokens.get(l_tokens.size()-1).isDefaultIndexType();
            }
            tokens = l_tokens;
        }
        
    }
    
    private void setTermsToDefaultTerms(){
        for(int i=0; i< tokens.size(); i++){
            Token currTok = tokens.get(i);
            if(currTok.getType() == TokenType.TERM && currTok.isDefaultIndexType()){
                currTok.setIndexType("Term");
            }
        }
    }
    
    private void applyDefaultOperator(){
        ArrayList<Token> l_tokens = new ArrayList<Token>();
        TokenType priorTermType = null;
        for(int i=0; i< tokens.size(); i++){
            Token currTok = tokens.get(i);
            if(currTok.getType() == TokenType.TERM && priorTermType == TokenType.TERM){
                if(this.defaultOp != null && this.defaultOp == "AND"){
                    l_tokens.add(new Token("AND",TokenType.AND_OP));
                } else {
                    l_tokens.add(new Token("OR",TokenType.OR_OP));
                }
                
            }
            l_tokens.add(currTok);
            priorTermType = l_tokens.get(l_tokens.size()-1).getType();
        }
        tokens = l_tokens;
    }
    
    public String getOutputQueryString(){
        StringBuilder out = new StringBuilder();
        TokenType priorTermType = null;
        for(int i=0; i< tokens.size(); i++){
            Token currTok = tokens.get(i);
            switch(currTok.getType()){
                case START:
                    out.append("{ ");
                    break;
                case OPEN_PAREN:
                    out.append("[ ");
                    break;
                case CLOSE_PAREN:
                    out.append("] ");
                    break;
                case AND_OP:
                case OR_OP:
                    out.append(currTok.getContent() + " ");
                    break;
                case NOT_OP:
                    out.append("AND ");
                    break;
                case END:
                    out.append("}");
                    break;
                case TERM:
                    String termToSet = "";
                    if(priorTermType == TokenType.NOT_OP){
                        termToSet = "<" + currTok.getIndexType() + ":" + ( (currTok.isPhraseTerm) ? ("\"" + currTok.getContent() + "\"") : currTok.getContent()) + ">";
                    } else {
                        termToSet = currTok.getIndexType() + ":" + ( (currTok.isPhraseTerm) ? ("\"" + currTok.getContent() + "\"") : currTok.getContent());
                    }
                    out.append(termToSet + " ");
                    break;
            }
            priorTermType = currTok.getType();
        }
        return out.toString();
    }
    
    public boolean doesNonDefaultTermExist(){
        boolean out = false;
        for(int i=0; i< tokens.size(); i++){
            Token currTok = tokens.get(i);
            if(currTok.getType() == TokenType.TERM){
                if(!currTok.isDefaultIndexType()){
                   return  true;
                }
            }
        }
        return out;
    }
    
    public boolean doesOperatorExist(){
        return operatorCount > 0;
    }
   
    
    public ArrayList<Token> getQueryAsPostfix(){
        ArrayList<Token> l_tokens = new ArrayList<Token>();
        Stack st = new Stack();
        for(int i=0; i< tokens.size(); i++){
            Token currToken = tokens.get(i);
            switch(currToken.getType()){
                case TERM:
                    l_tokens.add(currToken.cloneToken());
                    break;
                case AND_OP:
                case OR_OP:
                case NOT_OP:
                {
                    
                    while(!st.empty() && isCurrLowOrEqPrecedence(currToken, ((Token) st.peek())) && ((Token) st.peek()).getType() != TokenType.OPEN_PAREN){
                        l_tokens.add(((Token) st.pop()).cloneToken());
                    }
                    st.push(currToken);
                }   
                    break;
                case OPEN_PAREN:
                    st.push(currToken);
                    break;
                case CLOSE_PAREN:
                {
                    while(!st.empty() && ((Token) st.peek()).getType() != TokenType.OPEN_PAREN){
                        l_tokens.add(((Token) st.pop()).cloneToken());
                    }
                    if(!st.empty() && ((Token) st.peek()).getType() == TokenType.OPEN_PAREN){
                        st.pop();
                    }
                }
                break;
                case END:
                    while(!st.empty()){
                        l_tokens.add(((Token)st.pop()).cloneToken());
                    }
                    break;
            }
        }
        
        return l_tokens;
    }
    
    public boolean isCurrLowOrEqPrecedence(Token curr, Token topOfStack){
        if(topOfStack.getType() == TokenType.OR_OP && (curr.getType() == TokenType.AND_OP || curr.getType() == TokenType.NOT_OP)){
            return false;
        } else {
            return true;
        }
    }
    
    public void processValidQuery() throws Exception{
        //The sequence of these operations matter. So dont change the order
//        System.out.println("start() --> " + tokens.get(1).getContent());
        cleanUpQoutes();
//        System.out.println("cleanUpQoutes() --> " + tokens.get(1).getContent());
        applyIndexToTerms();
//        System.out.println("applyIndexToTerms() --> " + tokens.get(1).getContent());
        applyParenForDefaultTerms();
//        System.out.println("applyParenForDefaultTerms() --> " + tokens.get(1).getContent());
        setTermsToDefaultTerms();
//        System.out.println("setTermsToDefaultTerms() --> " + tokens.get(1).getContent());
        applyDefaultOperator();
//        System.out.println("applyDefaultOperator() --> " + tokens.get(1).getContent());
        applyPhrasesToTerms();
//        System.out.println("applyPhrasesToTerms() --> " + tokens.get(1).getContent());
        //applyFilterOnTermContents();
//        System.out.println("applyFilterOnTermContents() --> " + tokens.get(1).getContent());
    }
    
    private void applyPhrasesToTerms(){
        for(int i = 0; i < tokens.size(); i++){
            Token currToken = tokens.get(i);
            if(currToken.getType() == TokenType.TERM){
                String tokContent = currToken.getContent();
                if(phrases.containsKey(tokContent)){
                    currToken.setContent(phrases.get(tokContent));
                    currToken.isPhraseTerm = true;
                }
            }
        }
    }
    
    public void applyFilterOnTermContents() throws Exception{
    	for(int i = 0; i < tokens.size(); i++){
            Token currToken = tokens.get(i);
            if(currToken.getType() == TokenType.TERM){
            	String filteredContent = filterStream(getFieldName(currToken.getIndexType()), currToken.getContent());
                if(!filteredContent.trim().isEmpty()){
                	currToken.setContent(filteredContent.trim());
                } else {
                	returnZeroResults = true;
                }
            }
        }
    }
    
    private static FieldNames getFieldName(String indexTypeString){
    	FieldNames out = null;
        char ch = indexTypeString.charAt(0);
        switch(ch){
            case 'T':
                out = FieldNames.CONTENT;
                break;
            case 'A':
                out = FieldNames.AUTHOR;
                break;
            case 'P':
                out = FieldNames.PLACE;
                break;
            case 'C':
                out = FieldNames.CATEGORY;
        }
        return out;
    }
    
    
    public List<String> getQueryTerms(){
        List<String> out = new ArrayList<String>();
        
        for(int i = 0; i < tokens.size(); i++){
            Token currToken = tokens.get(i);
            if(currToken.getType() == TokenType.TERM){
                out.add(String.valueOf(currToken.getContent()));
            }
        }
        return out;
    }
    
    public List<KeyValueVO<String,IndexType>> getTermsAndIndexType(){
        List<KeyValueVO<String,IndexType>> out = new ArrayList<KeyValueVO<String,IndexType>>();
        //List<String[]> out = new ArrayList<HashMap<String,IndexType>>();
        
        for(int i = 0; i < tokens.size(); i++){
            Token currToken = tokens.get(i);
            if(currToken.getType() == TokenType.TERM){
                KeyValueVO<String,IndexType> temp = new KeyValueVO<String,IndexType>(String.valueOf(currToken.getContent()), getIndexType(currToken.getIndexType()));
                out.add(temp);
            }
        }
        return out;
    }
    
    
    
    private static IndexType getIndexType(String indexTypeString){
        IndexType out = null;
        char ch = indexTypeString.charAt(0);
        switch(ch){
            case 'T':
                out = IndexType.TERM;
                break;
            case 'A':
                out = IndexType.AUTHOR;
                break;
            case 'P':
                out = IndexType.PLACE;
                break;
            case 'C':
                out = IndexType.CATEGORY;
        }
        return out;
    }
    
    private static Pattern p1 = Pattern.compile("(\\\"?)(\\)*)$");
    private static Pattern p2 = Pattern.compile("\\)*$");
    private static Pattern p3 = Pattern.compile("^(\\(+)(\\\"?)");
    private static Pattern p4 = Pattern.compile("^((Category|Place|Author|Term):)([\\(\"])");
    private static Pattern p5 = Pattern.compile("^(Category|Place|Author|Term):");
    
    private String filterStream(FieldNames fieldName, String content) throws Exception{
		String out = "";
		
//		ArrayList<edu.buffalo.cse.irf14.analysis.Token> temp = new ArrayList<edu.buffalo.cse.irf14.analysis.Token>();
//		temp.add(new edu.buffalo.cse.irf14.analysis.Token(content));
		TokenStream in = (new Tokenizer()).consume(content);
		AnalyzerFactory analyzerFact = AnalyzerFactory.getInstance();
		Analyzer analyzer = analyzerFact.getAnalyzerForField(fieldName, in);
		analyzer.increment();
		in.reset();
		while(in.hasNext()){
			out = out + " " + in.next().toString();
		} 
		in.reset();
         
		return out;
	}
    
    public static void main(String[] args) {
        //Failed: \"hello world\"
        
        String query = "\"hello world\"" ;//" Category:War AND Author:Dutt AND Place:Baghdad AND (prisoners detainees rebels)";
        QueryData q = new QueryData(query,"OR");
        boolean isValid = q.isValidQueryString();
        System.out.println(isValid);
        //q.processValidQuery();
        String out = q.getOutputQueryString();
        System.out.println(out);
        //ArrayList<Token> PostFixTokens = q.getQueryAsPostfix();
        
        
        /*
        ArrayList<Token> tok = tokenArrayListPopulator().getQueryAsPostfix();
        
        for(Token t: tok){
            System.out.print(t.getContent() + "  ");
        }
        */
    }
    
    public static QueryData tokenArrayListPopulator(){
        
        QueryData q = new QueryData();
        q.tokens.add(new Token("",TokenType.START));
        
        /*
        q.tokens.add(new Token("(",TokenType.OPEN_PAREN));
        q.tokens.add(new Token("(",TokenType.OPEN_PAREN));
        q.tokens.add(new Token("A",TokenType.TERM));
        q.tokens.add(new Token("OR",TokenType.OR_OP));
        q.tokens.add(new Token("B",TokenType.TERM));
        q.tokens.add(new Token(")",TokenType.CLOSE_PAREN));
        q.tokens.add(new Token("AND",TokenType.AND_OP));
        q.tokens.add(new Token("C",TokenType.TERM));
        q.tokens.add(new Token("OR",TokenType.OR_OP));
        q.tokens.add(new Token("D",TokenType.TERM));
        q.tokens.add(new Token(")",TokenType.CLOSE_PAREN));
        q.tokens.add(new Token("AND",TokenType.AND_OP));
        q.tokens.add(new Token("E",TokenType.TERM));
        */
        /*
        q.tokens.add(new Token("A",TokenType.TERM));
        q.tokens.add(new Token("AND",TokenType.AND_OP));
        q.tokens.add(new Token("(",TokenType.OPEN_PAREN));
        q.tokens.add(new Token("B",TokenType.TERM));
        q.tokens.add(new Token("OR",TokenType.OR_OP));
        q.tokens.add(new Token("C",TokenType.TERM));
        q.tokens.add(new Token(")",TokenType.CLOSE_PAREN));
        */
        q.tokens.add(new Token("(",TokenType.OPEN_PAREN));
        q.tokens.add(new Token("4",TokenType.TERM));
        q.tokens.add(new Token("OR",TokenType.OR_OP));
        q.tokens.add(new Token("8",TokenType.TERM));
        q.tokens.add(new Token(")",TokenType.CLOSE_PAREN));
        q.tokens.add(new Token("AND",TokenType.AND_OP));
        q.tokens.add(new Token("(",TokenType.OPEN_PAREN));
        q.tokens.add(new Token("6",TokenType.TERM));
        q.tokens.add(new Token("OR",TokenType.OR_OP));
        q.tokens.add(new Token("5",TokenType.TERM));
        q.tokens.add(new Token(")",TokenType.CLOSE_PAREN));
        q.tokens.add(new Token("AND",TokenType.AND_OP));
        q.tokens.add(new Token("(",TokenType.OPEN_PAREN));
        q.tokens.add(new Token("(",TokenType.OPEN_PAREN));
        q.tokens.add(new Token("3",TokenType.TERM));
        q.tokens.add(new Token("OR",TokenType.OR_OP));
        q.tokens.add(new Token("2",TokenType.TERM));
        q.tokens.add(new Token(")",TokenType.CLOSE_PAREN));
        q.tokens.add(new Token("AND",TokenType.AND_OP));
        q.tokens.add(new Token("(",TokenType.OPEN_PAREN));
        q.tokens.add(new Token("2",TokenType.TERM));
        q.tokens.add(new Token("OR",TokenType.OR_OP));
        q.tokens.add(new Token("2",TokenType.TERM));
        q.tokens.add(new Token(")",TokenType.CLOSE_PAREN));
        q.tokens.add(new Token(")",TokenType.CLOSE_PAREN));
        
        q.tokens.add(new Token("",TokenType.END));
        return q;
        
    }
    
}
