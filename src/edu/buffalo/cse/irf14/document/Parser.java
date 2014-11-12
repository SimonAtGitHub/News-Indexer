/**
 *
 */
package edu.buffalo.cse.irf14.document;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

import edu.buffalo.cse.irf14.constants.ParserRegEx;
import edu.buffalo.cse.irf14.util.text.StringManipulator;

/**
 * @author nikhillo Class that parses a given file into a Document
 */
public class Parser {

    public static enum Tags {

        TITLE, AUTHOR, PLACEDATE, CONTENT
    }

    /**
     * Static method to parse the given file into the Document object
     *
     * @param filename : The fully qualified filename to be parsed
     * @return The parsed and fully loaded Document object
     * @throws ParserException In case any error occurs during parsing
     */
    public static Document parse(String filename) throws ParserException {
        // TODO YOU MUST IMPLEMENT THIS
        ArrayList<StringBuilder> data = new ArrayList<StringBuilder>();
        Document docObj = null;
        BufferedReader buf = null;

        try {

 //           if (filename != null && !filename.isEmpty()) {
            try {

                buf = new BufferedReader(new FileReader(filename));
                String line = null;

                while ((line = buf.readLine()) != null) {
                    data.add(new StringBuilder(line));
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

            docObj = docCreator(data, filename);
            /*            
             } else {
               
             throw new Exception("Invalid File Name");
             }
             */

        } catch (Exception e) {
            throw new ParserException(e);
        }
        return docObj;
    }

    public static Document docCreator(ArrayList<StringBuilder> contentList, String filename) throws Exception {
        Tags tag = Tags.TITLE;
        boolean titleStarted = false;
        StringBuilder title = new StringBuilder();
        String[][] authors = new String[2][];
        String place = "";
        String date = "";
        StringBuilder content = new StringBuilder("");
        Document docObj = new Document();

        for (int index = 0, size = contentList.size(); index < size; index++) {
            switch (tag) {
                //Extract Until Title is complete
                case TITLE:
                    if (!titleStarted && contentList.get(index).toString().equals("")) {
                        continue;
                    } else if (contentList.get(index).toString().equals("")) {
                        tag = Tags.AUTHOR;
                    } else {
                        titleStarted = true;
                        title.append(contentList.get(index));
                    }
                    break;
                //Extract Author
                case AUTHOR:
                    if (StringManipulator.isAuthorTag(contentList.get(index))) {
                        //It is an author tag. So extracting it
                        authors = StringManipulator.parseAuthorTag(contentList.get(index));
                        tag = Tags.PLACEDATE;
                        break;
                    }
                //Extract Place
                case PLACEDATE:
                    place = placeCleanUp(StringManipulator.regexStringReturn(contentList.get(index), ParserRegEx.PLACE, 2));
                    date = StringManipulator.safeTrim( StringManipulator.regexStringReturn(contentList.get(index), ParserRegEx.DATE, 1) );
                    content = new StringBuilder(StringManipulator.regexStringReturn(contentList.get(index), ParserRegEx.REMAINING_TEXT_FIRSTLINE, 1));
                    tag = Tags.CONTENT;
                    break;
                //Extract remaining content
                case CONTENT:
                    content.append(" " + contentList.get(index));
                    break;
                default:
                    throw new IllegalStateException();
            }
        }

        String[] token = filename.split(Pattern.quote(File.separator));
        docObj.setField(FieldNames.FILEID, token[token.length - 1]);
        docObj.setField(FieldNames.CATEGORY, token[token.length - 2]);
        docObj.setField(FieldNames.TITLE, title.toString());
        docObj.setField(FieldNames.AUTHOR, authors[1]);
        docObj.setField(FieldNames.AUTHORORG, authors[0]);
        docObj.setField(FieldNames.PLACE, place);
        docObj.setField(FieldNames.NEWSDATE, date);
        docObj.setField(FieldNames.CONTENT, content.toString());

        return docObj;
    }

    private static String placeCleanUp(String in) {
        String out = null;

        if (in != null) {
            if (!in.isEmpty()) {
                in = in.trim();
                char lastChar = in.charAt(in.length() - 1);
                if (lastChar == ',') {
                    out = in.substring(0, in.length() - 1);
                } else {
                    out = in;
                }
            } else {
                out = in;
            }

        }

        return out;
    }

    public static void main(String[] args) {
        System.out.println(placeCleanUp("     COMMACK, N.Y.,"));
    }

}
