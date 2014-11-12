/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.buffalo.cse.irf14.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author san
 */
public class DateTokenFilter extends TokenFilter {

    private static HashMap<String, String> monthValues = new HashMap<String, String>();

    //Use find()
    private static Pattern MAY_BE_DATE_TIME = Pattern.compile("^(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec|[0-9])");

    //Group 1 gives time with colon seperated, Group 4 gives AM/PM (Ignore case)
    private static Pattern MAY_BE_TIME = Pattern.compile("^(\\d{1,2}(:\\d{1,2}){0,1}(:\\d{1,2}){0,1})(AM|PM)?[,?!.-:;]*$", Pattern.CASE_INSENSITIVE);

    //Group 1 gives AM/PM (Ignore case), Group 2 gives extra content left over 
    private static Pattern AM_PM_TIME = Pattern.compile("^(AM|PM)+(\\W*)$", Pattern.CASE_INSENSITIVE);

    //Group 1 gives First 4digit year, Group 2 gives second 2/4 digit year 
    private static Pattern IS_YEAR_RANGE = Pattern.compile("^(\\d{4})-(\\d{2,4})(\\W*)(?:A\\.?D\\.?)?\\W*$");

    //Group 1 gives year (remove commas if any)
    //Group 2 BC or AD (remove periods if any)
    //Group 3 gives remaining extra special chars to append
    private static Pattern IS_BC_AD_YEAR = Pattern.compile("^(\\d{1,3},?\\d{1,3})\\W*((?:A\\.?D)|(?:B\\.?C)){1}(\\W*)$");

    //Use find() and not matches()
    //Group 1 gives year (remove commas if any)
    //Group 2 BC or AD (remove periods if any)
    //Group 3 gives remaining extra special chars to append
    private static Pattern IS_BC_AD_YEAR_WithExtraTokens = Pattern.compile("^(\\d{1,3},?\\d{1,3})\\W* ((?:A\\.?D)|(?:B\\.?C)){1}(\\W*)");

    //Use matches()
    //Logic pivots based on (Group 5 = or != null) and (Group 11 = or != null). Refer in code for exact group# usage
    //As this accomodates DD_MMMMM and DD_MMMMM_YYYY
    private static Pattern IS_DD_MMMMM_YYYY_WithExtraTokens = Pattern.compile("(^(\\d{1,2}) ((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec){1}[a-z]{0,6})(\\p{Punct}*)(?: (\\d{4})(\\p{Punct}*))?$)|(^(\\d{1,2}) ((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec){1}[a-z]{0,6})(\\p{Punct}*)(.*)$)");

    //Use matches()
    //Logic pivots based on (Group 5 = or != null) and (Group 11 = or != null). Refer in code for exact group# usage
    //As this accomodates MMMM_DD and MMMMM_DD_YYYY
    private static Pattern IS_MMMMM_DD_YYYY_WithExtraTokens = Pattern.compile("(^((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec){1}[a-z]{0,6}) (\\d{1,2})(\\p{Punct}*)(?: (\\d{4})(\\p{Punct}*))?$)|(^((?: Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec){1}[a-z]{0,6}) (\\d{1,2})(\\p{Punct}*)( .*)$)");

    //Initializes the static Accent and replacement list.
    static {
        monthValuePopulator();
    }

    private class DateContent {

        //boolean isValidDateOrTime = false;
//        boolean isDate = false;
//        boolean isTime = false;
//        
        String date = "01";
        String month = "01";
        String year = "1900";
        String year2 = "1900";
        String ADBC = "AD";

        boolean isDateExtractionComplete = false;
        boolean isYearRange = false;
        boolean isDateExtractionWIP = false;

        private void formatDateToPrint() {
            if (date.length() < 2) {
                date = "0" + date;
            }

            if (month.length() < 2) {
                month = "0" + month;
            }

            int length = year.length();
            while (length < 4) {
                year = "0" + year;
                length++;
            }

        }

        //Ex: year = 2014 & year2 = 16 ==> year2 = 2016
        private void formatYearRange() {
            if (year2.length() == 2) {
                year2 = year.substring(0, 2) + year2;
            }
        }

        @Override
        public String toString() {
            StringBuilder out = new StringBuilder();
            formatDateToPrint();
            if (isYearRange) {
                formatYearRange();
                out.append(year).append(month).append(date).append("-").append(year2).append(month).append(date);
            } else {
                if (ADBC.equalsIgnoreCase("BC")) {
                    out.append("-");
                }
                out.append(year).append(month).append(date);
            }

            return new String(out);
        }

    }

