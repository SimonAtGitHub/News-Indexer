/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.buffalo.cse.irf14.util.text;

import edu.buffalo.cse.irf14.constants.ParserRegEx;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author san
 */
public class StringManipulator {

    private static Pattern p_anyLowerCase = Pattern.compile("[a-z]");
    private static Matcher m_anyLowerCase = p_anyLowerCase.matcher(" ");
    
    private static Pattern p_camelCase = Pattern.compile("^[A-Z][a-z][a-z0-9[\\p{Punct}]]*$");
    private static Matcher m_camelCase = p_camelCase.matcher(" ");
    
    /**
     * This method will return the matching string say "Author", "Place", Date
     * from the input StringBuilder It will also remove the found matching
     * referenced in the StringBuilder
     * @param input
     * @param regexPatFind
     * @param groupid
     * @return
     */
    public static String regexStringReturn(StringBuilder input, Pattern regexPatFind, int groupid) {
        Matcher matcher;
        String output = "";

        if (input != null && input.length() > 0) {

            matcher = regexPatFind.matcher(input);
            if (matcher.find()) {
                //This line pastes the matching pattern content to the output String
                output = matcher.group(groupid);
            }
        }

        return output;
    }

    public static boolean isEmptyLine(StringBuilder input, Pattern regexPatFind) {
        boolean output = false;

        output = regexPatFind.matcher(input).matches();

        return output;
    }

    /**
     * out[1] --> Has String[1]:      is the Org name - if present, 
     *                                if none - then has empty string.
     * out[2] --> Has String[1 to 2]: is/are the Author name(s) - if present, 
     *                                if none - then returns null 
     *                                if using - always check for null.
     * @param authorTagLine
     * @return 
     */
    public static String[][] parseAuthorTag(StringBuilder inAuthorTagLine) {
        String[][] out = new String[2][];
        String authorTagLine = ""; 
        //Author tag is cleaned and inner content assigned ignoring tag and by
        authorTagLine = regexStringReturn(inAuthorTagLine, ParserRegEx.AUTHOR_TAG_CONTENT, 4);

        int indexOfCommaB4Org = authorTagLine.lastIndexOf(',');
        int lengthOfContent = authorTagLine.length();
        String [] org = {""};
        
        //Sets Org if present
        if (indexOfCommaB4Org > 0) {
            //Org is present is extracted
            org[0] = safeTrim(authorTagLine.substring(indexOfCommaB4Org + 1, lengthOfContent));
            out[0] = org;
            //Org if present is cleaned from the text
            authorTagLine = authorTagLine.substring(0, indexOfCommaB4Org);
        }

        //Sets Author
        out[1] = safeArrayTrim(authorTagLine.split("(?i)(\\s+and\\s+)"));
        return out;
    }
    
    public static boolean isAuthorTag(StringBuilder inAuthorTagLine) {
        boolean out = false;
    	Matcher matcher;
        
        if (inAuthorTagLine != null && inAuthorTagLine.length() > 0) {
            matcher = ParserRegEx.IS_AUTHOR_TAG.matcher(inAuthorTagLine);
            out = matcher.matches();
        }

        return out;
    }

    //Null and Empty String safe Trim
    public static String safeTrim(String in) {
        String out = null;
        
        if(in != null && !in.isEmpty()){
            out = in.trim();
        } else {
            out = in;
        }
        
        return out;
    }

    //Null and Empty String safe Trim
    public static String[] safeArrayTrim(String [] in) {
        String [] out = null;
        
        if(in != null){
            out = new String [in.length];
            for(int i = 0, size = in.length; i < size; i++){
                out[i] = safeTrim(in[i]);
            }
        }
        
        return out;
    }
    
    public static boolean isAllUpperCase_safe(String s){
        boolean out = true;
        if(s == null){
            return false;
        }
        
        m_anyLowerCase.reset(s);
        if(m_anyLowerCase.find()){
            return false;
        }
        return out;
    }
    
    public static boolean isCamelCase_safe(String s){
        boolean out = false;
        if(s != null && !s.isEmpty()){
            m_camelCase.reset(s);
            if(m_camelCase.matches()){
                return true;
            }
        }
        return out;
    }
    
    //Padded with '|'
    public static String getIntCodeForString(String s){
        
        if(s.isEmpty())
            return "|00000|";
        
        StringBuilder sb = new StringBuilder();
        char[] sCh = s.toCharArray();
        for(char t : sCh){
            int val = (int) t;
            sb.append(val).append('|');
        }
        return "|" + sb.toString() + "|";
    }
    
    public static void main(String[] args) {
        /*
        System.out.println(safeTrim("  -   XYZ  "));
        safeArrayTrim(new String[3]);
        safeArrayTrim(new String[]{"",""});
        safeArrayTrim(null);
        safeArrayTrim(new String[]{"XYZ",""});
        safeArrayTrim(new String[]{"XYZ:","   "});
        safeArrayTrim(new String[]{" ABC:"," cam  "});
        */
        parseAuthorTag(new StringBuilder("<AUTHOR>    By Simon Cox, Reuters</AUTHOR>"));
    }
}
