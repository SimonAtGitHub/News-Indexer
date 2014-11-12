/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.buffalo.cse.irf14.constants;

import java.util.regex.Pattern;

/**
 *
 * @author san
 */
public class ParserRegEx {
    
    /**
     * Assumption: 
     * ===========
     * STARTS with Line Start ^ (to the beginning)
     * Followed by - 0 or more space (or) tab (or) any white space character
     * Followed by - First three characters of date spelled correct Case Insensitive
     * Followed by - 0 to 6 alphabets. (As September being the longest with 9 chars and first 3 char is taken care above
     * Followed by - 1 or more space (or) tab (or) any white space character
     * Followed by - 1 to 2    digits to represent date.
     * Followed by - 0 or more space (or) tab (or) any white space character
     * ENDS with 1 dash "-" (to the end)
     * Pass Cases :: ", Jan  02", ",Febru 27-", ",   AprIL     2    -"
     * Return     :: Group 2
     */        
    public static final Pattern DATE = Pattern.compile(",(\\s*((?i)(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec){1}[a-zA-Z]{0,6}\\s+\\d{1,2}))\\s*-");
    
    
    /**
     * Assumption: 
     * ===========
     * STARTS with Line Start ^ (to the beginning)
     * Followed by - 0 or more space (or) tab (or) any white space character
     * Followed by - 1 or more characters (a-zA-Z) (0-1) (Special Chars) (White Space) Excluding comma
     * Followed by - 1 comma
     * Followed by - 0 or 1 combination of (Followed by - 0 or more characters (a-zA-Z) (0-1) (Special Chars) (White Space) Excluding comma AND Followed by - 1 comma)
     * Followed by - 1 or more space (or) tab (or) any white space character)
     * Followed by - 0 or more any characters
     * ENDS with dash (-) (to the end)
     * Pass Cases :: "     COMMACK, N.Y., Feb 21 - ", "N.Y.C., CT., -", "     New Jersey, - "
     * Return     :: Group 2
     */       
//    public static final Pattern PLACE = Pattern.compile("^(\\s*((.[^,])+,{1}((.[^,])*,{1})?)\\s+).*\\d{1,2}\\s*-");
    public static final Pattern PLACE = Pattern.compile("^(\\s*((.[^,])+,{1}((.[^,])*,{1})?)\\s+).*-");
    
    
    public static final Pattern EMPTYLINE = Pattern.compile("^\\s*$");
    
    /**
     * Assumption: 
     * ===========
     * STARTS with dash (-)
     * Followed by - 0 or more characters (Can be any char including white space and alphanumeric and special char)
     * ENDS Line ending
     * Pass Cases :: " -   asdasd"
     * Return     :: Group 1
     */   
    public static final Pattern REMAINING_TEXT_FIRSTLINE = Pattern.compile("-(.*)$");
    
    
    //Regex for extracting Author tag context after removing the tag and by
    //Return     :: Group 4
    public static final Pattern AUTHOR_TAG_CONTENT = Pattern.compile("^(?i)(\\s*(<AUTHOR>){1}\\s*(by){1}\\s*(.*)\\s*(</AUTHOR>){1}\\s*)$");
    
    public static final Pattern IS_AUTHOR_TAG = Pattern.compile("^(?i)(\\s*(<AUTHOR>){1}(.*)(</AUTHOR>){1}\\s*)$");
}