    private class TimeContent {

        String hour = "00";
        String min = "00";
        String sec = "00";
        String AM_PM = "";
        boolean isTimeExtractionComplete = false;

        private void convertTimeto24Hrs() {
            if (AM_PM.equalsIgnoreCase("PM")) {
                int temp_hr = Integer.parseInt(hour);
                if (temp_hr < 12) {
                    hour = String.valueOf(temp_hr + 12);
                }
            }
        }

        private boolean stringToTimeBuilder(String time) {
            boolean isValidTime = false;
            String[] timeArray = time.split(":");
            switch (timeArray.length) {
                case 1:
                    hour = timeArray[0];
                    isValidTime = true;
                    break;
                case 2:
                    hour = timeArray[0];
                    min = timeArray[1];
                    isValidTime = true;
                    break;
                case 3:
                    hour = timeArray[0];
                    min = timeArray[1];
                    sec = timeArray[2];
                    isValidTime = true;
                    break;
                case 0:
                default:
                    break;
            }
            return isValidTime;
        }

        private void formatTimeTo2Digits() {
            if (hour.length() < 2) {
                hour = "0" + hour;
            }

            if (min.length() < 2) {
                min = "0" + min;
            }

            if (sec.length() < 2) {
                sec = "0" + sec;
            }
        }

        @Override
        public String toString() {
            convertTimeto24Hrs();
            formatTimeTo2Digits();

            return hour + ":" + min + ":" + sec;
        }

    }

    /**
     * @param stream
     */
    public DateTokenFilter(TokenStream stream) {
        super(stream);
    }

