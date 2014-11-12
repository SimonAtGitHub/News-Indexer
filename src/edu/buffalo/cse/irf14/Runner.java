/**
 * 
 */
package edu.buffalo.cse.irf14;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
//import java.util.Date;



import edu.buffalo.cse.irf14.document.Document;
//import edu.buffalo.cse.irf14.document.FieldNames;
import edu.buffalo.cse.irf14.document.Parser;
import edu.buffalo.cse.irf14.document.ParserException;
import edu.buffalo.cse.irf14.index.IndexWriter;
import edu.buffalo.cse.irf14.index.IndexerException;

/**
 * @author nikhillo
 *
 */
public class Runner {

	/**
	 * 
	 */
	public Runner() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		System.out.println("Start Time: " + new Date().toString());
		String ipDir = args[0];
		String indexDir = args[1];
		//more? idk!
		
		File ipDirectory = new File(ipDir);
		String[] catDirectories = ipDirectory.list();
		
		String[] files;
		File dir;
		
		Document d = null;
		IndexWriter writer = new IndexWriter(indexDir);
		//Below is Temp collection. Delete for full implementation
		ArrayList <Document> docCol = new ArrayList<Document>();
		
		try {
			for (String cat : catDirectories) {
				dir = new File(ipDir+ File.separator+ cat);
				files = dir.list();
				
				if (files == null)
					continue;
				
				for (String f : files) {
					try {
                                                //System.out.println("File being Parsed: \n" + "cat : " + cat + "-" +f);
						d = Parser.parse(dir.getAbsolutePath() + File.separator +f);
						writer.addDocument(d);
						//Below is Temp collection. Delete for full implementation
						docCol.add(d);
						
					} catch (ParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}
				
			}
			//System.out.println("Final collection size: " + docCol.size());
			//System.out.println("Random Print: \nFile Name: " + docCol.get(0000005).getField(FieldNames.FILEID)[0] + " " + docCol.get(11400).getField(FieldNames.CATEGORY)[0] + "\n Content:\n" + docCol.get(11400).getField(FieldNames.CONTENT)[0] );
			writer.close();
			PrintStream stream = null;
			try {
				stream = new PrintStream(new File(indexDir + File.separator + "output.txt"));
			}catch (Exception e){
				e.printStackTrace();
			}
			SearchRunner q = new SearchRunner(indexDir, ipDir, 'q',stream);
//			q.getCorrections();
			q.query(new File(indexDir + File.separator + "EvaluationMode.txt"));
		} catch (IndexerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println("End Time: " + new Date().toString());
	}

}