    /* (non-Javadoc)
     * @see edu.buffalo.cse.irf14.analysis.Analyzer#increment()
     */
    @Override
    public boolean increment() throws TokenizerException {
        // TODO Auto-generated method stub
        boolean out = false;
        
        /*
         if(!out)
         return out;
        */
         
        try {
            //Write the unit business process for cleaning a single token.
            Matcher matcher_MAY_BE_DATE_TIME;
            Matcher matcher_MAY_BE_TIME;
            if (getStream().hasNext()) {

                Token wip = getStream().next();
                String currTokenStringWIP = wip.toString();
                matcher_MAY_BE_DATE_TIME = MAY_BE_DATE_TIME.matcher(currTokenStringWIP);

                if (matcher_MAY_BE_DATE_TIME.find()) {
                    
                    matcher_MAY_BE_TIME = MAY_BE_TIME.matcher(currTokenStringWIP);
                    TimeContent timeContent = new TimeContent();
                    DateContent dateContent = new DateContent();
                    String extraDataAtEndToAppend = "";
                    boolean isCurrTokenProcComplete = false;
                    boolean isProccComplWithExtraTokens = false;
                    int countExtraTokensRead = 0;

                    if (matcher_MAY_BE_TIME.matches()) {

                        String extractOptAMPMInfo = matcher_MAY_BE_TIME.group(4);
                        if (extractOptAMPMInfo != null
                                && (extractOptAMPMInfo.equalsIgnoreCase("AM") || extractOptAMPMInfo.equalsIgnoreCase("PM"))) {
                        //At this case the given token itself is a valid Time.
                            //TODO: Sankar - populate date object and check if Date is available forward.
                            String timeDate = matcher_MAY_BE_TIME.group(1);
                            timeContent.stringToTimeBuilder(timeDate);
                            timeContent.AM_PM = extractOptAMPMInfo.toUpperCase();
                            timeContent.isTimeExtractionComplete = true;
                            isCurrTokenProcComplete = true;

                        //The below two lines will cache any extra content after AM/PM
                            //Which needs to be preserved in the Token
                            String[] tempString = currTokenStringWIP.split(extractOptAMPMInfo, 2);
                            if (tempString.length > 1) {
                                extraDataAtEndToAppend = tempString[1];
                            }

                        } else {
                            //This could be a time, but read the next Token to confirm if its a real time.

                            ArrayList<Token> extraTokensRead = getStream().getExtraTokensCopy(1);

                            if (extraTokensRead.size() == 1) {
                                Token a = extraTokensRead.get(0);
                                String extraTokenStringWIP = a.toString();
                                Matcher matcher_AM_PM_TIME = AM_PM_TIME.matcher(extraTokenStringWIP);

                                if (matcher_AM_PM_TIME.matches()) {
                                    //Two tokens together make a valid time, hence code reached here
                                    String timeDate = matcher_MAY_BE_TIME.group(1);
                                    timeContent.stringToTimeBuilder(timeDate);
                                    timeContent.AM_PM = matcher_AM_PM_TIME.group(1).toUpperCase();
                                    timeContent.isTimeExtractionComplete = true;
                                    isCurrTokenProcComplete = true;
                                    countExtraTokensRead = 1;

                                //The below line will cache any extra content after AM/PM
                                    //Which needs to be preserved in the Token
                                    extraDataAtEndToAppend = matcher_AM_PM_TIME.group(2);

                                }

                            }

                        }

                    }

                    if (!isCurrTokenProcComplete) {
                        /*
                         Once a token enters here, this could possibly be a date content.
                         Hence next 2 more tokens will be retrieved to assert if its a valid date content or not.
                         Possible date contents are as below:
                         1 January 1978    --> 19780101
                         December 7, 1941, --> 19411207
                         84 BC		--> -00840101
                         1948		--> 19480101
                         10:15 am.		--> 10:15:00.
                         January 30, 1948 	--> 19480130
                         5:15PM.		--> 17:15:00.
                         847AD.		--> 08470101.
                         April 11		--> 19000411
                         2011-12.		--> 20110101-20120101.
                         */

                        //Is the given token an Year Range?
                        {
                            Matcher matcher_IS_YEAR_RANGE = IS_YEAR_RANGE.matcher(currTokenStringWIP);
                            if (matcher_IS_YEAR_RANGE.matches()) {
                                dateContent.year = matcher_IS_YEAR_RANGE.group(1); //First 4 digit year
                                dateContent.year2 = matcher_IS_YEAR_RANGE.group(2); //Second 2/4 digit year
                                dateContent.isYearRange = true;
                                dateContent.isDateExtractionComplete = true;
                                isCurrTokenProcComplete = true;
                                extraDataAtEndToAppend = matcher_IS_YEAR_RANGE.group(3);
                            }
                        }

                        //Is the given token an year with AD/BC
                        {
                            if (!isCurrTokenProcComplete && !dateContent.isDateExtractionWIP) {
                                Matcher matcher_IS_BC_AD_YEAR = IS_BC_AD_YEAR.matcher(currTokenStringWIP);
                                if (matcher_IS_BC_AD_YEAR.matches()) {
                                    //TODOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
                                    dateContent.year = matcher_IS_BC_AD_YEAR.group(1).replaceAll(",", ""); //year yet to remove comma
                                    dateContent.ADBC = matcher_IS_BC_AD_YEAR.group(2).replaceAll(".", ""); //AD or BC. yet to remove dots
                                    extraDataAtEndToAppend = matcher_IS_BC_AD_YEAR.group(3);
                                    dateContent.isDateExtractionComplete = true;
                                    isCurrTokenProcComplete = true;
                                }
                            }
                        }

                        //Is the given token an year with 4 digits?
                        {
                            if (!isCurrTokenProcComplete && !dateContent.isDateExtractionWIP) {
                                //Matcher matcher_IS_BC_AD_YEAR = IS_BC_AD_YEAR.matcher(currTokenStringWIP);
                                if (currTokenStringWIP.matches("^[1-2]\\d{3}$") && Integer.parseInt(currTokenStringWIP) < 3000) {
                                    //TODOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
                                    dateContent.year = currTokenStringWIP;
                                    dateContent.isDateExtractionComplete = true;
                                }
                            }
                        }

                        //Given single Token is insufficient to decide hence refer next 3 tokens to find it.                    
                        if (!isCurrTokenProcComplete) {

                            ArrayList<Token> extraTokensRead = getStream().getExtraTokensCopy(2);

                            //TODOOOOOOOOOOOOOOOOOOOOOOOOOOOOO IMP
                            String textToCheckWithExtractedTokens = currTokenStringWIP;
                            for (Token temp : extraTokensRead) {
                                if (temp.toString().isEmpty()) {
                                    textToCheckWithExtractedTokens = textToCheckWithExtractedTokens + " " + "XX";
                                } else {
                                    textToCheckWithExtractedTokens = textToCheckWithExtractedTokens + " " + temp;
                                }
                            }

                            //First check if an AD/BC Year can be constructed with the given first 2 tokens.
                            {
                                Matcher matchr_IS_BC_AD_YEAR_WithExtraTokens = IS_BC_AD_YEAR_WithExtraTokens.matcher(textToCheckWithExtractedTokens);

                                if (matchr_IS_BC_AD_YEAR_WithExtraTokens.find()) {
                                    dateContent.year = matchr_IS_BC_AD_YEAR_WithExtraTokens.group(1).replaceAll(",", ""); //year yet to remove comma
                                    dateContent.ADBC = matchr_IS_BC_AD_YEAR_WithExtraTokens.group(2).replaceAll("\\.", ""); //AD or BC. yet to remove dots
                                    extraDataAtEndToAppend = matchr_IS_BC_AD_YEAR_WithExtraTokens.group(3);
                                    dateContent.isDateExtractionComplete = true;
                                    countExtraTokensRead = 1;
                                    isProccComplWithExtraTokens = true;
                                }
                            }

                            //Second check if 3 tokens together make a date of format 1 January 1978
                            {
                                Matcher matchr_IS_DD_MMMMM_YYYY_WithExtraTokens = IS_DD_MMMMM_YYYY_WithExtraTokens.matcher(textToCheckWithExtractedTokens);

                                if (matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.matches()) {
                                    if ((matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.group(11) != null && monthValues.containsKey(matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.group(9)))
                                            || (matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.group(11) == null && monthValues.containsKey(matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.group(3)))) {
                                        //Its a valid date
                                        if (matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.group(11) != null) {
                                            //It is a date of format "30 January, XYZ" So consume only one extra token
                                            dateContent.date = matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.group(8); //Day
                                            dateContent.month = monthValues.get(matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.group(9)); //Month value say Jan --> 01, February --> 02
                                            extraDataAtEndToAppend = matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.group(10);
                                            dateContent.isDateExtractionComplete = true;
                                            countExtraTokensRead = 1;
                                            isProccComplWithExtraTokens = true;
                                        } else {
                                            //It is a date of could be of format "30 January, 1947" (or) "December 3,"
                                            dateContent.date = matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.group(2); //Day
                                            dateContent.month = monthValues.get(matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.group(3)); //Month value say Jan --> 01, February --> 02
                                            if (matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.group(5) != null) {
                                                //Date is of format "30 January, 1947" So consume two tokens
                                                dateContent.year = matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.group(5); //Year
                                                extraDataAtEndToAppend = matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.group(6);
                                                dateContent.isDateExtractionComplete = true;
                                                countExtraTokensRead = 2;
                                                isProccComplWithExtraTokens = true;
                                            } else {
                                                //Date is of format "30 January," So consume One token
                                                extraDataAtEndToAppend = matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.group(4);
                                                dateContent.isDateExtractionComplete = true;
                                                countExtraTokensRead = 1;
                                                isProccComplWithExtraTokens = true;
                                            }

                                        }
                                    } else {
                                    //Erroneous data found in First token say: Janitor 01 1982
                                        //So proceed to next token.
                                        isCurrTokenProcComplete = true;
                                    }

                                }

                                /*
                                 if (matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.find()) {
                                 if (monthValues.containsKey(matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.group(2))) {
                                 //Its a valid date
                                 dateContent.date = matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.group(1); //Day
                                 dateContent.month = monthValues.get(matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.group(2)); //Month value say Jan --> 01, February --> 02
                                 if (matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.group(5) == null) {
                                 // Date is of the format 1 Jan (or) 21 February.,%
                                 // Hence only 1 extra Token consumed

                                 extraDataAtEndToAppend = matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.group(3);
                                 dateContent.isDateExtractionComplete = true;
                                 countExtraTokensRead = 1;
                                 isProccComplWithExtraTokens = true;
                                 } else {
                                 // Date is of the format 1 Jan 1984 (or) 21 February 2014.,%
                                 // Hence only 2 extra Token consumed
                                 dateContent.year = matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.group(5);
                                 extraDataAtEndToAppend = matchr_IS_DD_MMMMM_YYYY_WithExtraTokens.group(6);
                                 dateContent.isDateExtractionComplete = true;
                                 countExtraTokensRead = 2;
                                 isProccComplWithExtraTokens = true;

                                 }

                                 } else {
                                 //Erroneous data found in second token say: Janitor
                                 //So proceed to next token.
                                 isCurrTokenProcComplete = true;
                                 }

                                 }*/
                            }

                            //Third check if 3 tokens together make a date of format January 30, 1948 (or) Apr 11.;: (or) etc
                            {
                                if (!isCurrTokenProcComplete) {

                                    Matcher matchr_IS_MMMMM_DD_YYYY_WithExtraTokens = IS_MMMMM_DD_YYYY_WithExtraTokens.matcher(textToCheckWithExtractedTokens);

                                    if (matchr_IS_MMMMM_DD_YYYY_WithExtraTokens.matches()) {
                                        if ((matchr_IS_MMMMM_DD_YYYY_WithExtraTokens.group(11) != null && monthValues.containsKey(matchr_IS_MMMMM_DD_YYYY_WithExtraTokens.group(8)))
                                                || (matchr_IS_MMMMM_DD_YYYY_WithExtraTokens.group(11) == null && monthValues.containsKey(matchr_IS_MMMMM_DD_YYYY_WithExtraTokens.group(2)))) {
                                            //Its a valid date
                                            if (matchr_IS_MMMMM_DD_YYYY_WithExtraTokens.group(11) != null) {
                                                //It is a date of format "January 30, XYZ" So consume only one extra token
                                                dateContent.date = matchr_IS_MMMMM_DD_YYYY_WithExtraTokens.group(9); //Day
                                                dateContent.month = monthValues.get(matchr_IS_MMMMM_DD_YYYY_WithExtraTokens.group(8)); //Month value say Jan --> 01, February --> 02
                                                extraDataAtEndToAppend = matchr_IS_MMMMM_DD_YYYY_WithExtraTokens.group(10);
                                                dateContent.isDateExtractionComplete = true;
                                                countExtraTokensRead = 1;
                                                isProccComplWithExtraTokens = true;
                                            } else {
                                                //It is a date of could be of format "January 30, 1947" (or) "January 30,"
                                                dateContent.date = matchr_IS_MMMMM_DD_YYYY_WithExtraTokens.group(3); //Day
                                                dateContent.month = monthValues.get(matchr_IS_MMMMM_DD_YYYY_WithExtraTokens.group(2)); //Month value say Jan --> 01, February --> 02
                                                if (matchr_IS_MMMMM_DD_YYYY_WithExtraTokens.group(5) != null) {
                                                    //Date is of format "January 30, 1947" So consume two tokens
                                                    dateContent.year = matchr_IS_MMMMM_DD_YYYY_WithExtraTokens.group(5); //Year
                                                    extraDataAtEndToAppend = matchr_IS_MMMMM_DD_YYYY_WithExtraTokens.group(6);
                                                    dateContent.isDateExtractionComplete = true;
                                                    countExtraTokensRead = 2;
                                                    isProccComplWithExtraTokens = true;
                                                } else {
                                                    //Date is of format "January 30," So consume One token
                                                    extraDataAtEndToAppend = matchr_IS_MMMMM_DD_YYYY_WithExtraTokens.group(6);
                                                    dateContent.isDateExtractionComplete = true;
                                                    countExtraTokensRead = 1;
                                                    isProccComplWithExtraTokens = true;
                                                }

                                            }
                                        } else {
                                        //Erroneous data found in First token say: Janitor 01 1982
                                            //So proceed to next token.
                                            isCurrTokenProcComplete = true;
                                        }

                                    }

                                }
                            }

                        }

                    }

                    /*
                     for(int i = 0, size = currTokenContent.length; i < size; i++){
                     char currChar = currTokenContent [i];
                     if (listOfAccents.containsKey(currChar)) {
                     currTokenStringWIP = currTokenStringWIP.replaceAll(String.valueOf(currChar), listOfAccents.get(currChar));
                     }
                     }
                
                     wip.setTermText(currTokenStringWIP);
                     */
                    //This is necessary otherwise if no special char at the end, it appends null as a string to the end
                    if (extraDataAtEndToAppend == null) {
                        extraDataAtEndToAppend = "";
                    }
                    switch (countExtraTokensRead) {
                        case 0: //No Extra Tokens Read to make a complete Date or Time
                            if (timeContent.isTimeExtractionComplete) {
                                wip.setTermText(timeContent.toString() + extraDataAtEndToAppend);
                                wip.setDateTime(true);
                            } else if (dateContent.isDateExtractionComplete) {
                                wip.setTermText(dateContent.toString() + extraDataAtEndToAppend);
                                wip.setDateTime(true);
                            }
                            break;
                        case 1: //One Extra Token Read to make a complete Date or Time
                            if (timeContent.isTimeExtractionComplete) {
                                wip.setTermText(timeContent.toString() + extraDataAtEndToAppend);
                                wip.setDateTime(true);
                            } else if (dateContent.isDateExtractionComplete) {
                                wip.setTermText(dateContent.toString() + extraDataAtEndToAppend);
                                wip.setDateTime(true);
                            }
                            break;
                        case 2: //Two Extra Tokens Read to make a complete Date or Time
                            if (timeContent.isTimeExtractionComplete) {
                                wip.setTermText(timeContent.toString() + extraDataAtEndToAppend);
                                wip.setDateTime(true);
                            } else if (dateContent.isDateExtractionComplete) {
                                wip.setTermText(dateContent.toString() + extraDataAtEndToAppend);
                                wip.setDateTime(true);
                            }
                            break;
                    }
//                if (countExtraTokensRead == 0) {
//                    //No Extra Tokens Read to make a complete Date or Time
//                    if (timeContent.isTimeExtractionComplete) {
//                        wip.setTermText(timeContent.toString() + extraDataAtEndToAppend);
//                    } else if (dateContent.isDateExtractionComplete) {
//                        wip.setTermText(dateContent.toString() + extraDataAtEndToAppend);
//                    }
//                }

                    //This will reset the pointer of the Token Stream for any additional copies of the Tokens consumed.
                    while (countExtraTokensRead > 0) {
                        getStream().next().setTermText("");
                        countExtraTokensRead--;
                    }

                    
                    matcher_MAY_BE_TIME.reset();
                }
                matcher_MAY_BE_DATE_TIME.reset();
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

    //TODO: Sankar - Remove this method
    private static ArrayList<Token> readExtraTokens(TokenStream baseTokenStream, int count) {
        ArrayList<Token> out = new ArrayList<Token>();

        if (baseTokenStream.hasNext()) {
            for (int i = 0; i < count; i++) {
                out.add(baseTokenStream.next());
            }
        }

        return out;
    }

    //Populates the list of accent characters which needs to be replaced in the Tokens
    private static void monthValuePopulator() {
        monthValues.put("Jan", "01");
        monthValues.put("Feb", "02");
        monthValues.put("Mar", "03");
        monthValues.put("Apr", "04");
        monthValues.put("May", "05");
        monthValues.put("Jun", "06");
        monthValues.put("Jul", "07");
        monthValues.put("Aug", "08");
        monthValues.put("Sep", "09");
        monthValues.put("Oct", "10");
        monthValues.put("Nov", "11");
        monthValues.put("Dec", "12");
        monthValues.put("January", "01");
        monthValues.put("February", "02");
        monthValues.put("March", "03");
        monthValues.put("April", "04");
        monthValues.put("May", "05");
        monthValues.put("June", "06");
        monthValues.put("July", "07");
        monthValues.put("August", "08");
        monthValues.put("September", "09");
        monthValues.put("October", "10");
        monthValues.put("November", "11");
        monthValues.put("December", "12");
    }

    private static boolean isYearRange(String input) {
        Matcher matcher_MAY_BE_TIME = IS_YEAR_RANGE.matcher(input);
        return matcher_MAY_BE_TIME.matches();
    }

    //TODO: Sankar: REMOVE THE BELOW TWO METHODS FOR PROD
    //**********************************************************REMOVE THE BELOW TWO METHODS FOR PROD***********************************************
    //**********************************************************REMOVE THE BELOW TWO METHODS FOR PROD***********************************************
    //**********************************************************REMOVE THE BELOW TWO METHODS FOR PROD***********************************************
    public static void main(String[] args) throws TokenizerException {

        TokenFilter test = new DateTokenFilter(null);

//        "Vidya Balan born 1 January " + "1978 is an Indian actress."
        String rv[] = ((DateTokenFilter) test).runTest(TokenFilterType.DATE, "1 January 1978");
        System.out.println("\n");
        for (String i : rv) {
            System.out.print("\"" + i + "\", ");
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

                if (s != null && !s.isEmpty()) {
                    list.add(s);
                }
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
